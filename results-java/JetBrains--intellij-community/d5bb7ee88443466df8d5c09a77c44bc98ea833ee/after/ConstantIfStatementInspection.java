/*
 * Copyright 2003-2007 Dave Griffith, Bas Leijdekkers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.controlflow;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.BoolUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ConstantIfStatementInspection extends BaseInspection {

    @NotNull
    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "constant.if.statement.display.name");
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "constant.if.statement.problem.descriptor");
    }

    public BaseInspectionVisitor buildVisitor() {
        return new ConstantIfStatementVisitor();
    }

    public InspectionGadgetsFix buildFix(Object... infos) {
        //if (PsiUtil.isInJspFile(location)) {
        //    return null;
        //}
        return new ConstantIfStatementFix();
    }

    private static class ConstantIfStatementFix extends InspectionGadgetsFix {

        @NotNull
        public String getName() {
            return InspectionGadgetsBundle.message(
                    "constant.conditional.expression.simplify.quickfix");
        }

        public void doFix(Project project, ProblemDescriptor descriptor)
                throws IncorrectOperationException {
            final PsiElement ifKeyword = descriptor.getPsiElement();
            final PsiIfStatement statement =
                    (PsiIfStatement)ifKeyword.getParent();
            assert statement != null;
            final PsiStatement thenBranch = statement.getThenBranch();
            final PsiStatement elseBranch = statement.getElseBranch();
            final PsiExpression condition = statement.getCondition();
            if (BoolUtils.isFalse(condition)) {
                if (elseBranch != null) {
                    replaceStatementWithUnwrapping(elseBranch, statement);
                } else {
                    deleteElement(statement);
                }
            } else {
                replaceStatementWithUnwrapping(thenBranch, statement);
            }
        }

        private static void replaceStatementWithUnwrapping(
                PsiStatement branch, PsiIfStatement statement)
                throws IncorrectOperationException {
            if (branch instanceof PsiBlockStatement &&
                    !(statement.getParent() instanceof PsiIfStatement)) {
                final PsiCodeBlock parentBlock =
                        PsiTreeUtil.getParentOfType(branch, PsiCodeBlock.class);
                if (parentBlock == null) {
                    final String elseText = branch.getText();
                    replaceStatement(statement, elseText);
                    return;
                }
                final PsiCodeBlock block =
                        ((PsiBlockStatement)branch).getCodeBlock();
                final boolean hasConflicts =
                        containsConflictingDeclarations(block, parentBlock);
                if (hasConflicts) {
                    final String elseText = branch.getText();
                    replaceStatement(statement, elseText);
                } else {
                    final PsiElement containingElement = statement.getParent();
                    final PsiStatement[] statements = block.getStatements();
                    if (statements.length > 0) {
                        assert containingElement != null;
                        final PsiElement added = containingElement.addRangeBefore(statements[0], statements[statements.length - 1], statement);
                        final Project project = statement.getProject();
                        final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
                        codeStyleManager.reformat(added);
                    }
                    statement.delete();
                }
            } else {
                final String elseText = branch.getText();
                replaceStatement(statement, elseText);
            }
        }

        private static boolean containsConflictingDeclarations(
                PsiCodeBlock block, PsiCodeBlock parentBlock) {
            final PsiStatement[] statements = block.getStatements();
            final Set<PsiElement> declaredVars = new HashSet<PsiElement>();
            for (final PsiStatement statement : statements) {
                if (statement instanceof PsiDeclarationStatement) {
                    final PsiDeclarationStatement declaration =
                            (PsiDeclarationStatement)statement;
                    final PsiElement[] vars = declaration.getDeclaredElements();
                    for (PsiElement var : vars) {
                        if (var instanceof PsiLocalVariable) {
                            declaredVars.add(var);
                        }
                    }
                }
            }
            for (Object declaredVar : declaredVars) {
                final PsiLocalVariable variable =
                        (PsiLocalVariable)declaredVar;
                final String variableName = variable.getName();
                if (conflictingDeclarationExists(variableName, parentBlock,
                        block)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean conflictingDeclarationExists(
                String name, PsiCodeBlock parentBlock,
                PsiCodeBlock exceptBlock) {
            final ConflictingDeclarationVisitor visitor =
                    new ConflictingDeclarationVisitor(name, exceptBlock);
            parentBlock.accept(visitor);
            return visitor.hasConflictingDeclaration();
        }
    }

    private static class ConstantIfStatementVisitor
            extends BaseInspectionVisitor {

        @Override public void visitIfStatement(PsiIfStatement statement) {
            super.visitIfStatement(statement);
            final PsiExpression condition = statement.getCondition();
            if (condition == null) {
                return;
            }
            final PsiStatement thenBranch = statement.getThenBranch();
            if (thenBranch == null) {
                return;
            }
            if (BoolUtils.isTrue(condition) || BoolUtils.isFalse(condition)) {
                registerStatementError(statement);
            }
        }
    }

    private static class ConflictingDeclarationVisitor
            extends JavaRecursiveElementVisitor {

        private final String variableName;
        private final PsiCodeBlock exceptBlock;
        private boolean hasConflictingDeclaration = false;

        ConflictingDeclarationVisitor(String variableName,
                                      PsiCodeBlock exceptBlock) {
            super();
            this.variableName = variableName;
            this.exceptBlock = exceptBlock;
        }

        @Override public void visitElement(@NotNull PsiElement element) {
            if (!hasConflictingDeclaration) {
                super.visitElement(element);
            }
        }

        @Override public void visitCodeBlock(PsiCodeBlock block) {
            if (hasConflictingDeclaration) {
                return;
            }
            if (block.equals(exceptBlock)) {
                return;
            }
            super.visitCodeBlock(block);
        }

        @Override public void visitVariable(PsiVariable variable) {
            if (hasConflictingDeclaration) {
                return;
            }
            super.visitVariable(variable);
            final String name = variable.getName();
            if (name != null && name.equals(variableName)) {
                hasConflictingDeclaration = true;
            }
        }

        public boolean hasConflictingDeclaration() {
            return hasConflictingDeclaration;
        }
    }
}