package com.intellij.codeInsight.generation;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.psi.infos.CandidateInfo;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;

public class GenerateConstructorHandler extends GenerateMembersHandlerBase {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.generation.GenerateConstructorHandler");
  private boolean myCopyJavadoc;

  public GenerateConstructorHandler() {
    super(CodeInsightBundle.message("generate.constructor.fields.chooser.title"));
  }

  protected Object[] getAllOriginalMembers(PsiClass aClass) {
    PsiField[] fields = aClass.getFields();
    ArrayList<PsiElement> array = new ArrayList<PsiElement>();
    final PsiSearchHelper searchHelper = aClass.getManager().getSearchHelper();
    for (PsiField field : fields) {
      if (field.hasModifierProperty(PsiModifier.STATIC)) continue;

      if (field.hasModifierProperty(PsiModifier.FINAL) && field.getInitializer() != null) continue;
      if (searchHelper.isFieldBoundToForm(field)) continue;
      array.add(field);
    }
    return array.toArray(new PsiElement[array.size()]);
  }

  protected Object[] chooseOriginalMembers(PsiClass aClass, Project project) {
    if (aClass instanceof PsiAnonymousClass){
      Messages.showMessageDialog(project,
                                 CodeInsightBundle.message("error.attempt.to.generate.constructor.for.anonymous.class"),
                                 CommonBundle.getErrorTitle(),
                                 Messages.getErrorIcon());
      return null;
    }

    myCopyJavadoc = false;
    PsiMethod[] baseConstructors = null;
    PsiClass baseClass = aClass.getSuperClass();
    if (baseClass != null){
      ArrayList<PsiMethod> array = new ArrayList<PsiMethod>();
      PsiMethod[] methods = baseClass.getMethods();
      for (PsiMethod method : methods) {
        if (method.isConstructor()) {
          if (method.getManager().getResolveHelper().isAccessible(method, aClass, aClass)) {
            array.add(method);
          }
        }
      }
      if (array.size() > 0){
        if (array.size() == 1){
          baseConstructors = new PsiMethod[]{array.get(0)};
        }
        else{
          PsiSubstitutor substitutor = TypeConversionUtil.getSuperClassSubstitutor(baseClass, aClass, PsiSubstitutor.EMPTY);
          Object[] constructors;
          if (substitutor != PsiSubstitutor.EMPTY) {
            constructors = new CandidateInfo[array.size()];
            for (int i = 0; i < array.size(); i++) {
              constructors[i] = new CandidateInfo(array.get(i), substitutor);
            }
          } else {
            constructors = array.toArray(new Object[array.size()]);
          }
          MemberChooser chooser = new MemberChooser(constructors, false, true, project);
          chooser.setTitle(CodeInsightBundle.message("generate.constructor.super.constructor.chooser.title"));
          chooser.show();
          Object[] elements = chooser.getSelectedElements();
          if (elements == null || elements.length == 0) return null;
          baseConstructors = new PsiMethod[elements.length];
          for(int i = 0; i < elements.length; i++){
            if (elements[i] instanceof PsiMethod) {
              baseConstructors[i] = (PsiMethod)elements[i];
            }
            else {
              baseConstructors[i] = (PsiMethod)((CandidateInfo)elements[i]).getElement();
            }
          }
          myCopyJavadoc = chooser.isCopyJavadoc();
        }
      }
    }

    Object[] allMembers = getAllOriginalMembers(aClass);
    Object[] members;
    if (allMembers.length == 0) {
      members = PsiElement.EMPTY_ARRAY;
    }
    else{
      members = chooseMembers(allMembers, true, false, project);
      if (members == null) return null;
    }
    if (baseConstructors != null){
      ArrayList<Object> array = new ArrayList<Object>();
      for (PsiMethod baseConstructor : baseConstructors) {
        array.add(baseConstructor);
      }
      for (Object member : members) {
        array.add(member);
      }
      members = array.toArray(new PsiElement[array.size()]);
    }

    return members;
  }

  protected Object[] generateMemberPrototypes(PsiClass aClass, Object[] members) throws IncorrectOperationException {
    ArrayList<PsiMethod> baseConstructors = new ArrayList<PsiMethod>();
    ArrayList<PsiElement> fieldsVector = new ArrayList<PsiElement>();
    for (Object member1 : members) {
      PsiElement member = (PsiElement)member1;
      if (member instanceof PsiMethod) {
        baseConstructors.add((PsiMethod)member);
      }
      else {
        fieldsVector.add(member);
      }
    }
    PsiField[] fields = fieldsVector.toArray(new PsiField[fieldsVector.size()]);

    PsiMethod[] constructors;
    if (baseConstructors.size() > 0){
      constructors = new PsiMethod[baseConstructors.size()];
      PsiSubstitutor substitutor = TypeConversionUtil.getSuperClassSubstitutor(baseConstructors.get(0).getContainingClass(), aClass, PsiSubstitutor.EMPTY);
      for(int i = 0; i < baseConstructors.size(); i++){
        PsiMethod baseConstructor = baseConstructors.get(i);
        if (substitutor != PsiSubstitutor.EMPTY) {
          baseConstructor = GenerateMembersUtil.substituteGenericMethod(baseConstructor, substitutor);
        }
        constructors[i] = generateConstructorPrototype(aClass, baseConstructor, myCopyJavadoc, fields);
      }
    }
    else{
      constructors = new PsiMethod[1];
      constructors[0] = generateConstructorPrototype(aClass, null, false, fields);
    }
    return constructors;
  }

  public static PsiMethod generateConstructorPrototype(PsiClass aClass, PsiMethod baseConstructor, boolean copyJavaDoc, PsiField[] fields) throws IncorrectOperationException {
    PsiManager manager = aClass.getManager();
    PsiElementFactory factory = manager.getElementFactory();
    CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(manager.getProject());

    PsiMethod constructor = factory.createConstructor();
    String modifier = aClass.hasModifierProperty(PsiModifier.ABSTRACT) ? PsiModifier.PROTECTED : PsiModifier.PUBLIC;
    constructor.getModifierList().setModifierProperty(modifier, true);

    if (baseConstructor != null){
      PsiJavaCodeReferenceElement[] throwRefs = baseConstructor.getThrowsList().getReferenceElements();
      for (PsiJavaCodeReferenceElement ref : throwRefs) {
        constructor.getThrowsList().add(ref);
      }

      if(copyJavaDoc) {
        final PsiDocComment docComment = ((PsiMethod)baseConstructor.getNavigationElement()).getDocComment();
        if(docComment != null) {
          constructor.addAfter(docComment, null);
        }
      }
    }

    @NonNls StringBuffer buffer = new StringBuffer();
    buffer.append("{\n");

    if (baseConstructor != null){
      PsiClass superClass = aClass.getSuperClass();
      LOG.assertTrue(superClass != null);
      if (!"java.lang.Enum".equals(superClass.getQualifiedName())) {
        if (baseConstructor instanceof PsiCompiledElement){ // to get some parameter names
          PsiClass dummyClass = factory.createClass("Dummy");
          baseConstructor = (PsiMethod)dummyClass.add(baseConstructor);
        }
        PsiParameter[] parms = baseConstructor.getParameterList().getParameters();
        for (PsiParameter parm : parms) {
          constructor.getParameterList().add(parm);
        }
        if (parms.length > 0){
          buffer.append("super(");
          for(int j = 0; j < parms.length; j++) {
            PsiParameter parm = parms[j];
            if (j > 0){
              buffer.append(",");
            }
            buffer.append(parm.getName());
          }
          buffer.append(");\n");
        }
      }
    }

    for (PsiField field : fields) {
      String fieldName = field.getName();
      String name = codeStyleManager.variableNameToPropertyName(fieldName, VariableKind.FIELD);
      String parmName = codeStyleManager.propertyNameToVariableName(name, VariableKind.PARAMETER);
      PsiParameter parm = factory.createParameter(parmName, field.getType());

      PsiAnnotation[] annotations = field.getModifierList().getAnnotations();
      for(PsiAnnotation ann: annotations) {
        if (AnnotationUtil.NOT_NULL.equals(ann.getQualifiedName())) {
          final PsiAnnotation annotation = factory.createAnnotationFromText("@" + AnnotationUtil.NOT_NULL, field);
          parm.getModifierList().addAfter(annotation, null);
          break;
        }
      }

      constructor.getParameterList().add(parm);
      if (fieldName.equals(parmName)) {
        buffer.append("this.");
      }
      buffer.append(fieldName);
      buffer.append("=");
      buffer.append(parmName);
      buffer.append(";\n");
    }

    buffer.append("}");
    PsiCodeBlock body = factory.createCodeBlockFromText(buffer.toString(), null);
    constructor.getBody().replace(body);
    constructor = (PsiMethod)codeStyleManager.reformat(constructor);
    return constructor;
  }

  protected Object[] generateMemberPrototypes(PsiClass aClass, Object originalMember) {
    LOG.assertTrue(false);
    return null;
  }
}