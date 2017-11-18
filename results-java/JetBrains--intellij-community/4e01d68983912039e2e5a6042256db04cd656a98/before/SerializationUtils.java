/*
 * Copyright 2003-2006 Dave Griffith, Bas Leijdekkers
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
package com.siyeh.ig.psiutils;

import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SerializationUtils {

    private static final String SERIALIZABLE_CLASS_NAME =
            "java.io.Serializable";
    private static final String EXTERNALIZABLE_CLASS_NAME =
            "java.io.Externalizable";
    @NonNls
    private static final String READ_OBJECT = "readObject";
    @NonNls
    private static final String WRITE_OBJECT = "writeObject";

    private SerializationUtils() {
        super();
    }

    public static boolean isSerializable(@Nullable PsiClass aClass) {
        return ClassUtils.isSubclass(aClass, SERIALIZABLE_CLASS_NAME);
    }

    public static boolean isExternalizable(@Nullable PsiClass aClass) {
        return ClassUtils.isSubclass(aClass, EXTERNALIZABLE_CLASS_NAME);
    }

    public static boolean isDirectlySerializable(@NotNull PsiClass aClass) {
        final PsiReferenceList implementsList = aClass.getImplementsList();
        if (implementsList != null) {
            final PsiJavaCodeReferenceElement[] interfaces =
                    implementsList.getReferenceElements();
            for (PsiJavaCodeReferenceElement aInterfaces : interfaces) {
                final PsiClass implemented = (PsiClass) aInterfaces.resolve();
                if (implemented != null) {
                    final String name = implemented.getQualifiedName();
                    if (SERIALIZABLE_CLASS_NAME.equals(name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasReadObject(@NotNull PsiClass aClass) {
        final PsiMethod[] methods = aClass.findMethodsByName(READ_OBJECT, false);
        for (final PsiMethod method : methods) {
            if (isReadObject(method)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasWriteObject(@NotNull PsiClass aClass) {
        final PsiMethod[] methods =
                aClass.findMethodsByName(WRITE_OBJECT, false);
        for (final PsiMethod method : methods) {
            if (isWriteObject(method)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isReadObject(@NotNull PsiMethod method) {
        final PsiManager manager = method.getManager();
        final PsiElementFactory factory = manager.getElementFactory();
        final Project project = method.getProject();
        final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        final PsiClassType type = factory.createTypeByFQClassName(
                "java.io.ObjectInputStream", scope);
        return MethodUtils.methodMatches(method, null,
                PsiType.VOID, READ_OBJECT, type);
    }

    public static boolean isWriteObject(@NotNull PsiMethod method) {
        final PsiManager manager = method.getManager();
        final PsiElementFactory factory = manager.getElementFactory();
        final Project project = method.getProject();
        final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        final PsiClassType type = factory.createTypeByFQClassName(
                "java.io.ObjectOutputStream", scope);
        return MethodUtils.methodMatches(method, null,
                PsiType.VOID, WRITE_OBJECT, type);
    }

    public static boolean isReadResolve(@NotNull PsiMethod method) {
        final String methodName = method.getName();
        @NonNls final String readResolve = "readResolve";
        if (!readResolve.equals(methodName)) {
            return false;
        }
        final PsiParameterList parameterList = method.getParameterList();
        final PsiParameter[] parameters = parameterList.getParameters();
        if (parameters.length != 0) {
            return false;
        }
        final PsiType returnType = method.getReturnType();
        return TypeUtils.isJavaLangObject(returnType);
    }

    public static boolean isWriteReplace(@NotNull PsiMethod method) {
        final String methodName = method.getName();
        @NonNls final String writeReplace = "writeReplace";
        if (!writeReplace.equals(methodName)) {
            return false;
        }
        final PsiParameterList parameterList = method.getParameterList();
        final PsiParameter[] parameters = parameterList.getParameters();
        if (parameters.length != 0) {
            return false;
        }
        final PsiType returnType = method.getReturnType();
        return TypeUtils.isJavaLangObject(returnType);
    }

    public static boolean isProbablySerializable(PsiType type) {
        if (type instanceof PsiWildcardType) {
            return true;
        }
        if (type instanceof PsiPrimitiveType) {
            return true;
        }
        if (type instanceof PsiArrayType) {
            final PsiType componentType = ((PsiArrayType) type).getComponentType();
            return isProbablySerializable(componentType);
        }
        if (type instanceof PsiClassType) {
            final PsiClassType classTYpe = (PsiClassType) type;
            final PsiClass psiClass = classTYpe.resolve();
            if (isSerializable(psiClass)) {
                return true;
            }
            if (isExternalizable(psiClass)) {
                return true;
            }
            if (ClassUtils.isSubclass(psiClass, "java.util.Collection") ||
                    ClassUtils.isSubclass(psiClass, "java.util.Map")) {
                final PsiType[] parameters = classTYpe.getParameters();
                for (PsiType parameter : parameters) {
                    if (!isProbablySerializable(parameter)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }
}