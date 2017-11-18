package com.intellij.codeInsight.highlighting;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.ExceptionUtil;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.LangBundle;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

public class HighlightExceptionsHandler extends HighlightUsagesHandlerBase {
  private PsiElement myTarget;
  private PsiClassType[] myClassTypes;
  private PsiElement myPlace;
  private Condition<PsiType> myTypeFilter;

  public HighlightExceptionsHandler(final Editor editor, final PsiFile file, final PsiElement target, final PsiClassType[] classTypes,
                                    final PsiElement place, final Condition<PsiType> typeFilter) {
    super(editor, file);
    myTarget = target;
    myClassTypes = classTypes;
    myPlace = place;
    myTypeFilter = typeFilter;
  }

  public void highlightUsages() {
    final Project project = myEditor.getProject();
    final boolean clearHighlights = HighlightUsagesHandler.isClearHighlights(myEditor, HighlightManager.getInstance(project));
    if (myClassTypes == null || myClassTypes.length == 0) {
      String text = CodeInsightBundle.message("highlight.exceptions.thrown.notfound");
      HintManager.getInstance().showInformationHint(myEditor, text);
      return;
    }
    new ChooseClassAndDoHighlightRunnable(myClassTypes, myEditor, CodeInsightBundle.message("highlight.exceptions.thrown.chooser.title")) {
      protected void selected(PsiClass... classes) {
        List<PsiReference> refs = new ArrayList<PsiReference>();
        final ArrayList<PsiElement> otherOccurrences = new ArrayList<PsiElement>();
        final PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();

        for (PsiClass aClass : classes) {
          addExceptionThrownPlaces(refs, otherOccurrences, factory.createType(aClass), myPlace, myTypeFilter);
        }

        HighlightUsagesHandler.highlightReferences(project, myTarget, refs, myEditor, myFile, clearHighlights);
        TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);
        HighlightUsagesHandler.doHighlightElements(myEditor, new PsiElement[]{myTarget}, attributes, clearHighlights);
        HighlightUsagesHandler.highlightOtherOccurrences(otherOccurrences, myEditor, clearHighlights);
        HighlightUsagesHandler.setStatusText(project, LangBundle.message("java.terms.exception"), refs.size(), clearHighlights);
      }
    }.run();
  }

  private static void addExceptionThrownPlaces(final List<PsiReference> refs, final List<PsiElement> otherOccurrences, final PsiType type,
                                               final PsiElement block, final Condition<PsiType> typeFilter) {
    if (type instanceof PsiClassType) {
      block.accept(new JavaRecursiveElementVisitor() {
        @Override public void visitReferenceExpression(PsiReferenceExpression expression) {
          visitElement(expression);
        }

        @Override public void visitThrowStatement(PsiThrowStatement statement) {
          super.visitThrowStatement(statement);
          PsiClassType[] exceptionTypes = ExceptionUtil.getUnhandledExceptions(statement, block);
          for (final PsiClassType actualType : exceptionTypes) {
            if (type.isAssignableFrom(actualType) && typeFilter.value(actualType)) {
              PsiExpression psiExpression = statement.getException();
              if (psiExpression instanceof PsiReferenceExpression) {
                PsiReferenceExpression referenceExpression = (PsiReferenceExpression)psiExpression;
                if (!refs.contains(referenceExpression)) refs.add(referenceExpression);
              }
              else if (psiExpression instanceof PsiNewExpression) {
                PsiJavaCodeReferenceElement ref = ((PsiNewExpression)psiExpression).getClassReference();
                if (ref != null && !refs.contains(ref)) refs.add(ref);
              }
              else {
                otherOccurrences.add(statement.getException());
              }
            }
          }
        }

        @Override public void visitMethodCallExpression(PsiMethodCallExpression expression) {
          super.visitMethodCallExpression(expression);
          PsiReference reference = expression.getMethodExpression().getReference();
          if (reference == null || refs.contains(reference)) return;
          PsiClassType[] exceptionTypes = ExceptionUtil.getUnhandledExceptions(expression, block);
          for (final PsiClassType actualType : exceptionTypes) {
            if (type.isAssignableFrom(actualType) && typeFilter.value(actualType)) {
              refs.add(reference);
              break;
            }
          }
        }

        @Override public void visitNewExpression(PsiNewExpression expression) {
          super.visitNewExpression(expression);
          PsiJavaCodeReferenceElement classReference = expression.getClassOrAnonymousClassReference();
          if (classReference == null || refs.contains(classReference)) return;
          PsiClassType[] exceptionTypes = ExceptionUtil.getUnhandledExceptions(expression, block);
          for (PsiClassType actualType : exceptionTypes) {
            if (type.isAssignableFrom(actualType) && typeFilter.value(actualType)) {
              refs.add(classReference);
              break;
            }
          }
        }
      });
    }
  }
}