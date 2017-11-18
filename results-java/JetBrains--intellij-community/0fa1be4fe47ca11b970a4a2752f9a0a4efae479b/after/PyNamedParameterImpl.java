package com.jetbrains.python.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.psi.PsiElement;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PyElementTypes;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.stubs.PyNamedParameterStub;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Set;

/**
 * @author yole
 */
public class PyNamedParameterImpl extends PyPresentableElementImpl<PyNamedParameterStub> implements PyNamedParameter {
  public PyNamedParameterImpl(ASTNode astNode) {
    super(astNode);
  }

  public PyNamedParameterImpl(final PyNamedParameterStub stub) {
    super(stub, PyElementTypes.NAMED_PARAMETER);
  }

  @Nullable
  @Override
  public String getName() {
    final PyNamedParameterStub stub = getStub();
    if (stub != null) {
      return stub.getName();
    }
    else {
      ASTNode node = getNode().findChildByType(PyTokenTypes.IDENTIFIER);
      return node != null ? node.getText() : null;
    }
  }

  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    final ASTNode nameElement = PyElementGenerator.getInstance(getProject()).createNameIdentifier(name);
    getNode().replaceChild(getNode().getFirstChildNode(), nameElement);
    return this;
  }

  @Override
  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyNamedParameter(this);
  }

  public boolean isPositionalContainer() {
    final PyNamedParameterStub stub = getStub();
    if (stub != null) {
      return stub.isPositionalContainer();
    }
    else {
      return getNode().findChildByType(PyTokenTypes.MULT) != null;
    }
  }

  public boolean isKeywordContainer() {
    final PyNamedParameterStub stub = getStub();
    if (stub != null) {
      return stub.isKeywordContainer();
    }
    else {
      return getNode().findChildByType(PyTokenTypes.EXP) != null;
    }
  }

  @Nullable
  public PyExpression getDefaultValue() {
    ASTNode[] nodes = getNode().getChildren(PyElementTypes.EXPRESSIONS);
    if (nodes.length > 0) {
      return (PyExpression)nodes[0].getPsi();
    }
    return null;
  }

  public boolean hasDefaultValue() {
    final PyNamedParameterStub stub = getStub();
    if (stub != null) {
      return stub.hasDefaultValue();
    }
    return getDefaultValue() != null;
  }

  @NotNull
  public String getRepr(boolean includeDefaultValue) {
    StringBuffer sb = new StringBuffer();
    if (isPositionalContainer()) sb.append("*");
    else if (isKeywordContainer()) sb.append("**");
    sb.append(getName());
    if (includeDefaultValue) {
      PyExpression default_v = getDefaultValue();
      if (default_v != null) sb.append("=").append(PyUtil.getReadableRepr(default_v, true));
    }
    return sb.toString();
  }

  public Icon getIcon(final int flags) {
    return Icons.PARAMETER_ICON;
  }

  public PyNamedParameter getAsNamed() {
    return this;
  }

  public PyTupleParameter getAsTuple() {
    return null; // we're not a tuple
  }

  public PyType getType() {
    if (getParent() instanceof PyParameterList) {
      PyParameterList parameterList = (PyParameterList) getParent();
      final PyParameter[] params = parameterList.getParameters();
      if (parameterList.getParent() instanceof PyFunction) {
        PyFunction func = (PyFunction) parameterList.getParent();
        final Set<PyFunction.Flag> flags = PyUtil.detectDecorationsAndWrappersOf(func);
        if (params [0] == this && !flags.contains(PyFunction.Flag.CLASSMETHOD) && !flags.contains(PyFunction.Flag.STATICMETHOD)) {
          // must be 'self'
          final PyClass containingClass = func.getContainingClass();
          if (containingClass != null) {
            return new PyClassType(containingClass, false);
          }
        }
        if (isKeywordContainer()) {
          return PyBuiltinCache.getInstance(this).getDictType();
        }
        if (isPositionalContainer()) {
          return PyBuiltinCache.getInstance(this).getTupleType();
        }
        for(PyTypeProvider provider: Extensions.getExtensions(PyTypeProvider.EP_NAME)) {
          PyType result = provider.getParameterType(this, func);
          if (result != null) return result;
        }
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return super.toString() + "('" + getName() + "')";
  }
}