/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.patterns;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class StringPattern extends ObjectPattern<String, StringPattern> {
  protected StringPattern() {
    super(new InitialPatternCondition<String>(String.class) {
      public boolean accepts(@Nullable final Object o,
                                final MatchingContext matchingContext, @NotNull final TraverseContext traverseContext) {
        return o instanceof String;
      }

      public String toString() {
        return "string()";
      }
    });
  }

  @NotNull
  public StringPattern startsWith(@NonNls @NotNull final String s) {
    return with(new PatternCondition<String>() {
      public boolean accepts(@NotNull final String str, final MatchingContext matchingContext, @NotNull final TraverseContext traverseContext) {
        return str.startsWith(s);
      }
    });
  }

  @NotNull
  public StringPattern endsWith(@NonNls @NotNull final String s) {
    return with(new PatternCondition<String>() {
      public boolean accepts(@NotNull final String str, final MatchingContext matchingContext, @NotNull final TraverseContext traverseContext) {
        return str.endsWith(s);
      }
    });
  }
  @NotNull
  public StringPattern contains(@NonNls @NotNull final String s) {
    return with(new PatternCondition<String>() {
      public boolean accepts(@NotNull final String str, final MatchingContext matchingContext, @NotNull final TraverseContext traverseContext) {
        return str.contains(s);
      }

      public String toString() {
        return "contains(" + s + ")";
      }
    });
  }

  @NotNull
  public StringPattern contains(@NonNls @NotNull final ElementPattern pattern) {
    return with(new PatternCondition<String>() {
      public boolean accepts(@NotNull final String str, final MatchingContext matchingContext, @NotNull final TraverseContext traverseContext) {
        for (int i = 0; i < str.length(); i++) {
          if (pattern.accepts(str.charAt(i))) return true;
        }
        return false;
      }
    });
  }

  public StringPattern longerThan(final int minLength) {
    return with(new PatternCondition<String>() {
      public boolean accepts(@NotNull final String s,
                                final MatchingContext matchingContext, @NotNull final TraverseContext traverseContext) {
        return s.length() > minLength;
      }
    });
  }

  @NotNull
  public StringPattern oneOf(@NonNls final String... values) {
    return super.oneOf(values);
  }

  @NotNull
  public StringPattern oneOfIgnoreCase(@NonNls final String... values) {
    return with(new PatternCondition<String>() {
      public boolean accepts(@NotNull final String str, final MatchingContext matchingContext, @NotNull final TraverseContext traverseContext) {
        for (final String value : values) {
          if (str.equalsIgnoreCase(value)) return true;
        }
        return false;
      }

      public String toString() {
        return "oneOfIgnoreCase(" + Arrays.toString(values) + ")";
      }
    });

  }

  @NotNull
  public StringPattern oneOf(@NonNls final Collection<String> set) {
    return super.oneOf(set);
  }
}