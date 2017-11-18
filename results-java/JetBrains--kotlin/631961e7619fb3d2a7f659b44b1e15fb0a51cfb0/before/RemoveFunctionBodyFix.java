/*
 * Copyright 2010-2012 JetBrains s.r.o.
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

package org.jetbrains.jet.plugin.quickfix;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.diagnostics.DiagnosticWithPsiElement;
import org.jetbrains.jet.lang.psi.JetElement;
import org.jetbrains.jet.lang.psi.JetExpression;
import org.jetbrains.jet.lang.psi.JetFunction;
import org.jetbrains.jet.lexer.JetTokens;
import org.jetbrains.jet.plugin.JetBundle;

/**
 * @author svtk
 */
public class RemoveFunctionBodyFix extends JetIntentionAction<JetFunction> {

    public RemoveFunctionBodyFix(@NotNull JetFunction element) {
        super(element);
    }

    @NotNull
    @Override
    public String getText() {
        return JetBundle.message("remove.function.body");
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return JetBundle.message("remove.function.body");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return super.isAvailable(project, editor, file) &&
                element.getBodyExpression() != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        JetFunction function = (JetFunction) element.copy();
        JetExpression bodyExpression = function.getBodyExpression();
        assert bodyExpression != null;
        if (function.hasBlockBody()) {
            PsiElement prevElement = bodyExpression.getPrevSibling();
            QuickFixUtil.removePossiblyWhiteSpace(function, prevElement);
            function.deleteChildInternal(bodyExpression.getNode());
        }
        else {
            PsiElement prevElement = bodyExpression.getPrevSibling();
            PsiElement prevPrevElement = prevElement.getPrevSibling();
            QuickFixUtil.removePossiblyWhiteSpace(function, prevElement);
            removePossiblyEquationSign(function, prevElement);
            removePossiblyEquationSign(function, prevPrevElement);
            function.deleteChildInternal(bodyExpression.getNode());
        }
        element.replace(function);
    }

    private static boolean removePossiblyEquationSign(@NotNull JetElement element, @Nullable PsiElement possiblyEq) {
        if (possiblyEq instanceof LeafPsiElement && ((LeafPsiElement)possiblyEq).getElementType() == JetTokens.EQ) {
            QuickFixUtil.removePossiblyWhiteSpace(element, possiblyEq.getNextSibling());
            element.deleteChildInternal(possiblyEq.getNode());
            return true;
        }
        return false;
    }

    public static JetIntentionActionFactory<JetFunction> createFactory() {
        return new JetIntentionActionFactory<JetFunction>() {
            @Override
            public JetIntentionAction<JetFunction> createAction(DiagnosticWithPsiElement diagnostic) {
                assert diagnostic.getPsiElement() instanceof JetFunction;
                return new RemoveFunctionBodyFix((JetFunction) diagnostic.getPsiElement());
            }
        };
    }
}