package com.siyeh.ig.bugs;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.psi.*;
import com.siyeh.ig.*;

public class TextLabelInSwitchStatementInspection extends StatementInspection {

    public String getDisplayName() {
        return "Text label in 'switch' statement";
    }

    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return "Text label #ref: in 'switch' statement #loc ";
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager, boolean onTheFly) {
        return new TextLabelInSwitchStatementVisitor(this, inspectionManager, onTheFly);
    }

    private static class TextLabelInSwitchStatementVisitor extends StatementInspectionVisitor {
        private TextLabelInSwitchStatementVisitor(BaseInspection inspection, InspectionManager inspectionManager, boolean isOnTheFly) {
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitSwitchStatement(PsiSwitchStatement statement) {
            super.visitSwitchStatement(statement);
            final PsiCodeBlock body = statement.getBody();
            if (body == null) {
                return;
            }
            final PsiStatement[] statements = body.getStatements();
            if (statements == null) {
                return;
            }
            for (int i = 0; i < statements.length; i++) {
                checkForLabel(statements[i]);
            }
        }

        private void checkForLabel(PsiStatement statement) {
            if (!(statement instanceof PsiLabeledStatement)) {
                return;
            }
            final PsiIdentifier label = ((PsiLabeledStatement) statement).getLabelIdentifier();
            registerError(label);
        }
    }

}