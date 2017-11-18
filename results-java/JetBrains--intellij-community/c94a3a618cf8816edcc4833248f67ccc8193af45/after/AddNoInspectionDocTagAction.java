package com.intellij.codeInsight.daemon.impl.actions;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.daemon.impl.SuppressUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.jsp.jspJava.JspHolderMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ven
 */
public class AddNoInspectionDocTagAction implements IntentionAction {
  private String myID;
  protected PsiElement myContext;

  public AddNoInspectionDocTagAction(LocalInspectionTool tool, PsiElement context) {
    myID = tool.getID();
    myContext = context;
  }

  public AddNoInspectionDocTagAction(HighlightDisplayKey key, PsiElement context) {
    myID = key.getID();
    myContext = context;
  }

  public AddNoInspectionDocTagAction(String ID, PsiElement context) {
    myID = ID;
    myContext = context;
  }

  @NotNull
  public String getText() {
    PsiDocCommentOwner container = getContainer();

    @NonNls String key = container instanceof PsiClass
                         ? "suppress.inspection.class"
                         : container instanceof PsiMethod
                           ? "suppress.inspection.method"
                           : "suppress.inspection.field";
    return InspectionsBundle.message(key);
  }

  @Nullable protected PsiDocCommentOwner getContainer() {
    if (!myContext.isValid() || !(myContext.getContainingFile().getLanguage() instanceof JavaLanguage) || myContext instanceof PsiFile){
      return null;
    }
    PsiElement container = myContext;
    do {
      container = PsiTreeUtil.getParentOfType(container, PsiDocCommentOwner.class);
    }
    while (container instanceof PsiTypeParameter);
    return (PsiDocCommentOwner)container;
  }

  @NotNull
  public String getFamilyName() {
    return InspectionsBundle.message("suppress.inspection.family");
  }

  @SuppressWarnings({"SimplifiableIfStatement"})
  public boolean isAvailable(Project project, Editor editor, PsiFile file) {
    final PsiDocCommentOwner container = getContainer();
    final boolean isValid = container != null && !(container instanceof JspHolderMethod);
    if (!isValid) return false;
    if (SuppressUtil.canHave15Suppressions(file) && !SuppressUtil.alreadyHas14Suppressions(container)) return false;
    return myContext.isValid() && myContext.getManager().isInProject(myContext);
  }

  public void invoke(Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    PsiDocCommentOwner container = getContainer();
    assert container != null;
    final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(project)
      .ensureFilesWritable(container.getContainingFile().getVirtualFile());
    if (status.hasReadonlyFiles()) return;
    PsiDocComment docComment = container.getDocComment();
    PsiManager manager = myContext.getManager();
    if (docComment == null) {
      String commentText = "/** @" + GlobalInspectionContextImpl.SUPPRESS_INSPECTIONS_TAG_NAME + " "+ myID + "*/";
      docComment = manager.getElementFactory().createDocCommentFromText(commentText, null);
      PsiElement firstChild = container.getFirstChild();
      container.addBefore(docComment, firstChild);
      return;
    }

    PsiDocTag noInspectionTag = docComment.findTagByName(GlobalInspectionContextImpl.SUPPRESS_INSPECTIONS_TAG_NAME);
    if (noInspectionTag != null) {
      String tagText = "@" + GlobalInspectionContextImpl.SUPPRESS_INSPECTIONS_TAG_NAME + " "
                       + noInspectionTag.getValueElement().getText() + ","+ myID;
      noInspectionTag.replace(manager.getElementFactory().createDocTagFromText(tagText, null));
    } else {
      String tagText = "@" + GlobalInspectionContextImpl.SUPPRESS_INSPECTIONS_TAG_NAME + " " + myID;
      docComment.add(manager.getElementFactory().createDocTagFromText(tagText, null));
    }
  }

  public boolean startInWriteAction() {
    return true;
  }
}