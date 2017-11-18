package com.intellij.openapi.vcs.changes.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.peer.PeerFactory;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.*;

/**
 * @author max
 */
public class TreeModelBuilder {
  @NonNls public static final String ROOT_NODE_VALUE = "root";

  private Project myProject;
  private boolean showFlatten;
  private DefaultTreeModel model;
  private ChangesBrowserNode root;

  public TreeModelBuilder(final Project project, final boolean showFlatten) {
    myProject = project;
    this.showFlatten = showFlatten;
    root = new ChangesBrowserNode(myProject, ROOT_NODE_VALUE);
    model = new DefaultTreeModel(root);
  }

  public DefaultTreeModel buildModel(final List<Change> changes) {
    final HashMap<FilePath, ChangesBrowserNode> foldersCache = new HashMap<FilePath, ChangesBrowserNode>();
    final HashMap<Module, ChangesBrowserNode> moduleCache = new HashMap<Module, ChangesBrowserNode>();
    for (Change change : changes) {
      insertChangeNode(change, foldersCache, moduleCache, root);
    }

    collapseDirectories(model, root);
    sortNodes();

    return model;
  }

  public DefaultTreeModel buildModel(final List<? extends ChangeList> changeLists,
                                     final List<VirtualFile> unversionedFiles,
                                     final List<FilePath> locallyDeletedFiles,
                                     final List<VirtualFile> modifiedWithoutEditing,
                                     @Nullable final List<VirtualFile> ignoredFiles) {

    for (ChangeList list : changeLists) {
      ChangesBrowserNode listNode = new ChangesBrowserNode(myProject, list);
      model.insertNodeInto(listNode, root, 0);
      final HashMap<FilePath, ChangesBrowserNode> foldersCache = new HashMap<FilePath, ChangesBrowserNode>();
      final HashMap<Module, ChangesBrowserNode> moduleCache = new HashMap<Module, ChangesBrowserNode>();
      for (Change change : list.getChanges()) {
        insertChangeNode(change, foldersCache, moduleCache, listNode);
      }
    }

    if (!modifiedWithoutEditing.isEmpty()) {
      buildVirtualFiles(modifiedWithoutEditing, ChangesListView.MODIFIED_WITHOUT_EDITING_TAG);
    }
    if (!unversionedFiles.isEmpty()) {
      buildVirtualFiles(unversionedFiles, ChangesListView.UNVERSIONED_FILES_TAG);
    }
    if (ignoredFiles != null && !ignoredFiles.isEmpty()) {
      buildVirtualFiles(ignoredFiles, ChangesListView.IGNORED_FILES_TAG);
    }

    if (!locallyDeletedFiles.isEmpty()) {
      ChangesBrowserNode locallyDeletedNode = new ChangesBrowserNode(myProject, VcsBundle.message("changes.nodetitle.locally.deleted.files"));
      model.insertNodeInto(locallyDeletedNode, root, root.getChildCount());
      final HashMap<FilePath, ChangesBrowserNode> foldersCache = new HashMap<FilePath, ChangesBrowserNode>();
      final HashMap<Module, ChangesBrowserNode> moduleCache = new HashMap<Module, ChangesBrowserNode>();
      for (FilePath file : locallyDeletedFiles) {
        final ChangesBrowserNode node = new ChangesBrowserNode(myProject, file);
        model.insertNodeInto(node, getParentNodeFor(node, foldersCache, moduleCache, locallyDeletedNode), 0);
      }
    }

    collapseDirectories(model, root);
    sortNodes();

    return model;
  }

  private void buildVirtualFiles(final List<VirtualFile> files, final Object tag) {
    ChangesBrowserNode filesNode = new ChangesBrowserNode(myProject, tag);
    model.insertNodeInto(filesNode, root, root.getChildCount());
    final HashMap<FilePath, ChangesBrowserNode> foldersCache = new HashMap<FilePath, ChangesBrowserNode>();
    final HashMap<Module, ChangesBrowserNode> moduleCache = new HashMap<Module, ChangesBrowserNode>();
    for (VirtualFile file : files) {
      insertChangeNode(file, foldersCache, moduleCache, filesNode);
    }
  }

  private void insertChangeNode(final Object change, final HashMap<FilePath, ChangesBrowserNode> foldersCache, final HashMap<Module, ChangesBrowserNode> moduleCache,
                                final ChangesBrowserNode listNode) {
    final FilePath nodePath = getPathForObject(change);
    nodePath.refresh();
    ChangesBrowserNode oldNode = foldersCache.get(nodePath);
    if (oldNode != null) {
      oldNode.setUserObject(change);
    }
    else {
      final ChangesBrowserNode node = new ChangesBrowserNode(myProject, change);
      model.insertNodeInto(node, getParentNodeFor(node, foldersCache, moduleCache, listNode), 0);
      foldersCache.put(nodePath, node);
    }
  }

  private void sortNodes() {
    TreeUtil.sort(model, new Comparator() {
      public int compare(final Object n1, final Object n2) {
        Object o1 = ((ChangesBrowserNode)n1).getUserObject();
        Object o2 = ((ChangesBrowserNode)n2).getUserObject();

        final int classdiff = getNodeClassWeight(o1) - getNodeClassWeight(o2);
        if (classdiff != 0) return classdiff;

        if (o1 instanceof Change && o2 instanceof Change) {
          return ChangesUtil.getFilePath((Change)o1).getName().compareToIgnoreCase(ChangesUtil.getFilePath((Change)o2).getName());
        }

        if (o1 instanceof ChangeList && o2 instanceof ChangeList) {
          return ((ChangeList)o1).getName().compareToIgnoreCase(((ChangeList)o2).getName());
        }

        if (o1 instanceof VirtualFile && o2 instanceof VirtualFile) {
          return ((VirtualFile)o1).getName().compareToIgnoreCase(((VirtualFile)o2).getName());
        }

        if (o1 instanceof FilePath && o2 instanceof FilePath) {
          return ((FilePath)o1).getPath().compareToIgnoreCase(((FilePath)o2).getPath());
        }

        if (o1 instanceof Module && o2 instanceof Module) {
          return ((Module)o1).getName().compareToIgnoreCase(((Module) o2).getName());
        }

        return 0;
      }

      private int getNodeClassWeight(Object userObject) {
        if (userObject instanceof ChangeList) {
          if (userObject instanceof LocalChangeList && ((LocalChangeList)userObject).isDefault()) return 1;
          return 2;
        }

        if (userObject instanceof Module) return 3;

        if (userObject instanceof FilePath) {
          if (((FilePath)userObject).isDirectory()) return 4;
          return 5;
        }

        if (userObject instanceof Change) return 6;
        if (userObject instanceof VirtualFile) return 7;
        return 8;
      }

    });

    model.nodeStructureChanged((TreeNode)model.getRoot());
  }

  private static void collapseDirectories(DefaultTreeModel model, ChangesBrowserNode node) {
    if (node.getUserObject() instanceof FilePath && node.getChildCount() == 1) {
      final ChangesBrowserNode child = (ChangesBrowserNode)node.getChildAt(0);
      if (child.getUserObject() instanceof FilePath && !child.isLeaf()) {
        ChangesBrowserNode parent = (ChangesBrowserNode)node.getParent();
        final int idx = parent.getIndex(node);
        model.removeNodeFromParent(node);
        model.removeNodeFromParent(child);
        model.insertNodeInto(child, parent, idx);
        collapseDirectories(model, parent);
      }
    }
    else {
      final Enumeration children = node.children();
      while (children.hasMoreElements()) {
        ChangesBrowserNode child = (ChangesBrowserNode)children.nextElement();
        collapseDirectories(model, child);
      }
    }
  }

  private static FilePath getPathForObject(Object o) {
    if (o instanceof Change) {
      return ChangesUtil.getFilePath((Change)o);
    }
    else if (o instanceof VirtualFile) {
      return PeerFactory.getInstance().getVcsContextFactory().createFilePathOn((VirtualFile)o);
    }
    else if (o instanceof FilePath) {
      return (FilePath)o;
    }

    return null;
  }

  private ChangesBrowserNode getParentNodeFor(ChangesBrowserNode node,
                                Map<FilePath, ChangesBrowserNode> folderNodesCache,
                                Map<Module, ChangesBrowserNode> moduleNodesCache,
                                ChangesBrowserNode rootNode) {
    if (showFlatten) {
      return rootNode;
    }

    ProjectFileIndex index = ProjectRootManager.getInstance(myProject).getFileIndex();
    final FilePath path = getPathForObject(node.getUserObject());

    VirtualFile vFile = path.getVirtualFile();
    if (vFile != null && vFile == index.getContentRootForFile(vFile)) {
      Module module = index.getModuleForFile(vFile);
      return getNodeForModule(module, moduleNodesCache, rootNode);
    }

    FilePath parentPath = path.getParentPath();
    if (parentPath == null) {
      return rootNode;
    }

    ChangesBrowserNode parentNode = folderNodesCache.get(parentPath);
    if (parentNode == null) {
      parentNode = new ChangesBrowserNode(myProject, parentPath);
      ChangesBrowserNode grandPa = getParentNodeFor(parentNode, folderNodesCache, moduleNodesCache, rootNode);
      model.insertNodeInto(parentNode, grandPa, grandPa.getChildCount());
      folderNodesCache.put(parentPath, parentNode);
    }

    return parentNode;
  }

  private ChangesBrowserNode getNodeForModule(Module module,
                                              Map<Module, ChangesBrowserNode> moduleNodesCache,
                                              ChangesBrowserNode root) {
    ChangesBrowserNode node = moduleNodesCache.get(module);
    if (node == null) {
      node = new ChangesBrowserNode(myProject, module);
      model.insertNodeInto(node, root, root.getChildCount());
      moduleNodesCache.put(module, node);
    }
    return node;
  }

}