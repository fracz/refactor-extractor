package com.siyeh.ig.performance;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.siyeh.ig.*;
import com.siyeh.ig.psiutils.ParenthesesUtils;
import com.siyeh.ig.psiutils.SideEffectChecker;

public class ManualArrayCopyInspection extends ExpressionInspection {
    private final ManualArrayCopyFix fix = new ManualArrayCopyFix();

    public String getDisplayName() {
        return "Manual array copy";
    }

    public String getGroupDisplayName() {
        return GroupNames.PERFORMANCE_GROUP_NAME;
    }

    public boolean isEnabledByDefault(){
        return true;
    }

    public String buildErrorString(PsiElement location) {
        return "Manual array copy #loc";
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager, boolean onTheFly) {
        return new ManualArrayCopyVisitor(this, inspectionManager, onTheFly);
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        return fix;
    }

    private static class ManualArrayCopyFix extends InspectionGadgetsFix {
        public String getName() {
            return "Replace with System.arrayCopy()";
        }

        public void applyFix(Project project, ProblemDescriptor descriptor) {
            if(isQuickFixOnReadOnlyFile(project, descriptor)) return;
            final PsiElement forElement = descriptor.getPsiElement();
            final PsiForStatement forStatement = (PsiForStatement) forElement.getParent();
            final String newExpression = getSystemArrayCopyText(forStatement);
            replaceStatement(project, forStatement, newExpression);
        }

        private static String getSystemArrayCopyText(PsiForStatement forStatement) {
            final PsiBinaryExpression condition = (PsiBinaryExpression) forStatement.getCondition();
            final PsiExpression limit = condition.getROperand();
            final String lengthText = limit.getText();
            final PsiExpressionStatement body = getBody(forStatement);
            final PsiAssignmentExpression assignment = (PsiAssignmentExpression) body.getExpression();
            final PsiArrayAccessExpression lhs = (PsiArrayAccessExpression) assignment.getLExpression();
            final PsiExpression lArray = lhs.getArrayExpression();
            final String toArrayText = lArray.getText();
            final PsiArrayAccessExpression rhs = (PsiArrayAccessExpression) assignment.getRExpression();
            final PsiExpression rArray = rhs.getArrayExpression();
            final String fromArrayText = rArray.getText();
            final PsiExpression rhsIndex = rhs.getIndexExpression();
            final String fromOffsetText = getOffsetText(rhsIndex);
            final PsiExpression lhsIndex = lhs.getIndexExpression();
            final String toOffsetText = getOffsetText(lhsIndex);
            final StringBuffer buffer = new StringBuffer(25 + fromArrayText.length() + toArrayText.length() + lengthText.length());
            buffer.append("System.arraycopy(");
            buffer.append(fromArrayText);
            buffer.append(", ");
            buffer.append(fromOffsetText);
            buffer.append(", ");
            buffer.append(toArrayText);
            buffer.append(", ");
            buffer.append(toOffsetText);
            buffer.append(", ");
            buffer.append(lengthText);
            buffer.append(");");
            return buffer.toString();
        }

        private static String getOffsetText(PsiExpression indexExpression) {
            if (indexExpression instanceof PsiBinaryExpression) {
                final PsiBinaryExpression binaryExp = (PsiBinaryExpression) indexExpression;
                final PsiExpression rhs = binaryExp.getROperand();
                final String rhsText = rhs.getText();
                final PsiJavaToken sign = binaryExp.getOperationSign();
                if (sign.getTokenType().equals(JavaTokenType.MINUS)) {
                    return '-' + rhsText;
                } else {
                    return rhsText;
                }
            } else {
                return "0";
            }
        }

        private static PsiExpressionStatement getBody(PsiForStatement forStatement) {
            PsiStatement body = forStatement.getBody();
            while (body instanceof PsiBlockStatement) {
                final PsiCodeBlock codeBlock = ((PsiBlockStatement) body).getCodeBlock();
                final PsiStatement[] statements = codeBlock.getStatements();
                body = statements[0];
            }
            return (PsiExpressionStatement) body;
        }
    }

    private static class ManualArrayCopyVisitor extends BaseInspectionVisitor {
        private ManualArrayCopyVisitor(BaseInspection inspection, InspectionManager inspectionManager, boolean isOnTheFly) {
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitForStatement(PsiForStatement forStatement) {
            super.visitForStatement(forStatement);
            final PsiStatement initialization = forStatement.getInitialization();
            if (!(initialization instanceof PsiDeclarationStatement)) {
                return;
            }
            final PsiDeclarationStatement declaration = (PsiDeclarationStatement) initialization;
            if (declaration.getDeclaredElements().length!= 1) {
                return;
            }
            final PsiLocalVariable var = (PsiLocalVariable) declaration.getDeclaredElements()[0];
            final PsiExpression initialValue = var.getInitializer();
            if (initialValue != null) {
                final String initializerText = initialValue.getText();
                if (!"0".equals(initializerText)) {
                    return;
                }
            }
            final PsiExpression condition = forStatement.getCondition();
            if (!isComparison(condition, var)) {
                return;
            }
            final PsiStatement update = forStatement.getUpdate();
            if (!isIncrement(update, var)) {
                return;
            }
            final PsiStatement body = forStatement.getBody();
            if (!bodyIsArrayMove(body, var)) {
                return;
            }
            registerStatementError(forStatement);
        }

        private boolean bodyIsArrayMove(PsiStatement body, PsiLocalVariable var) {
            if (body instanceof PsiExpressionStatement) {
                final PsiExpressionStatement exp = (PsiExpressionStatement) body;
                final PsiExpression expression = exp.getExpression();
                if (expression == null) {
                    return false;
                }
                return expressionIsArrayMove(expression, var);
            } else if (body instanceof PsiBlockStatement) {
                final PsiCodeBlock codeBlock = ((PsiBlockStatement) body).getCodeBlock();
                if (codeBlock == null) {
                    return false;
                }
                final PsiStatement[] statements = codeBlock.getStatements();
                if (statements == null) {
                    return false;
                }
                if (statements.length != 1) {
                    return false;
                }
                return bodyIsArrayMove(statements[0], var);
            }
            return false;
        }

        private static boolean expressionIsArrayMove(PsiExpression exp, PsiLocalVariable var) {
            final PsiExpression strippedExpression = ParenthesesUtils.stripParentheses(exp);
            if (strippedExpression == null) {
                return false;
            }
            if (!(strippedExpression instanceof PsiAssignmentExpression)) {
                return false;
            }
            final PsiAssignmentExpression assignment = (PsiAssignmentExpression) strippedExpression;
            final PsiJavaToken sign = assignment.getOperationSign();
            if (sign == null) {
                return false;
            }
            if (!(sign.getTokenType().equals(JavaTokenType.EQ))) {
                return false;
            }
            final PsiExpression lhs = assignment.getLExpression();
            if (lhs == null) {
                return false;
            }
            if (SideEffectChecker.mayHaveSideEffects(lhs)) {
                return false;
            }
            if (!isOffsetArrayAccess(lhs, var)) {
                return false;
            }
            final PsiExpression rhs = assignment.getRExpression();
            if (rhs == null) {
                return false;
            }
            if (SideEffectChecker.mayHaveSideEffects(rhs)) {
                return false;
            }
            return isOffsetArrayAccess(rhs, var);
        }

        private static boolean isOffsetArrayAccess(PsiExpression expression, PsiLocalVariable var) {
            final PsiExpression strippedExpression = ParenthesesUtils.stripParentheses(expression);
            if (!(strippedExpression instanceof PsiArrayAccessExpression)) {
                return false;
            }
            final PsiArrayAccessExpression arrayExp = (PsiArrayAccessExpression) strippedExpression;
            final PsiExpression index = arrayExp.getIndexExpression();
            if (index == null) {
                return false;
            }
            return expressionIsOffsetVariableLookup(index, var);
        }

        private static boolean isIncrement(PsiStatement statement, PsiLocalVariable var) {
            if (!(statement instanceof PsiExpressionStatement)) {
                return false;
            }
            PsiExpression exp = ((PsiExpressionStatement) statement).getExpression();
            exp = ParenthesesUtils.stripParentheses(exp);
            if (exp instanceof PsiPrefixExpression) {
                final PsiPrefixExpression prefixExp = (PsiPrefixExpression) exp;
                final PsiJavaToken sign = prefixExp.getOperationSign();
                if (sign == null) {
                    return false;
                }
                final IElementType tokenType = sign.getTokenType();
                if (!tokenType.equals(JavaTokenType.PLUSPLUS)) {
                    return false;
                }
                final PsiExpression operand = prefixExp.getOperand();
                return expressionIsVariableLookup(operand, var);
            } else if (exp instanceof PsiPostfixExpression) {
                final PsiPostfixExpression postfixExp = (PsiPostfixExpression) exp;
                final PsiJavaToken sign = postfixExp.getOperationSign();
                if (sign == null) {
                    return false;
                }
                final IElementType tokenType = sign.getTokenType();
                if (!tokenType.equals(JavaTokenType.PLUSPLUS)) {
                    return false;
                }
                final PsiExpression operand = postfixExp.getOperand();
                return expressionIsVariableLookup(operand, var);
            }
            return true;
        }

        private static boolean isComparison(PsiExpression condition, PsiLocalVariable var) {
            final PsiExpression strippedCondition = ParenthesesUtils.stripParentheses(condition);

            if (!(strippedCondition instanceof PsiBinaryExpression)) {
                return false;
            }
            final PsiBinaryExpression binaryExp = (PsiBinaryExpression) strippedCondition;
            final PsiJavaToken sign = binaryExp.getOperationSign();
            if (sign == null) {
                return false;
            }
            if (!sign.getTokenType().equals(JavaTokenType.LT)) {
                return false;
            }
            final PsiExpression lhs = binaryExp.getLOperand();
            return expressionIsVariableLookup(lhs, var);
        }

        private static boolean expressionIsVariableLookup(PsiExpression expression, PsiLocalVariable var) {
            final PsiExpression strippedExpression = ParenthesesUtils.stripParentheses(expression);
            if (strippedExpression == null) {
                return false;
            }
            final String expressionText = strippedExpression.getText();
            final String varText = var.getName();
            return expressionText.equals(varText);
        }

        private static boolean expressionIsOffsetVariableLookup(PsiExpression expression, PsiLocalVariable var) {
            final PsiExpression strippedExpression = ParenthesesUtils.stripParentheses(expression);

            if (expressionIsVariableLookup(strippedExpression, var)) {
                return true;
            }
            if (!(strippedExpression instanceof PsiBinaryExpression)) {
                return false;
            }
            final PsiBinaryExpression binaryExp = (PsiBinaryExpression) strippedExpression;
            final PsiExpression lhs = binaryExp.getLOperand();
            if (!expressionIsVariableLookup(lhs, var)) {
                return false;
            }
            final PsiJavaToken sign = binaryExp.getOperationSign();
            if (sign == null) {
                return false;
            }
            final IElementType tokenType = sign.getTokenType();
            return !(!tokenType.equals(JavaTokenType.PLUS) &&
                    !tokenType.equals(JavaTokenType.MINUS));
        }

    }

}