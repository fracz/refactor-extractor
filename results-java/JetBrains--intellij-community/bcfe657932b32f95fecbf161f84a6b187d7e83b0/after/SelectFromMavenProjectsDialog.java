package org.jetbrains.idea.maven.navigator;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleNodeVisitor;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.tasks.MavenTasksManager;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class SelectFromMavenProjectsDialog extends DialogWrapper {
  private final Project myProject;
  private final SimpleTree myTree;
  private final NodeSelector mySelector;

  public SelectFromMavenProjectsDialog(Project project,
                                       String title,
                                       final Class<? extends MavenProjectsStructure.CustomNode> nodeClass,
                                       NodeSelector selector) {
    super(project, false);
    myProject = project;
    mySelector = selector;
    setTitle(title);

    myTree = new SimpleTree();
    myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    MavenProjectsStructure treeStructure = new MavenProjectsStructure(myProject,
                                                                      MavenProjectsManager.getInstance(myProject),
                                                                      MavenTasksManager.getInstance(myProject),
                                                                      MavenProjectsNavigator.getInstance(myProject),
                                                                      myTree) {
      @Override
      protected Class<? extends CustomNode>[] getVisibleNodesClasses() {
        return new Class[]{nodeClass};
      }
    };
    SimpleTreeBuilder treeBuilder = new SimpleTreeBuilder(myTree, (DefaultTreeModel)myTree.getModel(), treeStructure, null);
    treeBuilder.initRoot();
    Disposer.register(myProject, treeBuilder);

    treeStructure.buildTree();
    final SimpleNode[] selection = new SimpleNode[]{null};
    treeStructure.getRootElement().accept(new SimpleNodeVisitor() {
      public boolean accept(SimpleNode each) {
        if (!mySelector.shouldSelect(each)) return true;

        selection[0] = each;
        return false;
      }
    });

    treeBuilder.updateFromRoot(true);
    myTree.expandPath(new TreePath(myTree.getModel().getRoot()));

    if (selection[0] != null) {
      treeStructure.select(selection[0]);
    }

    init();
  }

  protected SimpleNode getSelectedNode() {
    return myTree.getNodeFor(myTree.getSelectionPath());
  }

  @Nullable
  protected JComponent createCenterPanel() {
    final JScrollPane pane = new JScrollPane(myTree);
    pane.setPreferredSize(new Dimension(320, 400));
    return pane;
  }

  protected interface NodeSelector {
    boolean shouldSelect(SimpleNode node);
  }
}