/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 05.12.2006
 * Time: 19:39:22
 */
package com.intellij.openapi.vcs.changes.committed;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.CommittedChangesProvider;
import com.intellij.openapi.vcs.RepositoryLocation;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.ui.FilterComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommittedChangesPanel extends JPanel implements TypeSafeDataProvider {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.vcs.changes.committed.CommittedChangesPanel");

  private CommittedChangesBrowser myBrowser;
  private final Project myProject;
  private CommittedChangesProvider myProvider;
  private ChangeBrowserSettings mySettings;
  private RepositoryLocation myLocation;
  private int myMaxCount = 0;
  private FilterComponent myFilterComponent = new MyFilterComponent();
  private List<CommittedChangeList> myChangesFromProvider;

  public CommittedChangesPanel(Project project, final CommittedChangesProvider provider, final ChangeBrowserSettings settings) {
    super(new BorderLayout());
    mySettings = settings;
    myProject = project;
    myProvider = provider;
    myBrowser = new CommittedChangesBrowser(project, new CommittedChangesTableModel(new ArrayList<CommittedChangeList>()));
    add(myBrowser, BorderLayout.CENTER);

    JPanel toolbarPanel = new JPanel(new BorderLayout());
    ActionGroup group = (ActionGroup) ActionManager.getInstance().getAction("CommittedChangesToolbar");
    ActionToolbar toolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true);
    toolbarPanel.add(toolBar.getComponent(), BorderLayout.WEST);
    toolbarPanel.add(myFilterComponent, BorderLayout.EAST);
    myBrowser.addToolBar(toolbarPanel);
    myBrowser.setTableContextMenu(group);
    final AnAction anAction = ActionManager.getInstance().getAction("CommittedChanges.Refresh");
    anAction.registerCustomShortcutSet(CommonShortcuts.getRerun(), this);
  }

  public void setRepositoryLocation(final RepositoryLocation location) {
    myLocation = location;
  }

  public void setMaxCount(final int maxCount) {
    myMaxCount = maxCount;
  }

  public void setProvider(final CommittedChangesProvider provider) {
    if (myProvider != provider) {
      myProvider = provider;
      mySettings = provider.createDefaultSettings();
    }
  }

  public void refreshChanges() {
    final Ref<VcsException> refEx = new Ref<VcsException>();
    final Ref<List<CommittedChangeList>> changes = new Ref<List<CommittedChangeList>>();
    boolean completed = ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        try {
          if (myLocation == null) {
            changes.set(myProvider.getAllCommittedChanges(mySettings, myMaxCount));
          }
          else {
            changes.set(myProvider.getCommittedChanges(mySettings, myLocation, myMaxCount));
          }
        }
        catch (VcsException ex) {
          refEx.set(ex);
        }
      }
    }, "Loading changes", true, myProject);
    if (!refEx.isNull()) {
      LOG.info(refEx.get());
      Messages.showErrorDialog(myProject, "Error refreshing view: " + StringUtil.join(refEx.get().getMessages(), "\n"), "Committed Changes");
    }
    else if (completed) {
      myChangesFromProvider = changes.get();
      updateFilteredModel();
    }
  }

  private void updateFilteredModel() {
    if (myChangesFromProvider == null) {
      return;
    }
    if (StringUtil.isEmpty(myFilterComponent.getFilter())) {
      myBrowser.setModel(new CommittedChangesTableModel(myChangesFromProvider, myProvider.getColumns()));
    }
    else {
      final String[] strings = myFilterComponent.getFilter().split(" ");
      for(int i=0; i<strings.length; i++) {
        strings [i] = strings [i].toLowerCase();
      }
      List<CommittedChangeList> filteredChanges = new ArrayList<CommittedChangeList>();
      for(CommittedChangeList changeList: myChangesFromProvider) {
        if (changeListMatches(changeList, strings)) {
          filteredChanges.add(changeList);
        }
      }
        myBrowser.setModel(new CommittedChangesTableModel(filteredChanges, myProvider.getColumns()));
    }
  }

  private static boolean changeListMatches(@NotNull final CommittedChangeList changeList, final String[] filterWords) {
    for(String word: filterWords) {
      final String comment = changeList.getComment();
      final String committer = changeList.getCommitterName();
      if ((comment != null && comment.toLowerCase().indexOf(word) >= 0) ||
          (committer != null && committer.toLowerCase().indexOf(word) >= 0) ||
          Long.toString(changeList.getNumber()).indexOf(word) >= 0) {
        return true;
      }
    }
    return false;
  }

  public void setChangesFilter() {
    CommittedChangesFilterDialog filterDialog = new CommittedChangesFilterDialog(myProject, myProvider.createFilterUI(true), mySettings);
    filterDialog.show();
    if (filterDialog.isOK()) {
      mySettings = filterDialog.getSettings();
      refreshChanges();
    }
  }

  public void calcData(DataKey key, DataSink sink) {
    if (key.equals(DataKeys.CHANGES)) {
      final CommittedChangeList list = myBrowser.getSelectedChangeList();
      if (list != null) {
        final Collection<Change> changes = list.getChanges();
        sink.put(DataKeys.CHANGES, changes.toArray(new Change[changes.size()]));
      }
    }
    else if (key.equals(DataKeys.CHANGE_LISTS)) {
      final CommittedChangeList list = myBrowser.getSelectedChangeList();
      if (list != null) {
        sink.put(DataKeys.CHANGE_LISTS, new ChangeList[] { list });
      }
    }
  }

  private class MyFilterComponent extends FilterComponent {
    public MyFilterComponent() {
      super("COMMITTED_CHANGES_FILTER_HISTORY", 20);
    }

    public void filter() {
      updateFilteredModel();
    }
  }
}