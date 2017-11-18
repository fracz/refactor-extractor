/*
 * User: anna
 * Date: 14-Feb-2008
 */
package com.intellij.application.options.editor;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.options.SearchableConfigurable;

public interface EditorOptionsProvider extends SearchableConfigurable {
  ExtensionPointName<EditorOptionsProvider> EP_NAME = ExtensionPointName.create("com.intellij.editorOptionsProvider");
}