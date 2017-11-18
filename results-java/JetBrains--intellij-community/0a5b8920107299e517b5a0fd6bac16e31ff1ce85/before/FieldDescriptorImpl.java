package com.intellij.debugger.ui.impl.watch;

import com.intellij.debugger.DebuggerContext;
import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.DebuggerManagerThreadImpl;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluateExceptionUtil;
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.PositionUtil;
import com.intellij.debugger.impl.PositionUtil;
import com.intellij.debugger.ui.tree.FieldDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.sun.jdi.*;

public class FieldDescriptorImpl extends ValueDescriptorImpl implements FieldDescriptor{
  private static final Logger LOG = Logger.getInstance("#com.intellij.debugger.ui.impl.watch.FieldDescriptorImpl");
  private final Field myField;
  private ObjectReference myObject;
  private Boolean myIsPrimitive = null;
  private final boolean myIsStatic;

  public FieldDescriptorImpl(Project project, ObjectReference objRef, Field field) {
    super(project);
    myObject = objRef;
    myField = field;
    myIsStatic = field.isStatic();
    setLvalue(!field.isFinal());
    LOG.assertTrue(myField != null);
  }

  public Field getField() {
    return myField;
  }

  public ObjectReference getObject() {
    return myObject;
  }

  public SourcePosition getSourcePosition(final Project project, final DebuggerContextImpl context) {
    if (context.getFrameProxy() == null) return null;
    final ReferenceType type = myField.declaringType();
    final PsiManager psiManager = PsiManager.getInstance(project);
    final String fieldName = myField.name();
    if (fieldName.startsWith("val$")) {
      // this field actually mirrors a local variable in the outer class
      String varName = fieldName.substring(fieldName.lastIndexOf('$') + 1);
      PsiElement element = PositionUtil.getContextElement(context);
      if (element == null) {
        return null;
      }
      PsiClass aClass = PsiTreeUtil.getParentOfType(element, PsiClass.class, false);
      if (aClass == null) {
        return null;
      }
      aClass = (PsiClass) aClass.getNavigationElement();
      PsiVariable psiVariable = psiManager.getResolveHelper().resolveReferencedVariable(varName, aClass);
      if (psiVariable == null) {
        return null;
      }
      return SourcePosition.createFromOffset(psiVariable.getContainingFile(), psiVariable.getTextOffset());
    }
    else {
      PsiClass aClass = psiManager.findClass(type.name().replace('$', '.'), GlobalSearchScope.allScope(myProject));
      if (aClass == null) return null;
      aClass = (PsiClass) aClass.getNavigationElement();
      PsiField[] fields = aClass.getFields();
      for (int i = 0; i < fields.length; i++) {
        PsiField field = fields[i];
        if (field.getName().equals(fieldName)) {
          return SourcePosition.createFromOffset(field.getContainingFile(), field.getTextOffset());
        }
      }
      return null;
    }
  }

  public void setAncestor(NodeDescriptorImpl oldDescriptor) {
    super.setAncestor(oldDescriptor);
    final Boolean isPrimitive = ((FieldDescriptorImpl)oldDescriptor).myIsPrimitive;
    if (isPrimitive != null) { // was cached
      // do not loose cached info
      myIsPrimitive = isPrimitive;
    }
  }

  public boolean isPrimitive() {
    if (myIsPrimitive == null) {
      try {
        myIsPrimitive = (myField.type() instanceof PrimitiveType)? Boolean.TRUE : Boolean.FALSE;
      }
      catch (Exception e) {
        return super.isPrimitive();
      }
    }
    return myIsPrimitive.booleanValue();
  }

  public Value calcValue(EvaluationContextImpl evaluationContext) throws EvaluateException {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    if ((myObject != null)? !VirtualMachineProxyImpl.isCollected(myObject) : true) {
      return (myObject != null) ? myObject.getValue(myField) : myField.declaringType().getValue(myField);
    }
    else {
      throw EvaluateExceptionUtil.OBJECT_WAS_COLLECTED;
    }
  }

  public boolean isStatic() {
    return myIsStatic;
  }

  public String getName() {
    return myField.name();
  }

  public String calcValueName() {
    return getName() + ": " + myField.typeName();
  }

  public PsiExpression getDescriptorEvaluation(DebuggerContext context) throws EvaluateException {
    PsiElementFactory elementFactory = PsiManager.getInstance(context.getProject()).getElementFactory();
    String fieldName;
    if(isStatic()) {
      String typeName = myField.declaringType().name().replace('$', '.');
      typeName = DebuggerTreeNodeExpression.normalize(typeName, PositionUtil.getContextElement(context), context.getProject());
      fieldName = typeName + "." + getName();
    }
    else {
      fieldName = "this." + getName();
    }
    try {
      return (PsiReferenceExpression)elementFactory.createExpressionFromText(fieldName, null);
    }
    catch (IncorrectOperationException e) {
      throw new EvaluateException("Invalid field name '" + getName() + "'", e);
    }
  }
}