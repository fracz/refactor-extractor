/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.find.impl;

import com.intellij.CommonBundle;
import com.intellij.find.*;
import com.intellij.find.actions.ShowUsagesAction;
import com.intellij.icons.AllIcons;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.util.scopeChooser.ScopeChooserCombo;
import com.intellij.ide.util.scopeChooser.ScopeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.MnemonicHelper;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.progress.util.ReadTask;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.psi.PsiBundle;
import com.intellij.psi.search.SearchScope;
import com.intellij.ui.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.FindUsagesProcessPresentation;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageViewPresentation;
import com.intellij.usages.impl.UsagePreviewPanel;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SmartList;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.intellij.find.impl.FindDialog.createCheckbox;

public class FindPopupPanel extends JBPanel implements FindUI {
  private static final Logger LOG = Logger.getInstance(FindPopupPanel.class);

  private static final boolean PREVIEW_IS_EDITABLE = true;//todo move it to registry at least
  // unify with CommonShortcuts.CTRL_ENTER
  private static final KeyStroke OK_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, SystemInfo.isMac

                                                                                          ? InputEvent.META_DOWN_MASK
                                                                                          : InputEvent.CTRL_DOWN_MASK);

  private static final KeyStroke MOVE_CARET_DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
  private static final KeyStroke MOVE_CARET_UP = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
  private static final KeyStroke NEW_LINE = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

  private static final KeyStroke MOVE_CARET_DOWN_ALTERNATIVE = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK);
  private static final KeyStroke MOVE_CARET_UP_ALTERNATIVE = KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK);
  private static final KeyStroke NEW_LINE_ALTERNATIVE = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK);
  protected static final String SIZE_KEY = "find.popup";
  private final FindUIHelper myHelper;

  private JComponent myCodePreviewComponent;
  private SearchTextArea mySearchTextArea;
  private SearchTextArea myReplaceTextArea;
  private ActionListener myOkActionListener;
  private AtomicBoolean myCanClose = new AtomicBoolean();

  enum Scope {
    PROJECT, MODULE, DIRECTORY, SCOPE
  }

  @NotNull private final Project myProject;
  @NotNull private final Disposable myDisposable;

  private Alarm mySearchRescheduleOnCancellationsAlarm;
  private Alarm myUpdateResultsPopupBoundsAlarm;
  private volatile ProgressIndicatorBase myResultsPreviewSearchProgress;


  private JLabel myTitleLabel;
  private StateRestoringCheckBox myCbCaseSensitive;
  private StateRestoringCheckBox myCbPreserveCase;
  private StateRestoringCheckBox myCbWholeWordsOnly;
  private StateRestoringCheckBox myCbRegularExpressions;
  private StateRestoringCheckBox myCbFileFilter;
  private ActionToolbarImpl myScopeSelectionToolbar;
  private TextFieldWithAutoCompletion<String> myFileMaskField;
  private ArrayList<String> myFileMasks = new ArrayList<>();
  private ActionButton myFilterContextButton;
  private ActionButton myTabResultsButton;
  private JButton myOKButton;
  private JTextArea mySearchComponent;
  private JTextArea myReplaceComponent;
  private String mySelectedContextName = FindBundle.message("find.context.anywhere.scope.label");
  private Scope mySelectedScope = Scope.PROJECT;
  private JPanel myScopeDetailsPanel;
  private ComboBox myModuleComboBox;
  private ComboBox myDirectoryComboBox;
  private FixedSizeButton mySelectDirectoryButton;
  private JToggleButton myRecursiveDirectoryButton;
  private ScopeChooserCombo myScopeCombo;

  private JBTable myResultsPreviewTable;
  private UsagePreviewPanel myUsagePreviewPanel;
  private JBPopup myBalloon;

  public void showUI() {
    if (myBalloon != null && myBalloon.isVisible()) {
      return;
    }
    if (myBalloon != null && !myBalloon.isDisposed()) {
      myBalloon.cancel();
    }
    if (myBalloon == null || myBalloon.isDisposed()) {
      final ComponentPopupBuilder builder = JBPopupFactory.getInstance().createComponentPopupBuilder(this, getPreferredFocusedComponent());
      myBalloon = builder
        .setProject(myHelper.getProject())
        .setResizable(true)
        .setMayBeParent(true)
        .setModalContext(false)
        .setRequestFocus(true)
        .setCancelCallback(() -> {
          DimensionService.getInstance().setSize(SIZE_KEY, myBalloon.getSize(), myHelper.getProject() );
          DimensionService.getInstance().setLocation(SIZE_KEY, myBalloon.getLocationOnScreen(), myHelper.getProject() );
          return Boolean.TRUE;
        })
        .createPopup();
      Disposer.register(myBalloon, myDisposable);
      registerCloseAction(myBalloon);
      final Window window = WindowManager.getInstance().suggestParentWindow(myProject);
      Component parent = UIUtil.findUltimateParent(window);
      RelativePoint showPoint = null;
      Point screenPoint = DimensionService.getInstance().getLocation(SIZE_KEY);
      if (screenPoint != null) {
        showPoint = new RelativePoint(screenPoint);
      }
      if (parent != null && showPoint == null) {
        int height = UISettings.getInstance().getShowNavigationBar() ? 135 : 115;
        if (parent instanceof IdeFrameImpl && ((IdeFrameImpl)parent).isInFullScreen()) {
          height -= 20;
        }
        showPoint = new RelativePoint(parent, new Point((parent.getSize().width - getPreferredSize().width) / 2, height));
      }
      mySearchComponent.selectAll();
      if (showPoint != null) {
        myBalloon.show(showPoint);
      } else {
        myBalloon.showCenteredInCurrentWindow(myProject);
      }
      WindowMoveListener windowListener = new WindowMoveListener(this);
      myTitleLabel.addMouseListener(windowListener);
      myTitleLabel.addMouseMotionListener(windowListener);
      myBalloon.pack(true, true);
      Dimension panelSize = getPreferredSize();
      Dimension prev = DimensionService.getInstance().getSize(SIZE_KEY);
      if (!myCbPreserveCase.isVisible()) {
        panelSize.width += myCbPreserveCase.getPreferredSize().width + 4;
      }
      myBalloon.setMinimumSize(panelSize);
      int width = prev != null && prev.width > panelSize.width ? prev.width : panelSize.width;
      myBalloon.setSize(new Dimension(width, panelSize.height));
    }
  }

  FindPopupPanel(FindUIHelper helper) {
    myHelper = helper;
    myProject = myHelper.getProject();
    myDisposable = Disposer.newDisposable();
    Disposer.register(myDisposable, new Disposable() {
      @Override
      public void dispose() {
        FindPopupPanel.this.finishPreviousPreviewSearch();
        if (mySearchRescheduleOnCancellationsAlarm != null) Disposer.dispose(mySearchRescheduleOnCancellationsAlarm);
        if (myUpdateResultsPopupBoundsAlarm != null) Disposer.dispose(myUpdateResultsPopupBoundsAlarm);
        if (myUsagePreviewPanel != null) Disposer.dispose(myUsagePreviewPanel);
      }
    });

    initComponents();
    initByModel();

    ApplicationManager.getApplication().invokeLater(() -> this.scheduleResultsUpdate(), ModalityState.any());
  }

  @NotNull
  @Override
  public Disposable getDisposable() {
    return myDisposable;
  }

  private void initComponents() {
    myTitleLabel = new JBLabel(FindBundle.message("find.in.path.dialog.title"), UIUtil.ComponentStyle.REGULAR);
    myTitleLabel.setFont(myTitleLabel.getFont().deriveFont(Font.BOLD));
    myCbCaseSensitive = createCheckbox(FindBundle.message("find.popup.case.sensitive"));
    ItemListener liveResultsPreviewUpdateListener = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        scheduleResultsUpdate();
      }
    };
    myCbCaseSensitive.addItemListener(liveResultsPreviewUpdateListener);
    myCbPreserveCase = createCheckbox(FindBundle.message("find.options.replace.preserve.case"));
    myCbPreserveCase.addItemListener(liveResultsPreviewUpdateListener);
    myCbPreserveCase.setVisible(myHelper.getModel().isReplaceState());
    myCbWholeWordsOnly = createCheckbox(FindBundle.message("find.popup.whole.words"));
    myCbWholeWordsOnly.addItemListener(liveResultsPreviewUpdateListener);
    myCbRegularExpressions = createCheckbox(FindBundle.message("find.popup.regex"));
    myCbRegularExpressions.addItemListener(liveResultsPreviewUpdateListener);
    myCbFileFilter = createCheckbox("");
    myCbFileFilter.setMnemonic('a');
    myCbFileFilter.setToolTipText("<html>Use file m<u>a</u>sk(s)");
    myCbFileFilter.setMargin(JBUI.emptyInsets());
    myCbFileFilter.setBorder(null);
    myCbFileFilter.addItemListener(liveResultsPreviewUpdateListener);
    myFileMaskField =
      new TextFieldWithAutoCompletion<String>(myProject, new TextFieldWithAutoCompletion.StringsCompletionProvider(myFileMasks, null),
                                              false, null) {
        @Override
        public void setEnabled(boolean enabled) {
          super.setEnabled(enabled);
          setBackground(enabled ? JBColor.background() : UIUtil.getComboBoxDisabledBackground());
        }
      };
    myFileMaskField.setPreferredWidth(JBUI.scale(100));
    myFileMaskField.addDocumentListener(new com.intellij.openapi.editor.event.DocumentAdapter() {
      @Override
      public void documentChanged(com.intellij.openapi.editor.event.DocumentEvent e) {
        scheduleResultsUpdate();
      }
    });
    myCbFileFilter.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        myFileMaskField.setEnabled(myCbFileFilter.isSelected());
      }
    });
    DefaultActionGroup switchContextGroup = new DefaultActionGroup();
    switchContextGroup.add(new MySwitchContextToggleAction(FindModel.SearchContext.ANY));
    switchContextGroup.add(new MySwitchContextToggleAction(FindModel.SearchContext.IN_COMMENTS));
    switchContextGroup.add(new MySwitchContextToggleAction(FindModel.SearchContext.IN_STRING_LITERALS));
    switchContextGroup.add(new MySwitchContextToggleAction(FindModel.SearchContext.EXCEPT_COMMENTS));
    switchContextGroup.add(new MySwitchContextToggleAction(FindModel.SearchContext.EXCEPT_STRING_LITERALS));
    switchContextGroup.add(new MySwitchContextToggleAction(FindModel.SearchContext.EXCEPT_COMMENTS_AND_STRING_LITERALS));
    switchContextGroup.setPopup(true);
    Presentation filterPresentation = new Presentation();
    filterPresentation.setIcon(AllIcons.General.Filter);
    myFilterContextButton =
      new ActionButton(switchContextGroup, filterPresentation, ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE) {
        @Override
        public int getPopState() {
          int state = super.getPopState();
          if (state != ActionButtonComponent.NORMAL) return state;
          return mySelectedContextName.equals(FindDialog.getPresentableName(FindModel.SearchContext.ANY))
                 ? ActionButtonComponent.NORMAL
                 : ActionButtonComponent.PUSHED;
        }
      };

    DefaultActionGroup tabResultsContextGroup = new DefaultActionGroup();
    tabResultsContextGroup.add(new ToggleAction(FindBundle.message("find.options.skip.results.tab.with.one.usage.checkbox")) {
      @Override
      public boolean isSelected(AnActionEvent e) {
        return FindSettings.getInstance().isSkipResultsWithOneUsage();
      }

      @Override
      public void setSelected(AnActionEvent e, boolean state) {
        myHelper.setSkipResultsWithOneUsage(state);
      }

      @Override
      public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setVisible(!myHelper.isReplaceState());
      }
    });
    tabResultsContextGroup.add(new ToggleAction(FindBundle.message("find.open.in.new.tab.checkbox")) {
      @Override
      public boolean isSelected(AnActionEvent e) {
        return FindSettings.getInstance().isShowResultsInSeparateView();
      }

      @Override
      public void setSelected(AnActionEvent e, boolean state) {
        myHelper.setUseSeparateView(state);
      }
    });
    tabResultsContextGroup.setPopup(true);
    Presentation tabSettingsPresentation = new Presentation();
    tabSettingsPresentation.setIcon(AllIcons.General.SecondaryGroup);
    myTabResultsButton =
      new ActionButton(tabResultsContextGroup, tabSettingsPresentation, ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);
    myOKButton = new JButton(FindBundle.message("find.popup.find.button"));
    myOkActionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        FindModel validateModel = myHelper.getModel().clone();
        applyTo(validateModel, false);

        ValidationInfo validationInfo = getValidationInfo(validateModel);

        if (validationInfo == null) {
          myHelper.getModel().copyFrom(validateModel);
          myHelper.updateFindSettings();
          myHelper.doOKAction();
        }
        else {
          String message = validationInfo.message;
          Messages.showMessageDialog(
            FindPopupPanel.this,
            message,
            CommonBundle.getErrorTitle(),
            Messages.getErrorIcon()
          );
          return;
        }
        Disposer.dispose(myBalloon);
      }
    };
    myOKButton.addActionListener(myOkActionListener);
    registerKeyboardAction(myOkActionListener, OK_KEYSTROKE, WHEN_IN_FOCUSED_WINDOW);
    mySearchComponent = new JTextArea();
    mySearchComponent.setColumns(25);
    mySearchComponent.setRows(1);
    myReplaceComponent = new JTextArea();
    myReplaceComponent.setColumns(25);
    myReplaceComponent.setRows(1);
    mySearchTextArea = new SearchTextArea(mySearchComponent, true, true);
    myReplaceTextArea = new SearchTextArea(myReplaceComponent, false, false);
    DocumentAdapter documentAdapter = new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        int searchRows1 = mySearchComponent.getRows();
        int searchRows2 = Math.max(1, Math.min(3, StringUtil.countChars(mySearchComponent.getText(), '\n') + 1));
        mySearchComponent.setRows(searchRows2);
        int replaceRows1 = myReplaceComponent.getRows();
        int replaceRows2 = Math.max(1, Math.min(3, StringUtil.countChars(myReplaceComponent.getText(), '\n') + 1));
        myReplaceComponent.setRows(replaceRows2);

        if (myBalloon == null) return;

        if (searchRows1 != searchRows2 || replaceRows1 != replaceRows2) {
          adjustPopup();
        }
        scheduleResultsUpdate();
      }
    };
    mySearchComponent.getDocument().addDocumentListener(documentAdapter);
    myReplaceComponent.getDocument().addDocumentListener(documentAdapter);


    DefaultActionGroup scopeActionGroup = new DefaultActionGroup();
    scopeActionGroup.add(new MySelectScopeToggleAction(Scope.PROJECT));
    scopeActionGroup.add(new MySelectScopeToggleAction(Scope.MODULE));
    scopeActionGroup.add(new MySelectScopeToggleAction(Scope.DIRECTORY));
    scopeActionGroup.add(new MySelectScopeToggleAction(Scope.SCOPE));
    myScopeSelectionToolbar =
      (ActionToolbarImpl)ActionManager.getInstance().createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, scopeActionGroup, true);
    myScopeSelectionToolbar.setForceMinimumSize(true);
    myScopeSelectionToolbar.setLayoutPolicy(ActionToolbar.NOWRAP_LAYOUT_POLICY);

    Module[] modules = ModuleManager.getInstance(myProject).getModules();
    String[] names = new String[modules.length];
    for (int i = 0; i < modules.length; i++) {
      names[i] = modules[i].getName();
    }

    Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
    myModuleComboBox = new ComboBox(names);
    myModuleComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        scheduleResultsUpdate();
      }
    });
    myDirectoryComboBox = new ComboBox(200);
    Component editorComponent = myDirectoryComboBox.getEditor().getEditorComponent();
    if (editorComponent instanceof JTextField) {
      JTextField field = (JTextField)editorComponent;
      field.setColumns(40);
    }
    initCombobox(myDirectoryComboBox);
    myDirectoryComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        scheduleResultsUpdate();
      }
    });
    mySelectDirectoryButton = new FixedSizeButton(myDirectoryComboBox);
    TextFieldWithBrowseButton.MyDoClickAction.addTo(mySelectDirectoryButton, myDirectoryComboBox);
    mySelectDirectoryButton.setMargin(JBUI.emptyInsets());
    mySelectDirectoryButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        FindModel tmp = myHelper.getModel().clone();
        FileChooser.chooseFiles(descriptor, myProject, FindPopupPanel.this, null, new FileChooser.FileChooserConsumer() {
          @Override
          public void consume(List<VirtualFile> files) {
            tmp.setDirectoryName(files.get(0).getPresentableUrl());
            FindManager.getInstance(myProject).showFindDialog(tmp, myHelper.getOkHandler());
          }

          @Override
          public void cancelled() {
            FindManager.getInstance(myProject).showFindDialog(tmp, myHelper.getOkHandler());
          }
        });
      }
    });

    myRecursiveDirectoryButton = new JToggleButton(AllIcons.General.Recursive, myHelper.getModel().isWithSubdirectories());
    myRecursiveDirectoryButton.setIcon(AllIcons.General.Recursive);
    myRecursiveDirectoryButton.setMargin(JBUI.emptyInsets());
    myRecursiveDirectoryButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        scheduleResultsUpdate();
      }
    });
    //DefaultActionGroup recursiveActionGroup = new DefaultActionGroup();
    //ActionToolbar recursiveDirectoryToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, recursiveActionGroup, true);
    JPanel directoryPanel = new JPanel(new BorderLayout());
    directoryPanel.add(myDirectoryComboBox, BorderLayout.CENTER);
    JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
    buttonsPanel.add(mySelectDirectoryButton);
    buttonsPanel.add(myRecursiveDirectoryButton);
    directoryPanel.add(buttonsPanel, BorderLayout.EAST);

    myScopeCombo = new ScopeChooserCombo();
    myScopeCombo.init(myProject, true, true, FindSettings.getInstance().getDefaultScopeName(), new Condition<ScopeDescriptor>() {
      final String projectFilesScopeName = PsiBundle.message("psi.search.scope.project");
      final String moduleFilesScopeName;

      {
        String moduleScopeName = PsiBundle.message("search.scope.module", "");
        final int ind = moduleScopeName.indexOf(' ');
        moduleFilesScopeName = moduleScopeName.substring(0, ind + 1);
      }

      @Override
      public boolean value(ScopeDescriptor descriptor) {
        final String display = descriptor.getDisplay();
        return !projectFilesScopeName.equals(display) && !display.startsWith(moduleFilesScopeName);
      }
    });
    myScopeCombo.setBrowseListener(new ScopeChooserCombo.BrowseListener() {

      private FindModel myModelSnapshot;

      @Override
      public void onBeforeBrowseStarted() {
        myModelSnapshot = myHelper.getModel().clone();
        myCanClose.set(true);
        Disposer.dispose(myBalloon);
      }

      @Override
      public void onAfterBrowseFinished() {
        if (myModelSnapshot != null) {
          SearchScope scope = myScopeCombo.getSelectedScope();
          if (scope != null) {
            myModelSnapshot.setCustomScope(scope);
          }
          FindManager.getInstance(myProject).showFindDialog(myModelSnapshot, myHelper.getOkHandler());
        }
      }
    });
    myScopeCombo.getComboBox().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        scheduleResultsUpdate();
      }
    });
    Disposer.register(myDisposable, myScopeCombo);


    myScopeDetailsPanel = new JPanel(new CardLayout());
    myScopeDetailsPanel.add(Scope.PROJECT.name(), new JLabel());
    myScopeDetailsPanel.add(Scope.MODULE.name(), myModuleComboBox);
    myScopeDetailsPanel.add(Scope.DIRECTORY.name(), directoryPanel);
    myScopeDetailsPanel.add(Scope.SCOPE.name(), myScopeCombo);

    myResultsPreviewTable = new JBTable() {
      @Override
      public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(getWidth(), 1 + getRowHeight() * Math.min(9, Math.max(4, getRowCount())));
      }
    };
    myResultsPreviewTable.getEmptyText().setShowAboveCenter(false);
    myResultsPreviewTable.setShowColumns(false);
    myResultsPreviewTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    myResultsPreviewTable.setShowGrid(false);
    myResultsPreviewTable.setIntercellSpacing(JBUI.emptySize());
    new NavigateToSourceListener().installOn(myResultsPreviewTable);
    applyFont(JBUI.Fonts.label(), myCbCaseSensitive, myCbPreserveCase, myCbWholeWordsOnly, myCbRegularExpressions,
              myResultsPreviewTable);
    KeymapUtil.reassignAction(mySearchComponent, MOVE_CARET_DOWN, MOVE_CARET_DOWN_ALTERNATIVE, WHEN_IN_FOCUSED_WINDOW);
    KeymapUtil.reassignAction(mySearchComponent, MOVE_CARET_UP, MOVE_CARET_UP_ALTERNATIVE, WHEN_IN_FOCUSED_WINDOW);
    KeymapUtil.reassignAction(mySearchComponent, NEW_LINE, NEW_LINE_ALTERNATIVE, WHEN_IN_FOCUSED_WINDOW);
    UIUtil.redirectKeystrokes(myDisposable, mySearchComponent, myResultsPreviewTable, MOVE_CARET_UP, MOVE_CARET_DOWN, NEW_LINE);


    myUsagePreviewPanel = new UsagePreviewPanel(myProject, new UsageViewPresentation(), PREVIEW_IS_EDITABLE) {
      @Override
      public Dimension getPreferredSize() {
        return new Dimension(myResultsPreviewTable.getWidth(), Math.max(getHeight(), getLineHeight() * 15));
      }
    };
    Disposer.register(myDisposable, myUsagePreviewPanel);
    myResultsPreviewTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int index = myResultsPreviewTable.getSelectedRow();
        if (index != -1) {
          UsageInfo usageInfo = ((UsageInfo2UsageAdapter)myResultsPreviewTable.getModel().getValueAt(index, 0)).getUsageInfo();
          myUsagePreviewPanel.updateLayout(Collections.singletonList(usageInfo));
          VirtualFile file = usageInfo.getVirtualFile();
          myUsagePreviewPanel.setBorder(IdeBorderFactory.createTitledBorder(file != null ? file.getPath() : "", false));
        }
        else {
          myUsagePreviewPanel.updateLayout(null);
          myUsagePreviewPanel.setBorder(IdeBorderFactory.createBorder());
        }
      }
    });
    mySearchRescheduleOnCancellationsAlarm = new Alarm();
    myUpdateResultsPopupBoundsAlarm = new Alarm();

    Splitter splitter = new JBSplitter(true, .33F);
    splitter.setDividerWidth(1);
    splitter.getDivider().setBackground(OnePixelDivider.BACKGROUND);
    JBScrollPane scrollPane = new JBScrollPane(myResultsPreviewTable) {
      @Override
      public Dimension getMinimumSize() {
        Dimension size = super.getMinimumSize();
        size.height = Math.max(size.height, myResultsPreviewTable.getPreferredScrollableViewportSize().height);
        return size;
      }
    };
    scrollPane.setBorder(IdeBorderFactory.createEmptyBorder());
    splitter.setFirstComponent(scrollPane);
    JPanel bottomPanel = new JPanel(new MigLayout("flowx, ins 4 4 0 4, fillx, hidemode 3, gap 0"));
    bottomPanel.add(myTabResultsButton);
    bottomPanel.add(Box.createHorizontalGlue(), "growx, pushx");
    JBLabel label = new JBLabel(KeymapUtil.getShortcutsText(new Shortcut[]{new KeyboardShortcut(OK_KEYSTROKE, null)}));
    label.setEnabled(false);
    bottomPanel.add(label, "gapright 10");
    bottomPanel.add(myOKButton);

    myCodePreviewComponent = myUsagePreviewPanel.createComponent();
    myCodePreviewComponent.setBorder(IdeBorderFactory.createBorder(SideBorder.BOTTOM));
    splitter.setSecondComponent(myCodePreviewComponent);

    setLayout(new MigLayout("flowx, ins 4, fillx, hidemode 2, gap 0"));
    add(myTitleLabel, "gapleft 4, sx 2, growx, pushx, growy");
    add(myCbCaseSensitive);
    add(myCbPreserveCase);
    add(myCbWholeWordsOnly);
    add(myCbRegularExpressions);
    add(RegExHelpPopup.createRegExLink("<html><body><b>?</b></body></html>", myCbRegularExpressions, LOG), "gapright 8");
    add(myCbFileFilter);
    add(myFileMaskField);
    add(myFilterContextButton, "wrap");
    add(mySearchTextArea, "pushx, growx, sx 10, wrap");
    add(myReplaceTextArea, "pushx, growx, sx 10, wrap");
    add(myScopeSelectionToolbar.getComponent(), "gaptop 4");
    add(myScopeDetailsPanel, "sx 9, pushx, growx, wrap");
    add(splitter, "pushx, growx, sx 10, wrap, pad 0 -4 0 4");
    add(bottomPanel, "pushx, growx, sx 10, pad 0 -4 0 4");

    MnemonicHelper.init(this);
  }

  private void adjustPopup() {
    if (myBalloon == null || !myBalloon.isVisible()) return;
    myBalloon.pack(false, true);
  }

  private void registerCloseAction(JBPopup popup) {
    final AnAction escape = ActionManager.getInstance().getAction("EditorEscape");
    DumbAwareAction closeAction = new DumbAwareAction() {
      @Override
      public void actionPerformed(AnActionEvent e) {
        if (myBalloon != null && myBalloon.isVisible()) {
          myBalloon.cancel();
        }
      }
    };
    closeAction.registerCustomShortcutSet(escape == null ? CommonShortcuts.ESCAPE : escape.getShortcutSet(), popup.getContent(), popup);
  }

  @Override
  public void addNotify() {
    super.addNotify();
    ApplicationManager.getApplication().invokeLater(() -> ScrollingUtil.ensureSelectionExists(myResultsPreviewTable), ModalityState.any());
    myScopeSelectionToolbar.updateActionsImmediately();
  }

  private void scheduleUpdateResultsPopupBounds() {
    if (myUpdateResultsPopupBoundsAlarm == null || myUpdateResultsPopupBoundsAlarm.isDisposed()) return;
    boolean later = myUpdateResultsPopupBoundsAlarm.getActiveRequestCount() > 0;

    myUpdateResultsPopupBoundsAlarm.cancelAllRequests();
    myUpdateResultsPopupBoundsAlarm.addRequest(() -> adjustPopup(), later ? 50 : 0);
  }


  private void initByModel() {
    FindModel myModel = myHelper.getModel();
    myCbCaseSensitive.setSelected(myModel.isCaseSensitive());
    myCbWholeWordsOnly.setSelected(myModel.isWholeWordsOnly());
    myCbRegularExpressions.setSelected(myModel.isRegularExpressions());

    mySelectedContextName = FindDialog.getSearchContextName(myModel);
    if (myModel.isReplaceState()) {
      myCbPreserveCase.setSelected(myModel.isPreserveCase());
    }
    mySelectedScope = getScope(myModel);
    final String dirName = myModel.getDirectoryName();
    setDirectories(FindInProjectSettings.getInstance(myProject).getRecentDirectories(), dirName);

    if (!StringUtil.isEmptyOrSpaces(dirName)) {
      VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(dirName);
      if (dir != null) {
        Module module = ModuleUtilCore.findModuleForFile(dir, myProject);
        if (module != null) {
          myModuleComboBox.setSelectedItem(module.getName());
        }
      }
    }

    if (mySelectedScope == Scope.MODULE) {
      myModuleComboBox.setSelectedItem(myModel.getModuleName());
    }
    boolean isThereFileFilter = myModel.getFileFilter() != null && !myModel.getFileFilter().isEmpty();
    myCbFileFilter.setSelected(isThereFileFilter);
    List<String> variants = Arrays.asList(ArrayUtil.reverseArray(FindSettings.getInstance().getRecentFileMasks()));
    myFileMaskField.setVariants(variants);
    if (!variants.isEmpty()) {
      myFileMaskField.setText(variants.get(0));
    }
    myFileMaskField.setEnabled(isThereFileFilter);
    String toSearch = myModel.getStringToFind();
    FindInProjectSettings findInProjectSettings = FindInProjectSettings.getInstance(myProject);

    if (StringUtil.isEmpty(toSearch)) {
      String[] history = findInProjectSettings.getRecentFindStrings();
      toSearch = history.length > 0 ? history[history.length - 1] : "";
    }

    mySearchComponent.setText(toSearch);
    String toReplace = myModel.getStringToReplace();

    if (StringUtil.isEmpty(toReplace)) {
      String[] history = findInProjectSettings.getRecentReplaceStrings();
      toReplace = history.length > 0 ? history[history.length - 1] : "";
    }
    myReplaceComponent.setText(toReplace);
    updateControls();
    updateScopeDetailsPanel();
    updateReplaceVisibility();
  }

  private void setDirectories(@NotNull List<String> strings, String directoryName) {
    if (myDirectoryComboBox.getItemCount() > 0) {
      myDirectoryComboBox.removeAllItems();
    }
    if (directoryName != null && !directoryName.isEmpty()) {
      if (strings.contains(directoryName)) {
        strings.remove(directoryName);
      }
      myDirectoryComboBox.addItem(directoryName);
    }
    for (int i = strings.size() - 1; i >= 0; i--) {
      myDirectoryComboBox.addItem(strings.get(i));
    }
    if (myDirectoryComboBox.getItemCount() == 0) {
      myDirectoryComboBox.addItem("");
    }
  }

  private static Scope getScope(FindModel model) {
    if (model.isCustomScope()) {
      return Scope.SCOPE;
    } else
    if (model.isProjectScope()) {
      return Scope.PROJECT;
    } else
    if (model.getDirectoryName() != null) {
       return Scope.DIRECTORY;
    } else
    if (model.getModuleName() != null) {
       return Scope.MODULE;
    }
    return Scope.PROJECT;
  }

  private void updateControls() {
    FindModel myModel = myHelper.getModel();
    if (myCbRegularExpressions.isSelected()) {
      myCbWholeWordsOnly.makeUnselectable(false);
    }
    else {
      myCbWholeWordsOnly.makeSelectable();
    }
    if (myModel.isReplaceState()) {
      if (myCbRegularExpressions.isSelected() || myCbCaseSensitive.isSelected()) {
        myCbPreserveCase.makeUnselectable(false);
      }
      else {
        myCbPreserveCase.makeSelectable();
      }

      if (myCbPreserveCase.isSelected()) {
        myCbRegularExpressions.makeUnselectable(false);
        myCbCaseSensitive.makeUnselectable(false);
      }
      else {
        myCbRegularExpressions.makeSelectable();
        myCbCaseSensitive.makeSelectable();
      }
    }
    myRecursiveDirectoryButton.setSelected(myModel.isWithSubdirectories());
  }

  public void updateReplaceVisibility() {
    boolean isReplaceState = myHelper.isReplaceState();
    myTitleLabel.setText(myHelper.getTitle());
    myReplaceTextArea.setVisible(isReplaceState);
    myCbPreserveCase.setVisible(isReplaceState);
    adjustPopup();
  }

  public JComponent getPreferredFocusedComponent() {
    return mySearchComponent;
  }

  private static void applyFont(JBFont font, Component... components) {
    for (Component component : components) {
      component.setFont(font);
    }
  }

  private void updateScopeDetailsPanel() {
    ((CardLayout)myScopeDetailsPanel.getLayout()).show(myScopeDetailsPanel, mySelectedScope.name());
    myScopeDetailsPanel.revalidate();
    myScopeDetailsPanel.repaint();
  }

  private void scheduleResultsUpdate() {
    if (myBalloon == null || !myBalloon.isVisible()) return;
    if (mySearchRescheduleOnCancellationsAlarm == null || mySearchRescheduleOnCancellationsAlarm.isDisposed()) return;
    updateControls();
    mySearchRescheduleOnCancellationsAlarm.cancelAllRequests();
    mySearchRescheduleOnCancellationsAlarm.addRequest(() -> findSettingsChanged(), 100);
  }

  private void finishPreviousPreviewSearch() {
    if (myResultsPreviewSearchProgress != null && !myResultsPreviewSearchProgress.isCanceled()) {
      myResultsPreviewSearchProgress.cancel();
    }
  }


  private void findSettingsChanged() {
    if (isShowing()) {
      ScrollingUtil.ensureSelectionExists(myResultsPreviewTable);
    }
    final ModalityState state = ModalityState.current();
    finishPreviousPreviewSearch();
    mySearchRescheduleOnCancellationsAlarm.cancelAllRequests();
    applyTo(myHelper.getModel(), false);
    myHelper.updateFindSettings();
    FindModel findInProjectModel = FindManager.getInstance(myProject).getFindInProjectModel();
    FindModel copy = new FindModel();
    copy.copyFrom(findInProjectModel);

    findInProjectModel.copyFrom(myHelper.getModel());
    ((FindManagerImpl)FindManager.getInstance(myProject)).changeGlobalSettings(myHelper.getModel());//todo check if we really need to do it now
    FindSettings findSettings = FindSettings.getInstance();
    findSettings.setDefaultScopeName(myScopeCombo.getSelectedScopeName());
    findSettings.setFileMask(myHelper.getModel().getFileFilter());

    ValidationInfo result = getValidationInfo(myHelper.getModel());

    final ProgressIndicatorBase progressIndicatorWhenSearchStarted = new ProgressIndicatorBase();
    myResultsPreviewSearchProgress = progressIndicatorWhenSearchStarted;

    final DefaultTableModel model = new DefaultTableModel() {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };

    model.addColumn("Usages");
    // Use previously shown usage files as hint for faster search and better usage preview performance if pattern length increased
    final LinkedHashSet<VirtualFile> filesToScanInitially = new LinkedHashSet<>();

    if (myHelper.myPreviousModel != null && myHelper.myPreviousModel.getStringToFind().length() < myHelper.getModel().getStringToFind().length()) {
      final DefaultTableModel previousModel = (DefaultTableModel)myResultsPreviewTable.getModel();
      for (int i = 0, len = previousModel.getRowCount(); i < len; ++i) {
        final UsageInfo2UsageAdapter usage = (UsageInfo2UsageAdapter)previousModel.getValueAt(i, 0);
        final VirtualFile file = usage.getFile();
        if (file != null) filesToScanInitially.add(file);
      }
    }

    myHelper.myPreviousModel = myHelper.getModel().clone();

    myCodePreviewComponent.setVisible(false);

    myResultsPreviewTable.setModel(model);

    if (result != null) {
      myResultsPreviewTable.getEmptyText().setText(UIBundle.message("message.nothingToShow"));
      adjustPopup();
      return;
    }

    myResultsPreviewTable.getColumnModel().getColumn(0).setCellRenderer(new FindDialog.UsageTableCellRenderer(myCbFileFilter.isSelected(), false));
    myResultsPreviewTable.getEmptyText().setText("Searching...");

    final AtomicInteger resultsCount = new AtomicInteger();
    final AtomicInteger resultsFilesCount = new AtomicInteger();

    ProgressIndicatorUtils.scheduleWithWriteActionPriority(myResultsPreviewSearchProgress, new ReadTask() {
      @Override
      public void computeInReadAction(@NotNull ProgressIndicator indicator) {
        final UsageViewPresentation presentation =
          FindInProjectUtil.setupViewPresentation(findSettings.isShowResultsInSeparateView(), /*findModel*/myHelper.getModel().clone());
        final boolean showPanelIfOnlyOneUsage = !findSettings.isSkipResultsWithOneUsage();

        final FindUsagesProcessPresentation processPresentation =
          FindInProjectUtil.setupProcessPresentation(myProject, showPanelIfOnlyOneUsage, presentation);
        Ref<VirtualFile> lastUsageFileRef = new Ref<>();

        FindInProjectUtil.findUsages(myHelper.getModel().clone(), myProject, info -> {
          final Usage usage = UsageInfo2UsageAdapter.CONVERTER.fun(info);
          usage.getPresentation().getIcon(); // cache icon

          VirtualFile file = lastUsageFileRef.get();
          VirtualFile usageFile = info.getVirtualFile();
          if (file == null || !file.equals(usageFile)) {
            resultsFilesCount.incrementAndGet();
            lastUsageFileRef.set(usageFile);
          }

          ApplicationManager.getApplication().invokeLater(() -> {
            model.addRow(new Object[]{usage});
            myCodePreviewComponent.setVisible(true);
            if (model.getRowCount() == 1 && myResultsPreviewTable.getModel() == model) {
              myResultsPreviewTable.setRowSelectionInterval(0, 0);
            }
            scheduleUpdateResultsPopupBounds();
          }, state);
          if (resultsCount.get() < 10) ApplicationManager.getApplication().invokeLater(() -> {adjustPopup();});
          return resultsCount.incrementAndGet() < ShowUsagesAction.USAGES_PAGE_SIZE;
        }, processPresentation, filesToScanInitially);

        boolean succeeded = !progressIndicatorWhenSearchStarted.isCanceled();
        if (succeeded) {
          ApplicationManager.getApplication().invokeLater(() -> {
            if (progressIndicatorWhenSearchStarted == myResultsPreviewSearchProgress && !myResultsPreviewSearchProgress.isCanceled()) {
              int occurrences = resultsCount.get();
              int filesWithOccurrences = resultsFilesCount.get();
              if (occurrences == 0) myResultsPreviewTable.getEmptyText().setText(UIBundle.message("message.nothingToShow"));
              myCodePreviewComponent.setVisible(occurrences > 0);
              StringBuilder info = new StringBuilder();
              if (occurrences > 0) {
                info.append(Math.min(ShowUsagesAction.USAGES_PAGE_SIZE, occurrences));
                boolean foundAllUsages = occurrences < ShowUsagesAction.USAGES_PAGE_SIZE;
                if (!foundAllUsages) {
                  info.append("+");
                }
                info.append(UIBundle.message("message.matches", occurrences));
                info.append(" in ");
                info.append(filesWithOccurrences);
                if (!foundAllUsages) {
                  info.append("+");
                }
                info.append(UIBundle.message("message.files", filesWithOccurrences));
              }
              mySearchTextArea.setInfoText(info.toString());
            }
          }, state);
        }
      }

      @Override
      public void onCanceled(@NotNull ProgressIndicator indicator) {
        if (isShowing() && progressIndicatorWhenSearchStarted == myResultsPreviewSearchProgress) {
          scheduleResultsUpdate();
        }
      }
    });
  }

  @Nullable
  public String getFileTypeMask() {
    String mask = null;
    if (myCbFileFilter != null && myCbFileFilter.isSelected()) {
      mask = myFileMaskField.getText();
    }
    return mask;
  }

  @Nullable("null means OK")
  private ValidationInfo getValidationInfo(@NotNull FindModel model) {
    if (mySelectedScope == Scope.DIRECTORY) {
      VirtualFile directory = FindInProjectUtil.getDirectory(model);
      if (directory == null) {
        return new ValidationInfo(FindBundle.message("find.directory.not.found.error", getDirectory()), myDirectoryComboBox);
      }
    }

    if (!myHelper.canSearchThisString()) {
      return new ValidationInfo("String to find is empty", mySearchComponent);
    }

    if (myCbRegularExpressions != null && myCbRegularExpressions.isSelected() && myCbRegularExpressions.isEnabled()) {
      String toFind = getStringToFind();
      try {
        boolean isCaseSensitive = myCbCaseSensitive != null && myCbCaseSensitive.isSelected() && myCbCaseSensitive.isEnabled();
        Pattern pattern =
          Pattern.compile(toFind, isCaseSensitive ? Pattern.MULTILINE : Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        if (pattern.matcher("").matches() && !toFind.endsWith("$") && !toFind.startsWith("^")) {
          return new ValidationInfo(FindBundle.message("find.empty.match.regular.expression.error"), mySearchComponent);
        }
      }
      catch (PatternSyntaxException e) {
        return new ValidationInfo(FindBundle.message("find.invalid.regular.expression.error", toFind, e.getDescription()),
                                  mySearchComponent);
      }
    }

    final String mask = getFileTypeMask();

    if (mask != null) {
      if (mask.isEmpty()) {
        return new ValidationInfo(FindBundle.message("find.filter.empty.file.mask.error"), myFileMaskField);
      }

      if (mask.contains(";")) {
        return new ValidationInfo("File masks should be comma-separated", myFileMaskField);
      }

      else {
        try {
          FindInProjectUtil.createFileMaskRegExp(mask);   // verify that the regexp compiles
        }
        catch (PatternSyntaxException ex) {
          return new ValidationInfo(FindBundle.message("find.filter.invalid.file.mask.error", mask), myFileMaskField);
        }
      }
    }
    return null;
  }

  @NotNull
  public String getStringToFind() {
    return mySearchComponent.getText();
  }

  @NotNull
  private String getStringToReplace() {
    return myReplaceComponent.getText();
  }

  private String getDirectory() {
    return (String)myDirectoryComboBox.getSelectedItem();
  }


  private void applyTo(@NotNull FindModel model, boolean findAll) {
    model.setCaseSensitive(myCbCaseSensitive.isSelected());

    if (model.isReplaceState()) {
      model.setPreserveCase(myCbPreserveCase.isSelected());
    }

    model.setWholeWordsOnly(myCbWholeWordsOnly.isSelected());

    String selectedSearchContextInUi = mySelectedContextName;
    FindModel.SearchContext searchContext = FindDialog.parseSearchContext(selectedSearchContextInUi);

    model.setSearchContext(searchContext);

    model.setRegularExpressions(myCbRegularExpressions.isSelected());
    String stringToFind = getStringToFind();
    model.setStringToFind(stringToFind);

    if (model.isReplaceState()) {
      model.setPromptOnReplace(true);
      model.setReplaceAll(false);
      String stringToReplace = getStringToReplace();
      model.setStringToReplace(StringUtil.convertLineSeparators(stringToReplace));
    }


    model.setProjectScope(mySelectedScope == Scope.PROJECT);
    model.setDirectoryName(null);
    model.setModuleName(null);
    model.setCustomScopeName(null);
    model.setCustomScope(null);
    model.setCustomScope(false);

    if (mySelectedScope == Scope.DIRECTORY) {
      String directory = getDirectory();
      model.setDirectoryName(directory == null ? "" : directory);
      model.setWithSubdirectories(myRecursiveDirectoryButton.isSelected());
    }
    else if (mySelectedScope == Scope.MODULE) {
      model.setModuleName((String)myModuleComboBox.getSelectedItem());
    }
    else if (mySelectedScope == Scope.SCOPE) {
      SearchScope selectedScope = myScopeCombo.getSelectedScope();
      String customScopeName = selectedScope == null ? null : selectedScope.getDisplayName();
      model.setCustomScopeName(customScopeName);
      model.setCustomScope(selectedScope);
      model.setCustomScope(true);
    }

    model.setFindAll(findAll);

    String mask = getFileTypeMask();
    model.setFileFilter(mask);
  }

  private static void initCombobox(@NotNull final ComboBox comboBox) {
    comboBox.setEditable(true);
    comboBox.setMaximumRowCount(8);
  }


  private class MySwitchContextToggleAction extends ToggleAction {
    public MySwitchContextToggleAction(FindModel.SearchContext context) {
      super(FindDialog.getPresentableName(context));
    }

    @Override
    public void beforeActionPerformedUpdate(@NotNull AnActionEvent e) {
      super.beforeActionPerformedUpdate(e);
    }

    @Override
    public boolean isSelected(AnActionEvent e) {
      return Comparing.equal(mySelectedContextName, getTemplatePresentation().getText());
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
      if (state) {
        mySelectedContextName = getTemplatePresentation().getText();
        scheduleResultsUpdate();
      }
    }
  }


  private class MySelectScopeToggleAction extends ToggleAction implements CustomComponentAction {
    private final Scope myScope;

    public MySelectScopeToggleAction(Scope scope) {
      super(FindBundle.message("find.popup.scope." + scope.name().toLowerCase()), null, EmptyIcon.ICON_0);
      getTemplatePresentation().setHoveredIcon(EmptyIcon.ICON_0);
      getTemplatePresentation().setDisabledIcon(EmptyIcon.ICON_0);
      myScope = scope;
    }

    @Override
    public JComponent createCustomComponent(Presentation presentation) {
      return new ActionButtonWithText(this, presentation, ActionPlaces.EDITOR_TOOLBAR, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);
    }

    @Override
    public boolean displayTextInToolbar() {
      return true;
    }

    @Override
    public boolean isSelected(AnActionEvent e) {
      return mySelectedScope == myScope;
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
      if (state) {
        mySelectedScope = myScope;
        myScopeSelectionToolbar.updateActionsImmediately();
        updateScopeDetailsPanel();
        scheduleResultsUpdate();
      }
    }
  }

  private class NavigateToSourceListener extends DoubleClickListener {

    @Override
    protected boolean onDoubleClick(MouseEvent event) {
      Object source = event.getSource();
      if (!(source instanceof JBTable)) return false;
      navigateToSelectedUsage((JBTable)source);
      return true;
    }

    @Override
    public void installOn(@NotNull final Component c) {
      super.installOn(c);

      if (c instanceof JBTable) {
        AnAction anAction = new AnAction() {
          @Override
          public void actionPerformed(AnActionEvent e) {
            navigateToSelectedUsage((JBTable)c);
          }
        };

        String key = "navigate.to.usage";
        JComponent component = (JComponent)c;
        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(NEW_LINE, key);
        component.getActionMap().put(key, new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            navigateToSelectedUsage((JBTable)c);
          }
        });
        anAction.registerCustomShortcutSet(CommonShortcuts.ALT_ENTER, component);
        anAction.registerCustomShortcutSet(CommonShortcuts.getEditSource(), component);
      }
    }
  }

  private void navigateToSelectedUsage(JBTable source) {
    int[] rows = source.getSelectedRows();
    java.util.List<Usage> navigations = null;
    for (int row : rows) {
      Object valueAt = source.getModel().getValueAt(row, 0);
      if (valueAt instanceof Usage) {
        if (navigations == null) navigations = new SmartList<>();
        Usage at = (Usage)valueAt;
        navigations.add(at);
      }
    }

    if (navigations != null) {
      applyTo(FindManager.getInstance(myProject).getFindInProjectModel(), false);
      myBalloon.cancel();

      navigations.get(0).navigate(true);
      for (int i = 1; i < navigations.size(); ++i) navigations.get(i).highlightInEditor();
    }
  }
}