package com.intellij.refactoring.removemiddleman;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.refactoring.RefactorJBundle;
import com.intellij.refactoring.removemiddleman.usageInfo.DeleteMethod;
import com.intellij.refactoring.removemiddleman.usageInfo.InlineDelegatingCall;
import com.intellij.refactoring.util.FixableUsageInfo;
import com.intellij.refactoring.util.FixableUsagesRefactoringProcessor;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RemoveMiddlemanProcessor extends FixableUsagesRefactoringProcessor {
  private static final Logger LOG = Logger.getInstance("#" + RemoveMiddlemanProcessor.class.getName());

  private final PsiField field;
  private final PsiClass containingClass;
  private final List<MemberInfo> myDelegateMethodInfos;
  private PsiMethod getter;

  public RemoveMiddlemanProcessor(PsiField field, List<MemberInfo> memberInfos) {
    super(field.getProject());
    this.field = field;
    containingClass = field.getContainingClass();
    final Project project = field.getProject();
    final String propertyName = PropertyUtil.suggestPropertyName(project, field);
    final boolean isStatic = field.hasModifierProperty(PsiModifier.STATIC);
    getter = PropertyUtil.findPropertyGetter(containingClass, propertyName, isStatic, false);
    myDelegateMethodInfos = memberInfos;
  }

  protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos) {
    return new RemoveMiddlemanUsageViewDescriptor(field);
  }


  public void findUsages(@NotNull List<FixableUsageInfo> usages) {
    for (final MemberInfo memberInfo : myDelegateMethodInfos) {
      if (!memberInfo.isChecked()) continue;
      final PsiMethod method = (PsiMethod)memberInfo.getMember();
      final Project project = method.getProject();
      final String getterName = PropertyUtil.suggestGetterName(project, field);
      final int[] paramPermutation = DelegationUtils.getParameterPermutation(method);
      final PsiMethod delegatedMethod = DelegationUtils.getDelegatedMethod(method);
      LOG.assertTrue(!DelegationUtils.isAbstract(method));
      processUsagesForMethod(memberInfo.isToAbstract(), method, paramPermutation, getterName, delegatedMethod, usages);
    }
  }

  @Override
  protected boolean preprocessUsages(final Ref<UsageInfo[]> refUsages) {
    final List<String> conflicts = new ArrayList<String>();
    for (MemberInfo memberInfo : myDelegateMethodInfos) {
      if (memberInfo.isChecked() && memberInfo.isToAbstract()) {
        final PsiMember psiMember = memberInfo.getMember();
        if (psiMember instanceof PsiMethod && ((PsiMethod)psiMember).findDeepestSuperMethods().length > 0) {
          conflicts.add(SymbolPresentationUtil.getSymbolPresentableText(psiMember) + " will be deleted. Hierarchy will be broken");
        }
      }
    }
    return showConflicts(conflicts);
  }

  private void processUsagesForMethod(final boolean deleteMethodHierarchy, PsiMethod method, int[] paramPermutation, String getterName, PsiMethod delegatedMethod,
                                        List<FixableUsageInfo> usages) {
    for (PsiReference reference : ReferencesSearch.search(method)) {
      final PsiElement referenceElement = reference.getElement();
      final PsiMethodCallExpression call = (PsiMethodCallExpression)referenceElement.getParent();
      final String access;
      if (call.getMethodExpression().getQualifierExpression() == null) {
        access = field.getName();
      } else {
        access = getterName + "()";
        if (getter == null) {
          getter = PropertyUtil.generateGetterPrototype(field);
        }
      }
      usages.add(new InlineDelegatingCall(call, paramPermutation, access, delegatedMethod.getName()));
    }
    if (deleteMethodHierarchy) {
      usages.add(new DeleteMethod(method));
    }
  }

  protected void performRefactoring(UsageInfo[] usageInfos) {
    if (getter != null) {
      try {
        if (containingClass.findMethodBySignature(getter, false) == null) {
          containingClass.add(getter);
        }
      }
      catch (IncorrectOperationException e) {
        LOG.error(e);
      }
    }

    super.performRefactoring(usageInfos);
  }

  protected String getCommandName() {
    return RefactorJBundle.message("exposed.delegation.command.name", containingClass.getName(), '.', field.getName());
  }
}