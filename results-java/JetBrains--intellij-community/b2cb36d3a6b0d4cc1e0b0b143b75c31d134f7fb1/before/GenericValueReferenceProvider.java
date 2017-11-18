/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.util.xml.impl;

import com.intellij.javaee.web.PsiReferenceConverter;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.ReferenceType;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ReflectionCache;
import com.intellij.util.xml.*;
import com.intellij.util.xml.reflect.DomAttributeChildDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author peter
 */
public class GenericValueReferenceProvider implements PsiReferenceProvider {

  private final Map<Class, PsiReferenceFactory> myProviders = new HashMap<Class, PsiReferenceFactory>();

  public void addReferenceProviderForClass(Class clazz, PsiReferenceFactory provider) {
    myProviders.put(clazz, provider);
  }

  @NotNull
  public final PsiReference[] getReferencesByElement(PsiElement psiElement) {
    if (!(psiElement instanceof XmlTag || psiElement instanceof XmlAttributeValue)) {
      return PsiReference.EMPTY_ARRAY;
    }

    PsiElement originalElement = psiElement.getUserData(PsiUtil.ORIGINAL_KEY);
    if (originalElement != null) {
      psiElement = originalElement;
    }

    final XmlTag tag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class, false);

    PsiManager psiManager = psiElement.getManager();
    final DomManager domManager = DomManager.getDomManager(psiManager.getProject());
    DomElement domElement = domManager.getDomElement(tag);
    if (domElement == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    if (psiElement instanceof XmlAttributeValue) {
      final PsiElement parent = psiElement.getParent();
      if (parent instanceof XmlAttribute) {
        final XmlAttribute attribute = (XmlAttribute)parent;
        final String name = attribute.getLocalName();
        final DomAttributeChildDescription childDescription = domElement.getGenericInfo().getAttributeChildDescription(name);
        if (childDescription != null) {
          domElement = childDescription.getDomAttributeValue(domElement);
        }
      }
    }

    if (!(domElement instanceof GenericDomValue)) {
      return PsiReference.EMPTY_ARRAY;
    }

    GenericDomValue domValue = (GenericDomValue)domElement;

    final Referencing referencing = domValue.getAnnotation(Referencing.class);
    final Object converter;
    if (referencing == null) {
      converter = domValue.getConverter();
    }
    else {
      Class<? extends CustomReferenceConverter> clazz = referencing.value();
      converter = ((ConverterManagerImpl)domManager.getConverterManager()).getInstance(clazz);
    }
    PsiReference[] references = createReferences(domValue, (XmlElement)psiElement, converter);

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

  @Nullable
  private static DomInvocationHandler getInvocationHandler(final GenericDomValue domValue) {
    return DomManagerImpl.getDomInvocationHandler(domValue);
  }

  @NotNull
  private PsiReference[] createReferences(final GenericDomValue domValue, final XmlElement psiElement, final Object converter) {
    if (converter instanceof CustomReferenceConverter) {
      return ((CustomReferenceConverter)converter).createReferences(domValue, psiElement, new AbstractConvertContext() {
        @NotNull
        public DomElement getInvocationElement() {
          return domValue;
        }

        public PsiManager getPsiManager() {
          return psiElement.getManager();
        }
      });
    }
    if (converter instanceof PsiReferenceConverter) {
      return ((PsiReferenceConverter)converter).createReferences(psiElement, true);
    }
    if (converter instanceof ResolvingConverter) {
      return new PsiReference[] {new GenericDomValueReference(domValue)};
    }

    final DomInvocationHandler invocationHandler = getInvocationHandler(domValue);
    assert invocationHandler != null;
    final Class clazz = DomUtil.getGenericValueParameter(invocationHandler.getDomElementType());
    if (clazz == null) return PsiReference.EMPTY_ARRAY;

    if (ReflectionCache.isAssignable(PsiType.class, clazz)) {
      return new PsiReference[]{new PsiTypeReference((GenericDomValue<PsiType>)domValue)};
    }
    if (ReflectionCache.isAssignable(PsiClass.class, clazz)) {
      ExtendClass extendClass = invocationHandler.getAnnotation(ExtendClass.class);
      final JavaClassReferenceProvider provider;
      if (extendClass == null) {
        provider = new JavaClassReferenceProvider(getScope(domValue));
      }
      else {
        provider = new JavaClassReferenceProvider(extendClass.value(), extendClass.instantiatable(), getScope(domValue));
      }
      provider.setSoft(true);
      return provider.getReferencesByElement(psiElement);
    }
    if (ReflectionCache.isAssignable(PsiPackage.class, clazz)) {
      final JavaClassReferenceProvider provider = new JavaClassReferenceProvider(getScope(domValue));
      provider.setSoft(true);
      return provider.getReferencesByElement(psiElement, new ReferenceType(ReferenceType.JAVA_PACKAGE));
    }
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

    return new PsiReference[]{new GenericDomValueReference(domValue)};
  }

  @Nullable
  private static GlobalSearchScope getScope(final GenericDomValue domValue) {
    final Module module = domValue.getModule();
    return module == null ? null : GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
  }

  @NotNull
  public final PsiReference[] getReferencesByElement(PsiElement element, ReferenceType type) {
    return getReferencesByElement(element);
  }

  @NotNull
  public final PsiReference[] getReferencesByString(String str, PsiElement position, ReferenceType type, int offsetInPosition) {
    return getReferencesByElement(position);
  }

  public void handleEmptyContext(PsiScopeProcessor processor, PsiElement position) {
  }
}