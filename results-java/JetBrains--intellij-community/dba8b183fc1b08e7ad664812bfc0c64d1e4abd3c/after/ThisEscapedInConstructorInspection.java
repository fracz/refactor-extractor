package com.siyeh.ig.initialization;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.GroupNames;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.ig.psiutils.WellFormednessUtils;

public class ThisEscapedInConstructorInspection extends ClassInspection{
    public String getID(){
        return "ThisEscapedInObjectConstruction";
    }

    public String getDisplayName(){
        return "'this' reference escaped in object construction";
    }

    public String getGroupDisplayName(){
        return GroupNames.INITIALIZATION_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location){
        return "Escape of '#ref' during object construction #loc";
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager,
                                               boolean onTheFly){
        return new ThisExposedInConstructorInspectionVisitor(this,
                                                             inspectionManager,
                                                             onTheFly);
    }

    private static class ThisExposedInConstructorInspectionVisitor
            extends BaseInspectionVisitor{
        private boolean m_inClass = false;

        private ThisExposedInConstructorInspectionVisitor(BaseInspection inspection,
                                                          InspectionManager inspectionManager,
                                                          boolean isOnTheFly){
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitClass(PsiClass aClass){
            final boolean wasInClass = m_inClass;
            if(!m_inClass){

                m_inClass = true;
                super.visitClass(aClass);
            }
            m_inClass = wasInClass;
        }

        public void visitNewExpression(PsiNewExpression psiNewExpression){

            super.visitNewExpression(psiNewExpression);

            final boolean isInInitialization =
                    checkForInitialization(psiNewExpression);
            if(!isInInitialization){
                return;
            }

            if(psiNewExpression.getClassReference() == null){
                return;
            }

            final PsiThisExpression thisExposed =
                    checkArgumentsForThis(psiNewExpression);
            if(thisExposed == null){
                return;
            }

            final PsiJavaCodeReferenceElement refElement =
                    psiNewExpression.getClassReference();
            if(refElement != null){
                final PsiClass constructorClass =
                        (PsiClass) refElement.resolve();

                if(constructorClass != null){
                    // Skips inner classes and containing classes (as well as top level package class with file-named class)
                    if(constructorClass.getContainingFile()
                            .equals(psiNewExpression.getContainingFile())){
                        return;
                    }
                }
            }

            registerError(thisExposed);
        }

        public void visitAssignmentExpression(PsiAssignmentExpression assignment){
            super.visitAssignmentExpression(assignment);
            if(!WellFormednessUtils.isWellFormed(assignment)){
                return;
            }
            final boolean isInInitialization =
                    checkForInitialization(assignment);
            if(!isInInitialization){
                return;
            }
            final PsiExpression psiExpression =
                    getLastRightExpression(assignment);

            if(psiExpression == null ||
                       !(psiExpression instanceof PsiThisExpression)){
                return;
            }
            final PsiThisExpression thisExpression =
                    (PsiThisExpression) psiExpression;

            // Need to confirm that LeftExpression is outside of class relatives
            if(!(assignment.getLExpression() instanceof PsiReferenceExpression)){
                return;
            }
            final PsiReferenceExpression leftExpression =
                    (PsiReferenceExpression) assignment.getLExpression();
            if(!(leftExpression.resolve() instanceof PsiField)){
                return;
            }
            final PsiField field = (PsiField) leftExpression.resolve();

            if(field.getContainingFile()
                    .equals(assignment.getContainingFile())){
                return;
            }

            // Inheritance check
            final PsiClass cls = ClassUtils.getContainingClass(assignment);
            if(cls.isInheritor(field.getContainingClass(), true)){
                return;
            }

            registerError(thisExpression);
        }

        public void visitMethodCallExpression(PsiMethodCallExpression call){
            super.visitMethodCallExpression(call);

            final boolean isInInitialization = checkForInitialization(call);
            if(!isInInitialization){
                return;
            }

            final PsiReferenceExpression methodExpression =
                    call.getMethodExpression();
            if(methodExpression == null){
                return;
            }
            final PsiMethod calledMethod = call.resolveMethod();
            if(calledMethod == null){
                return;
            }
            if(calledMethod.isConstructor()){
                return;
            }

            final PsiClass calledMethodClass =
                    calledMethod.getContainingClass();
            final PsiClass methodClass =
                    ClassUtils.getContainingClass(call);

            if(calledMethodClass.equals(methodClass))   // compares class types statically?
            {
                return;
            }
            final PsiThisExpression thisExposed = checkArgumentsForThis(call);
            if(thisExposed == null){
                return;
            }

            // Methods - static or not - from superclasses don't trigger
            if(methodClass.isInheritor(calledMethodClass, true)){
                return;
            }

            // Make sure using this with members of self or superclasses doesn't trigger
            final PsiExpression qualifier =
                    methodExpression.getQualifierExpression();
            if(!(qualifier instanceof PsiReferenceExpression)){
                return;
            }
            final PsiReferenceExpression qualifiedExpression =
                    (PsiReferenceExpression) qualifier;
            final PsiElement referent = qualifiedExpression.resolve();
            if(referent instanceof PsiField){
                final PsiField field = (PsiField) referent;
                final PsiClass containingClass = field.getContainingClass();

                if(methodClass.equals(containingClass) ||
                           methodClass.isInheritor(containingClass, true)){
                    return;
                }
            }

            registerError(thisExposed);
        }

        // Get rightmost expression of assignment. Used when assignments are chained. Recursive
        private static PsiExpression getLastRightExpression(PsiAssignmentExpression assignmentExp){

            if(assignmentExp == null){
                return null;
            }

            final PsiExpression expression = assignmentExp.getRExpression();
            if(expression == null){
                return null;
            }

            if(expression instanceof PsiAssignmentExpression){
                return getLastRightExpression((PsiAssignmentExpression) expression);
            }
            return expression;
        }

        /**
                 * @param call
                 * @return true if CallExpression is in a constructor, instance
                 *         initializer, or field initializaer. Otherwise it returns
                 *         false
                 */
        private static boolean checkForInitialization(PsiElement call){
            final PsiMethod method =
                    (PsiMethod) PsiTreeUtil.getParentOfType(call,
                                                            PsiMethod.class);
            if(method != null){
                return method.isConstructor();
            }
            final PsiField field =
                    (PsiField) PsiTreeUtil.getParentOfType(call,
                                                           PsiField.class);
            if(field != null){
                return true;
            }
            final PsiClassInitializer classInitializer =
                    (PsiClassInitializer) PsiTreeUtil.getParentOfType(call,
                                                                      PsiClassInitializer.class);
            if(classInitializer != null){
                return !classInitializer.hasModifierProperty(PsiModifier.STATIC);
            }
            return false;
        }

        // If there are more than two of 'this' as arguments, only marks the first until it is removed. No big deal.
        private static PsiThisExpression checkArgumentsForThis(PsiCall call){
            final PsiExpressionList peList = call.getArgumentList();
            if(peList == null){   // array initializer
                return null;
            }
            final PsiExpression[] argExpressions = peList.getExpressions();
            for(int i = 0; i < argExpressions.length; i++){
                final PsiExpression argExpression = argExpressions[i];

                if(argExpression instanceof PsiThisExpression){
                    return (PsiThisExpression) argExpression;
                }
            }
            return null;
        }
    }
}