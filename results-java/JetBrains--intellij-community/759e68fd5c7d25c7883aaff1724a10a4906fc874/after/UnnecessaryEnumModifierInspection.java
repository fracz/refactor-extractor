package com.siyeh.ig.verbose;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.GroupNames;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.ClassUtils;

public class UnnecessaryEnumModifierInspection extends BaseInspection{
    public String getDisplayName(){
        return "Unnecessary enum modifier";
    }

    public String getGroupDisplayName(){
        return GroupNames.VERBOSE_GROUP_NAME;
    }

    public ProblemDescriptor[] doCheckClass(PsiClass aClass,
                                            InspectionManager manager,
                                            boolean isOnTheFly){
        if(!aClass.isPhysical()){
            return super.doCheckClass(aClass, manager, isOnTheFly);
        }
        final BaseInspectionVisitor visitor = createVisitor(manager, isOnTheFly);
        aClass.accept(visitor);

        return visitor.getErrors();
    }

    public ProblemDescriptor[] doCheckMethod(PsiMethod method,
                                             InspectionManager manager,
                                             boolean isOnTheFly){
        if(!method.isPhysical()){
            return super.doCheckMethod(method, manager, isOnTheFly);
        }
        final BaseInspectionVisitor visitor = createVisitor(manager, isOnTheFly);
        method.accept(visitor);
        return visitor.getErrors();
    }

    public String buildErrorString(PsiElement location){
        final PsiModifierList modifierList;
        if(location instanceof PsiModifierList){
            modifierList = (PsiModifierList) location;
        } else{
            modifierList = (PsiModifierList) location.getParent();
        }
        final PsiElement parent = modifierList.getParent();
        if(parent instanceof PsiMethod){
            return "Modifier '#ref' is redundant for enum constructors #loc";
        } else{
            return "Modifier '#ref' is redundant for inner enums #loc";
        }
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager,
                                               boolean onTheFly){
        return new UnnecessaryInterfaceModifierVisitor(this, inspectionManager,
                                                       onTheFly);
    }

    public InspectionGadgetsFix buildFix(PsiElement location){
        return new UnnecessaryEnumModifierFix(location);
    }

    private static class UnnecessaryEnumModifierFix
            extends InspectionGadgetsFix{
        private final String m_name;

        private UnnecessaryEnumModifierFix(PsiElement fieldModifiers){
            super();
            m_name = "Remove '" + fieldModifiers.getText() + '\'';
        }

        public String getName(){
            return m_name;
        }

        public void applyFix(Project project, ProblemDescriptor descriptor){
            if(isQuickFixOnReadOnlyFile(project, descriptor)){
                return;
            }
            try{
                final PsiElement element = descriptor.getPsiElement();
                final PsiModifierList modifierList;
                if(element instanceof PsiModifierList){
                    modifierList = (PsiModifierList) element;
                } else{
                    modifierList = (PsiModifierList) element.getParent();
                }
                if(modifierList.getParent() instanceof PsiClass){
                    modifierList.setModifierProperty(PsiModifier.STATIC, false);
                } else{
                    modifierList.setModifierProperty(PsiModifier.PRIVATE,
                                                     false);
                }
            } catch(IncorrectOperationException e){
                final Class aClass = getClass();
                final String className = aClass.getName();
                final Logger logger = Logger.getInstance(className);
                logger.error(e);
            }
        }
    }

    private static class UnnecessaryInterfaceModifierVisitor
            extends BaseInspectionVisitor{
        private UnnecessaryInterfaceModifierVisitor(BaseInspection inspection,
                                                    InspectionManager inspectionManager,
                                                    boolean isOnTheFly){
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitClass(PsiClass aClass){
            if(!aClass.isEnum()){
                return;
            }
            if(!ClassUtils.isInnerClass(aClass)){
                return;
            }
            if(!aClass.hasModifierProperty(PsiModifier.STATIC)){
                return;
            }
            final PsiModifierList modifiers = aClass.getModifierList();
            if(modifiers == null){
                return;
            }
            final PsiElement[] children = modifiers.getChildren();
            for(int i = 0; i < children.length; i++){
                final PsiElement child = children[i];
                final String text = child.getText();
                if(PsiModifier.STATIC.equals(text)){
                    registerError(child);
                }
            }
        }

        public void visitMethod(PsiMethod method){
            // don't call super, to keep this from drilling in
            if(!method.isConstructor()){
                return;
            }
            if(!method.hasModifierProperty(PsiModifier.PRIVATE)){
                return;
            }
            final PsiClass aClass = method.getContainingClass();
            if(aClass == null){
                return;
            }
            if(!aClass.isEnum()){
                return;
            }

            final PsiModifierList modifiers = method.getModifierList();
            if(modifiers == null){
                return;
            }
            final PsiElement[] children = modifiers.getChildren();
            for(int i = 0; i < children.length; i++){
                final PsiElement child = children[i];
                final String text = child.getText();
                if(PsiModifier.PRIVATE.equals(text)){
                    registerError(child);
                }
            }
        }
    }
}