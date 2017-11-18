package com.intellij.lang.ant.psi.impl.reference;

import com.intellij.lang.ant.psi.AntElement;
import com.intellij.lang.ant.psi.impl.AntCallImpl;
import com.intellij.lang.ant.psi.impl.AntProjectImpl;
import com.intellij.lang.ant.psi.impl.AntPropertyImpl;
import com.intellij.lang.ant.psi.impl.AntTargetImpl;
import com.intellij.lang.ant.psi.impl.reference.providers.AntPropertyFileReferenceProvider;
import com.intellij.lang.ant.psi.impl.reference.providers.AntSingleTargetReferenceProvider;
import com.intellij.lang.ant.psi.impl.reference.providers.AntTargetListReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.GenericReferenceProvider;
import com.intellij.util.containers.HashMap;

import java.util.Map;

public class AntReferenceProvidersRegistry {

  private static final GenericReferenceProvider[] EMPTY_ARRAY = new GenericReferenceProvider[0];
  private static final Map<Class, GenericReferenceProvider[]> ourProviders;

  static {
    ourProviders = new HashMap<Class, GenericReferenceProvider[]>();
    ourProviders.put(AntProjectImpl.class, new GenericReferenceProvider[]{new AntSingleTargetReferenceProvider()});
    ourProviders.put(AntTargetImpl.class, new GenericReferenceProvider[]{new AntTargetListReferenceProvider()});
    ourProviders.put(AntCallImpl.class, new GenericReferenceProvider[]{new AntSingleTargetReferenceProvider()});
    ourProviders.put(AntPropertyImpl.class, new GenericReferenceProvider[]{new AntPropertyFileReferenceProvider()});
  }

  private AntReferenceProvidersRegistry() {
  }

  public static GenericReferenceProvider[] getProvidersByElement(final AntElement element) {
    GenericReferenceProvider[] result = ourProviders.get(element.getClass());
    return (result != null) ? result : EMPTY_ARRAY;
  }
}