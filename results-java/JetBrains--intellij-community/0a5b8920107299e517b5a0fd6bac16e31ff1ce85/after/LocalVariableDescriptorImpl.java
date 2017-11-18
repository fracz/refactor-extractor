package com.intellij.debugger.ui.impl.watch;

import com.intellij.debugger.DebuggerContext;
import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.PositionUtil;
import com.intellij.debugger.jdi.LocalVariableProxyImpl;
import com.intellij.debugger.jdi.StackFrameProxyImpl;
import com.intellij.debugger.ui.tree.LocalVariableDescriptor;
import com.intellij.debugger.ui.tree.NodeDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.Value;

public class LocalVariableDescriptorImpl extends ValueDescriptorImpl implements LocalVariableDescriptor {
  private static final Logger LOG = Logger.getInstance("#com.intellij.debugger.ui.impl.watch.LocalVariableDescriptorImpl");

  private final StackFrameProxyImpl myFrameProxy;
  private final LocalVariableProxyImpl myLocalVariable;

  private String myTypeName = "<unknown>";
  private boolean myIsPrimitive;

  private boolean myIsNewLocal = true;
  private boolean myIsVisible = true;

  public LocalVariableDescriptorImpl(Project project,
                                     LocalVariableProxyImpl local) {
    super(project);
    setLvalue(true);
    LOG.assertTrue(local      != null);
    myFrameProxy = local.getFrame();
    myLocalVariable = local;
  }

  private boolean calcPrimitive(LocalVariableProxyImpl local, EvaluationContextImpl evaluationContext) throws EvaluateException {
    try {
      return local.getType() instanceof PrimitiveType;
    }
    catch (ClassNotLoadedException e) {
      try {
        evaluationContext.getDebugProcess().loadClass(evaluationContext, myTypeName, evaluationContext.getClassLoader());
        return local.getType() instanceof PrimitiveType;
      }
      catch (Exception e1) {
        LOG.debug(e1);
      }
    }
    return false;
  }

  public LocalVariableProxyImpl getLocalVariable() {
    return myLocalVariable;
  }

  public SourcePosition getSourcePosition(final Project project, final DebuggerContextImpl context) {
    StackFrameProxyImpl frame = context.getFrameProxy();
    if (frame == null) return null;

    PsiElement place = PositionUtil.getContextElement(context);

    if (place == null) {
      return null;
    }

    PsiVariable psiVariable = PsiManager.getInstance(project).getResolveHelper().resolveReferencedVariable(getName(), place);
    if (psiVariable == null) {
      return null;
    }

    PsiFile containingFile = psiVariable.getContainingFile();
    if(containingFile == null) return null;

    return SourcePosition.createFromOffset(containingFile, psiVariable.getTextOffset());
  }

  public boolean isNewLocal() {
    return myIsNewLocal;
  }

  public boolean isPrimitive() {
    return myIsPrimitive;
  }

  public Value calcValue(EvaluationContextImpl evaluationContext) throws EvaluateException {
    myIsVisible = myFrameProxy.isLocalVariableVisible(getLocalVariable());
    if (myIsVisible) {
      myTypeName = getLocalVariable().getVariable().typeName();
      myIsPrimitive = calcPrimitive(getLocalVariable(), evaluationContext);
      return myFrameProxy.getValue(getLocalVariable());
    }

    return null;
  }

  public void setNewLocal(boolean aNew) {
    myIsNewLocal = aNew;
  }

  public void displayAs(NodeDescriptor descriptor) {
    super.displayAs(descriptor);
    if(descriptor instanceof LocalVariableDescriptorImpl) {
      myIsNewLocal = ((LocalVariableDescriptorImpl)descriptor).myIsNewLocal;
    }
  }

  public String getName() {
    return myLocalVariable.name();
  }

  public String calcValueName() {
    if (myIsVisible) {
      return getName() + ": " + myTypeName;
    }
    return getName() + ": " + myTypeName;
  }

  public PsiExpression getDescriptorEvaluation(DebuggerContext context) throws EvaluateException {
    PsiElementFactory elementFactory = PsiManager.getInstance(context.getProject()).getElementFactory();
    try {
      return elementFactory.createExpressionFromText(getName(), PositionUtil.getContextElement(context));
    }
    catch (IncorrectOperationException e) {
      throw new EvaluateException("Invalid local variable name '" + getName() + "'", e);
    }
  }
}