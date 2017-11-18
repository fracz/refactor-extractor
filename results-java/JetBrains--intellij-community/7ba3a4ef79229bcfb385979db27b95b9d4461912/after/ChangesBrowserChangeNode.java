/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.openapi.vcs.changes.ui;

import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;

import java.awt.*;
import java.io.File;

/**
 * @author yole
 */
public class ChangesBrowserChangeNode extends ChangesBrowserNode {
  protected ChangesBrowserChangeNode(Change userObject) {
    super(userObject);
    if (!ChangesUtil.getFilePath(userObject).isDirectory()) {
      myCount = 1;
    }
  }

  @Override
  protected boolean isDirectory() {
    return ChangesUtil.getFilePath((Change) userObject).isDirectory();
  }

  @Override
  public void render(final ChangesBrowserNodeRenderer renderer, final boolean selected, final boolean expanded, final boolean hasFocus) {
    final Change change = (Change)userObject;
    final FilePath filePath = ChangesUtil.getFilePath(change);
    final String fileName = filePath.getName();
    VirtualFile vFile = filePath.getVirtualFile();
    final Color changeColor = change.getFileStatus().getColor();
    renderer.appendFileName(vFile, fileName, changeColor);

    if (change.isRenamed() || change.isMoved()) {
      FilePath beforePath = change.getBeforeRevision().getFile();
      if (change.isRenamed()) {
        renderer.append(" - renamed from "+ beforePath.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      }
      else {
        renderer.append(" - moved from " + change.getMoveRelativePath(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      }
    }

    if (renderer.isShowFlatten()) {
      final File parentFile = filePath.getIOFile().getParentFile();
      if (parentFile != null) {
        renderer.append(" (" + parentFile.getPath() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
      }
    }
    else if (getCount() != 1 || getDirectoryCount() != 0) {
      appendCount(renderer);
    }

    if (filePath.isDirectory()) {
      renderer.setIcon(Icons.DIRECTORY_CLOSED_ICON);
    }
    else {
      renderer.setIcon(filePath.getFileType().getIcon());
    }
  }
}