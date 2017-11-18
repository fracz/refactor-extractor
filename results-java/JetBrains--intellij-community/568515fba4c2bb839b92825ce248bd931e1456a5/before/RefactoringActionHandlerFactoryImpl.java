/*
 * Copyright (c) 2000-2004 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.refactoring.openapi.impl;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.DataConstantsEx;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringActionHandlerFactory;
import com.intellij.refactoring.anonymousToInner.AnonymousToInnerHandler;
import com.intellij.refactoring.changeSignature.ChangeSignatureHandler;
import com.intellij.refactoring.convertToInstanceMethod.ConvertToInstanceMethodHandler;
import com.intellij.refactoring.encapsulateFields.EncapsulateFieldsHandler;
import com.intellij.refactoring.extractInterface.ExtractInterfaceHandler;
import com.intellij.refactoring.extractMethod.ExtractMethodHandler;
import com.intellij.refactoring.inheritanceToDelegation.InheritanceToDelegationHandler;
import com.intellij.refactoring.inline.InlineHandler;
import com.intellij.refactoring.introduceField.IntroduceConstantHandler;
import com.intellij.refactoring.introduceField.IntroduceFieldHandler;
import com.intellij.refactoring.introduceParameter.IntroduceParameterHandler;
import com.intellij.refactoring.introduceVariable.IntroduceVariableHandler;
import com.intellij.refactoring.makeStatic.MakeStaticHandler;
import com.intellij.refactoring.memberPullUp.PullUpHandler;
import com.intellij.refactoring.memberPushDown.PushDownHandler;
import com.intellij.refactoring.move.MoveHandler;
import com.intellij.refactoring.rename.PsiElementRenameHandler;
import com.intellij.refactoring.replaceConstructorWithFactory.ReplaceConstructorWithFactoryHandler;
import com.intellij.refactoring.safeDelete.SafeDeleteHandler;
import com.intellij.refactoring.tempWithQuery.TempWithQueryHandler;
import com.intellij.refactoring.turnRefsToSuper.TurnRefsToSuperHandler;
import com.intellij.refactoring.typeCook.TypeCookHandler;
import com.intellij.refactoring.util.duplicates.MethodDuplicatesHandler;

/**
 * @author dsl
 */
public class RefactoringActionHandlerFactoryImpl extends RefactoringActionHandlerFactory implements ApplicationComponent {
  public String getComponentName() {
    return "RefactoringActionHandlerFactory";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  public RefactoringActionHandler createSafeDeleteHandler() {
    return new SafeDeleteHandler();
  }

  public RefactoringActionHandler createAnonymousToInnerHandler() {
    return new AnonymousToInnerHandler();
  }

  public RefactoringActionHandler createPullUpHandler() {
    return new PullUpHandler();
  }

  public RefactoringActionHandler createPushDownHandler() {
    return new PushDownHandler();
  }

  public RefactoringActionHandler createTurnRefsToSuperHandler() {
    return new TurnRefsToSuperHandler();
  }

  public RefactoringActionHandler createTempWithQueryHandler() {
    return new TempWithQueryHandler();
  }

  public RefactoringActionHandler createIntroduceParameterHandler() {
    return new IntroduceParameterHandler();
  }

  public RefactoringActionHandler createMakeMethodStaticHandler() {
    return new MakeStaticHandler();
  }

  public RefactoringActionHandler createConvertToInstanceMethodHandler() {
    return new ConvertToInstanceMethodHandler();
  }

  public RefactoringActionHandler createReplaceConstructorWithFactoryHandler() {
    return new ReplaceConstructorWithFactoryHandler();
  }

  public RefactoringActionHandler createEncapsulateFieldsHandler() {
    return new EncapsulateFieldsHandler();
  }

  public RefactoringActionHandler createMethodDuplicatesHandler() {
    return new MethodDuplicatesHandler();
  }

  public RefactoringActionHandler createChangeSignatureHandler() {
    return new ChangeSignatureHandler();
  }

  public RefactoringActionHandler createExtractSuperclassHandler() {
    return new ChangeSignatureHandler();
  }

  public RefactoringActionHandler createTypeCookHandler() {
    return new TypeCookHandler();
  }

  public RefactoringActionHandler createInlineHandler() {
    return new InlineHandler();
  }

  public RefactoringActionHandler createExtractMethodHandler() {
    return new ExtractMethodHandler();
  }

  public RefactoringActionHandler createInheritanceToDelegationHandler() {
    return new InheritanceToDelegationHandler();
  }

  public RefactoringActionHandler createExtractInterfaceHandler() {
    return new ExtractInterfaceHandler();
  }

  public RefactoringActionHandler createMoveHandler() {
    return new MoveHandler(new MoveHandler.TargetContainerFinder() {
      public PsiElement getTargetContainer(DataContext dataContext) {
        return (PsiElement)dataContext.getData(DataConstantsEx.TARGET_PSI_ELEMENT);
      }
    });
  }

  public RefactoringActionHandler createRenameHandler() {
    return new PsiElementRenameHandler();
  }

  public RefactoringActionHandler createIntroduceFieldHandler() {
    return new IntroduceFieldHandler();
  }

  public RefactoringActionHandler createIntroduceVariableHandler() {
    return new IntroduceVariableHandler();
  }



  public RefactoringActionHandler createIntroduceConstantHandler() {
    return new IntroduceConstantHandler();
  }
}