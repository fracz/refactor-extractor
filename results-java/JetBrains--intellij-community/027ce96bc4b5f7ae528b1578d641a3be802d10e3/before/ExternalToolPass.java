package com.intellij.codeInsight.daemon.impl;

import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightUtil;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ven
 */
public class ExternalToolPass extends TextEditorHighlightingPass {
  private final PsiFile myFile;
  private final int myStartOffset;
  private final int myEndOffset;
  private AnnotationHolderImpl myAnnotationHolder;
  HighlightInfoHolder myHolder;
  private Project myProject;

  public ExternalToolPass(PsiFile file,
                          Editor editor,
                          int startOffset,
                          int endOffset) {
    super(editor.getDocument());
    myFile = file;
    myStartOffset = startOffset;
    myEndOffset = endOffset;
    myProject = file.getProject();
    myAnnotationHolder = new AnnotationHolderImpl();

  }

  public void doCollectInformation(ProgressIndicator progress) {
    final PsiFile[] psiRoots = myFile.getPsiRoots();
    for (PsiFile psiRoot : psiRoots) {
      if (!HighlightUtil.isRootInspected(psiRoot)) continue;
      final List<ExternalAnnotator> externalAnnotators = psiRoot.getLanguage().getExternalAnnotators();

      if (externalAnnotators.size() > 0) {
        final HighlightInfo[] errors = DaemonCodeAnalyzerImpl.getHighlights(myDocument, HighlightSeverity.ERROR, myProject, myStartOffset, myEndOffset);

        for (HighlightInfo error : errors) {
          if (error.group != UpdateHighlightersUtil.EXTERNAL_TOOLS_HIGHLIGHTERS_GROUP) {
            return;
          }
        }
        for(ExternalAnnotator externalAnnotator: externalAnnotators) {
          externalAnnotator.annotate(psiRoot, myAnnotationHolder);
        }
      }
    }
  }

  public void doApplyInformationToEditor() {
    if (myAnnotationHolder.hasAnnotations()) {
      List<HighlightInfo> infos = getHighlights();

      UpdateHighlightersUtil.setHighlightersToEditor(myProject, myDocument, myStartOffset, myEndOffset,
                                                     infos, UpdateHighlightersUtil.EXTERNAL_TOOLS_HIGHLIGHTERS_GROUP);
    }
  }

  public List<HighlightInfo> getHighlights() {
    Annotation[] annotations = myAnnotationHolder.getResult();
    List<HighlightInfo> infos = new ArrayList<HighlightInfo>();
    for (Annotation annotation : annotations) {
      infos.add(HighlightUtil.convertToHighlightInfo(annotation));
    }
    return infos;
  }

  public int getPassId() {
    return Pass.EXTERNAL_TOOLS;
  }
}