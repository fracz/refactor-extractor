/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.intellij.util.xml;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import com.intellij.util.IncorrectOperationException;
import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.ide.IdeBundle;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

/**
 * @author peter
 */
public abstract class Converter<T> {
  public abstract @Nullable T fromString(@Nullable @NonNls String s, final ConvertContext context);
  public abstract @Nullable String toString(@Nullable T t, final ConvertContext context);

  public String getErrorMessage(@Nullable String s, final ConvertContext context) {
    return CodeInsightBundle.message("error.cannot.convert.default.message", s);
  }

  public static final Converter<Integer> INTEGER_CONVERTER = new Converter<Integer>() {
    public Integer fromString(final String s, final ConvertContext context) {
      if (s == null) return null;
      try {
        return Integer.decode(s);
      }
      catch (Exception e) {
        return null;
      }
    }

    public String toString(final Integer t, final ConvertContext context) {
      return t == null? null: t.toString();
    }

    public String getErrorMessage(final String s, final ConvertContext context) {
      return IdeBundle.message("value.should.be.integer");
    }
  };

  public static final Converter<String> EMPTY_CONVERTER = new Converter<String>() {
    public String fromString(final String s, final ConvertContext context) {
      return s;
    }

    public String toString(final String t, final ConvertContext context) {
      return t;
    }

  };

  public static final Converter<PsiClass> PSI_CLASS_CONVERTER = new Converter<PsiClass>() {
    public PsiClass fromString(final String s, final ConvertContext context) {
      return s == null? null:context.findClass(s, null);
    }

    public String toString(final PsiClass t, final ConvertContext context) {
      return t==null?null:t.getQualifiedName();
    }

  };

  public static final Converter<PsiType> PSI_TYPE_CONVERTER = new Converter<PsiType>() {
    public PsiType fromString(final String s, final ConvertContext context) {
      if (s == null) return null;
      try {
        return context.getFile().getManager().getElementFactory().createTypeFromText(s, null);
      }
      catch (IncorrectOperationException e) {
        return null;
      }
    }

    public String toString(final PsiType t, final ConvertContext context) {
      return t == null? null:t.getCanonicalText();
    }

  };

}