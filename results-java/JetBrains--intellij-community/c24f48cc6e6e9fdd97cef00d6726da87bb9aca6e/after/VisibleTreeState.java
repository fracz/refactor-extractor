package com.intellij.codeInspection.ex;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.profile.codeInspection.ui.InspectionConfigTreeNode;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

/**
 * User: anna
 * Date: Dec 18, 2004
 */
public class VisibleTreeState implements JDOMExternalizable{

  @NonNls private static final String EXPANDED = "expanded_node";
  @NonNls private static final String SELECTED = "selected_node";
  @NonNls private static final String NAME = "name";

  private final HashSet<String> myExpandedNodes = new HashSet<String>();
  private final HashSet<String> mySelectedNodes = new HashSet<String>();

  public VisibleTreeState(VisibleTreeState src) {
    myExpandedNodes.addAll(src.myExpandedNodes);
    mySelectedNodes.addAll(src.mySelectedNodes);
  }

  public VisibleTreeState() {
  }

  public void expandNode(String nodeTitle) {
    myExpandedNodes.add(nodeTitle);
  }

  public void collapseNode(String nodeTitle) {
    myExpandedNodes.remove(nodeTitle);
  }

  public void restoreVisibleState(Tree tree) {
    ArrayList<TreePath> pathsToExpand = new ArrayList<TreePath>();
    ArrayList<TreePath> toSelect = new ArrayList<TreePath>();
    traverseNodes((DefaultMutableTreeNode)tree.getModel().getRoot(), pathsToExpand, toSelect);
    TreeUtil.restoreExpandedPaths(tree, pathsToExpand);
    if (toSelect.isEmpty()) {
      TreeUtil.selectFirstNode(tree);
    }
    else {
      for (final TreePath aToSelect : toSelect) {
        TreeUtil.selectPath(tree, aToSelect);
      }
    }
  }

  private void traverseNodes(final DefaultMutableTreeNode root, List<TreePath> pathsToExpand, List<TreePath> toSelect) {
    final Descriptor descriptor = ((InspectionConfigTreeNode)root).getDesriptor();
    final TreeNode[] rootPath = root.getPath();
    if (descriptor != null) {
      final String shortName = descriptor.getKey().toString();
      if (mySelectedNodes.contains(shortName)) {
        toSelect.add(new TreePath(rootPath));
      }
      if (myExpandedNodes.contains(shortName)) {
        pathsToExpand.add(new TreePath(rootPath));
      }
    }
    else {
      final String str = ((InspectionConfigTreeNode)root).getGroupName();
      if (mySelectedNodes.contains(str)) {
        toSelect.add(new TreePath(rootPath));
      }
      if (myExpandedNodes.contains(str)) {
        pathsToExpand.add(new TreePath(rootPath));
      }
      for (int i = 0; i < root.getChildCount(); i++) {
        traverseNodes((DefaultMutableTreeNode)root.getChildAt(i), pathsToExpand, toSelect);
      }
    }
  }

  public void saveVisibleState(Tree tree) {
    myExpandedNodes.clear();
    final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)tree.getModel().getRoot();
    Enumeration<TreePath> expanded = tree.getExpandedDescendants(new TreePath(rootNode.getPath()));
    if (expanded != null) {
      while (expanded.hasMoreElements()) {
        final TreePath treePath = expanded.nextElement();
        final InspectionConfigTreeNode node = (InspectionConfigTreeNode)treePath.getLastPathComponent();
        final Descriptor descriptor = node.getDesriptor();
        String expandedNode;
        if (descriptor != null) {
          expandedNode = descriptor.getKey().toString();
        }
        else {
          expandedNode = node.getGroupName();
        }
        myExpandedNodes.add(expandedNode);
      }
    }

    setSelectionPaths(tree.getSelectionPaths());
  }

  public void setSelectionPaths(final TreePath[] selectionPaths) {
    mySelectedNodes.clear();
    if (selectionPaths != null) {
      for (TreePath selectionPath : selectionPaths) {
        final InspectionConfigTreeNode node = (InspectionConfigTreeNode)selectionPath.getLastPathComponent();
        final Descriptor descriptor = node.getDesriptor();
        String selectedNode;
        if (descriptor != null) {
          selectedNode = descriptor.getKey().toString();
        }
        else {
          selectedNode = node.getGroupName();
        }
        mySelectedNodes.add(selectedNode);
      }
    }
  }


  public void readExternal(Element element) throws InvalidDataException {
    myExpandedNodes.clear();
    List list = element.getChildren(EXPANDED);
    for (final Object element1 : list) {
      myExpandedNodes.add(((Element)element1).getAttributeValue(NAME));
    }
    mySelectedNodes.clear();
    list = element.getChildren(SELECTED);
    for (Object child : list) {
      mySelectedNodes.add(((Element)child).getAttributeValue(NAME));
    }
  }

  public void writeExternal(Element element) throws WriteExternalException {
    for (final String expandedNode : myExpandedNodes) {
      Element exp = new Element(EXPANDED);
      exp.setAttribute(NAME, expandedNode);
      element.addContent(exp);
    }
    for (final String selectedNode : mySelectedNodes) {
      Element exp = new Element(SELECTED);
      exp.setAttribute(NAME, selectedNode);
      element.addContent(exp);
    }
  }

  public boolean compare(Object object) {
    if (!(object instanceof VisibleTreeState)) return false;
    final VisibleTreeState that = (VisibleTreeState)object;
    if (myExpandedNodes.size() != that.myExpandedNodes.size()) return false;
    for (final String myExpandedNode : myExpandedNodes) {
      if (!that.myExpandedNodes.contains(myExpandedNode)) {
        return false;
      }
    }
    if (mySelectedNodes == null) {
      return that.mySelectedNodes == null;
    }
    if (mySelectedNodes.size() != that.mySelectedNodes.size()) return false;
    for (final String mySelectedNode : mySelectedNodes) {
      if (!that.mySelectedNodes.contains(mySelectedNode)) {
        return false;
      }
    }
    return true;
  }
}