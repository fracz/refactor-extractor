package org.jetbrains.postfixCompletion.Infrastructure;

import com.intellij.codeInsight.lookup.*;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.util.*;
import com.intellij.psi.*;
import com.intellij.psi.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

// todo: check usages of PsiBinaryExpression with PsiPolyadicExpression (2 + 2 + 2)
// todo: support '2 + 2 .var' (with spacing)

public final class PostfixTemplatesManager implements ApplicationComponent {
  @NotNull private final List<TemplateProviderInfo> myProviders;

  public PostfixTemplatesManager(@NotNull PostfixTemplateProvider[] providers) {
    myProviders = new ArrayList<TemplateProviderInfo>();

    for (PostfixTemplateProvider provider : providers) {
      TemplateProvider annotation = provider.getClass().getAnnotation(TemplateProvider.class);
      if (annotation != null) {
        myProviders.add(new TemplateProviderInfo(provider, annotation));
      }
    }
  }

  private static class TemplateProviderInfo {
    @NotNull public final PostfixTemplateProvider provider;
    @NotNull public final TemplateProvider annotation;

    public TemplateProviderInfo(
        @NotNull PostfixTemplateProvider provider, @NotNull TemplateProvider annotation) {
      this.provider = provider;
      this.annotation = annotation;
    }
  }

  @Nullable public final PostfixTemplateContext isAvailable(
      @NotNull PsiElement positionElement, @NotNull PostfixExecutionContext executionContext) {

    // postfix name always is identifier
    if (!(positionElement instanceof PsiIdentifier)) return null;

    final PsiElement parent = positionElement.getParent();
    if (parent instanceof PsiReferenceExpression) {
      final PsiReferenceExpression reference = (PsiReferenceExpression) parent;

      // easy case: 'expr.postfix'
      PsiExpression qualifier = reference.getQualifierExpression();
      if (qualifier != null) {
        return new PostfixTemplateContext(reference, qualifier, executionContext) {
          @Override @NotNull
          public PrefixExpressionContext fixExpression(@NotNull PrefixExpressionContext context) {
            PsiExpression expression = (PsiExpression) context.expression;
            PsiReferenceExpression referenceExpression = (PsiReferenceExpression) postfixReference;

            // replace 'expr.postfix' with 'expr'
            if (expression.getParent() == referenceExpression) {
              PsiExpression newExpression = (PsiExpression) referenceExpression.replace(expression);
              return new PrefixExpressionContext(this, newExpression);
            }

            // replace '0 > expr.postfix' with '0 > expr'
            if (PsiTreeUtil.findCommonParent(referenceExpression, expression) == expression) {
              PsiElement expr = referenceExpression.getQualifier();
              assert expr != null : "expr != null";

              referenceExpression.replace(expr);
              return context; // yes, the same context
            }

            return context;
          }
        };
      }

      // cases like 'x > 0.if' and '2.var + 2' (two expression-statement and broken literal)
      if (reference.getFirstChild() instanceof PsiReferenceParameterList &&
          reference.getLastChild() == positionElement) {
        // find enclosing expression-statement through expressions
        final PsiExpressionStatement exprStatement = findContainingExprStatement(reference.getParent());
        if (exprStatement != null) {
          final PsiStatement lhsStatement = PsiTreeUtil.getPrevSiblingOfType(exprStatement, PsiStatement.class);
          final PsiExpression lhsExpression = findUnfinishedExpression(lhsStatement);
          final PsiLiteralExpression brokenLiteral = findBrokenLiteral(lhsExpression);
          if (lhsExpression != null && brokenLiteral != null) {
            final boolean isComplexRhs = !(reference.getParent() instanceof PsiExpressionStatement);
            return new PostfixTemplateContext(reference, brokenLiteral, executionContext) {
              @Override @NotNull
              public PrefixExpressionContext fixExpression(@NotNull PrefixExpressionContext context) {
                PsiExpression newExpression = fixCompletelyBrokenCase(
                  (PsiExpression) context.expression, brokenLiteral, reference, lhsExpression);
                removeStatementPreventingExtraTokens(exprStatement, lhsStatement);

                return new PrefixExpressionContext(this, newExpression);
              }

              @Nullable @Override
              public PsiStatement getContainingStatement(@NotNull PrefixExpressionContext expressionContext) {
                PsiStatement statement = super.getContainingStatement(expressionContext);
                if (statement != null) {
                  // ignore expression-statements produced by broken expr like '2.var + 2'
                  assert lhsStatement.isValid() : "lhsStatement.isValid()";
                  if (isComplexRhs && (statement == lhsStatement)) return null;
                }

                return statement;
              }
            };
          }
        }
      }
    } else if (parent instanceof PsiJavaCodeReferenceElement) {
      // check for qualifier of '.postfix' parsed as code-reference-element
      final PsiElement qualifier = ((PsiJavaCodeReferenceElement) parent).getQualifier();
      if (!(qualifier instanceof PsiJavaCodeReferenceElement)) return null;


      PsiElement referenceParent = parent.getParent();
      if (referenceParent instanceof PsiTypeElement) {
        final PsiElement psiElement = referenceParent.getParent();

        // todo: drop this in favor of more general PsiJavaCodeReferenceElement handling?
        // handle 'foo instanceof Bar.postfix' expressions
        if (psiElement instanceof PsiInstanceOfExpression) {
          PsiJavaCodeReferenceElement reference = (PsiJavaCodeReferenceElement) parent;
          PsiExpression instanceOfExpression = (PsiInstanceOfExpression) psiElement;

          return new PostfixTemplateContext(reference, instanceOfExpression, executionContext) {
            @NotNull @Override
            public PrefixExpressionContext fixExpression(@NotNull PrefixExpressionContext context) {
              parent.replace(qualifier);
              assert context.expression.isValid();
              return context;
            }
          };
        }

        // handle 'Bar<T>.postfix' type expressions (parsed as declaration-statement)
        if (psiElement instanceof PsiDeclarationStatement &&
            psiElement.getLastChild() instanceof PsiErrorElement && // only simple
            psiElement.getFirstChild() == referenceParent) {
          // copy and fix type usage from '.postfix' suffix
          PsiTypeElement refParentCopy = (PsiTypeElement) referenceParent.copy();
          PsiJavaCodeReferenceElement referenceElement = refParentCopy.getInnermostComponentReferenceElement();
          assert referenceElement != null : "referenceElement != null";

          PsiElement referenceQualifier = referenceElement.getQualifier();
          assert referenceQualifier != null : "referenceQualifier != null";
          referenceElement.replace(referenceQualifier); // remove '.postfix'

          // reinterpret type usages as 'new T();' expression-statement
          // todo: do not do this?
          PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiElement.getProject());
          final PsiExpressionStatement statement = (PsiExpressionStatement)
            factory.createStatementFromText("new " + refParentCopy.getText() + "()", psiElement);

          PsiJavaCodeReferenceElement reference = (PsiJavaCodeReferenceElement) parent;
          return new PostfixTemplateContext(reference, statement.getExpression(), executionContext) {
            @NotNull @Override
            public PrefixExpressionContext fixExpression(@NotNull PrefixExpressionContext context) {
              PsiExpressionStatement newStatement = (PsiExpressionStatement) psiElement.replace(statement);
              return new PrefixExpressionContext(this, newStatement.getExpression());
            }

            // todo: override referencedElement
            @Override public boolean isFakeContextFromType() { return true; }
          };
        }
      }
    }

    return null;
  }

  @NotNull private static PsiExpression fixCompletelyBrokenCase(
    @NotNull PsiExpression expressionToFix, @NotNull PsiLiteralExpression brokenLiteral,
    @NotNull PsiReferenceExpression reference, @NotNull PsiExpression lhsExpression) {

    // fix broken double literal by cutting of "." suffix
    Project project = expressionToFix.getProject();
    String literalText = brokenLiteral.getText();
    int dotIndex = literalText.lastIndexOf('.');
    assert dotIndex >= 0 : "dotIndex >= 0";

    String fixedText = literalText.substring(0, dotIndex);
    PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
    PsiLiteralExpression newLiteral = (PsiLiteralExpression)
      factory.createExpressionFromText(fixedText, null);

    // 'int a = 2.|var + 3;' => 'int a = 2.|2 + 3;'
    PsiExpression newExpression, oldExpression;
    if (lhsExpression == brokenLiteral) {
      oldExpression = brokenLiteral;
      newExpression = (PsiExpression) reference.replace(newLiteral);
    } else { // 'int a = 1 + 2.|var + 3;' => 'int a = 1 + 2.|1 + 2 + 3;'
      brokenLiteral.replace(newLiteral);
      oldExpression = lhsExpression;
      newExpression = (PsiExpression) reference.replace(lhsExpression.copy());
    }

    assert newExpression.isPhysical() : "newExpression.isPhysical()";
    assert oldExpression.isPhysical() : "oldExpression.isPhysical()";

    // 'int a = 1 + 2.|1 + 2 + 3;' => 'int a = 1 + 2 + 3;'
    PsiStatement statement = PsiTreeUtil.getParentOfType(newExpression, PsiStatement.class);
    if (statement instanceof PsiExpressionStatement) {
      newExpression.putCopyableUserData(marker, marker);

      PsiExpression outerExpression = ((PsiExpressionStatement) statement).getExpression();
      newExpression = (PsiExpression) oldExpression.replace(outerExpression);

      PsiExpression marked = findMarkedExpression(newExpression);
      if (marked != null) newExpression = marked;
    } else { // 'int a = 1 + 2.|1 + 2;' => 'int a = 1 + 2;'
      newExpression = (PsiExpression) oldExpression.replace(newExpression);
    }

    return newExpression;
  }

  private void removeStatementPreventingExtraTokens(
      @NotNull PsiExpressionStatement statementToRemove, @NotNull PsiStatement targetStatement) {

    PsiElement target = targetStatement;
    if (targetStatement instanceof PsiDeclarationStatement) {
      for (PsiElement element : ((PsiDeclarationStatement) targetStatement).getDeclaredElements())
        if (element instanceof PsiLocalVariable) target = element;
    }

    PsiElement node = statementToRemove.getExpression();
    while ((node = node.getNextSibling()) != null) {
      target.addBefore(node.copy(), null);

      if (node instanceof PsiJavaToken && target instanceof PsiLocalVariable) {
        if (((PsiJavaToken) node).getTokenType() == JavaTokenType.SEMICOLON)
          target = target.getParent();
      }
    }

    statementToRemove.delete();
  }

  // todo: maybe fix prefix matcher?
  @Nullable private PsiLiteralExpression findBrokenLiteral(@Nullable PsiExpression expr) {
    if (expr == null) return null;

    PsiExpression expression = expr;
    do {
      // look for double literal broken by dot at end
      if (expression instanceof PsiLiteralExpression) {
        PsiJavaToken token = PsiTreeUtil.getChildOfType(expression, PsiJavaToken.class);
        if (token != null
            && token.getTokenType() == JavaTokenType.DOUBLE_LITERAL
            && token.getText().matches("^.*?\\.\\D*$")) // omfg
          return (PsiLiteralExpression) expression;
      }

      // skip current expression and look its last inner expression
      PsiElement last = expression.getLastChild();
      if (last instanceof PsiExpression) expression = (PsiExpression) last;
      else expression = PsiTreeUtil.getPrevSiblingOfType(last, PsiExpression.class);
    } while (expression != null);

    return null;
  }

  @Nullable private static PsiExpression findUnfinishedExpression(@Nullable PsiStatement statement) {
    if (statement == null) return null;

    PsiElement lastChild = statement.getLastChild();
    while (lastChild != null) {
      if (lastChild instanceof PsiErrorElement &&
          lastChild.getPrevSibling() instanceof PsiExpression) {
        return (PsiExpression) lastChild.getPrevSibling();
      }

      lastChild = lastChild.getLastChild();
    }

    return null;
  }

  @Nullable private static PsiExpressionStatement findContainingExprStatement(@NotNull PsiElement element) {
    while (element instanceof PsiBinaryExpression ||
           element instanceof PsiPolyadicExpression) {
      // todo: check with postfix/prefix expressions
      element = element.getParent();
    }

    if (element instanceof PsiExpressionStatement) {
      return (PsiExpressionStatement) element;
    }

    return null;
  }

  @Nullable private static PsiExpression findMarkedExpression(@NotNull PsiExpression expression) {
    if (expression.getCopyableUserData(marker) != null) return expression;

    for (PsiElement node = expression.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (node instanceof PsiExpression) {
        PsiExpression markedExpression = findMarkedExpression((PsiExpression) node);
        if (markedExpression != null) return markedExpression;
      }
    }

    return null;
  }

  static final com.intellij.openapi.util.Key marker = new Key(PostfixTemplatesManager.class.getName());

  @NotNull public List<LookupElement> collectTemplates(@NotNull PostfixTemplateContext context) {
    // disable all providers over package names
    PsiElement referencedElement = context.innerExpression.referencedElement;
    if (referencedElement instanceof PsiPackage) return Collections.emptyList();

    // check we invoked on type
    boolean invokedOnType = (referencedElement instanceof PsiClass);
    boolean insideCodeFragment = context.insideCodeFragment;
    List<LookupElement> elements = new ArrayList<LookupElement>();

    for (TemplateProviderInfo providerInfo : myProviders)
    {
      if (invokedOnType && !providerInfo.annotation.worksOnTypes()) continue;
      if (insideCodeFragment && !providerInfo.annotation.worksInsideFragments()) continue;

      try {
        providerInfo.provider.createItems(context, elements);
      } catch (Exception ex) {
        LOG.error(ex);
      }
    }

    return elements;
  }

  private static final Logger LOG = Logger.getInstance("#org.jetbrains.postfixCompletion");

  @Override public void initComponent() { }
  @Override public void disposeComponent() { }

  @NotNull @Override public String getComponentName() {
    return PostfixTemplatesManager.class.getName();
  }
}