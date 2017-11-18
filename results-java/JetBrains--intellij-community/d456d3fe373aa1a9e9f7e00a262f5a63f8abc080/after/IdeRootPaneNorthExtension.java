/*
 * User: anna
 * Date: 12-Nov-2007
 */
package com.intellij.openapi.wm.impl;

import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.extensions.ExtensionPointName;

import javax.swing.*;

public abstract class IdeRootPaneNorthExtension implements Disposable {
  public static final ExtensionPointName<IdeRootPaneNorthExtension> EP_NAME = ExtensionPointName.create("com.intellij.ideRootPaneNorth");

  public abstract String getKey();

  public abstract JComponent getComponent();

  public abstract void uiSettingsChanged(UISettings settings);
}