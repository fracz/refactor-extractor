/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.util.xml;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.util.containers.BidirectionalMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author peter
 */
public class JvmPsiTypeConverterImpl extends JvmPsiTypeConverter implements CustomReferenceConverter<PsiType> {
  private static final BidirectionalMap<PsiType, Character> ourPrimitiveTypes = new BidirectionalMap<PsiType, Character>();
  static {
    ourPrimitiveTypes.put(PsiType.BYTE, 'B');
    ourPrimitiveTypes.put(PsiType.CHAR, 'C');
    ourPrimitiveTypes.put(PsiType.DOUBLE, 'D');
    ourPrimitiveTypes.put(PsiType.FLOAT, 'F');
    ourPrimitiveTypes.put(PsiType.INT, 'I');
    ourPrimitiveTypes.put(PsiType.LONG, 'L');
    ourPrimitiveTypes.put(PsiType.SHORT, 'S');
    ourPrimitiveTypes.put(PsiType.BOOLEAN, 'Z');
  }

  public PsiType fromString(final String s, final ConvertContext context) {
    return convertFromString(s, context);
  }

  @Nullable
  public static PsiType convertFromString(final String s, final ConvertContext context) {
    if (s == null) return null;

    if (s.startsWith("[")) {
      int arrayDimensions = getArrayDimensions(s);

      if (arrayDimensions >= s.length()) {
        return null;
      }

      final char c = s.charAt(arrayDimensions);
      if (c == 'L') {
        if (!s.endsWith(";")) return null;
          final PsiClass aClass = context.findClass(s.substring(arrayDimensions + 1, s.length() - 1), null);
          return aClass == null ? null : makeArray(arrayDimensions, createType(aClass));
      }

      if (s.length() == arrayDimensions + 1) {
        final List<PsiType> list = ourPrimitiveTypes.getKeysByValue(c);
        return list == null || list.isEmpty() ? null : makeArray(arrayDimensions, list.get(0));
      }

      return null;
    }

    final PsiClass aClass1 = context.findClass(s, null);
    return aClass1 == null ? null : createType(aClass1);
  }

  private static int getArrayDimensions(final String s) {
    int arrayDimensions = 0;

    while (arrayDimensions < s.length() && s.charAt(arrayDimensions) == '[') {
      arrayDimensions++;
    }
    return arrayDimensions;
  }

  private static PsiClassType createType(final PsiClass aClass) {
    return aClass.getManager().getElementFactory().createType(aClass);
  }

  private static PsiType makeArray(final int dimensions, final PsiType type) {
    return dimensions == 0 ? type : makeArray(dimensions - 1, new PsiArrayType(type));
  }

  public String toString(final PsiType psiType, final ConvertContext context) {
    return convertToString(psiType);
  }

  @Nullable
  public static String convertToString(final PsiType psiType) {
    if (psiType instanceof PsiArrayType) {
      return '[' + toStringArray(((PsiArrayType)psiType).getComponentType());
    }
    else if (psiType instanceof PsiClassType) {
      return psiType.getCanonicalText();
    }
    return null;
  }

  @NonNls @Nullable
  private static String toStringArray(final PsiType psiType) {
    if (psiType instanceof PsiArrayType) {
      return '[' + toStringArray(((PsiArrayType)psiType).getComponentType());
    }
    else if (psiType instanceof PsiPrimitiveType) {
      return String.valueOf(ourPrimitiveTypes.get(psiType));
    }
    else if (psiType instanceof PsiClassType) {
      return "L" + psiType.getCanonicalText() + ";";
    }
    return null;
  }

  @NotNull
  public PsiReference[] createReferences(GenericDomValue<PsiType> value, PsiElement element, ConvertContext context) {
    final PsiType psiType = value.getValue();
    final String s = value.getStringValue();
    assert s != null;
    final int dimensions = getArrayDimensions(s);
    if (dimensions > 0) {
      if (s.charAt(dimensions) == 'L' && s.endsWith(";")) {
        final JavaClassReferenceProvider provider = new JavaClassReferenceProvider(true);
        provider.setSoft(true);
        return provider.getReferencesByString(s.substring(dimensions + 1), element, JavaClassReferenceProvider.CLASS_REFERENCE_TYPE, element.getText().indexOf(s) + dimensions + 1);
      }
      if (psiType != null) return PsiReference.EMPTY_ARRAY;
    }
    final JavaClassReferenceProvider provider = new JavaClassReferenceProvider();
    provider.setSoft(true);
    return provider.getReferencesByElement(element);
  }
}