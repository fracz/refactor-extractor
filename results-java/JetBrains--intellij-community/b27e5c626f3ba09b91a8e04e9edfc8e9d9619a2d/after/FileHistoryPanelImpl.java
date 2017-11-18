package com.intellij.openapi.vcs.history;

import com.intellij.ide.BrowserUtil;
import com.intellij.history.LocalHistory;
import com.intellij.history.LocalHistoryAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.PanelWithActionsAndCloseButton;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.annotate.AnnotationProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vcs.changes.issueLinks.IssueLinkHtmlRenderer;
import com.intellij.openapi.vcs.changes.issueLinks.IssueLinkRenderer;
import com.intellij.openapi.vcs.changes.issueLinks.TableLinkMouseListener;
import com.intellij.openapi.vcs.fileView.DualViewColumnInfo;
import com.intellij.openapi.vcs.ui.ReplaceFileConfirmationDialog;
import com.intellij.openapi.vcs.vfs.VcsFileSystem;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.dualView.CellWrapper;
import com.intellij.ui.dualView.DualTreeElement;
import com.intellij.ui.dualView.DualView;
import com.intellij.util.Alarm;
import com.intellij.util.Icons;
import com.intellij.util.TreeItem;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.SortableColumnModel;
import com.intellij.util.ui.TableViewModel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * author: lesya
 */
public class FileHistoryPanelImpl extends PanelWithActionsAndCloseButton implements FileHistoryPanel {
  private static final Logger LOG = Logger.getInstance("#com.intellij.cvsSupport2.ui.FileHistoryDialog");

  private JEditorPane myComments;
  private final DefaultActionGroup myPopupActions;

  private final Project myProject;
  private final VcsHistoryProvider myProvider;
  private final AnnotationProvider myAnnotationProvider;
  private VcsHistorySession myHistorySession;
  private final FilePath myFilePath;
  private final DualView myDualView;

  private final Alarm myUpdateAlarm;

  private static final String COMMIT_MESSAGE_TITLE = VcsBundle.message("label.selected.revision.commit.message");

  public static final DualViewColumnInfo REVISION =
    new VcsColumnInfo<VcsRevisionNumber>(VcsBundle.message("column.name.revision.version")) {
      protected VcsRevisionNumber getDataOf(VcsFileRevision object) {
        return object.getRevisionNumber();
      }

      public String valueOf(VcsFileRevision object) {
        return object.getRevisionNumber().asString();
      }

      @Override
      public String getPreferredStringValue() {
        return "123.4567";
      }
    };

  public static final DualViewColumnInfo DATE = new VcsColumnInfo<String>(VcsBundle.message("column.name.revision.date")) {
    protected String getDataOf(VcsFileRevision object) {
      Date date = object.getRevisionDate();
      if (date == null) return "";
      return DATE_FORMAT.format(date);
    }

    public int compare(VcsFileRevision o1, VcsFileRevision o2) {
      return o1.getRevisionDate().compareTo(o2.getRevisionDate());
    }

    @Override
    public String getPreferredStringValue() {
      return DATE_FORMAT.format(new Date());
    }
  };

  public static final DualViewColumnInfo AUTHOR = new VcsColumnInfo<String>(VcsBundle.message("column.name.revision.list.author")) {
    protected String getDataOf(VcsFileRevision object) {
      return object.getAuthor();
    }

    @Override
    @NonNls
    public String getPreferredStringValue() {
      return "author_author";
    }
  };

  private static class MessageRenderer extends ColoredTableCellRenderer {
    private IssueLinkRenderer myIssueLinkRenderer;

    public MessageRenderer(Project project) {
      myIssueLinkRenderer = new IssueLinkRenderer(project, this);
    }

    protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
      setOpaque(selected);
      String message = (String) value;
      myIssueLinkRenderer.appendTextWithLinks(message);
      setToolTipText(message);
    }

    @Override
    protected void paintComponent(final Graphics g) {
      // skip border erasing code in SimpleColoredRenderer
      doPaint(g);
    }
  }

  private static class MessageColumnInfo extends VcsColumnInfo<String> {
    private MessageRenderer myRenderer;

    public MessageColumnInfo(Project project) {
      super(FileHistoryPanelImpl.COMMIT_MESSAGE_TITLE);
      myRenderer = new MessageRenderer(project);
    }

    protected String getDataOf(VcsFileRevision object) {
      final String originalMessage = object.getCommitMessage();
      if (originalMessage != null) {
        String commitMessage = originalMessage.trim();
        int index13 = commitMessage.indexOf('\r');
        int index10 = commitMessage.indexOf('\n');

        if (index10 < 0 && index13 < 0) {
          return commitMessage;
        }
        else {
          return commitMessage.substring(0, getSuitableIndex(index10, index13)) + "...";
        }
      }
      else {
        return "";
      }
    }

    private int getSuitableIndex(int index10, int index13) {
      if (index10 < 0) {
        return index13;
      }
      else if (index13 < 0) {
        return index10;
      }
      else {
        return Math.min(index10, index13);
      }
    }

    @Override
    public String getPreferredStringValue() {
      return StringUtil.repeatSymbol('a', 125);
    }

    public TableCellRenderer getRenderer(VcsFileRevision p0) {
      return myRenderer;
    }
  }

  private final DualViewColumnInfo[] COLUMNS;

  public static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
  private Map<VcsFileRevision, VirtualFile> myRevisionToVirtualFile = new HashMap<VcsFileRevision, VirtualFile>();


  public FileHistoryPanelImpl(Project project,
                              FilePath filePath,
                              VcsHistorySession session,
                              VcsHistoryProvider provider,
                              AnnotationProvider annotationProvider,
                              ContentManager contentManager) {
    super(contentManager, provider.getHelpId());
    myProvider = provider;
    myAnnotationProvider = annotationProvider;
    myProject = project;
    myHistorySession = session;
    myFilePath = filePath;

    COLUMNS = createColumnList(project, provider);

    myComments = new JEditorPane(UIUtil.HTML_MIME, "");
    myComments.setPreferredSize(new Dimension(150, 100));
    myComments.setEditable(false);
    myComments.setBackground(UIUtil.getComboBoxDisabledBackground());
    myComments.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(final HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          BrowserUtil.launchBrowser(e.getDescription());
        }
      }
    });

    myUpdateAlarm = new Alarm(session.allowAsyncRefresh() ? Alarm.ThreadToUse.SHARED_THREAD : Alarm.ThreadToUse.SWING_THREAD);

    HistoryAsTreeProvider treeHistoryProvider = provider.getTreeHistoryProvider();

    @NonNls String storageKey = "FileHistory." + provider.getClass().getName();
    if (treeHistoryProvider != null) {
      myDualView = new DualView(new TreeNodeOnVcsRevision(null, treeHistoryProvider.createTreeOn(myHistorySession.getRevisionList())),
                                COLUMNS, storageKey, project);
    }
    else {
      myDualView = new DualView(new TreeNodeOnVcsRevision(null, wrapWithTreeElements(myHistorySession.getRevisionList())), COLUMNS,
                                storageKey, project);
      myDualView.switchToTheFlatMode();
    }
    final TableLinkMouseListener listener = new TableLinkMouseListener();
    listener.install(myDualView.getFlatView());
    listener.install(myDualView.getTreeView());

    createDualView(null);

    myPopupActions = createPopupActions();

    myUpdateAlarm.addRequest(new Runnable() {
      public void run() {
        if (myProject.isDisposed()) {
          return;
        }
        final boolean refresh = myHistorySession.refresh();
        myUpdateAlarm.cancelAllRequests();
        myUpdateAlarm.addRequest(this, 10000);

        if (refresh) {
          final VcsHistorySession session;
          try {
            session = getHistoryProvider().createSessionFor(myFilePath);
          }
          catch (VcsException e) {
            LOG.info(e);
            return;
          }
          if (session != null) {
            if (myHistorySession.allowAsyncRefresh()) {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  refresh(session);
                }
              });
            }
            else {
              refresh(session);
            }
          }
        }
      }
    }, 10000);

    init();

    chooseView();
  }

  private static DualViewColumnInfo[] createColumnList(Project project, VcsHistoryProvider provider) {
    ColumnInfo[] additionalColunms = provider.getRevisionColumns();
    ArrayList<DualViewColumnInfo> columns = new ArrayList<DualViewColumnInfo>();
    if (provider.isDateOmittable()) {
      columns.addAll(Arrays.asList(REVISION, AUTHOR));
    }
    else {
      columns.addAll(Arrays.asList(REVISION, DATE, AUTHOR));
    }

    columns.addAll(wrapAdditionalColumns(additionalColunms));
    columns.add(new MessageColumnInfo(project));
    return columns.toArray(new DualViewColumnInfo[columns.size()]);
  }

  private static Collection<DualViewColumnInfo> wrapAdditionalColumns(ColumnInfo[] additionalColumns) {
    ArrayList<DualViewColumnInfo> result = new ArrayList<DualViewColumnInfo>();
    if (additionalColumns != null) {
      for (ColumnInfo additionalColumn : additionalColumns) {
        result.add(new MyColumnWrapper(additionalColumn));
      }
    }
    return result;
  }

  private static List<TreeItem<VcsFileRevision>> wrapWithTreeElements(List<VcsFileRevision> revisions) {
    ArrayList<TreeItem<VcsFileRevision>> result = new ArrayList<TreeItem<VcsFileRevision>>();
    for (final VcsFileRevision revision : revisions) {
      result.add(new TreeItem<VcsFileRevision>(revision));
    }
    return result;
  }

  public void refresh(VcsHistorySession session) {
    myHistorySession = session;
    HistoryAsTreeProvider treeHistoryProvider = getHistoryProvider().getTreeHistoryProvider();

    if (treeHistoryProvider != null) {
      myDualView.setRoot(new TreeNodeOnVcsRevision(null, treeHistoryProvider.createTreeOn(myHistorySession.getRevisionList())));
    }
    else {
      myDualView.setRoot(new TreeNodeOnVcsRevision(null, wrapWithTreeElements(myHistorySession.getRevisionList())));
    }

    myDualView.rebuild();
    myDualView.repaint();
  }

  protected void addActionsTo(DefaultActionGroup group) {
    addToGroup(false, group);
  }

  private void createDualView(final ColumnInfo defaultColumnToSortBy) {
    myDualView.setShowGrid(true);
    myDualView.getTreeView().addMouseListener(new PopupHandler() {
      public void invokePopup(Component comp, int x, int y) {
        ActionPopupMenu popupMenu = ActionManager.getInstance()
          .createActionPopupMenu(ActionPlaces.UPDATE_POPUP, myPopupActions);
        popupMenu.getComponent().show(comp, x, y);
      }
    });

    myDualView.getFlatView().addMouseListener(new PopupHandler() {
      public void invokePopup(Component comp, int x, int y) {
        ActionPopupMenu popupMenu = ActionManager.getInstance()
          .createActionPopupMenu(ActionPlaces.UPDATE_POPUP, myPopupActions);
        popupMenu.getComponent().show(comp, x, y);
      }
    });

    myDualView.requestFocus();
    myDualView.setSelectionInterval(0, 0);


    myDualView.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        updateMessage();
      }
    });

    myDualView.setRootVisible(false);

    myDualView.expandAll();

    final TreeCellRenderer defaultCellRenderer = myDualView.getTree().getCellRenderer();

    myDualView.setTreeCellRenderer(new MyTreeCellRenderer(defaultCellRenderer, myHistorySession));

    myDualView.setCellWrapper(new MyCellWrapper(myHistorySession));

    TableViewModel sortableModel = myDualView.getFlatView().getTableViewModel();
    sortableModel.setSortable(true);

    if (defaultColumnToSortBy == null) {
      sortableModel.sortByColumn(0, SortableColumnModel.SORT_DESCENDING);
    }
    else {
      sortableModel.sortByColumn(getColumnIndex(defaultColumnToSortBy), SortableColumnModel.SORT_DESCENDING);
    }

  }

  private int getColumnIndex(final ColumnInfo defaultColumnToSortBy) {
    for (int i = 0; i < COLUMNS.length; i++) {
      DualViewColumnInfo dualViewColumnInfo = COLUMNS[i];
      if (dualViewColumnInfo instanceof MyColumnWrapper) {
        if (((MyColumnWrapper)dualViewColumnInfo).getOriginalColumn() == defaultColumnToSortBy) {
          return i;
        }
      }
    }
    return 0;
  }

  private static void makeBold(Component component) {
    if (component instanceof JComponent) {
      JComponent jComponent = (JComponent)component;
      Font font = jComponent.getFont();
      if (font != null) {
        jComponent.setFont(font.deriveFont(Font.BOLD));
      }
    }
    else if (component instanceof Container) {
      Container container = (Container)component;
      for (int i = 0; i < container.getComponentCount(); i++) {
        makeBold(container.getComponent(i));
      }
    }

  }

  private void updateMessage() {
    List selection = getSelection();
    if (selection.size() != 1) {
      myComments.setText("");
    }
    else {
      final String message = getFirstSelectedRevision().getCommitMessage();
      myComments.setText("<html><body>" + IssueLinkHtmlRenderer.formatTextWithLinks(myProject, message) + "</body></html>");
      myComments.setCaretPosition(0);
    }
  }


  private void showDifferences(Project project, VcsFileRevision revision1, VcsFileRevision revision2) {

    try {
      revision1.loadContent();
      revision2.loadContent();

      VcsFileRevision left = revision1;
      VcsFileRevision right = revision2;
      if (VcsHistoryUtil.compare(revision1, revision2) > 0) {
        left = revision2;
        right = revision1;
      }
      byte[] content1 = left.getContent();
      byte[] content2 = right.getContent();


      SimpleDiffRequest diffData = new SimpleDiffRequest(myProject, myFilePath.getPresentableUrl());

      diffData.addHint(DiffTool.HINT_SHOW_FRAME);

      LOG.assertTrue(content1 != null);
      LOG.assertTrue(content2 != null);

      Document doc = myFilePath.getDocument();

      String charset = myFilePath.getCharset().name();
      FileType fileType = myFilePath.getFileType();
      diffData.setContentTitles(left.getRevisionNumber().asString(), right.getRevisionNumber().asString());
      diffData.setContents(createContent(project, content1, left, doc, charset, fileType),
                           createContent(project, content2, right, doc, charset, fileType));
      DiffManager.getInstance().getDiffTool().show(diffData);
    }
    catch (final VcsException e) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          Messages.showErrorDialog(VcsBundle.message("message.text.cannot.show.differences", e.getLocalizedMessage()),
                                   VcsBundle.message("message.title.show.differences"));
        }
      });
    }
    catch (IOException e) {
      LOG.error(e);
    }
    catch (ProcessCanceledException ex) {
      return;
    }
  }

  private static DiffContent createContent(Project project,
                                           byte[] content1,
                                           VcsFileRevision revision,
                                           Document doc,
                                           String charset,
                                           FileType fileType) {
    if (isCurrent(revision) && (doc != null)) return new DocumentContent(project, doc);
    return new BinaryContent(content1, charset, fileType);
  }

  private static boolean isCurrent(VcsFileRevision revision) {
    return revision instanceof CurrentRevision;
  }


  protected JComponent createCenterPanel() {
    Splitter splitter = new Splitter(true, getSplitterProportion());

    splitter.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (Splitter.PROP_PROPORTION.equals(evt.getPropertyName())) {
          setSplitterProportionTo((Float)evt.getNewValue());
        }
      }
    });

    JPanel commentGroup = new JPanel(new BorderLayout(4, 4));
    commentGroup.add(new JLabel(COMMIT_MESSAGE_TITLE + ":"), BorderLayout.NORTH);
    commentGroup.add(ScrollPaneFactory.createScrollPane(myComments), BorderLayout.CENTER);

    splitter.setFirstComponent(myDualView);
    splitter.setSecondComponent(commentGroup);
    return splitter;
  }

  private void chooseView() {
    if (showTree()) {
      myDualView.switchToTheTreeMode();
    }
    else {
      myDualView.switchToTheFlatMode();
    }
  }

  private boolean showTree() {
    return getConfiguration().SHOW_FILE_HISTORY_AS_TREE;
  }

  private void setSplitterProportionTo(Float newProportion) {
    getConfiguration().FILE_HISTORY_SPLITTER_PROPORTION = newProportion.floatValue();
  }

  private float getSplitterProportion() {
    return getConfiguration().FILE_HISTORY_SPLITTER_PROPORTION;
  }

  private VcsConfiguration getConfiguration() {
    return VcsConfiguration.getInstance(myProject);
  }

  private DefaultActionGroup createPopupActions() {
    return addToGroup(true, new DefaultActionGroup(null, false));

  }

  private DefaultActionGroup addToGroup(boolean popup, DefaultActionGroup result) {
    if (popup) {
      result.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
    }

    final MyDiffAction diffAction = new MyDiffAction();
    diffAction.registerCustomShortcutSet(CommonShortcuts.getDiff(), this);
    result.add(diffAction);
    MyDiffWithCurrentAction diffWithCurrent = new MyDiffWithCurrentAction();
    if (!popup) {
      myDualView.installDoubleClickHandler(diffWithCurrent);
    }
    result.add(diffWithCurrent);
    result.add(new MyGetVersionAction());
    result.add(new MyAnnotateAction());
    AnAction[] additionalActions = getHistoryProvider().getAdditionalActions(this);
    if (additionalActions != null) {
      for (AnAction additionalAction : additionalActions) {
        result.add(additionalAction);
      }
    }
    result.add(new AnAction(VcsBundle.message("action.name.refresh"), VcsBundle.message("action.desctiption.refresh"),
                            IconLoader.getIcon("/actions/sync.png")) {
      public void actionPerformed(AnActionEvent e) {
        refresh();

      }
    });

    if (!popup && supportsTree()) {
      result.add(new MyShowAsTreeAction());
    }

    return result;
  }

  public void refresh() {
    try {
      final VcsHistorySession session = getHistoryProvider().createSessionFor(myFilePath);
      if (session == null) return;
      refresh(session);
    }
    catch (VcsException e1) {
      Messages.showErrorDialog(VcsBundle.message("message.text.cannot.refresh.file.history", e1.getLocalizedMessage()),
                               VcsBundle.message("message.title.refresh.file.history"));
    }
  }

  private boolean supportsTree() {
    return getHistoryProvider().getTreeHistoryProvider() != null;
  }

  private VcsHistoryProvider getHistoryProvider() {
    return myProvider;
  }

  private class MyShowAsTreeAction extends ToggleAction {
    public MyShowAsTreeAction() {
      super(VcsBundle.message("action.name.show.files.as.tree"), null, Icons.SMALL_VCS_CONFIGURABLE);
    }

    public boolean isSelected(AnActionEvent e) {
      return getConfiguration().SHOW_FILE_HISTORY_AS_TREE;
    }

    public void setSelected(AnActionEvent e, boolean state) {
      getConfiguration().SHOW_FILE_HISTORY_AS_TREE = state;
      chooseView();
    }
  }

  private class MyDiffAction extends AbstractActionForSomeSelection {
    public MyDiffAction() {
      super(VcsBundle.message("action.name.compare"), VcsBundle.message("action.description.compare"), "diff", 2,
            FileHistoryPanelImpl.this);
    }

    protected void actionPerformed() {
      try {
        showDifferences(myProject, (VcsFileRevision)getSelection().get(0), (VcsFileRevision)getSelection().get(1));
      }
      catch (Exception e) {
        LOG.error(e);
      }
    }

    public boolean isEnabled() {
      if (!super.isEnabled()) return false;
      if (!myHistorySession.isContentAvailable(getSelectedRevision(0)) || !myHistorySession.isContentAvailable(getSelectedRevision(1))) {
        return false;
      }
      return true;
    }
  }

  private class MyDiffWithCurrentAction extends AbstractActionForSomeSelection {
    public MyDiffWithCurrentAction() {
      super(VcsBundle.message("action.name.compare.with.local"), VcsBundle.message("action.description.compare.with.local"),
            "diffWithCurrent", 1, FileHistoryPanelImpl.this);
    }

    protected void actionPerformed() {
      final VcsRevisionNumber currentRevisionNumber = myHistorySession.getCurrentRevisionNumber();
      if (currentRevisionNumber != null) {
        showDifferences(myProject, getFirstSelectedRevision(), new CurrentRevision(myFilePath.getVirtualFile(), currentRevisionNumber));
      }
    }

    public boolean isEnabled() {
      if (myHistorySession.getCurrentRevisionNumber() == null) return false;
      if (myFilePath.getVirtualFile() == null) return false;
      if (!myHistorySession.isContentAvailable(getFirstSelectedRevision())) return false;
      if (!super.isEnabled()) return false;
      //if (CvsEntriesManager.getInstance().getEntryFor(myVirtualFile) == null) return false;
      return true;
    }
  }

  private class MyGetVersionAction extends AbstractActionForSomeSelection {
    public MyGetVersionAction() {
      super(VcsBundle.message("action.name.get.file.content.from.repository"),
            VcsBundle.message("action.description.get.file.content.from.repository"), "get", 1, FileHistoryPanelImpl.this);
    }

    public void update(AnActionEvent e) {
      if (getVirtualParent() == null) {
        Presentation presentation = e.getPresentation();
        presentation.setVisible(false);
        presentation.setEnabled(false);
      }
      else {
        super.update(e);
      }
    }

    @Override
    public boolean isEnabled() {
      if (!super.isEnabled()) return false;
      if (!myHistorySession.isContentAvailable(getFirstSelectedRevision())) return false;
      return true;
    }

    protected void actionPerformed() {
      final VcsFileRevision revision = getFirstSelectedRevision();
      if (getVirtualFile() != null) {
        if (!new ReplaceFileConfirmationDialog(myProject, VcsBundle.message("acton.name.get.revision"))
          .confirmFor(new VirtualFile[]{getVirtualFile()})) {
          return;
        }
      }

      try {
        revision.loadContent();
      }
      catch (VcsException e) {
        Messages.showErrorDialog(VcsBundle.message("message.text.cannot.load.version", e.getLocalizedMessage()),
                                 VcsBundle.message("message.title.get.version"));
      }
      catch (ProcessCanceledException ex) {
        return;
      }
      getVersion(revision);
      refreshFile(revision);
    }

    private void refreshFile(VcsFileRevision revision) {
      if (getVirtualFile() == null) {
        final LocalHistoryAction action = startLocalHistoryAction(revision);
        if (getVirtualParent() != null) {
          getVirtualParent().refresh(true, true, new Runnable() {
            public void run() {
              myFilePath.refresh();
              action.finish();
            }
          });
        }
      }
    }

    private void getVersion(final VcsFileRevision revision) {
      final VirtualFile file = getVirtualFile();
      if ((file != null) && !file.isWritable()) {
        if (ReadonlyStatusHandler.getInstance(myProject).ensureFilesWritable(file).hasReadonlyFiles()) {
          return;
        }
      }

      LocalHistoryAction action = file != null ? startLocalHistoryAction(revision) : LocalHistoryAction.NULL;

      final byte[] revisionContent;
      try {
        revision.loadContent();
        revisionContent = revision.getContent();
      }
      catch (IOException e) {
        LOG.error(e);
        return;
      }
      catch (VcsException e) {
        Messages.showMessageDialog(VcsBundle.message("message.text.cannot.load.revision", e.getLocalizedMessage()),
                                   VcsBundle.message("message.title.get.revision.content"), Messages.getInformationIcon());
        return;
      }
      catch (ProcessCanceledException ex) {
        return;
      }
      final byte[] finalRevisionContent = revisionContent;
      try {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            CommandProcessor.getInstance().executeCommand(myProject, new Runnable() {
                public void run() {
                  try {
                    write(finalRevisionContent);
                  }
                  catch (IOException e) {
                    Messages.showMessageDialog(VcsBundle.message("message.text.cannot.save.content", e.getLocalizedMessage()),
                                               VcsBundle.message("message.title.get.revision.content"), Messages.getErrorIcon());
                  }
                }
              }, createGetActionTitle(revision), null);
          }
        });
        if (file != null) {
          VcsDirtyScopeManager.getInstance(myProject).fileDirty(file);
        }
      }
      finally {
        action.finish();
      }
    }

    private LocalHistoryAction startLocalHistoryAction(final VcsFileRevision revision) {
      return LocalHistory.startAction(myProject, createGetActionTitle(revision));
    }

    private String createGetActionTitle(final VcsFileRevision revision) {
      return VcsBundle.message("action.name.for.file.get.version", getIOFile().getAbsolutePath(), revision.getRevisionNumber());
    }

    private File getIOFile() {
      return myFilePath.getIOFile();
    }

    private String getFilePath() {
      return myFilePath.getPath();
    }

    private void write(byte[] revision) throws IOException {
      if (getVirtualFile() == null) {
        writeContentToIOFile(revision);
      }
      else {
        Document document = myFilePath.getDocument();
        if (document == null) {
          writeContentToFile(revision);
        }
        else {
          writeContentToDocument(document, revision);
        }
      }
    }

    private void writeContentToIOFile(byte[] revisionContent) throws IOException {
      FileOutputStream outputStream = new FileOutputStream(getIOFile());
      try {
        outputStream.write(revisionContent);
      }
      finally {
        outputStream.close();
      }
    }

    private void writeContentToFile(final byte[] revision) throws IOException {
      getVirtualFile().setBinaryContent(revision);
    }

    private void writeContentToDocument(final Document document, byte[] revisionContent) throws IOException {
      final String content = StringUtil.convertLineSeparators(new String(revisionContent, myFilePath.getCharset().name()));

      CommandProcessor.getInstance().executeCommand(myProject, new Runnable() {
        public void run() {
          document.replaceString(0, document.getTextLength(), content);
        }
      }, VcsBundle.message("message.title.get.version"), null);
    }

  }

  private class MyAnnotateAction extends AnAction {
    public MyAnnotateAction() {
      super(VcsBundle.message("annotate.action.name"), VcsBundle.message("annotate.action.description"),
            IconLoader.getIcon("/actions/annotate.png"));
    }

    public void update(AnActionEvent e) {
      VirtualFile revisionVirtualFile = e.getData(VcsDataKeys.VCS_VIRTUAL_FILE);
      VcsFileRevision revision = e.getData(VcsDataKeys.VCS_FILE_REVISION);
      FileType fileType = revisionVirtualFile == null ? null : revisionVirtualFile.getFileType();
      e.getPresentation()
        .setEnabled(revision != null && revisionVirtualFile != null && !fileType.isBinary() && myAnnotationProvider != null);
    }


    public void actionPerformed(AnActionEvent e) {
      VcsFileRevision revision = e.getData(VcsDataKeys.VCS_FILE_REVISION);
      VirtualFile revisionVirtualFile = e.getData(VcsDataKeys.VCS_VIRTUAL_FILE);
      try {
        final FileAnnotation annotation = myAnnotationProvider.annotate(revisionVirtualFile, revision);
        AbstractVcsHelper.getInstance(myProject).showAnnotation(annotation, revisionVirtualFile);
      }
      catch (VcsException e1) {
        AbstractVcsHelper.getInstance(myProject).showError(e1, VcsBundle.message("operation.name.annotate"));
      }

    }
  }

  public Object getData(String dataId) {
    VcsFileRevision firstSelectedRevision = getFirstSelectedRevision();
    if (DataConstants.NAVIGATABLE.equals(dataId)) {
      List selectedItems = getSelection();
      if (selectedItems.size() != 1) return null;
      if (!myHistorySession.isContentAvailable(firstSelectedRevision)) {
        return null;
      }
      VirtualFile virtualFileForRevision = createVirtualFileForRevision(firstSelectedRevision);
      if (virtualFileForRevision != null) {
        return new OpenFileDescriptor(myProject, virtualFileForRevision);
      }
      else {
        return null;
      }
    }
    else if (DataConstants.PROJECT.equals(dataId)) {
      return myProject;
    }
    else if (VcsDataConstants.VCS_FILE_REVISION.equals(dataId)) {
      return firstSelectedRevision;
    }
    else if (VcsDataConstants.VCS_VIRTUAL_FILE.equals(dataId)) {
      if (firstSelectedRevision == null) return null;
      return createVirtualFileForRevision(firstSelectedRevision);
    }
    else if (VcsDataConstants.FILE_PATH.equals(dataId)) {
      return myFilePath;
    }
    else if (VcsDataConstants.IO_FILE.equals(dataId)) {
      return myFilePath.getIOFile();
    }
    else if (DataConstants.VIRTUAL_FILE.equals(dataId)) {
      if (getVirtualFile() == null) return null;
      if (getVirtualFile().isValid()) {
        return getVirtualFile();
      }
      else {
        return null;
      }
    }
    else if (VcsDataConstants.FILE_HISTORY_PANEL.equals(dataId)) {
      return this;
    }
    else {
      return super.getData(dataId);
    }
  }

  private VirtualFile createVirtualFileForRevision(VcsFileRevision revision) {
    if (!myRevisionToVirtualFile.containsKey(revision)) {
      myRevisionToVirtualFile.put(revision, new VcsVirtualFile(myFilePath.getPath(), revision, VcsFileSystem.getInstance()));
    }
    return myRevisionToVirtualFile.get(revision);
  }

  public List getSelection() {
    return myDualView.getSelection();
  }

  @Nullable
  private VcsFileRevision getFirstSelectedRevision() {
    List selection = getSelection();
    if (selection.isEmpty()) return null;
    return ((TreeNodeOnVcsRevision)selection.get(0)).myRevision;
  }

  @Nullable
  private VcsFileRevision getSelectedRevision(int index) {
    List selection = getSelection();
    if (selection.isEmpty()) return null;
    return ((TreeNodeOnVcsRevision)selection.get(index)).myRevision;
  }

  class TreeNodeOnVcsRevision extends DefaultMutableTreeNode implements VcsFileRevision, DualTreeElement {
    private final VcsFileRevision myRevision;

    public TreeNodeOnVcsRevision(VcsFileRevision revision, List<TreeItem<VcsFileRevision>> roots) {
      myRevision = revision == null ? VcsFileRevision.NULL : revision;
      for (final TreeItem<VcsFileRevision> root : roots) {
        add(new TreeNodeOnVcsRevision(root.getData(), root.getChildren()));
      }
    }

    public String getAuthor() {
      return myRevision.getAuthor();
    }

    public String getCommitMessage() {
      return myRevision.getCommitMessage();
    }

    public void loadContent() throws VcsException {
      myRevision.loadContent();
    }

    public VcsRevisionNumber getRevisionNumber() {
      return myRevision.getRevisionNumber();
    }

    public Date getRevisionDate() {
      return myRevision.getRevisionDate();
    }

    public String getBranchName() {
      return myRevision.getBranchName();
    }

    public byte[] getContent() throws IOException {
      return myRevision.getContent();
    }

    public String toString() {
      return getRevisionNumber().asString();
    }

    public boolean shouldBeInTheFlatView() {
      return myRevision != VcsFileRevision.NULL;
    }

  }

  protected void dispose() {
    super.dispose();
    myDualView.dispose();
    myUpdateAlarm.cancelAllRequests();
  }

  abstract class AbstractActionForSomeSelection extends AnAction {
    private final int mySuitableSelectedElements;
    private final FileHistoryPanelImpl mySelectionProvider;

    public AbstractActionForSomeSelection(String name,
                                          String description,
                                          @NonNls String iconName,
                                          int suitableSelectionSize,
                                          FileHistoryPanelImpl tableProvider) {
      super(name, description, IconLoader.getIcon("/actions/" + iconName + ".png"));
      mySuitableSelectedElements = suitableSelectionSize;
      mySelectionProvider = tableProvider;
    }

    protected abstract void actionPerformed();

    public boolean isEnabled() {
      return mySelectionProvider.getSelection().size() == mySuitableSelectedElements;
    }

    public void actionPerformed(AnActionEvent e) {
      if (!isEnabled()) return;
      actionPerformed();
    }

    public void update(AnActionEvent e) {
      Presentation presentation = e.getPresentation();
      presentation.setVisible(true);
      presentation.setEnabled(isEnabled());
    }
  }

  abstract static class VcsColumnInfo<T extends Comparable> extends DualViewColumnInfo<VcsFileRevision, String>
    implements Comparator<VcsFileRevision> {
    public VcsColumnInfo(String name) {
      super(name);
    }

    protected abstract T getDataOf(VcsFileRevision o);

    public Comparator<VcsFileRevision> getComparator() {
      return this;
    }

    public String valueOf(VcsFileRevision object) {
      T result = getDataOf(object);
      return result == null ? "" : result.toString();
    }

    public int compare(VcsFileRevision o1, VcsFileRevision o2) {
      return compareObjects(getDataOf(o1), getDataOf(o2));
    }

    private int compareObjects(Comparable data1, Comparable data2) {
      if (data1 == data2) return 0;
      if (data1 == null) return -1;
      if (data2 == null) return 1;
      return data1.compareTo(data2);
    }

    public boolean shouldBeShownIsTheTree() {
      return true;
    }

    public boolean shouldBeShownIsTheTable() {
      return true;
    }

  }

  private static class MyColumnWrapper<T> extends DualViewColumnInfo<TreeNodeOnVcsRevision, Object> {
    private final ColumnInfo<VcsFileRevision, T> myBaseColumn;

    public Comparator<TreeNodeOnVcsRevision> getComparator() {
      final Comparator comparator = myBaseColumn.getComparator();
      if (comparator == null) return null;
      return new Comparator<TreeNodeOnVcsRevision>() {
        public int compare(TreeNodeOnVcsRevision o1, TreeNodeOnVcsRevision o2) {
          if (o1 == null) return -1;
          if (o2 == null) return 1;
          VcsFileRevision revision1 = o1.myRevision;
          VcsFileRevision revision2 = o2.myRevision;
          if (revision1 == null) return -1;
          if (revision2 == null) return 1;
          return comparator.compare(revision1, revision2);
        }
      };
    }

    public String getName() {
      return myBaseColumn.getName();
    }

    public Class getColumnClass() {
      return myBaseColumn.getColumnClass();
    }

    public boolean isCellEditable(TreeNodeOnVcsRevision o) {
      return myBaseColumn.isCellEditable(o.myRevision);
    }

    public void setValue(TreeNodeOnVcsRevision o, Object aValue) {
      //noinspection unchecked
      myBaseColumn.setValue(o.myRevision, (T)aValue);
    }

    public TableCellRenderer getRenderer(TreeNodeOnVcsRevision p0) {
      return myBaseColumn.getRenderer(p0.myRevision);
    }

    public TableCellEditor getEditor(TreeNodeOnVcsRevision item) {
      return myBaseColumn.getEditor(item.myRevision);
    }

    public String getMaxStringValue() {
      return myBaseColumn.getMaxStringValue();
    }

    public int getAdditionalWidth() {
      return myBaseColumn.getAdditionalWidth();
    }

    public int getWidth(JTable table) {
      return myBaseColumn.getWidth(table);
    }

    public void setName(String s) {
      myBaseColumn.setName(s);
    }

    public MyColumnWrapper(ColumnInfo<VcsFileRevision, T> additionalColunm) {
      super(additionalColunm.getName());
      myBaseColumn = additionalColunm;
    }

    public boolean shouldBeShownIsTheTree() {
      return true;
    }

    public boolean shouldBeShownIsTheTable() {
      return true;
    }

    public Object valueOf(TreeNodeOnVcsRevision o) {
      return myBaseColumn.valueOf(o.myRevision);
    }

    public ColumnInfo getOriginalColumn() {
      return myBaseColumn;
    }
  }

  private VirtualFile getVirtualFile() {
    return myFilePath.getVirtualFile();
  }

  private VirtualFile getVirtualParent() {
    return myFilePath.getVirtualFileParent();
  }


  private class MyTreeCellRenderer implements TreeCellRenderer {
    private final TreeCellRenderer myDefaultCellRenderer;
    private final VcsHistorySession myHistorySession;

    public MyTreeCellRenderer(final TreeCellRenderer defaultCellRenderer, final VcsHistorySession historySession) {
      myDefaultCellRenderer = defaultCellRenderer;
      myHistorySession = historySession;
    }

    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
      Component result = myDefaultCellRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

      TreePath path = tree.getPathForRow(row);
      if (path == null) return result;
      VcsFileRevision revision = row >= 0 ? (VcsFileRevision)path.getLastPathComponent() : null;

      if (revision != null) {

        if (Comparing.equal(revision.getRevisionNumber(), myHistorySession.getCurrentRevisionNumber())) {
          makeBold(result);
        }
        if (!selected && Comparing.equal(revision.getRevisionNumber(), myHistorySession.getCurrentRevisionNumber())) {
          result.setBackground(new Color(188, 227, 231));
          ((JComponent)result).setOpaque(false);
        }
      }
      else if (selected) {
        result.setBackground(UIUtil.getTableSelectionBackground());
      }
      else {
        result.setBackground(UIUtil.getTableBackground());
      }

      return result;
    }
  }

  private static class MyCellWrapper implements CellWrapper {
    private final VcsHistorySession myHistorySession;

    public MyCellWrapper(final VcsHistorySession historySession) {
      myHistorySession = historySession;
    }

    public void wrap(Component component,
                     JTable table,
                     Object value,
                     boolean isSelected,
                     boolean hasFocus,
                     int row,
                     int column,
                     Object treeNode) {
      VcsFileRevision revision = (VcsFileRevision)treeNode;
      if (revision == null) return;
      if (myHistorySession.isCurrentRevision(revision.getRevisionNumber())) {
        makeBold(component);
      }
    }
  }
}