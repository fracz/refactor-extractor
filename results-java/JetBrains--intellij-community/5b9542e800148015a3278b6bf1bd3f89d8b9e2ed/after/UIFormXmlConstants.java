/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package com.intellij.uiDesigner;

/**
 * @author yole
 * @noinspection HardCodedStringLiteral
 */
public class UIFormXmlConstants {
  public static final String ATTRIBUTE_BIND_TO_CLASS = "bind-to-class";
  public static final String ATTRIBUTE_VALUE = "value";
  public static final String ATTRIBUTE_NOI18N = "noi18n";
  public static final String ATTRIBUTE_RESOURCE_BUNDLE = "resource-bundle";
  public static final String ATTRIBUTE_KEY = "key";
  public static final String ATTRIBUTE_TITLE = "title";
  public static final String ATTRIBUTE_TITLE_RESOURCE_BUNDLE = "title-resource-bundle";
  public static final String ATTRIBUTE_TITLE_KEY = "title-key";
  public static final String ATTRIBUTE_COLOR = "color";
  public static final String ATTRIBUTE_SWING_COLOR = "swing-color";
  public static final String ATTRIBUTE_SYSTEM_COLOR = "system-color";
  public static final String ATTRIBUTE_AWT_COLOR = "awt-color";
  public static final String ATTRIBUTE_NAME = "name";
  public static final String ATTRIBUTE_SIZE = "size";
  public static final String ATTRIBUTE_STYLE = "style";
  public static final String ATTRIBUTE_SWING_FONT = "swing-font";
  public static final String ATTRIBUTE_INDENT = "indent";
  public static final String ATTRIBUTE_ID = "id";
  public static final String ATTRIBUTE_FORM_FILE = "form-file";
  public static final String ATTRIBUTE_SAME_SIZE_HORIZONTALLY = "same-size-horizontally";
  public static final String ATTRIBUTE_SAME_SIZE_VERTICALLY = "same-size-vertically";
  public static final String ATTRIBUTE_USE_PARENT_LAYOUT = "use-parent-layout";

  public static final String ELEMENT_BUTTON_GROUPS = "buttonGroups";
  public static final String ELEMENT_GROUP = "group";
  public static final String ELEMENT_MEMBER = "member";
  public static final String ELEMENT_NESTED_FORM = "nested-form";
  public static final String ELEMENT_TOOLBAR = "toolbar";

  public static final String LAYOUT_INTELLIJ = "GridLayoutManager";
  public static final String LAYOUT_GRIDBAG = "GridBagLayout";
  public static final String LAYOUT_BORDER = "BorderLayout";

  private UIFormXmlConstants() {
  }
}