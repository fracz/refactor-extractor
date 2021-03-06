/*
 * Copyright 2010-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.codegen;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.asm4.MethodVisitor;
import org.jetbrains.asm4.Type;
import org.jetbrains.asm4.commons.InstructionAdapter;
import org.jetbrains.jet.codegen.context.CodegenBinding;
import org.jetbrains.jet.codegen.context.CodegenContext;
import org.jetbrains.jet.codegen.state.GenerationState;
import org.jetbrains.jet.lang.descriptors.NamespaceDescriptor;
import org.jetbrains.jet.lang.descriptors.PropertyDescriptor;
import org.jetbrains.jet.lang.diagnostics.DiagnosticUtils;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.java.JvmAbi;
import org.jetbrains.jet.lang.resolve.java.JvmClassName;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.utils.Progress;

import java.io.File;
import java.util.Collection;

import static org.jetbrains.asm4.Opcodes.*;

/**
 * @author max
 */
public class NamespaceCodegen {
    @NotNull
    private final ClassBuilderOnDemand v;
    @NotNull private final FqName name;
    private final GenerationState state;
    private final Collection<JetFile> files;

    public NamespaceCodegen(
            @NotNull ClassBuilderOnDemand v,
            @NotNull final FqName fqName,
            GenerationState state,
            Collection<JetFile> namespaceFiles
    ) {
        checkAllFilesHaveSameNamespace(namespaceFiles);

        this.v = v;
        name = fqName;
        this.state = state;
        this.files = namespaceFiles;

        final PsiFile sourceFile = namespaceFiles.iterator().next().getContainingFile();

        v.addOptionalDeclaration(new ClassBuilderOnDemand.ClassBuilderCallback() {
            @Override
            public void doSomething(@NotNull ClassBuilder v) {
                v.defineClass(sourceFile, V1_6,
                              ACC_PUBLIC/*|ACC_SUPER*/,
                              getJVMClassNameForKotlinNs(fqName).getInternalName(),
                              null,
                              //"jet/lang/Namespace",
                              "java/lang/Object",
                              new String[0]
                );
                // TODO figure something out for a namespace that spans multiple files
                v.visitSource(sourceFile.getName(), null);
            }
        });
    }

    public void generate(CompilationErrorHandler errorHandler, final Progress progress) {
        boolean multiFile = CodegenBinding.isMultiFileNamespace(state.getBindingContext(), name);

        for (JetFile file : files) {
            VirtualFile vFile = file.getVirtualFile();
            try {
                final String path = vFile != null ? vFile.getPath() : "no_virtual_file/" + file.getName();
                if (progress != null) {
                    v.addOptionalDeclaration(new ClassBuilderOnDemand.ClassBuilderCallback() {
                        @Override
                        public void doSomething(@NotNull ClassBuilder classBuilder) {
                            progress.log("For source: " + path);
                        }
                    });
                }
                generate(file, multiFile);
            }
            catch (ProcessCanceledException e) {
                throw e;
            }
            catch (Throwable e) {
                if (errorHandler != null) errorHandler.reportException(e, vFile == null ? "no file" : vFile.getUrl());
                DiagnosticUtils.throwIfRunningOnServer(e);
                if (ApplicationManager.getApplication().isInternal()) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }
        }

        assert v.isActivated() == shouldGenerateNSClass(files) : "Different algorithms for generating namespace class and for heuristics";

        if (hasNonConstantPropertyInitializers()) {
            generateStaticInitializers();
        }
    }

    private void generate(JetFile file, boolean multiFile) {
        NamespaceDescriptor descriptor = state.getBindingContext().get(BindingContext.FILE_TO_NAMESPACE, file);
        for (JetDeclaration declaration : file.getDeclarations()) {
            if (declaration instanceof JetProperty) {
                final CodegenContext context = CodegenContext.STATIC.intoNamespace(descriptor);
                state.getMemberCodegen().generateFunctionOrProperty(
                        (JetTypeParameterListOwner) declaration, context, v.getClassBuilder());
            }
            else if (declaration instanceof JetNamedFunction) {
                if (!multiFile) {
                    final CodegenContext context = CodegenContext.STATIC.intoNamespace(descriptor);
                    state.getMemberCodegen().generateFunctionOrProperty(
                            (JetTypeParameterListOwner) declaration, context, v.getClassBuilder());
                }
            }
            else if (declaration instanceof JetClassOrObject) {
                final CodegenContext context = CodegenContext.STATIC.intoNamespace(descriptor);
                state.getClassCodegen().generate(context, (JetClassOrObject) declaration);
            }
            else if (declaration instanceof JetScript) {
                state.getScriptCodegen().generate((JetScript) declaration);
            }
        }

        if (multiFile) {
            int k = 0;
            for (JetDeclaration declaration : file.getDeclarations()) {
                if (declaration instanceof JetNamedFunction) {
                    k++;
                }
            }

            if (k > 0) {
                PsiFile containingFile = file.getContainingFile();
                String namespaceInternalName = name.child(Name.identifier(JvmAbi.PACKAGE_CLASS)).getFqName().replace('.', '/');
                String className = getMultiFileNamespaceInternalName(namespaceInternalName, containingFile);
                ClassBuilder builder = state.getFactory().forNamespacepart(className);

                builder.defineClass(containingFile, V1_6,
                                    ACC_PUBLIC/*|ACC_SUPER*/,
                                    className,
                                    null,
                                    //"jet/lang/Namespace",
                                    "java/lang/Object",
                                    new String[0]
                );
                builder.visitSource(containingFile.getName(), null);

                for (JetDeclaration declaration : file.getDeclarations()) {
                    if (declaration instanceof JetNamedFunction) {
                        {
                            final CodegenContext context =
                                    CodegenContext.STATIC.intoNamespace(descriptor);
                            state.getMemberCodegen()
                                    .generateFunctionOrProperty((JetTypeParameterListOwner) declaration, context, builder);
                        }
                        {
                            final CodegenContext context =
                                    CodegenContext.STATIC.intoNamespacePart(className, descriptor);
                            state.getMemberCodegen()
                                    .generateFunctionOrProperty((JetTypeParameterListOwner) declaration, context, v.getClassBuilder());
                        }
                    }
                }

                builder.done();
            }
        }
    }

    /**
     * @param namespaceFiles all files should have same package name
     * @return
     */
    public static boolean shouldGenerateNSClass(Collection<JetFile> namespaceFiles) {
        checkAllFilesHaveSameNamespace(namespaceFiles);

        for (JetFile file : namespaceFiles) {
            for (JetDeclaration declaration : file.getDeclarations()) {
                if (declaration instanceof JetProperty || declaration instanceof JetNamedFunction) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void checkAllFilesHaveSameNamespace(Collection<JetFile> namespaceFiles) {
        FqName commonFqName = null;
        for (JetFile file : namespaceFiles) {
            FqName fqName = JetPsiUtil.getFQName(file);
            if (commonFqName != null) {
                if (!commonFqName.equals(fqName)) {
                    throw new IllegalArgumentException("All files should have same package name");
                }
            }
            else {
                commonFqName = JetPsiUtil.getFQName(file);
            }
        }
    }

    private void generateStaticInitializers() {
        final JetFile namespace = files.iterator().next(); // @todo: hack

        v.addOptionalDeclaration(new ClassBuilderOnDemand.ClassBuilderCallback() {
            @Override
            public void doSomething(@NotNull ClassBuilder v) {
                MethodVisitor mv = v.newMethod(namespace, ACC_PUBLIC | ACC_STATIC, "<clinit>", "()V", null, null);
                if (state.getClassBuilderMode() == ClassBuilderMode.FULL) {
                    mv.visitCode();

                    FrameMap frameMap = new FrameMap();
                    ExpressionCodegen codegen = new ExpressionCodegen(mv, frameMap, Type.VOID_TYPE, CodegenContext.STATIC, state);

                    for (JetFile file : files) {
                        for (JetDeclaration declaration : file.getDeclarations()) {
                            if (declaration instanceof JetProperty) {
                                final JetExpression initializer = ((JetProperty) declaration).getInitializer();
                                if (initializer != null && !(initializer instanceof JetConstantExpression)) {
                                    final PropertyDescriptor descriptor =
                                            (PropertyDescriptor) state.getBindingContext().get(BindingContext.VARIABLE, declaration);
                                    assert descriptor != null;
                                    codegen.genToJVMStack(initializer);
                                    StackValue.Property propValue = codegen.intermediateValueForProperty(descriptor, true, null);
                                    propValue.store(propValue.type, new InstructionAdapter(mv));
                                }
                            }
                        }
                    }

                    mv.visitInsn(RETURN);
                    FunctionCodegen.endVisit(mv, "static initializer for namespace", namespace);
                    mv.visitEnd();
                }
            }
        });
    }

    private boolean hasNonConstantPropertyInitializers() {
        for (JetFile file : files) {
            for (JetDeclaration declaration : file.getDeclarations()) {
                if (declaration instanceof JetProperty) {
                    final JetExpression initializer = ((JetProperty) declaration).getInitializer();
                    if (initializer != null && !(initializer instanceof JetConstantExpression)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void done() {
        v.done();
    }

    @NotNull
    public static JvmClassName getJVMClassNameForKotlinNs(@NotNull FqName fqName) {
        if (fqName.isRoot()) {
            return JvmClassName.byInternalName(JvmAbi.PACKAGE_CLASS);
        }

        return JvmClassName.byInternalName(fqName.getFqName().replace('.', '/') + "/" + JvmAbi.PACKAGE_CLASS);
    }

    @NotNull
    public static String getMultiFileNamespaceInternalName(String namespaceInternalName, PsiFile file) {
        String name = file.getName();

        int substringFrom = name.lastIndexOf(File.separator) + 1;

        int substringTo = name.lastIndexOf('.');
        if (substringTo == -1) {
            substringTo = name.length();
        }

        return namespaceInternalName + "$src$" + name.substring(substringFrom, substringTo);
    }
}