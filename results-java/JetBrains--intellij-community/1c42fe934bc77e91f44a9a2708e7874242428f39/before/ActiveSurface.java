package org.hanuna.gitalk.ui.frame;

import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.actionSystem.TypeSafeDataProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.committed.CommittedChangesTreeBrowser;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowser;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ArrayUtil;
import com.intellij.vcs.log.VcsCommit;
import org.hanuna.gitalk.data.VcsLogDataHolder;
import org.hanuna.gitalk.graph.elements.Node;
import org.hanuna.gitalk.ui.VcsLogUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Graph + the panel with branch labels above it.
 *
 * @author Kirill Likhodedov
 */
public class ActiveSurface extends JPanel implements TypeSafeDataProvider {

  private static final Logger LOG = Logger.getInstance("Vcs.Log");

  @NotNull private final VcsLogGraphTable myGraphTable;
  @NotNull private final BranchesPanel myBranchesPanel;
  @NotNull private final VcsLogDataHolder myLogDataHolder;
  @NotNull private final DetailsPanel myDetailsPanel;
  @NotNull private final Splitter myDetailsSplitter;

  ActiveSurface(@NotNull VcsLogDataHolder logDataHolder, @NotNull VcsLogUI vcsLogUI, @NotNull Project project) {
    myLogDataHolder = logDataHolder;
    myGraphTable = new VcsLogGraphTable(vcsLogUI);
    myBranchesPanel = new BranchesPanel(logDataHolder, vcsLogUI);
    myDetailsPanel = new DetailsPanel(logDataHolder, myGraphTable);

    final ChangesBrowser changesBrowser = new ChangesBrowser(project, null, Collections.<Change>emptyList(), null, false, false, null,
                                                       ChangesBrowser.MyUseCase.COMMITTED_CHANGES, null);
    changesBrowser.getDiffAction().registerCustomShortcutSet(CommonShortcuts.getDiff(), myGraphTable);
    setDefaultEmptyText(changesBrowser);

    myGraphTable.getSelectionModel().addListSelectionListener(new CommitSelectionListener(changesBrowser));
    myGraphTable.getSelectionModel().addListSelectionListener(myDetailsPanel);

    myDetailsSplitter = new Splitter(true, 0.7f);
    myDetailsSplitter.setFirstComponent(ScrollPaneFactory.createScrollPane(myGraphTable));

    Splitter splitter = new Splitter(false, 0.7f);
    splitter.setFirstComponent(myDetailsSplitter);
    splitter.setSecondComponent(changesBrowser);

    setLayout(new BorderLayout());
    add(myBranchesPanel, BorderLayout.NORTH);
    add(splitter, BorderLayout.CENTER);
  }

  private static void setDefaultEmptyText(ChangesBrowser changesBrowser) {
    changesBrowser.getViewer().setEmptyText("No commits selected");
  }

  @NotNull
  public VcsLogGraphTable getGraphTable() {
    return myGraphTable;
  }

  @NotNull
  public BranchesPanel getBranchesPanel() {
    return myBranchesPanel;
  }

  @Override
  public void calcData(DataKey key, DataSink sink) {
    if (VcsDataKeys.CHANGES.equals(key)) {
      if (myGraphTable.getSelectedRowCount() != 1) {
        return;
      }
      Node node = myGraphTable.getSelectedNodes().get(0);
      try {
        VcsCommit commitData = myLogDataHolder.getDataPack().getCommitDataGetter().getCommitData(node);
        sink.put(VcsDataKeys.CHANGES, ArrayUtil.toObjectArray(commitData.getChanges(), Change.class));
      }
      catch (VcsException e) {
        LOG.error(e);
      }
    }
  }

  public void setupDetailsSplitter(boolean state) {
    myDetailsSplitter.setSecondComponent(state ? myDetailsPanel : null);
  }

  private class CommitSelectionListener implements ListSelectionListener {
    private final ChangesBrowser myChangesBrowser;

    public CommitSelectionListener(ChangesBrowser changesBrowser) {
      myChangesBrowser = changesBrowser;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
      int rows = myGraphTable.getSelectedRowCount();
      if (rows < 1) {
        setDefaultEmptyText(myChangesBrowser);
        myChangesBrowser.setChangesToDisplay(Collections.<Change>emptyList());
      }
      else {
        List<Change> changes = new ArrayList<Change>();
        for (Node node : myGraphTable.getSelectedNodes()) {
          try {
            VcsCommit commitData = myLogDataHolder.getDataPack().getCommitDataGetter().getCommitData(node);
            changes.addAll(commitData.getChanges());
          }
          catch (VcsException ex) {
            LOG.error(ex);
          }
        }
        myChangesBrowser.setChangesToDisplay(CommittedChangesTreeBrowser.zipChanges(changes));
      }
    }
  }
}