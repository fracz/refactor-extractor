package com.intellij.refactoring.rename;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.util.RefactoringUtil;
import com.intellij.usageView.UsageInfo;

import java.util.List;
import java.util.Map;

public class JavaUnresolvableLocalCollisionDetector implements RenameCollisionDetector {
  private static final Logger LOG = Logger.getInstance("#com.intellij.refactoring.rename.JavaUnresolvableLocalCollisionDetector");

  public void findCollisions(final PsiElement element, final String newName, final Map<? extends PsiElement, String> allRenames, final List<UsageInfo> result) {
    if (!(element instanceof PsiLocalVariable || element instanceof PsiParameter)) {
      return;
    }


    PsiElement scope;
    PsiElement anchor = null;
    if (element instanceof PsiLocalVariable) {
      scope = RefactoringUtil.getVariableScope((PsiLocalVariable)element);
      if (!(element instanceof ImplicitVariable)) {
        anchor = element.getParent();
      }
    }
    else {
      // element is a PsiParameter
      scope = ((PsiParameter)element).getDeclarationScope();
    }
    LOG.assertTrue(scope != null);

    final CollidingVariableVisitor collidingNameVisitor = new CollidingVariableVisitor() {
      public void visitCollidingElement(PsiVariable collidingVariable) {
        if (collidingVariable.equals(element) || collidingVariable instanceof PsiField) return;
        LocalHidesRenamedLocalUsageInfo collision = new LocalHidesRenamedLocalUsageInfo(element, collidingVariable);
        result.add(collision);
      }
    };

    visitLocalsCollisions(element, newName, scope, anchor, collidingNameVisitor);


    /*PsiElement place = scope.getLastChild();
    PsiResolveHelper helper = place.getManager().getResolveHelper();
    PsiVariable refVar = helper.resolveReferencedVariable(newName, place, null);

    if (refVar != null) {
      LocalHidesRenamedLocalUsageInfo collision = new LocalHidesRenamedLocalUsageInfo(element, refVar);
      result.add(collision);
    }*/
  }

  public static void visitLocalsCollisions(PsiElement element, final String newName,
                                           PsiElement scope,
                                           PsiElement place,
                                           final CollidingVariableVisitor collidingNameVisitor) {
    if (scope == null) return;
    visitDownstreamCollisions(scope, place, newName, collidingNameVisitor);
    visitUpstreamLocalCollisions(element, scope, newName, collidingNameVisitor);
  }

  private static void visitDownstreamCollisions(PsiElement scope, PsiElement place, final String newName,
                                                final CollidingVariableVisitor collidingNameVisitor
                                               ) {
    ConflictingLocalVariablesVisitor collector =
      new ConflictingLocalVariablesVisitor(newName, collidingNameVisitor);
    if (place == null) {
      scope.accept(collector);
    }
    else {
      LOG.assertTrue(place.getParent() == scope);
      for (PsiElement sibling = place; sibling != null; sibling = sibling.getNextSibling()) {
        sibling.accept(collector);
      }

    }
  }

  public interface CollidingVariableVisitor {
    void visitCollidingElement(PsiVariable collidingVariable);
  }

  private static void visitUpstreamLocalCollisions(PsiElement element, PsiElement scope,
                                                  String newName,
                                                  final CollidingVariableVisitor collidingNameVisitor) {
    final PsiVariable collidingVariable =
      JavaPsiFacade.getInstance(scope.getProject()).getResolveHelper().resolveReferencedVariable(newName, scope);
    if (collidingVariable instanceof PsiLocalVariable || collidingVariable instanceof PsiParameter) {
      final PsiElement commonParent = PsiTreeUtil.findCommonParent(element, collidingVariable);
      if (commonParent != null) {
        PsiElement current = element;
        while (current != null && current != commonParent) {
          if (current instanceof PsiMethod || current instanceof PsiClass) {
            return;
          }
          current = current.getParent();
        }
      }
    }

    if (collidingVariable != null) {
      collidingNameVisitor.visitCollidingElement(collidingVariable);
    }
  }

  public static class ConflictingLocalVariablesVisitor extends JavaRecursiveElementVisitor {
    protected final String myName;
    protected CollidingVariableVisitor myCollidingNameVisitor;

    public ConflictingLocalVariablesVisitor(String newName, CollidingVariableVisitor collidingNameVisitor) {
      myName = newName;
      myCollidingNameVisitor = collidingNameVisitor;
    }

    @Override public void visitReferenceExpression(PsiReferenceExpression expression) {
      visitElement(expression);
    }

    @Override public void visitClass(PsiClass aClass) {
    }

    @Override public void visitVariable(PsiVariable variable) {
      if (myName.equals(variable.getName())) {
        myCollidingNameVisitor.visitCollidingElement(variable);
      }
    }
  }



}