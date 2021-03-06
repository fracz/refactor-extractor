/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.groovy.mvc.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import icons.JetgroovyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;

/**
 * @author Dmitry Krasilschikov
 */
public class DomainClassNode extends ClassNode {
  public DomainClassNode(@NotNull final Module module,
                         @NotNull final GrTypeDefinition typeDefinition,
                         @Nullable final ViewSettings viewSettings) {
    super(module, typeDefinition, viewSettings);
  }

  @Override
  protected String getTestPresentationImpl(@NotNull final NodeId nodeId, @NotNull final PsiElement psiElement) {
    return "Domain class: " + ((GrTypeDefinition)psiElement).getName();
  }

  @Override
  protected void updateImpl(final PresentationData data) {
    super.updateImpl(data);
    data.setIcon(JetgroovyIcons.Mvc.Domain_class);
  }

  @Override
  public boolean validate() {
    if (!super.validate()) {
      return false;
    }
    return getValue() != null;
  }

}