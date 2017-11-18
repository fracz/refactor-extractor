package com.intellij.openapi.keymap.impl.ui;

import com.intellij.ide.actionMacro.ActionMacro;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.actionSystem.ex.QuickList;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.keymap.KeyMapBundle;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapExtension;
import com.intellij.openapi.keymap.KeymapGroup;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.IconLoader;
import com.intellij.tools.Tool;
import com.intellij.tools.ToolManager;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

public class ActionsTreeUtil {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.keymap.impl.ui.ActionsTreeUtil");

  public static final String MAIN_MENU_TITLE = KeyMapBundle.message("main.menu.action.title");
  public static final String MAIN_TOOLBAR = KeyMapBundle.message("main.toolbar.title");
  public static final String EDITOR_POPUP = KeyMapBundle.message("editor.popup.menu.title");

  private static final Icon MAIN_MENU_ICON = IconLoader.getIcon("/nodes/keymapMainMenu.png");
  private static final Icon EDITOR_ICON = IconLoader.getIcon("/nodes/keymapEditor.png");
  private static final Icon EDITOR_OPEN_ICON = IconLoader.getIcon("/nodes/keymapEditorOpen.png");
  private static final Icon TOOLS_ICON = IconLoader.getIcon("/nodes/keymapTools.png");
  private static final Icon TOOLS_OPEN_ICON = IconLoader.getIcon("/nodes/keymapToolsOpen.png");
  private static final Icon OTHER_ICON = IconLoader.getIcon("/nodes/keymapOther.png");
  public static final String EDITOR_TAB_POPUP = KeyMapBundle.message("editor.tab.popup.menu.title");
  public static final String FAVORITES_POPUP = KeyMapBundle.message("favorites.popup.title");
  public static final String PROJECT_VIEW_POPUP = KeyMapBundle.message("project.view.popup.menu.title");
  public static final String COMMANDER_POPUP = KeyMapBundle.message("commender.view.popup.menu.title");
  public static final String J2EE_POPUP = KeyMapBundle.message("j2ee.view.popup.menu.title");

  @NonNls
  private static final String VCS_GROUP_ID = "VcsGroup";
  @NonNls
  private static final String EDITOR_PREFIX = "Editor";

  private ActionsTreeUtil() {
  }

  private static Group createPluginsActionsGroup(Condition<AnAction> filtered) {
    Group pluginsGroup = new Group(KeyMapBundle.message("plugins.group.title"), null, null);
    final KeymapManagerEx keymapManager = KeymapManagerEx.getInstanceEx();
    ActionManagerEx managerEx = ActionManagerEx.getInstanceEx();
    final IdeaPluginDescriptor[] plugins = ApplicationManager.getApplication().getPlugins();
    for (IdeaPluginDescriptor plugin : plugins) {
      Group pluginGroup = new Group(plugin.getName(), null, null);
      final String[] pluginActions = managerEx.getPluginActions(plugin.getPluginId());
      if (pluginActions == null || pluginActions.length == 0) {
        continue;
      }
      for (String pluginAction : pluginActions) {
        if (keymapManager.getBoundActions().contains(pluginAction)) continue;
        final AnAction anAction = managerEx.getActionOrStub(pluginAction);
        if (filtered == null || filtered.value(anAction)) {
          pluginGroup.addActionId(pluginAction);
        }
      }
      if (pluginGroup.getSize() > 0) {
        pluginsGroup.addGroup(pluginGroup);
      }
    }
    return pluginsGroup;
  }

  private static Group createBookmarksActionsGroup(Condition<AnAction> filtered) {
    return createGroup((ActionGroup)ActionManager.getInstance().getActionOrStub(IdeActions.GROUP_BOOKMARKS), true, filtered);
  }

  private static Group createVcsGroup(Condition<AnAction> filtered) {
    Group group = new Group(KeyMapBundle.message("version.control.group.title"), VCS_GROUP_ID, null, null);
    ActionGroup versionControls = (ActionGroup)ActionManager.getInstance().getActionOrStub(VCS_GROUP_ID);
    fillGroupIgnorePopupFlag(versionControls, group, filtered);
    return group;
  }

  private static Group createMainMenuGroup(Condition<AnAction> filtered) {
    Group group = new Group(MAIN_MENU_TITLE, IdeActions.GROUP_MAIN_MENU, MAIN_MENU_ICON, MAIN_MENU_ICON);
    ActionGroup mainMenuGroup = (ActionGroup)ActionManager.getInstance().getActionOrStub(IdeActions.GROUP_MAIN_MENU);
    fillGroupIgnorePopupFlag(mainMenuGroup, group, filtered);
    return group;
  }

  private static void fillGroupIgnorePopupFlag(ActionGroup actionGroup, Group group, Condition<AnAction> filtered) {
    AnAction[] mainMenuTopGroups = actionGroup instanceof DefaultActionGroup
                                   ? ((DefaultActionGroup)actionGroup).getChildActionsOrStubs(null)
                                   : actionGroup.getChildren(null);
    for (AnAction action : mainMenuTopGroups) {
      Group subGroup = createGroup((ActionGroup)action, false, filtered);
      if (subGroup.getSize() > 0) {
        group.addGroup(subGroup);
      }
    }
  }

  public static Group createGroup(ActionGroup actionGroup, boolean ignore, Condition<AnAction> filtered) {
    final String name = actionGroup.getTemplatePresentation().getText();
    return createGroup(actionGroup, name != null ? name : ActionManager.getInstance().getId(actionGroup), null, null, ignore, filtered);
  }

  public static Group createGroup(ActionGroup actionGroup,
                                  String groupName,
                                  Icon icon,
                                  Icon openIcon,
                                  boolean ignore,
                                  Condition<AnAction> filtered) {
    ActionManager actionManager = ActionManager.getInstance();
    Group group = new Group(groupName, actionManager.getId(actionGroup), icon, openIcon);
    AnAction[] children = actionGroup instanceof DefaultActionGroup
                          ? ((DefaultActionGroup)actionGroup).getChildActionsOrStubs(null)
                          : actionGroup.getChildren(null);

    for (int i = 0; i < children.length; i++) {
      AnAction action = children[i];

      if (action instanceof ActionGroup) {
        Group subGroup = createGroup((ActionGroup)action, ignore, filtered);
        if (subGroup.getSize() > 0) {
          if (!ignore && !((ActionGroup)action).isPopup()) {
            group.addAll(subGroup);
          }
          else {
            group.addGroup(subGroup);
          }
        }
        else if (filtered == null || filtered.value(actionGroup)) {
          group.addGroup(subGroup);
        }
      }
      else if (action instanceof Separator) {
        if (group.getSize() > 0 && i < children.length - 1 && !(group.getChildren().get(group.getSize() - 1)instanceof Separator)) {
          group.addSeparator();
        }
      }
      else {
        String id = action instanceof ActionStub ? ((ActionStub)action).getId() : actionManager.getId(action);
        if (id != null) {
          if (id.startsWith(Tool.ACTION_ID_PREFIX)) continue;
          if (filtered == null || filtered.value(action)) {
            group.addActionId(id);
          }
        }
      }
    }
    group.normalizeSeparators();
    return group;
  }

  private static Group createDebuggerActionsGroup(Condition<AnAction> filtered) {
    ActionManager actionManager = ActionManager.getInstance();
    DefaultActionGroup debuggerGroup = (DefaultActionGroup)actionManager.getActionOrStub(IdeActions.GROUP_DEBUGGER);
    AnAction[] debuggerActions = debuggerGroup.getChildActionsOrStubs(null);

    ArrayList<String> ids = new ArrayList<String>();
    for (AnAction debuggerAction : debuggerActions) {
      String actionId = debuggerAction instanceof ActionStub ? ((ActionStub)debuggerAction).getId() : actionManager.getId(debuggerAction);
      if (filtered == null || filtered.value(debuggerAction)) {
        ids.add(actionId);
      }
    }

    Collections.sort(ids);
    Group group = new Group(KeyMapBundle.message("debugger.actions.group.title"), IdeActions.GROUP_DEBUGGER, null, null);
    for (String id : ids) {
      group.addActionId(id);
    }

    return group;
  }

  private static Group createEditorActionsGroup(Condition<AnAction> filtered) {
    ActionManager actionManager = ActionManager.getInstance();
    DefaultActionGroup editorGroup = (DefaultActionGroup)actionManager.getActionOrStub(IdeActions.GROUP_EDITOR);
    AnAction[] editorActions = editorGroup.getChildActionsOrStubs(null);

    ArrayList<String> ids = new ArrayList<String>();
    for (AnAction editorAction : editorActions) {
      String actionId = editorAction instanceof ActionStub ? ((ActionStub)editorAction).getId() : actionManager.getId(editorAction);
      if (actionId == null) continue;
      if (actionId.startsWith(EDITOR_PREFIX)) {
        AnAction action = actionManager.getActionOrStub('$' + actionId.substring(6));
        if (action != null) continue;
      }
      if (filtered == null || filtered.value(editorAction)) {
        ids.add(actionId);
      }
    }

    Collections.sort(ids);
    Group group = new Group(KeyMapBundle.message("editor.actions.group.title"), IdeActions.GROUP_EDITOR, EDITOR_ICON, EDITOR_OPEN_ICON);
    for (String id : ids) {
      group.addActionId(id);
    }

    return group;
  }

  private static Group createExtensionGroup(Condition<AnAction> filtered, final Project project, KeymapExtension provider) {
    Group group = new Group(provider.getGroupName(), provider.getOpenIcon(), provider.getIcon());

    for (KeymapGroup entry : provider.createSubGroups(filtered, project)) {
      group.addGroup((Group) entry);
    }

    return group;
  }

  private static Group createMacrosGroup(Condition<AnAction> filtered) {
    final ActionManagerEx actionManager = ActionManagerEx.getInstanceEx();
    String[] ids = actionManager.getActionIds(ActionMacro.MACRO_ACTION_PREFIX);
    Arrays.sort(ids);
    Group group = new Group(KeyMapBundle.message("macros.group.title"), null, null);
    for (String id : ids) {
      if (filtered == null || filtered.value(actionManager.getActionOrStub(id))) {
        group.addActionId(id);
      }
    }
    return group;
  }

  private static Group createQuickListsGroup(final Condition<AnAction> filtered, final String filter, final boolean forceFiltering, final QuickList[] quickLists) {
    Arrays.sort(quickLists, new Comparator<QuickList>() {
      public int compare(QuickList l1, QuickList l2) {
        return l1.getActionId().compareTo(l2.getActionId());
      }
    });

    Group group = new Group(KeyMapBundle.message("quick.lists.group.title"), null, null);
    for (QuickList quickList : quickLists) {
      if (filtered != null && filtered.value(ActionManagerEx.getInstanceEx().getAction(quickList.getActionId()))) {
        group.addQuickList(quickList);
      } else if (SearchUtil.isComponentHighlighted(quickList.getDisplayName(), filter, forceFiltering,
                                                   ShowSettingsUtil.getInstance().findApplicationConfigurable(KeymapConfigurable.class))) {
        group.addQuickList(quickList);
      } else if (filtered == null && filter == null) {
        group.addQuickList(quickList);
      }
    }
    return group;
  }


  private static Group createExternalToolsGroup(Condition<AnAction> filtered) {
    final ActionManagerEx actionManager = ActionManagerEx.getInstanceEx();
    String[] ids = actionManager.getActionIds(Tool.ACTION_ID_PREFIX);
    Arrays.sort(ids);
    Group group = new Group(KeyMapBundle.message("actions.tree.external.tools.group"), TOOLS_ICON, TOOLS_OPEN_ICON);

    ToolManager toolManager = ToolManager.getInstance();

    HashMap<String, Group> toolGroupNameToGroup = new HashMap<String, Group>();

    for (String id : ids) {
      if (filtered != null && !filtered.value(actionManager.getActionOrStub(id))) continue;
      String groupName = toolManager.getGroupByActionId(id);

      if (groupName != null && groupName.trim().length() == 0) {
        groupName = null;
      }

      Group subGroup = toolGroupNameToGroup.get(groupName);
      if (subGroup == null) {
        subGroup = new Group(groupName, null, null);
        toolGroupNameToGroup.put(groupName, subGroup);
        if (groupName != null) {
          group.addGroup(subGroup);
        }
      }

      subGroup.addActionId(id);
    }

    Group subGroup = toolGroupNameToGroup.get(null);
    if (subGroup != null) {
      group.addAll(subGroup);
    }

    return group;
  }

  private static Group createOtherGroup(Condition<AnAction> filtered, Group addedActions, final Keymap keymap) {
    addedActions.initIds();
    ArrayList<String> result = new ArrayList<String>();

    if (keymap != null) {
      String[] actionIds = keymap.getActionIds();
      for (String id : actionIds) {
        if (id.startsWith(EDITOR_PREFIX)) {
          AnAction action = ActionManager.getInstance().getActionOrStub("$" + id.substring(6));
          if (action != null) continue;
        }

        if (!id.startsWith(QuickList.QUICK_LIST_PREFIX) && !addedActions.containsId(id)) {
          result.add(id);
        }
      }
    }

    // add all registered actions
    final ActionManagerEx actionManager = ActionManagerEx.getInstanceEx();
    final KeymapManagerEx keymapManager = KeymapManagerEx.getInstanceEx();
    String[] registeredActionIds = actionManager.getActionIds("");
    for (String id : registeredActionIds) {
      if (actionManager.getActionOrStub(id)instanceof ActionGroup) {
        continue;
      }
      if (id.startsWith(QuickList.QUICK_LIST_PREFIX) || addedActions.containsId(id) || result.contains(id)) {
        continue;
      }

      if (keymapManager.getBoundActions().contains(id)) continue;

      result.add(id);
    }

    filterOtherActionsGroup(result);

    Collections.sort(result, new Comparator<String>() {
      public int compare(String id1, String id2) {
        return getTextToCompare(id1).compareToIgnoreCase(getTextToCompare(id2));
      }

      private String getTextToCompare(String id) {
        AnAction action = actionManager.getActionOrStub(id);
        if (action == null) {
          return id;
        }
        String text = action.getTemplatePresentation().getText();
        return text != null ? text : id;
      }
    });

    Group group = new Group(KeyMapBundle.message("other.group.title"), OTHER_ICON, OTHER_ICON);
    for (String id : result) {
      if (filtered == null || filtered.value(actionManager.getActionOrStub(id))) group.addActionId(id);
    }
    return group;
  }

  private static void filterOtherActionsGroup(ArrayList<String> actions) {
    filterOutGroup(actions, IdeActions.GROUP_GENERATE);
    filterOutGroup(actions, IdeActions.GROUP_NEW);
    filterOutGroup(actions, IdeActions.GROUP_CHANGE_SCHEME);
  }

  private static void filterOutGroup(ArrayList<String> actions, String groupId) {
    if (groupId == null) {
      throw new IllegalArgumentException();
    }
    ActionManager actionManager = ActionManager.getInstance();
    AnAction action = actionManager.getActionOrStub(groupId);
    if (action instanceof DefaultActionGroup) {
      DefaultActionGroup group = (DefaultActionGroup)action;
      AnAction[] children = group.getChildActionsOrStubs(null);
      for (AnAction child : children) {
        String childId = child instanceof ActionStub ? ((ActionStub)child).getId() : actionManager.getId(child);
        if (childId == null) {
          // SCR 35149
          continue;
        }
        if (child instanceof DefaultActionGroup) {
          filterOutGroup(actions, childId);
        }
        else {
          actions.remove(childId);
        }
      }
    }
  }

  public static DefaultMutableTreeNode createNode(Group group) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(group);
    for (Object child : group.getChildren()) {
      if (child instanceof Group) {
        DefaultMutableTreeNode childNode = createNode((Group)child);
        node.add(childNode);
      }
      else {
        node.add(new DefaultMutableTreeNode(child));
      }
    }
    return node;
  }

  public static Group createMainGroup(final Project project, final Keymap keymap, final QuickList[] quickLists) {
    return createMainGroup(project, keymap, quickLists, null, false, null);
  }

  public static Group createMainGroup(final Project project,
                                      final Keymap keymap,
                                      final QuickList[] quickLists,
                                      final String filter,
                                      final boolean forceFiltering,
                                      final Condition<AnAction> filtered) {
    Group mainGroup = new Group(KeyMapBundle.message("all.actions.group.title"), null, null);
    mainGroup.addGroup(createEditorActionsGroup(filtered));
    mainGroup.addGroup(createMainMenuGroup(filtered));
    mainGroup.addGroup(createVcsGroup(filtered));
    for (KeymapExtension extension : Extensions.getExtensions(KeymapExtension.EXTENSION_POINT_NAME)) {
      mainGroup.addGroup(createExtensionGroup(filtered, project, extension));
    }
    mainGroup.addGroup(createDebuggerActionsGroup(filtered));
    mainGroup.addGroup(createBookmarksActionsGroup(filtered));
    mainGroup.addGroup(createExternalToolsGroup(filtered));
    mainGroup.addGroup(createMacrosGroup(filtered));
    mainGroup.addGroup(createQuickListsGroup(filtered, filter, forceFiltering, quickLists));
    mainGroup.addGroup(createOtherGroup(filtered, mainGroup, keymap));
    mainGroup.addGroup(createPluginsActionsGroup(filtered));
    if (filter != null || filtered != null) {
      final ArrayList list = mainGroup.getChildren();
      for (Iterator i = list.iterator(); i.hasNext();) {
        final Object o = i.next();
        if (o instanceof Group) {
          final Group group = (Group)o;
          if (group.getSize() == 0) {
            i.remove();
          }
        }
      }
    }
    return mainGroup;
  }

  public static Condition<AnAction> isActionFiltered(final String filter, final boolean force) {
    return new Condition<AnAction>() {
      public boolean value(final AnAction action) {
        if (filter == null) return true;
        if (action == null) return false;
        final String insensitiveFilter = filter.toLowerCase();
        final KeymapConfigurable keymapConfigurable =
          ShowSettingsUtil.getInstance().findApplicationConfigurable(KeymapConfigurable.class);
        final String text = action.getTemplatePresentation().getText();
        if (text != null) {
          final String lowerText = text.toLowerCase();
          if (SearchUtil
            .isComponentHighlighted(lowerText, insensitiveFilter, force, keymapConfigurable)) {
            return true;
          }
          else if (lowerText.indexOf(insensitiveFilter) != -1) {
            return true;
          }
        }
        final String description = action.getTemplatePresentation().getDescription();
        if (description != null) {
          final String insensitiveDescription = description.toLowerCase();
          if (SearchUtil
            .isComponentHighlighted(insensitiveDescription, insensitiveFilter, force, keymapConfigurable)) {
            return true;
          }
          else if (insensitiveDescription.indexOf(insensitiveFilter) != -1) {
            return true;
          }
        }
        return false;
      }
    };
  }

  public static Condition<AnAction> isActionFiltered(final ActionManager actionManager,
                                                     final Keymap keymap,
                                                     final KeyboardShortcut keyboardShortcut) {
    return new Condition<AnAction>() {
      public boolean value(final AnAction action) {
        if (keyboardShortcut == null) return true;
        if (action == null) return false;
        final Shortcut[] actionShortcuts =
          keymap.getShortcuts(action instanceof ActionStub ? ((ActionStub)action).getId() : actionManager.getId(action));
        for (Shortcut shortcut : actionShortcuts) {
          if (shortcut instanceof KeyboardShortcut) {
            final KeyboardShortcut keyboardActionShortcut = (KeyboardShortcut)shortcut;
            if (Comparing.equal(keyboardActionShortcut, keyboardShortcut)) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }
}