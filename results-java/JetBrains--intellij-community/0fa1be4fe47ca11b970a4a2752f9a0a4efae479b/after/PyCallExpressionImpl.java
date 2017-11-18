package com.jetbrains.python.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class PyCallExpressionImpl extends PyElementImpl implements PyCallExpression {

  public PyCallExpressionImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyCallExpression(this);
  }

  @PsiCached
  @Nullable
  public PyExpression getCallee() {
    //return PsiTreeUtil.getChildOfType(this, PyReferenceExpression.class); what we call can be whatever expr, not always a ref
    return (PyExpression)getFirstChild();
  }

  @PsiCached
  public PyArgumentList getArgumentList() {
    return PsiTreeUtil.getChildOfType(this, PyArgumentList.class);
  }

  @NotNull
  public PyExpression[] getArguments() {
    final PyArgumentList argList = getArgumentList();
    return argList != null ? argList.getArguments() : PyExpression.EMPTY_ARRAY;
  }

  public void addArgument(PyExpression expression) {
    PyCallExpressionHelper.addArgument(this, expression);
  }

  public PyMarkedFunction resolveCallee() {
    return PyCallExpressionHelper.resolveCallee(this);
  }

  @Override
  public String toString() {
    return "PyCallExpression: " + PyUtil.getReadableRepr(getCallee(), true); //or: getCalledFunctionReference().getReferencedName();
  }

  public PyType getType() {
    PyExpression callee = getCallee();
    if (callee instanceof PyReferenceExpression) {
      // hardwired special cases
      if ("super".equals(callee.getText())) {
        final PyType superCallType = getSuperCallType(callee);
        if (superCallType != null) {
          return superCallType;
        }
      }
      // normal cases
      ResolveResult[] targets = ((PyReferenceExpression)callee).getReference().multiResolve(false);
      if (targets.length > 0) {
        PsiElement target = targets[0].getElement();
        if (target instanceof PyClass) {
          return new PyClassType((PyClass) target, false); // we call a class name, that is, the constructor, we get an instance.
        }
        else if (target instanceof PyFunction && PyNames.INIT.equals(((PyFunction)target).getName())) {
          return new PyClassType(((PyFunction)target).getContainingClass(), false); // resolved to __init__, back to class
        }
        // TODO: look at well-known functions and their return types
        return PyReferenceExpressionImpl.getReferenceTypeFromProviders(target);
      }
    }
    if (callee == null) return null;
    else return callee.getType();
  }

  private PyType getSuperCallType(PyExpression callee) {
    PsiElement must_be_super_init = ((PyReferenceExpression)callee).getReference().resolve();
    if (must_be_super_init instanceof PyFunction) {
      PyClass must_be_super = ((PyFunction)must_be_super_init).getContainingClass();
      if (must_be_super == PyBuiltinCache.getInstance(this).getClass("super")) {
        PyArgumentList arglist = getArgumentList();
        if (arglist != null) {
          PyExpression[] args = arglist.getArguments();
          if (args.length > 1) {
            PyExpression first_arg = args[0];
            if (first_arg instanceof PyReferenceExpression) {
              PsiElement possible_class = ((PyReferenceExpression)first_arg).getReference().resolve();
              if (possible_class instanceof PyClass && ((PyClass)possible_class).isNewStyleClass()) {
                final PyClass first_class = (PyClass)possible_class;
                // check 2nd argument, too; it should be an instance
                PyExpression second_arg = args[1];
                if (second_arg != null) {
                  PyType second_type = second_arg.getType();
                  if (second_type instanceof PyClassType) {
                    // imitate isinstance(second_arg, possible_class)
                    PyClass second_class = ((PyClassType)second_type).getPyClass();
                    assert second_class != null;
                    if (first_class == second_class) {
                      final PyClass[] supers = first_class.getSuperClasses();
                      if (supers.length > 0) {
                        return new PyClassType(supers [0], false);
                      }
                    }
                    if (second_class.isSubclass(first_class)) {
                      // TODO: super(Foo, Bar) is a superclass of Foo directly preceding Bar in MRO
                      return new PyClassType(first_class, false); // super(Foo, self) has type of Foo, modulo __get__()
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return null;
  }
}