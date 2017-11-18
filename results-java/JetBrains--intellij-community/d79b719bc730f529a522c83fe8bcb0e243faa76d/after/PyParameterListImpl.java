package com.jetbrains.python.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.PyElementTypes;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.stubs.PyParameterListStub;
import com.jetbrains.python.toolbox.ArrayIterable;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class PyParameterListImpl extends PyBaseElementImpl<PyParameterListStub> implements PyParameterList {
  public PyParameterListImpl(ASTNode astNode) {
    super(astNode);
  }

  public PyParameterListImpl(final PyParameterListStub stub) {
    super(stub, PyElementTypes.PARAMETER_LIST);
  }

  @Override
  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyParameterList(this);
  }

  public PyParameter[] getParameters() {
    return getStubOrPsiChildren(PyElementTypes.PARAMETERS, new PyParameter[0]);
  }

  public void addParameter(final PyNamedParameter param) {
    PsiElement paren = getLastChild();
    if (paren != null && ")".equals(paren.getText())) {
      PyUtil.ensureWritable(this);
      ASTNode beforeWhat = paren.getNode(); // the closing paren will be this
      PyParameter[] params = getParameters();
      PyUtil.addListNode(this, param, beforeWhat, true, params.length == 0);
    }
  }

  public boolean hasPositionalContainer() {
    for (PyParameter parameter: getParameters()) {
      if (parameter instanceof PyNamedParameter && ((PyNamedParameter) parameter).isPositionalContainer()) {
        return true;
      }
    }
    return false;
  }

  public boolean hasKeywordContainer() {
    for (PyParameter parameter: getParameters()) {
      if (parameter instanceof PyNamedParameter && ((PyNamedParameter) parameter).isKeywordContainer()) {
        return true;
      }
    }
    return false;
  }

  public boolean isCompatibleTo(@NotNull PyParameterList another) {
    PyParameter[] parameters = getParameters();
    final PyParameter[] anotherParameters = another.getParameters();
    final int parametersLength = parameters.length;
    final int anotherParametersLength = anotherParameters.length;
    if (parametersLength == anotherParametersLength) {
      if (hasPositionalContainer() == another.hasPositionalContainer() && hasKeywordContainer() == another.hasKeywordContainer()) {
        return true;
      }
    }

    int i = 0;
    int j = 0;
    while (i < parametersLength && j < anotherParametersLength) {
      PyParameter parameter = parameters[i];
      PyParameter anotherParameter = anotherParameters[j];
      if (parameter instanceof PyNamedParameter && anotherParameter instanceof PyNamedParameter) {
        PyNamedParameter namedParameter = (PyNamedParameter)parameter;
        PyNamedParameter anotherNamedParameter = (PyNamedParameter)anotherParameter;

        if (namedParameter.isPositionalContainer()) {
          while (j < anotherParametersLength
                 && !anotherNamedParameter.isPositionalContainer()
                 && !anotherNamedParameter.isKeywordContainer()) {
            anotherParameter = anotherParameters[j++];
            anotherNamedParameter = (PyNamedParameter) anotherParameter;
          }
          ++i;
          continue;
        }

        if (anotherNamedParameter.isPositionalContainer()) {
          while (i < parametersLength
                 && !namedParameter.isPositionalContainer()
                 && !namedParameter.isKeywordContainer()) {
            parameter = parameters[i++];
            namedParameter = (PyNamedParameter) parameter;
          }
          ++j;
          continue;
        }

        if (namedParameter.isKeywordContainer() || anotherNamedParameter.isKeywordContainer()) {
          break;
        }
      }

      // both are simple parameters
      ++i;
      ++j;
    }

    if (i < parametersLength) {
      if (parameters[i] instanceof PyNamedParameter) {
        if (((PyNamedParameter) parameters[i]).isKeywordContainer()) {
          ++i;
        }
      }
    }

    if (j < anotherParametersLength) {
      if (anotherParameters[j] instanceof PyNamedParameter) {
        if (((PyNamedParameter) anotherParameters[j]).isKeywordContainer()) {
          ++j;
        }
      }
    }
    return (i >= parametersLength) && (j >= anotherParametersLength);
    //
    //if (weHaveStarred && parameters.length - 1 <= anotherParameters.length) {
    //  if (weHaveDoubleStarred == anotherHasDoubleStarred) {
    //    return true;
    //  }
    //}
    //if ((anotherHasDoubleStarred && parameters.length == anotherParameters.length - 1)
    //  || (weHaveDoubleStarred && parameters.length == anotherParameters.length + 1)) {
    //  return true;
    //}
    //return false;
  }

  @NotNull
  public Iterable<PyElement> iterateNames() {
    return new ArrayIterable<PyElement>(getParameters());
  }

  public PyElement getElementNamed(final String the_name) {
    return IterHelper.findName(iterateNames(), the_name);
  }

  public boolean mustResolveOutside() {
    return false;  // we don't exactly have children to resolve, but if we did...
  }
}