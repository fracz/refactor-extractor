/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * User: anna
 * Date: 09-Jan-2007
 */
package com.intellij.codeInspection.offlineViewer;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.DescriptorProviderInspection;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.codeInspection.ex.QuickFixWrapper;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.codeInspection.reference.RefEntity;
import com.intellij.codeInspection.ui.ProblemDescriptionNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMember;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OfflineProblemDescriptorNode extends ProblemDescriptionNode {

  public OfflineProblemDescriptorNode(final OfflineProblemDescriptor descriptor,
                                      final boolean replaceProblemDescriptorTemplateMessage,
                                      final DescriptorProviderInspection tool) {
    super(descriptor, replaceProblemDescriptorTemplateMessage, tool);
  }

  @Nullable
  public RefEntity getElement() {
    if (userObject instanceof CommonProblemDescriptor) {
      return myElement;
    }
    if (userObject == null) {
      return null;
    }
    myElement = ((OfflineProblemDescriptor)userObject).getRefElement(myTool.getContext().getRefManager());
    return myElement;
  }

  @Nullable
  public CommonProblemDescriptor getDescriptor() {
    if (userObject == null) return null;
    if (userObject instanceof CommonProblemDescriptor) {
      return (CommonProblemDescriptor)userObject;
    }
    final InspectionManager inspectionManager = InspectionManager.getInstance(myTool.getContext().getProject());
    final OfflineProblemDescriptor offlineProblemDescriptor = (OfflineProblemDescriptor)userObject;
    final RefEntity element = getElement();
    if (myTool instanceof LocalInspectionToolWrapper) {
      final ProblemsHolder holder = new ProblemsHolder(inspectionManager);
      if (element instanceof RefElement) {
        final PsiElement psiElement = ((RefElement)element).getElement();
        if (psiElement != null) {
          final PsiElementVisitor visitor = ((LocalInspectionToolWrapper)myTool).getTool().buildVisitor(holder, false);
          final List<PsiElement> elementsInRange = CodeInsightUtil.getElementsInRange(psiElement.getContainingFile(),
                                                                                      psiElement.getTextRange().getStartOffset(),
                                                                                      psiElement.getTextRange().getEndOffset(),
                                                                                      true);
          for (PsiElement el : elementsInRange) {
            el.accept(visitor);
          }
          if (holder.hasResults()) {
            final List<ProblemDescriptor> list = holder.getResults();
            final int idx = offlineProblemDescriptor.getProblemIndex();
            if (list != null) {
              int curIdx = 0;
              for (ProblemDescriptor descriptor : list) {
                final PsiMember member = PsiTreeUtil.getParentOfType(descriptor.getPsiElement(), PsiMember.class, false);
                if (member != null && member.equals(psiElement)) {
                  if (curIdx == idx) {
                    setUserObject(descriptor);
                    return descriptor;
                  }
                  curIdx++;
                }
              }
            }
          }
        }
      }
      setUserObject(null);
      return null;
    }
    final List<String> hints = offlineProblemDescriptor.getHints();
    if (element instanceof RefElement) {
      final PsiElement psiElement = ((RefElement)element).getElement();
      ProblemDescriptor descriptor = inspectionManager.createProblemDescriptor(psiElement, offlineProblemDescriptor.getDescription(),
                                                                               (LocalQuickFix)null,
                                                                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
      final LocalQuickFix[] quickFixes = getFixes(descriptor, hints);
      if (quickFixes != null) {
        descriptor = inspectionManager.createProblemDescriptor(psiElement, offlineProblemDescriptor.getDescription(), quickFixes,
                                                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
      }
      setUserObject(descriptor);
      return descriptor;
    }
    CommonProblemDescriptor descriptor =
      inspectionManager.createProblemDescriptor(offlineProblemDescriptor.getDescription(), (QuickFix)null);
    final QuickFix[] quickFixes = getFixes(descriptor, hints);
    if (quickFixes != null) {
      descriptor = inspectionManager.createProblemDescriptor(offlineProblemDescriptor.getDescription(), quickFixes);
    }
    setUserObject(descriptor);
    return descriptor;
  }

  @Nullable
  private LocalQuickFix[] getFixes(CommonProblemDescriptor descriptor, List<String> hints) {
    final List<LocalQuickFix> fixes = new ArrayList<LocalQuickFix>();
    if (hints == null) {
      addFix(descriptor, fixes, null);
    } else {
      for (String hint : hints) {
        addFix(descriptor, fixes, hint);
      }
    }
    return fixes.isEmpty() ? null : fixes.toArray(new LocalQuickFix[fixes.size()]);
  }

  private void addFix(final CommonProblemDescriptor descriptor, final List<LocalQuickFix> fixes, String hint) {
    final IntentionAction intentionAction = myTool.findQuickFixes(descriptor, hint);
    if (intentionAction instanceof QuickFixWrapper) {
      fixes.add(((QuickFixWrapper)intentionAction).getFix());
    }
  }

  public boolean isValid() {
    return getDescriptor() != null && super.isValid();
  }
}