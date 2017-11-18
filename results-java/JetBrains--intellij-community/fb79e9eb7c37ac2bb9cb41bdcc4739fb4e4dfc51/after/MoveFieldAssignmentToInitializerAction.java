package com.intellij.codeInsight.intention.impl;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author cdr
 */
public class MoveFieldAssignmentToInitializerAction extends BaseIntentionAction {
  public String getFamilyName() {
    return getText();
  }

  public String getText() {
    return CodeInsightBundle.message("intention.move.field.assignment.to.initializer");
  }

  public boolean isAvailable(Project project, Editor editor, PsiFile file) {
    final PsiAssignmentExpression assignment = getAssignmentUnderCaret(editor, file);
    if (assignment == null) return false;
    PsiField field = getAssignedField(assignment);
    if (field == null || field.hasInitializer()) return false;
    PsiClass psiClass = field.getContainingClass();

    if (psiClass == null || psiClass.isInterface()) return false;
    if (psiClass.getContainingFile() != file) return false;
    PsiModifierListOwner ctrOrInitializer = insideConstructorOrClassInitializer(assignment, field);
    if (ctrOrInitializer==null) return false;
    if (!isValidAsFieldInitializer(assignment.getRExpression(), ctrOrInitializer)) return false;
    if (!isInitializedWithSameExpression(field, assignment, null)) return false;
    return true;
  }

  private static boolean isValidAsFieldInitializer(final PsiExpression initializer, final PsiModifierListOwner ctrOrInitializer) {
    final Ref<Boolean> result = new Ref<Boolean>(Boolean.TRUE);
    initializer.accept(new PsiRecursiveElementVisitor() {
      public void visitReferenceExpression(PsiReferenceExpression expression) {
        PsiElement resolved = expression.resolve();
        if (resolved == null) return;
        if (PsiTreeUtil.isAncestor(ctrOrInitializer, resolved, false) && !PsiTreeUtil.isAncestor(initializer, resolved, false)) {
          // resolved somewhere inside construcor but outside initializer
          result.set(Boolean.FALSE);
        }
      }
    });
    return result.get().booleanValue();
  }

  private static PsiModifierListOwner insideConstructorOrClassInitializer(final PsiAssignmentExpression assignment, final PsiField field) {
    PsiElement parentCodeBlock = assignment;
    while (true) {
      parentCodeBlock = PsiTreeUtil.getParentOfType(parentCodeBlock, PsiCodeBlock.class, true, PsiMember.class);
      if (parentCodeBlock == null) return null;
      PsiElement parent = parentCodeBlock.getParent();
      if (!(parent instanceof PsiModifierListOwner)) continue;
      if (((PsiModifierListOwner)parent).hasModifierProperty(PsiModifier.STATIC) != field.hasModifierProperty(PsiModifier.STATIC)) return null;
      if (parent instanceof PsiMethod && ((PsiMethod)parent).isConstructor() || parent instanceof PsiClassInitializer) {
        return (PsiModifierListOwner)parent;
      }
    }
  }

  private static boolean isInitializedWithSameExpression(final PsiField field, final PsiAssignmentExpression assignment, final Collection<PsiAssignmentExpression> initializingAssignments) {
    final PsiExpression expression = assignment.getRExpression();
    final Ref<Boolean> result = new Ref<Boolean>(Boolean.TRUE);
    PsiClass containingClass = field.getContainingClass();
    containingClass.accept(new PsiRecursiveElementVisitor(){
      private PsiCodeBlock currentInitializingBlock; //ctr or class initializer

      public void visitCodeBlock(PsiCodeBlock block) {
        PsiElement parent = block.getParent();
        if (parent instanceof PsiClassInitializer || parent instanceof PsiMethod && ((PsiMethod)parent).isConstructor()) {
          currentInitializingBlock = block;
          super.visitCodeBlock(block);
          currentInitializingBlock = null;
        }
        else {
          super.visitCodeBlock(block);
        }
      }

      public void visitReferenceExpression(PsiReferenceExpression reference) {
        if (!result.get().booleanValue()) return;
        super.visitReferenceExpression(reference);
        if (currentInitializingBlock == null) return; // ignore usages other than intializing
        if (!PsiUtil.isOnAssignmentLeftHand(reference)) return;
        PsiElement resolved = reference.resolve();
        if (resolved != field) return;
        PsiExpression rValue = ((PsiAssignmentExpression)reference.getParent()).getRExpression();
        if (rValue == null || !expressionsEquivalent(rValue, expression)) {
          result.set(Boolean.FALSE);
        }
        if (initializingAssignments != null) {
          initializingAssignments.add((PsiAssignmentExpression)reference.getParent());
        }
      }
    });
    return result.get().booleanValue();
  }

  private static boolean expressionsEquivalent(final PsiExpression rValue, final PsiExpression expression) {
    return PsiEquivalenceUtil.areElementsEquivalent(rValue, expression);
  }

  private static PsiField getAssignedField(final PsiAssignmentExpression assignment) {
    PsiExpression lExpression = assignment.getLExpression();
    if (!(lExpression instanceof PsiReferenceExpression)) return null;
    PsiElement resolved = ((PsiReferenceExpression)lExpression).resolve();
    if (!(resolved instanceof PsiField)) return null;
    PsiField field = (PsiField)resolved;
    return field;
  }

  private static PsiAssignmentExpression getAssignmentUnderCaret(final Editor editor, final PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(offset);
    if (element == null || element instanceof PsiCompiledElement) return null;
    final PsiAssignmentExpression assignment = PsiTreeUtil.getParentOfType(element, PsiAssignmentExpression.class, false, PsiMember.class);
    return assignment;
  }

  public void invoke(Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    final PsiAssignmentExpression assignment = getAssignmentUnderCaret(editor, file);
    if (assignment == null) return;
    PsiField field = getAssignedField(assignment);
    if (field == null) return;
    if (!CodeInsightUtil.prepareFileForWrite(file)) return;

    ArrayList<PsiAssignmentExpression> assignments = new ArrayList<PsiAssignmentExpression>();
    if (!isInitializedWithSameExpression(field, assignment, assignments)) return;
    PsiExpression initializer = assignment.getRExpression();
    field.setInitializer(initializer);

    for (PsiAssignmentExpression assignmentExpression : assignments) {
      PsiElement statement = assignmentExpression.getParent();
      PsiElement parent = statement.getParent();
      if (parent instanceof PsiIfStatement || parent instanceof PsiWhileStatement || parent instanceof PsiForStatement || parent instanceof PsiForeachStatement) {
        PsiStatement emptyStatement = file.getManager().getElementFactory().createStatementFromText(";", statement);
        statement.replace(emptyStatement);
      }
      else {
        statement.delete();
      }
    }
  }
}