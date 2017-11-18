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
package org.springframework.config.java.internal.parsing.asm;

import static org.springframework.config.java.Util.*;
import static org.springframework.config.java.internal.parsing.asm.MutableAnnotationUtils.*;
import static org.springframework.util.ClassUtils.*;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.springframework.config.java.Configuration;
import org.springframework.config.java.ConfigurationClass;
import org.springframework.config.java.ConfigurationModel;
import org.springframework.config.java.plugin.Extension;
import org.springframework.util.ClassUtils;


/**
 * Visits a {@link Configuration} class, populating a {@link ConfigurationClass} instance with
 * information gleaned along the way.
 *
 * @author Chris Beams
 */
class ConfigurationClassVisitor extends ClassAdapter {

    private static final Log log = LogFactory.getLog(ConfigurationClassVisitor.class);
    private static final String OBJECT_DESC = convertClassNameToResourcePath(Object.class.getName());

    private final ConfigurationClass configClass;
    private final ConfigurationModel model;
    private final HashMap<String, ConfigurationClass> innerClasses = new HashMap<String, ConfigurationClass>();

    private boolean processInnerClasses = true;

    public ConfigurationClassVisitor(ConfigurationClass configClass, ConfigurationModel model) {
        super(AsmUtils.EMPTY_VISITOR);
        this.configClass = configClass;
        this.model = model;
    }

    public void setProcessInnerClasses(boolean processInnerClasses) {
        this.processInnerClasses = processInnerClasses;
    }

    @Override
    public void visitSource(String sourceFile, String debug) {
        String resourcePath =
            convertClassNameToResourcePath(configClass.getName())
            .substring(0, configClass.getName().lastIndexOf('.')+1)
            .concat(sourceFile);

        configClass.setSource(resourcePath);
    }

    @Override
    public void visit(int classVersion, int modifiers, String classTypeDesc, String arg3,
                      String superTypeDesc, String[] arg5) {
        visitSuperType(superTypeDesc);

        configClass.setName(convertResourcePathToClassName(classTypeDesc));

        // ASM always adds ACC_SUPER to the opcodes/modifiers for class definitions.
        // Unknown as to why (JavaDoc is silent on the matter), but it should be
        // eliminated in order to comply with java.lang.reflect.Modifier values.
        configClass.setModifiers(modifiers - Opcodes.ACC_SUPER);
    }

    private void visitSuperType(String superTypeDesc) {
        // traverse up the type hierarchy unless the next ancestor is java.lang.Object
        if(OBJECT_DESC.equals(superTypeDesc))
            return;

        ConfigurationClassVisitor visitor = new ConfigurationClassVisitor(configClass, model);

        ClassReader reader =  AsmUtils.newClassReader(superTypeDesc);
        reader.accept(visitor, false);
    }

    /**
     * Visits a class level annotation on a {@link Configuration @Configuration} class.
     * Accounts for all possible class-level annotations that are respected by JavaConfig
     * including AspectJ's {@code @Aspect} annotation.
     * <p>
     * Upon encountering such an annotation, update the {@link #configClass} model object
     * appropriately, and then return an {@link AnnotationVisitor} implementation that can
     * populate the annotation appropriately with data.
     *
     * @see MutableAnnotation
     */
    @Override
    public AnnotationVisitor visitAnnotation(String annoTypeDesc, boolean visible) {
        String annoTypeName = AsmUtils.convertTypeDescriptorToClassName(annoTypeDesc);

        if (Configuration.class.getName().equals(annoTypeName)) {
            Configuration mutableConfiguration = createMutableAnnotation(Configuration.class);
            configClass.setMetadata(mutableConfiguration);
            return new MutableAnnotationVisitor(mutableConfiguration);
        }

        // TODO: re-enable for @Import support
//        if (Import.class.getName().equals(annoTypeName)) {
//            ImportStack importStack = ImportStackHolder.getImportStack();
//
//            if(importStack.contains(configClass))
//                throw new CircularImportException(configClass, importStack);
//
//            importStack.push(configClass);
//
//            return new ImportAnnotationVisitor(model);
//        }

        // -------------------------------------
        // Detect @Plugin annotations
        // -------------------------------------
        PluginAnnotationDetectingClassVisitor classVisitor = new PluginAnnotationDetectingClassVisitor();

        String className = AsmUtils.convertTypeDescriptorToClassName(annoTypeDesc);
        String resourcePath = ClassUtils.convertClassNameToResourcePath(className);
        ClassReader reader = AsmUtils.newClassReader(resourcePath);
        reader.accept(classVisitor, false);

        if(!classVisitor.hasPluginAnnotation())
            return super.visitAnnotation(annoTypeDesc, visible);

        Class<? extends Annotation> annoType = loadToolingSafeClass(annoTypeName);

        if(annoType == null)
            return super.visitAnnotation(annoTypeDesc, visible);

        Annotation pluginAnno = createMutableAnnotation(annoType);
        configClass.addPluginAnnotation(pluginAnno);
        return new MutableAnnotationVisitor(pluginAnno);
    }

    private static class PluginAnnotationDetectingClassVisitor extends ClassAdapter {
        private boolean hasPluginAnnotation = false;
        private final Extension pluginAnnotation = createMutableAnnotation(Extension.class);

        public PluginAnnotationDetectingClassVisitor() {
            super(AsmUtils.EMPTY_VISITOR);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String typeDesc, boolean arg1) {
            if(Extension.class.getName().equals(AsmUtils.convertTypeDescriptorToClassName(typeDesc))) {
                hasPluginAnnotation = true;
                return new MutableAnnotationVisitor(pluginAnnotation);
            }
            return super.visitAnnotation(typeDesc, arg1);
        }

        public boolean hasPluginAnnotation() {
            return hasPluginAnnotation;
        }

        public Extension getPluginAnnotation() {
            return pluginAnnotation;
        }
    }

    /**
     * Delegates all {@link Configuration @Configuration} class method parsing to
     * {@link ConfigurationClassMethodVisitor}.
     */
    @Override
    public MethodVisitor visitMethod(int modifiers, String methodName, String methodDescriptor,
                                     String arg3, String[] arg4) {

        return new ConfigurationClassMethodVisitor(configClass, methodName, methodDescriptor, modifiers);
    }

    /**
     * Implementation deals with inner classes here even though it would have
     * been more intuitive to deal with outer classes.  Due to limitations in ASM
     * (resulting from limitations in the VM spec) we cannot directly look for outer classes
     * in all cases, so instead build up a model of {@link #innerClasses} and process
     * declaring class logic in a kind of inverted manner.
     */
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if(processInnerClasses == false)
            return;

        String innerClassName = convertResourcePathToClassName(name);
        String configClassName = configClass.getName();

        // if the innerClassName is equal to configClassName, we just
        // ran into the outermost inner class look up the outer class
        // associated with this
        if(innerClassName.equals(configClassName)) {
            if(innerClasses.containsKey(outerName)) {
                configClass.setDeclaringClass(innerClasses.get(outerName));
            }
            return;
        }

        ConfigurationClass innerConfigClass = new ConfigurationClass();

        ConfigurationClassVisitor ccVisitor =
            new ConfigurationClassVisitor(innerConfigClass, new ConfigurationModel());
        ccVisitor.setProcessInnerClasses(false);

        ClassReader reader = AsmUtils.newClassReader(name);
        reader.accept(ccVisitor, false);

        if(innerClasses.containsKey(outerName))
            innerConfigClass.setDeclaringClass(innerClasses.get(outerName));

        // is the inner class a @Configuration class?  If so, add it to the list
        if(innerConfigClass.getMetadata() != null)
            innerClasses.put(name, innerConfigClass);
    }
}