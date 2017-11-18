/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.refactoring;

import com.intellij.openapi.components.*;
import com.intellij.refactoring.RefactoringSettings;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author ilyas
 */

@State(
  name = "GroovyRefactoringSettings",
  storages = {
    @Storage(
      id ="groovy_config",
      file = "$APP_CONFIG$/groovy_config.xml"
    )}
)
public class GroovyRefactoringSettings implements PersistentStateComponent<GroovyRefactoringSettings> {

  public Boolean SPECIFY_TYPE_EXPLICITLY = null;
  public Boolean INTRODUCE_LOCAL_CREATE_FINALS = null;
  public Boolean EXTRACT_METHOD_SPECIFY_TYPE = null;
  public String EXTRACT_METHOD_VISIBILITY = null;

  public GroovyRefactoringSettings getState() {
    return this;
  }

  public void loadState(GroovyRefactoringSettings groovyRefactoringSettings) {
    XmlSerializerUtil.copyBean(groovyRefactoringSettings, this);
  }

  public static GroovyRefactoringSettings getInstance() {
    return ServiceManager.getService(GroovyRefactoringSettings.class);
  }
}