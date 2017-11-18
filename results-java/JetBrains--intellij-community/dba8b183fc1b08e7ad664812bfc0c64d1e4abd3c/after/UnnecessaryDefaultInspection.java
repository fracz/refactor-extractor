package com.siyeh.ig.verbose;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.psi.*;
import com.siyeh.ig.*;

public class UnnecessaryDefaultInspection extends StatementInspection {

    public String getDisplayName() {
        return "Unnecessary 'default' for enum switch statement";
    }

    public String getGroupDisplayName() {
        return GroupNames.VERBOSE_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return "'#ref' branch is unnecessary #loc";
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager, boolean onTheFly) {
        return new UnnecessaryDefaultVisitor(this, inspectionManager,
                onTheFly);
    }

    private static class UnnecessaryDefaultVisitor
            extends StatementInspectionVisitor {
        private UnnecessaryDefaultVisitor(BaseInspection inspection,
                                          InspectionManager inspectionManager,
                                          boolean isOnTheFly) {
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitSwitchStatement(PsiSwitchStatement statement) {
            super.visitSwitchStatement(statement);
            if (!switchStatementContainsUnnecessaryDefault(statement)) {
                return;
            }
            final PsiCodeBlock body = statement.getBody();
            final PsiStatement[] statements = body.getStatements();
            for (int i = statements.length - 1; i >= 0; i--) {
                final PsiStatement child = statements[i];
                if (child instanceof PsiSwitchLabelStatement) {
                    final PsiSwitchLabelStatement label =
                            (PsiSwitchLabelStatement) child;
                    if (label.isDefaultCase()) {
                        registerStatementError(label);
                    }
                }
            }
        }

        private static boolean switchStatementContainsUnnecessaryDefault(PsiSwitchStatement statement) {
            final PsiExpression expression = statement.getExpression();
            if (expression == null) {
                return false;
            }
            final PsiType type = expression.getType();
            if (type == null) {
                return false;
            }
            if (!(type instanceof PsiClassType)) {
                return false;
            }
            final PsiClass aClass = ((PsiClassType) type).resolve();
            if (aClass == null) {
                return false;
            }
            if (!aClass.isEnum()) {
                return false;
            }
            final PsiCodeBlock body = statement.getBody();
            if (body == null) {
                return false;
            }
            final PsiStatement[] statements = body.getStatements();
            if (statements == null) {
                return false;
            }
            boolean containsDefault = false;
            int numCases = 0;
            for (int i = 0; i < statements.length; i++) {
                final PsiStatement child = statements[i];
                if (child instanceof PsiSwitchLabelStatement) {
                    if (((PsiSwitchLabelStatement) child).isDefaultCase()) {
                        containsDefault = true;
                    } else {
                        numCases++;
                    }
                }
            }
            if (!containsDefault) {
                return false;
            }
            final PsiField[] fields = aClass.getFields();
            int numEnums = 0;
            for (int i = 0; i < fields.length; i++) {
                final PsiField field = fields[i];
                if (field.getType().equals(type)) {
                    numEnums++;
                }

            }
            return numEnums == numCases;
        }
    }

}