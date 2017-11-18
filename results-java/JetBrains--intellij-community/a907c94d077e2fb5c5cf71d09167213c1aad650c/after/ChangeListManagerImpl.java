package com.intellij.openapi.vcs.changes;

import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.ide.actions.DeleteAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.ui.ChangesListView;
import com.intellij.openapi.vcs.changes.ui.CommitChangeListDialog;
import com.intellij.openapi.vcs.changes.ui.RollbackChangesDialog;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.util.Alarm;
import com.intellij.util.EventDispatcher;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ui.tree.TreeUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author max
 */
public class ChangeListManagerImpl extends ChangeListManager implements ProjectComponent, ChangeListOwner, JDOMExternalizable {
  private Project myProject;
  private final ProjectLevelVcsManager myVcsManager;
  private static final String TOOLWINDOW_ID = VcsBundle.message("changes.toolwindow.name");

  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"})
  private static Alarm ourUpdateAlarm = new Alarm(Alarm.ThreadToUse.OWN_THREAD);

  private Alarm myRepaintAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

  private boolean myInitilized = false;

  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"})
  private boolean myDisposed = false;

  private boolean SHOW_FLATTEN_MODE = true;

  private UnversionedFilesHolder myUnversionedFilesHolder;
  private DeletedFilesHolder myDeletedFilesHolder = new DeletedFilesHolder();
  private final List<ChangeList> myChangeLists = new ArrayList<ChangeList>();

  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"})
  private ChangeList myDefaultChangelist;

  private ChangesListView myView;
  private JLabel myProgressLabel;

  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"})
  private EventDispatcher<ChangeListListener> myListeners = EventDispatcher.create(ChangeListListener.class);

  private final Object myPendingUpdatesLock = new Object();
  private boolean myUpdateInProgress = false;

  @NonNls private static final String ATT_FLATTENED_VIEW = "flattened_view";
  @NonNls private static final String NODE_LIST = "list";
  @NonNls private static final String ATT_NAME = "name";
  @NonNls private static final String NODE_CHANGE = "change";
  @NonNls private static final String ATT_DEFAULT = "default";
  @NonNls private static final String ATT_VALUE_TRUE = "true";
  @NonNls private static final String ATT_CHANGE_TYPE = "type";
  @NonNls private static final String ATT_CHANGE_BEFORE_PATH = "beforePath";
  @NonNls private static final String ATT_CHANGE_AFTER_PATH = "afterPath";
  private List<CommitExecutor> myExecutors = new ArrayList<CommitExecutor>();

  public ChangeListManagerImpl(final Project project, ProjectLevelVcsManager vcsManager) {
    myProject = project;
    myVcsManager = vcsManager;
    myView = new ChangesListView(project);
    myUnversionedFilesHolder = new UnversionedFilesHolder(project);
  }

  public void projectOpened() {
    if (myChangeLists.isEmpty()) {
      final ChangeList list = ChangeList.createEmptyChangeList(VcsBundle.message("changes.default.changlist.name"));
      myChangeLists.add(list);
      setDefaultChangeList(list);
    }

    StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable() {
      public void run() {
        ToolWindowManager.getInstance(myProject).registerToolWindow(TOOLWINDOW_ID, createChangeViewComponent(), ToolWindowAnchor.BOTTOM);
        myInitilized = true;
      }
    });
  }

  public void projectClosed() {
    myDisposed = true;
    ourUpdateAlarm.cancelAllRequests();
    myRepaintAlarm.cancelAllRequests();

    ToolWindowManager.getInstance(myProject).unregisterToolWindow(TOOLWINDOW_ID);
    myView.dispose();
  }

  @NonNls
  public String getComponentName() {
    return "ChangeListManager";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  private JComponent createChangeViewComponent() {
    JPanel panel = new JPanel(new BorderLayout());
    DefaultActionGroup modelActionsGroup = new DefaultActionGroup();

    RefreshAction refreshAction = new RefreshAction();
    refreshAction.registerCustomShortcutSet(CommonShortcuts.getRerun(), panel);

    AddChangeListAction newChangeListAction = new AddChangeListAction();
    newChangeListAction.registerCustomShortcutSet(CommonShortcuts.getNew(), panel);

    final RemoveChangeListAction removeChangeListAction = new RemoveChangeListAction();
    removeChangeListAction.registerCustomShortcutSet(CommonShortcuts.DELETE, panel);

    final ShowDiffAction diffAction = new ShowDiffAction();
    diffAction.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                                                                                      SystemInfo.isMac
                                                                                      ? KeyEvent.META_DOWN_MASK
                                                                                      : KeyEvent.CTRL_DOWN_MASK)),
                                         panel);

    final MoveChangesToAnotherListAction toAnotherListAction = new MoveChangesToAnotherListAction();
    toAnotherListAction.registerCustomShortcutSet(CommonShortcuts.getMove(), panel);

    final SetDefaultChangeListAction setDefaultChangeListAction = new SetDefaultChangeListAction();
    final CommitAction commitAction = new CommitAction();
    final RollbackAction rollbackAction = new RollbackAction();

    modelActionsGroup.add(refreshAction);
    modelActionsGroup.add(commitAction);

    for (CommitExecutor executor : myExecutors) {
      modelActionsGroup.add(new CommitUsingExecutorAction(executor));
    }

    modelActionsGroup.add(rollbackAction);
    modelActionsGroup.add(newChangeListAction);
    modelActionsGroup.add(removeChangeListAction);
    modelActionsGroup.add(setDefaultChangeListAction);
    modelActionsGroup.add(toAnotherListAction);
    modelActionsGroup.add(diffAction);

    JPanel toolbarPanel = new JPanel(new BorderLayout());
    toolbarPanel.add(createToolbarComponent(modelActionsGroup), BorderLayout.WEST);

    DefaultActionGroup visualActionsGroup = new DefaultActionGroup();
    final Expander expander = new Expander();
    visualActionsGroup.add(CommonActionsManager.getInstance().createCollapseAllAction(expander));
    visualActionsGroup.add(CommonActionsManager.getInstance().createExpandAllAction(expander));

    ToggleShowFlattenAction showFlattenAction = new ToggleShowFlattenAction();
    showFlattenAction.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                                                                                             SystemInfo.isMac
                                                                                             ? KeyEvent.META_DOWN_MASK
                                                                                             : KeyEvent.CTRL_DOWN_MASK)),
                                                panel);
    visualActionsGroup.add(showFlattenAction);
    toolbarPanel.add(createToolbarComponent(visualActionsGroup), BorderLayout.CENTER);


    DefaultActionGroup menuGroup = new DefaultActionGroup();
    menuGroup.add(refreshAction);

    menuGroup.add(commitAction);
    for (CommitExecutor executor : myExecutors) {
      modelActionsGroup.add(new CommitUsingExecutorAction(executor));
    }

    menuGroup.add(rollbackAction);
    menuGroup.add(newChangeListAction);
    menuGroup.add(removeChangeListAction);
    menuGroup.add(setDefaultChangeListAction);
    menuGroup.add(toAnotherListAction);
    menuGroup.add(diffAction);
    menuGroup.addSeparator();
    menuGroup.add(ActionManager.getInstance().getAction(IdeActions.GROUP_VERSION_CONTROLS));
    menuGroup.add(new DeleteAction());
    menuGroup.add(new ScheduleForAdditionAction());
    menuGroup.add(new ScheduleForRemovalAction());
    menuGroup.addSeparator();
    menuGroup.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));

    myView.setMenuActions(menuGroup);

    myView.setShowFlatten(SHOW_FLATTEN_MODE);

    myProgressLabel = new JLabel();

    panel.add(toolbarPanel, BorderLayout.WEST);
    panel.add(new JScrollPane(myView), BorderLayout.CENTER);
    panel.add(myProgressLabel, BorderLayout.SOUTH);

    myView.installDndSupport(this);
    return panel;
  }

  private static JComponent createToolbarComponent(final DefaultActionGroup group) {
    return ActionManager.getInstance().createActionToolbar(ActionPlaces.CHANGES_VIEW, group, false).getComponent();
  }

  private class Expander implements TreeExpander {
    public void expandAll() {
      TreeUtil.expandAll(myView);
    }

    public boolean canExpand() {
      return true;
    }

    public void collapseAll() {
      TreeUtil.collapseAll(myView, 2);
      TreeUtil.expand(myView, 1);
    }

    public boolean canCollapse() {
      return true;
    }
  }

  private void updateProgressText(final String text) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        myProgressLabel.setText(text);
      }
    });
  }

  public boolean ensureUpToDate(boolean canBeCanceled) {
    final boolean ok = ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) {
          indicator.setText(VcsBundle.message("commit.wait.util.synced.message"));
        }

        synchronized (myPendingUpdatesLock) {
          scheduleUpdate(10);
          while (ourUpdateAlarm.getActiveRequestCount() > 0 || myUpdateInProgress) {
            if (indicator != null && indicator.isCanceled()) break;

            try {
              myPendingUpdatesLock.wait(100);
            }
            catch (InterruptedException e) {
              break;
            }
          }
        }
      }
    }, VcsBundle.message("commit.wait.util.synced.title"), canBeCanceled, myProject);

    if (ok) {
      refreshView();
    }

    return ok;
  }

  private static class DisposedException extends RuntimeException {}

  private void scheduleUpdate(int millis) {
    ourUpdateAlarm.cancelAllRequests();
    ourUpdateAlarm.addRequest(new Runnable() {
      public void run() {
        if (myDisposed) return;
        if (!myInitilized) {
          scheduleUpdate();
          return;
        }

        updateImmediately();
      }

      private void updateImmediately() {
        try {
          synchronized (myPendingUpdatesLock) {
            myUpdateInProgress = true;
          }

          if (myDisposed) return;

          final List<VcsDirtyScope> scopes = ((VcsDirtyScopeManagerImpl)VcsDirtyScopeManager.getInstance(myProject)).retreiveScopes();
          for (final VcsDirtyScope scope : scopes) {
            updateProgressText(VcsBundle.message("changes.update.progress.message", scope.getScopeRoot().getPresentableUrl()));
            ApplicationManager.getApplication().runReadAction(new Runnable() {
              public void run() {
                synchronized (myChangeLists) {
                  for (ChangeList list : getChangeLists()) {
                    if (myDisposed) return;
                    list.startProcessingChanges(scope);
                  }
                }
              }
            });

            final UnversionedFilesHolder unversionedHolder = myUnversionedFilesHolder.copy();
            unversionedHolder.cleanScope(scope);

            final DeletedFilesHolder deletedHolder = myDeletedFilesHolder.copy();
            deletedHolder.cleanScope(scope);

            try {
              final AbstractVcs vcs = myVcsManager.getVcsFor(scope.getScopeRoot());
              if (vcs != null) {
                final ChangeProvider changeProvider = vcs.getChangeProvider();
                if (changeProvider != null) {
                  changeProvider.getChanges(scope, new ChangelistBuilder() {
                    public void processChange(final Change change) {
                      if (myDisposed) throw new DisposedException();

                      ApplicationManager.getApplication().runReadAction(new Runnable() {
                        public void run() {
                          if (isUnder(change, scope)) {
                            try {
                              synchronized (myChangeLists) {
                                for (ChangeList list : myChangeLists) {
                                  if (list == myDefaultChangelist) continue;
                                  if (list.processChange(change)) return;
                                }

                                myDefaultChangelist.processChange(change);
                              }
                            }
                            finally {
                              scheduleRefresh();
                            }
                          }
                        }
                      });
                    }

                    public void processUnversionedFile(VirtualFile file) {
                      if (myDisposed) throw new DisposedException();
                      if (scope.belongsTo(PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(file))) {
                        unversionedHolder.addFile(file);
                        scheduleRefresh();
                      }
                    }

                    public void processLocallyDeletedFile(File file) {
                      if (myDisposed) throw new DisposedException();
                      if (scope.belongsTo(PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(file))) {
                        deletedHolder.addFile(file);
                        scheduleRefresh();
                      }
                    }


                    public void processModifiedWithoutEditing(VirtualFile file) {
                      if (myDisposed) throw new DisposedException();
                      //TODO:
                    }
                  }, null); // TODO: make real indicator
                }
              }
            }
            finally {
              if (!myDisposed) {
                synchronized (myChangeLists) {
                  for (ChangeList list : myChangeLists) {
                    if (list.doneProcessingChanges()) {
                      myListeners.getMulticaster().changeListChanged(list);
                    }
                  }
                }
              }
            }

            myUnversionedFilesHolder = unversionedHolder;
            myDeletedFilesHolder = deletedHolder;
            scheduleRefresh();
          }
        }
        catch (DisposedException e) {
          // OK, we're finishing all the stuff now.
        }
        finally {
          updateProgressText("");
          synchronized (myPendingUpdatesLock) {
            myUpdateInProgress = false;
            myPendingUpdatesLock.notifyAll();
          }
        }
      }

      private boolean isUnder(final Change change, final VcsDirtyScope scope) {
        final ContentRevision before = change.getBeforeRevision();
        final ContentRevision after = change.getAfterRevision();
        return before != null && scope.belongsTo(before.getFile()) || after != null && scope.belongsTo(after.getFile());
      }
    }, millis);
  }

  public void scheduleUpdate() {
    scheduleUpdate(300);
  }

  private void scheduleRefresh() {
    myRepaintAlarm.cancelAllRequests();
    myRepaintAlarm.addRequest(new Runnable() {
      public void run() {
        refreshView();
      }
    }, 100, ModalityState.NON_MMODAL);
  }

  private void refreshView() {
    if (myDisposed) return;
    myView.updateModel(getChangeListsCopy(),
                       new ArrayList<VirtualFile>(myUnversionedFilesHolder.getFiles()),
                       new ArrayList<File>(myDeletedFilesHolder.getFiles()));
  }

  private List<ChangeList> getChangeListsCopy() {
    synchronized (myChangeLists) {
      List<ChangeList> copy = new ArrayList<ChangeList>(myChangeLists.size());
      for (ChangeList list : myChangeLists) {
        copy.add(list.clone());
      }
      return copy;
    }
  }

  @NotNull
  public List<ChangeList> getChangeLists() {
    synchronized (myChangeLists) {
      return myChangeLists;
    }
  }

  public List<File> getAffectedPaths() {
    List<File> files = new ArrayList<File>();
    for (ChangeList list : myChangeLists) {
      final Collection<Change> changes = list.getChanges();
      for (Change change : changes) {
        File beforeFile = null;
        ContentRevision beforeRevision = change.getBeforeRevision();
        if (beforeRevision != null) {
          beforeFile = beforeRevision.getFile().getIOFile();
        }

        if (beforeFile != null) {
          files.add(beforeFile);
        }

        ContentRevision afterRevision = change.getAfterRevision();
        if (afterRevision != null) {
          final File afterFile = afterRevision.getFile().getIOFile();
          if (afterFile != null && !afterFile.equals(beforeFile)) {
            files.add(afterFile);
          }
        }
      }
    }

    return files;
  }


  @NotNull
  public List<VirtualFile> getAffectedFiles() {
    List<VirtualFile> files = new ArrayList<VirtualFile>();
    for (ChangeList list : myChangeLists) {
      final Collection<Change> changes = list.getChanges();
      for (Change change : changes) {
        final ContentRevision afterRevision = change.getAfterRevision();
        if (afterRevision != null) {
          final VirtualFile vFile = afterRevision.getFile().getVirtualFile();
          if (vFile != null) {
            files.add(vFile);
          }
        }
      }
    }
    return files;
  }

  public boolean isFileAffected(final VirtualFile file) {
    synchronized (myChangeLists) {
      for (ChangeList list : myChangeLists) {
        for (Change change : list.getChanges()) {
          final ContentRevision afterRevision = change.getAfterRevision();
          if (afterRevision != null && afterRevision.getFile().getVirtualFile() == file) return true;
        }
      }
    }

    return myUnversionedFilesHolder.containsFile(file);
  }

  public ChangeList addChangeList(String name) {
    synchronized (myChangeLists) {
      final ChangeList list = ChangeList.createEmptyChangeList(name);
      myChangeLists.add(list);
      myListeners.getMulticaster().changeListAdded(list);
      scheduleRefresh();
      return list;
    }
  }

  public void removeChangeList(ChangeList list) {
    synchronized (myChangeLists) {
      if (list.isDefault()) throw new RuntimeException(new IncorrectOperationException("Cannot remove default changelist"));

      final Collection<Change> changes = list.getChanges();
      for (Change change : changes) {
        addChangeToList(change, myDefaultChangelist);
      }
      myChangeLists.remove(list);
      myListeners.getMulticaster().changeListRemoved(list);

      scheduleRefresh();
    }
  }

  private void addChangeToList(final Change change, final ChangeList list) {
    list.addChange(change);
    myListeners.getMulticaster().changeListChanged(list);
  }

  private void removeChangeFromList(final Change change, final ChangeList list) {
    list.removeChange(change);
    myListeners.getMulticaster().changeListChanged(list);
  }

  public void setDefaultChangeList(ChangeList list) {
    synchronized (myChangeLists) {
      if (myDefaultChangelist != null) myDefaultChangelist.setDefault(false);
      list = findRealByCopy(list);
      list.setDefault(true);
      myDefaultChangelist = list;
      myListeners.getMulticaster().defaultListChanged(list);
      scheduleRefresh();
    }
  }

  private ChangeList findRealByCopy(ChangeList list) {
    for (ChangeList changeList : myChangeLists) {
      if (changeList.equals(list)) {
        return changeList;
      }
    }
    return list;
  }

  public ChangeList getChangeList(Change change) {
    synchronized (myChangeLists) {
      for (ChangeList list : myChangeLists) {
        if (list.getChanges().contains(change)) return list;
      }
      return null;
    }
  }

  @Nullable
  public Change getChange(VirtualFile file) {
    synchronized (myChangeLists) {
      for (ChangeList list : myChangeLists) {
        for (Change change : list.getChanges()) {
          final ContentRevision afterRevision = change.getAfterRevision();
          if (afterRevision != null && afterRevision.getFile().getVirtualFile() == file) return change;
        }
      }

      return null;
    }
  }

  private boolean fileIsInContent(VirtualFile file) {
    return ProjectRootManager.getInstance(myProject).getFileIndex().isInContent(file);
  }

  @NotNull
  public FileStatus getStatus(VirtualFile file) {
    if (!fileIsInContent(file)) return FileStatus.NOT_CHANGED;

    if (myUnversionedFilesHolder.containsFile(file)) return FileStatus.UNKNOWN;
    final AbstractVcs vcs = myVcsManager.getVcsFor(file);
    if (vcs == null) return FileStatus.NOT_CHANGED;
    final Change change = getChange(file);
    return change == null ? FileStatus.NOT_CHANGED : change.getFileStatus();
  }

  @NotNull
  public Collection<Change> getChangesIn(VirtualFile dir) {
    return getChangesIn(PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(dir));
  }

  @NotNull
  public Collection<Change> getChangesIn(final FilePath dirPath) {
    synchronized (myChangeLists) {
      List<Change> changes = new ArrayList<Change>();
      for (ChangeList list : myChangeLists) {
        for (Change change : list.getChanges()) {
          final ContentRevision afterRevision = change.getAfterRevision();
          if (afterRevision != null && afterRevision.getFile().isUnder(dirPath, false)) {
            changes.add(change);
            continue;
          }

          final ContentRevision beforeRevision = change.getBeforeRevision();
          if (beforeRevision != null && beforeRevision.getFile().isUnder(dirPath, false)) {
            changes.add(change);
          }
        }
      }

      return changes;
    }
  }

  public class ToggleShowFlattenAction extends ToggleAction {
    public ToggleShowFlattenAction() {
      super(VcsBundle.message("changes.action.show.directories.text"),
            VcsBundle.message("changes.action.show.directories.description"),
            Icons.DIRECTORY_CLOSED_ICON);
    }

    public boolean isSelected(AnActionEvent e) {
      return !SHOW_FLATTEN_MODE;
    }

    public void setSelected(AnActionEvent e, boolean state) {
      SHOW_FLATTEN_MODE = !state;
      myView.setShowFlatten(SHOW_FLATTEN_MODE);
      refreshView();
    }
  }

  public class RefreshAction extends AnAction {
    public RefreshAction() {
      super(VcsBundle.message("changes.action.refresh.text"),
            VcsBundle.message("changes.action.refresh.description"),
            IconLoader.getIcon("/actions/sync.png"));
    }

    public void actionPerformed(AnActionEvent e) {
      VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    }
  }

  public class AddChangeListAction extends AnAction {
    public AddChangeListAction() {
      super(VcsBundle.message("changes.action.new.changelist.text"),
            VcsBundle.message("changes.action.new.changelist.description"),
            IconLoader.getIcon("/actions/include.png"));
    }

    public void actionPerformed(AnActionEvent e) {
      String rc = Messages.showInputDialog(myProject, VcsBundle.message("changes.dialog.newchangelist.text"),
                                           VcsBundle.message("changes.dialog.newchangelist.title"), Messages.getQuestionIcon());
      if (rc != null) {
        if (rc.length() == 0) {
          rc = getUniqueName();
        }

        addChangeList(rc);
      }
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    private String getUniqueName() {
      synchronized (myChangeLists) {
        int unnamedcount = 0;
        for (ChangeList list : getChangeLists()) {
          if (list.getDescription().startsWith("Unnamed")) {
            unnamedcount++;
          }
        }

        return unnamedcount == 0 ? "Unnamed" : "Unnamed (" + unnamedcount + ")";
      }
    }
  }

  public class SetDefaultChangeListAction extends AnAction {
    public SetDefaultChangeListAction() {
      super(VcsBundle.message("changes.action.setdefaultchangelist.text"),
            VcsBundle.message("changes.action.setdefaultchangelist.description"), IconLoader.getIcon("/actions/submit1.png"));
    }


    public void update(AnActionEvent e) {
      ChangeList[] lists = (ChangeList[])e.getDataContext().getData(DataConstants.CHANGE_LISTS);
      e.getPresentation().setEnabled(lists != null && lists.length == 1 && !lists[0].isDefault());
    }

    public void actionPerformed(AnActionEvent e) {
      final ChangeList[] lists = ((ChangeList[])e.getDataContext().getData(DataConstants.CHANGE_LISTS));
      assert lists != null;
      setDefaultChangeList(lists[0]);
    }
  }

  @Nullable
  private ChangeList getChangeListIfOnlyOne(Change[] changes) {
    if (changes == null || changes.length == 0) {
      return null;
    }

    ChangeList selectedList = null;
    for (Change change : changes) {
      final ChangeList list = getChangeList(change);
      if (selectedList == null) {
        selectedList = list;
      }
      else if (selectedList != list) {
        return null;
      }
    }
    return selectedList;
  }

  private class CommitUsingExecutorAction extends AnAction {
    private CommitExecutor myExecutor;

    public CommitUsingExecutorAction(CommitExecutor executor) {
      super(executor.getActionText(), executor.getActionDescription(), executor.getActionIcon());
      myExecutor = executor;
    }

    public void update(AnActionEvent e) {
      Change[] changes = (Change[])e.getDataContext().getData(DataConstants.CHANGES);
      e.getPresentation().setEnabled(getChangeListIfOnlyOne(changes) != null);
    }

    public void actionPerformed(AnActionEvent e) {
      Change[] changes = (Change[])e.getDataContext().getData(DataConstants.CHANGES);
      final ChangeList list = getChangeListIfOnlyOne(changes);
      if (list == null) return;

      CommitChangeListDialog.commitChanges(myProject, Arrays.asList(changes), myExecutor);
    }
  }

  public class CommitAction extends AnAction {
    public CommitAction() {
      super(VcsBundle.message("changes.action.commit.text"), VcsBundle.message("changes.action.commit.description"),
            IconLoader.getIcon("/actions/execute.png"));
    }

    public void update(AnActionEvent e) {
      Change[] changes = (Change[])e.getDataContext().getData(DataConstants.CHANGES);
      e.getPresentation().setEnabled(getChangeListIfOnlyOne(changes) != null);
    }

    public void actionPerformed(AnActionEvent e) {
      Change[] changes = (Change[])e.getDataContext().getData(DataConstants.CHANGES);
      final ChangeList list = getChangeListIfOnlyOne(changes);
      if (list == null) return;

      CommitChangeListDialog.commitChanges(myProject, Arrays.asList(changes));
    }
  }

  public class RollbackAction extends AnAction {
    public RollbackAction() {
      super("Rollback", "Rollback selected changes",
            IconLoader.getIcon("/actions/rollback.png"));
    }

    public void update(AnActionEvent e) {
      Change[] changes = (Change[])e.getDataContext().getData(DataConstants.CHANGES);
      e.getPresentation().setEnabled(getChangeListIfOnlyOne(changes) != null);
    }

    public void actionPerformed(AnActionEvent e) {
      Change[] changes = (Change[])e.getDataContext().getData(DataConstants.CHANGES);
      final ChangeList list = getChangeListIfOnlyOne(changes);
      if (list == null) return;

      RollbackChangesDialog.rollbackChanges(myProject, Arrays.asList(changes));
    }
  }

  public class ScheduleForAdditionAction extends AnAction {
    public ScheduleForAdditionAction() {
      super("Add to VCS", "Schedule selected files to be added to VCS",
            IconLoader.getIcon("/actions/include.png"));
    }

    public void update(AnActionEvent e) {
      List<VirtualFile> files = (List<VirtualFile>)e.getDataContext().getData(ChangesListView.UNVERSIONED_FILES_KEY);
      boolean enabled = files != null && !files.isEmpty();
      e.getPresentation().setEnabled(enabled);
      e.getPresentation().setVisible(enabled);
    }

    public void actionPerformed(AnActionEvent e) {
      final List<VirtualFile> files = (List<VirtualFile>)e.getDataContext().getData(ChangesListView.UNVERSIONED_FILES_KEY);
      if (files == null) return;

      ChangesUtil.processVirtualFilesByVcs(myProject, files, new ChangesUtil.PerVcsProcessor<VirtualFile>() {
        public void process(final AbstractVcs vcs, final List<VirtualFile> items) {
          final ChangeProvider provider = vcs.getChangeProvider();
          if (provider != null) {
            provider.scheduleUnversionedFilesForAddition(files);
          }
        }
      });

      for (VirtualFile file : files) {
        VcsDirtyScopeManager.getInstance(myProject).fileDirty(file);
        FileStatusManager.getInstance(myProject).fileStatusChanged(file);
      }

      scheduleRefresh();
    }
  }

  public class ScheduleForRemovalAction extends AnAction {
    public ScheduleForRemovalAction() {
      super("Remove from VCS", "Schedule selected files to be removed from VCS",
            IconLoader.getIcon("/actions/exclude.png"));
    }

    public void update(AnActionEvent e) {
      List<File> files = (List<File>)e.getDataContext().getData(ChangesListView.MISSING_FILES_KEY);
      boolean enabled = files != null && !files.isEmpty();
      e.getPresentation().setEnabled(enabled);
      e.getPresentation().setVisible(enabled);
    }

    public void actionPerformed(AnActionEvent e) {
      final List<File> files = (List<File>)e.getDataContext().getData(ChangesListView.MISSING_FILES_KEY);
      if (files == null) return;

      ChangesUtil.processIOFilesByVcs(myProject, files, new ChangesUtil.PerVcsProcessor<File>() {
        public void process(final AbstractVcs vcs, final List<File> items) {
          final ChangeProvider provider = vcs.getChangeProvider();
          if (provider != null) {
            provider.scheduleMissingFileForDeletion(files);
          }
        }
      });

      for (File file : files) {
        FilePath path = PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(file);
        VcsDirtyScopeManager.getInstance(myProject).fileDirty(path);
      }
      scheduleRefresh();
    }
  }

  public static class RemoveChangeListAction extends AnAction {
    public RemoveChangeListAction() {
      super(VcsBundle.message("changes.action.removechangelist.text"), VcsBundle.message("changes.action.removechangelist.description"), IconLoader.getIcon("/actions/exclude.png"));
    }

    public void update(AnActionEvent e) {
      Project project = (Project)e.getDataContext().getData(DataConstants.PROJECT);
      ChangeList[] lists = (ChangeList[])e.getDataContext().getData(DataConstants.CHANGE_LISTS);
      e.getPresentation().setEnabled(project != null && lists != null && lists.length == 1 && !lists[0].isDefault());
    }

    public void actionPerformed(AnActionEvent e) {
      Project project = (Project)e.getDataContext().getData(DataConstants.PROJECT);
      final ChangeList[] lists = ((ChangeList[])e.getDataContext().getData(DataConstants.CHANGE_LISTS));
      assert lists != null;
      final ChangeList list = lists[0];
      int rc = list.getChanges().size() == 0 ? DialogWrapper.OK_EXIT_CODE :
               Messages.showYesNoDialog(project,
                                        VcsBundle.message("changes.removechangelist.warning.text", list.getDescription()),
                                        VcsBundle.message("changes.removechangelist.warning.title"),
                                        Messages.getQuestionIcon());

      if (rc == DialogWrapper.OK_EXIT_CODE) {
        ChangeListManager.getInstance(project).removeChangeList(list);
      }
    }
  }

  public void moveChangesTo(final ChangeList list, final Change[] changes) {
    synchronized (myChangeLists) {
      for (ChangeList existingList : getChangeLists()) {
        for (Change change : changes) {
          removeChangeFromList(change, existingList);
        }
      }

      for (Change change : changes) {
        addChangeToList(change, list);
      }
    }

    scheduleRefresh();
  }

  public void addChangeListListner(ChangeListListener listener) {
    myListeners.addListener(listener);
  }


  public void removeChangeListListner(ChangeListListener listener) {
    myListeners.removeListener(listener);
  }

  public void registerCommitExecutor(CommitExecutor executor) {
    myExecutors.add(executor);
  }

  @SuppressWarnings({"unchecked"})
  public void readExternal(Element element) throws InvalidDataException {
    SHOW_FLATTEN_MODE = Boolean.valueOf(element.getAttributeValue(ATT_FLATTENED_VIEW)).booleanValue();

    final List<Element> listNodes = (List<Element>)element.getChildren(NODE_LIST);
    for (Element listNode : listNodes) {
      ChangeList list = addChangeList(listNode.getAttributeValue(ATT_NAME));
      final List<Element> changeNodes = (List<Element>)listNode.getChildren(NODE_CHANGE);
      for (Element changeNode : changeNodes) {
        try {
          addChangeToList(readChange(changeNode), list);
        }
        catch (OutdatedFakeRevisionException e) {
          // Do nothing. Just skip adding outdated revisions to the list.
        }
      }

      if (ATT_VALUE_TRUE.equals(listNode.getAttributeValue(ATT_DEFAULT))) {
        setDefaultChangeList(list);
      }
    }

    if (myChangeLists.size() > 0 && myDefaultChangelist == null) {
      setDefaultChangeList(myChangeLists.get(0));
    }
  }

  public void writeExternal(Element element) throws WriteExternalException {
    element.setAttribute(ATT_FLATTENED_VIEW, String.valueOf(SHOW_FLATTEN_MODE));

    synchronized (myChangeLists) {
      for (ChangeList list : myChangeLists) {
        Element listNode = new Element(NODE_LIST);
        element.addContent(listNode);
        if (list.isDefault()) {
          listNode.setAttribute(ATT_DEFAULT, ATT_VALUE_TRUE);
        }

        listNode.setAttribute(ATT_NAME, list.getDescription());
        for (Change change : list.getChanges()) {
          writeChange(listNode, change);
        }
      }
    }
  }

  private static void writeChange(final Element listNode, final Change change) {
    Element changeNode = new Element(NODE_CHANGE);
    listNode.addContent(changeNode);
    changeNode.setAttribute(ATT_CHANGE_TYPE, change.getType().name());

    final ContentRevision bRev = change.getBeforeRevision();
    final ContentRevision aRev = change.getAfterRevision();

    changeNode.setAttribute(ATT_CHANGE_BEFORE_PATH, bRev != null ? bRev.getFile().getPath() : "");
    changeNode.setAttribute(ATT_CHANGE_AFTER_PATH, aRev != null ? aRev.getFile().getPath() : "");
  }

  private static Change readChange(Element changeNode) throws OutdatedFakeRevisionException {
    String bRev = changeNode.getAttributeValue(ATT_CHANGE_BEFORE_PATH);
    String aRev = changeNode.getAttributeValue(ATT_CHANGE_AFTER_PATH);
    return new Change(StringUtil.isEmpty(bRev) ? null : new FakeRevision(bRev), StringUtil.isEmpty(aRev) ? null : new FakeRevision(aRev));
  }

  private static final class OutdatedFakeRevisionException extends Exception {}

  private static class FakeRevision implements ContentRevision {
    private final FilePath myFile;

    public FakeRevision(String path) throws OutdatedFakeRevisionException {
      final FilePath file = PeerFactory.getInstance().getVcsContextFactory().createFilePathOn(new File(path));
      if (file == null) throw new OutdatedFakeRevisionException();
      myFile = file;
    }

    @Nullable
    public String getContent() { return null; }

    @NotNull
    public FilePath getFile() {
      return myFile;
    }
  }
}