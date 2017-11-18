/** $Id$ */
/*
 * Copyright 2004-2005 Alexey Efimov
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
package org.intellij.images.thumbnail.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.intellij.images.thumbnail.ThumbnailView;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Thumbnail view.
 *
 * @author <a href="mailto:aefimov.box@gmail.com">Alexey Efimov</a>
 */
final class ThumbnailViewImpl implements ThumbnailView {
    private static final Icon TOOL_WINDOW_ICON = IconLoader.getIcon("/org/intellij/images/icons/ThumbnailToolWindow.png");

    private final Project project;
    private final ToolWindow toolWindow;

    private boolean recursive = false;
    private VirtualFile root = null;

    public ThumbnailViewImpl(Project project) {
        this.project = project;

        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        toolWindow = windowManager.registerToolWindow(TOOLWINDOW_ID, new ThumbnailViewUI(this), ToolWindowAnchor.BOTTOM);
        toolWindow.setIcon(TOOL_WINDOW_ICON);
    }

    private ThumbnailViewUI getUI() {
        return ((ThumbnailViewUI)toolWindow.getComponent());
    }

    public void setRoot(@NotNull VirtualFile root) {
        this.root = root;
        updateUI();
    }

    @NotNull public VirtualFile getRoot() {
        return root;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
        updateUI();
    }

    public void setSelected(@NotNull VirtualFile file, boolean selected) {
        if (isVisible()) {
            getUI().setSelected(file, selected);
        }
    }

    public boolean isSelected(@NotNull VirtualFile file) {
        if (isVisible()) {
            return getUI().isSelected(file);
        }
        return false;
    }

    public @NotNull VirtualFile[] getSelection() {
        if (isVisible()) {
            return getUI().getSelection();
        }
        return VirtualFile.EMPTY_ARRAY;
    }

    public void scrollToSelection() {
        if (isVisible()) {
            if (!toolWindow.isActive()) {
                toolWindow.activate(new LazyScroller());
            } else {
                getUI().scrollToSelection();
            }
        }
    }

    public boolean isVisible() {
        return toolWindow.isAvailable();
    }

    public void activate() {
        if (isVisible() && !toolWindow.isActive()) {
            toolWindow.activate(null);
        }
    }

    public void setVisible(boolean visible) {
        toolWindow.setAvailable(visible, null);
        if (visible) {
            getUI().refresh();
        } else {
            getUI().dispose();
        }
    }

    private void updateUI() {
        if (isVisible()) {
            getUI().refresh();
        }
    }

    @NotNull public Project getProject() {
        return project;
    }

    public void setTransparencyChessboardVisible(boolean visible) {
        if (isVisible()) {
            getUI().setTransparencyChessboardVisible(visible);
        }
    }

    public boolean isTransparencyChessboardVisible() {
        return isVisible() && getUI().isTransparencyChessboardVisible();
    }

    public void dispose() {
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        windowManager.unregisterToolWindow(TOOLWINDOW_ID);
    }

    private final class LazyScroller implements Runnable {
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    getUI().scrollToSelection();
                }
            });
        }
    }
}