package com.siyeh.ipp.switchtoif;

import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ipp.base.Intention;
import com.siyeh.ipp.base.PsiElementPredicate;
import com.siyeh.ipp.psiutils.ControlFlowUtils;
import com.siyeh.ipp.psiutils.SideEffectChecker;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReplaceSwitchWithIfIntention extends Intention{
    public String getText(){
        return "Replace switch with if";
    }

    public String getFamilyName(){
        return "Replace Switch With If";
    }

    @NotNull
    public PsiElementPredicate getElementPredicate(){
        return new SwitchPredicate();
    }

    public void processIntention(PsiElement element)
            throws IncorrectOperationException{
        final PsiJavaToken switchToken =
                (PsiJavaToken) element;
        final PsiSwitchStatement switchStatement =
                (PsiSwitchStatement) switchToken.getParent();
        assert switchStatement != null;

        final PsiManager mgr = switchStatement.getManager();
        final PsiExpression switchExpression = switchStatement.getExpression();
        final CodeStyleManager codeStyleMgr = mgr.getCodeStyleManager();
        final String declarationString;
        final boolean hadSideEffects;
        final String expressionText;
        if(SideEffectChecker.mayHaveSideEffects(switchExpression)){
            hadSideEffects = true;
            final PsiType switchExpressionType = switchExpression.getType();
            final String variableName =
                    codeStyleMgr.suggestUniqueVariableName("i",
                                                           switchExpression,
                                                           true);
            expressionText = variableName;
            declarationString =
                    switchExpressionType.getPresentableText() + ' ' +
                            variableName +
                            " = " +
                            switchExpression.getText() +
                            ';';
        } else{
            hadSideEffects = false;
            declarationString = null;
            expressionText = switchExpression.getText();
        }
        final PsiCodeBlock body = switchStatement.getBody();
        final PsiStatement[] statements = body.getStatements();

        boolean renameBreaks = false;
        for(int i = 1; i < statements.length - 1; i++){
            if(CaseUtil.containsHiddenBreak(statements[i])){
                renameBreaks = true;
                break;
            }
        }
        final StringBuffer ifStatementBuffer=new StringBuffer(1024);

        String breakLabel = null;
        if(renameBreaks){
            breakLabel = CaseUtil.findUniqueLabel(switchStatement, "Label");
            ifStatementBuffer.append(breakLabel + ':');
        }
        final List<SwitchStatementBranch> openBranches=new ArrayList<SwitchStatementBranch>(10);
        final Set<PsiLocalVariable> declaredVars=new HashSet<PsiLocalVariable>(5);
        final List<SwitchStatementBranch> allBranches=new ArrayList<SwitchStatementBranch>(10);
        SwitchStatementBranch currentBranch=null;
        final PsiElement[] children = body.getChildren();
        for(int i = 1; i < children.length - 1; i++){
            final PsiElement statement = children[i];
            if(statement instanceof PsiSwitchLabelStatement){
                final PsiSwitchLabelStatement label =
                        (PsiSwitchLabelStatement) statement;
                if(currentBranch == null){
                    openBranches.clear();
                    currentBranch = new SwitchStatementBranch();
                    currentBranch.addPendingVariableDeclarations(declaredVars);
                    allBranches.add(currentBranch);
                    openBranches.add(currentBranch);
                } else if(currentBranch.hasStatements()){
                    currentBranch = new SwitchStatementBranch();
                    allBranches.add(currentBranch);
                    openBranches.add(currentBranch);
                }
                if(label.isDefaultCase()){
                    currentBranch.setDefault();
                } else{
                    PsiExpression value = label.getCaseValue();
                    while(value instanceof PsiParenthesizedExpression){
                        value = ((PsiParenthesizedExpression) value).getExpression();
                    }
                    final String valueText = value.getText();
                    currentBranch.addLabel(valueText);
                }
            } else{
                if(statement instanceof PsiStatement){
                    if(statement instanceof PsiDeclarationStatement){
                        final PsiDeclarationStatement decl =
                                (PsiDeclarationStatement) statement;
                        final PsiElement[] elements =
                                decl.getDeclaredElements();
                        for(PsiElement varElement : elements){
                            final PsiLocalVariable var =
                                    (PsiLocalVariable) varElement;
                            declaredVars.add(var);
                        }
                    }
                    for(SwitchStatementBranch branch : openBranches){
                        branch.addStatement(statement);
                    }
                    if(!ControlFlowUtils.statementMayCompleteNormally((PsiStatement) statement)){
                        currentBranch = null;
                    }
                } else{
                    for(Object openBranche : openBranches){
                        final SwitchStatementBranch branch =
                                (SwitchStatementBranch) openBranche;
                        if(statement instanceof PsiWhiteSpace){
                            branch.addWhiteSpace(statement);
                        } else{
                            branch.addComment(statement);
                        }
                    }
                }
            }
        }
        boolean firstBranch = true;
        SwitchStatementBranch defaultBranch = null;

        for(SwitchStatementBranch branch : allBranches){
            if(branch.isDefault()){
                defaultBranch = branch;
            } else{
                final List<String> labels = branch.getLabels();
                final List<PsiElement> bodyElements = branch.getBodyElements();
                final Set<PsiLocalVariable> pendingVariableDeclarations =
                        branch.getPendingVariableDeclarations();
                dumpBranch(ifStatementBuffer, expressionText,
                           labels, bodyElements, firstBranch,
                           renameBreaks, breakLabel,
                           pendingVariableDeclarations);
                firstBranch = false;
            }
        }
        if(defaultBranch != null){
            final List<PsiElement> bodyElements = defaultBranch.getBodyElements();
            final Set<PsiLocalVariable> pendingVariableDeclarations =
                    defaultBranch.getPendingVariableDeclarations();
            dumpDefaultBranch(ifStatementBuffer, bodyElements,
                              firstBranch, renameBreaks, breakLabel,
                              pendingVariableDeclarations);
        }
        final PsiElementFactory factory = mgr.getElementFactory();
        if(hadSideEffects){
            final PsiStatement declarationStatement =
                    factory.createStatementFromText(declarationString, null);
            final String ifStatementString = ifStatementBuffer.toString();
            final PsiStatement ifStatement =
                    factory.createStatementFromText(ifStatementString, null);
            PsiElement ifElement = switchStatement.replace(ifStatement);
            ifElement = codeStyleMgr.reformat(ifElement);
            final PsiElement parent = ifElement.getParent();
            assert parent != null;
            final PsiElement declarationElement =
                    parent.addBefore(declarationStatement, ifElement);
            codeStyleMgr.reformat(declarationElement);
            codeStyleMgr.reformat(parent);
        } else{
            final String ifStatementString = ifStatementBuffer.toString();
            final PsiStatement newStatement =
                    factory.createStatementFromText(ifStatementString, null);
            final PsiElement replacedStatement =
                    switchStatement.replace(newStatement);
            codeStyleMgr.reformat(replacedStatement);
        }
    }

    private static void dumpBranch(StringBuffer ifStatementString,
                                   String expressionText, List<String> labels,
                                   List<PsiElement> bodyStatements,
                                   boolean firstBranch,
                                   boolean renameBreaks,
                                   String breakLabel,
                                   Set<PsiLocalVariable> variableDecls){
        if(!firstBranch){
            ifStatementString.append("else ");
        }
        dumpLabels(ifStatementString, expressionText, labels);
        dumpBody(ifStatementString, bodyStatements, renameBreaks, breakLabel,
                 variableDecls);
    }

    private static void dumpDefaultBranch(StringBuffer ifStatementString,
                                          List<PsiElement> bodyStatements,
                                          boolean firstBranch,
                                          boolean renameBreaks,
                                          String breakLabel,
                                          Set<PsiLocalVariable> variableDecls){
        if(!firstBranch){
            ifStatementString.append("else ");
        }
        dumpBody(ifStatementString, bodyStatements, renameBreaks, breakLabel,
                 variableDecls);
    }

    private static void dumpLabels(StringBuffer ifStatementString,
                                   String expressionText, List<String> labels){
        ifStatementString.append("if(");
        boolean firstLabel = true;
        for(Object label : labels){
            if(!firstLabel){
                ifStatementString.append("||");
            }
            firstLabel = false;
            final String valueText = (String) label;
            ifStatementString.append(expressionText);
            ifStatementString.append("==");
            ifStatementString.append(valueText);
        }
        ifStatementString.append(')');
    }

    private static void dumpBody(StringBuffer ifStatementString,
                                 List<PsiElement> bodyStatements,
                                 boolean renameBreaks,
                                 String breakLabel,
                                 Set<PsiLocalVariable> variableDecls){

        ifStatementString.append('{');
        for(Object variableDecl : variableDecls){
            final PsiLocalVariable var = (PsiLocalVariable) variableDecl;
            if(CaseUtil.isUsedByStatementList(var, bodyStatements)){
                final PsiType varType = var.getType();
                ifStatementString.append(varType.getPresentableText() + ' ' +
                        var.getName() + ';');
            }
        }

        for(Object bodyStatement1 : bodyStatements){
            final PsiElement bodyStatement = (PsiElement) bodyStatement1;
            final String text = bodyStatement.getText();
            if(!"break;".equals(text)){
                appendElement(ifStatementString, bodyStatement, renameBreaks,
                              breakLabel);
            }
        }
        ifStatementString.append('}');
    }

    private static void appendElement(StringBuffer ifStatementString,
                                      PsiElement element,
                                      boolean renameBreakElements,
                                      String breakLabelString){
        if(!renameBreakElements){
            final String text = element.getText();
            ifStatementString.append(text);
        } else if(element instanceof PsiBreakStatement){
            final PsiIdentifier identifier =
                    ((PsiBreakStatement) element).getLabelIdentifier();
            if(identifier == null || "".equals(identifier.getText())){
                ifStatementString.append("break " + breakLabelString + ';');
            } else{
                final String text = element.getText();
                ifStatementString.append(text);
            }
        } else if(element instanceof PsiBlockStatement ||
                element instanceof PsiCodeBlock ||
                element instanceof PsiIfStatement){
            final PsiElement[] children = element.getChildren();
            for(final PsiElement child : children){
                appendElement(ifStatementString, child, renameBreakElements,
                              breakLabelString);
            }
        } else{
            final String text = element.getText();
            ifStatementString.append(text);
        }
    }
}