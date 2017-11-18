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

package org.jetbrains.plugins.groovy;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author ilyas
 */
public interface GroovyIcons {

  public static final Icon FILE_TYPE = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/groovy_fileType.png");
  public static final Icon SMALLEST = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/groovy_16x16.png");
  public static final Icon BIG_ICON = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/groovy_32x32.png");
  public static final Icon CLASS = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/class.png");
  public static final Icon ABSTRACT_CLASS = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/abstractClass.png");
  public static final Icon INTERFACE = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/interface.png");
  public static final Icon ANNOTATION_TYPE = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/annotationType.png");
  public static final Icon ENUM = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/enum.png");
  public static final Icon PROPERTY = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/property.png");
  public static final Icon METHOD = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/method.png");
  public static final Icon DYNAMIC = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/dynamicProperty.png");
  public static final Icon DEF = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/def.png");
  public static final Icon FIELD = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/field.png");

  public static final Icon NO_GROOVY_SDK = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/no_groovy_sdk.png");
  public static final Icon GROOVY_SDK = IconLoader.findIcon("/org/jetbrains/plugins/groovy/images/groovy_sdk.png");

}