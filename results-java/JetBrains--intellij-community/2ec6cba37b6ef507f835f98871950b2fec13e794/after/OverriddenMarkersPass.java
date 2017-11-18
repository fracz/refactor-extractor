
package com.intellij.codeInsight.daemon.impl;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.TypeConversionUtil;
import gnu.trove.THashMap;

import javax.swing.*;
import java.util.*;

public class OverriddenMarkersPass extends TextEditorHighlightingPass {
  private static final Icon OVERRIDEN_METHOD_MARKER_RENDERER = IconLoader.getIcon("/gutter/overridenMethod.png");
  private static final Icon IMPLEMENTED_METHOD_MARKER_RENDERER = IconLoader.getIcon("/gutter/implementedMethod.png");
  private static final Icon IMPLEMENTED_INTERFACE_MARKER_RENDERER = IconLoader.getIcon("/gutter/implementedMethod.png");
  private static final Icon SUBCLASSED_CLASS_MARKER_RENDERER = IconLoader.getIcon("/gutter/overridenMethod.png");

  private final Project myProject;
  private final PsiFile myFile;
  private final Document myDocument;
  private final int myStartOffset;
  private final int myEndOffset;

  private Collection<LineMarkerInfo> myMarkers = Collections.emptyList();

  private Map<PsiClass,PsiClass> myClassToFirstDerivedMap = new THashMap<PsiClass, PsiClass>();

  public OverriddenMarkersPass(
    Project project,
    PsiFile file,
    Document document,
    int startOffset,
    int endOffset
    ) {
    super(document);
    myProject = project;
    myFile = file;
    myDocument = document;
    myStartOffset = startOffset;
    myEndOffset = endOffset;
  }

  public void doCollectInformation(ProgressIndicator progress) {
    PsiElement[] psiRoots = myFile.getPsiRoots();
    for (final PsiElement psiRoot : psiRoots) {
      if (!HighlightUtil.isRootHighlighted(psiRoot)) continue;
      List<PsiElement> elements = CodeInsightUtil.getElementsInRange(psiRoot, myStartOffset, myEndOffset);
      myMarkers = collectLineMarkers(elements);
    }
  }

  public void doApplyInformationToEditor() {
    UpdateHighlightersUtil.setLineMarkersToEditor(
      myProject, myDocument, myStartOffset, myEndOffset,
      myMarkers, UpdateHighlightersUtil.OVERRIDEN_MARKERS_GROUP);

    DaemonCodeAnalyzerImpl daemonCodeAnalyzer = (DaemonCodeAnalyzerImpl)DaemonCodeAnalyzer.getInstance(myProject);
    daemonCodeAnalyzer.getFileStatusMap().markFileUpToDate(myDocument, FileStatusMap.OVERRIDEN_MARKERS);
  }

  public int getPassId() {
    return Pass.UPDATE_OVERRIDEN_MARKERS;
  }

  private Collection<LineMarkerInfo> collectLineMarkers(List<PsiElement> elements) throws ProcessCanceledException {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    List<LineMarkerInfo> array = new ArrayList<LineMarkerInfo>();
    for (PsiElement element : elements) {
      ProgressManager.getInstance().checkCanceled();
      addLineMarkerInfo(element, array);
    }
    return array;
  }

  private void addLineMarkerInfo(PsiElement element, List<LineMarkerInfo> result) {
    if (element instanceof PsiIdentifier) {
      PsiManager manager = PsiManager.getInstance(myProject);
      if (element.getParent() instanceof PsiMethod) {
        collectOverridingMethods(element, manager, result);
      }
      else if (element.getParent() instanceof PsiClass && !(element.getParent() instanceof PsiTypeParameter)) {
        collectInheritingClasses(element, result);
      }
    }
  }

  private void collectInheritingClasses(PsiElement element, List<LineMarkerInfo> result) {
    PsiClass aClass = (PsiClass) element.getParent();
    if (element.equals(aClass.getNameIdentifier())) {
      if (!aClass.hasModifierProperty(PsiModifier.FINAL)) {
        if ("java.lang.Object".equals(aClass.getQualifiedName())) return; // It's useless to have overriden markers for object.

        final PsiClass inheritor = ClassInheritorsSearch.search(aClass, false).findFirst();
        if (inheritor != null) {
          if (!myClassToFirstDerivedMap.containsKey(aClass)){
            myClassToFirstDerivedMap.put(aClass, inheritor);
          }
          int offset = element.getTextRange().getStartOffset();
          LineMarkerInfo info = new LineMarkerInfo(LineMarkerInfo.SUBCLASSED_CLASS, aClass, offset, aClass.isInterface() ? IMPLEMENTED_INTERFACE_MARKER_RENDERER : SUBCLASSED_CLASS_MARKER_RENDERER);

          result.add(info);
        }
      }
    }
  }

  private void collectOverridingMethods(PsiElement element, PsiManager manager, List<LineMarkerInfo> result) {
    PsiMethod method = (PsiMethod)element.getParent();
    if (method.getNameIdentifier().equals(element)){
      if (!PsiUtil.canBeOverriden(method)) return;

      PsiClass parentClass = method.getContainingClass();
      if ("java.lang.Object".equals(parentClass.getQualifiedName())) return; // It's useless to have overriden markers for object.

      if (!myClassToFirstDerivedMap.containsKey(parentClass)){
        final PsiClass derived = ClassInheritorsSearch.search(parentClass, false).findFirst();
        myClassToFirstDerivedMap.put(parentClass, derived);
      }

      PsiClass derived = myClassToFirstDerivedMap.get(parentClass);
      if (derived == null) return;

      PsiSubstitutor substitutor = TypeConversionUtil.getSuperClassSubstitutor(parentClass, derived, PsiSubstitutor.EMPTY);
      if (substitutor == null) substitutor = PsiSubstitutor.EMPTY;
      MethodSignature signature = method.getSignature(substitutor);
      PsiMethod method1 = MethodSignatureUtil.findMethodBySuperSignature(derived, signature, false);
      if (method1 != null) {
        if (method1.hasModifierProperty(PsiModifier.STATIC) ||
            (method.hasModifierProperty(PsiModifier.PACKAGE_LOCAL) && !manager.arePackagesTheSame(parentClass, derived))) {
          method1 = null;
        }
      }
      boolean found = method1 != null;

      if (!found){
        found = OverridingMethodsSearch.search(method, true).findFirst() != null;
      }

      if (found) {
        boolean overrides;
        overrides = !method.hasModifierProperty(PsiModifier.ABSTRACT);

        int offset = method.getNameIdentifier().getTextRange().getStartOffset();
        LineMarkerInfo info = new LineMarkerInfo(LineMarkerInfo.OVERRIDEN_METHOD, method, offset, overrides ? OVERRIDEN_METHOD_MARKER_RENDERER : IMPLEMENTED_METHOD_MARKER_RENDERER);

        result.add(info);
      }
    }
  }
}