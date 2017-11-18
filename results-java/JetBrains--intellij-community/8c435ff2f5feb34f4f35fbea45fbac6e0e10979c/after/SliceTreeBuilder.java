package com.intellij.slicer;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AlphaComparator;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

/**
 * @author cdr
 */
public class SliceTreeBuilder extends AbstractTreeBuilder{
  public SliceTreeBuilder(JTree tree, Project project) {
    super(tree, (DefaultTreeModel)tree.getModel(), new SliceTreeStructure(project), AlphaComparator.INSTANCE, false);
    initRootNode();
  }

  protected boolean isAlwaysShowPlus(NodeDescriptor nodeDescriptor) {
    return false;
  }

  protected boolean isAutoExpandNode(NodeDescriptor nodeDescriptor) {
    return false;
  }

  public void setRoot(SliceNode root) {
    ((SliceTreeStructure)getTreeStructure()).setRoot(root);
  }
}