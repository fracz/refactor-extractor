package com.intellij.openapi.vcs.changes.ui;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.localVcs.LocalVcs;
import com.intellij.openapi.localVcs.LvcsAction;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputException;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.checkin.VcsOperation;
import com.intellij.openapi.vcs.ui.CheckinDialog;
import com.intellij.openapi.vcs.ui.CommitMessage;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.Alarm;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * @author max
 */
public class CommitChangeListDialog extends DialogWrapper implements CheckinProjectPanel, DataProvider {
  private CommitMessage myCommitMessageArea;
  private Splitter myRootPane;
  private JPanel myAdditionalOptionsPanel;

  private ChangesBrowser myBrowser;

  private List<RefreshableOnComponent> myAdditionalComponents = new ArrayList<RefreshableOnComponent>();
  private List<CheckinHandler> myHandlers = new ArrayList<CheckinHandler>();
  private String myActionName;
  private Project myProject;
  private final CommitSession mySession;
  private final Alarm myOKButtonUpdateAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

  private static void commit(Project project, List<ChangeList> list, final List<Change> changes, final CommitExecutor executor) {
    new CommitChangeListDialog(project, list, changes, executor).show();
  }

  public static void commitPaths(final Project project, Collection<FilePath> paths) {
    final ChangeListManager manager = ChangeListManager.getInstance(project);
    final Collection<Change> changes = new HashSet<Change>();
    for (FilePath path : paths) {
      changes.addAll(manager.getChangesIn(path));
    }

    commitChanges(project, changes);
  }

  public static void commitChanges(final Project project, final Collection<Change> changes, final CommitExecutor executor) {
    final ChangeListManager manager = ChangeListManager.getInstance(project);

    if (changes.isEmpty()) {
      Messages.showWarningDialog(project, VcsBundle.message("commit.dialog.no.changes.detected.text") ,
                                 VcsBundle.message("commit.dialog.no.changes.detected.title"));
      return;
    }

    Set<ChangeList> lists = new THashSet<ChangeList>();
    for (Change change : changes) {
      lists.add(manager.getChangeList(change));
    }

    commit(project, new ArrayList<ChangeList>(lists), new ArrayList<Change>(changes), executor);
  }

  public static void commitChanges(final Project project, final Collection<Change> changes) {
    commitChanges(project, changes, null);
  }

  private CommitChangeListDialog(final Project project,
                                 List<ChangeList> changeLists,
                                 final List<Change> changes,
                                 final CommitExecutor executor) {
    super(project, true);
    myProject = project;
    mySession = executor != null ? executor.createCommitSession() : null;

    myBrowser = new ChangesBrowser(project, changeLists, changes);

    myActionName = executor != null ? executor.getActionDescription() : VcsBundle.message("commit.dialog.title");

    myAdditionalOptionsPanel = new JPanel();
    myCommitMessageArea = new CommitMessage();

    setCommitMessage(CheckinDialog.getInitialMessage(getPaths(), project));
    myCommitMessageArea.init();

    myAdditionalOptionsPanel.setLayout(new BorderLayout());
    Box optionsBox = Box.createVerticalBox();

    boolean hasVcsOptions = false;
    Box vcsCommitOptions = Box.createVerticalBox();
    if (executor == null) {
      hasVcsOptions = false;
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
    }
    else {
      final JComponent ui = mySession.getAdditionalConfigurationUI();
      if (ui != null) {
        vcsCommitOptions.add(ui);
        hasVcsOptions = true;
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

    setOKButtonText(executor != null ? executor.getActionText() : getCommitActionName());

    setTitle(myActionName);

    restoreState();

    myOKButtonUpdateAlarm.addRequest(new Runnable() {
      public void run() {
        myOKButtonUpdateAlarm.cancelAllRequests();
        myOKButtonUpdateAlarm.addRequest(this, 300, ModalityState.stateForComponent(myBrowser));
        updateButtons();
      }
    }, 300, ModalityState.stateForComponent(myBrowser));

    init();
  }


  @Override
  protected void dispose() {
    super.dispose();
    myOKButtonUpdateAlarm.cancelAllRequests();
    myBrowser.dispose();
  }

  private String getCommitActionName() {
    String name = null;
    for (AbstractVcs vcs : getAffectedVcses()) {
      final CheckinEnvironment env = vcs.getCheckinEnvironment();
      if (env != null) {
        if (name == null) {
          name = env.getCheckinOperationName();
        }
        else {
          name = VcsBundle.message("commit.dialog.default.commit.operation.name");
        }
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

  public boolean runBeforeCommitHandlers() {
    for (CheckinHandler handler : myHandlers) {
      final CheckinHandler.ReturnResult result = handler.beforeCheckin();
      if (result == CheckinHandler.ReturnResult.COMMIT) continue;
      if (result == CheckinHandler.ReturnResult.CANCEL) return false;
      if (result == CheckinHandler.ReturnResult.CLOSE_WINDOW) {
        doCancelAction();
        return false;
      }
    }

    return true;
  }


  public void doCancelAction() {
    if (mySession != null) {
      mySession.executionCanceled();
    }

    super.doCancelAction();
  }

  protected void doOKAction() {
    if (!checkComment()) {
      return;
    }

    VcsConfiguration.getInstance(myProject).saveCommitMessage(getCommitMessage());
    try {
      saveState();

      if (!runBeforeCommitHandlers()) {
        return;
      }

      doCommit();

      super.doOKAction();
    }
    catch (InputException ex) {
      ex.show();
    }
  }

  private void doCommit() {
    final Runnable checkinAction = new Runnable() {
      public void run() {
        final List<VcsException> vcsExceptions = new ArrayList<VcsException>();
        final List<Change> changesFailedToCommit = new ArrayList<Change>();

        ProgressManager.getInstance()
          .runProcessWithProgressSynchronously(checkinAction(vcsExceptions, changesFailedToCommit), myActionName, true, myProject);
      }
    };

    AbstractVcsHelper.getInstance(myProject).optimizeImportsAndReformatCode(getVirtualFiles(),
                                                                            VcsConfiguration.getInstance(myProject), checkinAction, true);
  }

  private Runnable checkinAction(final List<VcsException> vcsExceptions, final List<Change> changesFailedToCommit) {
    if (mySession != null) {
      return new Runnable() {
        public void run() {
          mySession.execute(myBrowser.getCurrentIncludedChanges(), getCommitMessage());
        }
      };
    }

    return new Runnable() {
      public void run() {
        try {
          final List<FilePath> pathsToRefresh = new ArrayList<FilePath>();
          ChangesUtil.processChangesByVcs(myProject, myBrowser.getCurrentIncludedChanges(), new ChangesUtil.PerVcsProcessor<Change>() {
            public void process(AbstractVcs vcs, List<Change> changes) {
              final CheckinEnvironment environment = vcs.getCheckinEnvironment();
              if (environment != null) {
                List<FilePath> paths = new ArrayList<FilePath>();
                for (Change change : changes) {
                  paths.add(ChangesUtil.getFilePath(change));
                }

                pathsToRefresh.addAll(paths);

                final List<VcsException> exceptions =
                  environment.commit(paths.toArray(new FilePath[paths.size()]), myProject, getCommitMessage());
                if (exceptions.size() > 0) {
                  vcsExceptions.addAll(exceptions);
                  changesFailedToCommit.addAll(changes);
                }
              }
            }
          });

          final LvcsAction lvcsAction = LocalVcs.getInstance(myProject).startAction(myActionName, "", true);
          VirtualFileManager.getInstance().refresh(true, new Runnable() {
            public void run() {
              lvcsAction.finish();
              FileStatusManager.getInstance(myProject).fileStatusesChanged();
              for (FilePath path : pathsToRefresh) {
                VcsDirtyScopeManager.getInstance(myProject).fileDirty(path);
              }
            }
          });
          AbstractVcsHelper.getInstance(myProject).showErrors(vcsExceptions, myActionName);
        }
        finally {
          commitCompleted(vcsExceptions, changesFailedToCommit, VcsConfiguration.getInstance(myProject), myHandlers, getCommitMessage());
        }
      }
    };
  }

  private void commitCompleted(final List<VcsException> allExceptions,
                               final List<Change> failedChanges,
                               VcsConfiguration config,
                               final List<CheckinHandler> checkinHandlers,
                               String commitMessage) {
    final List<VcsException> errors = collectErrors(allExceptions);
    final int errorsSize = errors.size();
    final int warningsSize = allExceptions.size() - errorsSize;

    if (errorsSize == 0) {
      for (CheckinHandler handler : checkinHandlers) {
        handler.checkinSuccessful();
      }
    }
    else {
      for (CheckinHandler handler : checkinHandlers) {
        handler.checkinFailed(errors);
      }

      final ChangeListManager changeListManager = ChangeListManager.getInstance(myProject);
      final ChangeList failedList =
        changeListManager.addChangeList(VcsBundle.message("commit.dialog.failed.commit.template", commitMessage));

      changeListManager.moveChangesTo(failedList, failedChanges.toArray(new Change[failedChanges.size()]));
    }

    config.ERROR_OCCURED = errorsSize > 0;


    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        if (errorsSize > 0 && warningsSize > 0) {
          Messages.showErrorDialog(VcsBundle.message("message.text.commit.failed.with.errors.and.warnings"),
                                   VcsBundle.message("message.title.commit"));
        }
        else if (errorsSize > 0) {
          Messages.showErrorDialog(VcsBundle.message("message.text.commit.failed.with.errors"), VcsBundle.message("message.title.commit"));
        }
        else if (warningsSize > 0) {
          Messages
            .showErrorDialog(VcsBundle.message("message.text.commit.finished.with.warnings"), VcsBundle.message("message.title.commit"));
        }

      }
    }, ModalityState.NON_MMODAL);

  }

  private static List<VcsException> collectErrors(final List<VcsException> vcsExceptions) {
    final ArrayList<VcsException> result = new ArrayList<VcsException>();
    for (VcsException vcsException : vcsExceptions) {
      if (!vcsException.isWarning()) {
        result.add(vcsException);
      }
    }
    return result;
  }

  @Nullable
  protected JComponent createCenterPanel() {
    myRootPane = new Splitter(true);
    myRootPane.setHonorComponentsMinimumSize(true);

    myRootPane.setFirstComponent(myBrowser);

    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add(myAdditionalOptionsPanel, BorderLayout.EAST);
    bottomPanel.add(myCommitMessageArea, BorderLayout.CENTER);

    myRootPane.setSecondComponent(bottomPanel);
    myRootPane.setProportion(0.5f);
    return myRootPane;
  }

  public List<AbstractVcs> getAffectedVcses() {
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
    return myRootPane;
  }

  public boolean hasDiffs() {
    return !myBrowser.getCurrentIncludedChanges().isEmpty();
  }

  public void addSelectionChangeListener(SelectionChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  public void removeSelectionChangeListener(SelectionChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  public Collection<VirtualFile> getVirtualFiles() {
    List<VirtualFile> result = new ArrayList<VirtualFile>();
    for (Change change: myBrowser.getCurrentIncludedChanges()) {
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
    for (Change change: myBrowser.getCurrentIncludedChanges()) {
      final FilePath path = ChangesUtil.getFilePath(change);
      final File file = path.getIOFile();
      if (file != null) {
        result.add(file);
      }
    }

    return result;
  }

  private FilePath[] getPaths() {
    List<FilePath> result = new ArrayList<FilePath>();
    for (Change change : myBrowser.getCurrentIncludedChanges()) {
      result.add(ChangesUtil.getFilePath(change));
    }
    return result.toArray(new FilePath[result.size()]);
  }

  public Project getProject() {
    return myProject;
  }

  public List<VcsOperation> getCheckinOperations(CheckinEnvironment checkinEnvironment) {
    throw new UnsupportedOperationException();
  }

  public void setCommitMessage(final String currentDescription) {
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
    setOKActionEnabled(hasDiffs() &&
                       (mySession == null || mySession.canExecute(myBrowser.getCurrentIncludedChanges(), getCommitMessage())));
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

  @Nullable
  public Object getData(String dataId) {
    if (CheckinProjectPanel.PANEL.equals(dataId)) {
      return this;
    }
    return myBrowser.getData(dataId);
  }
}