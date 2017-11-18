package com.siyeh.ig.resources;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.GroupNames;

import java.util.HashSet;
import java.util.Set;

public class JDBCResourceInspection extends ExpressionInspection{
    private static final String[] creationMethodClassName =
            new String[]{
                            "java.sql.Driver",
                            "java.sql.DriverManager",
                            "java.sql.Connection",
                            "java.sql.Connection",
                            "java.sql.Connection",
                            "java.sql.Statement",
                            "java.sql.Statement",
                    };
    private static final String[] creationMethodName =
            new String[]{
                            "connect",
                            "getConnection",
                            "createStatement",
                            "prepareStatement",
                            "prepareCall",
                            "executeQuery",
                            "getResultSet",
                    };

    /**
         * @noinspection StaticCollection
         */
    private static final Set creationMethodNameSet = new HashSet(8);

    static
    {
        for(int i = 0; i < creationMethodClassName.length; i++){
            creationMethodNameSet.add(creationMethodClassName[i]);
        }
    }

    public String getID(){
        return "JDBCResourceOpenedButNotSafelyClosed";
    }

    public String getDisplayName(){
        return "JDBC resource opened but not safely closed";
    }

    public String getGroupDisplayName(){
        return GroupNames.RESOURCE_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location){
        final PsiExpression expression = (PsiExpression) location;
        final PsiType type = expression.getType();
        final String text = type.getPresentableText();
        return "JDBC " + text +
                       " should be opened in a try block, and closed in a finally block #loc";
    }

    public BaseInspectionVisitor createVisitor(InspectionManager inspectionManager,
                                               boolean onTheFly){
        return new JDBCResourceVisitor(this, inspectionManager, onTheFly);
    }

    private static class JDBCResourceVisitor extends BaseInspectionVisitor{
        private JDBCResourceVisitor(BaseInspection inspection,
                                    InspectionManager inspectionManager,
                                    boolean isOnTheFly){
            super(inspection, inspectionManager, isOnTheFly);
        }

        public void visitMethodCallExpression(PsiMethodCallExpression expression){
            super.visitMethodCallExpression(expression);
            if(!isJDBCResourceCreation(expression)){
                return;
            }
            final PsiElement parent = expression.getParent();
            if(!(parent instanceof PsiAssignmentExpression)){
                registerError(expression);
                return;
            }
            final PsiAssignmentExpression assignment =
                    (PsiAssignmentExpression) parent;
            final PsiExpression lhs = assignment.getLExpression();
            if(!(lhs instanceof PsiReferenceExpression)){
                return;
            }
            final PsiElement referent = ((PsiReference) lhs).resolve();
            if(referent == null || !(referent instanceof PsiVariable)){
                return;
            }
            final PsiVariable boundVariable = (PsiVariable) referent;

            PsiElement currentContext = expression;
            while(true){
                final PsiTryStatement tryStatement =
                        (PsiTryStatement) PsiTreeUtil.getParentOfType(currentContext,
                                                                      PsiTryStatement.class);
                if(tryStatement == null){
                    registerError(expression);
                    return;
                }
                if(resourceIsOpenedInTryAndClosedInFinally(tryStatement,
                                                           expression,
                                                           boundVariable)){
                    return;
                }
                currentContext = tryStatement;
            }
        }

        private static boolean resourceIsOpenedInTryAndClosedInFinally(PsiTryStatement tryStatement,
                                                                       PsiExpression lhs,
                                                                       PsiVariable boundVariable){
            final PsiCodeBlock finallyBlock = tryStatement.getFinallyBlock();
            if(finallyBlock == null){
                return false;
            }
            final PsiCodeBlock tryBlock = tryStatement.getTryBlock();
            if(tryBlock == null){
                return false;
            }
            if(!PsiTreeUtil.isAncestor(tryBlock, lhs, true)){
                return false;
            }
            return containsResourceClose(finallyBlock, boundVariable);
        }

        private static boolean containsResourceClose(PsiCodeBlock finallyBlock,
                                                     PsiVariable boundVariable){
            final ResourceCloseVisitor visitor =
                    new ResourceCloseVisitor(boundVariable);
            finallyBlock.accept(visitor);
            return visitor.containsResourceClose();
        }
    }

    private static class ResourceCloseVisitor
            extends PsiRecursiveElementVisitor{
        private boolean containsResourceClose = false;
        private PsiVariable streamToClose;

        private ResourceCloseVisitor(PsiVariable streamToClose){
            super();
            this.streamToClose = streamToClose;
        }

        public void visitElement(PsiElement element){
            if(!containsResourceClose){
                super.visitElement(element);
            }
        }

        public void visitMethodCallExpression(PsiMethodCallExpression call){
            if(containsResourceClose){
                return;
            }
            super.visitMethodCallExpression(call);
            final PsiReferenceExpression methodExpression =
                    call.getMethodExpression();
            if(methodExpression == null){
                return;
            }
            final String methodName = methodExpression.getReferenceName();
            if(!"close".equals(methodName)){
                return;
            }
            final PsiExpression qualifier =
                    methodExpression.getQualifierExpression();
            if(!(qualifier instanceof PsiReferenceExpression)){
                return;
            }
            final PsiElement referent =
                    ((PsiReference) qualifier).resolve();
            if(referent == null){
                return;
            }
            if(referent.equals(streamToClose)){
                containsResourceClose = true;
            }
        }

        public boolean containsResourceClose(){
            return containsResourceClose;
        }
    }

    private static boolean isJDBCResourceCreation(PsiMethodCallExpression expression){
        final PsiReferenceExpression methodExpression =
                expression.getMethodExpression();
        if(methodExpression == null){
            return false;
        }
        final String name = methodExpression.getReferenceName();

        if(!creationMethodNameSet.contains(name)){
            return false;
        }
        final PsiMethod method = expression.resolveMethod();
        if(method == null){
            return false;
        }
        for(int i = 0; i < creationMethodName.length; i++){
            if(name.equals(creationMethodName[i])){
                final PsiClass containingClass = method.getContainingClass();
                if(containingClass == null){
                    return false;
                }
                final String className = containingClass.getQualifiedName();
                if(className.equals(creationMethodClassName[i])){
                    return true;
                }
            }
        }
        return false;
    }
}