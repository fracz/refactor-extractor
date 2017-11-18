package com.intellij.refactoring.introduceparameterobject.usageInfo;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiPostfixExpression;
import com.intellij.psi.PsiPrefixExpression;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.psi.MutationUtils;
import com.intellij.refactoring.util.FixableUsageInfo;
import com.intellij.util.IncorrectOperationException;

public class ReplaceParameterIncrementDecrement extends FixableUsageInfo {
  private final PsiExpression expression;
  private final String newParameterName;
  private final String parameterSetterName;
  private final String parameterGetterName;

  public ReplaceParameterIncrementDecrement(PsiExpression element,
                                            String newParameterName,
                                            String parameterSetterName,
                                            String parameterGetterName) {

    super(element);
    this.parameterSetterName = parameterSetterName;
    this.parameterGetterName = parameterGetterName;
    this.newParameterName = newParameterName;
    final PsiPrefixExpression prefixExpr = PsiTreeUtil.getParentOfType(element, PsiPrefixExpression.class);
    if (prefixExpr != null) {
      expression = prefixExpr;
    }
    else {
      expression = PsiTreeUtil.getParentOfType(element, PsiPostfixExpression.class);
    }
  }

  public void fixUsage() throws IncorrectOperationException {
    final PsiJavaToken sign = expression instanceof PsiPrefixExpression
                              ? ((PsiPrefixExpression)expression).getOperationSign()
                              : ((PsiPostfixExpression)expression).getOperationSign();
    final String operator = sign.getText();
    final String strippedOperator = operator.substring(0, operator.length() - 1);
    final String newExpression =
      newParameterName + '.' + parameterSetterName + '(' + newParameterName + '.' + parameterGetterName + "()" + strippedOperator + "1)";
    MutationUtils.replaceExpression(newExpression, expression);
  }

}