/*
 * Copyright (c) 2004 JetBrains s.r.o. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of JetBrains or IntelliJ IDEA
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. JETBRAINS AND ITS LICENSORS SHALL NOT
 * BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT
 * OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL JETBRAINS OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN
 * IF JETBRAINS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */
package com.intellij.ide.highlighter;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

public class GuiFormFileType implements FileType {
  @NonNls public static final String DEFAULT_EXTENSION = "form";
  @NonNls public static final String DOT_DEFAULT_EXTENSION = "." + DEFAULT_EXTENSION;

  @NotNull
  public String getName() {
    return "GUI_DESIGNER_FORM";
  }

  @NotNull
  public String getDescription() {
    return IdeBundle.message("filetype.description.gui.designer.form");
  }

  @NotNull
  public String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  public Icon getIcon() {
    return Icons.UI_FORM_ICON;
  }

  public boolean isBinary() {
    return false;
  }

  public boolean isReadOnly() {
    return false;
  }

  public String getCharset(@NotNull VirtualFile file, final byte[] content) {
    return CharsetToolkit.UTF8;
  }

  public StructureViewBuilder getStructureViewBuilder(@NotNull VirtualFile file, @NotNull Project project) {
    return null;
  }

}