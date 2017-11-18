package com.intellij.openapi.vcs.changes.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.actions.ShowDiffAction;
import com.intellij.openapi.vcs.changes.actions.MoveChangesToAnotherListAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.EventDispatcher;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

/**
 * @author max
 */
public class ChangesBrowser extends JPanel implements TypeSafeDataProvider {
  private ChangesTreeList myViewer;
  private ChangeList mySelectedChangeList;
  private Collection<Change> myAllChanges;
  private Collection<Change> myChangesToDisplay;
  private final Map<Change, LocalChangeList> myChangeListsMap = new HashMap<Change, LocalChangeList>();
  private Project myProject;
  private EventDispatcher<SelectedListChangeListener> myDispatcher = EventDispatcher.create(SelectedListChangeListener.class);
  private final JPanel myHeaderPanel;
  private boolean myReadOnly;
  private DefaultActionGroup myToolBarGroup;
  private ChangeListListener myChangeListListener = new MyChangeListListener();
  private ChangesBrowser.ChangeListChooser myChangeListChooser;
  private boolean myShowingAllChangeLists;

  public void setChangesToDisplay(final List<Change> changes) {
    myChangesToDisplay = changes;
    myViewer.setChangesToDisplay(changes);
  }

  public interface SelectedListChangeListener extends EventListener{
    void selectedListChanged();
  }

  public void addSelectedListChangeListener(SelectedListChangeListener listener) {
    myDispatcher.addListener(listener);
  }

  public void removeSelectedListChangeListener(SelectedListChangeListener listener) {
    myDispatcher.removeListener(listener);
  }

  @NonNls private final static String FLATTEN_OPTION_KEY = "ChangesBrowser.SHOW_FLATTEN";

  public ChangesBrowser(final Project project, List<? extends ChangeList> changeLists, final List<Change> changes,
                        ChangeList initialListSelection, final boolean showChangelistChooser,
                        final boolean capableOfExcludingChanges) {
    super(new BorderLayout());

    myProject = project;
    myAllChanges = new ArrayList<Change>();
    myReadOnly = !showChangelistChooser;

    myShowingAllChangeLists = changeLists.equals(ChangeListManager.getInstance(project).getChangeLists());
    for (ChangeList list : changeLists) {
      if (list instanceof LocalChangeList) {
        myAllChanges.addAll(list.getChanges());
        if (initialListSelection == null) {
          for(Change c: list.getChanges()) {
            if (changes.contains(c)) {
              initialListSelection = list;
              break;
            }
          }
        }
      }
    }

    if (initialListSelection == null) {
      for(ChangeList list: changeLists) {
        if (list instanceof LocalChangeList && ((LocalChangeList) list).isDefault()) {
          initialListSelection = list;
          break;
        }
      }
      if (initialListSelection == null && !changeLists.isEmpty()) {
        initialListSelection = changeLists.get(0);
      }
    }

    myViewer = new ChangesTreeList(myProject, changes, capableOfExcludingChanges);
    myViewer.setDoubleClickHandler(new Runnable() {
      public void run() {
        showDiff();
      }
    });

    setSelectedList(initialListSelection);

    JPanel listPanel = new JPanel(new BorderLayout());
    listPanel.add(myViewer);
    listPanel.setBorder(IdeBorderFactory.createTitledHeaderBorder(VcsBundle.message("commit.dialog.changed.files.label")));
    add(listPanel, BorderLayout.CENTER);

    myHeaderPanel = new JPanel(new BorderLayout());
    if (showChangelistChooser) {
      myChangeListChooser = new ChangeListChooser(changeLists);
      myHeaderPanel.add(myChangeListChooser, BorderLayout.EAST);
    }
    myHeaderPanel.add(createToolbar(), BorderLayout.WEST);
    add(myHeaderPanel, BorderLayout.NORTH);

    myViewer.installPopupHandler(myToolBarGroup);

    myViewer.setShowFlatten(PropertiesComponent.getInstance(myProject).isTrueValue(FLATTEN_OPTION_KEY));
    ChangeListManager.getInstance(myProject).addChangeListListener(myChangeListListener);
  }

  public void dispose() {
    ChangeListManager.getInstance(myProject).removeChangeListListener(myChangeListListener);
  }

  public void addRollbackAction() {
    myToolBarGroup.add(new RollbackAction());
  }

  public void addToolbarAction(AnAction action) {
    myToolBarGroup.add(action);
  }

  public void addToolbarActions(ActionGroup group) {
    myToolBarGroup.addSeparator();
    myToolBarGroup.add(group);
  }

  public JPanel getHeaderPanel() {
    return myHeaderPanel;
  }

  public Collection<Change> getAllChanges() {
    return myAllChanges;
  }

  public void calcData(DataKey key, DataSink sink) {
    if (key == DataKeys.CHANGES) {
      sink.put(DataKeys.CHANGES, myViewer.getSelectedChanges());
    }
    else if (key == DataKeys.CHANGE_LISTS) {
      sink.put(DataKeys.CHANGE_LISTS, getSelectedChangeLists());
    }
    else if (key == DataKeys.VIRTUAL_FILE_ARRAY) {
      sink.put(DataKeys.VIRTUAL_FILE_ARRAY, getSelectedFiles());
    }
    else if (key == DataKeys.NAVIGATABLE_ARRAY) {
      sink.put(DataKeys.NAVIGATABLE_ARRAY, ChangesUtil.getNavigatableArray(myProject, getSelectedFiles()));
    }
  }

  private class MoveAction extends MoveChangesToAnotherListAction {
    private final Change myChange;

    public MoveAction(final Change change) {
      myChange = change;
    }

    public void actionPerformed(AnActionEvent e) {
      askAndMove(myProject, new Change[]{myChange}, null);
    }
  }

  private class ToggleChangeAction extends CheckboxAction {
    private final Change myChange;

    public ToggleChangeAction(final Change change) {
      super(VcsBundle.message("commit.dialog.include.action.name"));
      myChange = change;
    }

    public boolean isSelected(AnActionEvent e) {
      return myViewer.isIncluded(myChange);
    }

    public void setSelected(AnActionEvent e, boolean state) {
      if (state) {
        myViewer.includeChange(myChange);
      }
      else {
        myViewer.excludeChange(myChange);
      }
    }
  }

  private void showDiff() {
    final Change leadSelection = myViewer.getLeadSelection();
    Change[] changes = myViewer.getSelectedChanges();

    if (changes.length < 2) {
      final Collection<Change> displayedChanges = getCurrentDisplayedChanges();
      changes = displayedChanges.toArray(new Change[displayedChanges.size()]);
    }

    int indexInSelection = Arrays.asList(changes).indexOf(leadSelection);
    if (indexInSelection >= 0) {
      ShowDiffAction.showDiffForChange(changes, indexInSelection, myProject, !myReadOnly ? new DiffToolbarActionsFactory() : null);
    }
    else {
      ShowDiffAction.showDiffForChange(new Change[]{leadSelection}, 0, myProject, !myReadOnly ? new DiffToolbarActionsFactory() : null);
    }
  }

  private class DiffToolbarActionsFactory implements ShowDiffAction.AdditionalToolbarActionsFactory {
    public List<? extends AnAction> createActions(Change change) {
      return Arrays.asList(new MoveAction(change), new ToggleChangeAction(change));
    }
  }

  private void rebuildList() {
    if (myChangesToDisplay == null) {
      final ChangeListManager manager = ChangeListManager.getInstance(myProject);
      myChangeListsMap.clear();
      for (Change change : myAllChanges) {
        myChangeListsMap.put(change, manager.getChangeList(change));
      }
    }

    myViewer.setChangesToDisplay(getCurrentDisplayedChanges());
  }

  private class ChangeListChooser extends JPanel {
    private JComboBox myChooser;

    public ChangeListChooser(List<? extends ChangeList> lists) {
      super(new BorderLayout());
      myChooser = new JComboBox();
      myChooser.setRenderer(new ColoredListCellRenderer() {
        protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
          final LocalChangeList l = ((LocalChangeList)value);
          append(l.getName(),
                 l.isDefault() ? SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
      });

      myChooser.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            setSelectedList((LocalChangeList)myChooser.getSelectedItem());
          }
        }
      });

      updateLists(lists);
      myChooser.setEditable(false);
      add(myChooser, BorderLayout.EAST);

      JLabel label = new JLabel(VcsBundle.message("commit.dialog.changelist.label"));
      label.setDisplayedMnemonic('l');
      label.setLabelFor(myChooser);
      add(label, BorderLayout.CENTER);
    }

    public void updateLists(List<? extends ChangeList> lists) {
      myChooser.setModel(new DefaultComboBoxModel(lists.toArray()));
      myChooser.setEnabled(lists.size() > 1);
      myChooser.setSelectedItem(mySelectedChangeList);
    }
  }

  private void setSelectedList(final ChangeList list) {
    mySelectedChangeList = list;
    rebuildList();
    myDispatcher.getMulticaster().selectedListChanged();
  }

  private JComponent createToolbar() {
    myToolBarGroup = new DefaultActionGroup();
    final ShowDiffAction diffAction = new ShowDiffAction() {
      public void actionPerformed(AnActionEvent e) {
        showDiff();
      }
    };

    diffAction.registerCustomShortcutSet(
      new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_D, SystemInfo.isMac ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK)),
      myViewer);
    myToolBarGroup.add(diffAction);

    if (!myReadOnly) {
      final MoveChangesToAnotherListAction moveAction = new MoveChangesToAnotherListAction() {
        public void actionPerformed(AnActionEvent e) {
          super.actionPerformed(e);
          rebuildList();
        }
      };

      moveAction.registerCustomShortcutSet(CommonShortcuts.getMove(), myViewer);
      myToolBarGroup.add(moveAction);
    }

    ToggleShowDirectoriesAction directoriesAction = new ToggleShowDirectoriesAction();
    directoriesAction.registerCustomShortcutSet(
      new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_P, SystemInfo.isMac ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK)),
      myViewer);

    myToolBarGroup.add(directoriesAction);

    for(AnAction action: myViewer.getTreeActions()) {
      myToolBarGroup.add(action);
    }

    return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, myToolBarGroup, true).getComponent();
  }

  public List<Change> getCurrentDisplayedChanges() {
    final List<Change> list;
    if (myChangesToDisplay != null) {
      list = new ArrayList<Change>(myChangesToDisplay);
    }
    else if (!(mySelectedChangeList instanceof LocalChangeList)) {
      list = new ArrayList<Change>(mySelectedChangeList.getChanges());
    }
    else {
      list = filterBySelectedChangeList(myAllChanges);
    }
    Collections.sort(list, new Comparator<Change>() {
      public int compare(final Change o1, final Change o2) {
        return ChangesUtil.getFilePath(o1).getName().compareToIgnoreCase(ChangesUtil.getFilePath(o2).getName());
      }
    });
    return list;
  }

  public List<Change> getCurrentIncludedChanges() {
    return filterBySelectedChangeList(myViewer.getIncludedChanges());
  }

  public ChangeList getSelectedChangeList() {
    return mySelectedChangeList;
  }

  private List<Change> filterBySelectedChangeList(final Collection<Change> changes) {
    List<Change> filtered = new ArrayList<Change>();
    for (Change change : changes) {
      if (myReadOnly || getList(change) == mySelectedChangeList) {
        filtered.add(change);
      }
    }
    return filtered;
  }

  private ChangeList getList(final Change change) {
    return myChangeListsMap.get(change);
  }

  public JComponent getPrefferedFocusComponent() {
    return myViewer;
  }

  private ChangeList[] getSelectedChangeLists() {
    return new ChangeList[]{mySelectedChangeList};
  }

  private VirtualFile[] getSelectedFiles() {
    final Change[] changes = myViewer.getSelectedChanges();
    ArrayList<VirtualFile> files = new ArrayList<VirtualFile>();
    for (Change change : changes) {
      final ContentRevision afterRevision = change.getAfterRevision();
      if (afterRevision != null) {
        final VirtualFile file = afterRevision.getFile().getVirtualFile();
        if (file != null && file.isValid()) {
          files.add(file);
        }
      }
    }
    return files.toArray(new VirtualFile[files.size()]);
  }

  public class ToggleShowDirectoriesAction extends ToggleAction {
    public ToggleShowDirectoriesAction() {
      super(VcsBundle.message("changes.action.show.directories.text"),
            VcsBundle.message("changes.action.show.directories.description"),
            Icons.DIRECTORY_CLOSED_ICON);
    }

    public boolean isSelected(AnActionEvent e) {
      return !PropertiesComponent.getInstance(myProject).isTrueValue(FLATTEN_OPTION_KEY);
    }

    public void setSelected(AnActionEvent e, boolean state) {
      PropertiesComponent.getInstance(myProject).setValue(FLATTEN_OPTION_KEY, String.valueOf(!state));
      myViewer.setShowFlatten(!state);
    }
  }

  public class RollbackAction extends AnAction {
    public RollbackAction() {
      super(VcsBundle.message("changes.action.rollback.text"), VcsBundle.message("changes.action.rollback.description"),
            IconLoader.getIcon("/actions/rollback.png"));
    }

    public void actionPerformed(AnActionEvent e) {
      Change[] changes = e.getData(DataKeys.CHANGES);
      RollbackChangesDialog.rollbackChanges(myProject, Arrays.asList(changes), true);
      ChangeListManager.getInstance(myProject).ensureUpToDate(false);
      rebuildList();
    }

    public void update(AnActionEvent e) {
      Change[] changes = e.getData(DataKeys.CHANGES);
      e.getPresentation().setEnabled(changes != null);
    }
  }

  private class MyChangeListListener implements ChangeListListener {
    public void changeListAdded(ChangeList list) {
      if (myChangeListChooser != null && myShowingAllChangeLists) {
        myChangeListChooser.updateLists(ChangeListManager.getInstance(myProject).getChangeLists());
      }
    }

    public void changeListRemoved(ChangeList list) {
    }

    public void changeListChanged(ChangeList list) {
    }

    public void changeListRenamed(ChangeList list, String oldName) {
    }

    public void changesMoved(Collection<Change> changes, ChangeList fromList, ChangeList toList) {
    }

    public void defaultListChanged(ChangeList newDefaultList) {
    }

    public void changeListUpdateDone() {
    }
  }
}