package org.jetbrains.idea.maven.navigator;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiManager;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.embedder.MavenEmbedderFactory;
import org.jetbrains.idea.maven.project.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectProblem;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.tasks.MavenTasksManager;
import org.jetbrains.idea.maven.utils.*;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.InputEvent;
import java.net.URL;
import java.util.*;
import java.util.List;

public class MavenProjectsStructure extends SimpleTreeStructure {
  private static final Icon MAVEN_PROJECT_ICON = IconLoader.getIcon("/images/mavenProject.png");

  private static final Icon OPEN_PROFILES_ICON = IconLoader.getIcon("/images/profilesOpen.png");
  private static final Icon CLOSED_PROFILES_ICON = IconLoader.getIcon("/images/profilesClosed.png");

  private static final Icon OPEN_PHASES_ICON = IconLoader.getIcon("/images/phasesOpen.png");
  private static final Icon CLOSED_PHASES_ICON = IconLoader.getIcon("/images/phasesClosed.png");
  private static final Icon PHASE_ICON = IconLoader.getIcon("/images/phase.png");

  private static final Icon OPEN_PLUGINS_ICON = IconLoader.getIcon("/images/phasesOpen.png");
  private static final Icon CLOSED_PLUGINS_ICON = IconLoader.getIcon("/images/phasesClosed.png");
  private static final Icon PLUGIN_ICON = IconLoader.getIcon("/images/mavenPlugin.png");
  private static final Icon PLUGIN_GOAL_ICON = IconLoader.getIcon("/images/pluginGoal.png");

  private static final Icon OPEN_MODULES_ICON = IconLoader.getIcon("/images/modulesOpen.png");
  private static final Icon CLOSED_MODULES_ICON = IconLoader.getIcon("/images/modulesClosed.png");

  private static final URL ERROR_ICON_URL = MavenProjectsStructure.class.getResource("/images/error.png");
  private static final URL WARNING_ICON_URL = MavenProjectsStructure.class.getResource("/images/warning.png");

  private static final CustomNode[] EMPTY_NODES_ARRAY = new CustomNode[0];

  private static final Collection<String> BASIC_PHASES = MavenEmbedderFactory.getBasicPhasesList();
  private static final Collection<String> PHASES = MavenEmbedderFactory.getPhasesList();

  private static final Comparator<SimpleNode> NODE_COMPARATOR = new Comparator<SimpleNode>() {
    public int compare(SimpleNode o1, SimpleNode o2) {
      if (o1 instanceof ProfilesNode) return -1;
      if (o2 instanceof ProfilesNode) return 1;
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  };

  private final Project myProject;
  private final MavenProjectsManager myProjectsManager;
  private final MavenTasksManager myTasksManager;
  private final MavenProjectsNavigator myProjectsNavigator;

  private final SimpleTreeBuilder myTreeBuilder;
  private final RootNode myRoot = new RootNode();

  private final Map<MavenProject, ProjectNode> myProjectToNodeMapping = new THashMap<MavenProject, ProjectNode>();

  public MavenProjectsStructure(Project project,
                                MavenProjectsManager projectsManager,
                                MavenTasksManager tasksManager,
                                MavenProjectsNavigator projectsNavigator,
                                SimpleTree tree) {
    myProject = project;
    myProjectsManager = projectsManager;
    myTasksManager = tasksManager;
    myProjectsNavigator = projectsNavigator;

    configureTree(tree);

    myTreeBuilder = new SimpleTreeBuilder(tree, (DefaultTreeModel)tree.getModel(), this, null);
    Disposer.register(myProject, myTreeBuilder);

    myTreeBuilder.initRoot();
    myTreeBuilder.expand(myRoot, null);
  }

  private void configureTree(final SimpleTree tree) {
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);

    MavenUIUtil.installCheckboxRenderer(tree, new MavenUIUtil.CheckboxHandler() {
      public void toggle(TreePath treePath, InputEvent e) {
        SimpleNode node = tree.getNodeFor(treePath);
        if (node != null) {
          node.handleDoubleClickOrEnter(tree, e);
        }
      }

      public boolean isVisible(Object userObject) {
        return userObject instanceof ProfileNode;
      }

      public boolean isSelected(Object userObject) {
        return ((ProfileNode)userObject).isActive();
      }
    });
  }

  public RootNode getRootElement() {
    return myRoot;
  }

  public List<ProjectNode> getProjectNodes() {
    return new ArrayList<ProjectNode>(myProjectToNodeMapping.values());
  }

  public void buildTree() {
    updateProjects(myProjectsManager.getProjects());
  }

  public void update(boolean restructure) {
    if (restructure) {
      buildTree();
    }
    else {
      updateFrom(myRoot);
    }
  }

  private void updateFrom(SimpleNode node) {
    myTreeBuilder.addSubtreeToUpdateByElement(node);
  }

  public void updateProjects(List<MavenProject> projects) {
    for (MavenProject each : projects) {
      ProjectNode node = findNodeFor(each);
      if (node == null) {
        node = new ProjectNode(each);
        myProjectToNodeMapping.put(each, node);
      }
      doUpdateProject(node);
    }
    myRoot.updateProfiles();
  }

  private void doUpdateProject(ProjectNode node) {
    MavenProject project = node.getMavenProject();

    ProjectsGroupNode newParentNode = myRoot;

    if (myProjectsNavigator.getGroupModules()) {
      MavenProject aggregator = myProjectsManager.findAggregator(project);
      if (aggregator != null) {
        ProjectNode aggregatorNode = findNodeFor(aggregator);
        if (aggregatorNode != null && aggregatorNode.isVisible()) {
          newParentNode = aggregatorNode.getModulesNode();
        }
      }
    }

    node.updateNode();
    reconnectNode(node, newParentNode);

    ProjectsGroupNode newModulesParentNode = myProjectsNavigator.getGroupModules() && node.isVisible()
                                             ? node.getModulesNode()
                                             : myRoot;
    for (MavenProject each : myProjectsManager.getModules(project)) {
      ProjectNode moduleNode = findNodeFor(each);
      if (moduleNode != null && !moduleNode.getStructuralParent().equals(newModulesParentNode)) {
        reconnectNode(moduleNode, newModulesParentNode);
      }
    }
  }

  private void reconnectNode(ProjectNode node, ProjectsGroupNode newParentNode) {
    ProjectsGroupNode oldParentNode = node.getStructuralParent();
    if (oldParentNode == null || !oldParentNode.equals(newParentNode)) {
      if (oldParentNode != null) {
        oldParentNode.remove(node);
        updateFrom(oldParentNode);
      }
      newParentNode.add(node);
      updateFrom(newParentNode);
    }
    else {
      newParentNode.sortProjects();
      updateFrom(newParentNode);
    }
  }

  public void removeProject(MavenProject project) {
    ProjectNode node = myProjectToNodeMapping.remove(project);
    if (node != null) {
      ProjectsGroupNode parent = node.getStructuralParent();
      parent.remove(node);
      myRoot.updateProfiles();
      updateFrom(myRoot.getProfilesNode());
    }
  }

  public void setActiveProfiles(List<String> profiles) {
    myRoot.setActiveProfiles(profiles);
    updateFrom(myRoot.getProfilesNode());
  }

  public void updateIgnored(List<MavenProject> projects) {
    for (MavenProject each : projects) {
      ProjectNode node = findNodeFor(each);
      if (node == null) continue;
      updateFrom(node.getStructuralParent());
    }
  }

  public void updateShortcuts() {
    for (ProjectNode each : myProjectToNodeMapping.values()) {
      each.updateShortcuts();
      updateFrom(each);
    }
  }

  public void select(MavenProject project) {
    ProjectNode node = findNodeFor(project);
    if (node != null) select(node);
  }

  public void select(SimpleNode node) {
    myTreeBuilder.select(node, null);
  }

  private ProjectNode findNodeFor(MavenProject project) {
    return myProjectToNodeMapping.get(project);
  }

  private boolean shouldDisplayNode(CustomNode node) {
    Class[] visibles = getVisibleNodesClasses();
    if (visibles == null) return true;

    for (Class each : visibles) {
      if (each.isInstance(node)) return true;
    }
    return false;
  }

  protected Class<? extends CustomNode>[] getVisibleNodesClasses() {
    return null;
  }

  public static <T extends CustomNode> List<T> getSelectedNodes(SimpleTree tree, Class<T> nodeClass) {
    final List<T> filtered = new ArrayList<T>();
    for (SimpleNode node : getSelectedNodes(tree)) {
      if ((nodeClass != null) && (!nodeClass.isInstance(node))) {
        filtered.clear();
        break;
      }
      //noinspection unchecked
      filtered.add((T)node);
    }
    return filtered;
  }

  private static List<SimpleNode> getSelectedNodes(SimpleTree tree) {
    List<SimpleNode> nodes = new ArrayList<SimpleNode>();
    TreePath[] treePaths = tree.getSelectionPaths();
    if (treePaths != null) {
      for (TreePath treePath : treePaths) {
        nodes.add(tree.getNodeFor(treePath));
      }
    }
    return nodes;
  }

  private static <T extends SimpleNode> void insertSorted(List<T> list, T newObject) {
    int pos = Collections.binarySearch(list, newObject, NODE_COMPARATOR);
    list.add(pos >= 0 ? pos : -pos - 1, newObject);
  }

  @Nullable
  public static ProjectNode getCommonProjectNode(Collection<? extends CustomNode> nodes) {
    ProjectNode parent = null;
    for (CustomNode node : nodes) {
      ProjectNode nextParent = node.getParent(ProjectNode.class);
      if (parent == null) {
        parent = nextParent;
      }
      else if (parent != nextParent) {
        return null;
      }
    }
    return parent;
  }

  public enum ErrorLevel {
    NONE, WARNING, ERROR
  }

  public abstract class CustomNode extends SimpleNode {
    private CustomNode myStructuralParent;
    private ErrorLevel myNodeErrorLevel = ErrorLevel.NONE;
    private ErrorLevel myOverallErrorLevelCache = null;

    public CustomNode(CustomNode parent) {
      super(parent);
      setStructuralParent(parent);
    }

    public void setStructuralParent(CustomNode structuralParent) {
      myStructuralParent = structuralParent;
    }

    public CustomNode getStructuralParent() {
      return myStructuralParent;
    }

    @Override
    public NodeDescriptor getParentDescriptor() {
      return myStructuralParent;
    }

    public <T extends CustomNode> T getParent(Class<T> parentClass) {
      CustomNode node = this;
      while (true) {
        node = node.myStructuralParent;
        if (node == null || parentClass.isInstance(node)) {
          //noinspection unchecked
          return (T)node;
        }
      }
    }

    public boolean isVisible() {
      return shouldDisplayNode(this);
    }

    public CustomNode[] getChildren() {
      List<? extends CustomNode> children = getStructuralChildren();
      if (children.isEmpty()) return EMPTY_NODES_ARRAY;

      List<CustomNode> result = new ArrayList<CustomNode>();
      for (CustomNode each : children) {
        if (each.isVisible()) result.add(each);
      }
      return result.toArray(new CustomNode[result.size()]);
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      return Collections.emptyList();
    }

    protected void resetChildrenCaches() {
      myOverallErrorLevelCache = null;
    }

    public ErrorLevel getOverallErrorLevel() {
      if (myOverallErrorLevelCache == null) {
        myOverallErrorLevelCache = calcOverallErrorLevel();
      }
      return myOverallErrorLevelCache;
    }

    private ErrorLevel calcOverallErrorLevel() {
      ErrorLevel childrenErrorLevel = getChildrenErrorLevel();
      return childrenErrorLevel.compareTo(myNodeErrorLevel) > 0
             ? childrenErrorLevel
             : myNodeErrorLevel;
    }

    public ErrorLevel getChildrenErrorLevel() {
      ErrorLevel result = ErrorLevel.NONE;
      for (CustomNode each : getStructuralChildren()) {
        ErrorLevel eachLevel = each.getOverallErrorLevel();
        if (eachLevel.compareTo(result) > 0) result = eachLevel;
      }
      return result;
    }

    public void setNodeErrorLevel(ErrorLevel level) {
      if (myNodeErrorLevel == level) return;
      myNodeErrorLevel = level;

      CustomNode each = this;
      while (each != null) {
        each.resetChildrenCaches();
        each.updateNameAndDescription();
        each = each.myStructuralParent;
      }
    }

    protected abstract void updateNameAndDescription();

    @Override
    protected void doUpdate() {
      super.doUpdate();
      updateNameAndDescription();
    }

    protected void setNameAndDescription(String name, String description) {
      setNameAndDescription(name, description, (String)null);
    }

    protected void setNameAndDescription(String name, String description, @Nullable String hint) {
      setNameAndDescription(name, description, getPlainAttributes());
      if (hint != null) {
        addColoredFragment(" (" + hint + ")", description, SimpleTextAttributes.GRAY_ATTRIBUTES);
      }
    }

    protected void setNameAndDescription(String name, String description, SimpleTextAttributes attribs) {
      clearColoredText();
      addColoredFragment(name, description, prepareAttribs(attribs));
    }

    private SimpleTextAttributes prepareAttribs(SimpleTextAttributes from) {
      ErrorLevel level = getOverallErrorLevel();
      Color waveColor = level == ErrorLevel.NONE
                        ? null : (level == ErrorLevel.WARNING ? Color.GRAY : Color.RED);
      int style = from.getStyle();
      if (waveColor != null) style |= SimpleTextAttributes.STYLE_WAVED;
      return new SimpleTextAttributes(from.getBgColor(),
                                      from.getFgColor(),
                                      waveColor,
                                      style);
    }

    @Nullable
    @NonNls
    String getActionId() {
      return null;
    }

    @Nullable
    @NonNls
    String getMenuId() {
      return null;
    }

    public void handleDoubleClickOrEnter(SimpleTree tree, InputEvent inputEvent) {
      String actionId = getActionId();
      if (actionId != null) {
        MavenUIUtil.executeAction(actionId, inputEvent);
      }
    }
  }

  public abstract class GroupNode extends CustomNode {
    public GroupNode(CustomNode parent) {
      super(parent);
    }

    @Override
    public boolean isVisible() {
      if (!super.isVisible()) return false;
      for (CustomNode each : getStructuralChildren()) {
        if (each.isVisible()) return true;
      }
      return false;
    }
  }

  public abstract class ProjectsGroupNode extends GroupNode {
    protected final List<ProjectNode> myProjectNodes = new ArrayList<ProjectNode>();

    public ProjectsGroupNode(CustomNode parent) {
      super(parent);
      setIcons(CLOSED_MODULES_ICON, OPEN_MODULES_ICON);
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      return myProjectNodes;
    }

    public List<ProjectNode> getProjectNodes() {
      return myProjectNodes;
    }

    protected void add(ProjectNode projectNode) {
      projectNode.setStructuralParent(this);
      insertSorted(myProjectNodes, projectNode);
      resetChildrenCaches();
    }

    public void remove(ProjectNode projectNode) {
      projectNode.setStructuralParent(null);
      myProjectNodes.remove(projectNode);
      resetChildrenCaches();
    }

    public void sortProjects() {
      Collections.sort(myProjectNodes, NODE_COMPARATOR);
    }
  }

  public class RootNode extends ProjectsGroupNode {
    private ProfilesNode myProfilesNode;

    public RootNode() {
      super(null);
      myProfilesNode = new ProfilesNode(this);
      updateNameAndDescription();
    }

    protected void updateNameAndDescription() {
      setNameAndDescription(NavigatorBundle.message("node.root"), null);
    }

    @Override
    public boolean isVisible() {
      return true;
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      return ContainerUtil.concat(Collections.singletonList(myProfilesNode), myProjectNodes);
    }

    public ProfilesNode getProfilesNode() {
      return myProfilesNode;
    }

    public void updateProfiles() {
      myProfilesNode.updateProfiles();
    }

    public void setActiveProfiles(List<String> profiles) {
      myProfilesNode.setActiveProfiles(profiles);
    }
  }

  public class ProjectNode extends CustomNode {
    private final MavenProject myMavenProject;
    private final LifecycleNode myLifecycleNode;
    private final PluginsNode myPluginsNode;
    private final ModulesNode myModulesNode;

    public ProjectNode(MavenProject mavenProject) {
      super(null);
      myMavenProject = mavenProject;

      myLifecycleNode = new LifecycleNode(this);
      myPluginsNode = new PluginsNode(this);
      myModulesNode = new ModulesNode(this);

      updateNode();
      setUniformIcon(MAVEN_PROJECT_ICON);
    }

    @Override
    public ProjectsGroupNode getStructuralParent() {
      return (ProjectsGroupNode)super.getStructuralParent();
    }

    @Override
    public boolean isVisible() {
      return super.isVisible() && (myProjectsNavigator.getShowIgnored() || !myProjectsManager.isIgnored(myMavenProject));
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      return Arrays.asList(myLifecycleNode, myPluginsNode, myModulesNode);
    }

    public ModulesNode getModulesNode() {
      return myModulesNode;
    }

    public String getProjectName() {
      return myMavenProject.getDisplayName();
    }

    public MavenProject getMavenProject() {
      return myMavenProject;
    }

    public VirtualFile getFile() {
      return myMavenProject.getFile();
    }

    public String getPath() {
      return getFile().getPath();
    }

    @Nullable
    @NonNls
    protected String getMenuId() {
      return "Maven.ProjectMenu";
    }

    @Nullable
    public Navigatable getNavigatable() {
      return PsiManager.getInstance(MavenProjectsStructure.this.myProject).findFile(getFile());
    }

    private void updateNode() {
      updateErrorLevel();
      updateNameAndDescription();
      myPluginsNode.updatePlugins(myMavenProject);
    }

    private void updateErrorLevel() {
      List<MavenProjectProblem> problems = myMavenProject.getProblems();
      if (problems.isEmpty()) {
        setNodeErrorLevel(ErrorLevel.NONE);
      }
      else {
        boolean isError = false;
        for (MavenProjectProblem each : problems) {
          if (each.isCritical()) {
            isError = true;
            break;
          }
        }
        setNodeErrorLevel(isError ? ErrorLevel.ERROR : ErrorLevel.WARNING);
      }
    }

    @Override
    protected SimpleTextAttributes getPlainAttributes() {
      if (myProjectsManager.isIgnored(myMavenProject)) {
        return new SimpleTextAttributes(SimpleTextAttributes.STYLE_STRIKEOUT, Color.GRAY);
      }
      return super.getPlainAttributes();
    }

    @Override
    protected void updateNameAndDescription() {
      setNameAndDescription(getProjectName(), makeDescription());
    }

    private String makeDescription() {
      StringBuilder desc = new StringBuilder();
      desc.append("<html>");
      desc.append("<table>");
      desc.append("<tr>");
      desc.append("<td>");
      desc.append("  <table>");
      desc.append("  <tr>");
      desc.append("  <td>Project:</td>");
      desc.append("  <td nowrap>" + myMavenProject.getMavenId() + "</td>");
      desc.append("  </tr>");
      desc.append("  <tr>");
      desc.append("  <td>Location:</td>");
      desc.append("  <td nowrap>" + getFile().getPath() + "</td>");
      desc.append("  </tr>");
      desc.append("  </table>");
      desc.append("</td>");
      desc.append("</tr>");
      appendProblems(desc, true);
      appendProblems(desc, false);

      if (getModulesErrorLevel() != ErrorLevel.NONE) {
        desc.append("<tr>");
        desc.append("<td><i>Some modules have problems.</i></td>");
        desc.append("</tr>");
      }

      desc.append("</table>");
      desc.append("</html>");
      return desc.toString();
    }

    private ErrorLevel getModulesErrorLevel() {
      ErrorLevel result = ErrorLevel.NONE;
      for (ProjectNode each : myModulesNode.myProjectNodes) {
        ErrorLevel moduleLevel = each.getOverallErrorLevel();
        if (moduleLevel.compareTo(result) > 0) result = moduleLevel;
      }
      return result;
    }

    private void appendProblems(StringBuilder desc, boolean critical) {
      List<MavenProjectProblem> problems = collectProblems(critical);
      if (problems.isEmpty()) return;

      desc.append("<tr>");
      desc.append("<td>");
      desc.append("<table>");
      boolean first = true;
      for (MavenProjectProblem each : problems) {
        desc.append("<tr>");
        if (first) {
          desc.append("<td valign=top>" + MavenUtil.formatHtmlImage(critical ? ERROR_ICON_URL : WARNING_ICON_URL) + "</td>");
          desc.append("<td valign=top>" + (critical ? "Errors" : "Warnings") + ":</td>");
          first = false;
        }
        else {
          desc.append("<td colspan=2></td>");
        }
        desc.append("<td valign=top>" + wrappedText(each));
        desc.append("</tr>");
      }
      desc.append("</table>");
      desc.append("</td>");
      desc.append("</tr>");
    }

    private String wrappedText(MavenProjectProblem each) {
      String text = each.getDescription();
      StringBuffer result = new StringBuffer();
      int count = 0;
      for (int i = 0; i < text.length(); i++) {
        char ch = text.charAt(i);
        result.append(ch);

        if (count++ > 80) {
          if (ch == ' ') {
            count = 0;
            result.append("<br>");
          }
        }
      }
      return result.toString();
    }

    private List<MavenProjectProblem> collectProblems(boolean critical) {
      List<MavenProjectProblem> result = new ArrayList<MavenProjectProblem>();
      for (MavenProjectProblem each : myMavenProject.getProblems()) {
        if (critical == each.isCritical()) result.add(each);
      }
      return result;
    }

    public void updateShortcuts() {
      myLifecycleNode.updateShortcuts();
      myPluginsNode.updateShortcuts();
    }
  }

  public class ModulesNode extends ProjectsGroupNode {
    public ModulesNode(ProjectNode parent) {
      super(parent);
      setIcons(CLOSED_MODULES_ICON, OPEN_MODULES_ICON);
    }

    @Override
    protected void updateNameAndDescription() {
      setNameAndDescription(NavigatorBundle.message("node.modules"), null);
    }
  }

  public abstract class GoalsGroupNode extends GroupNode {
    protected final List<GoalNode> myGoalNodes = new ArrayList<GoalNode>();

    public GoalsGroupNode(CustomNode parent) {
      super(parent);
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      return myGoalNodes;
    }

    public void updateShortcuts() {
      for (GoalNode each : myGoalNodes) {
        each.update();
      }
    }
  }

  public abstract class GoalNode extends CustomNode {
    private final MavenProject myMavenProject;
    private final String myGoal;
    private final String myDisplayName;

    public GoalNode(GoalsGroupNode parent, String goal, String displayName) {
      super(parent);
      myMavenProject = getParent(ProjectNode.class).getMavenProject();
      myGoal = goal;
      myDisplayName = displayName;
      updateNameAndDescription();
      setUniformIcon(PHASE_ICON);
    }

    public String getProjectPath() {
      return myMavenProject.getPath();
    }

    @Override
    protected void updateNameAndDescription() {
      String hint = myTasksManager.getActionDescription(getProjectPath(), myGoal);
      setNameAndDescription(myDisplayName, null, hint);
    }

    @Override
    protected SimpleTextAttributes getPlainAttributes() {
      if (myGoal.equals(myMavenProject.getDefaultGoal())) {
        return new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, getColor());
      }
      return super.getPlainAttributes();
    }

    public String getGoal() {
      return myGoal;
    }

    @Nullable
    @NonNls
    protected String getActionId() {
      return "Maven.RunGoal";
    }

    @Nullable
    @NonNls
    protected String getMenuId() {
      return "Maven.GoalMenu";
    }
  }

  public class LifecycleNode extends GoalsGroupNode {
    public LifecycleNode(ProjectNode parent) {
      super(parent);

      for (String goal : PHASES) {
        myGoalNodes.add(new StandardGoalNode(this, goal));
      }
      updateNameAndDescription();
      setIcons(CLOSED_PHASES_ICON, OPEN_PHASES_ICON);
    }

    @Override
    protected void updateNameAndDescription() {
      setNameAndDescription(NavigatorBundle.message("node.lifecycle"), null);
    }
  }

  public class StandardGoalNode extends GoalNode {
    public StandardGoalNode(GoalsGroupNode parent, String goal) {
      super(parent, goal, goal);
    }

    public boolean isVisible() {
      return super.isVisible() && (!myProjectsNavigator.getShowBasicPhasesOnly() || BASIC_PHASES.contains(getName()));
    }
  }

  public class ProfilesNode extends GroupNode {
    private final List<ProfileNode> myProfileNodes = new ArrayList<ProfileNode>();

    public ProfilesNode(CustomNode parent) {
      super(parent);
      updateNameAndDescription();
      setIcons(CLOSED_PROFILES_ICON, OPEN_PROFILES_ICON);
    }

    @Override
    protected void updateNameAndDescription() {
      setNameAndDescription(NavigatorBundle.message("node.profiles"), null);
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      return myProfileNodes;
    }

    public void updateProfiles() {
      List<String> allProfiles = myProjectsManager.getAvailableProfiles();
      for (ProfileNode each : new ArrayList<ProfileNode>(myProfileNodes)) {
        if (!allProfiles.contains(each.getProfileName())) myProfileNodes.remove(each);
      }
      for (String each : allProfiles) {
        if (!hasNodeFor(each)) {
          insertSorted(myProfileNodes, new ProfileNode(this, each));
        }
      }
      setActiveProfiles(myProjectsManager.getActiveProfiles());
    }

    private boolean hasNodeFor(String profileName) {
      for (ProfileNode each : myProfileNodes) {
        if (each.getProfileName().equals(profileName)) return true;
      }
      return false;
    }

    public void setActiveProfiles(List<String> activeProfiles) {
      for (ProfileNode each : myProfileNodes) {
        each.setActive(activeProfiles.contains(each.getProfileName()));
      }
    }
  }

  public class ProfileNode extends CustomNode {
    private final String myProfileName;
    private boolean isActive;

    public ProfileNode(ProfilesNode parent, String profileName) {
      super(parent);
      myProfileName = profileName;
      updateNameAndDescription();
    }

    @Override
    protected void updateNameAndDescription() {
      setNameAndDescription(myProfileName, null);
    }

    public String getProfileName() {
      return myProfileName;
    }

    @Nullable
    @NonNls
    protected String getActionId() {
      return "Maven.ToggleProfile";
    }

    public boolean isActive() {
      return isActive;
    }

    private void setActive(boolean active) {
      isActive = active;
    }
  }

  public class PluginsNode extends GroupNode {
    private final List<PluginNode> myPluginNodes = new ArrayList<PluginNode>();

    public PluginsNode(ProjectNode parent) {
      super(parent);
      updateNameAndDescription();
      setIcons(CLOSED_PLUGINS_ICON, OPEN_PLUGINS_ICON);
    }

    @Override
    protected void updateNameAndDescription() {
      setNameAndDescription(NavigatorBundle.message("node.plugins"), null);
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      return myPluginNodes;
    }

    public void updatePlugins(MavenProject mavenProject) {
      List<MavenPlugin> plugins = mavenProject.getPlugins();
      for (PluginNode each : new ArrayList<PluginNode>(myPluginNodes)) {
        if (!plugins.contains(each.getPlugin())) myPluginNodes.remove(each);
      }
      for (MavenPlugin each : plugins) {
        if (!hasNodeFor(each)) {
          insertSorted(myPluginNodes, new PluginNode(this, each));
        }
      }
    }

    private boolean hasNodeFor(MavenPlugin plugin) {
      for (PluginNode each : myPluginNodes) {
        if (each.getPlugin().equals(plugin)) {
          return true;
        }
      }
      return false;
    }

    public void updateShortcuts() {
      for (PluginNode each : myPluginNodes) {
        each.updateShortcuts();
      }
    }
  }

  public class PluginNode extends GoalsGroupNode {
    private final MavenPlugin myPlugin;
    private MavenPluginInfo myPluginInfo;

    public PluginNode(PluginsNode parent, MavenPlugin plugin) {
      super(parent);
      myPlugin = plugin;

      updateNameAndDescription();
      setUniformIcon(PLUGIN_ICON);
    }

    @Override
    protected void updateNameAndDescription() {
      boolean hadPluginInfo = myPluginInfo != null;

      myPluginInfo = MavenArtifactUtil.readPluginInfo(myProjectsManager.getLocalRepository(), myPlugin.getMavenId());
      if (myPluginInfo == null) {
        setNameAndDescription(myPlugin.getMavenId().toString(), null);
      }
      else {
        setNameAndDescription(myPluginInfo.getGoalPrefix(), null, getPlugin().toString());
      }
      setNodeErrorLevel(myPluginInfo == null ? ErrorLevel.WARNING : ErrorLevel.NONE);

      // there is no need to update goals since plugins do not change
      if (hadPluginInfo || myPluginInfo == null) return;

      for (MavenPluginInfo.Mojo mojo : myPluginInfo.getMojos()) {
        myGoalNodes.add(new PluginGoalNode(this, mojo.getQualifiedGoal(), mojo.getDisplayName()));
      }
    }

    public MavenPlugin getPlugin() {
      return myPlugin;
    }
  }

  public class PluginGoalNode extends GoalNode {
    public PluginGoalNode(PluginNode parent, String goal, String displayName) {
      super(parent, goal, displayName);
      setUniformIcon(PLUGIN_GOAL_ICON);
    }
  }
}