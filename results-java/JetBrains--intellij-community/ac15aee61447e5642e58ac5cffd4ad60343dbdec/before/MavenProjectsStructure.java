package org.jetbrains.idea.maven.navigator;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiManager;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.embedder.MavenEmbedderFactory;
import org.jetbrains.idea.maven.tasks.MavenTasksManager;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.utils.IdeaAPIHelper;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;
import org.jetbrains.idea.maven.utils.MavenId;
import org.jetbrains.idea.maven.utils.MavenPluginInfo;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.InputEvent;
import java.net.URL;
import java.util.*;
import java.util.List;

public abstract class MavenProjectsStructure extends SimpleTreeStructure {
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
  protected final Project myProject;
  protected final MavenProjectsManager myProjectsManager;
  protected final MavenTasksManager myTasksManager;

  protected final RootNode myRoot = new RootNode();

  protected final Collection<String> myStandardPhases = MavenEmbedderFactory.getStandardPhasesList();
  protected final Collection<String> myStandardGoals = MavenEmbedderFactory.getStandardGoalsList();

  public MavenProjectsStructure(Project project,
                            MavenProjectsManager projectsManager,
                            MavenTasksManager tasksManager) {
    myProject = project;
    myProjectsManager = projectsManager;
    myTasksManager = tasksManager;
  }

  public Object getRootElement() {
    return myRoot;
  }

  protected abstract MavenProjectsNavigatorSettings getTreeViewSettings();

  protected abstract void updateTreeFrom(@Nullable SimpleNode node);

  private boolean shouldDisplay(CustomNode node) {
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

  private static final Comparator<SimpleNode> nodeComparator = new Comparator<SimpleNode>() {
    public int compare(SimpleNode o1, SimpleNode o2) {
      if (o1 instanceof ProfilesNode) return -1;
      if (o2 instanceof ProfilesNode) return 1;
      return getRepr(o1).compareToIgnoreCase(getRepr(o2));
    }

    private String getRepr(SimpleNode node) {
      return node.getName();
    }
  };

  private static <T extends SimpleNode> void insertSorted(List<T> list, T newObject) {
    int pos = Collections.binarySearch(list, newObject, nodeComparator);
    list.add(pos >= 0 ? pos : -pos - 1, newObject);
  }

  public static List<SimpleNode> getSelectedNodes(SimpleTree tree) {
    List<SimpleNode> nodes = new ArrayList<SimpleNode>();
    TreePath[] treePaths = tree.getSelectionPaths();
    if (treePaths != null) {
      for (TreePath treePath : treePaths) {
        nodes.add(tree.getNodeFor(treePath));
      }
    }
    return nodes;
  }

  public static <T extends SimpleNode> List<T> getSelectedNodes(SimpleTree tree, Class<T> aClass) {
    final List<T> filtered = new ArrayList<T>();
    for (SimpleNode node : getSelectedNodes(tree)) {
      if ((aClass != null) && (!aClass.isInstance(node))) {
        filtered.clear();
        break;
      }
      //noinspection unchecked
      filtered.add((T)node);
    }
    return filtered;
  }

  @Nullable
  public static PomNode getCommonParent(Collection<? extends CustomNode> goalNodes) {
    PomNode parent = null;
    for (CustomNode node : goalNodes) {
      PomNode nextParent = node.getParent(PomNode.class);
      if (parent == null) {
        parent = nextParent;
      }
      else if (parent != nextParent) {
        return null;
      }
    }
    return parent;
  }

  public void selectNode(SimpleTreeBuilder builder, SimpleNode node) {
    builder.select(node, null);
  }

  protected String formatHtmlImage(URL url) {
    return "<img src=\"" + url + "\"> ";
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

    @Override
    public NodeDescriptor getParentDescriptor() {
      return myStructuralParent;
    }

    public <T extends CustomNode> T getParent(Class<T> aClass) {
      CustomNode node = this;
      while (true) {
        node = node.myStructuralParent;
        if (node == null || aClass.isInstance(node)) {
          //noinspection unchecked,ConstantConditions
          return (T)node;
        }
      }
    }

    public boolean isVisible() {
      for (CustomNode each : getStructuralChildren()) {
        if (each.isVisible()) return true;
      }
      return shouldDisplay(this);
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

    public ErrorLevel getNodeErrorLevel() {
      return myNodeErrorLevel;
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

    void updateSubTree() {
      updateTreeFrom(this);
    }

    public void handleDoubleClickOrEnter(final SimpleTree tree, final InputEvent inputEvent) {
      final String actionId = getActionId();
      if (actionId != null) {
        IdeaAPIHelper.executeAction(actionId, inputEvent);
      }
    }
  }

  public abstract class PomGroupNode extends CustomNode {
    public final List<PomNode> pomNodes = new ArrayList<PomNode>();

    public PomGroupNode(CustomNode parent) {
      super(parent);
    }

    public boolean isVisible() {
      return !pomNodes.isEmpty() && super.isVisible();
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      return pomNodes;
    }

    protected boolean addUnderExisting(final PomNode newNode) {
      for (PomNode node : pomNodes) {
        if (node.isAncestor(newNode)) {
          node.addNestedPom(newNode);
          return true;
        }
      }
      return false;
    }

    public boolean addUnder(PomNode newNode) {
      if (getTreeViewSettings().groupStructurally) {
        if (addUnderExisting(newNode)) {
          return true;
        }
      }

      for (PomNode child : removeChildren(newNode, new ArrayList<PomNode>())) {
        newNode.addNestedPom(child);
      }

      add(newNode);
      return true;
    }

    protected Collection<PomNode> removeChildren(final PomNode parent, final Collection<PomNode> children) {
      return removeOwnChildren(parent, children);
    }

    protected final Collection<PomNode> removeOwnChildren(final PomNode parent, final Collection<PomNode> children) {
      for (PomNode node : pomNodes) {
        if (parent.isAncestor(node)) {
          children.add(node);
        }
      }
      pomNodes.removeAll(children);

      resetChildrenCaches();
      return children;
    }

    protected void add(PomNode pomNode) {
      boolean wasVisible = isVisible();

      pomNode.setStructuralParent(this);
      insertSorted(pomNodes, pomNode);

      resetChildrenCaches();
      updateGroupNode(wasVisible);
    }

    public void remove(PomNode pomNode) {
      boolean wasVisible = isVisible();

      pomNode.setStructuralParent(null);
      pomNodes.remove(pomNode);
      adoptOrphans(pomNode);

      resetChildrenCaches();
      updateGroupNode(wasVisible);
    }

    public void reinsert(PomNode pomNode) {
      pomNodes.remove(pomNode);
      insertSorted(pomNodes, pomNode);
    }

    private void adoptOrphans(final PomNode pomNode) {
      PomGroupNode newParent = getNewParentForOrphans();
      newParent.merge(pomNode.modulePomsNode);
      newParent.merge(pomNode.nonModulePomsNode);
      if (newParent != this) {
        updateTreeFrom(newParent);
      }
    }

    protected PomGroupNode getNewParentForOrphans() {
      return this;
    }

    private void merge(PomGroupNode groupNode) {
      for (PomNode pomNode : groupNode.pomNodes) {
        insertSorted(pomNodes, pomNode);
      }
      groupNode.clear();
      resetChildrenCaches();
    }

    public void clear() {
      pomNodes.clear();
      resetChildrenCaches();
    }

    private void updateGroupNode(boolean wasVisible) {
      if (wasVisible && isVisible()) {
        updateSubTree();
      }
      else {
        updateTreeFrom(getVisibleParent());
      }
    }

    @NotNull
    protected abstract CustomNode getVisibleParent();
  }

  public class RootNode extends PomGroupNode {
    public ProfilesNode profilesNode;

    public RootNode() {
      super(null);
      profilesNode = new ProfilesNode(this);
      updateNameAndDescription();
    }

    protected void updateNameAndDescription() {
      setNameAndDescription(NavigatorBundle.message("node.root"), null);
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      List<CustomNode> result = new ArrayList<CustomNode>();
      result.add(profilesNode);
      result.addAll(pomNodes);
      return result;
    }

    @NotNull
    protected CustomNode getVisibleParent() {
      return myRoot;
    }

    public void rebuild(final Collection<PomNode> allPomNodes) {
      pomNodes.clear();

      updateProfileNodes();

      for (PomNode node : allPomNodes) {
        node.unlinkNested();
      }
      for (PomNode pomNode : allPomNodes) {
        addToStructure(pomNode);
      }
    }

    void addToStructure(PomNode pomNode) {
      if (!getTreeViewSettings().showIgnored && myProjectsManager.isIgnored(pomNode.myMavenProject)) {
        return;
      }
      addUnder(pomNode);
    }

    public void updateProfileNodes() {
      profilesNode.clear();
      for (String each : myProjectsManager.getAvailableProfiles()) {
        profilesNode.add(each);
      }
      profilesNode.updateActive(myProjectsManager.getActiveProfiles(), true);
      profilesNode.updateSubTree();
    }

    public void setActiveProfiles(List<String> profiles) {
      profilesNode.updateActive(profiles, false);
    }
  }

  public class PomNode extends CustomNode {
    final private MavenProject myMavenProject;
    private String savedPath = "";
    private String actionIdPrefix = "";

    private LifecycleNode lifecycleNode;
    private PluginsNode pluginsNode;
    public NestedPomsNode modulePomsNode;
    public NestedPomsNode nonModulePomsNode;

    public PomNode(MavenProject mavenProject) {
      super(null);
      this.myMavenProject = mavenProject;

      lifecycleNode = new LifecycleNode(this);
      pluginsNode = new PluginsNode(this);
      modulePomsNode = new ModulePomsNode(this);
      nonModulePomsNode = new NonModulePomsNode(this);

      modulePomsNode.sibling = nonModulePomsNode;
      nonModulePomsNode.sibling = modulePomsNode;

      updateNode();
      setUniformIcon(MAVEN_PROJECT_ICON);
    }

    public VirtualFile getFile() {
      return myMavenProject.getFile();
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      List<CustomNode> result = new ArrayList<CustomNode>();
      result.add(lifecycleNode);
      result.add(pluginsNode);
      result.add(modulePomsNode);
      result.add(nonModulePomsNode);
      return result;
    }

    @Nullable
    @NonNls
    protected String getMenuId() {
      return "Maven.PomMenu";
    }

    public String getProjectName() {
      return myMavenProject.getDisplayName();
    }

    public MavenProject getMavenProject() {
      return myMavenProject;
    }

    private VirtualFile getDirectory() {
      //noinspection ConstantConditions
      return myMavenProject.getDirectoryFile();
    }

    public boolean isAncestor(PomNode that) {
      return VfsUtil.isAncestor(getDirectory(), that.getDirectory(), true);
    }

    @Nullable
    public Navigatable getNavigatable() {
      return PsiManager.getInstance(MavenProjectsStructure.this.myProject).findFile(myMavenProject.getFile());
    }

    private void updateNode() {
      updateErrorLevel();
      updateNameAndDescription();

      savedPath = myMavenProject.getFile().getPath();
      actionIdPrefix = myTasksManager.getActionId(savedPath, null);

      lifecycleNode.updateGoals();
      createPluginsNode();
      regroupNested();
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
      for (PomNode each : modulePomsNode.pomNodes) {
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
          desc.append("<td valign=top>" + formatHtmlImage(critical ? ERROR_ICON_URL : WARNING_ICON_URL) + "</td>");
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

    private void createPluginsNode() {
      pluginsNode.clear();
      for (MavenPlugin each : myMavenProject.getPlugins()) {
        if (!hasPlugin(each)) {
          pluginsNode.add(new PluginNode(this, each.getMavenId()));
        }
      }
    }

    public boolean hasPlugin(MavenPlugin plugin) {
      for (PluginNode node : pluginsNode.pluginNodes) {
        if (node.getId().matches(plugin.getMavenId())) {
          return true;
        }
      }
      return false;
    }

    public void addNestedPom(PomNode child) {
      if (!modulePomsNode.addUnder(child)) {
        nonModulePomsNode.addUnder(child);
      }
    }

    void onFileUpdate() {
      final String oldName = getName();
      final String oldPath = savedPath;

      updateNode();

      if (!oldPath.equals(savedPath)) {
        removeFromParent();
        myRoot.addToStructure(this);
      }
      else if (!oldName.equals(getName())) {
        PomGroupNode groupNode = getParent(PomGroupNode.class);
        groupNode.reinsert(this);
        updateTreeFrom(getVisibleParent());
      }
      else {
        updateSubTree();
      }
    }

    private CustomNode getVisibleParent() {
      return getParent(getTreeViewSettings().groupStructurally
                       ? NonModulePomsNode.class
                       : RootNode.class);
    }

    void removeFromParent() {
      // parent can be null if we are trying to remove an excluded node
      // thus not present in structure and has no parent
      PomGroupNode parent = getParent(PomGroupNode.class);
      if (parent != null) parent.remove(this);
    }

    public void unlinkNested() {
      modulePomsNode.clear();
      nonModulePomsNode.clear();
    }

    public boolean containsAsModule(PomNode node) {
      MavenProjectsManager m = MavenProjectsManager.getInstance(MavenProjectsStructure.this.myProject);
      return m.isModuleOf(myMavenProject, node.myMavenProject);
    }

    public void setIgnored(boolean on) {
      updateNameAndDescription();
      if (getTreeViewSettings().showIgnored) {
        updateSubTree();
      }
      else {
        if (on) {
          removeFromParent();
        }
        else {
          myRoot.addToStructure(this);
        }
      }
    }

    @Nullable
    public GoalNode findGoalNode(final String goalName) {
      for (GoalNode goalNode : collectGoalNodes()) {
        if (goalNode.getGoal().equals(goalName)) {
          return goalNode;
        }
      }

      return null;
    }

    public void updateShortcuts(@Nullable String actionId) {
      if (actionId == null || actionId.startsWith(actionIdPrefix)) {
        boolean update = false;
        for (GoalNode goalNode : collectGoalNodes()) {
          update = goalNode.updateShortcut(actionId) || update;
        }
        if (update) {
          updateSubTree();
        }
      }
    }

    private Collection<GoalNode> collectGoalNodes() {
      Collection<GoalNode> goalNodes = new ArrayList<GoalNode>(lifecycleNode.goalNodes);

      goalNodes.addAll(lifecycleNode.goalNodes);

      for (PluginNode pluginNode : pluginsNode.pluginNodes) {
        goalNodes.addAll(pluginNode.goalNodes);
      }

      return goalNodes;
    }

    public void regroupNested() {
      regroupMisplaced(modulePomsNode, false);
      regroupMisplaced(nonModulePomsNode, true);
    }

    private void regroupMisplaced(final NestedPomsNode src, final boolean misplacedFlag) {
      Collection<PomNode> misplaced = new ArrayList<PomNode>();
      for (PomNode node : src.pomNodes) {
        if (containsAsModule(node) == misplacedFlag) {
          misplaced.add(node);
        }
      }
      for (PomNode node : misplaced) {
        src.pomNodes.remove(node);
        addNestedPom(node);
      }
      if (!misplaced.isEmpty()) {
        updateSubTree();
      }
    }
  }

  public abstract class NestedPomsNode extends PomGroupNode {
    private PomGroupNode sibling;

    public NestedPomsNode(PomNode parent) {
      super(parent);
      setIcons(CLOSED_MODULES_ICON, OPEN_MODULES_ICON);
    }

    public boolean isVisible() {
      return getTreeViewSettings().groupStructurally && super.isVisible();
    }

    @NotNull
    protected CustomNode getVisibleParent() {
      return getParent(PomNode.class);
    }

    protected Collection<PomNode> removeChildren(final PomNode node, final Collection<PomNode> children) {
      super.removeChildren(node, children);
      if (sibling != null) {
        sibling.removeOwnChildren(node, children);
      }
      return children;
    }
  }

  public class ModulePomsNode extends NestedPomsNode {
    public ModulePomsNode(PomNode parent) {
      super(parent);
      updateNameAndDescription();
    }

    @Override
    protected void updateNameAndDescription() {
      setNameAndDescription(NavigatorBundle.message("node.modules"), null);
    }

    public boolean addUnder(PomNode newNode) {
      if (getParent(PomNode.class).containsAsModule(newNode)) {
        add(newNode);
        return true;
      }
      return addUnderExisting(newNode);
    }

    protected PomGroupNode getNewParentForOrphans() {
      return getParent(PomNode.class).nonModulePomsNode;
    }
  }

  public class NonModulePomsNode extends NestedPomsNode {
    public NonModulePomsNode(PomNode parent) {
      super(parent);
      updateNameAndDescription();
    }

    @Override
    protected void updateNameAndDescription() {
      setNameAndDescription(NavigatorBundle.message("node.nested.poms"), null);
    }
  }

  public abstract class GoalGroupNode extends CustomNode {
    final List<GoalNode> goalNodes = new ArrayList<GoalNode>();
    private final PomNode pomNode;

    public GoalGroupNode(PomNode parent) {
      super(parent);
      pomNode = parent;
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      return goalNodes;
    }
  }

  public abstract class GoalNode extends CustomNode {
    private final String goal;
    private final PomNode pomNode;
    private final String displayName;
    private String actionId;

    public GoalNode(GoalGroupNode parent, String goal, String displayName) {
      super(parent);
      this.goal = goal;
      this.displayName = displayName;
      pomNode = parent.pomNode;
      updateNameAndDescription();
      setUniformIcon(PHASE_ICON);
    }

    @Override
    protected void updateNameAndDescription() {
      actionId = pomNode.actionIdPrefix + goal;
      String hint = myTasksManager.getActionDescription(pomNode.savedPath, goal);
      setNameAndDescription(displayName, null, hint);
    }

    @Override
    protected SimpleTextAttributes getPlainAttributes() {
      if (goal.equals(pomNode.myMavenProject.getDefaultGoal())) {
        return new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, getColor());
      }
      return super.getPlainAttributes();
    }

    public boolean updateShortcut(@Nullable String actionId) {
      if (actionId == null || actionId.equals(this.actionId)) {
        updateNameAndDescription();
        return true;
      }
      return false;
    }

    public String getGoal() {
      return goal;
    }

    @Nullable
    @NonNls
    protected String getActionId() {
      //return "RunClass";
      return "Maven.RunGoal";
    }

    @Nullable
    @NonNls
    protected String getMenuId() {
      return "Maven.GoalMenu";
    }
  }

  public class LifecycleNode extends GoalGroupNode {
    public LifecycleNode(PomNode parent) {
      super(parent);
      updateNameAndDescription();
      setIcons(CLOSED_PHASES_ICON, OPEN_PHASES_ICON);
    }

    @Override
    protected void updateNameAndDescription() {
      setNameAndDescription(NavigatorBundle.message("node.lifecycle"), null);
    }

    public void updateGoals() {
      goalNodes.clear();
      for (String goal : myStandardGoals) {
        goalNodes.add(new StandardGoalNode(this, goal));
      }
    }
  }

  public class StandardGoalNode extends GoalNode {
    public StandardGoalNode(GoalGroupNode parent, String goal) {
      super(parent, goal, goal);
    }

    public boolean isVisible() {
      return (!getTreeViewSettings().filterStandardPhases
              || myStandardPhases.contains(getName())) && super.isVisible();
    }
  }

  public class ProfilesNode extends CustomNode {
    final List<ProfileNode> profileNodes = new ArrayList<ProfileNode>();

    public ProfilesNode(final CustomNode parent) {
      super(parent);
      updateNameAndDescription();
      setIcons(CLOSED_PROFILES_ICON, OPEN_PROFILES_ICON);
    }

    @Override
    protected void updateNameAndDescription() {
      setNameAndDescription(NavigatorBundle.message("node.profiles"), null);
    }

    public boolean isVisible() {
      return !profileNodes.isEmpty() && super.isVisible();
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      return profileNodes;
    }

    public void clear() {
      profileNodes.clear();
    }

    public void add(final String profileName) {
      insertSorted(profileNodes, new ProfileNode(this, profileName));
    }

    public void updateActive(final Collection<String> activeProfiles, boolean force) {
      for (ProfileNode node : profileNodes) {
        final boolean active = activeProfiles.contains(node.getProfile());
        if (active != node.isActive() || force) {
          node.setActive(active);
        }
      }
    }
  }

  public class ProfileNode extends CustomNode {
    private final String name;
    private boolean active;

    public ProfileNode(final ProfilesNode parent, String name) {
      super(parent);
      this.name = name;
      updateNameAndDescription();
    }

    @Override
    protected void updateNameAndDescription() {
      setNameAndDescription(name, null);
    }

    public String getProfile() {
      return name;
    }

    @Nullable
    @NonNls
    protected String getActionId() {
      return "Maven.ToggleProfile";
    }

    @Nullable
    @NonNls
    protected String getMenuId() {
      return "Maven.ProfileMenu";
    }

    public boolean isActive() {
      return active;
    }

    private void setActive(final boolean active) {
      this.active = active;
    }
  }

  public class PluginsNode extends CustomNode {
    final List<PluginNode> pluginNodes = new ArrayList<PluginNode>();

    public PluginsNode(final PomNode parent) {
      super(parent);
      updateNameAndDescription();
      setIcons(CLOSED_PLUGINS_ICON, OPEN_PLUGINS_ICON);
    }

    @Override
    protected void updateNameAndDescription() {
      setNameAndDescription(NavigatorBundle.message("node.plugins"), null);
    }

    public boolean isVisible() {
      return !pluginNodes.isEmpty() && super.isVisible();
    }

    protected List<? extends CustomNode> getStructuralChildren() {
      return pluginNodes;
    }

    public void clear() {
      pluginNodes.clear();
    }

    public void add(PluginNode node) {
      insertSorted(pluginNodes, node);
    }
  }

  public class PluginNode extends GoalGroupNode {
    private final MavenId myId;
    private MavenPluginInfo myPluginInfo;

    public PluginNode(PomNode parent, final MavenId id) {
      super(parent);
      myId = id;

      myPluginInfo = MavenArtifactUtil.readPluginInfo(myProjectsManager.getLocalRepository(), myId);
      setNodeErrorLevel(myPluginInfo == null ? ErrorLevel.WARNING : ErrorLevel.NONE);
      updateNameAndDescription();
      setUniformIcon(PLUGIN_ICON);

      if (myPluginInfo != null) {
        for (MavenPluginInfo.Mojo mojo : myPluginInfo.getMojos()) {
          goalNodes.add(new PluginGoalNode(this, mojo.getQualifiedGoal(), mojo.getDisplayName()));
        }
      }
    }

    @Override
    protected void updateNameAndDescription() {
      if (myPluginInfo == null) {
        setNameAndDescription(myId.toString(), null);
      }
      else {
        setNameAndDescription(myPluginInfo.getGoalPrefix(), null, getId().toString());
      }
    }

    public MavenId getId() {
      return myId;
    }
  }

  public class PluginGoalNode extends GoalNode {
    public PluginGoalNode(PluginNode parent, String goal, String displayName) {
      super(parent, goal, displayName);
      setUniformIcon(PLUGIN_GOAL_ICON);
    }
  }
}