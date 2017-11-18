/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.profile.codeInspection.ui;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.packageDependencies.ui.TreeExpansionMonitor;
import com.intellij.profile.ProfileManager;
import com.intellij.profile.codeInspection.SeverityProvider;
import com.intellij.profile.codeInspection.InspectionProfileManager;
import com.intellij.ui.*;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.Tree;
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
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.List;

/**
 * User: anna
 * Date: 31-May-2006
 */
public class SingleInspectionProfilePanel extends JPanel {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.ex.InspectionToolsPanel");
  private static final Icon SHOW_INSPECTION_SETTINGS = IconLoader.getIcon("/objectBrowser/showGlobalInspections.png");
  @NonNls private static final String INSPECTION_FILTER_HISTORY = "INSPECTION_FILTER_HISTORY";
  private static final String UNDER_CONSTRUCTION = InspectionsBundle.message("inspection.tool.description.under.construction.text");
  private final ArrayList<Descriptor> myDescriptors = new ArrayList<Descriptor>();
  private ModifiableModel mySelectedProfile;
  private JEditorPane myBrowser;
  private JPanel myOptionsPanel;
  private final UserActivityWatcher myUserActivityWatcher = new UserActivityWatcher();
  private final JPanel myInspectionProfilePanel = new JPanel(new BorderLayout());
  private FilterComponent myProfileFilter;
  private final MyTreeNode myRoot = new MyTreeNode(InspectionsBundle.message("inspection.root.node.title"), false, false);
  private final Alarm myAlarm = new Alarm();
  private boolean myShowInspections = true;
  private boolean myModified = false;
  private Tree myTree;
  private TreeExpander myTreeExpander;
  private String myInitialProfile;
  @NonNls private static final String EMPTY_HTML = "<html><body></body></html>";

  public SingleInspectionProfilePanel(String inspectionProfileName) {
    this(inspectionProfileName, null);
  }

  public SingleInspectionProfilePanel(final String inspectionProfileName, final ModifiableModel profile) {
    super(new BorderLayout());
    mySelectedProfile = profile;
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
    ((InspectionProfileImpl)mySelectedProfile).getExpandedNodes().restoreVisibleState(myTree);
    repaintTableData();
    final TreePath selectionPath = myTree.getSelectionPath();
    if (selectionPath != null) {
      TreeUtil.showRowCentered(myTree, myTree.getRowForPath(selectionPath), false);
    }
  }


  private void wereToolSettingsModified() {
    for (Descriptor descriptor : myDescriptors) {
      if (mySelectedProfile.getErrorLevel(descriptor.getKey()) != descriptor.getLevel()) {
        myModified = true;
        return;
      }
      InspectionProfileEntry tool = descriptor.getTool();
      if (tool != null) {
        if (descriptor.isEnabled()) {
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
    myModified = false;
  }

  private void updateProperSettingsForSelection() {
    final TreePath selectionPath = myTree.getSelectionPath();
    if (selectionPath != null) {
      MyTreeNode node = (MyTreeNode)selectionPath.getLastPathComponent();
      if (node.getUserObject()instanceof Descriptor) {
        final boolean properSetting = mySelectedProfile.isProperSetting(((Descriptor)node.getUserObject()).getKey());
        if (node.isProperSetting != properSetting) {
          myAlarm.cancelAllRequests();
          myAlarm.addRequest(new Runnable() {
            public void run() {
              myTree.repaint();
            }
          }, 300);
          node.isProperSetting = properSetting;
          updateUpHierarchy(node, (MyTreeNode)node.getParent());
        }
      }
    }
  }

  private void initDescriptors() {
    if (mySelectedProfile == null) return;
    myDescriptors.clear();
    InspectionProfileEntry[] tools = mySelectedProfile.getInspectionTools();
    final InspectionProfile profile = getSavedProfile();
    for (InspectionProfileEntry tool : tools) {
      myDescriptors.add(new Descriptor(tool, profile != null
                                             ? profile
                                             : InspectionProfileImpl.getDefaultProfile())); //fix for new profile - isModified == true by default
    }
  }

  public void resetToBaseAction() {
    mySelectedProfile.resetToBase();
    wereToolSettingsModified();
    //resetup configs
    for (Descriptor descriptor : myDescriptors) {
      descriptor.resetConfigPanel();
    }
    fillTreeData(myProfileFilter.getFilter(), true);
    repaintTableData();
    updateOptionsAndDescriptionPanel(myTree.getSelectionPath());
  }

  @Nullable
  public static ModifiableModel createNewProfile(final int initValue,
                                                 ModifiableModel selectedProfile,
                                                 Project project,
                                                 ProfileManager ideProfileManager,
                                                 ProfileManager currentProfileManager,
                                                 String profileName,
                                                 boolean isLocalProfile) {
    InputDialog dlg = new InputDialog(ideProfileManager != null && !project.isDefault(), profileName, isLocalProfile ? InspectionsBundle
      .message("profile.save.as.project.checkbox.title") : InspectionsBundle
      .message("profile.save.as.ide.checkbox.title"), project);
    dlg.show();
    if (!dlg.isOK()) return null;
    profileName = dlg.getName();
    final boolean isLocal = isLocalProfile && !dlg.isChecked() || !isLocalProfile && dlg.isChecked();
    ProfileManager profileManager = isLocal && ideProfileManager != null ? ideProfileManager : currentProfileManager;
    if (ArrayUtil.find(currentProfileManager.getAvailableProfileNames(), profileName) != -1 ||
        ideProfileManager != null && ArrayUtil.find(ideProfileManager.getAvailableProfileNames(), profileName) != -1) {
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
        final InspectionProfileEntry[] profileEntries = profileModifiableModel.getInspectionTools();
        for (InspectionProfileEntry entry : profileEntries) {
          profileModifiableModel.disableTool(entry.getShortName());
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
      ((InspectionProfileImpl)mySelectedProfile).getExpandedNodes().saveVisibleState(myTree);
      fillTreeData(filter, true);
      ((DefaultTreeModel)myTree.getModel()).reload();
      ((InspectionProfileImpl)mySelectedProfile).getExpandedNodes().restoreVisibleState(myTree);
      if (myTree.getSelectionPath() == null) {
        TreeUtil.selectFirstNode(myTree);
      }
    }
  }

  private ActionToolbar createTreeToolbarPanel() {
    final CommonActionsManager actionManager = CommonActionsManager.getInstance();

    DefaultActionGroup actions = new DefaultActionGroup();
    actions.add(actionManager.createExpandAllAction(myTreeExpander, myTree));
    actions.add(actionManager.createCollapseAllAction(myTreeExpander, myTree));

    actions.add(new ToggleAction(InspectionsBundle.message("inspection.tools.action.show.global.inspections.text"),
                                 InspectionsBundle.message("inspection.tools.action.show.global.inspections.description"),
                                 SHOW_INSPECTION_SETTINGS) {
      public boolean isSelected(AnActionEvent e) {
        return !myShowInspections;
      }

      public void setSelected(AnActionEvent e, boolean state) {
        myShowInspections = !state;
        filterTree(myProfileFilter.getFilter());
      }
    });
    actions.add(new AnAction(InspectionsBundle.message("what.s.new.in.idea.7"),
                             InspectionsBundle.message("what.s.new.in.idea.7.description"), IconLoader.getIcon("/actions/lightning.png")) {
      public void actionPerformed(AnActionEvent e) {
        setFilter("\"New in 7\"");
        myProfileFilter.selectText();
        myProfileFilter.requestFocusInWindow();
      }
    });

    return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true);
  }

  private void repaintTableData() {
    if (myTree != null) {
      ((InspectionProfileImpl)mySelectedProfile).getExpandedNodes().saveVisibleState(myTree);
      ((DefaultTreeModel)myTree.getModel()).reload();
      ((InspectionProfileImpl)mySelectedProfile).getExpandedNodes().restoreVisibleState(myTree);
    }
  }

  public void selectInspectionTool(String name) {
    MyTreeNode node = null;
    for (int i = 0; i < myRoot.getChildCount(); i++) {
      final MyTreeNode child = (MyTreeNode)myRoot.getChildAt(i);
      for (int j = 0; j < child.getChildCount(); j++) {
        if (((Descriptor)((DefaultMutableTreeNode)child.getChildAt(j)).getUserObject()).getKey().toString().equals(name)) {
          node = (MyTreeNode)child.getChildAt(j);
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

    final MyTreeCellRenderer renderer = new MyTreeCellRenderer();
    myTree = new Tree(myRoot) {
      public Dimension getPreferredScrollableViewportSize() {
        Dimension size = super.getPreferredScrollableViewportSize();
        size = new Dimension(size.width + 10, size.height);
        return size;
      }

      protected void processMouseEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
          int row = myTree.getRowForLocation(e.getX(), e.getY());
          if (row >= 0) {
            Rectangle rowBounds = myTree.getRowBounds(row);
            renderer.setBounds(rowBounds);
            Rectangle checkBounds = renderer.myCheckbox.getBounds();

            checkBounds.setLocation(rowBounds.getLocation());

            if (checkBounds.contains(e.getPoint())) {
              MyTreeNode node = (MyTreeNode)myTree.getPathForRow(row).getLastPathComponent();
              toggleNode(node);
              myTree.setSelectionRow(row);
            }
          }
        }
        super.processMouseEvent(e);
      }

      public int getToggleClickCount() {
        return -1;
      }
    };


    myTree.setCellRenderer(renderer);
    myTree.setRootVisible(true);
    myTree.setShowsRootHandles(true);
    UIUtil.setLineStyleAngled(myTree);
    TreeToolTipHandler.install(myTree);
    TreeUtil.installActions(myTree);

    myTree.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (!e.isConsumed() && e.getKeyCode() == KeyEvent.VK_SPACE && !SpeedSearchBase.hasActiveSpeedSearch(myTree)) {
          final int selectionRow = myTree.getLeadSelectionRow();
          final int[] rows = myTree.getSelectionRows();
          for (int i = 0; rows != null && i < rows.length; i++) {
            final TreePath path = myTree.getPathForRow(rows[i]);
            final MyTreeNode node = (MyTreeNode)path.getLastPathComponent();
            if (Arrays.binarySearch(rows, myTree.getRowForPath(path.getParentPath())) < 0) {
              toggleNode(node);
            }
          }
          myTree.setSelectionRow(selectionRow);
          e.consume();
        }
      }
    });

    myTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        if (myTree.getSelectionPaths() != null && myTree.getSelectionPaths().length == 1) {
          updateOptionsAndDescriptionPanel(myTree.getSelectionPaths()[0]);
        }
        else {
          initOptionsAndDescriptionPanel();
        }
      }
    });


    myTree.addMouseListener(new PopupHandler() {
      public void invokePopup(Component comp, int x, int y) {
        if (myTree.getPathForLocation(x, y) != null && Arrays.binarySearch(myTree.getSelectionRows(), myTree.getRowForLocation(x, y)) > -1)
        {
          compoundPopup().show(comp, x, y);
        }
      }
    });


    new TreeSpeedSearch(myTree, new Convertor<TreePath, String>() {
      public String convert(TreePath o) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)o.getLastPathComponent();
        final Object userObject = node.getUserObject();
        if (userObject instanceof Descriptor) {
          return getDisplayTextToSort(((Descriptor)userObject).getText());
        }
        return getDisplayTextToSort(userObject.toString());
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
        final Object userObject = ((DefaultMutableTreeNode)treePath.getLastPathComponent()).getUserObject();
        if (userObject instanceof Descriptor) {
          return ((Descriptor)userObject).getText();
        }
        else {
          return (String)userObject;
        }
      }

      public void treeCollapsed(TreeExpansionEvent event) {
        ((InspectionProfileImpl)mySelectedProfile).getExpandedNodes().collapseNode(getExpandedString(event.getPath()));
      }

      public void treeExpanded(TreeExpansionEvent event) {
        ((InspectionProfileImpl)mySelectedProfile).getExpandedNodes().expandNode(getExpandedString(event.getPath()));
      }
    });

    myTreeExpander = new DefaultTreeExpander(myTree);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        myTree.requestFocus();
      }
    });

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

  private static String renderSeverity(HighlightSeverity severity) {
    return InspectionsBundle.message("inspection.as", severity.toString().toLowerCase());
  }

  private void toggleNode(MyTreeNode node) {
    List<TreePath> expandedPaths = TreeUtil.collectExpandedPaths(myTree);
    node.isEnabled = !node.isEnabled;
    Object userObject = node.getUserObject();
    final MyTreeNode parent = (MyTreeNode)node.getParent();
    if (userObject instanceof Descriptor) {
      toggleToolNode(node);
    }
    else {
      final Enumeration children = node.children();
      node.isProperSetting = false;
      while (children.hasMoreElements()) {
        MyTreeNode child = (MyTreeNode)children.nextElement();
        child.isEnabled = node.isEnabled;
        child.isProperSetting = false;
        if (child.getUserObject()instanceof Descriptor) {
          toggleToolNode(child);
        }
        else {
          final Enumeration descriptorNodes = child.children();
          while (descriptorNodes.hasMoreElements()) {
            MyTreeNode descriptorNode = (MyTreeNode)descriptorNodes.nextElement();
            descriptorNode.isEnabled = child.isEnabled;
            if (descriptorNode.getUserObject()instanceof Descriptor) {
              toggleToolNode(descriptorNode);
            }
            child.isProperSetting |= descriptorNode.isProperSetting;
          }
        }
        node.isProperSetting |= child.isProperSetting;
      }
    }
    updateUpHierarchy(node, parent);
    ((DefaultTreeModel)myTree.getModel()).reload();
    updateOptionsAndDescriptionPanel(new TreePath(node.getPath()));
    TreeUtil.restoreExpandedPaths(myTree, expandedPaths);
  }

  private void toggleToolNode(final MyTreeNode toolNode) {
    final Descriptor descriptor = (Descriptor)toolNode.getUserObject();
    final HighlightDisplayKey key = descriptor.getKey();
    final String toolShortName = key.toString();
    if (toolNode.isEnabled) {
      mySelectedProfile.enableTool(toolShortName);
    }
    else {
      mySelectedProfile.disableTool(toolShortName);
    }
    toolNode.isProperSetting = mySelectedProfile.isProperSetting(key);
  }

  private void updateUpHierarchy(final MyTreeNode node, final MyTreeNode parent) {
    if (parent != null) {
      if (node.isProperSetting) {
        parent.isProperSetting = true;
        myRoot.isProperSetting = true;
      }
      else {
        parent.isProperSetting = wasModified(parent);
        myRoot.isProperSetting = parent.isProperSetting || wasModified(myRoot);
      }
    }
  }

  private static boolean wasModified(MyTreeNode node) {
    for (int i = 0; i < node.getChildCount(); i++) {
      final MyTreeNode childNode = (MyTreeNode)node.getChildAt(i);
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
    if (StringUtil.containsIgnoreCase(descriptor.getGroup(), filter)) {
      return true;
    }
    for (String stripped : quoted) {
      if (StringUtil.containsIgnoreCase(descriptor.getText(),stripped)) {
        return true;
      }
      if (StringUtil.containsIgnoreCase(descriptor.getGroup(),stripped)) {
        return true;
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
    myRoot.isEnabled = false;
    myRoot.isProperSetting = false;
    List<Set<String>> keySetList = new ArrayList<Set<String>>();
    final Set<String> quated = new HashSet<String>();
    if (filter != null && filter.length() > 0) {
      keySetList.addAll(SearchUtil.findKeys(filter, quated));
    }
    for (Descriptor descriptor : myDescriptors) {
      if (descriptor.getTool() != null && !(descriptor.getTool()instanceof LocalInspectionToolWrapper) && !myShowInspections) continue;
      if (filter != null && filter.length() > 0 && !isDescriptorAccepted(descriptor, filter, forceInclude, keySetList, quated)) {
        continue;
      }
      final HighlightDisplayKey key = descriptor.getKey();
      final boolean enabled = mySelectedProfile.isToolEnabled(key);
      final boolean properSetting = mySelectedProfile.isProperSetting(key);
      final MyTreeNode node = new MyTreeNode(descriptor, enabled, properSetting);
      final MyTreeNode groupNode = getGroupNode(myRoot, descriptor.getGroup());
      groupNode.add(node);
      groupNode.isEnabled |= enabled;
      groupNode.isProperSetting |= properSetting;
      myRoot.isEnabled |= enabled;
      myRoot.isProperSetting |= properSetting;
    }
    if (filter != null && forceInclude && myRoot.getChildCount() == 0) {
      final Set<String> filters = SearchableOptionsRegistrar.getInstance().getProcessedWords(filter);
      if (filters.size() > 1) {
        fillTreeData(filter, false);
      }
    }
    sortInspections();
  }

  private void sortInspections() {
    Comparator<MyTreeNode> comparator = new Comparator<MyTreeNode>() {
      public int compare(MyTreeNode o1, MyTreeNode o2) {
        String s1 = null;
        String s2 = null;
        Object userObject1 = o1.getUserObject();
        Object userObject2 = o2.getUserObject();

        if (userObject1 instanceof String && userObject2 instanceof String) {
          s1 = (String)userObject1;
          s2 = (String)userObject2;
        }

        if (userObject1 instanceof Descriptor && userObject2 instanceof Descriptor) {
          s1 = ((Descriptor)userObject1).getText();
          s2 = ((Descriptor)userObject2).getText();
        }

        if (s1 != null && s2 != null) {
          return getDisplayTextToSort(s1).compareToIgnoreCase(getDisplayTextToSort(s2));
        }

        //can't be
        return -1;
      }
    };
    TreeUtil.sort(myRoot, comparator);
  }

  public static String getDisplayTextToSort(String s) {
    if (s.length() == 0) {
      return s;
    }
    while (!Character.isLetterOrDigit(s.charAt(0))) {
      s = s.substring(1);
      if (s.length() == 0) {
        return s;
      }
    }
    return s;
  }

  private void updateOptionsAndDescriptionPanel(TreePath path) {
    if (path == null) return;
    final MyTreeNode node = (MyTreeNode)path.getLastPathComponent();
    final Object userObject = node.getUserObject();
    if (userObject instanceof Descriptor) {
      final Descriptor descriptor = (Descriptor)userObject;
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

      final LevelChooser chooser = new LevelChooser(((SeverityProvider)mySelectedProfile.getProfileManager()).getOwnSeverityRegistrar());
      chooser.getComboBox().addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          boolean toUpdate = mySelectedProfile.getErrorLevel(descriptor.getKey()) != chooser.getLevel();
          mySelectedProfile.setErrorLevel(descriptor.getKey(), chooser.getLevel());
          if (toUpdate) node.isProperSetting = mySelectedProfile.isProperSetting(descriptor.getKey());
        }
      });
      chooser.setLevel(mySelectedProfile.getErrorLevel(descriptor.getKey()));
      final JPanel withSeverity = new JPanel(new GridBagLayout());
      withSeverity.add(new JLabel(InspectionsBundle.message("inspection.severity")), new GridBagConstraints(0, 0, 1, 1, 0, 0,
                                                                                                            GridBagConstraints.WEST,
                                                                                                            GridBagConstraints.NONE,
                                                                                                            new Insets(0, 5, 5, 10), 0, 0));
      Dimension dimension = new Dimension(150, chooser.getPreferredSize().height);
      chooser.setPreferredSize(dimension);
      chooser.setMinimumSize(dimension);
      withSeverity.add(chooser, new GridBagConstraints(1, 0, 1, 1, 1.0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                       new Insets(0, 0, 5, 0), 0, 0));
      final JComponent config = descriptor.getAdditionalConfigPanel(mySelectedProfile);
      if (config != null) {
        withSeverity.add(config, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                                        new Insets(0, 0, 0, 0), 0, 0));
      }
      else {
        withSeverity.add(new JPanel(), new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                                              new Insets(0, 0, 0, 0), 0, 0));
      }

      myOptionsPanel.removeAll();
      myOptionsPanel.add(withSeverity);
      myOptionsPanel.validate();
      GuiUtils.enableChildren(myOptionsPanel, node.isEnabled);
    }
    else {
      initOptionsAndDescriptionPanel();
    }
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

  private static MyTreeNode getGroupNode(MyTreeNode root, String group) {
    final int childCount = root.getChildCount();
    for (int i = 0; i < childCount; i++) {
      MyTreeNode child = (MyTreeNode)root.getChildAt(i);
      if (group.equals(child.getUserObject())) return child;
    }
    MyTreeNode child = new MyTreeNode(group, false, false);
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

  public void setFilter(final String filter) {
    myProfileFilter.setFilter(filter);
  }

  public boolean isResetEnabled() {
    return myRoot.isProperSetting;
  }

  private void setSelectedProfile(final ModifiableModel modifiableModel) {
    mySelectedProfile = modifiableModel;
    myInitialProfile = mySelectedProfile.getName();
    initDescriptors();
    filterTree(myProfileFilter != null ? myProfileFilter.getFilter() : null);
  }

  @Nullable
  private String getHint(Descriptor descriptor) {
    if (!myShowInspections) return null;
    if (descriptor.getTool() == null) {
      return InspectionsBundle.message("inspection.tool.availability.in.tree.node");
    }
    if (descriptor.getTool()instanceof LocalInspectionToolWrapper) {
      return null;
    }
    return InspectionsBundle.message("inspection.tool.availability.in.tree.node1");
  }

  public Dimension getPreferredSize() {
    return new Dimension(700, -1);
  }

  public void disposeUI() {
    if (mySelectedProfile != null && getSavedProfile() != null) {
      ModifiableModel profile = mySelectedProfile.getParentProfile().getModifiableModel();
      ((InspectionProfileImpl)profile).getExpandedNodes().saveVisibleState(myTree);
      //TODO lesya
      InspectionProfileManager.getInstance().fireProfileChanged(profile);
      /*
      try {
        profile.save();
      }
      catch (IOException e) {
        LOG.error(e);
      }
      */
      profile.getProfileManager().updateProfile(profile);
    }
    myAlarm.cancelAllRequests();
    myProfileFilter.dispose();
    mySelectedProfile = null;
  }

  private InspectionProfile getSavedProfile() {
    return (InspectionProfile)mySelectedProfile.getProfileManager().getProfile(mySelectedProfile.getName(), false);
  }

  private JPanel createInspectionProfileSettingsPanel() {

    myBrowser = new JEditorPane(UIUtil.HTML_MIME, EMPTY_HTML);
    myBrowser.setEditable(false);

    initDescriptors();
    fillTreeData(myProfileFilter != null ? myProfileFilter.getFilter() : null, true);

    JPanel descriptionPanel = new JPanel();
    descriptionPanel.setBorder(IdeBorderFactory.createTitledBorder(InspectionsBundle.message("inspection.description.title")));
    descriptionPanel.setLayout(new BorderLayout());
    descriptionPanel.add(ScrollPaneFactory.createScrollPane(myBrowser), BorderLayout.CENTER);

    JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 5));
    rightPanel.add(descriptionPanel);

    JPanel panel1 = new JPanel(new BorderLayout());
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
    for (Descriptor descriptor : myDescriptors) {
      if (mySelectedProfile.isToolEnabled(descriptor.getKey()) != descriptor.isEnabled()) {
        return setSelectedProfileModified(true);
      }
      if (mySelectedProfile.getErrorLevel(descriptor.getKey()) != descriptor.getLevel()) {
        return setSelectedProfileModified(true);
      }
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


  private static class InputDialog extends DialogWrapper {
    private JCheckBox myCheckBox;
    private JPanel myPanel;
    private JTextField myTextField;

    protected InputDialog(boolean showCheckBox, String initial, String checkBoxLabel, Project project) {
      super(project, true);
      myCheckBox.setVisible(showCheckBox);
      myCheckBox.setText(checkBoxLabel);
      myTextField.setText(initial);
      setTitle(InspectionsBundle.message("inspection.new.profile.dialog.title"));
      init();
    }

    @Nullable
    protected JComponent createCenterPanel() {
      return myPanel;
    }

    public boolean isChecked() {
      return myCheckBox.isSelected();
    }

    public String getName() {
      return myTextField.getText();
    }

    public JComponent getPreferredFocusedComponent() {
      return myTextField;
    }
  }

  public static class MyTreeNode extends DefaultMutableTreeNode {
    public boolean isEnabled;
    public boolean isProperSetting;

    public MyTreeNode(Object userObject, boolean enabled, boolean properSetting) {
      super(userObject);
      isEnabled = enabled;
      isProperSetting = properSetting;
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof MyTreeNode)) return false;
      MyTreeNode node = (MyTreeNode)obj;
      return isEnabled == node.isEnabled &&
             (getUserObject() != null ? node.getUserObject().equals(getUserObject()) : node.getUserObject() == null);
    }

    public int hashCode() {
      return getUserObject() != null ? getUserObject().hashCode() : 0;
    }
  }

  public static class LevelChooser extends ComboboxWithBrowseButton {
    private final MyRenderer ourRenderer = new MyRenderer();

    public LevelChooser(final SeverityRegistrar severityRegistrar) {
      final JComboBox comboBox = getComboBox();
      final DefaultComboBoxModel model = new DefaultComboBoxModel();
      comboBox.setModel(model);
      fillModel(model, severityRegistrar);
      comboBox.setRenderer(ourRenderer);
      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          final SeverityEditorDialog dlg = new SeverityEditorDialog(LevelChooser.this, (HighlightSeverity)getComboBox().getSelectedItem(), severityRegistrar);
          dlg.show();
          if (dlg.isOK()) {
            fillModel(model, severityRegistrar);
            final HighlightInfoType type = dlg.getSelectedType();
            if (type != null) {
              getComboBox().setSelectedItem(type.getSeverity(null));
            }
          }
        }
      });
    }

    private static void fillModel(DefaultComboBoxModel model, final SeverityRegistrar severityRegistrar) {
      model.removeAllElements();
      final TreeSet<HighlightSeverity> severities = new TreeSet<HighlightSeverity>(severityRegistrar);
      for (SeverityRegistrar.SeverityBasedTextAttributes type : severityRegistrar.getRegisteredHighlightingInfoTypes()) {
        severities.add(type.getSeverity());
      }
      severities.add(HighlightSeverity.ERROR);
      severities.add(HighlightSeverity.WARNING);
      severities.add(HighlightSeverity.INFO);
      severities.add(HighlightSeverity.GENERIC_SERVER_ERROR_OR_WARNING);
      for (HighlightSeverity severity : severities) {
        model.addElement(severity);
      }
    }

    public HighlightDisplayLevel getLevel() {
      HighlightSeverity severity = (HighlightSeverity)getComboBox().getSelectedItem();
      if (severity == null) return HighlightDisplayLevel.WARNING;
      return HighlightDisplayLevel.find(severity);
    }

    public void setLevel(HighlightDisplayLevel level) {
      getComboBox().setSelectedItem(level.getSeverity());
    }

    private static class MyRenderer extends DefaultListCellRenderer {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof HighlightSeverity) {
          HighlightSeverity severity = (HighlightSeverity)value;
          setText(renderSeverity(severity));
          setIcon(HighlightDisplayLevel.find(severity).getIcon());
        }
        return this;
      }
    }
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
        final MyTreeNode node = (MyTreeNode)myTree.getPathForRow(rows[i]).getLastPathComponent();
        final MyTreeNode parent = (MyTreeNode)node.getParent();
        if (node.getUserObject()instanceof Descriptor) {
          updateErrorLevel(node, showOptionsAndDescriptorPanels);
          updateUpHierarchy(node, parent);
        }
        else {
          node.isProperSetting = false;
          for (int j = 0; j < node.getChildCount(); j++) {
            final MyTreeNode child = (MyTreeNode)node.getChildAt(j);
            if (child.getUserObject()instanceof Descriptor) {     //group node
              updateErrorLevel(child, showOptionsAndDescriptorPanels);
            }
            else {                                               //root node
              child.isProperSetting = false;
              for (int k = 0; k < child.getChildCount(); k++) {
                final MyTreeNode descriptorNode = (MyTreeNode)child.getChildAt(k);
                if (descriptorNode.getUserObject()instanceof Descriptor) {
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

    private void updateErrorLevel(final MyTreeNode child, final boolean showOptionsAndDescriptorPanels) {
      final HighlightDisplayKey key = ((Descriptor)child.getUserObject()).getKey();
      mySelectedProfile.setErrorLevel(key, myLevel);
      child.isProperSetting = mySelectedProfile.isProperSetting(key);
      if (showOptionsAndDescriptorPanels) {
        updateOptionsAndDescriptionPanel(new TreePath(child.getPath()));
      }
    }
  }

  private class MyTreeCellRenderer extends JPanel implements TreeCellRenderer {
    private final ColoredTreeCellRenderer myTextRenderer;
    public final JCheckBox myCheckbox;

    public MyTreeCellRenderer() {
      super(new BorderLayout());
      myCheckbox = new JCheckBox();
      myTextRenderer = new ColoredTreeCellRenderer() {
        public void customizeCellRenderer(JTree tree,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          boolean leaf,
                                          int row,
                                          boolean hasFocus) {
        }
      };
      myTextRenderer.setOpaque(true);
      add(myCheckbox, BorderLayout.WEST);
      add(myTextRenderer, BorderLayout.CENTER);
    }

    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
      invalidate();
      MyTreeNode node = (MyTreeNode)value;
      Object object = node.getUserObject();
      setOpaque(true);
      final Color background = selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground();
      setBackground(background);
      myCheckbox.setSelected(node.isEnabled);
      myCheckbox.setOpaque(true);
      myCheckbox.setBackground(background);
      @NonNls String text = null;
      int style = Font.PLAIN;
      String hint = null;
      if (object instanceof String) {
        text = (String)object;
        style = Font.BOLD;
      }
      else if (object instanceof Descriptor) {
        final Descriptor descriptor = (Descriptor)object;
        text = descriptor.getText();
        hint = getHint(descriptor);
      }
      Color foreground =
        selected ? UIUtil.getTreeSelectionForeground() : node.isProperSetting ? Color.BLUE : UIUtil.getTreeTextForeground();
      myTextRenderer.clear();
      if (text != null) {
        SearchUtil.appendFragments(myProfileFilter != null ? myProfileFilter.getFilter() : null, text, style, foreground, background,
                                   myTextRenderer);
      }
      if (hint != null) {
        myTextRenderer
          .append(" " + hint, selected ? new SimpleTextAttributes(Font.PLAIN, foreground) : SimpleTextAttributes.GRAYED_ATTRIBUTES);
      }
      setForeground(foreground);
      myCheckbox.setForeground(foreground);
      myTextRenderer.setForeground(foreground);
      return this;
    }
  }

  private class MyFilterComponent extends FilterComponent {
    private final TreeExpansionMonitor<DefaultMutableTreeNode> myExpansionMonitor = TreeExpansionMonitor.install(myTree);

    public MyFilterComponent() {
      super(INSPECTION_FILTER_HISTORY, 10);
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
      ((DefaultTreeModel)myTree.getModel()).reload();
      TreeUtil.expandAll(myTree);
      if (filter == null || filter.length() == 0) {
        TreeUtil.collapseAll(myTree, 0);
        myExpansionMonitor.restore();
      }
    }
  }

}