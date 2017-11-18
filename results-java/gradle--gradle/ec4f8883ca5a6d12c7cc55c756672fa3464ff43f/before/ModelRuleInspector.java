/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.model.internal.inspect;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import org.gradle.internal.Factory;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.reflect.JavaMethod;
import org.gradle.internal.reflect.JavaReflectionUtil;
import org.gradle.model.*;
import org.gradle.model.internal.core.ModelPath;
import org.gradle.model.internal.core.ModelReference;
import org.gradle.model.internal.core.ModelType;
import org.gradle.model.internal.core.rule.Inputs;
import org.gradle.model.internal.core.rule.ModelCreator;
import org.gradle.model.internal.core.rule.describe.MethodModelRuleSourceDescriptor;
import org.gradle.model.internal.core.rule.describe.ModelRuleSourceDescriptor;
import org.gradle.model.internal.registry.ModelRegistry;
import org.gradle.model.internal.registry.ReflectiveRule;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Set;

public class ModelRuleInspector {

    private final static Set<Class<? extends Annotation>> TYPE_ANNOTATIONS = ImmutableSet.of(Model.class, Mutate.class, Finalize.class);

    // TODO return a richer data structure that provides meta data about how the source was found, for use is diagnostics
    public Set<Class<?>> getDeclaredSources(Class<?> container) {
        Class<?>[] declaredClasses = container.getDeclaredClasses();
        if (declaredClasses.length == 0) {
            return Collections.emptySet();
        } else {
            ImmutableSet.Builder<Class<?>> found = ImmutableSet.builder();
            for (Class<?> declaredClass : declaredClasses) {
                if (declaredClass.isAnnotationPresent(RuleSource.class)) {
                    found.add(declaredClass);
                }
            }

            return found.build();
        }
    }

    // TODO should either return the extracted rule, or metadata about the extraction (i.e. for reporting etc.)
    public <T> void inspect(Class<T> source, ModelRegistry modelRegistry) {
        validate(source);
        Method[] methods = source.getDeclaredMethods();
        for (Method method : methods) {
            Annotation annotation = getTypeAnnotation(method);
            if (annotation != null) {
                if (annotation instanceof Model) {
                    creationMethod(modelRegistry, method, (Model) annotation);
                } else if (annotation instanceof Mutate) {
                    mutationMethod(modelRegistry, method, false);
                } else if (annotation instanceof Finalize) {
                    mutationMethod(modelRegistry, method, true);
                } else {
                    throw new IllegalStateException("Unhandled rule type annotation: " + annotation);
                }
            }
        }
    }

    private void mutationMethod(ModelRegistry modelRegistry, final Method method, boolean finalize) {
        if (method.getTypeParameters().length > 0) {
            throw invalid("model rule", method, "cannot have type variables (i.e. cannot be a generic method)");
        }

        ReflectiveRule.rule(modelRegistry, method, finalize, new Factory<Object>() {
            public Object create() {
                return toInstance(method.getDeclaringClass());
            }
        });
    }

    private Annotation getTypeAnnotation(Method method) {
        Annotation annotation = null;
        for (Annotation declaredAnnotation : method.getDeclaredAnnotations()) {
            if (TYPE_ANNOTATIONS.contains(declaredAnnotation.annotationType())) {
                if (annotation == null) {
                    annotation = declaredAnnotation;
                } else {
                    throw invalid("model rule", method, "can only be annotated with one of " + typeAnnotationsDescription());
                }
            }
        }

        return annotation;
    }

    private static String typeAnnotationsDescription() {
        String desc = Joiner.on(", ").join(Iterables.transform(TYPE_ANNOTATIONS, new Function<Class<? extends Annotation>, String>() {
            public String apply(Class<? extends Annotation> input) {
                return input.getName();
            }
        }));

        return "[" + desc + "]";
    }

    private void creationMethod(ModelRegistry modelRegistry, Method method, Model modelAnnotation) {
        if (method.getParameterTypes().length > 0) {
            throw new IllegalArgumentException("@Model rules cannot take arguments");
        }

        // TODO other validations on method: synthetic, bridge methods, varargs, abstract, native

        // TODO validate model name
        String modelName = determineModelName(modelAnnotation, method);

        if (method.getTypeParameters().length > 0) {
            throw invalid("model creation rule", method, "cannot have type variables (i.e. cannot be a generic method)");
        }

        // TODO validate the return type (generics?)

        TypeToken<?> returnType = TypeToken.of(method.getGenericReturnType());

        doRegisterCreation(method, returnType, modelName, modelRegistry);
    }

    private <T, R> void doRegisterCreation(final Method method, final TypeToken<R> returnType, final String modelName, ModelRegistry modelRegistry) {
        @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>) method.getDeclaringClass();
        @SuppressWarnings("unchecked") Class<R> returnTypeClass = (Class<R>) returnType.getRawType();
        final JavaMethod<T, R> methodWrapper = JavaReflectionUtil.method(clazz, returnTypeClass, method);

        modelRegistry.create(modelName, Collections.<String>emptyList(), new ModelCreator<R>() {

            public ModelReference<R> getReference() {
                return new ModelReference<R>(new ModelPath(modelName), new ModelType<R>(returnType));
            }

            public R create(Inputs inputs) {
                T instance = Modifier.isStatic(method.getModifiers()) ? null : toInstance(clazz);
                // ignore inputs, we know they're empty
                return methodWrapper.invoke(instance);
            }

            public ModelRuleSourceDescriptor getSourceDescriptor() {
                return new MethodModelRuleSourceDescriptor(method);
            }
        });
    }

    private static <T> T toInstance(Class<T> source) {
        try {
            return source.newInstance();
        } catch (InstantiationException e) {
            throw UncheckedException.throwAsUncheckedException(e);
        } catch (IllegalAccessException e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }

    private String determineModelName(Model modelAnnotation, Method method) {
        String annotationValue = modelAnnotation.value();
        if (annotationValue == null || annotationValue.isEmpty()) {
            return method.getName();
        } else {
            return annotationValue;
        }
    }

    /**
     * Validates that the given class is effectively static and has no instance state.
     *
     * @param source the class the validate
     */
    public void validate(Class<?> source) throws InvalidModelRuleDeclarationException {

        // TODO - exceptions thrown here should point to some extensive documentation on the concept of class rule sources

        int modifiers = source.getModifiers();

        if (Modifier.isInterface(modifiers)) {
            throw invalid(source, "must be a class, not an interface");
        }
        if (Modifier.isAbstract(modifiers)) {
            throw invalid(source, "class cannot be abstract");
        }

        if (source.getEnclosingClass() != null) {
            if (Modifier.isStatic(modifiers)) {
                if (Modifier.isPrivate(modifiers)) {
                    throw invalid(source, "class cannot be private");
                }
            } else {
                throw invalid(source, "enclosed classes must be static and non private");
            }
        }

        Class<?> superclass = source.getSuperclass();
        if (!superclass.equals(Object.class)) {
            throw invalid(source, "cannot have superclass");
        }

        Constructor<?>[] constructors = source.getDeclaredConstructors();
        if (constructors.length > 1) {
            throw invalid(source, "cannot have more than one constructor");
        }

        Constructor constructor = constructors[0];
        if (constructor.getParameterTypes().length > 0) {
            throw invalid(source, "constructor cannot take any arguments");
        }

        Field[] fields = source.getDeclaredFields();
        for (Field field : fields) {
            int fieldModifiers = field.getModifiers();
            if (!field.isSynthetic() && !(Modifier.isStatic(fieldModifiers) && Modifier.isFinal(fieldModifiers))) {
                throw invalid(source, "field " + field.getName() + " is not static final");
            }
        }

    }

    private static RuntimeException invalid(Class<?> source, String reason) {
        return new InvalidModelRuleDeclarationException("Type " + source.getName() + " is not a valid model rule source: " + reason);
    }

    private static RuntimeException invalid(String description, Method method, String reason) {
        StringBuilder sb = new StringBuilder();
        new MethodModelRuleSourceDescriptor(method).describeTo(sb);
        sb.append(" is not a valid ").append(description).append(": ").append(reason);
        return new InvalidModelRuleDeclarationException(sb.toString());
    }

}