package org.jetbrains.idea.maven.navigator;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.util.Function;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.core.util.IdeaAPIHelper;
import org.jetbrains.idea.maven.events.MavenEventsHandler;
import org.jetbrains.idea.maven.project.MavenProjectModel;
import org.jetbrains.idea.maven.project.MavenProjectModelProblem;
import org.jetbrains.idea.maven.project.ProjectBundle;
import org.jetbrains.idea.maven.state.MavenProjectsManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@State(name = "MavenProjectNavigator", storages = {@Storage(id = "default", file = "$WORKSPACE_FILE$")})
public class MavenProjectNavigator extends MavenTreeStructure implements ProjectComponent, PersistentStateComponent<PomTreeViewSettings> {
  @NonNls private static final String MAVEN_NAVIGATOR_TOOLWINDOW_ID = "Maven projects";
  private static final Icon ICON = IconLoader.getIcon("/images/mavenEmblem.png");

  private PomTreeViewSettings mySettings = new PomTreeViewSettings();

  private SimpleTreeBuilder myTreeBuilder;
  private SimpleTree myTree;

  private Map<VirtualFile, PomNode> fileToNode = new LinkedHashMap<VirtualFile, PomNode>();
  private MavenProjectsManager.Listener myMavenProjectsListener;
  private MavenEventsHandler.Listener myMavenEventsListener;

  public static MavenProjectNavigator getInstance(Project project) {
    return project.getComponent(MavenProjectNavigator.class);
  }

  public MavenProjectNavigator(Project project,
                               MavenProjectsManager projectsManager,
                               MavenEventsHandler eventsHandler) {
    super(project, projectsManager, eventsHandler);
  }

  @NotNull
  @NonNls
  public String getComponentName() {
    return "MavenProjectNavigator";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  public void projectOpened() {
    if (myProject.isDefault()) return;
    if (ApplicationManager.getApplication().isUnitTestMode()) return;
    if (ApplicationManager.getApplication().isHeadlessEnvironment()) return;

    initMavenProjectsTree();
    initToolWindow();
    subscribeForChanges();
  }

  public void projectClosed() {
    unsubscribe();
  }

  private void initMavenProjectsTree() {
    myTree = new SimpleTree() {
      private JLabel myLabel = new JLabel(ProjectBundle.message("maven.navigator.nothing.to.display",
                                                                formatHtmlImage("/general/add.png"),
                                                                formatHtmlImage("/actions/sync.png")));

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (myProjectsManager.isMavenizedProject()) return;

        myLabel.setFont(getFont());
        myLabel.setBackground(getBackground());
        myLabel.setForeground(getForeground());
        Rectangle bounds = getBounds();
        Dimension size = myLabel.getPreferredSize();
        myLabel.setBounds(0, 0, size.width, size.height);

        int x = (bounds.width - size.width) / 2;
        Graphics g2 = g.create(bounds.x + x, bounds.y + 20, bounds.width, bounds.height);
        try {
          myLabel.paint(g2);
        }
        finally {
          g2.dispose();
        }
      }
    };

    myTree.setRootVisible(false);
    myTree.setShowsRootHandles(true);
    myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

    myTreeBuilder = new SimpleTreeBuilder(myTree, (DefaultTreeModel)myTree.getModel(), this, null);
    myTreeBuilder.initRoot();

    Disposer.register(myProject, myTreeBuilder);

    myTree.addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {
      }

      public void mouseMoved(MouseEvent e) {
        TreePath p = myTree.getPathForLocation(e.getX(), e.getY());
        if (p != null) {
          Object last = p.getLastPathComponent();
          Object object = ((DefaultMutableTreeNode)last).getUserObject();
          if (object instanceof PomNode) {
            PomNode pomNode = (PomNode)object;
            showProblemsToolTip(pomNode);
          }
        }
      }
    });

    IdeaAPIHelper.installCheckboxRenderer(myTree, new IdeaAPIHelper.CheckboxHandler() {
      public void toggle(final TreePath treePath, final InputEvent e) {
        final SimpleNode node = myTree.getNodeFor(treePath);
        if (node != null) {
          node.handleDoubleClickOrEnter(myTree, e);
        }
      }

      public boolean isVisible(final Object userObject) {
        return userObject instanceof ProfileNode;
      }

      public boolean isSelected(final Object userObject) {
        return ((ProfileNode)userObject).isActive();
      }
    });
  }


  private void showProblemsToolTip(PomNode pomNode) {
    List<MavenProjectModelProblem> problems = pomNode.getMavenProjectModel().getProblems();
    if (problems.isEmpty()) {
      myTree.setToolTipText(null);
      return;
    }

    String s = StringUtil.join(problems, new Function<MavenProjectModelProblem, String>() {
      public String fun(MavenProjectModelProblem each) {
        String image = each.isCritical() ? "/images/error.png" : "/images/warning.png";
        return formatHtmlImage(image) + each.getDescription();
      }
    }, "<br>");
    myTree.setToolTipText("<html>" + s + "</html>");
  }

  private String formatHtmlImage(String path) {
    return "<img src=\"" + getClass().getResource(path) + "\"> ";
  }

  private void subscribeForChanges() {
    myMavenProjectsListener = new MavenProjectsManager.Listener() {
      boolean isActivated;
      public void activate() {
        for (MavenProjectModel each : myProjectsManager.getProjects()) {
          fileToNode.put(each.getFile(), new PomNode(each));
        }

        updateFromRoot(true, true);
        isActivated = true;
      }

      public void setIgnored(VirtualFile file, boolean on) {
        if (!isActivated) return;

        final PomNode pomNode = fileToNode.get(file);
        if (pomNode != null) {
          pomNode.setIgnored(on);
        }
      }

      public void profilesChanged(List<String> profiles) {
        if (!isActivated) return;

        root.setActiveProfiles(profiles);
        myTree.repaint();
      }

      public void projectAdded(final MavenProjectModel n) {
        if (!isActivated) return;

        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            if (myProject.isDisposed()) return;

            final PomNode newNode = new PomNode(n);
            fileToNode.put(n.getFile(), newNode);
            root.addToStructure(newNode);
            root.updateProfileNodes();

            updateFromRoot(true, true);
            myTree.repaint();
          }
        });
      }

      public void beforeProjectUpdate(MavenProjectModel n) {
      }

      public void projectUpdated(final MavenProjectModel n) {
        if (!isActivated) return;

        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            if (myProject.isDisposed()) return;

            final PomNode pomNode = fileToNode.get(n.getFile());
            if (pomNode != null) {
              pomNode.onFileUpdate();
            }
            else {
              projectAdded(n);
            }
            root.updateProfileNodes();
            myTree.repaint();
          }
        });
      }

      public void projectRemoved(final MavenProjectModel n) {
        if (!isActivated) return;

        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            if (myProject.isDisposed()) return;

            final PomNode pomNode = fileToNode.get(n.getFile());
            if (pomNode != null) {
              fileToNode.remove(n.getFile());
              pomNode.removeFromParent();
            }
            root.updateProfileNodes();
            myTree.repaint();
          }
        });
      }
    };

    myMavenEventsListener = new MavenEventsHandler.Listener() {
      public void updateShortcuts(@Nullable String actionId) {
        for (PomNode pomNode : fileToNode.values()) {
          pomNode.updateShortcuts(actionId);
        }
      }
    };

    myProjectsManager.addListener(myMavenProjectsListener);
    myEventsHandler.addListener(myMavenEventsListener);

    myEventsHandler.installTaskSelector(new MavenEventsHandler.TaskSelector() {
      SelectMavenGoalDialog dialog;

      public boolean select(final Project project,
                            @Nullable final String pomPath,
                            @Nullable final String goal,
                            @NotNull final String title) {
        dialog = new SelectMavenGoalDialog(project, pomPath, goal, title);
        dialog.show();
        return dialog.isOK();
      }

      public String getSelectedPomPath() {
        return dialog.getSelectedPomPath();
      }

      public String getSelectedGoal() {
        return dialog.getSelectedGoal();
      }
    });
  }

  private void unsubscribe() {
    myEventsHandler.removeListener(myMavenEventsListener);
    myProjectsManager.removeListener(myMavenProjectsListener);
  }

  private void initToolWindow() {
    final JPanel navigatorPanel = new MavenNavigatorPanel(myProject, myProjectsManager, myTree);

    ToolWindow pomToolWindow = ToolWindowManager.getInstance(myProject)
        .registerToolWindow(MAVEN_NAVIGATOR_TOOLWINDOW_ID, navigatorPanel, ToolWindowAnchor.RIGHT, myProject);
    pomToolWindow.setIcon(ICON);
  }

  public PomTreeViewSettings getTreeViewSettings() {
    return mySettings;
  }

  public PomTreeViewSettings getState() {
    return mySettings;
  }

  public void loadState(PomTreeViewSettings state) {
    mySettings = state;
  }

  protected void updateTreeFrom(@Nullable SimpleNode node) {
    if (node != null) {
      final DefaultMutableTreeNode mutableTreeNode = TreeUtil.findNodeWithObject((DefaultMutableTreeNode)myTree.getModel().getRoot(), node);
      if (mutableTreeNode != null) {
        myTreeBuilder.addSubtreeToUpdate(mutableTreeNode);
        return;
      }
    }
    updateFromRoot(node == root, false);
  }

  public void updateFromRoot(boolean rebuild, boolean restructure) {
    if (restructure) {
      root.rebuild(fileToNode.values());
    }
    myTreeBuilder.updateFromRoot(rebuild);
    if (rebuild) {
      myTree.expandPath(new TreePath(myTree.getModel().getRoot()));
    }
  }
}