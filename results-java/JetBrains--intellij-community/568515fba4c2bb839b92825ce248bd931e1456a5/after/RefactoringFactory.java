/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package com.intellij.refactoring;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

/**
 * @author dsl
 */
public abstract class RefactoringFactory {
  public static RefactoringFactory getInstance(Project project) {
    return project.getComponent(RefactoringFactory.class);
  }

  public abstract RenameRefactoring createRename(PsiElement element, String newName);

  public abstract MoveInnerRefactoring createMoveInner(PsiClass innerClass, String newName,
                                                       boolean passOuterClass, String parameterName);

  /**
   * Creates move destination for a specified package that preserves source folders for moved items.
   */
  public abstract MoveDestination createSourceFolderPreservingMoveDestination(String targetPackageQualifiedName);

  /**
   * Creates move destination for a specified package that moves all items to a specifed source folder
   */
  public abstract MoveDestination createSourceRootMoveDestination(String targetPackageQualifiedName, VirtualFile sourceRoot);

  public abstract MoveClassesOrPackagesRefactoring createMoveClassesOrPackages(PsiElement[] elements, MoveDestination moveDestination);

  public abstract MoveMembersRefactoring createMoveMembers(PsiMember[] elements,
                                                           String targetClassQualifiedName,
                                                           String newVisibility);

  public abstract MakeStaticRefactoring<PsiMethod> createMakeMethodStatic(PsiMethod method,
                                                                     boolean replaceUsages,
                                                                     String classParameterName,
                                                                     PsiField[] fields,
                                                                     String[] names);

  public abstract MakeStaticRefactoring<PsiClass> createMakeClassStatic(PsiClass aClass,
                                                                     boolean replaceUsages,
                                                                     String classParameterName,
                                                                     PsiField[] fields,
                                                                     String[] names);

  public abstract ConvertToInstanceMethodRefactoring createConvertToInstanceMethod(PsiMethod method,
                                                                                   PsiParameter targetParameter);

  public abstract SafeDeleteRefactoring createSafeDelete(PsiElement[] elements);

  public abstract TurnRefsToSuperRefactoring createTurnRefsToSuper(PsiClass aClass,
                                                                   PsiClass aSuper,
                                                                   boolean replaceInstanceOf);

  public abstract ReplaceConstructorWithFactoryRefactoring createReplaceConstructorWithFactory(PsiMethod method,
                                                                                               PsiClass targetClass,
                                                                                               String factoryName);

  public abstract ReplaceConstructorWithFactoryRefactoring createReplaceConstructorWithFactory(PsiClass originalClass,
                                                                                               PsiClass targetClass,
                                                                                               String factoryName);

  public abstract TypeCookRefactoring createTypeCook(PsiElement[] elements,
                                                     boolean dropObsoleteCasts,
                                                     boolean leaveObjectsRaw,
                                                     boolean preserveRawArrays,
                                                     boolean exhaustive,
                                                     boolean cookObjects,
                                                     boolean cookToWildcards);

  /**
   * Creates Introduce Parameter refactoring that replaces local variable with parameter.
   * @param methodToReplaceIn Method that the local variable should be replaced in.
   * @param methodToSearchFor Method that usages of should be updated (for overriding methods)
   * @param parameterName Name of new parameter.
   * @param parameterInitializer Initializer to use in method calls.
   * @param localVariable local variable that will be replaced
   * @param removeLocalVariable should local variable be removed
   * @param declareFinal should created parameter be declared <code>final</code>
   */
  public abstract IntroduceParameterRefactoring createIntroduceParameterRefactoring(PsiMethod methodToReplaceIn,
                                                                           PsiMethod methodToSearchFor,
                                                                           String parameterName, PsiExpression parameterInitializer,
                                                                           PsiLocalVariable localVariable,
                                                                           boolean removeLocalVariable, boolean declareFinal);

  /**
   * Creates Introduce Parameter refactoring that replaces expression with parameter.
   * @param methodToReplaceIn Method that the local variable should be replaced in.
   * @param methodToSearchFor Method that usages of should be updated (for overriding methods)
   * @param parameterName Name of new parameter.
   * @param parameterInitializer Initializer to use in method calls.
   * @param expressionToSearchFor expression that should be replaced with parameters
   * @param declareFinal should created parameter be declared <code>final</code>
   * @param replaceAllOccurences should all occurences of expression be replaced
   */
  public abstract IntroduceParameterRefactoring createIntroduceParameterRefactoring(PsiMethod methodToReplaceIn,
                                                                                    PsiMethod methodToSearchFor,
                                                                                    String parameterName,
                                                                                    PsiExpression parameterInitializer,
                                                                                    PsiExpression expressionToSearchFor,
                                                                                    boolean declareFinal,
                                                                                    final boolean replaceAllOccurences);
}