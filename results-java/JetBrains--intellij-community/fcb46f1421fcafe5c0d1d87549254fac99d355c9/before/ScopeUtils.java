/*
 * Copyright 2003-2005 Dave Griffith
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
package com.siyeh.ig.dataflow;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ScopeUtils
{
    public static final Class[] TYPES =
            new Class[]{PsiCodeBlock.class, PsiForStatement.class};

    private ScopeUtils()
    {
    }

    @Nullable
    public static PsiElement findTighterDeclarationLocation(
            @NotNull PsiElement sibling, @NotNull PsiVariable variable)
    {
        PsiElement prevSibling = sibling.getPrevSibling();
        while (prevSibling instanceof PsiWhiteSpace ||
               prevSibling instanceof PsiComment)
        {
            prevSibling = prevSibling.getPrevSibling();
        }
        if (prevSibling instanceof PsiDeclarationStatement)
        {
            if (prevSibling.equals(variable.getParent()))
            {
                return null;
            }
            return findTighterDeclarationLocation(prevSibling, variable);
        }
        return prevSibling;
    }

    @Nullable
    public static PsiElement getChildWhichContainsElement(
            @NotNull PsiElement ancestor, @NotNull PsiElement descendant)
    {
        PsiElement element = descendant;
        while (!element.equals(ancestor))
        {
            descendant = element;
            element = descendant.getParent();
            if (element == null)
            {
                return null;
            }
        }
        return descendant;
    }

    @Nullable
    public static PsiElement getCommonParent(@NotNull PsiReference[] references)
    {
        PsiElement commonParent = null;
        for (PsiReference reference : references)
        {
            final PsiElement referenceElement = reference.getElement();
            final PsiElement parent = getParentOfTypes(referenceElement, TYPES);
            if (parent != null && commonParent != null &&
                !commonParent.equals(parent))
            {
                commonParent =
                PsiTreeUtil.findCommonParent(commonParent, parent);
                commonParent = getParentOfTypes(commonParent, TYPES);
            }
            else
            {
                commonParent = parent;
            }
        }

        // make common parent may only be for-statement if first reference is
        // the initialization of the for statement or the initialization is
        // empty.
        if (commonParent instanceof PsiForStatement)
        {
            final PsiForStatement forStatement = (PsiForStatement)commonParent;
            final PsiElement referenceElement = references[0].getElement();
            final PsiStatement initialization =
                    forStatement.getInitialization();
            if (!(initialization instanceof PsiEmptyStatement))
            {
                if (initialization instanceof PsiExpressionStatement)
                {
                    final PsiExpressionStatement statement =
                            (PsiExpressionStatement)initialization;
                    final PsiExpression expression = statement.getExpression();
                    if (expression instanceof PsiAssignmentExpression)
                    {
                        final PsiAssignmentExpression assignmentExpression =
                                (PsiAssignmentExpression)expression;
                        final PsiExpression lExpression =
                                assignmentExpression.getLExpression();
                        if (!lExpression.equals(referenceElement))
                        {
                            commonParent = PsiTreeUtil.getParentOfType(
                                    commonParent, PsiCodeBlock.class);
                        }

                    }
                    else
                    {
                        commonParent = PsiTreeUtil.getParentOfType(
                                commonParent, PsiCodeBlock.class);
                    }
                }
                 else
                {
                    commonParent = PsiTreeUtil.getParentOfType(commonParent,
                            PsiCodeBlock.class);
                }
            }
        }

        // common parent may not be a switch() statement to avoid narrowing
        // scope to inside switch branch
        if (commonParent != null)
        {
            final PsiElement parent = commonParent.getParent();
            if (parent instanceof PsiSwitchStatement && references.length > 1)
            {
                commonParent = PsiTreeUtil.getParentOfType(parent,
                        PsiCodeBlock.class, false);
            }
        }
        return commonParent;
    }

    @Nullable
    public static PsiElement getParentOfTypes(@Nullable PsiElement element,
                                              @NotNull Class[] classes)
    {
        if (element == null)
        {
            return null;
        }

        while (element != null)
        {
            for (Class clazz : classes)
            {
                if (clazz.isInstance(element))
                {
                    return element;
                }
            }
            element = element.getParent();
        }
        return null;
    }

    @Nullable
    public static PsiElement moveOutOfLoops(@NotNull PsiElement scope,
                                            @NotNull PsiElement maxScope)
    {
        PsiElement result = maxScope;
        if (result instanceof PsiLoopStatement)
        {
            return result;
        }
        while (!result.equals(scope))
        {
            final PsiElement element =
                    getChildWhichContainsElement(result, scope);
            if (element == null || element instanceof PsiLoopStatement)
            {
                while (result != null && !(result instanceof PsiCodeBlock))
                {
                    result = result.getParent();
                }
                return result;
            }
            else
            {
                result = element;
            }
        }
        return scope;
    }
}