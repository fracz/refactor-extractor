package com.siyeh.ig.verbose;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UnnecessaryBlockStatementInspection extends StatementInspection{
    private final UnnecessaryBlockFix fix = new UnnecessaryBlockFix();

    public String getID(){
        return "UnnecessaryCodeBlock";
    }

    public String getDisplayName(){
        return "Unnecessary code block";
    }

    public String getGroupDisplayName(){
        return GroupNames.VERBOSE_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location){
        return "Braces around this statement are unnecessary #loc";
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager,
                                               boolean onTheFly){
        return new UnnecessaryBlockStatementVisitor(this, inspectionManager,
                                                    onTheFly);
    }

    public InspectionGadgetsFix buildFix(PsiElement location){
        return fix;
    }

    private static class UnnecessaryBlockFix extends InspectionGadgetsFix{
        public String getName(){
            return "Unwrap block";
        }

        public void applyFix(Project project, ProblemDescriptor descriptor){
            if(isQuickFixOnReadOnlyFile(project, descriptor)){
                return;
            }
            final PsiElement leftBrace = descriptor.getPsiElement();
            final PsiCodeBlock block = (PsiCodeBlock) leftBrace.getParent();
            final PsiBlockStatement blockStatement =
                    (PsiBlockStatement) block.getParent();
            final PsiElement containingElement = blockStatement.getParent();
            try{
                final PsiElement[] children = block.getChildren();
                if(children.length > 2){
                    final PsiElement added =
                            containingElement.addRangeBefore(children[1],
                                                             children[children.length -
                            2],
                                                             blockStatement);
                    final CodeStyleManager codeStyleManager =
                            CodeStyleManager.getInstance(project);
                    codeStyleManager.reformat(added);
                }
                blockStatement.delete();
            } catch(IncorrectOperationException e){
                final Class aClass = getClass();
                final String className = aClass.getName();
                final Logger logger = Logger.getInstance(className);
                logger.error(e);
            }
        }
    }

    private static class UnnecessaryBlockStatementVisitor
            extends BaseInspectionVisitor{
        private UnnecessaryBlockStatementVisitor(BaseInspection inspection,
                                                 InspectionManager inspectionManager,
                                                 boolean isOnTheFly){
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitBlockStatement(PsiBlockStatement blockStatement){
            super.visitBlockStatement(blockStatement);
            final PsiElement parent = blockStatement.getParent();
            if(!(parent instanceof PsiCodeBlock)){
                return;
            }
            final PsiCodeBlock codeBlock = blockStatement.getCodeBlock();
            if(codeBlock == null){
                return;
            }
            final PsiJavaToken brace = codeBlock.getLBrace();
            if(brace == null){
                return;
            }
            final PsiCodeBlock parentBlock = (PsiCodeBlock) parent;
            if(parentBlock.getStatements().length > 1 &&
                       containsConflictingDeclarations(codeBlock, parentBlock)){
                return;
            }
            registerError(brace);
            final PsiJavaToken rbrace = codeBlock.getRBrace();
            if(rbrace != null){
                registerError(rbrace);
            }
        }

        private static boolean containsConflictingDeclarations(PsiCodeBlock block,
                                                               PsiCodeBlock parentBlock){
            final PsiStatement[] statements = block.getStatements();
            if(statements == null){
                return false;
            }
            final Set declaredVars = new HashSet();
            for(int i = 0; i < statements.length; i++){
                final PsiStatement statement = statements[i];
                if(statement instanceof PsiDeclarationStatement){
                    final PsiDeclarationStatement declaration =
                            (PsiDeclarationStatement) statement;
                    final PsiElement[] vars = declaration.getDeclaredElements();
                    for(int j = 0; j < vars.length; j++){
                        if(vars[j] instanceof PsiLocalVariable){
                            declaredVars.add(vars[j]);
                        }
                    }
                }
            }
            for(Iterator iterator = declaredVars.iterator();
                iterator.hasNext();){
                final PsiLocalVariable variable =
                        (PsiLocalVariable) iterator.next();
                final String variableName = variable.getName();
                if(conflictingDeclarationExists(variableName, parentBlock,
                                                block)){
                    return true;
                }
            }

            return false;
        }

        private static boolean conflictingDeclarationExists(String name,
                                                            PsiCodeBlock parentBlock,
                                                            PsiCodeBlock exceptBlock){
            final ConflictingDeclarationVisitor visitor =
                    new ConflictingDeclarationVisitor(name, exceptBlock);
            parentBlock.accept(visitor);
            return visitor.hasConflictingDeclaration();
        }
    }

    private static class ConflictingDeclarationVisitor
            extends PsiRecursiveElementVisitor
    {
        private final String variableName;
        private final PsiCodeBlock exceptBlock;
        private boolean hasConflictingDeclaration = false;

        ConflictingDeclarationVisitor(String variableName,
                                      PsiCodeBlock exceptBlock){
            super();
            this.variableName = variableName;
            this.exceptBlock = exceptBlock;
        }

        public void visitCodeBlock(PsiCodeBlock block){
            if(block.equals(exceptBlock)){
                return;
            }
            super.visitCodeBlock(block);
        }

        public void visitVariable(PsiVariable variable){
            super.visitVariable(variable);
            if(variable.getName().equals(variableName)){
                hasConflictingDeclaration = true;
            }
        }

        public boolean hasConflictingDeclaration(){
            return hasConflictingDeclaration;
        }
    }
}