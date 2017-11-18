package com.intellij.debugger.impl;

import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.ContextUtil;
import com.intellij.debugger.engine.StackFrameContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.CharArrayUtil;

/**
 * User: lex
 * Date: Oct 29, 2003
 * Time: 9:29:36 PM
 */
public class PositionUtil extends ContextUtil {
  public static SourcePosition getSourcePosition(final StackFrameContext context) {
    if(context instanceof DebuggerContextImpl) return ((DebuggerContextImpl)context).getSourcePosition();

    return ContextUtil.getSourcePosition(context);
  }

  public static PsiElement getContextElement(final StackFrameContext context) {
    if(context instanceof DebuggerContextImpl) return ((DebuggerContextImpl) context).getContextElement();

    return ContextUtil.getContextElement(context);
  }

  public static <T extends PsiElement> T getPsiElementAt(final Project project, final Class<T> expectedPsiElementClass, final SourcePosition sourcePosition) {
    return ApplicationManager.getApplication().runReadAction(new Computable<T>() {
      public T compute() {
        PsiFile psiFile = sourcePosition.getFile();
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);

        if(document == null) {
          return null;
        }

        final int spOffset = sourcePosition.getOffset();
        if (spOffset < 0) {
          return null;
        }
        final int offset = CharArrayUtil.shiftForward(document.getCharsSequence(), spOffset, " \t");
        return PsiTreeUtil.getParentOfType(psiFile.findElementAt(offset), expectedPsiElementClass, false);
      }
    });
  }
}