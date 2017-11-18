/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.util.xml.impl;

import com.intellij.javaee.web.PsiReferenceConverter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.xml.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.ReflectionCache;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author peter
 */
public class GenericValueReferenceProvider extends PsiReferenceProvider {

  private final static Logger LOG = Logger.getInstance("#com.intellij.util.xml.impl.GenericValueReferenceProvider");

  private final Map<Class, PsiReferenceFactory> myProviders = new HashMap<Class, PsiReferenceFactory>();

  public void addReferenceProviderForClass(Class clazz, PsiReferenceFactory provider) {
    myProviders.put(clazz, provider);
  }

  static boolean hasInjections(PsiLanguageInjectionHost host) {
    final Ref<Boolean> result = Ref.create(false);
    host.processInjectedPsi(new PsiLanguageInjectionHost.InjectedPsiVisitor() {
      public void visit(@NotNull final PsiFile injectedPsi, @NotNull final List<PsiLanguageInjectionHost.Shred> places) {
        result.set(true);
      }
    });
    return result.get().booleanValue();
  }

  @NotNull
  public final PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull final ProcessingContext context) {
    final DomManager domManager = DomManager.getDomManager(psiElement.getProject());

    final DomElement domElement;
    if (psiElement instanceof XmlTag) {
      domElement = domManager.getDomElement((XmlTag)psiElement);
    } else if (psiElement instanceof XmlAttributeValue && psiElement.getParent() instanceof XmlAttribute) {
      domElement = domManager.getDomElement((XmlAttribute)psiElement.getParent());
    } else {
      return PsiReference.EMPTY_ARRAY;
    }

    if (!(domElement instanceof GenericDomValue)) {
      return PsiReference.EMPTY_ARRAY;
    }

    if (psiElement instanceof XmlTag) {
      for (XmlText text : ((XmlTag)psiElement).getValue().getTextElements()) {
        if (hasInjections((PsiLanguageInjectionHost)text)) return PsiReference.EMPTY_ARRAY;
      }
    } else {
      if (hasInjections((PsiLanguageInjectionHost)psiElement)) return PsiReference.EMPTY_ARRAY;
    }

    final GenericDomValue domValue = (GenericDomValue)domElement;

    final Referencing referencing = domValue.getAnnotation(Referencing.class);
    final Object converter;
    if (referencing == null) {
      converter = WrappingConverter.getDeepestConverter(domValue.getConverter(), domValue);
    }
    else {
      Class<? extends CustomReferenceConverter> clazz = referencing.value();
      converter = ((ConverterManagerImpl)domManager.getConverterManager()).getInstance(clazz);
    }
    PsiReference[] references = createReferences(domValue, (XmlElement)psiElement, converter);
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      for (PsiReference reference : references) {
        if (!reference.isSoft()) {
          LOG.error("dom reference should be soft: " + reference + " (created by " + converter + ")");
        }
      }
    }
    // creating "declaration" reference
    if (references.length == 0) {
      final NameValue nameValue = domElement.getAnnotation(NameValue.class);
      if (nameValue != null && nameValue.referencable()) {
        DomElement parent = domElement.getParent();
        assert parent != null;
        references = ArrayUtil.append(references, PsiReferenceBase.createSelfReference(psiElement, parent.getXmlElement()), PsiReference.class);
      }
    }
    return references;
  }

  private static AbstractConvertContext createConvertContext(final PsiElement psiElement, final GenericDomValue domValue) {
    return new AbstractConvertContext() {
      @NotNull
      public DomElement getInvocationElement() {
        return domValue;
      }

      public PsiManager getPsiManager() {
        return psiElement.getManager();
      }
    };
  }

  @Nullable
  private static DomInvocationHandler getInvocationHandler(final GenericDomValue domValue) {
    return DomManagerImpl.getDomInvocationHandler(domValue);
  }

  @NotNull
  private PsiReference[] createReferences(final GenericDomValue domValue, final XmlElement psiElement, final Object converter) {
    if (converter instanceof CustomReferenceConverter) {
      final PsiReference[] references =
        ((CustomReferenceConverter)converter).createReferences(domValue, psiElement, createConvertContext(psiElement, domValue));

      if (references.length == 0 && converter instanceof ResolvingConverter) {
        return new PsiReference[]{new GenericDomValueReference(domValue)};
      } else {
        return references;
      }
    }
    if (converter instanceof PsiReferenceConverter) {
      return ((PsiReferenceConverter)converter).createReferences(psiElement, true);
    }
    if (converter instanceof ResolvingConverter) {
      return new PsiReference[]{new GenericDomValueReference(domValue)};
    }

    final DomInvocationHandler invocationHandler = getInvocationHandler(domValue);
    assert invocationHandler != null;
    final Class clazz = DomUtil.getGenericValueParameter(invocationHandler.getDomElementType());
    if (clazz == null) return PsiReference.EMPTY_ARRAY;

    if (ReflectionCache.isAssignable(Integer.class, clazz)) {
      return new PsiReference[]{new GenericDomValueReference<Integer>((GenericDomValue<Integer>)domValue) {
        public Object[] getVariants() {
          return new Object[]{"0"};
        }
      }};
    }
    if (ReflectionCache.isAssignable(String.class, clazz)) {
      return PsiReference.EMPTY_ARRAY;
    }
    PsiReferenceFactory provider = myProviders.get(clazz);
    if (provider != null) {
      return provider.getReferencesByElement(psiElement);
    }

    return PsiReference.EMPTY_ARRAY;
  }

}