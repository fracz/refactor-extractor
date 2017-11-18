/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.refactoring.inline;

import com.intellij.lang.refactoring.InlineHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.refactoring.util.RefactoringMessageDialog;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.util.GrVariableDeclarationOwner;
import org.jetbrains.plugins.groovy.lang.psi.impl.PsiImplUtil;
import org.jetbrains.plugins.groovy.refactoring.GroovyRefactoringBundle;
import org.jetbrains.plugins.groovy.refactoring.GroovyRefactoringUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author ilyas
 */
public class GroovyInlineHandler implements InlineHandler {

  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.groovy.refactoring.inline.GroovyInlineHandler");
  private static final String REFACTORING_NAME = GroovyRefactoringBundle.message("inline.variable.title");

  @Nullable
  public Settings prepareInlineElement(final PsiElement element, Editor editor, boolean invokedOnReference) {

    if (element instanceof GrVariable &&
        GroovyRefactoringUtil.isLocalVariable((GrVariable) element)) { // todo add method && class
      return inlineLocalVariableSettings((GrVariable) element, editor);
    } else {
      String message = GroovyRefactoringBundle.message("wrong.element.to.inline");
      CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.INLINE_VARIABLE, element.getProject());
    }

    return null;
  }

  /**
   * Returns Settings object for referenced definition in case of local variable
   */
  private Settings inlineLocalVariableSettings(final GrVariable variable, Editor editor) {
    final String localName = variable.getNameIdentifierGroovy().getText();
    final Project project = variable.getProject();
    final Collection<PsiReference> refs = ReferencesSearch.search(variable, GlobalSearchScope.projectScope(variable.getProject()), false).findAll();
    ArrayList<PsiElement> exprs = new ArrayList<PsiElement>();
    for (PsiReference ref : refs) {
      exprs.add(ref.getElement());
    }

    GroovyRefactoringUtil.highlightOccurrences(project, editor, exprs.toArray(PsiElement.EMPTY_ARRAY));
    if (variable.getInitializerGroovy() == null) {
      String message = GroovyRefactoringBundle.message("cannot.find.a.single.definition.to.inline.local.var");
      CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.INLINE_VARIABLE, variable.getProject());
      return null;
    }
    if (refs.isEmpty()) {
      String message = GroovyRefactoringBundle.message("variable.is.never.used.0", variable.getNameIdentifierGroovy().getText());
      CommonRefactoringUtil.showErrorMessage(REFACTORING_NAME, message, HelpID.INLINE_VARIABLE, variable.getProject());
      return null;
    }

    final String question = GroovyRefactoringBundle.message("inline.local.variable.prompt.0.1", localName, refs.size());
    RefactoringMessageDialog dialog = new RefactoringMessageDialog(
        REFACTORING_NAME,
        question,
        HelpID.INLINE_VARIABLE,
        "OptionPane.questionIcon",
        true,
        project);
    dialog.show();
    if (!dialog.isOK()) {
      WindowManager.getInstance().getStatusBar(project).setInfo(GroovyRefactoringBundle.message("press.escape.to.remove.the.highlighting"));
      return null;
    }

    return new Settings() {
      public boolean isOnlyOneReferenceToInline() {
        return false;
      }
    };
  }

  public void removeDefinition(final PsiElement element) {
    try {
      final PsiElement owner = element.getParent().getParent();
      if (element instanceof GrVariable &&
          owner instanceof GrVariableDeclarationOwner) {
        ((GrVariableDeclarationOwner) owner).removeVariable(((GrVariable) element));
      }
    } catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }

  @Nullable
  public Inliner createInliner(PsiElement element) {
    if (element instanceof GrVariable &&
        GroovyRefactoringUtil.isLocalVariable((GrVariable) element)) {
      return createInlinerForLocalVariable(((GrVariable) element));
    }
    return null;
  }

  /**
   * Creates new inliner for local variable occurences
   */
  private Inliner createInlinerForLocalVariable(final GrVariable variable) {
    return new Inliner() {

      @Nullable
      public Collection<String> getConflicts(PsiReference reference, PsiElement referenced) {
        return null;
      }

      public void inlineReference(final PsiReference reference, final PsiElement referenced) {
        GrExpression exprToBeReplaced = (GrExpression) reference.getElement();
        assert variable.getInitializerGroovy() != null;
        GrExpression initializerGroovy = variable.getInitializerGroovy();
        assert initializerGroovy != null;
        Project project = variable.getProject();
        GroovyElementFactory factory = GroovyElementFactory.getInstance(project);
        GrExpression newExpr = factory.createExpressionFromText(initializerGroovy.getText());

        if (exprToBeReplaced.getParent() instanceof GrExpression) {
          GrExpression parentExpr = (GrExpression) exprToBeReplaced.getParent();
          if (PsiImplUtil.getExprPriorityLevel(parentExpr) >= PsiImplUtil.getExprPriorityLevel(newExpr)) {
            newExpr = factory.createParenthesizedExpr(newExpr);
          }
        }

        try {
          newExpr = exprToBeReplaced.replaceWithExpression(newExpr);
          FileEditorManager manager = FileEditorManager.getInstance(project);
          Editor editor = manager.getSelectedTextEditor();
          GroovyRefactoringUtil.highlightOccurrences(project, editor, new PsiElement[]{newExpr});
          WindowManager.getInstance().getStatusBar(project).setInfo(GroovyRefactoringBundle.message("press.escape.to.remove.the.highlighting"));
        } catch (IncorrectOperationException e) {
          LOG.error(e);
        }
      }
    };
  }

}
