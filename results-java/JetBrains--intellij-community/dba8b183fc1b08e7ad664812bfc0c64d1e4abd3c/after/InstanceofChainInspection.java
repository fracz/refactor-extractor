package com.siyeh.ig.abstraction;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.psi.*;
import com.siyeh.ig.*;

public class InstanceofChainInspection extends StatementInspection {

    public String getID(){
        return "ChainOfInstanceofChecks";
    }

    public String getDisplayName() {
        return "Chain of 'instanceof' checks";
    }

    public String getGroupDisplayName() {
        return GroupNames.ABSTRACTION_GROUP_NAME;
    }

    protected String buildErrorString(PsiElement location) {
        return "Chain of 'instanceof' checks indicates abstraction failure #ref #loc";
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager, boolean onTheFly) {
        return new InstanceofChainVisitor(this, inspectionManager, onTheFly);
    }

    private static class InstanceofChainVisitor extends StatementInspectionVisitor {
        private InstanceofChainVisitor(BaseInspection inspection, InspectionManager inspectionManager, boolean isOnTheFly) {
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitIfStatement(PsiIfStatement statement) {
            super.visitIfStatement(statement);
            final PsiElement parent = statement.getParent();
            if (parent instanceof PsiIfStatement) {
                final PsiIfStatement parentStatement = (PsiIfStatement) parent;
                final PsiStatement elseBranch = parentStatement.getElseBranch();
                if (statement.equals(elseBranch)) {
                    return;
                }
            }
            int numChecks = 0;
            PsiIfStatement branch = statement;
            while (branch != null) {
                final PsiExpression condition = branch.getCondition();
                if (!isInstanceofCheck(condition)) {
                    return;
                }
                numChecks++;

                final PsiStatement elseBranch = branch.getElseBranch();
                if (elseBranch instanceof PsiIfStatement) {
                    branch = (PsiIfStatement) elseBranch;
                } else {
                    branch = null;
                }
            }
            if (numChecks < 2) {
                return;
            }
            registerStatementError(statement);
        }

        private boolean isInstanceofCheck(PsiExpression condition) {
            if (condition == null) {
                return false;
            } else if (condition instanceof PsiInstanceOfExpression) {
                return true;
            } else if (condition instanceof PsiBinaryExpression) {
                final PsiBinaryExpression binaryExpression = (PsiBinaryExpression) condition;
                final PsiExpression lhs = binaryExpression.getLOperand();
                final PsiExpression rhs = binaryExpression.getROperand();
                return isInstanceofCheck(lhs) && isInstanceofCheck(rhs);
            } else if (condition instanceof PsiParenthesizedExpression) {
                final PsiExpression contents = ((PsiParenthesizedExpression) condition).getExpression();
                return isInstanceofCheck(contents);
            } else if (condition instanceof PsiPrefixExpression) {
                final PsiExpression contents = ((PsiPrefixExpression) condition).getOperand();
                return isInstanceofCheck(contents);
            } else if (condition instanceof PsiPostfixExpression) {
                final PsiExpression contents = ((PsiPostfixExpression) condition).getOperand();
                return isInstanceofCheck(contents);
            }
            return false;
        }

    }
}