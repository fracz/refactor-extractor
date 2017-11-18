package com.intellij.codeInsight.navigation.actions;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.util.EditSourceUtil;
import com.intellij.ide.util.gotoByName.GotoSymbolCellRenderer;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;

public class GotoDeclarationAction extends BaseCodeInsightAction implements CodeInsightActionHandler {
  protected CodeInsightActionHandler getHandler() {
    return this;
  }

  protected boolean isValidForFile(Project project, Editor editor, final PsiFile file) {
    return true;
  }

  protected boolean isValidForLookup() {
    return true;
  }

  public void invoke(final Project project, Editor editor, PsiFile file) {
    PsiDocumentManager.getInstance(project).commitAllDocuments();

    int offset = editor.getCaretModel().getOffset();
    PsiElement element = findTargetElement(project, editor, offset);
    if (element == null) {
      FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.goto.declaration");
      chooseAmbiguousTarget(project, editor, offset);
      return;
    }

    FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.goto.declaration");
    PsiElement navElement = element.getNavigationElement();

    //TODO: move this logic to ClsMethodImpl.getNavigationElement
    if (navElement == element && element instanceof PsiCompiledElement && element instanceof PsiMethod) {
      PsiMethod method = (PsiMethod)element;
      if (method.isConstructor() && method.getParameterList().getParametersCount() == 0) {
        PsiClass aClass = method.getContainingClass();
        PsiElement navClass = aClass.getNavigationElement();
        if (aClass != navClass) navElement = navClass;
      }
    }

    if (navElement instanceof Navigatable) {
      if (((Navigatable)navElement).canNavigate()) {
        ((Navigatable)navElement).navigate(true);
      }
    }
    else if (navElement != null) {
      int navOffset = navElement.getTextOffset();
      VirtualFile virtualFile = PsiUtilBase.getVirtualFile(navElement);
      if (virtualFile != null) {
        new OpenFileDescriptor(project, virtualFile, navOffset).navigate(true);
      }
    }
  }

  private static void chooseAmbiguousTarget(final Project project, final Editor editor, int offset) {
    PsiElementProcessor<PsiElement> naviagteProcessor = new PsiElementProcessor<PsiElement>() {
      public boolean execute(final PsiElement element) {
        Navigatable navigatable = EditSourceUtil.getDescriptor(element);
        if (navigatable != null && navigatable.canNavigate()) {
          navigatable.navigate(true);
        }
        return true;
      }
    };
    chooseAmbiguousTarget(project, editor, offset,naviagteProcessor, CodeInsightBundle.message("declaration.navigation.title"));
  }

  // returns true if processor is run or is going to be run after showing popup
  public static boolean chooseAmbiguousTarget(final Project project, final Editor editor, int offset, PsiElementProcessor<PsiElement> processor,
                                              String titlePattern) {
    final PsiReference reference = TargetElementUtilBase.findReference(editor, offset);
    final Collection<PsiElement> candidates = suggestCandidates(project, reference);
    if (candidates.size() == 1) {
      PsiElement element = candidates.iterator().next();
      processor.execute(element);
      return true;
    }
    else if (candidates.size() > 1) {
      PsiElement[] elements = candidates.toArray(new PsiElement[candidates.size()]);
      final TextRange range = reference.getRangeInElement();
      final String refText = reference.getElement().getText().substring(range.getStartOffset(), range.getEndOffset());
      String title = MessageFormat.format(titlePattern, refText);
      NavigationUtil.getPsiElementPopup(elements, new GotoSymbolCellRenderer(), title, processor).showInBestPositionFor(editor);
      return true;
    }
    return false;
  }

  static Collection<PsiElement> suggestCandidates(Project project, final PsiReference reference) {
    if (reference == null) {
      return Collections.emptyList();
    }
    return resolveElements(reference, project);
  }

  @NotNull private static Collection<PsiElement> resolveElements(final PsiReference reference, final Project project) {
    PsiElement parent = reference.getElement().getParent();
    if (parent instanceof PsiMethodCallExpression) {
      PsiMethodCallExpression callExpr = (PsiMethodCallExpression) parent;
      boolean allowStatics = false;
      PsiExpression qualifier = callExpr.getMethodExpression().getQualifierExpression();
      if (qualifier == null) {
        allowStatics = true;
      } else if (qualifier instanceof PsiJavaCodeReferenceElement) {
        PsiElement referee = ((PsiJavaCodeReferenceElement) qualifier).advancedResolve(true).getElement();
        if (referee instanceof PsiClass) allowStatics = true;
      }
      PsiResolveHelper helper = JavaPsiFacade.getInstance(project).getResolveHelper();
      PsiElement[] candidates = PsiUtil.mapElements(helper.getReferencedMethodCandidates(callExpr, false));
      ArrayList<PsiElement> methods = new ArrayList<PsiElement>();
      for (PsiElement candidate1 : candidates) {
        PsiMethod candidate = (PsiMethod)candidate1;
        if (candidate.hasModifierProperty(PsiModifier.STATIC) && !allowStatics) continue;
        List<PsiMethod> supers = Arrays.asList(candidate.findSuperMethods());
        if (supers.isEmpty()) {
          methods.add(candidate);
        }
        else {
          methods.addAll(supers);
        }
      }
      return methods;
    }

    if (reference instanceof PsiPolyVariantReference) {
      final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
      final ArrayList<PsiElement> navigatableResults = new ArrayList<PsiElement>(results.length);

      for(ResolveResult r:results) {
        PsiElement element = r.getElement();
        if (EditSourceUtil.canNavigate(element) || element instanceof Navigatable && ((Navigatable)element).canNavigateToSource()) {
          navigatableResults.add(element);
        }
      }

      return navigatableResults;
    }
    PsiElement resolved = reference.resolve();
    if (resolved instanceof NavigationItem) {
      return Collections.singleton(resolved);
    }
    return Collections.emptyList();
  }

  public boolean startInWriteAction() {
    return false;
  }

  @Nullable
  public static PsiElement findTargetElement(Project project, Editor editor, int offset) {
    int flags = TargetElementUtilBase.REFERENCED_ELEMENT_ACCEPTED
                | TargetElementUtil.NEW_AS_CONSTRUCTOR
                | TargetElementUtilBase.LOOKUP_ITEM_ACCEPTED
                | TargetElementUtil.THIS_ACCEPTED
                | TargetElementUtil.SUPER_ACCEPTED;
    PsiElement element = TargetElementUtilBase.getInstance().findTargetElement(editor, flags, offset);

    if (element != null) return element;

    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    if (file == null) {
      return null;
    }
    PsiElement elementAt = file.findElementAt(offset);

    for(GotoDeclarationHandler handler: Extensions.getExtensions(GotoDeclarationHandler.EP_NAME)) {
      PsiElement result = handler.getGotoDeclarationTarget(elementAt);
      if (result != null) {
        return result;
      }
    }

    return null;
  }
}