package com.intellij.codeInsight.daemon.impl.quickfix;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.ExpectedTypeInfo;
import com.intellij.codeInsight.daemon.QuickFixBundle;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateBuilder;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.refactoring.util.FieldConflictsResolver;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mike
 */
public class CreateMethodFromUsageFix extends CreateFromUsageBaseFix {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.daemon.impl.quickfix.CreateMethodFromUsageFix");

  private final SmartPsiElementPointer myMethodCall;

  public CreateMethodFromUsageFix(PsiMethodCallExpression methodCall) {
    myMethodCall = SmartPointerManager.getInstance(methodCall.getProject()).createSmartPsiElementPointer(methodCall);
  }

  protected boolean isAvailableImpl(int offset) {
    final PsiMethodCallExpression call = getMethodCall();
    if (call == null) return false;
    PsiReferenceExpression ref = call.getMethodExpression();
    String name = ref.getReferenceName();

    if (name == null || !JavaPsiFacade.getInstance(ref.getProject()).getNameHelper().isIdentifier(name)) return false;
    if (hasErrorsInArgumentList(call)) return false;
    setText(QuickFixBundle.message("create.method.from.usage.text", name));
    return true;
  }

  private static boolean isMethodSignatureExists(PsiMethodCallExpression call, PsiClass target) {
    String name = call.getMethodExpression().getReferenceName();
    PsiExpressionList list = call.getArgumentList();
    PsiMethod[] methods = target.findMethodsByName(name, false);
    for (PsiMethod method : methods) {
      if (PsiUtil.isApplicable(method, PsiSubstitutor.EMPTY, list)) return true;
    }
    return false;
  }

  static boolean hasErrorsInArgumentList(final PsiMethodCallExpression call) {
    Project project = call.getProject();
    Document document = PsiDocumentManager.getInstance(project).getDocument(call.getContainingFile());
    if (document == null) return true;

    PsiExpressionList argumentList = call.getArgumentList();
    HighlightInfo[] errorsInArgList = DaemonCodeAnalyzerImpl.getHighlights(document, HighlightSeverity.ERROR, project,
                                                                           //strictly inside arg list
                                                                      argumentList.getTextRange().getStartOffset()+1,
                                                                      argumentList.getTextRange().getEndOffset()-1);
    return errorsInArgList.length != 0;
  }

  protected PsiElement getElement() {
    final PsiMethodCallExpression call = getMethodCall();
    if (call == null || !call.getManager().isInProject(call)) return null;
    return call;
  }

  @NotNull
  protected List<PsiClass> getTargetClasses(PsiElement element) {
    List<PsiClass> targets = super.getTargetClasses(element);
    ArrayList<PsiClass> result = new ArrayList<PsiClass>();
    PsiMethodCallExpression call = getMethodCall();
    if (call == null) return Collections.emptyList();
    for (PsiClass target : targets) {
      if (!isMethodSignatureExists(call, target)) {
        result.add(target);
      }
    }
    return result;
  }

  protected void invokeImpl(PsiClass targetClass) {
    if (targetClass == null) return;
    PsiMethodCallExpression expression = getMethodCall();
    if (expression == null) return;
    final Project project = expression.getProject();
    PsiReferenceExpression ref = expression.getMethodExpression();

    if (isValidElement(expression)) return;

    PsiClass parentClass = PsiTreeUtil.getParentOfType(expression, PsiClass.class);
    PsiMember enclosingContext = PsiTreeUtil.getParentOfType(expression, PsiMethod.class, PsiField.class, PsiClassInitializer.class);


    final PsiFile targetFile = targetClass.getContainingFile();

    String methodName = ref.getReferenceName();
    LOG.assertTrue(methodName != null);

    try {
      PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();

      PsiMethod method = factory.createMethod(methodName, PsiType.VOID);

      if (targetClass.equals(parentClass)) {
        method = (PsiMethod)targetClass.addAfter(method, enclosingContext);
      }
      else {
        PsiElement anchor = enclosingContext;
        while (anchor != null && anchor.getParent() != null && !anchor.getParent().equals(targetClass)) {
          anchor = anchor.getParent();
        }
        if (anchor != null && anchor.getParent() == null) anchor = null;
        if (anchor != null) {
          method = (PsiMethod)targetClass.addAfter(method, anchor);
        }
        else {
          method = (PsiMethod)targetClass.add(method);
        }
      }

      if (enclosingContext instanceof PsiMethod && methodName.equals(enclosingContext.getName()) &&
          PsiTreeUtil.isAncestor(targetClass, parentClass, true)) {
        FieldConflictsResolver.qualifyReference(ref, method, null);
      }

      PsiCodeBlock body = method.getBody();
      assert body != null;
      if (targetClass.isInterface()) {
        body.delete();
      }

      setupVisibility(parentClass, targetClass, method.getModifierList());

      if (shouldCreateStaticMember(expression.getMethodExpression(), targetClass) && !targetClass.isInterface()) {
        PsiUtil.setModifierProperty(method, PsiModifier.STATIC, true);
      }

      final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
      final Document document = documentManager.getDocument(targetFile);

      targetClass = method.getContainingClass();
      final PsiElement context = PsiTreeUtil.getParentOfType(expression, PsiClass.class, PsiMethod.class);

      method = CodeInsightUtilBase.forcePsiPostprocessAndRestoreElement(method);
      if (method == null) return;
      body = method.getBody();
      TemplateBuilder builder = new TemplateBuilder(method);

      PsiMethodCallExpression methodCall = getMethodCall();
      if (methodCall == null) return;
      PsiExpressionList argumentList = methodCall.getArgumentList();

      final PsiSubstitutor substitutor = getTargetSubstitutor(methodCall);
      CreateFromUsageUtils.setupMethodParameters(method, builder, argumentList, substitutor);
      final ExpectedTypeInfo[] expectedTypes = CreateFromUsageUtils.guessExpectedTypes(methodCall, true);
      new GuessTypeParameters(factory)
        .setupTypeElement(method.getReturnTypeElement(), expectedTypes, substitutor, builder, context, targetClass);
      builder.setEndVariableAfter(targetClass.isInterface() ? method : body.getLBrace());
      method = CodeInsightUtilBase.forcePsiPostprocessAndRestoreElement(method);
      if (method == null) return;

      RangeMarker rangeMarker = document.createRangeMarker(method.getTextRange());
      final Editor newEditor = positionCursor(project, targetFile, method);
      Template template = builder.buildTemplate();
      newEditor.getCaretModel().moveToOffset(rangeMarker.getStartOffset());
      newEditor.getDocument().deleteString(rangeMarker.getStartOffset(), rangeMarker.getEndOffset());

      if (!targetClass.isInterface()) {
        startTemplate(newEditor, template, project, new TemplateEditingAdapter() {
          public void templateFinished(Template template) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
              public void run() {
                PsiDocumentManager.getInstance(project).commitDocument(newEditor.getDocument());
                final int offset = newEditor.getCaretModel().getOffset();
                PsiMethod method = PsiTreeUtil.findElementOfClassAtOffset(targetFile, offset - 1, PsiMethod.class, false);

                if (method != null) {
                  try {
                    CreateFromUsageUtils.setupMethodBody(method);
                  }
                  catch (IncorrectOperationException e) {
                    LOG.error(e);
                  }

                  CreateFromUsageUtils.setupEditor(method, newEditor);
                }
              }
            });
          }
        });
      }
      else {
        startTemplate(newEditor, template, project);
      }
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }

  protected boolean isValidElement(PsiElement element) {
    PsiMethodCallExpression callExpression = (PsiMethodCallExpression) element;
    PsiReferenceExpression referenceExpression = callExpression.getMethodExpression();

    return CreateFromUsageUtils.isValidMethodReference(referenceExpression, callExpression);
  }

  @NotNull
  public String getFamilyName() {
    return QuickFixBundle.message("create.method.from.usage.family");
  }

  @Nullable
  private PsiMethodCallExpression getMethodCall() {
    return (PsiMethodCallExpression)myMethodCall.getElement();
  }
}