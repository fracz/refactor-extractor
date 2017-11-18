/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Chris Magnussen and Elior Boukhobza
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

package com.chrisrm.idea.tree;

import com.chrisrm.idea.MTConfig;
import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.ColoredTreeCellRenderer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eliorb on 09/04/2017.
 */
public final class MTProjectViewNodeDecorator implements ProjectViewNodeDecorator {

  private final Map<FileStatus, Color> fileStatusColorMap;

  public MTProjectViewNodeDecorator() {
    fileStatusColorMap = new HashMap<>(18);
    // TODO move into a properties file ?
    fileStatusColorMap.put(FileStatus.NOT_CHANGED_IMMEDIATE, ColorUtil.fromHex("#80CBC4"));
    fileStatusColorMap.put(FileStatus.NOT_CHANGED_RECURSIVE, ColorUtil.fromHex("#80CBC4"));
    fileStatusColorMap.put(FileStatus.DELETED, ColorUtil.fromHex("#F77669"));
    fileStatusColorMap.put(FileStatus.MODIFIED, ColorUtil.fromHex("#80CBC4"));
    fileStatusColorMap.put(FileStatus.ADDED, ColorUtil.fromHex("#C3E887"));
    fileStatusColorMap.put(FileStatus.MERGE, ColorUtil.fromHex("#C792EA"));
    fileStatusColorMap.put(FileStatus.UNKNOWN, ColorUtil.fromHex("#F77669"));
    fileStatusColorMap.put(FileStatus.IGNORED, ColorUtil.fromHex("#B0BEC5"));
    fileStatusColorMap.put(FileStatus.HIJACKED, ColorUtil.fromHex("#FFCB6B"));
    fileStatusColorMap.put(FileStatus.MERGED_WITH_CONFLICTS, ColorUtil.fromHex("#BC3F3C"));
    fileStatusColorMap.put(FileStatus.MERGED_WITH_BOTH_CONFLICTS, ColorUtil.fromHex("#BC3F3C"));
    fileStatusColorMap.put(FileStatus.MERGED_WITH_PROPERTY_CONFLICTS, ColorUtil.fromHex("#BC3F3C"));
    fileStatusColorMap.put(FileStatus.DELETED_FROM_FS, ColorUtil.fromHex("#626669"));
    fileStatusColorMap.put(FileStatus.SWITCHED, ColorUtil.fromHex("#F77669"));
    fileStatusColorMap.put(FileStatus.OBSOLETE, ColorUtil.fromHex("#FFCB6B"));
    fileStatusColorMap.put(FileStatus.SUPPRESSED, ColorUtil.fromHex("#3C3F41"));
  }

  @Override
  public void decorate(final PackageDependenciesNode node, final ColoredTreeCellRenderer cellRenderer) {

  }

  @Override
  public void decorate(final ProjectViewNode node, final PresentationData data) {
    if (!MTConfig.getInstance().isUseProjectViewDecorators()) {
      return;
    }

    final VirtualFile file = node.getVirtualFile();
    if (file == null) {
      return;
    }
    final Project project = node.getProject();

    // Color file status
    colorFileStatus(data, file, project);

    // Fix open/closed icons (TODO USE SETTING FOR THIS)
    setOpenOrClosedIcon(data, file, project);
  }

  /**
   * Try to mimic the "open or closed"  folder feature
   *
   */
  private void setOpenOrClosedIcon(final PresentationData data, final VirtualFile file, final Project project) {
    if (!file.isDirectory()) {
      return;
    }

    final FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(project);
    for (final EditorWindow editorWindow : manager.getWindows()) {
      final VirtualFile[] files = editorWindow.getFiles();
      for (final VirtualFile leaf : files) {
        if (leaf.getPath().contains(file.getPath())) {
          setDirectoryIcon(data, file, project);
        }
      }
    }
  }

  private void setDirectoryIcon(final PresentationData data, final VirtualFile file, final Project project) {
    if (ProjectRootManager.getInstance(project).getFileIndex().isExcluded(file)) {
      data.setIcon(IconLoader.findIcon("/icons/modules/ExcludedTreeOpen.png"));
    } else if (ProjectRootsUtil.isModuleContentRoot(file, project)) {
      data.setIcon(IconLoader.findIcon("/icons/nodes/ModuleOpen.png"));
    } else if (ProjectRootsUtil.isInSource(file, project)) {
      data.setIcon(IconLoader.findIcon("/icons/modules/sourceRootOpen.png"));
    } else if (ProjectRootsUtil.isInTestSource(file, project)) {
      data.setIcon(IconLoader.findIcon("/icons/modules/testRootOpen.png"));
    } else {
      data.setIcon(AllIcons.Nodes.TreeOpen);
    }
  }

  private void colorFileStatus(final PresentationData data, final VirtualFile file, final Project project) {
    final FileStatus status = FileStatusManager.getInstance(project).getStatus(file);
    final Color colorFromStatus = getColorFromStatus(status);
    if (colorFromStatus != null) {
      data.setForcedTextForeground(colorFromStatus);
    }
  }

  private Color getColorFromStatus(final FileStatus status) {
    return fileStatusColorMap.get(status);
  }
}