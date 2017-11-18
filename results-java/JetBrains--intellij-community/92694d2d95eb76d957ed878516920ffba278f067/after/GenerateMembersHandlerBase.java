package com.intellij.codeInsight.generation;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
abstract class GenerateMembersHandlerBase implements CodeInsightActionHandler {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.generation.GenerateMembersHandlerBase");

  private String myChooserTitle;
  protected boolean myToCopyJavaDoc = false;

  public GenerateMembersHandlerBase(String chooserTitle) {
    myChooserTitle = chooserTitle;
  }

  public final void invoke(final Project project, final Editor editor, PsiFile file) {
    if (!file.isWritable()){
      if (!FileDocumentManager.fileForDocumentCheckedOutSuccessfully(editor.getDocument(), project)){
        return;
      }
   }
    PsiDocumentManager.getInstance(project).commitAllDocuments();

    int offset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(offset);
    do {
      element = PsiTreeUtil.getParentOfType(element, PsiClass.class);
    } while (element instanceof PsiTypeParameter);

    final PsiClass aClass = (PsiClass) element;
    if (aClass == null || aClass.isInterface()) return; //?
    LOG.assertTrue(aClass.isValid());
    LOG.assertTrue(aClass.getContainingFile() != null);

    final ClassMember[] members = chooseOriginalMembers(aClass, project);
    if (members == null) return;

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        doGenerate(project, editor, aClass, members);
      }
    });
  }

  private void doGenerate(final Project project, final Editor editor, PsiClass aClass, ClassMember[] members) {
    int offset = editor.getCaretModel().getOffset();

    int col = editor.getCaretModel().getLogicalPosition().column;
    int line = editor.getCaretModel().getLogicalPosition().line;
    editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(0, 0));

    Object[] newMembers;
    try{
      Object[] prototypes = generateMemberPrototypes(aClass, members);
      newMembers = GenerateMembersUtil.insertMembersAtOffset(aClass.getContainingFile(), offset, prototypes);
    }
    catch(IncorrectOperationException e){
      LOG.error(e);
      return;
    }

    editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(line, col));

    final ArrayList<TemplateGenerationInfo> templates = new ArrayList<TemplateGenerationInfo>();
    for (Object member : newMembers) {
      if (member instanceof TemplateGenerationInfo) {
        templates.add((TemplateGenerationInfo) member);
      }
    }

    if (!templates.isEmpty()){
      new ProcessTemplatesRunnable(project, templates, editor).run();
    }
    else if (newMembers.length > 0){
      positionCaret(editor, (PsiElement)newMembers[0]);
    }
  }

  @Nullable
  protected ClassMember[] chooseOriginalMembers(PsiClass aClass, Project project) {
    ClassMember[] allMembers = getAllOriginalMembers(aClass);
    return chooseMembers(allMembers, false, false, project);
  }

  protected final ClassMember[] chooseMembers(ClassMember[] members, boolean allowEmptySelection, boolean copyJavadocCheckbox, Project project) {
    MemberChooser<ClassMember> chooser = new MemberChooser<ClassMember>(members, allowEmptySelection, true, project);
    chooser.setTitle(myChooserTitle);
    chooser.setCopyJavadocVisible(copyJavadocCheckbox);
    chooser.show();
    myToCopyJavaDoc = chooser.isCopyJavadoc();
    final List<ClassMember> list = chooser.getSelectedElements();
    return list == null ? null : list.toArray(new ClassMember[list.size()]);
  }

  protected Object[] generateMemberPrototypes(PsiClass aClass, ClassMember[] members) throws IncorrectOperationException {
    ArrayList<Object> array = new ArrayList<Object>();
    for (ClassMember member : members) {
      Object[] prototypes = generateMemberPrototypes(aClass, member);
      if (prototypes != null) {
        array.addAll(Arrays.asList(prototypes));
      }
    }
    return array.toArray(new Object[array.size()]);
  }

  protected abstract ClassMember[] getAllOriginalMembers(PsiClass aClass);

  protected abstract Object[] generateMemberPrototypes(PsiClass aClass, ClassMember originalMember) throws IncorrectOperationException;

  protected void positionCaret(Editor editor, PsiElement firstMember) {
    GenerateMembersUtil.positionCaret(editor, firstMember, false);
  }

  public boolean startInWriteAction() {
    return false;
  }
}