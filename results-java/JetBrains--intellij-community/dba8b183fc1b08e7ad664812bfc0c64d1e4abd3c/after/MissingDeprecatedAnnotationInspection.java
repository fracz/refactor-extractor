package com.siyeh.ig.classlayout;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.*;

public class MissingDeprecatedAnnotationInspection extends ClassInspection{
    private final MissingDeprecatedAnnotationFix fix = new MissingDeprecatedAnnotationFix();

    public String getDisplayName(){
        return "Missing @Deprecated annotation";
    }

    public String getGroupDisplayName(){
        return GroupNames.CLASSLAYOUT_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location){
        return "Missing @Deprecated annotation on '#ref' #loc";
    }

    protected InspectionGadgetsFix buildFix(PsiElement location){
        return fix;
    }

    private static class MissingDeprecatedAnnotationFix
            extends InspectionGadgetsFix{
        public String getName(){
            return "Add @Deprecated annotation";
        }

        public void applyFix(Project project, ProblemDescriptor descriptor){
            if(isQuickFixOnReadOnlyFile(project, descriptor)){
                return;
            }
            final PsiElement identifier = descriptor.getPsiElement();
            final PsiModifierListOwner parent =
                    (PsiModifierListOwner) identifier.getParent();
            try{
                final PsiManager psiManager = parent.getManager();
                final PsiElementFactory factory = psiManager
                                .getElementFactory();
                final PsiAnnotation annotation = factory
                        .createAnnotationFromText("@java.lang.Deprecated",
                                                  parent);

                final PsiModifierList modifierList = parent.getModifierList();
                modifierList.addAfter(annotation, null);
            } catch(IncorrectOperationException e){
                final Class aClass = getClass();
                final String className = aClass.getName();
                final Logger logger = Logger.getInstance(className);
                logger.error(e);
            }
        }
    }
    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager,
                                               boolean onTheFly){
        return new MissingDeprecatedAnnotationVisitor(this, inspectionManager,
                                                      onTheFly);
    }

    private static class MissingDeprecatedAnnotationVisitor
            extends BaseInspectionVisitor{
        private boolean inClass = false;

        private MissingDeprecatedAnnotationVisitor(BaseInspection inspection,
                                                   InspectionManager inspectionManager,
                                                   boolean isOnTheFly){
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitClass(PsiClass aClass){
            super.visitClass(aClass);
            final boolean wasInClass = inClass;
            if(!inClass){
                inClass = true;
                final PsiManager manager = aClass.getManager();
                final LanguageLevel languageLevel =
                        manager.getEffectiveLanguageLevel();
                if(languageLevel.equals(LanguageLevel.JDK_1_3) ||
                           languageLevel.equals(LanguageLevel.JDK_1_4)){
                    return;
                }
                if(!hasDeprecatedCommend(aClass)){
                    return;
                }
                if(hasDeprecatedAnnotation(aClass)){
                    return;
                }
                registerClassError(aClass);
            }
            inClass = wasInClass;
        }

        public void visitMethod(PsiMethod method){
            final PsiManager manager = method.getManager();
            final LanguageLevel languageLevel =
                    manager.getEffectiveLanguageLevel();
            if(languageLevel.equals(LanguageLevel.JDK_1_3) ||
                       languageLevel.equals(LanguageLevel.JDK_1_4)){
                return;
            }
            if(!hasDeprecatedCommend(method)){
                return;
            }
            if(hasDeprecatedAnnotation(method)){
                return;
            }
            registerMethodError(method);
        }

        public void visitField(PsiField field){
            final PsiManager manager = field.getManager();
            final LanguageLevel languageLevel =
                    manager.getEffectiveLanguageLevel();
            if(languageLevel.equals(LanguageLevel.JDK_1_3) ||
                       languageLevel.equals(LanguageLevel.JDK_1_4)){
                return;
            }
            if(!hasDeprecatedCommend(field)){
                return;
            }
            if(hasDeprecatedAnnotation(field)){
                return;
            }
            registerFieldError(field);
        }
    }

    private static boolean hasDeprecatedAnnotation(PsiModifierListOwner element){
        final PsiModifierList modifierList = element.getModifierList();
        if(modifierList == null){
            return false;
        }
        final PsiAnnotation[] annotations = modifierList.getAnnotations();
        if(annotations == null){
            return false;
        }
        for(int i = 0; i < annotations.length; i++){
            final PsiAnnotation annotation = annotations[i];
            final PsiJavaCodeReferenceElement reference = annotation.getNameReferenceElement();
            final PsiClass annotationClass =
                    (PsiClass) reference.resolve();
            if(annotationClass == null){
                return false;
            }
            final String annotationClassName =
                    annotationClass.getQualifiedName();
            if("java.lang.Deprecated".equals(annotationClassName)){
                return true;
            }
        }
        return false;
    }

    private  static boolean hasDeprecatedCommend(PsiDocCommentOwner element){
        final PsiDocComment comment = element.getDocComment();
        if(comment == null){
            return false;
        }
        final PsiDocTag[] tags = comment.getTags();
        for(int i = 0; i < tags.length; i++){
            final String tagName = tags[i].getName();
            if("deprecated".equals(tagName)){
                return true;
            }
        }
        return false;
    }
}