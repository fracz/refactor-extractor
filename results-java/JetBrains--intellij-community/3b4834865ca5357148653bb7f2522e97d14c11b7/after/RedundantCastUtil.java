/*
 * Created by IntelliJ IDEA.
 * User: max
 * Date: Mar 24, 2002
 * Time: 6:08:14 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.intellij.codeInspection.redundantCast;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.IncorrectOperationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedundantCastUtil {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.redundantCast.RedundantCastUtil");
  public static List<PsiTypeCastExpression> getRedundantCastsInside(PsiElement where) {
    final ArrayList<PsiTypeCastExpression> result = new ArrayList<PsiTypeCastExpression>();
    PsiElementProcessor<PsiTypeCastExpression> processor = new PsiElementProcessor<PsiTypeCastExpression>() {
      public boolean execute(PsiTypeCastExpression element) {
        result.add(element);
        return true;
      }
    };
    where.acceptChildren(new MyCollectingVisitor(processor));
    return result;
  }

  public static boolean isCastRedundant (PsiTypeCastExpression typeCast) {
    MyIsRedundantVisitor visitor = new MyIsRedundantVisitor();
    PsiElement parent = typeCast.getParent();
    while(parent instanceof PsiParenthesizedExpression) parent = parent.getParent();
    if (parent instanceof PsiExpressionList) parent = parent.getParent();
    if (parent instanceof PsiReferenceExpression) parent = parent.getParent();
    parent.accept(visitor);
    return visitor.isRedundant();
  }

  private static class MyCollectingVisitor extends MyIsRedundantVisitor {
    private final PsiElementProcessor<PsiTypeCastExpression> myProcessor;
    private Set<PsiTypeCastExpression> myFoundCasts = new HashSet<PsiTypeCastExpression>();
    public MyCollectingVisitor(PsiElementProcessor<PsiTypeCastExpression> processor) {
      myProcessor = processor;
    }

    public void visitElement(PsiElement element) {
      element.acceptChildren(this);
    }

    public void visitClass(PsiClass aClass) {
      // avoid multiple visit
    }

    public void visitMethod(PsiMethod method) {
      // avoid multiple visit
    }

    public void visitField(PsiField field) {
      // avoid multiple visit
    }

    protected void addToResults(PsiTypeCastExpression typeCast){
      if (!isTypeCastSemantical(typeCast) && myFoundCasts.add(typeCast)) {
        myProcessor.execute(typeCast);
      }
    }

    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
      super.visitAssignmentExpression(expression);
      expression.acceptChildren(this);
    }

    public void visitVariable(PsiVariable variable) {
      super.visitVariable(variable);
      variable.acceptChildren(this);
    }

    public void visitBinaryExpression(PsiBinaryExpression expression) {
      super.visitBinaryExpression(expression);
      expression.acceptChildren(this);
    }

    public void visitNewExpression(PsiNewExpression expression) {
      super.visitNewExpression(expression);
      expression.acceptChildren(this);
    }

    public void visitTypeCastExpression(PsiTypeCastExpression typeCast) {
      super.visitTypeCastExpression(typeCast);
      typeCast.acceptChildren(this);
    }
  }

  private static class MyIsRedundantVisitor extends PsiElementVisitor {
    public boolean isRedundant() {
      return myIsRedundant;
    }

    boolean myIsRedundant = false;
    protected void addToResults(PsiTypeCastExpression typeCast){
      if (!isTypeCastSemantical(typeCast)) {
        myIsRedundant = true;
      }
    }

    public void visitConditionalExpression(PsiConditionalExpression expression) {
      // Do not go inside conditional expression because branches are required to be exactly the same type, not assignable.
    }

    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
      processPossibleTypeCast(expression.getRExpression(), expression.getLExpression().getType());
    }

    public void visitVariable(PsiVariable variable) {
      processPossibleTypeCast(variable.getInitializer(), variable.getType());
    }

    public void visitBinaryExpression(PsiBinaryExpression expression) {
      PsiExpression rExpr = deParenthesize(expression.getLOperand());
      PsiExpression lExpr = deParenthesize(expression.getROperand());

      if (rExpr != null && lExpr != null) {
        final IElementType binaryToken = expression.getOperationSign().getTokenType();
        processBinaryExpressionOperand(lExpr, rExpr, binaryToken);
        processBinaryExpressionOperand(rExpr, lExpr, binaryToken);
      }
    }

    private void processBinaryExpressionOperand(final PsiExpression operand,
                                                final PsiExpression otherOperand,
                                                final IElementType binaryToken) {
      if (operand instanceof PsiTypeCastExpression) {
        PsiTypeCastExpression typeCast = (PsiTypeCastExpression)operand;
        PsiExpression castOperand = typeCast.getOperand();
        if (castOperand != null) {
          if (TypeConversionUtil.isBinaryOperatorApplicable(binaryToken, castOperand, otherOperand, false)) {
            addToResults(typeCast);
          }
        }
      }
    }

    private void processPossibleTypeCast(PsiExpression rExpr, PsiType lType) {
      rExpr = deParenthesize(rExpr);
      if (rExpr instanceof PsiTypeCastExpression) {
        PsiExpression castOperand = ((PsiTypeCastExpression)rExpr).getOperand();
        if (castOperand != null) {
          PsiType operandType = castOperand.getType();
          if (operandType != null) {
            if (lType != null && TypeConversionUtil.isAssignable(lType, operandType, false)) {
              addToResults((PsiTypeCastExpression)rExpr);
            }
          }
        }
      }
    }

    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
      processCall(expression);

      checkForVirtual(expression);
    }

    private void checkForVirtual(PsiMethodCallExpression methodCall) {
      PsiReferenceExpression methodExpr = methodCall.getMethodExpression();
      PsiExpression qualifier = methodExpr.getQualifierExpression();
      try {
        if (!(qualifier instanceof PsiParenthesizedExpression)) return;
        PsiExpression operand = ((PsiParenthesizedExpression)qualifier).getExpression();
        if (!(operand instanceof PsiTypeCastExpression)) return;
        PsiTypeCastExpression typeCast = (PsiTypeCastExpression)operand;
        PsiExpression castOperand = typeCast.getOperand();
        if (castOperand == null) return;

        PsiType type = castOperand.getType();
        if (type == null) return;
        if (type instanceof PsiPrimitiveType) return;

        final JavaResolveResult resolveResult = methodExpr.advancedResolve(false);
        PsiMethod targetMethod = (PsiMethod)resolveResult.getElement();
        if (targetMethod == null) return;
        if (targetMethod.hasModifierProperty(PsiModifier.STATIC)) return;

        try {
          PsiManager manager = methodExpr.getManager();
          PsiElementFactory factory = manager.getElementFactory();

          PsiMethodCallExpression newCall = (PsiMethodCallExpression)factory.createExpressionFromText(methodCall.getText(), methodCall);
          PsiExpression newQualifier = newCall.getMethodExpression().getQualifierExpression();
          PsiExpression newOperand = ((PsiTypeCastExpression)((PsiParenthesizedExpression)newQualifier).getExpression()).getOperand();
          newQualifier.replace(newOperand);

          final JavaResolveResult newResult = newCall.getMethodExpression().advancedResolve(false);
          if (!newResult.isValidResult()) return;
          final PsiMethod newTargetMethod = (PsiMethod)newResult.getElement();
          final PsiType newReturnType = newResult.getSubstitutor().substitute(newTargetMethod.getReturnType());
          final PsiType oldReturnType = resolveResult.getSubstitutor().substitute(targetMethod.getReturnType());
          if (newReturnType.equals(oldReturnType)) {
            if (newTargetMethod.equals(targetMethod)) {
                addToResults(typeCast);
            } else if (newTargetMethod.getSignature(newResult.getSubstitutor()).equals(targetMethod.getSignature(resolveResult.getSubstitutor())) &&
                       !(newTargetMethod.isDeprecated() && !targetMethod.isDeprecated())) { // see SCR11555, SCR14559
              addToResults(typeCast);
            }
          }
          qualifier = ((PsiTypeCastExpression) ((PsiParenthesizedExpression) qualifier).getExpression()).getOperand();
        }
        catch (IncorrectOperationException e) {
          return;
        }
      } finally {
        if (qualifier != null) {
          qualifier.accept(this);
        }
      }
    }

    public void visitNewExpression(PsiNewExpression expression) {
      processCall(expression);
    }

    public void visitReferenceExpression(PsiReferenceExpression expression) { }

    private void processCall(PsiCallExpression expression){
      PsiMethod oldMethod = null;
      PsiParameter[] methodParms = null;
      boolean[] typeCastCandidates = null;
      boolean hasCandidate = false;



      PsiExpressionList argumentList = expression.getArgumentList();
      if (argumentList == null) return;
      PsiExpression[] args = argumentList.getExpressions();
      for (int i = 0; i < args.length; i++) {
        PsiExpression arg = args[i];
        arg = deParenthesize(arg);
        if (arg instanceof PsiTypeCastExpression) {
          if (oldMethod == null){
            oldMethod = expression.resolveMethod();
            if (oldMethod == null) return;
            methodParms = oldMethod.getParameterList().getParameters();
            if (methodParms.length == 0 || methodParms.length > args.length) return;
            typeCastCandidates = new boolean[args.length];
          }

          PsiExpression castOperand = ((PsiTypeCastExpression)arg).getOperand();
          if (castOperand == null) return;
          PsiType operandType = castOperand.getType();
          if (operandType == null) return;
          PsiParameter methodParm = methodParms[Math.min(i, methodParms.length - 1)];
          if (!methodParm.getType().isAssignableFrom(operandType)) continue;

          //Check explicit cast for varargs parameter, see SCR 37199
          if (args.length == methodParms.length) {
            if (PsiType.NULL.equals(operandType) && methodParm.isVarArgs()) continue;
          }

          typeCastCandidates[i] = true;
          hasCandidate = true;
        }
      }

      if (hasCandidate) {
        PsiManager manager = expression.getManager();
        PsiElementFactory factory = manager.getElementFactory();

        JavaResolveResult newResult;

        try {
          PsiCallExpression newCall = (PsiCallExpression)factory.createExpressionFromText(expression.getText(), expression);
          PsiExpression[] newArgs = newCall.getArgumentList().getExpressions();
          for (int i = newArgs.length - 1; i >= 0; i--) {
            if (typeCastCandidates[i]){
              PsiTypeCastExpression castExpression = (PsiTypeCastExpression)deParenthesize(newArgs[i]);
              PsiExpression castOperand = castExpression.getOperand();
              if (castOperand == null) return;
              castExpression.replace(castOperand);
            }
          }

          newResult = newCall.resolveMethodGenerics();
        }
        catch (IncorrectOperationException e) {
          return;
        }

        if (oldMethod.equals(newResult.getElement()) && newResult.isValidResult()) {
          for(int i = 0; i < args.length; i++){
            PsiExpression arg = deParenthesize(args[i]);
            if (typeCastCandidates[i]){
              addToResults((PsiTypeCastExpression)arg);
            }
          }
        }
      }

      for (PsiExpression arg : args) {
        if (arg instanceof PsiTypeCastExpression) {
          PsiExpression castOperand = ((PsiTypeCastExpression)arg).getOperand();
          castOperand.accept(this);
        }
        else {
          arg.accept(this);
        }
      }
    }

    private PsiExpression deParenthesize(PsiExpression arg) {
      while (arg instanceof PsiParenthesizedExpression) arg = ((PsiParenthesizedExpression) arg).getExpression();
      return arg;
    }

    public void visitTypeCastExpression(PsiTypeCastExpression typeCast) {
      PsiExpression operand = typeCast.getOperand();
      if (operand == null) return;

      PsiElement expr = deParenthesize(operand);

      if (expr instanceof PsiTypeCastExpression) {
        PsiType castType = ((PsiTypeCastExpression)expr).getCastType().getType();
        if (!(castType instanceof PsiPrimitiveType)) {
          addToResults((PsiTypeCastExpression)expr);
        }
      }
      else {
        processAlreadyHasTypeCast(typeCast);
      }
    }

    private void processAlreadyHasTypeCast(PsiTypeCastExpression typeCast){
      PsiElement parent = typeCast.getParent();
      while(parent instanceof PsiParenthesizedExpression) parent = parent.getParent();
      if (parent instanceof PsiExpressionList) return; // do not replace in arg lists - should be handled by parent

      if (isTypeCastSemantical(typeCast)) return;

      PsiType toType = typeCast.getCastType().getType();
      PsiType fromType = typeCast.getOperand().getType();
      if (fromType == null || toType == null) return;
      if (parent instanceof PsiReferenceExpression) {
        if (toType instanceof PsiClassType && fromType instanceof PsiPrimitiveType) return; //explicit boxing
        //Check accessibility
        if (fromType instanceof PsiClassType) {
          final PsiReferenceExpression refExpression = ((PsiReferenceExpression)parent);
          PsiElement element = refExpression.resolve();
          if (!(element instanceof PsiMember)) return;
          PsiClass accessClass = ((PsiClassType)fromType).resolve();
          if (accessClass == null) return;
          if (!parent.getManager().getResolveHelper().isAccessible((PsiMember)element, typeCast, accessClass)) return;
          if (!isCastRedundantInRefExpression(refExpression, typeCast.getOperand())) return;
        }
      }

      if (TypeConversionUtil.isAssignable(toType, fromType, false)) {
        addToResults(typeCast);
      }
    }
  }

  private static boolean isCastRedundantInRefExpression (PsiReferenceExpression refExpression, PsiExpression castOperand) {
    PsiElement resolved = refExpression.resolve();
    final PsiReferenceExpression copy = (PsiReferenceExpression)refExpression.copy();
    try {
      copy.getQualifierExpression().replace(castOperand);
      if (copy.resolve() != resolved) return false;
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
      return false;
    }

    return true;
  }

  public static boolean isTypeCastSemantical(PsiTypeCastExpression typeCast) {
    PsiExpression operand = typeCast.getOperand();
    if (operand != null) {
      PsiType opType = operand.getType();
      PsiType castType = typeCast.getCastType().getType();
      if (castType instanceof PsiPrimitiveType) {
        if (opType instanceof PsiPrimitiveType) {
          return !opType.equals(castType); // let's suppose all not equal primitive casts are necessary
        }
      }
      else if (castType instanceof PsiClassType && ((PsiClassType)castType).hasParameters()) {
        if (opType instanceof PsiClassType && ((PsiClassType)opType).isRaw()) return true;
      }
    }

    return false;
  }
}