package org.jetbrains.idea.maven.navigator;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.events.MavenEventsHandler;
import org.jetbrains.idea.maven.project.MavenProjectModel;
import org.jetbrains.idea.maven.state.MavenProjectsManager;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * @author Vladislav.Kaznacheev
 */
public class SelectMavenGoalDialog extends DialogWrapper {

  private final SimpleTree tree;

  private String pomPath;

  private String goal;

  public SelectMavenGoalDialog(final Project project, final String pomPath, final String goal, final String title) {
    super(project, false);
    setTitle(title);

    tree = new SimpleTree();
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    final PopupMavenTreeStructure treeStructure = new PopupMavenTreeStructure(project);

    final SimpleTreeBuilder treeBuilder = new SimpleTreeBuilder(tree, (DefaultTreeModel)tree.getModel(), treeStructure, null);
    treeBuilder.initRoot();
    Disposer.register(project, treeBuilder);

    final MavenTreeStructure.GoalNode goalNode = treeStructure.init(pomPath, goal);

    treeBuilder.updateFromRoot(true);
    tree.expandPath(new TreePath(tree.getModel().getRoot()));

    if (goalNode != null) {
      tree.setSelectedNode(treeBuilder, goalNode, true);
      // TODO: does not work because of delayed children creation
    }

    init();
  }

  @Nullable
  protected JComponent createCenterPanel() {
    final JScrollPane pane = new JScrollPane(tree);
    pane.setPreferredSize(new Dimension(320, 400));
    return pane;
  }

  protected void doOKAction() {
    super.doOKAction();

    SimpleNode node = tree.getNodeFor(tree.getSelectionPath());
    if (node instanceof MavenTreeStructure.GoalNode) {
      final MavenTreeStructure.GoalNode goalNode = (MavenTreeStructure.GoalNode)node;
      pomPath = goalNode.getParent(MavenTreeStructure.PomNode.class).getMavenProjectModel().getPath();
      goal = goalNode.getGoal();
    }
  }

  public String getSelectedPomPath() {
    return pomPath;
  }

  public String getSelectedGoal() {
    return goal;
  }

  private class PopupMavenTreeStructure extends MavenTreeStructure {

    private PomTreeViewSettings myTreeViewSettings;

    public PopupMavenTreeStructure(final Project project) {
      super(project,
            MavenProjectsManager.getInstance(project),
            project.getComponent(MavenEventsHandler.class));
      myTreeViewSettings = project.getComponent(MavenProjectNavigator.class).getTreeViewSettings();
    }

    protected PomTreeViewSettings getTreeViewSettings() {
      return myTreeViewSettings;
    }

    protected void updateTreeFrom(SimpleNode node) {
    }

    protected boolean isMinimalView() {
      return true;
    }

    @Nullable
    GoalNode init(final String pomPath, final String goalName) {
      GoalNode goalNode = null;
      for (MavenProjectModel each : myProjectsManager.getProjects()) {
        final PomNode pomNode = new PomNode(each);
        root.addToStructure(pomNode);

        if (pomPath != null && pomPath.equals(each.getPath())) {
          goalNode = pomNode.findGoalNode(goalName);
        }
      }
      return goalNode;
    }
  }
}