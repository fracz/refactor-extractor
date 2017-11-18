package com.intellij.codeInsight.daemon.impl.actions;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.*;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.jsp.jspJava.JspHolderMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ven
 */
public class AddSuppressInspectionFix extends SuppressIntentionAction {
  private String myID;
  private String myKey;

  public AddSuppressInspectionFix(HighlightDisplayKey key) {
    this(key.getID());
  }

  public AddSuppressInspectionFix(String ID) {
    myID = ID;
  }

  @NotNull
  public String getText() {
    return myKey != null ? InspectionsBundle.message(myKey) : "Suppress for member";
  }

  @Nullable
  protected PsiDocCommentOwner getContainer(final PsiElement context) {
    if (context == null || !(context.getContainingFile().getLanguage() instanceof JavaLanguage) || context instanceof PsiFile) {
      return null;
    }
    PsiElement container = context;
    while (!(container instanceof PsiDocCommentOwner) || container instanceof PsiTypeParameter) {
      container = PsiTreeUtil.getParentOfType(container, PsiDocCommentOwner.class);
      if (container == null) return null;
    }
    return (PsiDocCommentOwner)container;
  }

  @NotNull
  public String getFamilyName() {
    return InspectionsBundle.message("suppress.inspection.family");
  }

  @SuppressWarnings({"SimplifiableIfStatement"})
  public boolean isAvailable(@NotNull final Project project, final Editor editor, @Nullable final PsiElement context) {
    final PsiDocCommentOwner container = getContainer(context);
    myKey = container instanceof PsiClass
            ? "suppress.inspection.class"
            : container instanceof PsiMethod ? "suppress.inspection.method" : "suppress.inspection.field";
    final boolean isValid = container != null && !(container instanceof JspHolderMethod);
    if (!isValid) return false;
    return context != null && context.getManager().isInProject(context);
  }

  public void invoke(final Project project, final Editor editor, final PsiElement element) throws IncorrectOperationException {
    PsiDocCommentOwner container = getContainer(element);
    assert container != null;
    final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(project)
        .ensureFilesWritable(container.getContainingFile().getVirtualFile());
    if (status.hasReadonlyFiles()) return;
    if (use15Suppressions(container)) {
      final PsiModifierList modifierList = container.getModifierList();
      if (modifierList != null) {
        addSuppressAnnotation(project, editor, container, modifierList, myID);
      }
    }
    else {
      PsiDocComment docComment = container.getDocComment();
      PsiManager manager = PsiManager.getInstance(project);
      if (docComment == null) {
        String commentText = "/** @" + SuppressionUtil.SUPPRESS_INSPECTIONS_TAG_NAME + " " + myID + "*/";
        docComment = JavaPsiFacade.getInstance(manager.getProject()).getElementFactory().createDocCommentFromText(commentText, null);
        PsiElement firstChild = container.getFirstChild();
        container.addBefore(docComment, firstChild);
      }
      else {
        PsiDocTag noInspectionTag = docComment.findTagByName(SuppressionUtil.SUPPRESS_INSPECTIONS_TAG_NAME);
        if (noInspectionTag != null) {
          final PsiDocTagValue valueElement = noInspectionTag.getValueElement();
          String tagText = "@" + SuppressionUtil
              .SUPPRESS_INSPECTIONS_TAG_NAME + " " + (valueElement != null ? valueElement.getText() + "," : "") + myID;
          noInspectionTag.replace(JavaPsiFacade.getInstance(manager.getProject()).getElementFactory().createDocTagFromText(tagText, null));
        }
        else {
          String tagText = "@" + SuppressionUtil.SUPPRESS_INSPECTIONS_TAG_NAME + " " + myID;
          docComment.add(JavaPsiFacade.getInstance(manager.getProject()).getElementFactory().createDocTagFromText(tagText, null));
        }
      }
    }
    DaemonCodeAnalyzer.getInstance(project).restart();
  }

  public static void addSuppressAnnotation(final Project project,
                                           final Editor editor,
                                           final PsiElement container,
                                           final PsiModifierList modifierList,
                                           final String id) throws IncorrectOperationException {
    PsiAnnotation annotation = modifierList.findAnnotation(SuppressManagerImpl.SUPPRESS_INSPECTIONS_ANNOTATION_NAME);
    if (annotation != null) {
      if (annotation.getText().indexOf("{") == -1) {
        final PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
        if (attributes.length == 1) {
          final String suppressedWarnings = attributes[0].getText();
          PsiAnnotation newAnnotation =
              JavaPsiFacade.getInstance(project).getElementFactory().createAnnotationFromText("@" + SuppressManagerImpl
                  .SUPPRESS_INSPECTIONS_ANNOTATION_NAME + "({" + suppressedWarnings + ", \"" + id + "\"})", container);
          annotation.replace(newAnnotation);
        }
      }
      else {
        final int curlyBraceIndex = annotation.getText().lastIndexOf("}");
        if (curlyBraceIndex > 0) {
          annotation.replace(JavaPsiFacade.getInstance(project).getElementFactory().createAnnotationFromText(
              annotation.getText().substring(0, curlyBraceIndex) + ", \"" + id + "\"})", container));
        }
        else if (!ApplicationManager.getApplication().isUnitTestMode() && editor != null) {
          Messages.showErrorDialog(editor.getComponent(),
                                   InspectionsBundle.message("suppress.inspection.annotation.syntax.error", annotation.getText()));
        }
      }
    }
    else {
      annotation = JavaPsiFacade.getInstance(project).getElementFactory()
          .createAnnotationFromText("@" + SuppressManagerImpl.SUPPRESS_INSPECTIONS_ANNOTATION_NAME + "({\"" + id + "\"})", container);
      modifierList.addBefore(annotation, modifierList.getFirstChild());
      JavaCodeStyleManager.getInstance(project).shortenClassReferences(modifierList);
    }
  }

  protected boolean use15Suppressions(final PsiDocCommentOwner container) {
    return SuppressManager.getInstance().canHave15Suppressions(container) &&
           !SuppressManager.getInstance().alreadyHas14Suppressions(container);
  }

  public boolean startInWriteAction() {
    return true;
  }
}