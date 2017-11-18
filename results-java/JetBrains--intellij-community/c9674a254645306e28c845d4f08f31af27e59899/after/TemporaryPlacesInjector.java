/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.intellij.plugins.intelliLang.inject;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.ElementManipulators;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Trinity;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class TemporaryPlacesInjector implements MultiHostInjector {

  @NotNull
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(PsiLanguageInjectionHost.class);
  }

  public void getLanguagesToInject(@NotNull final MultiHostRegistrar registrar, @NotNull final PsiElement context) {
    final List<Pair<SmartPsiElementPointer<PsiLanguageInjectionHost>,InjectedLanguage>> list =
      TemporaryPlacesRegistry.getInstance(context.getProject()).getTempInjectionsSafe();
    for (final Pair<SmartPsiElementPointer<PsiLanguageInjectionHost>, InjectedLanguage> pair : list) {
      if (pair.first.getElement() == context) {
        final PsiLanguageInjectionHost host = (PsiLanguageInjectionHost)context;
        XmlLanguageInjector.registerInjection(pair.second.getLanguage(), Collections.singletonList(
          Trinity.create(host, pair.second, ElementManipulators.getManipulator(host).getRangeInElement(host))), context.getContainingFile(), registrar);
        return;
      }
    }
  }

}