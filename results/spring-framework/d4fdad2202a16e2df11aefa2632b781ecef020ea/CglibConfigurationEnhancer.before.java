/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.config.java.internal.enhancement;

import static java.lang.String.*;
import static org.springframework.config.java.Util.*;
import static org.springframework.util.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.model.BeanDefinitionRegistrar;
import org.springframework.config.java.model.ConfigurationClass;
import org.springframework.config.java.model.ConfigurationModel;
import org.springframework.config.java.model.ModelMethod;



/**
 * Enhances {@link Configuration} classes by generating a CGLIB subclass capable of
 * interacting with the Spring container to respect bean semantics.
 *
 * @see #enhance(String)
 *
 * @author Chris Beams
 */
public class CglibConfigurationEnhancer implements ConfigurationEnhancer {

    private static final Log log = LogFactory.getLog(CglibConfigurationEnhancer.class);

    private final ArrayList<Class<? extends Callback>> callbackTypes =
            new ArrayList<Class<? extends Callback>>();

    private final LinkedHashSet<BeanDefinitionRegistrar> handlers =
            new LinkedHashSet<BeanDefinitionRegistrar>();

    private final ArrayList<Callback> callbackInstances =
            new ArrayList<Callback>();

    private final CallbackFilter callbackFilter =
        new CallbackFilter() {
            public int accept(Method candidateMethod) {
                Iterator<BeanDefinitionRegistrar> iter = handlers.iterator();
                for(int i=0; iter.hasNext(); i++)
                    if(iter.next().accepts(candidateMethod))
                        return i;

                throw new IllegalStateException(format("No registered handler is capable of " +
                		"handling method [%s].  Perhaps you forgot to register a catch-all registrar?",
                		candidateMethod.getName()));
            }
        };


    /**
     * Creates a new {@link CglibConfigurationEnhancer} instance.
     */
    public CglibConfigurationEnhancer(DefaultListableBeanFactory beanFactory, ConfigurationModel model) {
        notNull(beanFactory, "beanFactory must be non-null");
        notNull(model, "model must be non-null");

        populateHandlersAndCallbacks(beanFactory, model);
    }


    /**
     * Reads the contents of {@code model} in order to populate {@link #handlers},
     * {@link #callbackInstances} and {@link #callbackTypes} appropriately.
     *
     * @see #callbackFilter
     */
    private void populateHandlersAndCallbacks(DefaultListableBeanFactory beanFactory, ConfigurationModel model) {

        for (ConfigurationClass configClass : model.getAllConfigurationClasses()) {
            for (ModelMethod method : configClass.getMethods()) {
                handlers.add(method.getRegistrar());

                Callback callback = method.getCallback();

                if(callback instanceof BeanFactoryAware)
                    ((BeanFactoryAware)callback).setBeanFactory(beanFactory);

                callbackInstances.add(callback);
            }
        }

        handlers.add(new InitializingBeanRegistrar());
        callbackInstances.add(new InitializingBeanCallback(beanFactory));

        // register a 'catch-all' registrar
        handlers.add(new BeanDefinitionRegistrar() {

            public boolean accepts(Method method) {
                return true;
            }

            public void register(ModelMethod method, BeanDefinitionRegistry registry) {
                // no-op
            }
        });
        callbackInstances.add(NoOp.INSTANCE);

        for(Callback callback : callbackInstances)
            callbackTypes.add(callback.getClass());
    }


    /**
     * Loads the specified class and generates a CGLIB subclass of it equipped with container-aware
     * callbacks capable of respecting scoping and other bean semantics.
     */
    public String enhance(String configClassName) {
        if (log.isInfoEnabled())
            log.info("Enhancing " + configClassName);

        Class<?> superclass = loadRequiredClass(configClassName);

        Class<?> subclass = createClass(newEnhancer(superclass), superclass);

        subclass = nestOneClassDeeperIfAspect(superclass, subclass);

        if (log.isInfoEnabled())
            log.info(format("Successfully enhanced %s; enhanced class name is: %s",
                            configClassName, subclass.getName()));

        return subclass.getName();
    }

    /**
     * Creates a new CGLIB {@link Enhancer} instance.
     */
    private Enhancer newEnhancer(Class<?> superclass) {
        Enhancer enhancer = new Enhancer();

        // because callbackFilter and callbackTypes are dynamically populated
        // there's no opportunity for caching. This does not appear to be causing
        // any performance problem.
        enhancer.setUseCache(false);

        enhancer.setSuperclass(superclass);
        enhancer.setInterfaces(new Class<?>[]{InitializingBean.class});
        enhancer.setUseFactory(false);
        enhancer.setCallbackFilter(callbackFilter);
        enhancer.setCallbackTypes(callbackTypes.toArray(new Class<?>[]{}));

        return enhancer;
    }

    /**
     * Uses enhancer to generate a subclass of superclass, ensuring that
     * {@link #callbackInstances} are registered for the new subclass.
     */
    private Class<?> createClass(Enhancer enhancer, Class<?> superclass) {
        Class<?> subclass = enhancer.createClass();

        // see #registerThreadLocalCleanupBeanDefinition
        Enhancer.registerCallbacks(subclass, callbackInstances.toArray(new Callback[] {}));
        //Enhancer.registerStaticCallbacks(subclass, callbackInstances.toArray(new Callback[] {}));

        return subclass;
    }

    /**
     * Works around a constraint imposed by the AspectJ 5 annotation-style programming model. See
     * comments inline for detail.
     *
     * @return original subclass instance unless superclass is annnotated with @Aspect, in which
     *          case a subclass of the subclass is returned
     */
    private Class<?> nestOneClassDeeperIfAspect(Class<?> superclass, Class<?> origSubclass) {
        boolean superclassIsAnAspect = false;

        // check for @Aspect by name rather than by class literal to avoid
        // requiring AspectJ as a runtime dependency.
        for(Annotation anno : superclass.getAnnotations())
            if(anno.annotationType().getName().equals("org.aspectj.lang.annotation.Aspect"))
                superclassIsAnAspect = true;

        if(!superclassIsAnAspect)
	        return origSubclass;

        // the superclass is annotated with AspectJ's @Aspect.
        // this means that we must create a subclass of the subclass
        // in order to avoid some guard logic in Spring core that disallows
        // extending a concrete aspect class.
        Enhancer enhancer = newEnhancer(origSubclass);
        enhancer.setStrategy(new DefaultGeneratorStrategy() {
            @Override
            protected byte[] transform(byte[] b) throws Exception {
                ClassWriter writer = new ClassWriter(false);
                ClassAdapter adapter =
                    new AddAnnotationAdapter(writer, "Lorg/aspectj/lang/annotation/Aspect;");
                ClassReader reader = new ClassReader(b);
                reader.accept(adapter, false);
                return writer.toByteArray();
            }
        });

        // create a subclass of the original subclass
        Class<?> newSubclass = createClass(enhancer, origSubclass);

        return newSubclass;
    }

}