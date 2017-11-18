package com.siyeh.ig.jdk;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.siyeh.ig.*;

public class ForeachStatementInspection extends StatementInspection{
    private final ForEachFix fix = new ForEachFix();

    public String getDisplayName(){
        return "Extended 'for' statement";
    }

    public String getGroupDisplayName(){
        return GroupNames.JDK_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location){
        return "'Extended #ref' statement #loc";
    }

    protected InspectionGadgetsFix buildFix(PsiElement location){
        return fix;
    }

    private static class ForEachFix extends InspectionGadgetsFix{
        public String getName(){
            return "Replace with old-style 'for' statement";
        }

        public void applyFix(Project project, ProblemDescriptor descriptor){
            if(isQuickFixOnReadOnlyFile(project, descriptor)){
                return;
            }
            final PsiForeachStatement statement =
                    (PsiForeachStatement) descriptor.getPsiElement()
                            .getParent();

            final StringBuffer newStatement = new StringBuffer();
            final CodeStyleManager codeStyleManager =
                    CodeStyleManager.getInstance(project);
            final PsiExpression iteratedValue = statement.getIteratedValue();
            if(iteratedValue.getType() instanceof PsiArrayType){
                final String index = codeStyleManager.suggestUniqueVariableName("i",
                                                                   statement,
                                                                   true);
                newStatement.append("for(int " + index + " = 0;" + index + '<');
                newStatement.append(iteratedValue.getText());
                newStatement.append(".length;" + index + "++)");
                newStatement.append("{ ");
                newStatement.append(statement.getIterationParameter().getType()
                                            .getPresentableText());
                newStatement.append(' ');
                newStatement.append(statement.getIterationParameter()
                                            .getName());
                newStatement.append(" = ");
                newStatement.append(iteratedValue.getText());
                newStatement.append('[' + index + "];");
                final PsiStatement body = statement.getBody();
                if(body instanceof PsiBlockStatement){
                    final PsiCodeBlock block =
                            ((PsiBlockStatement) body).getCodeBlock();
                    final PsiElement[] children =
                            block.getChildren();
                    for(int i = 1; i < children.length - 1; i++){
                        //skip the braces
                        newStatement.append(children[i].getText());
                    }
                } else{
                    newStatement.append(body.getText());
                }
                newStatement.append('}');
            } else{

                final String iterator =  codeStyleManager.suggestUniqueVariableName("it", statement,
                                                                  true);
                final String typeText = statement.getIterationParameter()
                                .getType()
                                .getPresentableText();
                newStatement.append("for(java.util.Iterator<");
                newStatement.append(typeText);
                newStatement.append("> " + iterator + " = ");
                newStatement.append(iteratedValue.getText());
                newStatement.append(".iterator();" + iterator + ".hasNext();)");
                newStatement.append('{');
                newStatement.append(typeText);
                newStatement.append(' ');
                newStatement.append(statement.getIterationParameter()
                                            .getName());
                newStatement.append(" = ");
                newStatement.append(iterator + ".next();");

                final PsiStatement body = statement.getBody();
                if(body instanceof PsiBlockStatement){
                    final PsiCodeBlock block = ((PsiBlockStatement) body).getCodeBlock();
                    final PsiElement[] children = block.getChildren();
                    for(int i = 1; i < children.length - 1; i++){
                        //skip the braces
                        newStatement.append(children[i].getText());
                    }
                } else{
                    newStatement.append(body.getText());
                }
                newStatement.append('}');
            }
            replaceStatement(project, statement, newStatement.toString());
        }
    }


    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager,
                                               boolean onTheFly){
        return new ForeachStatementVisitor(this, inspectionManager, onTheFly);
    }

    private static class ForeachStatementVisitor extends StatementInspectionVisitor{
        private ForeachStatementVisitor(BaseInspection inspection,
                                        InspectionManager inspectionManager,
                                        boolean isOnTheFly){
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitForeachStatement(PsiForeachStatement statement){
            super.visitForeachStatement(statement);
            registerStatementError(statement);
        }
    }
}