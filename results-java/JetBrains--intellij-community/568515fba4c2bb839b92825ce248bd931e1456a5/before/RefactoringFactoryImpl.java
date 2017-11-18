/*
 * Copyright (c) 2000-2004 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.refactoring.openapi.impl;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.refactoring.*;
import com.intellij.refactoring.move.moveClassesOrPackages.AutocreatingSingleSourceRootMoveDestination;
import com.intellij.refactoring.move.moveClassesOrPackages.MultipleRootsMoveDestination;

/**
 * @author dsl
 */
public class RefactoringFactoryImpl extends RefactoringFactory implements ProjectComponent {
  private static final Logger LOG = Logger.getInstance("#com.intellij.refactoring.openapi.impl.RefactoringFactoryImpl");
  private final Project myProject;

  public RefactoringFactoryImpl(Project project) {
    myProject = project;
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  public String getComponentName() {
    return "RefactoringFactory";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  public RenameRefactoring createRename(PsiElement element, String newName) {
    return new RenameRefactoringImpl(myProject, element, newName, true, true);
  }

  public MoveInnerRefactoring createMoveInner(PsiClass innerClass, String newName, boolean passOuterClass, String parameterName) {
    return new MoveInnerRefactoringImpl(myProject, innerClass, newName, passOuterClass, parameterName);
  }

  public MoveDestination createSourceFolderPreservingMoveDestination(String targetPackage) {
    return new MultipleRootsMoveDestination(createPackageWrapper(targetPackage));
  }

  private PackageWrapper createPackageWrapper(String targetPackage) {
    return new PackageWrapper(PsiManager.getInstance(myProject), targetPackage);
  }

  public MoveDestination createSourceRootMoveDestination(String targetPackageQualifiedName, VirtualFile sourceRoot) {
    final PsiDirectory directory = PsiManager.getInstance(myProject).findDirectory(sourceRoot);
    LOG.assertTrue(directory != null && !directory.isSourceRoot(), "Should pass source root");
    return new AutocreatingSingleSourceRootMoveDestination(createPackageWrapper(targetPackageQualifiedName),
                                                           sourceRoot);
  }


  public MoveClassesOrPackagesRefactoring createMoveClassesOrPackages(PsiElement[] elements, MoveDestination moveDestination) {
    return new MoveClassesOrPackagesRefactoringImpl(myProject, elements, moveDestination);
  }

  public MoveMembersRefactoring createMoveMembers(final PsiMember[] elements,
                                                  final String targetClassQualifiedName,
                                                  final String newVisibility) {
    return new MoveMembersRefactoringImpl(myProject, elements, targetClassQualifiedName, newVisibility);
  }

  public MakeStaticRefactoring<PsiMethod> createMakeMethodStatic(PsiMethod method,
                                                            boolean replaceUsages,
                                                            String classParameterName,
                                                            PsiField[] fields,
                                                            String[] names) {
    return new MakeMethodStaticRefactoringImpl(myProject, method, replaceUsages, classParameterName, fields, names);
  }

  public MakeStaticRefactoring<PsiClass> createMakeClassStatic(PsiClass aClass,
                                                            boolean replaceUsages,
                                                            String classParameterName,
                                                            PsiField[] fields,
                                                            String[] names) {
    return new MakeClassStaticRefactoringImpl(myProject, aClass, replaceUsages, classParameterName, fields, names);
  }

  public ConvertToInstanceMethodRefactoring createConvertToInstanceMethod(PsiMethod method,
                                                                          PsiParameter targetParameter) {
    return new ConvertToInstanceMethodRefactoringImpl(myProject, method, targetParameter);
  }

  public SafeDeleteRefactoring createSafeDelete(PsiElement[] elements) {
    return new SafeDeleteRefactoringImpl(myProject, elements);
  }

  public TurnRefsToSuperRefactoring createTurnRefsToSuper(PsiClass aClass,
                                                          PsiClass aSuper,
                                                          boolean replaceInstanceOf) {
    return new TurnRefsToSuperRefactoringImpl(myProject, aClass, aSuper, replaceInstanceOf);
  }

  public ReplaceConstructorWithFactoryRefactoring createReplaceConstructorWithFactory(PsiMethod method,
                                                                                      PsiClass targetClass,
                                                                                      String factoryName) {
    return new ReplaceConstructorWithFactoryRefactoringImpl(myProject, method, targetClass, factoryName);
  }

  public ReplaceConstructorWithFactoryRefactoring createReplaceConstructorWithFactory(PsiClass originalClass,
                                                                                      PsiClass targetClass,
                                                                                      String factoryName) {
    return new ReplaceConstructorWithFactoryRefactoringImpl(myProject, originalClass, targetClass, factoryName);
  }

  public TypeCookRefactoring createTypeCook(PsiElement[] elements,
                                            boolean dropObsoleteCasts,
                                            boolean leaveObjectsRaw,
                                            boolean preserveRawArrays,
                                            boolean exhaustive,
                                            boolean cookObjects,
                                            boolean cookToWildcards) {
    return new TypeCookRefactoringImpl(myProject, elements, dropObsoleteCasts, leaveObjectsRaw, preserveRawArrays, exhaustive, cookObjects, cookToWildcards);
  }

  public IntroduceParameterRefactoring createIntroduceParameterRefactoring(PsiMethod methodToReplaceIn,
                                                                           PsiMethod methodToSearchFor,
                                                                           String parameterName, PsiExpression parameterInitializer,
                                                                           PsiLocalVariable localVariable,
                                                                           boolean removeLocalVariable, boolean declareFinal) {
    return new IntroduceParameterRefactoringImpl(myProject, methodToReplaceIn, methodToSearchFor, parameterName, parameterInitializer,
                                                 localVariable, removeLocalVariable, declareFinal);
  }

  public IntroduceParameterRefactoring createIntroduceParameterRefactoring(PsiMethod methodToReplaceIn,
                                                                           PsiMethod methodToSearchFor,
                                                                           String parameterName, PsiExpression parameterInitializer,
                                                                           PsiExpression expressionToSearchFor,
                                                                           boolean declareFinal, final boolean replaceAllOccurences) {
    return new IntroduceParameterRefactoringImpl(myProject, methodToReplaceIn, methodToSearchFor, parameterName, parameterInitializer,
                                                 expressionToSearchFor, declareFinal, replaceAllOccurences);
  }
}