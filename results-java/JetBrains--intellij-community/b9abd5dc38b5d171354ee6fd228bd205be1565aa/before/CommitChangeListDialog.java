package com.intellij.openapi.vcs.changes.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputException;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.CommitMessage;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.Alarm;
import com.intellij.util.OpenSourceUtil;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * @author max
 */
public class CommitChangeListDialog extends DialogWrapper implements CheckinProjectPanel, TypeSafeDataProvider {
  private CommitMessage myCommitMessageArea;
  private Splitter mySplitter;
  private JPanel myAdditionalOptionsPanel;

  private ChangesBrowser myBrowser;
  private CommitLegendPanel myLegend;

  private List<RefreshableOnComponent> myAdditionalComponents = new ArrayList<RefreshableOnComponent>();
  private List<CheckinHandler> myHandlers = new ArrayList<CheckinHandler>();
  private String myActionName;
  private Project myProject;
  private final List<CommitExecutor> myExecutors;
  private final Alarm myOKButtonUpdateAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);
  private String myLastKnownComment = "";
  private boolean myAllOfDefaultChangeListChangesIncluded;
  @NonNls private static final String SPLITTER_PROPORTION_OPTION = "CommitChangeListDialog.SPLITTER_PROPORTION";
  private final Action[] myExecutorActions;
  private boolean myShowVcsCommit;

  private static void commit(Project project, final List<Change> changes, final ChangeList initialSelection,
                             final List<CommitExecutor> executors, boolean showVcsCommit) {
    new CommitChangeListDialog(project, changes, initialSelection, executors, showVcsCommit).show();
  }

  public static void commitPaths(final Project project, Collection<FilePath> paths, final ChangeList initialSelection) {
    final ChangeListManager manager = ChangeListManager.getInstance(project);
    final Collection<Change> changes = new HashSet<Change>();
    for (FilePath path : paths) {
      changes.addAll(manager.getChangesIn(path));
    }

    commitChanges(project, changes, initialSelection, manager.getRegisteredExecutors(), true);
  }

  public static void commitPaths(final Project project, Collection<FilePath> paths, final ChangeList initialSelection,
                                 @Nullable final CommitExecutor executor) {
    final ChangeListManager manager = ChangeListManager.getInstance(project);
    final Collection<Change> changes = new HashSet<Change>();
    for (FilePath path : paths) {
      changes.addAll(manager.getChangesIn(path));
    }

    if (executor == null) {
      commitChanges(project, changes, initialSelection, manager.getRegisteredExecutors(), true);
    }
    else {
      commitChanges(project, changes, initialSelection, Collections.singletonList(executor), false);
    }
  }

  /*
  public static void commitChanges(final Project project, final Collection<Change> changes, final CommitExecutor executor) {
    final ChangeListManager manager = ChangeListManager.getInstance(project);

    if (changes.isEmpty()) {
      Messages.showWarningDialog(project, VcsBundle.message("commit.dialog.no.changes.detected.text") ,
                                 VcsBundle.message("commit.dialog.no.changes.detected.title"));
      return;
    }

    Set<LocalChangeList> lists = new THashSet<LocalChangeList>();
    for (Change change : changes) {
      lists.add(manager.getChangeList(change));
    }

    commit(project, new ArrayList<LocalChangeList>(lists), new ArrayList<Change>(changes), executor);
  }
  */

  public static void commitChanges(final Project project, final Collection<Change> changes, final ChangeList initialSelection,
                                   final List<CommitExecutor> executors, final boolean showVcsCommit) {
    if (changes.isEmpty()) {
      Messages.showWarningDialog(project, VcsBundle.message("commit.dialog.no.changes.detected.text") ,
                                 VcsBundle.message("commit.dialog.no.changes.detected.title"));
      return;
    }

    commit(project, new ArrayList<Change>(changes), initialSelection, executors, showVcsCommit);
  }

  private CommitChangeListDialog(final Project project,
                                 final List<Change> changes,
                                 final ChangeList initialSelection,
                                 final List<CommitExecutor> executors,
                                 final boolean showVcsCommit) {
    super(project, true);
    myProject = project;
    myExecutors = executors;
    myShowVcsCommit = showVcsCommit;
    if (!myShowVcsCommit && myExecutors.size() == 0) {
      throw new IllegalArgumentException("nothing found to execute commit with");
    }

    final ChangeListManager manager = ChangeListManager.getInstance(project);
    LocalChangeList defaultList = manager.getDefaultChangeList();
    ArrayList<LocalChangeList> changeLists = new ArrayList<LocalChangeList>(manager.getChangeLists());
    myAllOfDefaultChangeListChangesIncluded = changes.containsAll(defaultList.getChanges());

    myBrowser = new ChangesBrowser(project, changeLists, changes, initialSelection, true, true);
    myBrowser.addRollbackAction();
    myBrowser.addSelectedListChangeListener(new ChangesBrowser.SelectedListChangeListener() {
      public void selectedListChanged() {
        updateComment();
      }
    });

    final EditSourceAction editSourceAction = new EditSourceAction();
    editSourceAction.registerCustomShortcutSet(CommonShortcuts.getEditSource(), myBrowser);
    myBrowser.addToolbarAction(editSourceAction);

    myBrowser.addToolbarActions(CommitMessage.getToolbarActions());

    myCommitMessageArea = new CommitMessage(false);
    setCommitMessage(getInitialMessage(getPaths(), project));
    myCommitMessageArea.init();

    updateComment();

    myActionName = VcsBundle.message("commit.dialog.title");

    myAdditionalOptionsPanel = new JPanel();

    myAdditionalOptionsPanel.setLayout(new BorderLayout());
    Box optionsBox = Box.createVerticalBox();

    boolean hasVcsOptions = false;
    Box vcsCommitOptions = Box.createVerticalBox();
    final List<AbstractVcs> vcses = getAffectedVcses();
    for (AbstractVcs vcs : vcses) {
      final CheckinEnvironment checkinEnvironment = vcs.getCheckinEnvironment();
      if (checkinEnvironment != null) {
        final RefreshableOnComponent options = checkinEnvironment.createAdditionalOptionsPanelForCheckinProject(this);
        if (options != null) {
          JPanel vcsOptions = new JPanel(new BorderLayout());
          vcsOptions.add(options.getComponent());
          vcsOptions.setBorder(IdeBorderFactory.createTitledHeaderBorder(vcs.getDisplayName()));
          vcsCommitOptions.add(vcsOptions);
          myAdditionalComponents.add(options);
          hasVcsOptions = true;
        }
      }
    }

    if (hasVcsOptions) {
      vcsCommitOptions.add(Box.createVerticalGlue());
      optionsBox.add(vcsCommitOptions);
    }

    boolean beforeVisible = false;
    boolean afterVisible = false;
    Box beforeBox = Box.createVerticalBox();
    Box afterBox = Box.createVerticalBox();
    final List<CheckinHandlerFactory> handlerFactories = ProjectLevelVcsManager.getInstance(project).getRegisteredCheckinHandlerFactories();
    for (CheckinHandlerFactory factory : handlerFactories) {
      final CheckinHandler handler = factory.createHandler(this);
      myHandlers.add(handler);
      final RefreshableOnComponent beforePanel = handler.getBeforeCheckinConfigurationPanel();
      if (beforePanel != null) {
        beforeBox.add(beforePanel.getComponent());
        beforeVisible = true;
        myAdditionalComponents.add(beforePanel);
      }

      final RefreshableOnComponent afterPanel = handler.getAfterCheckinConfigurationPanel();
      if (afterPanel != null) {
        afterBox.add(afterPanel.getComponent());
        afterVisible = true;
        myAdditionalComponents.add(afterPanel);
      }
    }

    if (beforeVisible) {
      beforeBox.add(Box.createVerticalGlue());
      beforeBox.setBorder(IdeBorderFactory.createTitledHeaderBorder(VcsBundle.message("border.standard.checkin.options.group")));
      optionsBox.add(beforeBox);
    }

    if (afterVisible) {
      afterBox.add(Box.createVerticalGlue());
      afterBox.setBorder(IdeBorderFactory.createTitledHeaderBorder(VcsBundle.message("border.standard.after.checkin.options.group")));
      optionsBox.add(afterBox);
    }

    if (hasVcsOptions || beforeVisible || afterVisible) {
      optionsBox.add(Box.createVerticalGlue());
      myAdditionalOptionsPanel.add(optionsBox, BorderLayout.NORTH);
    }

    setOKButtonText(getCommitActionName());

    if (myShowVcsCommit) {
      setTitle(myActionName);
    }
    else {
      setTitle(myExecutors.get(0).getActionText());
    }

    restoreState();
    myExecutorActions = new Action[myExecutors.size()];

    for (int i = 0; i < myExecutors.size(); i++) {
      final CommitExecutor commitExecutor = myExecutors.get(i);
      myExecutorActions[i] = new AbstractAction(commitExecutor.getActionText()) {
        public void actionPerformed(ActionEvent e) {
          execute(commitExecutor);
        }
      };
    }

    init();
    updateButtons();

  }


  protected Action[] createActions() {
    Action[] result;
    if (myShowVcsCommit) {
      result = new Action[2 + myExecutors.size()];
      result[0] = getOKAction();
      System.arraycopy(myExecutorActions, 0, result, 1, myExecutorActions.length);
    }
    else {
      result = new Action[1 + myExecutors.size()];
      System.arraycopy(myExecutorActions, 0, result, 0, myExecutorActions.length);
    }
    result[result.length - 1] = getCancelAction();
    return result;
  }

  private void execute(final CommitExecutor commitExecutor) {
    if (!checkComment()) {
      return;
    }

    VcsConfiguration.getInstance(myProject).saveCommitMessage(getCommitMessage());

    saveState();
    final CommitSession session = commitExecutor.createCommitSession();
    boolean isOK = true;
    if (session.getAdditionalConfigurationUI() != null) {
      DialogWrapper sessionDialog = new SessionDialog(commitExecutor.getActionText(),
                                                      getProject(),
                                                      session,
                                                      getIncludedChanges(),
                                                      getCommitMessage());
      sessionDialog.show();
      isOK = sessionDialog.isOK();
    }
    if (isOK) {
      runBeforeCommitHandlers(new Runnable() {
        public void run() {
          try {
            final boolean completed = ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
              public void run() {
                session.execute(getIncludedChanges(), getCommitMessage());
              }
            }, commitExecutor.getActionText(), true, getProject());

            if (completed) {
              for (CheckinHandler handler : myHandlers) {
                handler.checkinSuccessful();
              }
              close(OK_EXIT_CODE);
            }
            else {
              session.executionCanceled();
            }
          }
          catch (Throwable e) {
            Messages.showErrorDialog(VcsBundle.message("error.executing.commit", commitExecutor.getActionText(), e.getLocalizedMessage()),
                                     commitExecutor.getActionText());

            for (CheckinHandler handler : myHandlers) {
              handler.checkinFailed(Arrays.asList(new VcsException[]{new VcsException(e)}));
            }
          }
        }
      });


    }
    else {
      session.executionCanceled();
    }
  }

  private static String getInitialMessage(FilePath[] filesToCheckin, Project project) {
    if (filesToCheckin != null) {
      for (FilePath virtualFile : filesToCheckin) {
        AbstractVcs activeVcs = VcsUtil.getVcsFor(project, virtualFile);
        if (activeVcs == null) continue;
        CheckinEnvironment checkinEnvironment = activeVcs.getCheckinEnvironment();
        if (checkinEnvironment != null) {
          String defaultMessage = checkinEnvironment.getDefaultMessageFor(filesToCheckin);
          if (defaultMessage != null) return defaultMessage;
        }

      }
    }

    VcsConfiguration config = VcsConfiguration.getInstance(project);

    if (config.SAVE_LAST_COMMIT_MESSAGE || config.ERROR_OCCURED) {
      return config.LAST_COMMIT_MESSAGE;
    }
    else {
      return "";
    }
  }

  private void updateComment() {
    final LocalChangeList list = (LocalChangeList)myBrowser.getSelectedChangeList();

    String listComment = list.getComment();
    if (StringUtil.isEmptyOrSpaces(listComment)) {
      final String listTitle = list.getName();
      if (!VcsBundle.message("changes.default.changlist.name").equals(listTitle)) {
        listComment = listTitle;
      }
      else {
        listComment = myLastKnownComment;
      }
    }

    myCommitMessageArea.setText(listComment);
  }


  @Override
  public void dispose() {
    myBrowser.dispose();
    super.dispose();
    myOKButtonUpdateAlarm.cancelAllRequests();
    PropertiesComponent.getInstance().setValue(SPLITTER_PROPORTION_OPTION, String.valueOf(mySplitter.getProportion()));
  }

  private String getCommitActionName() {
    String name = null;
    for (AbstractVcs vcs : getAffectedVcses()) {
      if (name == null && vcs.getCheckinEnvironment() != null) {
        name = vcs.getCheckinEnvironment().getCheckinOperationName();
      }
      else {
        name = VcsBundle.message("commit.dialog.default.commit.operation.name");
      }
    }
    return name != null ? name : VcsBundle.message("commit.dialog.default.commit.operation.name");
  }

  private boolean checkComment() {
    if (VcsConfiguration.getInstance(myProject).FORCE_NON_EMPTY_COMMENT && (getCommitMessage().length() == 0)) {
      int requestForCheckin = Messages.showYesNoDialog(VcsBundle.message("confirmation.text.check.in.with.empty.comment"),
                                                       VcsBundle.message("confirmation.title.check.in.with.empty.comment"),
                                                       Messages.getWarningIcon());
      return requestForCheckin == OK_EXIT_CODE;
    }
    else {
      return true;
    }
  }

  private void runBeforeCommitHandlers(final Runnable okAction) {
    Runnable proceedRunnable = new Runnable() {
      public void run() {
        FileDocumentManager.getInstance().saveAllDocuments();

        for (CheckinHandler handler : myHandlers) {
          final CheckinHandler.ReturnResult result = handler.beforeCheckin();
          if (result == CheckinHandler.ReturnResult.COMMIT) continue;
          if (result == CheckinHandler.ReturnResult.CANCEL) return;

          if (result == CheckinHandler.ReturnResult.CLOSE_WINDOW) {
            final ChangeList changeList = myBrowser.getSelectedChangeList();
            CommitHelper.moveToFailedList(changeList,
                             getCommitMessage(),
                             getIncludedChanges(),
                             VcsBundle.message("commit.dialog.rejected.commit.template", changeList.getName()),
                             myProject);
            doCancelAction();
            return;
          }
        }

        okAction.run();
      }
    };

    AbstractVcsHelper.getInstance(myProject).optimizeImportsAndReformatCode(getVirtualFiles(),
                                                                            VcsConfiguration.getInstance(myProject),
                                                                            proceedRunnable,
                                                                            true);
  }


  protected void doOKAction() {
    if (!checkComment()) {
      return;
    }

    VcsConfiguration.getInstance(myProject).saveCommitMessage(getCommitMessage());
    try {
      saveState();

      runBeforeCommitHandlers(new Runnable() {
        public void run() {
          CommitChangeListDialog.super.doOKAction();
          doCommit();
        }
      });

    }
    catch (InputException ex) {
      ex.show();
    }
  }

  @Override
  public void doCancelAction() {
    VcsConfiguration.getInstance(myProject).saveCommitMessage(getCommitMessage());
    super.doCancelAction();    //To change body of overridden methods use File | Settings | File Templates.
  }

  private void doCommit() {
    new CommitHelper(
      myProject,
      myBrowser.getSelectedChangeList(),
      getIncludedChanges(),
      myActionName,
      getCommitMessage(),
      myHandlers,
      myAllOfDefaultChangeListChangesIncluded, false).doCommit();
  }

  @Nullable
  protected JComponent createCenterPanel() {
    JPanel rootPane = new JPanel(new BorderLayout());

    mySplitter = new Splitter(true);
    mySplitter.setHonorComponentsMinimumSize(true);
    mySplitter.setFirstComponent(myBrowser);
    mySplitter.setSecondComponent(myCommitMessageArea);
    mySplitter.setProportion(calcSplitterProportion());
    rootPane.add(mySplitter, BorderLayout.CENTER);

    JComponent browserHeader = myBrowser.getHeaderPanel();
    myBrowser.remove(browserHeader);
    rootPane.add(browserHeader, BorderLayout.NORTH);

    JPanel infoPanel = new JPanel(new BorderLayout());
    myLegend = new CommitLegendPanel();
    infoPanel.add(myLegend.getComponent(), BorderLayout.NORTH);
    infoPanel.add(myAdditionalOptionsPanel, BorderLayout.CENTER);
    rootPane.add(infoPanel, BorderLayout.EAST);

    return rootPane;
  }

  private static float calcSplitterProportion() {
    try {
      final String s = PropertiesComponent.getInstance().getValue(SPLITTER_PROPORTION_OPTION);
      return s != null ? Float.valueOf(s).floatValue() : 0.5f;
    }
    catch (NumberFormatException e) {
      return 0.5f;
    }
  }

  public List<AbstractVcs> getAffectedVcses() {
    if (!myShowVcsCommit) {
      return Collections.emptyList();
    }
    Set<AbstractVcs> result = new HashSet<AbstractVcs>();
    for (Change change : myBrowser.getAllChanges()) {
      final AbstractVcs vcs = ChangesUtil.getVcsForChange(change, myProject);
      if (vcs != null) {
        result.add(vcs);
      }
    }
    return new ArrayList<AbstractVcs>(result);
  }

  public Collection<VirtualFile> getRoots() {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
    Set<VirtualFile> result = new HashSet<VirtualFile>();
    for (Change change : myBrowser.getCurrentDisplayedChanges()) {
      final FilePath filePath = ChangesUtil.getFilePath(change);
      VirtualFile root = VcsDirtyScope.getRootFor(fileIndex, filePath);
      if (root != null) {
        result.add(root);
      }
    }
    return result;
  }

  public JComponent getComponent() {
    return mySplitter;
  }

  public boolean hasDiffs() {
    return !getIncludedChanges().isEmpty();
  }

  public void addSelectionChangeListener(SelectionChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  public void removeSelectionChangeListener(SelectionChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  public Collection<VirtualFile> getVirtualFiles() {
    List<VirtualFile> result = new ArrayList<VirtualFile>();
    for (Change change: getIncludedChanges()) {
      final FilePath path = ChangesUtil.getFilePath(change);
      final VirtualFile vFile = path.getVirtualFile();
      if (vFile != null) {
        result.add(vFile);
      }
    }

    return result;
  }

  public Collection<File> getFiles() {
    List<File> result = new ArrayList<File>();
    for (Change change: getIncludedChanges()) {
      final FilePath path = ChangesUtil.getFilePath(change);
      final File file = path.getIOFile();
      result.add(file);
    }

    return result;
  }

  private FilePath[] getPaths() {
    List<FilePath> result = ChangesUtil.getPaths(getIncludedChanges());
    return result.toArray(new FilePath[result.size()]);
  }

  public Project getProject() {
    return myProject;
  }

  public void setCommitMessage(final String currentDescription) {
    myLastKnownComment = currentDescription;
    myCommitMessageArea.setText(currentDescription);
    myCommitMessageArea.requestFocusInMessage();
  }

  public String getCommitMessage() {
    return myCommitMessageArea.getComment();
  }

  public void refresh() {
    for (RefreshableOnComponent component : myAdditionalComponents) {
      component.refresh();
    }
  }

  public void saveState() {
    for (RefreshableOnComponent component : myAdditionalComponents) {
      component.saveState();
    }
  }

  public void restoreState() {
    for (RefreshableOnComponent component : myAdditionalComponents) {
      component.restoreState();
    }
  }

  private void updateButtons() {
    setOKActionEnabled(hasDiffs());
    for (Action executorAction : myExecutorActions) {
      executorAction.setEnabled(hasDiffs());
    }
    myOKButtonUpdateAlarm.cancelAllRequests();
    myOKButtonUpdateAlarm.addRequest(new Runnable() {
      public void run() {
        updateButtons();
        updateLegend();
      }
    }, 300, ModalityState.stateForComponent(myBrowser));
  }

  private void updateLegend() {
    myLegend.update(myBrowser.getCurrentDisplayedChanges(), myBrowser.getCurrentIncludedChanges());
  }

  private List<Change> getIncludedChanges() {
    return myBrowser.getCurrentIncludedChanges();
  }

  @NonNls
  protected String getDimensionServiceKey() {
    return "CommitChangelistDialog";
  }

  public JComponent getPreferredFocusedComponent() {
    if (VcsConfiguration.getInstance(myProject).PUT_FOCUS_INTO_COMMENT) {
      return myCommitMessageArea.getTextField();
    }
    else {
      return myBrowser.getPrefferedFocusComponent();
    }
  }

  public void calcData(DataKey key, DataSink sink) {
    if (key == CheckinProjectPanel.PANEL_KEY) {
      sink.put(CheckinProjectPanel.PANEL_KEY, this);
    }
    else {
      myBrowser.calcData(key, sink);
    }
  }

  private class EditSourceAction extends AnAction {
    public EditSourceAction() {
      super(ActionsBundle.actionText("EditSource"),
            ActionsBundle.actionDescription("EditSource"),
            IconLoader.getIcon("/actions/editSource.png"));
    }

    public void actionPerformed(AnActionEvent e) {
      final Navigatable[] navigatableArray = e.getData(DataKeys.NAVIGATABLE_ARRAY);
      if (navigatableArray != null && navigatableArray.length > 0) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            OpenSourceUtil.navigate(navigatableArray, true);
          }
        });
        doCancelAction();
      }
    }
  }
}