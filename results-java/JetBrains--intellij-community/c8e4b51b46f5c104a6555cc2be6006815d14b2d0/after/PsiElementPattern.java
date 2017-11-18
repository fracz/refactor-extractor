/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.patterns;

import com.intellij.lang.ASTNode;
import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.collection;
import static com.intellij.patterns.StandardPatterns.not;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public abstract class PsiElementPattern<T extends PsiElement,Self extends PsiElementPattern<T,Self>> extends TreeElementPattern<PsiElement,T,Self> {
  protected PsiElementPattern(final Class<T> aClass) {
    super(aClass);
  }

  protected PsiElementPattern(@NotNull final InitialPatternCondition<T> condition) {
    super(condition);
  }

  protected PsiElement[] getChildren(@NotNull final PsiElement element) {
    return element.getChildren();
  }

  protected PsiElement getParent(@NotNull final PsiElement element) {
    return element.getParent();
  }

  public Self withElementType(IElementType type) {
    return withElementType(PlatformPatterns.elementType().equalTo(type));
  }

  public Self withElementType(TokenSet type) {
    return withElementType(PlatformPatterns.elementType().tokenSet(type));
  }

  public Self afterLeaf(@NotNull final String withText) {
    return afterLeaf(psiElement().withText(withText));
  }

  public Self afterLeaf(@NotNull final ElementPattern<? extends PsiElement> pattern) {
    return afterLeafSkipping(psiElement().whitespaceCommentOrError(), pattern);
  }

  public Self whitespace() {
    return withElementType(TokenType.WHITE_SPACE);
  }

  public Self whitespaceCommentOrError() {
    return andOr(psiElement().whitespace(), psiElement(PsiComment.class), psiElement(PsiErrorElement.class));
  }

  public Self withFirstNonWhitespaceChild(@NotNull final ElementPattern pattern) {
    return withChildren(collection(PsiElement.class).filter(not(psiElement().whitespace()), collection(PsiElement.class).first(pattern)));
  }

  public Self equalTo(@NotNull final T o) {
    return with(new PatternCondition<T>() {
      public boolean accepts(@NotNull final T t, final MatchingContext matchingContext, @NotNull final TraverseContext traverseContext) {
        return t.getManager().areElementsEquivalent(t, o);
      }

      public String toString() {
        return "equalTo(" + o + ")";
      }
    });
  }

  public Self withElementType(final ElementPattern<IElementType> pattern) {
    return with(new PatternCondition<T>() {
      public boolean accepts(@NotNull final T t, final MatchingContext matchingContext, @NotNull final TraverseContext traverseContext) {
        final ASTNode node = t.getNode();
        return node != null && pattern.accepts(node.getElementType());
      }

      public String toString() {
        return "withElementType(" + pattern + ")";
      }
    });
  }

  public Self withText(@NotNull @NonNls final String text) {
    return withText(StandardPatterns.string().equalTo(text));
  }

  public Self withoutText(@NotNull final String text) {
    return withoutText(StandardPatterns.string().equalTo(text));
  }

  public Self withName(@NotNull @NonNls final String name) {
    return withName(StandardPatterns.string().equalTo(name));
  }

  public Self withName(@NotNull final ElementPattern<String> name) {
    return with(new PsiNamePatternCondition<T>(name));
  }

  public Self afterLeafSkipping(@NotNull final ElementPattern skip, @NotNull final ElementPattern pattern) {
    return with(new PatternCondition<T>() {
      public boolean accepts(@NotNull T t, final MatchingContext matchingContext, @NotNull final TraverseContext traverseContext) {
        PsiElement element = t;
        while (true) {
          final int offset = element.getTextRange().getStartOffset();
          if (offset == 0) return false;

          element = element.getContainingFile().findElementAt(offset - 1);
          if (element == null) return false;
          if (!skip.getCondition().accepts(element, matchingContext, traverseContext)) {
            return pattern.getCondition().accepts(element, matchingContext, traverseContext);
          }
        }
      }

      public String toString() {
        return "afterLeafSkipping(" + pattern.toString() + ")";
      }
    });
  }

  public Self withText(@NotNull final ElementPattern text) {
    return with(_withText(text));
  }

  private PatternCondition<T> _withText(final ElementPattern pattern) {
    return new PatternCondition<T>() {
      public boolean accepts(@NotNull final T t, final MatchingContext matchingContext, @NotNull final TraverseContext traverseContext) {
        return pattern.getCondition().accepts(t.getText(), matchingContext, traverseContext);
      }

      public String toString() {
        return "withText(" + pattern + ")";
      }
    };
  }

  public Self withoutText(@NotNull final ElementPattern text) {
    return without(_withText(text));
  }

  public static class Capture<T extends PsiElement> extends PsiElementPattern<T,Capture<T>> {

    protected Capture(final Class<T> aClass) {
      super(aClass);
    }

    protected Capture(@NotNull final InitialPatternCondition<T> condition) {
      super(condition);
    }


  }

  private class PsiNamePatternCondition<T extends PsiElement> extends PropertyPatternCondition<T, String> {
    private final ElementPattern<String> myName;

    public PsiNamePatternCondition(final ElementPattern<String> name) {
      super(name);
      myName = name;
    }

    protected String getPropertyValue(@NotNull final T t) {
      return t instanceof PsiNamedElement ? ((PsiNamedElement) t).getName() : null;
    }

    public String toString() {
      return "withName(" + myName + ")";
    }
  }
}