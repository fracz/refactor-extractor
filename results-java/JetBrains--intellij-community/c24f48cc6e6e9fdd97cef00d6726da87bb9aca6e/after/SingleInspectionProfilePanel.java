/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.profile.codeInspection.ui;

import com.intellij.CommonBundle;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.daemon.impl.SeverityRegistrar;
import com.intellij.codeInspection.InspectionProfile;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.ModifiableModel;
import com.intellij.codeInspection.ex.*;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ide.TreeExpander;
import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.ide.ui.search.SearchableOptionsRegistrar;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.packageDependencies.ui.TreeExpansionMonitor;
import com.intellij.profile.ProfileManager;
import com.intellij.profile.codeInspection.InspectionProfileManager;
import com.intellij.profile.codeInspection.SeverityProvider;
import com.intellij.profile.codeInspection.ui.actions.AddScopeAction;
import com.intellij.profile.codeInspection.ui.actions.DeleteScopeAction;
import com.intellij.profile.codeInspection.ui.actions.MoveScopeAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.List;

/**
 * User: anna
 * Date: 31-May-2006
 */
public class SingleInspectionProfilePanel extends JPanel implements DataProvider {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.ex.InspectionToolsPanel");
  @NonNls private static final String INSPECTION_FILTER_HISTORY = "INSPECTION_FILTER_HISTORY";
  private static final String UNDER_CONSTRUCTION = InspectionsBundle.message("inspection.tool.description.under.construction.text");
  private final Map<Descriptor, List<Descriptor>> myDescriptors = new HashMap<Descriptor, List<Descriptor>>();
  private InspectionProfileImpl mySelectedProfile;
  private JEditorPane myBrowser;
  private JPanel myOptionsPanel;
  private final UserActivityWatcher myUserActivityWatcher = new UserActivityWatcher();
  private final JPanel myInspectionProfilePanel = new JPanel(new BorderLayout());
  private FilterComponent myProfileFilter;
  private final InspectionConfigTreeNode myRoot = new InspectionConfigTreeNode(InspectionsBundle.message("inspection.root.node.title"), null, false, false,
                                                   false);
  private final Alarm myAlarm = new Alarm();
  private boolean myModified = false;
  private Tree myTree;
  private TreeExpander myTreeExpander;
  private String myInitialProfile;
  @NonNls private static final String EMPTY_HTML = "<html><body></body></html>";
  private boolean myIsInRestore = false;

  public static final DataKey<SingleInspectionProfilePanel> PANEL_KEY = DataKey.create(SingleInspectionProfilePanel.class.getName());

  public SingleInspectionProfilePanel(final String inspectionProfileName, final ModifiableModel profile) {
    super(new BorderLayout());
    mySelectedProfile = (InspectionProfileImpl)profile;
    myInitialProfile = inspectionProfileName;
    add(createInspectionProfileSettingsPanel(), BorderLayout.CENTER);
    myUserActivityWatcher.addUserActivityListener(new UserActivityListener() {
      public void stateChanged() {
        //invoke after all other listeners
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (mySelectedProfile == null) return; //panel was disposed
            updateProperSettingsForSelection();
            wereToolSettingsModified();
          }
        });
      }
    });
    myUserActivityWatcher.register(myOptionsPanel);
    updateSelectedProfileState();
  }

  private void updateSelectedProfileState() {
    if (mySelectedProfile == null) return;
    restoreTreeState();
    repaintTableData();
    final TreePath selectionPath = myTree.getSelectionPath();
    if (selectionPath != null) {
      TreeUtil.showRowCentered(myTree, myTree.getRowForPath(selectionPath), false);
    }
  }


  private void wereToolSettingsModified() {
    for (Descriptor key : myDescriptors.keySet()) {
      for (Descriptor descriptor : myDescriptors.get(key)) {
        if (mySelectedProfile.getErrorLevel(descriptor.getKey(), descriptor.getScope()) != descriptor.getLevel()) {
          myModified = true;
          return;
        }
        InspectionProfileEntry tool = descriptor.getTool();
        if (tool != null) {
          if (mySelectedProfile.isToolEnabled(descriptor.getKey())) {
            Element oldConfig = descriptor.getConfig();
            if (oldConfig == null) continue;
            @NonNls Element newConfig = new Element("options");
            try {
              tool.writeSettings(newConfig);
            }
            catch (WriteExternalException e) {
              LOG.error(e);
            }
            if (!JDOMUtil.areElementsEqual(oldConfig, newConfig)) {
              myAlarm.cancelAllRequests();
              myAlarm.addRequest(new Runnable() {
                public void run() {
                  myTree.repaint();
                }
              }, 300);
              myModified = true;
              return;
            }
          }
        }
      }
    }
    myModified = false;
  }

  private void updateProperSettingsForSelection() {
    final TreePath selectionPath = myTree.getSelectionPath();
    if (selectionPath != null) {
      InspectionConfigTreeNode node = (InspectionConfigTreeNode)selectionPath.getLastPathComponent();
      final Descriptor descriptor = node.getDesriptor();
      if (descriptor != null) {
        final boolean properSetting = mySelectedProfile.isProperSetting(descriptor.getKey());
        if (node.isProperSetting != properSetting) {
          myAlarm.cancelAllRequests();
          myAlarm.addRequest(new Runnable() {
            public void run() {
              myTree.repaint();
            }
          }, 300);
          node.isProperSetting = properSetting;
          updateUpHierarchy(node, (InspectionConfigTreeNode)node.getParent());
        }
      }
    }
  }

  private void initDescriptors() {
    if (mySelectedProfile == null) return;
    myDescriptors.clear();
    List<ScopeToolState> tools = mySelectedProfile.getDefaultStates();
    final InspectionProfile profile = mySelectedProfile;
    final InspectionProfile inspectionProfile = profile != null ? profile : InspectionProfileImpl.getDefaultProfile();
    for (ScopeToolState state : tools) {
      final ArrayList<Descriptor> descriptors = new ArrayList<Descriptor>();
      myDescriptors.put(new Descriptor(state, inspectionProfile), descriptors);
      final List<ScopeToolState> nonDefaultTools = mySelectedProfile.getNonDefaultTools(state.getTool().getShortName());
      if (nonDefaultTools != null) {
        for (ScopeToolState nonDefaultToolState : nonDefaultTools) {
          descriptors.add(new Descriptor(nonDefaultToolState, inspectionProfile));
        }
      }
    }

  }

  private void postProcessModification() {
    wereToolSettingsModified();
    //resetup configs
    for (ScopeToolState state : mySelectedProfile.getAllTools()) {
      state.resetConfigPanel();
    }
    fillTreeData(myProfileFilter.getFilter(), true);
    repaintTableData();
    updateOptionsAndDescriptionPanel(myTree.getSelectionPath());
  }

  @Nullable
  public static ModifiableModel createNewProfile(final int initValue,
                                                 ModifiableModel selectedProfile,
                                                 JPanel parent,
                                                 String profileName) {

    profileName = Messages.showInputDialog(parent, profileName, "Create new inspection profile", Messages.getQuestionIcon());
    if (profileName == null) return null;
    final boolean isLocal = selectedProfile.isLocal();
    final ProfileManager profileManager = selectedProfile.getProfileManager();
    if (ArrayUtil.find(profileManager.getAvailableProfileNames(), profileName) != -1) {
      Messages.showErrorDialog(InspectionsBundle.message("inspection.unable.to.create.profile.message", profileName),
                               InspectionsBundle.message("inspection.unable.to.create.profile.dialog.title"));
      return null;
    }
    InspectionProfileImpl inspectionProfile =
        new InspectionProfileImpl(profileName, InspectionToolRegistrar.getInstance(), profileManager);
      final ModifiableModel profileModifiableModel = inspectionProfile.getModifiableModel();
      if (selectedProfile != null) { //can be null for default or empty profile
        profileModifiableModel.copyFrom(selectedProfile);
      }
      if (initValue == -1) {
        final InspectionProfileEntry[] profileEntries = profileModifiableModel.getInspectionTools(null);
        for (InspectionProfileEntry entry : profileEntries) {
          profileModifiableModel.disableTool(entry.getShortName(), (NamedScope)null);
        }
      }
      else if (initValue == 1) {
        profileModifiableModel.resetToBase();
      }
      profileModifiableModel.setName(profileName);
      profileModifiableModel.setLocal(isLocal);
      return profileModifiableModel;
  }

  public void filterTree(String filter) {
    if (myTree != null) {
      mySelectedProfile.getExpandedNodes().saveVisibleState(myTree);
      fillTreeData(filter, true);
      reloadModel();
      restoreTreeState();
      if (myTree.getSelectionPath() == null) {
        TreeUtil.selectFirstNode(myTree);
      }
    }
  }

  private void reloadModel() {
    try {
      myIsInRestore = true;
      ((DefaultTreeModel)myTree.getModel()).reload();
    }
    finally {
      myIsInRestore = false;
    }

  }

  private void restoreTreeState() {

    try {
      myIsInRestore = true;
      mySelectedProfile.getExpandedNodes().restoreVisibleState(myTree);
    }
    finally {
      myIsInRestore = false;
    }
  }

  private ActionToolbar createTreeToolbarPanel() {
    final CommonActionsManager actionManager = CommonActionsManager.getInstance();

    DefaultActionGroup actions = new DefaultActionGroup();
    actions.add(actionManager.createExpandAllAction(myTreeExpander, myTree));
    actions.add(actionManager.createCollapseAllAction(myTreeExpander, myTree));

    actions.add(new AnAction(CommonBundle.message("button.reset.to.default"), CommonBundle.message("button.reset.to.default"), IconLoader.getIcon("/actions/reset-to-default.png")) {
      {
        registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK)), myTree);
      }
      @Override
      public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(myRoot.isProperSetting);
      }

      @Override
      public void actionPerformed(AnActionEvent e) {
        mySelectedProfile.resetToBase();
        postProcessModification();
      }
    });

    actions.add(new AnAction("Reset to Empty", "Reset to empty", IconLoader.getIcon("/general/reset.png")){

      @Override
      public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(mySelectedProfile != null && mySelectedProfile.isExecutable());
      }

      public void actionPerformed(AnActionEvent e) {
        mySelectedProfile.resetToEmpty();
        postProcessModification();
      }
    });

    actions.addSeparator();
    actions.add(new AddScopeAction(myTree, mySelectedProfile));
    actions.add(new DeleteScopeAction(myTree, mySelectedProfile));
    actions.add(new MoveScopeAction(myTree, mySelectedProfile, "Move Scope Up", IconLoader.getIcon("/actions/moveUp.png"), -1) {
      protected boolean isEnabledFor(int idx, InspectionConfigTreeNode parent) {
        return idx > 0;
      }
    });
    actions.add(new MoveScopeAction(myTree, mySelectedProfile, "Move Scope Down", IconLoader.getIcon("/actions/moveDown.png"), 1) {
      protected boolean isEnabledFor(int idx, InspectionConfigTreeNode parent) {
        return idx < parent.getChildCount() - 2;
      }
    });
    actions.addSeparator();

    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true);
    actionToolbar.setTargetComponent(this);
    return actionToolbar;
  }

  private void repaintTableData() {
    if (myTree != null) {
      mySelectedProfile.getExpandedNodes().saveVisibleState(myTree);
      reloadModel();
      restoreTreeState();
    }
  }

  public void selectInspectionTool(String name) {
    InspectionConfigTreeNode node = null;
    for (int i = 0; i < myRoot.getChildCount(); i++) {
      final InspectionConfigTreeNode child = (InspectionConfigTreeNode)myRoot.getChildAt(i);
      for (int j = 0; j < child.getChildCount(); j++) {
        final InspectionConfigTreeNode childChild = (InspectionConfigTreeNode)child.getChildAt(j);
        final Descriptor descriptor = childChild.getDesriptor();
        if (descriptor != null && descriptor.getKey().toString().equals(name)) {
          node = childChild;
          break;
        }
      }
    }
    if (node != null) {
      TreeUtil.showRowCentered(myTree, myTree.getRowForPath(new TreePath(node.getPath())) - 1, true);//myTree.isRootVisible ? 0 : 1;
      TreeUtil.selectNode(myTree, node);
    }
  }

  private JScrollPane initTreeScrollPane() {

    fillTreeData(null, true);

    final InspectionsConfigTreeRenderer renderer = new InspectionsConfigTreeRenderer(){
      protected String getFilter() {
        return myProfileFilter != null ? myProfileFilter.getFilter() : null;
      }
    };
    myTree = new CheckboxTree(renderer, myRoot) {
      public Dimension getPreferredScrollableViewportSize() {
        Dimension size = super.getPreferredScrollableViewportSize();
        size = new Dimension(size.width + 10, size.height);
        return size;
      }
      public int getToggleClickCount() {
        return -1;
      }

      @Override
      protected void onNodeStateChanged(final CheckedTreeNode node) {
        toggleToolNode((InspectionConfigTreeNode)node);
      }
    };


    myTree.setCellRenderer(renderer);
    myTree.setRootVisible(true);
    myTree.setShowsRootHandles(true);
    UIUtil.setLineStyleAngled(myTree);
    TreeToolTipHandler.install(myTree);
    TreeUtil.installActions(myTree);


    myTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        if (myTree.getSelectionPaths() != null && myTree.getSelectionPaths().length == 1) {
          updateOptionsAndDescriptionPanel(myTree.getSelectionPaths()[0]);
        }
        else {
          initOptionsAndDescriptionPanel();
        }

        if (!myIsInRestore) {
          InspectionProfileImpl selected = mySelectedProfile;
          if (selected != null) {
            InspectionProfileImpl baseProfile = (InspectionProfileImpl)selected.getParentProfile();
            if (baseProfile != null) {
              baseProfile.getExpandedNodes().setSelectionPaths(myTree.getSelectionPaths());
            }
            selected.getExpandedNodes().setSelectionPaths(myTree.getSelectionPaths());
          }
        }

      }
    });


    myTree.addMouseListener(new PopupHandler() {
      public void invokePopup(Component comp, int x, int y) {
        final int[] selectionRows = myTree.getSelectionRows();
        if (selectionRows != null && myTree.getPathForLocation(x, y) != null && Arrays.binarySearch(selectionRows, myTree.getRowForLocation(x, y)) > -1)
        {
          compoundPopup().show(comp, x, y);
        }
      }
    });


    new TreeSpeedSearch(myTree, new Convertor<TreePath, String>() {
      public String convert(TreePath o) {
        final InspectionConfigTreeNode node = (InspectionConfigTreeNode)o.getLastPathComponent();
        final Descriptor descriptor = node.getDesriptor();
        return descriptor != null ? InspectionsConfigTreeComparator.getDisplayTextToSort(descriptor.getText()) : InspectionsConfigTreeComparator
          .getDisplayTextToSort(node.getGroupName());
      }
    });


    myTree.setSelectionModel(new DefaultTreeSelectionModel());

    final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myTree);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    TreeUtil.collapseAll(myTree, 1);
    final Dimension preferredSize = new Dimension(myTree.getPreferredSize().width + 20, scrollPane.getPreferredSize().height);
    scrollPane.setPreferredSize(preferredSize);
    scrollPane.setMinimumSize(preferredSize);

    myTree.addTreeExpansionListener(new TreeExpansionListener() {
      private String getExpandedString(TreePath treePath) {
        final InspectionConfigTreeNode node = (InspectionConfigTreeNode)treePath.getLastPathComponent();
        final Descriptor descriptor = node.getDesriptor();
        if (descriptor != null) {
          return descriptor.getText();
        }
        else {
          return node.getGroupName();
        }
      }

      public void treeCollapsed(TreeExpansionEvent event) {
        InspectionProfileImpl selected = mySelectedProfile;
        String nodeTitle = getExpandedString(event.getPath());
        final InspectionProfileImpl parentProfile = (InspectionProfileImpl)selected.getParentProfile();
        if (parentProfile != null) {
          parentProfile.getExpandedNodes().collapseNode(nodeTitle);
        }
        selected.getExpandedNodes().collapseNode(nodeTitle);
      }

      public void treeExpanded(TreeExpansionEvent event) {
        InspectionProfileImpl selected = mySelectedProfile;
        String nodeTitle = getExpandedString(event.getPath());
        final InspectionProfileImpl parentProfile = (InspectionProfileImpl)selected.getParentProfile();
        if (parentProfile != null) {
          parentProfile.getExpandedNodes().expandNode(nodeTitle);
        }
        selected.getExpandedNodes().expandNode(nodeTitle);
      }
    });

    myTreeExpander = new DefaultTreeExpander(myTree);
    myProfileFilter = new MyFilterComponent();

    return scrollPane;
  }

  private JPopupMenu compoundPopup() {
    final JPopupMenu popup = new JPopupMenu(InspectionsBundle.message("inspection.error.level.popup.menu.title"));
    final SeverityRegistrar severityRegistrar = ((SeverityProvider)mySelectedProfile.getProfileManager()).getOwnSeverityRegistrar();
    TreeSet<HighlightSeverity> severities = new TreeSet<HighlightSeverity>(severityRegistrar);
    severities.add(HighlightSeverity.ERROR);
    severities.add(HighlightSeverity.WARNING);
    severities.add(HighlightSeverity.INFO);
    final Collection<SeverityRegistrar.SeverityBasedTextAttributes> infoTypes =
      severityRegistrar.getRegisteredHighlightingInfoTypes();
    for (SeverityRegistrar.SeverityBasedTextAttributes info : infoTypes) {
      severities.add(info.getSeverity());
    }
    for (HighlightSeverity severity : severities) {
      final HighlightDisplayLevel level = HighlightDisplayLevel.find(severity);
      final JMenuItem item = new JMenuItem(renderSeverity(level.getSeverity()));
      item.setIcon(level.getIcon()); //todo correct position
      item.addActionListener(new LevelSelection(level));
      popup.add(item);
    }
    return popup;
  }

  static String renderSeverity(HighlightSeverity severity) {
    return InspectionsBundle.message("inspection.as", severity.toString().toLowerCase());
  }

  private void toggleToolNode(final InspectionConfigTreeNode toolNode) {
    final Descriptor descriptor = toolNode.getDesriptor();
    if (descriptor!= null) {
      final HighlightDisplayKey key = descriptor.getKey();
      final String toolShortName = key.toString();
      if (toolNode.isChecked()) {
        if (toolNode.getScope() != null){
          mySelectedProfile.enableTool(toolShortName, toolNode.getScope());
        } else {
          if (toolNode.isInspectionNode()) {
            mySelectedProfile.enableTool(toolShortName, (PsiElement)null);
          } else {
            mySelectedProfile.enableTool(toolShortName);
          }
        }
      }
      else {
        if (toolNode.getScope() != null)  {
          mySelectedProfile.disableTool(toolShortName, toolNode.getScope());
        } else {
          if (toolNode.isInspectionNode()) {
            mySelectedProfile.disableTool(toolShortName, (PsiElement)null);
          } else {
            mySelectedProfile.disableTool(toolShortName);
          }
        }
      }
      toolNode.isProperSetting = mySelectedProfile.isProperSetting(key);
      updateUpHierarchy(toolNode, (InspectionConfigTreeNode)toolNode.getParent());
    }
    final TreePath path = new TreePath(toolNode.getPath());
    if (Comparing.equal(myTree.getSelectionPath(), path)) {
      updateOptionsAndDescriptionPanel(path);
    }
  }

  private static void updateUpHierarchy(final InspectionConfigTreeNode node, final InspectionConfigTreeNode parent) {
    if (parent != null) {
      parent.isProperSetting = node.isProperSetting || wasModified(parent);
      updateUpHierarchy(parent, (InspectionConfigTreeNode)parent.getParent());
    }
  }

  private static boolean wasModified(InspectionConfigTreeNode node) {
    for (int i = 0; i < node.getChildCount(); i++) {
      final InspectionConfigTreeNode childNode = (InspectionConfigTreeNode)node.getChildAt(i);
      if (childNode.isProperSetting) {
        return true;
      }
    }
    return false;
  }

  private static boolean isDescriptorAccepted(Descriptor descriptor,
                                              @NonNls String filter,
                                              final boolean forceInclude,
                                              final List<Set<String>> keySetList, final Set<String> quoted) {
    filter = filter.toLowerCase();
    if (StringUtil.containsIgnoreCase(descriptor.getText(), filter)) {
      return true;
    }
    final String[] groupPath = descriptor.getGroup();
    for (String group : groupPath) {
      if (StringUtil.containsIgnoreCase(group, filter)) {
        return true;
      }
    }
    for (String stripped : quoted) {
      if (StringUtil.containsIgnoreCase(descriptor.getText(),stripped)) {
        return true;
      }
      for (String group : groupPath) {
        if (StringUtil.containsIgnoreCase(group,stripped)) {
          return true;
        }
      }
      final String description = descriptor.getTool().loadDescription();
      if (description != null && StringUtil.containsIgnoreCase(description.toLowerCase(), stripped)) {
        if (!forceInclude) return true;
      } else if (forceInclude) return false;
    }
    for (Set<String> keySet : keySetList) {
      if (keySet.contains(descriptor.getKey().toString())) {
        if (!forceInclude) {
          return true;
        }
      }
      else {
        if (forceInclude) {
          return false;
        }
      }
    }
    return forceInclude;
  }

  private void fillTreeData(String filter, boolean forceInclude) {
    if (mySelectedProfile == null) return;
    myRoot.removeAllChildren();
    myRoot.setChecked(false);
    myRoot.isProperSetting = false;
    List<Set<String>> keySetList = new ArrayList<Set<String>>();
    final Set<String> quated = new HashSet<String>();
    if (filter != null && filter.length() > 0) {
      keySetList.addAll(SearchUtil.findKeys(filter, quated));
    }
    for (Descriptor descriptor : myDescriptors.keySet()) {
      final List<Descriptor> descriptors = myDescriptors.get(descriptor);
      if (descriptor.getTool() != null && !(descriptor.getTool()instanceof LocalInspectionToolWrapper)) continue;
      if (filter != null && filter.length() > 0 && !isDescriptorAccepted(descriptor, filter, forceInclude, keySetList, quated)) {
        continue;
      }
      final HighlightDisplayKey key = descriptor.getKey();
      final boolean enabled = mySelectedProfile.isToolEnabled(key);
      final boolean properSetting = mySelectedProfile.isProperSetting(key);
      final InspectionConfigTreeNode node = new InspectionConfigTreeNode(descriptor, null, descriptors.isEmpty(), enabled, properSetting, descriptors.isEmpty());
      getGroupNode(myRoot, descriptor.getGroup(), properSetting).add(node);
      if (!descriptors.isEmpty()) {
        for (Descriptor des : descriptors) {
          node.add(new InspectionConfigTreeNode(des, des.getScope(), des.getState().isEnabled(), properSetting, false));
        }
        node.add(new InspectionConfigTreeNode(descriptor, descriptor.getScope(), true, descriptor.getState().isEnabled(), properSetting, false));
      }
      myRoot.setEnabled(myRoot.isEnabled() || enabled);
      myRoot.isProperSetting |= properSetting;
    }
    if (filter != null && forceInclude && myRoot.getChildCount() == 0) {
      final Set<String> filters = SearchableOptionsRegistrar.getInstance().getProcessedWords(filter);
      if (filters.size() > 1) {
        fillTreeData(filter, false);
      }
    }
    TreeUtil.sort(myRoot, new InspectionsConfigTreeComparator());
  }

  private void updateOptionsAndDescriptionPanel(TreePath path) {
    if (path == null) return;
    final InspectionConfigTreeNode node = (InspectionConfigTreeNode)path.getLastPathComponent();
    final Descriptor descriptor = node.getDesriptor();
    if (descriptor != null) {
      final String description = descriptor.loadDescription();

      if (description != null) {
        // need this in order to correctly load plugin-supplied descriptions
        try {
          myBrowser.read(new StringReader(SearchUtil.markup(description, myProfileFilter.getFilter())), null);
        }
        catch (IOException e2) {
          try {
            //noinspection HardCodedStringLiteral
            myBrowser.read(new StringReader("<html><body><b>" + UNDER_CONSTRUCTION + "</b></body></html>"), null);
          }
          catch (IOException e1) {
            //Can't be
          }
        }

      }
      else {
        try {
          myBrowser.read(new StringReader(EMPTY_HTML), null);
        }
        catch (IOException e1) {
          //Can't be
        }
      }

      myOptionsPanel.removeAll();

      final NamedScope scope = node.getScope();
      if (scope != null || node.isInspectionNode()) {
        final HighlightDisplayKey key = descriptor.getKey();
        final LevelChooser chooser = new LevelChooser(((SeverityProvider)mySelectedProfile.getProfileManager()).getOwnSeverityRegistrar());
        chooser.getComboBox().addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            boolean toUpdate = mySelectedProfile.getErrorLevel(key, scope) != chooser.getLevel();
            mySelectedProfile.setErrorLevel(key, chooser.getLevel(), node.getParent().getIndex(node));
            if (toUpdate) node.isProperSetting = mySelectedProfile.isProperSetting(key);
          }
        });
        chooser.setLevel(mySelectedProfile.getErrorLevel(key, scope));

        final JPanel withSeverity = new JPanel(new GridBagLayout());
        withSeverity.add(new JLabel(InspectionsBundle.message("inspection.severity")), new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 0, 0,
                                                                                                              GridBagConstraints.WEST,
                                                                                                              GridBagConstraints.NONE,
                                                                                                              new Insets(0, 5, 5, 10), 0, 0));
        Dimension dimension = new Dimension(150, chooser.getPreferredSize().height);
        chooser.setPreferredSize(dimension);
        chooser.setMinimumSize(dimension);
        withSeverity.add(chooser, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                         new Insets(0, 0, 5, 0), 0, 0));

        final JComponent comp = descriptor.getState().getAdditionalConfigPanel();
        if (comp != null) {
          withSeverity.add(comp, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                                          new Insets(0, 0, 0, 0), 0, 0));
        }
        else {
          withSeverity.add(new JPanel(), new GridBagConstraints(0, GridBagConstraints.RELATIVE, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                                                new Insets(0, 0, 0, 0), 0, 0));
        }

        myOptionsPanel.add(withSeverity);
      }
      myOptionsPanel.validate();
      GuiUtils.enableChildren(myOptionsPanel, node.isChecked());
    }
    else {
      initOptionsAndDescriptionPanel();
    }
    myOptionsPanel.repaint();
  }

  private void initOptionsAndDescriptionPanel() {
    myOptionsPanel.removeAll();
    myOptionsPanel.add(new JPanel());
    try {
      myBrowser.read(new StringReader(EMPTY_HTML), null);
    }
    catch (IOException e1) {
      //Can't be
    }
    myOptionsPanel.validate();
  }

  private static InspectionConfigTreeNode getGroupNode(InspectionConfigTreeNode root, String[] groupPath, boolean properSetting) {
    InspectionConfigTreeNode currentRoot = root;
    for (final String group : groupPath) {
      currentRoot = getGroupNode(currentRoot, group, properSetting);
    }
    return currentRoot;
  }

  private static InspectionConfigTreeNode getGroupNode(InspectionConfigTreeNode root, String group, boolean properSetting) {
    final int childCount = root.getChildCount();
    for (int i = 0; i < childCount; i++) {
      InspectionConfigTreeNode child = (InspectionConfigTreeNode)root.getChildAt(i);
      if (group.equals(child.getUserObject())) {
        child.isProperSetting |= properSetting;
        return child;
      }
    }
    InspectionConfigTreeNode child = new InspectionConfigTreeNode(group, null, false, properSetting, false);
    root.add(child);
    return child;
  }

  public boolean setSelectedProfileModified(boolean modified) {
    mySelectedProfile.setModified(modified);
    return modified;
  }

  private ModifiableModel getSelectedProfile() {
    return mySelectedProfile;
  }

  private void setSelectedProfile(final ModifiableModel modifiableModel) {
    mySelectedProfile = (InspectionProfileImpl)modifiableModel;
    if (mySelectedProfile != null) {
      myInitialProfile = mySelectedProfile.getName();
    }
    initDescriptors();
    filterTree(myProfileFilter != null ? myProfileFilter.getFilter() : null);
  }

  public Dimension getPreferredSize() {
    return new Dimension(700, -1);
  }

  public void disposeUI() {
    myAlarm.cancelAllRequests();
    myProfileFilter.dispose();
    mySelectedProfile = null;
  }

  private JPanel createInspectionProfileSettingsPanel() {

    myBrowser = new JEditorPane(UIUtil.HTML_MIME, EMPTY_HTML);
    myBrowser.setEditable(false);
    myBrowser.addHyperlinkListener(new BrowserHyperlinkListener());

    initDescriptors();
    fillTreeData(myProfileFilter != null ? myProfileFilter.getFilter() : null, true);

    JPanel descriptionPanel = new JPanel();
    descriptionPanel.setBorder(IdeBorderFactory.createTitledBorder(InspectionsBundle.message("inspection.description.title")));
    descriptionPanel.setLayout(new BorderLayout());
    descriptionPanel.add(ScrollPaneFactory.createScrollPane(myBrowser), BorderLayout.CENTER);

    JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 5));
    rightPanel.add(descriptionPanel);

    JPanel panel1 = new JPanel(new VerticalFlowLayout());
    panel1.setBorder(IdeBorderFactory.createTitledBorder(InspectionsBundle.message("inspection.export.options.panel.title")));
    myOptionsPanel = panel1;
    initOptionsAndDescriptionPanel();
    rightPanel.add(myOptionsPanel);

    final JPanel treePanel = new JPanel(new BorderLayout());
    treePanel.add(initTreeScrollPane(), BorderLayout.CENTER);

    final JPanel northPanel = new JPanel(new BorderLayout());
    northPanel.setBorder(IdeBorderFactory.createEmptyBorder(2, 0, 2, 0));
    northPanel.add(createTreeToolbarPanel().getComponent(), BorderLayout.WEST);
    northPanel.add(myProfileFilter, BorderLayout.EAST);
    treePanel.add(northPanel, BorderLayout.NORTH);

    Splitter splitter = new Splitter(false);
    splitter.setShowDividerControls(false);
    splitter.setFirstComponent(treePanel);
    splitter.setSecondComponent(rightPanel);
    splitter.setProportion((float)treePanel.getPreferredSize().width/getPreferredSize().width);
    splitter.setHonorComponentsMinimumSize(true);

    myInspectionProfilePanel.add(splitter, BorderLayout.CENTER);
    myInspectionProfilePanel.setBorder(IdeBorderFactory.createEmptyBorder(2, 2, 0, 2));
    return myInspectionProfilePanel;
  }

  public boolean isModified() {
    if (myModified) return true;
    if (mySelectedProfile.isChanged()) return true;
    if (!Comparing.strEqual(myInitialProfile, mySelectedProfile.getName())) return true;
    if (descriptorsAreChanged()) {
      return setSelectedProfileModified(true);
    }
    setSelectedProfileModified(false);
    return false;
  }

  public void reset() {
    myModified = false;
    setSelectedProfile(mySelectedProfile);
    final String filter = myProfileFilter.getFilter();
    myProfileFilter.reset();
    myProfileFilter.setSelectedItem(filter);
  }

  public void apply() throws ConfigurationException {
    final ModifiableModel selectedProfile = getSelectedProfile();
    final InspectionProfile parentProfile = selectedProfile.getParentProfile();

    if (InspectionProfileManager.getInstance().getSchemesManager().isShared(selectedProfile)) {
      if (descriptorsAreChanged()) {
        throw new ConfigurationException("Shared profile cannot be modified. Please do \"Save As...\" first.");
      }

    }

    try {
      selectedProfile.commit();
    }
    catch (IOException e) {
      throw new ConfigurationException(e.getMessage());
    }
    setSelectedProfile(parentProfile.getModifiableModel());
    setSelectedProfileModified(false);
    myModified = false;
  }

  private boolean descriptorsAreChanged() {
    for (Descriptor defaultDescriptor : myDescriptors.keySet()) {
      if (mySelectedProfile.isToolEnabled(defaultDescriptor.getKey(), (PsiElement)null) != defaultDescriptor.isEnabled()){
        return true;
      }
      List<Descriptor> descriptors = myDescriptors.get(defaultDescriptor);
      for (Descriptor descriptor : descriptors) {
        if (mySelectedProfile.isToolEnabled(descriptor.getKey(), descriptor.getScope()) != descriptor.isEnabled()) {
          return true;
        }
        if (mySelectedProfile.getErrorLevel(descriptor.getKey(), descriptor.getScope()) != descriptor.getLevel()) {
          return true;
        }
      }

      final List<ScopeToolState> tools = mySelectedProfile.getNonDefaultTools(defaultDescriptor.getKey().toString());
      if (tools.size() != descriptors.size()) {
        return true;
      }
      for (int i = 0, toolsSize = tools.size(); i < toolsSize; i++) {
        final ScopeToolState pair = tools.get(i);
        if (!Comparing.equal(pair.getScope(), descriptors.get(i).getScope())) {
          return true;
        }
      }
    }


    return false;
  }

  public Object getData(@NonNls final String dataId) {
    if (dataId.equals(PANEL_KEY.getName())) {
      return this;
    }
    return null;
  }


  private class LevelSelection implements ActionListener {
    private final HighlightDisplayLevel myLevel;

    public LevelSelection(HighlightDisplayLevel level) {
      myLevel = level;
    }

    public void actionPerformed(ActionEvent e) {
      final int[] rows = myTree.getSelectionRows();
      final boolean showOptionsAndDescriptorPanels = rows != null && rows.length == 1;
      for (int i = 0; rows != null && i < rows.length; i++) {
        final InspectionConfigTreeNode node = (InspectionConfigTreeNode)myTree.getPathForRow(rows[i]).getLastPathComponent();
        final InspectionConfigTreeNode parent = (InspectionConfigTreeNode)node.getParent();
        if (node.getUserObject() instanceof Descriptor) {
          updateErrorLevel(node, showOptionsAndDescriptorPanels);
          updateUpHierarchy(node, parent);
        }
        else {
          node.isProperSetting = false;
          for (int j = 0; j < node.getChildCount(); j++) {
            final InspectionConfigTreeNode child = (InspectionConfigTreeNode)node.getChildAt(j);
            if (child.getUserObject()instanceof List) {     //group node
              updateErrorLevel(child, showOptionsAndDescriptorPanels);
            }
            else {                                               //root node
              child.isProperSetting = false;
              for (int k = 0; k < child.getChildCount(); k++) {
                final InspectionConfigTreeNode descriptorNode = (InspectionConfigTreeNode)child.getChildAt(k);
                if (descriptorNode.getUserObject()instanceof List) {
                  updateErrorLevel(descriptorNode, showOptionsAndDescriptorPanels);
                }
                child.isProperSetting |= descriptorNode.isProperSetting;
              }
            }
            node.isProperSetting |= child.isProperSetting;
          }
          updateUpHierarchy(node, parent);
        }
      }
      if (rows != null && rows.length == 1) {
        updateOptionsAndDescriptionPanel(myTree.getPathForRow(rows[0]));
      }
      else {
        initOptionsAndDescriptionPanel();
      }
      repaintTableData();
    }

    private void updateErrorLevel(final InspectionConfigTreeNode child, final boolean showOptionsAndDescriptorPanels) {
      final HighlightDisplayKey key = child.getDesriptor().getKey();
      mySelectedProfile.setErrorLevel(key, myLevel);
      child.isProperSetting = mySelectedProfile.isProperSetting(key);
      if (showOptionsAndDescriptorPanels) {
        updateOptionsAndDescriptionPanel(new TreePath(child.getPath()));
      }
    }
  }

  private class MyFilterComponent extends FilterComponent {
    private final TreeExpansionMonitor<DefaultMutableTreeNode> myExpansionMonitor = TreeExpansionMonitor.install(myTree);

    public MyFilterComponent() {
      super(INSPECTION_FILTER_HISTORY, 10);
      setHistory(Arrays.asList("\"New in 9\""));
    }

    public void filter() {
      final String filter = getFilter();
      if (filter != null && filter.length() > 0) {
        if (!myExpansionMonitor.isFreeze()) {
          myExpansionMonitor.freeze();
        }
      }
      filterTree(getFilter());
      TreeUtil.expandAll(myTree);
      if (filter == null || filter.length() == 0) {
        TreeUtil.collapseAll(myTree, 0);
        myExpansionMonitor.restore();
      }
    }

    protected void onlineFilter() {
      if (mySelectedProfile == null) return;
      final String filter = getFilter();
      if (filter != null && filter.length() > 0) {
        if (!myExpansionMonitor.isFreeze()) {
          myExpansionMonitor.freeze();
        }
      }
      fillTreeData(filter, true);
      reloadModel();
      TreeUtil.expandAll(myTree);
      if (filter == null || filter.length() == 0) {
        TreeUtil.collapseAll(myTree, 0);
        myExpansionMonitor.restore();
      }
    }
  }

}