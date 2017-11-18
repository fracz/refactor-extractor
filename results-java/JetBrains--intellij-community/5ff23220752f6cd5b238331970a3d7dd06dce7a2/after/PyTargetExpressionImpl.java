package com.jetbrains.python.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PyElementTypes;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.stubs.PyTargetExpressionStub;
import com.jetbrains.python.psi.types.PyTupleType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
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

  @Override
  public int getTextOffset() {
    final ASTNode nameElement = getNameElement();
    return nameElement != null ? nameElement.getStartOffset() : getTextRange().getStartOffset();
  }

  @Nullable
  public ASTNode getNameElement() {
    return getNode().findChildByType(PyTokenTypes.IDENTIFIER);
  }

  public PsiElement getNameIdentifier() {
    final ASTNode nameElement = getNameElement();
    return nameElement == null ? null : nameElement.getPsi();
  }

  public String getReferencedName() {
    return getName();
  }

  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    final ASTNode oldNameElement = getNameElement();
    if (oldNameElement != null) {
      final ASTNode nameElement = PyElementGenerator.getInstance(getProject()).createNameIdentifier(name);
      getNode().replaceChild(oldNameElement, nameElement);
    }
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

  public PyType getType(@NotNull TypeEvalContext context) {
    if (!TypeEvalStack.mayEvaluate(this)) {
      return null;
    }
    try {
      if (getParent() instanceof PyAssignmentStatement) {
        final PyAssignmentStatement assignmentStatement = (PyAssignmentStatement)getParent();
        final PyExpression assignedValue = assignmentStatement.getAssignedValue();
        if (assignedValue != null) {
          if (assignedValue instanceof PyReferenceExpression) {
            final PyReferenceExpression refex = (PyReferenceExpression)assignedValue;
            PyType maybe_type = PyUtil.getSpecialAttributeType(refex);
            if (maybe_type != null) return maybe_type;
            final ResolveResult[] resolveResult = refex.getReference(PyResolveContext.noImplicits()).multiResolve(false);
            if (resolveResult.length == 1) {
              PsiElement target = resolveResult [0].getElement();
              if (target == this || target == null) {
                return null;  // fix SOE on "a = a"
              }
              return PyReferenceExpressionImpl.getTypeFromTarget(target, context, null);
            }
          }
          return context.getType(assignedValue);
        }
      }
      if (getParent() instanceof PyTupleExpression) {
        return getTypeFromTupleAssignment(context);
      }
      return null;
    }
    finally {
      TypeEvalStack.evaluated(this);
    }
  }

  @Nullable
  private PyType getTypeFromTupleAssignment(TypeEvalContext context) {
    final PyTupleExpression tuple = (PyTupleExpression) getParent();
    PsiElement pparent = tuple;
    while (pparent.getParent() instanceof PyParenthesizedExpression) {
      pparent = pparent.getParent();
    }
    if (pparent.getParent() instanceof PyAssignmentStatement) {
      final PyAssignmentStatement stmt = (PyAssignmentStatement) pparent.getParent();
      final PyExpression assignedValue = stmt.getAssignedValue();
      if (assignedValue != null) {
        final PyType assignedType = context.getType(assignedValue);
        if (assignedType instanceof PyTupleType) {
          PyTupleType tupleType = (PyTupleType)assignedType;
          if (tuple.getElements().length == tupleType.getElementCount()) {
            int selfIndex = ArrayUtil.indexOf(tuple.getElements(), this);
            return tupleType.getElementType(selfIndex);
          }
        }
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
    if (getQualifier() != null || PsiTreeUtil.<PsiElement>getParentOfType(this, PyFunction.class, PyClass.class) instanceof PyClass) {
      return Icons.FIELD_ICON;
    }
    return Icons.VARIABLE_ICON;
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

  @NotNull
  @Override
  public PsiReference getReference() {
    if (getQualifier() != null) {
      return new PyQualifiedReferenceImpl(this, PyResolveContext.defaultContext());
    }
    return new PyTargetReference(this, PyResolveContext.defaultContext());
  }

  @NotNull
  @Override
  public SearchScope getUseScope() {
    final PyGlobalStatement globalStatement = PyGlobalStatementNavigator.getByArgument(this);
    if (globalStatement != null) {
      return super.getUseScope();
    }

    // find highest level function containing our var
    PyElement container = this;
    while(true) {
      PyElement parentContainer = PsiTreeUtil.getParentOfType(container, PyFunction.class, PyClass.class);
      if (parentContainer instanceof PyClass) {
        return super.getUseScope();
      }
      if (parentContainer == null) {
        break;
      }
      container = parentContainer;
    }
    if (container instanceof PyFunction) {
      return new LocalSearchScope(container);
    }
    return super.getUseScope();
  }
}