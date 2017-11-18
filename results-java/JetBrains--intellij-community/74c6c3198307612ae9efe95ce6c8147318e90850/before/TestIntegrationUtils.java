package com.intellij.testIntegration;

import com.intellij.codeInsight.TestUtil;
import com.intellij.codeInsight.daemon.impl.quickfix.CreateFromUsageUtils;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.ConstantNode;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.intellij.util.IncorrectOperationException;

import java.util.ArrayList;
import java.util.List;

public class TestIntegrationUtils {
  public enum MethodKind {
    SET_UP {
      public FileTemplateDescriptor getFileTemplateDescriptor(TestFrameworkDescriptor frameworkDescriptor) {
        return frameworkDescriptor.getSetUpMethodFileTemplateDescriptor();
      }},
    TEAR_DOWN {
      public FileTemplateDescriptor getFileTemplateDescriptor(TestFrameworkDescriptor frameworkDescriptor) {
        return frameworkDescriptor.getTearDownMethodFileTemplateDescriptor();
      }},
    TEST {
      public FileTemplateDescriptor getFileTemplateDescriptor(TestFrameworkDescriptor frameworkDescriptor) {
        return frameworkDescriptor.getTestMethodFileTemplateDescriptor();
      }};

    public abstract FileTemplateDescriptor getFileTemplateDescriptor(TestFrameworkDescriptor frameworkDescriptor);
  }

  public static boolean isTest(PsiElement element) {
    PsiClass klass = findOuterClass(element);
    return klass != null && TestUtil.isTestClass(klass);
  }

  public static PsiClass findOuterClass(PsiElement element) {
    PsiClass result = PsiTreeUtil.getParentOfType(element, PsiClass.class, false);
    if (result == null) return null;

    do {
      PsiClass nextParent = PsiTreeUtil.getParentOfType(result, PsiClass.class, true);
      if (nextParent == null) return result;
      result = nextParent;
    }
    while (true);
  }

  public static List<MemberInfo> extractClassMethods(PsiClass clazz, boolean includeInherited) {
    List<MemberInfo> result = new ArrayList<MemberInfo>();

    do {
      MemberInfo.extractClassMembers(clazz, result, new MemberInfo.Filter() {
        public boolean includeMember(PsiMember member) {
          if (!(member instanceof PsiMethod)) return false;
          PsiModifierList list = member.getModifierList();
          return list.hasModifierProperty(PsiModifier.PUBLIC);
        }
      }, false);
      clazz = clazz.getSuperClass();
    }
    while (clazz != null
           && clazz.getSuperClass() != null // not the Object
           && includeInherited);

    return result;
  }

  public static PsiMethod createMethod(PsiClass targetClass, String name, String annotation) throws IncorrectOperationException {
    PsiElementFactory f = JavaPsiFacade.getInstance(targetClass.getProject()).getElementFactory();
    PsiMethod result = f.createMethod(name, PsiType.VOID);
    result.getBody().add(f.createCommentFromText("// Add your code here", result));

    if (annotation != null) {
      PsiAnnotation a = f.createAnnotationFromText("@" + annotation, result);
      PsiModifierList modifiers = result.getModifierList();
      PsiElement first = modifiers.getFirstChild();
      if (first == null) {
        modifiers.add(a);
      }
      else {
        modifiers.addBefore(a, first);
      }

      JavaCodeStyleManager.getInstance(targetClass.getProject()).shortenClassReferences(modifiers);
    }

    return result;
  }

  public static void runTestMethodTemplate(MethodKind methodKind,
                                           TestFrameworkDescriptor descriptor,
                                           final Editor editor,
                                           PsiClass targetClass,
                                           final PsiMethod method,
                                           String name,
                                           boolean automatic) {
    Template template = createTestMethodTemplate(methodKind, descriptor, targetClass, name, automatic);

    final TextRange range = method.getTextRange();
    editor.getDocument().replaceString(range.getStartOffset(), range.getEndOffset(), "");
    editor.getCaretModel().moveToOffset(range.getStartOffset());

    final Project project = targetClass.getProject();

    TemplateEditingAdapter adapter = null;

    if (!automatic) {
      adapter = new TemplateEditingAdapter() {
        @Override
        public void templateFinished(Template template) {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
              PsiFile psi = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
              PsiElement method = psi.findElementAt(range.getStartOffset());

              if (method != null) {
                method = PsiTreeUtil.getParentOfType(method, PsiMethod.class, false);
                if (method != null) {
                  CreateFromUsageUtils.setupEditor((PsiMethod)method, editor);
                }
              }
            }
          });
        }
      };
    }

    TemplateManager.getInstance(project).startTemplate(editor, template, adapter);
    PsiDocumentManager.getInstance(targetClass.getProject()).commitDocument(editor.getDocument());
  }

  private static Template createTestMethodTemplate(MethodKind methodKind,
                                                   TestFrameworkDescriptor descriptor,
                                                   PsiClass targetClass,
                                                   String name,
                                                   boolean automatic) {
    FileTemplateDescriptor templateDesc = methodKind.getFileTemplateDescriptor(descriptor);
    String templateName = templateDesc.getFileName();
    FileTemplate fileTemplate = FileTemplateManager.getInstance().getCodeTemplate(templateName);
    Template template = TemplateManager.getInstance(targetClass.getProject()).createTemplate("", "");

    String templateText = fileTemplate.getText();
    int index = templateText.indexOf("${NAME}");

    if (index == -1) {
      template.addTextSegment(templateText);
    }
    else {
      if (index > 0 && !Character.isWhitespace(templateText.charAt(index - 1))) {
        name = StringUtil.capitalize(name);
      }
      else {
        name = StringUtil.decapitalize(name);
      }
      template.addTextSegment(templateText.substring(0, index));
      Expression nameExpr = new ConstantNode(name);
      template.addVariable("", nameExpr, nameExpr, !automatic);
      template.addTextSegment(templateText.substring(index + "${NAME}".length(), templateText.length()));
    }

    return template;
  }

  public static PsiMethod createDummyMethod(Project project) {
    PsiElementFactory f = JavaPsiFacade.getInstance(project).getElementFactory();
    return f.createMethod("dummy", PsiType.VOID);
  }
}