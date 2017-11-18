package com.siyeh.ig.bugs;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.*;
import com.siyeh.ig.psiutils.ComparisonUtils;
import com.siyeh.ig.psiutils.ParenthesesUtils;
import com.siyeh.ig.psiutils.TypeUtils;
import com.siyeh.ig.psiutils.WellFormednessUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ObjectEqualityInspection extends ExpressionInspection {
    /** @noinspection PublicField*/
    public boolean m_ignoreEnums = false;
    /** @noinspection PublicField*/
    public boolean m_ignoreClassObjects = false;

    private final EqualityToEqualsFix fix = new EqualityToEqualsFix();

    public String getDisplayName() {
        return "Object comparison using ==, instead of '.equals()'";
    }

    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    public JComponent createOptionsPanel() {
        final GridBagLayout layout = new GridBagLayout();
        final JPanel panel = new JPanel(layout);
        final JCheckBox arrayCheckBox = new JCheckBox("Ignore == between enumerated types", m_ignoreEnums);
        final ButtonModel enumeratedObjectModel = arrayCheckBox.getModel();
        enumeratedObjectModel.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                m_ignoreEnums = enumeratedObjectModel.isSelected();
            }
        });
        final JCheckBox classObjectCheckbox = new JCheckBox("Ignore == on java.lang.Class objects", m_ignoreClassObjects);
        final ButtonModel classObjectModel = classObjectCheckbox.getModel();
        classObjectModel.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                m_ignoreClassObjects = classObjectModel.isSelected();
            }
        });
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(arrayCheckBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(classObjectCheckbox, constraints);
        return panel;
    }

    public String buildErrorString(PsiElement location) {
        return "Object values are compared using '#ref', not '.equals()' #loc";
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager, boolean onTheFly) {
        return new ObjectEqualityVisitor(this, inspectionManager, onTheFly);
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        return fix;
    }

    private static class EqualityToEqualsFix extends InspectionGadgetsFix {
        public String getName() {
            return "Replace with .equals()";
        }

        public void applyFix(Project project, ProblemDescriptor descriptor) {
            if(isQuickFixOnReadOnlyFile(project, descriptor)) return;
            final PsiElement comparisonToken = descriptor.getPsiElement();
            final PsiBinaryExpression
                    expression = (PsiBinaryExpression) comparisonToken.getParent();
            boolean negated = false;
            final PsiJavaToken sign = expression.getOperationSign();
            if (sign == null) {
                return;
            }
            final IElementType tokenType = sign.getTokenType();
            if (JavaTokenType.NE.equals(tokenType)) {
                negated = true;
            }
            final PsiExpression lhs = expression.getLOperand();
            if (lhs == null) {
                return;
            }
            final PsiExpression strippedLhs = ParenthesesUtils.stripParentheses(lhs);
            final PsiExpression rhs = expression.getROperand();
            if (rhs == null) {
                return;
            }
            final PsiExpression strippedRhs = ParenthesesUtils.stripParentheses(rhs);

            final String expString;
            if (ParenthesesUtils.getPrecendence(strippedLhs) > ParenthesesUtils.METHOD_CALL_PRECEDENCE) {
                expString = '(' + strippedLhs.getText() + ").equals(" + strippedRhs.getText() + ')';
            } else {
                expString = strippedLhs.getText() + ".equals(" + strippedRhs.getText() + ')';
            }
            final String newExpression;
            if (negated) {
                newExpression = '!' + expString;
            } else {
                newExpression = expString;
            }
            replaceExpression(project, expression, newExpression);
        }
    }

    private class ObjectEqualityVisitor extends BaseInspectionVisitor {
        private ObjectEqualityVisitor(BaseInspection inspection, InspectionManager inspectionManager, boolean isOnTheFly) {
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitBinaryExpression(PsiBinaryExpression expression) {
            super.visitBinaryExpression(expression);
            if(!WellFormednessUtils.isWellFormed(expression)){
                return;
            }
            if (!ComparisonUtils.isEqualityComparison(expression)) {
                return;
            }
            final PsiExpression rhs = expression.getROperand();
            if (!isObjectType(rhs)) {
                return;
            }

            final PsiExpression lhs = expression.getLOperand();
            if (!isObjectType(lhs)) {
                return;
            }
            if (m_ignoreEnums && (isEnumType(rhs) || isEnumType(lhs))) {
                return;
            }
            if (m_ignoreClassObjects &&
                        (TypeUtils.expressionHasType("java.lang.Class", rhs) || TypeUtils.expressionHasType("java.lang.Class", lhs))) {
                return;
            }
            final PsiMethod method =
                    (PsiMethod) PsiTreeUtil.getParentOfType(expression,
                                                            PsiMethod.class);
            if(method != null) {
                final String methodName = method.getName();
                if ("equals".equals(methodName)) {
                    return;
                }
            }
            final PsiJavaToken sign = expression.getOperationSign();
            registerError(sign);
        }

        private boolean isEnumType(PsiExpression exp) {
            if (exp == null) {
                return false;
            }

            final PsiType type = exp.getType();
            if (type == null) {
                return false;
            }
            if(!(type instanceof PsiClassType))
            {
                return false;
            }
            final PsiClass aClass = ((PsiClassType)type).resolve();
            if(aClass == null)
            {
                return false;
            }
            return aClass.isEnum();
        }

        private  boolean isObjectType(PsiExpression exp) {
            if (exp == null) {
                return false;
            }

            final PsiType type = exp.getType();
            if (type == null) {
                return false;
            }
            return !(type instanceof PsiPrimitiveType)
                    && !TypeUtils.isJavaLangString(type);
        }

    }

}