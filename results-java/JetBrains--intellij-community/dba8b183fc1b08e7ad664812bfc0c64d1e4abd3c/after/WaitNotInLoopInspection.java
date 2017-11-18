package com.siyeh.ig.threading;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.psi.*;
import com.siyeh.ig.*;
import com.siyeh.ig.psiutils.ControlFlowUtils;

public class WaitNotInLoopInspection extends ExpressionInspection {

    public String getDisplayName() {
        return "'wait()' not in loop";
    }

    public String getGroupDisplayName() {
        return GroupNames.THREADING_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return "Call to '#ref()' is not made in a loop #loc";
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager, boolean onTheFly) {
        return new WaitNotInLoopVisitor(this, inspectionManager, onTheFly);
    }

    private static class WaitNotInLoopVisitor extends BaseInspectionVisitor {
        private WaitNotInLoopVisitor(BaseInspection inspection, InspectionManager inspectionManager, boolean isOnTheFly) {
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            super.visitMethodCallExpression(expression);
            final PsiReferenceExpression methodExpression = expression.getMethodExpression();
            if (methodExpression == null) {
                return;
            }
            final String methodName = methodExpression.getReferenceName();
            if (!"wait".equals(methodName)) {
                return;
            }
            final PsiMethod method = expression.resolveMethod();
            if (method == null) {
                return;
            }
            final PsiParameterList paramList = method.getParameterList();
            if (paramList == null) {
                return;
            }
            final PsiParameter[] parameters = paramList.getParameters();
            final int numParams = parameters.length;
            if (numParams > 2) {
                return;
            }
            if (numParams > 0) {
                final PsiType parameterType = parameters[0].getType();
                if (!parameterType.equals(PsiType.LONG)) {
                    return;
                }
            }

            if (numParams > 1) {
                final PsiType parameterType = parameters[1].getType();
                if (!parameterType.equals(PsiType.INT)) {
                    return;
                }
            }

            if (ControlFlowUtils.isInLoop(expression)) {
                return;
            }
            registerMethodCallError(expression);
        }
    }

}