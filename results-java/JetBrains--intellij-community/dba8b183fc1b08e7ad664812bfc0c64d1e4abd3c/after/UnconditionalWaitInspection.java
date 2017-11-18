package com.siyeh.ig.threading;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.GroupNames;
import com.siyeh.ig.MethodInspection;

public class UnconditionalWaitInspection extends MethodInspection {

    public String getDisplayName() {
        return "Unconditional 'wait()' call";
    }

    public String getGroupDisplayName() {
        return GroupNames.THREADING_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return "Unconditional call to '#ref()' #loc";
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager, boolean onTheFly) {
        return new UnconditionalWaitVisitor(this, inspectionManager, onTheFly);
    }

    private static class UnconditionalWaitVisitor extends BaseInspectionVisitor {
        private UnconditionalWaitVisitor(BaseInspection inspection, InspectionManager inspectionManager, boolean isOnTheFly) {
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitMethod(PsiMethod method) {
            super.visitMethod(method);
            if (!method.hasModifierProperty(PsiModifier.SYNCHRONIZED)) {
                return;
            }
            final PsiCodeBlock body = method.getBody();
            if (body != null) {
                checkBody(body);
            }
        }

        public void visitSynchronizedStatement(PsiSynchronizedStatement statement) {
            super.visitSynchronizedStatement(statement);
            final PsiCodeBlock body = statement.getBody();
            if (body != null) {
                checkBody(body);
            }
        }

        private void checkBody(PsiCodeBlock body) {
            final PsiStatement[] statements = body.getStatements();
            if (statements == null) {
                return;
            }
            if (statements.length == 0) {
                return;
            }
            for (int i = 0; i < statements.length; i++) {
                final PsiStatement statement = statements[i];
                if (isConditional(statement)) {
                    return;
                }
                if (!(statement instanceof PsiExpressionStatement)) {
                    continue;
                }
                final PsiExpression firstExpression =
                        ((PsiExpressionStatement) statement).getExpression();
                if (!(firstExpression instanceof PsiMethodCallExpression)) {
                    continue;
                }
                final PsiMethodCallExpression methodCallExpression =
                        (PsiMethodCallExpression) firstExpression;
                final PsiReferenceExpression methodExpression =
                        methodCallExpression.getMethodExpression();
                if (methodExpression == null) {
                    continue;
                }
                final String methodName = methodExpression.getReferenceName();

                if (!"wait".equals(methodName)) {
                    continue;
                }
                final PsiMethod method = methodCallExpression.resolveMethod();
                if (method == null) {
                    continue;
                }
                final PsiParameterList paramList = method.getParameterList();
                if (paramList == null) {
                    continue;
                }
                final PsiParameter[] parameters = paramList.getParameters();
                final int numParams = parameters.length;
                if (numParams > 2) {
                    continue;
                }
                if (numParams > 0) {
                    final PsiType parameterType = parameters[0].getType();
                    if (!parameterType.equals(PsiType.LONG)) {
                        continue;
                    }
                }

                if (numParams > 1) {
                    final PsiType parameterType = parameters[1].getType();
                    if (!parameterType.equals(PsiType.INT)) {
                        continue;
                    }
                }
                registerMethodCallError(methodCallExpression);
            }
        }

        private static boolean isConditional(PsiStatement statement) {
            return statement instanceof PsiIfStatement;

        }
    }

}