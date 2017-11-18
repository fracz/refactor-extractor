package com.jetbrains.python.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PyElementTypes;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.stubs.PyTargetExpressionStub;
import com.jetbrains.python.psi.types.PyType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * @author yole
 */
public class PyTargetExpressionImpl extends PyPresentableElementImpl<PyTargetExpressionStub> implements PyTargetExpression {
  public PyTargetExpressionImpl(ASTNode astNode) {
    super(astNode);
  }

  public PyTargetExpressionImpl(final PyTargetExpressionStub stub) {
    super(stub, PyElementTypes.TARGET_EXPRESSION);
  }

  @Override
  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyTargetExpression(this);
  }

  @Nullable
  @Override
  public String getName() {
    final PyTargetExpressionStub stub = getStub();
    if (stub != null) {
      return stub.getName();
    }
    ASTNode node = getNameElement();
    return node != null ? node.getText() : null;
  }

  private ASTNode getNameElement() {
    return getNode().findChildByType(PyTokenTypes.IDENTIFIER);
  }

  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    final ASTNode nameElement = PyElementGenerator.getInstance(getProject()).createNameIdentifier(name);
    getNode().replaceChild(getNameElement(), nameElement);
    return this;
  }

  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState substitutor,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    // in statements, process only the section in which the original expression was located
    PsiElement parent = getParent();
    if (parent instanceof PyStatement && lastParent == null && PsiTreeUtil.isAncestor(parent, place, true)) {
      return true;
    }

    // never resolve to references within the same assignment statement
    if (getParent() instanceof PyAssignmentStatement) {
      PsiElement placeParent = place.getParent();
      while (placeParent != null && placeParent instanceof PyExpression) {
        placeParent = placeParent.getParent();
      }
      if (placeParent == getParent()) {
        return true;
      }
    }

    if (this == place) {
      return true;
    }
    return processor.execute(this, substitutor);
  }

  public PyType getType() {
    if (getParent() instanceof PyAssignmentStatement) {
      final PyAssignmentStatement assignmentStatement = (PyAssignmentStatement)getParent();
      final PyExpression assignedValue = assignmentStatement.getAssignedValue();
      if (assignedValue != null) {
        if (assignedValue instanceof PyReferenceExpression) {
          final PyReferenceExpression refex = (PyReferenceExpression)assignedValue;
          PyType maybe_type = PyUtil.getSpecialAttributeType(refex);
          if (maybe_type != null) return maybe_type;
          final PsiElement resolveResult = refex.getReference().resolve();
          if (resolveResult == this) {
            return null;  // fix SOE on "a = a"
          }
          return PyReferenceExpressionImpl.getTypeFromTarget(resolveResult);
        }
        return assignedValue.getType();
      }
    }
    return null;
  }

  public PyExpression getQualifier() {
    final ASTNode[] nodes = getNode().getChildren(PyElementTypes.EXPRESSIONS);
    return (PyExpression)(nodes.length == 1 ? nodes[0].getPsi() : null);
  }

  public String toString() {
    return super.toString() + ": " + getName();
  }

  public Icon getIcon(final int flags) {
    return Icons.FIELD_ICON; // for the rare cases it shows in structure view
  }

  @Nullable
  public PyExpression findAssignedValue() {
    PyAssignmentStatement assignment = PsiTreeUtil.getParentOfType(this, PyAssignmentStatement.class);
    if (assignment != null) {
      List<Pair<PyExpression, PyExpression>> mapping = assignment.getTargetsToValuesMapping();
      for (Pair<PyExpression, PyExpression> pair : mapping) {
        PyExpression assigned_to = pair.getFirst();
        if (assigned_to == this) return pair.getSecond();
      }
    }
    return null;
  }

  public PyQualifiedName getAssignedQName() {
    final PyTargetExpressionStub stub = getStub();
    if (stub != null) {
      return stub.getInitializer();
    }
    final PyExpression value = findAssignedValue();
    return value instanceof PyReferenceExpression ? ((PyReferenceExpression) value).asQualifiedName() : null;
  }
}