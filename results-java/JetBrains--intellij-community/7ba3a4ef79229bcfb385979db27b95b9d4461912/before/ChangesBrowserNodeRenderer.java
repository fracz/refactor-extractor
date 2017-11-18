package com.intellij.openapi.vcs.changes.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author max
 */
class ChangesBrowserNodeRenderer extends ColoredTreeCellRenderer {
  private final boolean myShowFlatten;
  private ChangeListDecorator[] myDecorators;
  private WolfTheProblemSolver myProblemSolver;
  private ChangeListManager myChangeListManager;
  private final boolean myHighlightProblems;

  public ChangesBrowserNodeRenderer(final Project project, final boolean showFlatten, final boolean highlightProblems) {
    myShowFlatten = showFlatten;
    myDecorators = project.getComponents(ChangeListDecorator.class);
    myProblemSolver = WolfTheProblemSolver.getInstance(project);
    myChangeListManager = ChangeListManager.getInstance(project);
    myHighlightProblems = highlightProblems;
  }

  public void customizeCellRenderer(JTree tree,
                                    Object value,
                                    boolean selected,
                                    boolean expanded,
                                    boolean leaf,
                                    int row,
                                    boolean hasFocus) {
    ChangesBrowserNode node = (ChangesBrowserNode)value;
    Object object = node.getUserObject();
    if (object instanceof ChangeList) {
      if (object instanceof LocalChangeList) {
        final LocalChangeList list = ((LocalChangeList)object);
        append(list.getName(),
               list.isDefault() ? SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES : SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
        appendCount(node);
        for(ChangeListDecorator decorator: myDecorators) {
          decorator.decorateChangeList(list, this, selected, expanded, hasFocus);
        }
        if (list.isInUpdate()) {
          append(" " + VcsBundle.message("changes.nodetitle.updating"), SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }
      }
      else {
        final ChangeList list = ((ChangeList)object);
        append(list.getName(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
        appendCount(node);
      }
    }
    else if (object instanceof Change) {
      final Change change = (Change)object;
      final FilePath filePath = ChangesUtil.getFilePath(change);
      final String fileName = filePath.getName();
      VirtualFile vFile = filePath.getVirtualFile();
      final Color changeColor = getColor(change);
      appendFileName(vFile, fileName, changeColor);

      if (change.isRenamed() || change.isMoved()) {
        FilePath beforePath = change.getBeforeRevision().getFile();
        if (change.isRenamed()) {
          append(" - renamed from "+ beforePath.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
        else {
          append(" - moved from " + change.getMoveRelativePath(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
      }

      if (myShowFlatten) {
        final File parentFile = filePath.getIOFile().getParentFile();
        if (parentFile != null) {
          append(" (" + parentFile.getPath() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }
      }
      else if (node.getCount() != 1 || node.getDirectoryCount() != 0) {
        appendCount(node);
      }

      if (filePath.isDirectory()) {
        setIcon(Icons.DIRECTORY_CLOSED_ICON);
      }
      else {
        setIcon(filePath.getFileType().getIcon());
      }
    }
    else if (object instanceof VirtualFile) {
      final VirtualFile file = (VirtualFile)object;
      appendFileName(file, file.getName(), myChangeListManager.getStatus(file).getColor());
      if (myShowFlatten && file.isValid()) {
        final VirtualFile parentFile = file.getParent();
        assert parentFile != null;
        append(" (" + parentFile.getPresentableUrl() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
      }
      else if (node.getCount() != 1 || node.getDirectoryCount() != 0) {
        appendCount(node);
      }
      if (file.isDirectory()) {
        setIcon(Icons.DIRECTORY_CLOSED_ICON);
      }
      else {
        setIcon(file.getFileType().getIcon());
      }
    }
    else if (object instanceof FilePath) {
      final FilePath path = (FilePath)object;
      if (path.isDirectory() || !node.isLeaf()) {
        append(ChangesListView.getRelativePath(ChangesListView.safeCastToFilePath(((ChangesBrowserNode)node.getParent()).getUserObject()), path),
               SimpleTextAttributes.REGULAR_ATTRIBUTES);
        if (!node.isLeaf()) {
          appendCount(node);
        }
        setIcon(expanded ? Icons.DIRECTORY_OPEN_ICON : Icons.DIRECTORY_CLOSED_ICON);
      }
      else {
        if (myShowFlatten) {
          append(path.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
          final FilePath parent = path.getParentPath();
          append(" (" + parent.getPresentableUrl() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }
        else {
          append(ChangesListView.getRelativePath(ChangesListView.safeCastToFilePath(((ChangesBrowserNode)node.getParent()).getUserObject()), path),
                 SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
        setIcon(path.getFileType().getIcon());
      }
    }
    else if (object instanceof Module) {
      final Module module = (Module)object;

      append(module.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      appendCount(node);
      setIcon(module.getModuleType().getNodeIcon(expanded));
    }
    else {
      append(object.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      appendCount(node);
    }
  }

  private void appendFileName(final VirtualFile vFile, final String fileName, final Color color) {
    int style = SimpleTextAttributes.STYLE_PLAIN;
    Color underlineColor = null;
    if (myHighlightProblems && vFile != null && !vFile.isDirectory() && myProblemSolver.isProblemFile(vFile)) {
      underlineColor = Color.red;
      style = SimpleTextAttributes.STYLE_WAVED;
    }
    append(fileName, new SimpleTextAttributes(style, color, underlineColor));
  }

  private void appendCount(final ChangesBrowserNode node) {
    int count = node.getCount();
    int dirCount = node.getDirectoryCount();
    if (dirCount == 0) {
      append(" " + VcsBundle.message("changes.nodetitle.changecount", count), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
    }
    else if (count == 0 && dirCount > 0) {
      append(" " + VcsBundle.message("changes.nodetitle.directory.changecount", dirCount), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
    }
    else {
      append(" " + VcsBundle.message("changes.nodetitle.directory.file.changecount", dirCount, count), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
    }
  }

  private static Color getColor(final Change change) {
    return change.getFileStatus().getColor();
  }
}