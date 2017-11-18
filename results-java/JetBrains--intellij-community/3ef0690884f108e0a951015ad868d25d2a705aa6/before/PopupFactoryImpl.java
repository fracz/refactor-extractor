/*
 * Copyright (c) 2000-2004 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.ui.popup;

import com.intellij.CommonBundle;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionMenu;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.FocusTrackback;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.ui.popup.mock.MockConfirmation;
import com.intellij.ui.popup.tree.TreePopupImpl;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PopupFactoryImpl extends JBPopupFactory {
  private static final Logger LOG = Logger.getInstance("#com.intellij.ui.popup.PopupFactoryImpl");
  private static final Icon QUICK_LIST_ICON = IconLoader.getIcon("/actions/quickList.png");

  public ListPopup createConfirmation(String title, final Runnable onYes, int defaultOptionIndex) {
    return createConfirmation(title, CommonBundle.getYesButtonText(), CommonBundle.getNoButtonText(), onYes, defaultOptionIndex);
  }

  public ListPopup createConfirmation(String title, final String yesText, String noText, final Runnable onYes, int defaultOptionIndex) {
    return createConfirmation(title, yesText, noText, onYes, EmptyRunnable.getInstance(), defaultOptionIndex);
  }

  public ListPopup createConfirmation(String title, final String yesText, String noText, final Runnable onYes, final Runnable onNo, int defaultOptionIndex) {

      final BaseListPopupStep<String> step = new BaseListPopupStep<String>(title, new String[]{yesText, noText}) {
        public PopupStep onChosen(String selectedValue, final boolean finalChoice) {
          if (selectedValue.equals(yesText)) {
            onYes.run();
          }
          else {
            onNo.run();
          }
          return FINAL_CHOICE;
        }

        public void canceled() {
          onNo.run();
        }

        public boolean isMnemonicsNavigationEnabled() {
          return true;
        }
      };
      step.setDefaultOptionIndex(defaultOptionIndex);

    final ApplicationEx app = ApplicationManagerEx.getApplicationEx();
    if (app == null || !app.isUnitTestMode()) {
      return new ListPopupImpl(step);
    } else {
      return new MockConfirmation(step, yesText);
    }
  }


  public ListPopup createActionGroupPopup(final String title,
                                          final ActionGroup actionGroup,
                                          DataContext dataContext,
                                          boolean showNumbers,
                                          boolean showDisabledActions,
                                          boolean honorActionMnemonics,
                                          final Runnable disposeCallback,
                                          final int maxRowCount) {
    final Component component = PlatformDataKeys.CONTEXT_COMPONENT.getData(dataContext);
    LOG.assertTrue(component != null);

    ListPopupStep step = createActionsStep(actionGroup, dataContext, showNumbers, showDisabledActions, title, component, honorActionMnemonics);

    final ListPopupImpl popup = new ListPopupImpl(step, maxRowCount) {
      public void dispose() {
        if (disposeCallback != null) {
          disposeCallback.run();
        }
        ActionMenu.showDescriptionInStatusBar(true, component, null);
        super.dispose();
      }
    };
    popup.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e) {
        final JList list = (JList)e.getSource();
        final ActionItem actionItem = (ActionItem)list.getSelectedValue();
        if (actionItem == null) return;
        AnAction action = actionItem.getAction();
        Presentation presentation = new Presentation();
        presentation.setDescription(action.getTemplatePresentation().getDescription());
        action.update(new AnActionEvent(null, DataManager.getInstance().getDataContext(component), ActionPlaces.UNKNOWN, presentation,
                                        ActionManager.getInstance(), 0));
        ActionMenu.showDescriptionInStatusBar(true, component, presentation.getDescription());
      }
    });
    return popup;
  }

  public ListPopup createActionGroupPopup(String title,
                                          ActionGroup actionGroup,
                                          DataContext dataContext,
                                          ActionSelectionAid selectionAidMethod,
                                          boolean showDisabledActions) {
    return createActionGroupPopup(title, actionGroup, dataContext,
                                  selectionAidMethod == ActionSelectionAid.NUMBERING,
                                  showDisabledActions,
                                  selectionAidMethod == ActionSelectionAid.MNEMONICS,
                                  null, -1);
  }

  public ListPopup createActionGroupPopup(String title,
                                          ActionGroup actionGroup,
                                          DataContext dataContext,
                                          ActionSelectionAid selectionAidMethod,
                                          boolean showDisabledActions,
                                          Runnable disposeCallback,
                                          int maxRowCount) {
    return createActionGroupPopup(title, actionGroup, dataContext,
                                  selectionAidMethod == ActionSelectionAid.NUMBERING,
                                  showDisabledActions,
                                  selectionAidMethod == ActionSelectionAid.MNEMONICS,
                                  disposeCallback,
                                  maxRowCount);
  }

  public ListPopupStep createActionsStep(final ActionGroup actionGroup,
                                         final DataContext dataContext,
                                         final boolean showNumbers,
                                         final boolean showDisabledActions,
                                         final String title,
                                         final Component component,
                                         final boolean honorActionMnemonics) {
    return createActionsStep(actionGroup, dataContext, showNumbers, showDisabledActions, title, component, honorActionMnemonics, 0, false);
  }

  public ListPopupStep createActionsStep(ActionGroup actionGroup, DataContext dataContext, boolean showNumbers, boolean showDisabledActions,
                                         String title, Component component, boolean honorActionMnemonics, int defaultOptionIndex,
                                         final boolean autoSelectionEnabled) {
    final ArrayList<ActionItem> items = new ArrayList<ActionItem>();
    fillModel(items, actionGroup, dataContext, showNumbers, showDisabledActions, new HashMap<AnAction, Presentation>(), 0, false, null, honorActionMnemonics);

    return new ActionPopupStep(items, title, component, showNumbers || honorActionMnemonics && itemsHaveMnemonics(items), defaultOptionIndex,
                               autoSelectionEnabled);
  }

  private static boolean itemsHaveMnemonics(final ArrayList<ActionItem> items) {
    for (ActionItem item : items) {
      if (item.getAction().getTemplatePresentation().getMnemonic() != 0) return true;
    }

    return false;
  }

  public ListPopup createWizardStep(PopupStep step) {
    return new ListPopupImpl((ListPopupStep) step);
  }

  public ListPopup createListPopup(ListPopupStep step) {
    return new ListPopupImpl(step);
  }

  public TreePopup createTree(JBPopup parent, TreePopupStep aStep, Object parentValue) {
    return new TreePopupImpl(parent, aStep, parentValue);
  }

  public TreePopup createTree(TreePopupStep aStep) {
    return new TreePopupImpl(aStep);
  }

  public ComponentPopupBuilder createComponentPopupBuilder(JComponent content, JComponent prefferableFocusComponent) {
    return new ComponentPopupBuilderImpl(content, prefferableFocusComponent);
  }


  public RelativePoint guessBestPopupLocation(DataContext dataContext) {
    KeyboardFocusManager focusManager=KeyboardFocusManager.getCurrentKeyboardFocusManager();
    Component component = focusManager.getFocusOwner();
    JComponent focusOwner=component instanceof JComponent ? (JComponent)component : null;

    if (focusOwner == null) {
      Project project = PlatformDataKeys.PROJECT.getData(dataContext);
      IdeFrameImpl frame = project == null ? null : ((WindowManagerEx)WindowManager.getInstance()).getFrame(project);
      focusOwner = frame == null ? null : frame.getRootPane();
      if (focusOwner == null) {
        throw new IllegalArgumentException("focusOwner cannot be null");
      }
    }

    Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
    if (editor != null && focusOwner == editor.getContentComponent()) {
      return guessBestPopupLocation(editor);
    }
    else {
      return guessBestPopupLocation(focusOwner);
    }
  }

  public RelativePoint guessBestPopupLocation(final JComponent component) {
    Point popupMenuPoint = null;
    final Rectangle visibleRect = component.getVisibleRect();
    if (component instanceof JList) { // JList
      JList list = (JList)component;
      int firstVisibleIndex = list.getFirstVisibleIndex();
      int lastVisibleIndex = list.getLastVisibleIndex();
      int[] selectedIndices = list.getSelectedIndices();
      for (int index : selectedIndices) {
        if (firstVisibleIndex <= index && index <= lastVisibleIndex) {
          Rectangle cellBounds = list.getCellBounds(index, index);
          popupMenuPoint = new Point(visibleRect.x + visibleRect.width / 4, cellBounds.y + cellBounds.height);
          break;
        }
      }
    }
    else if (component instanceof JTree) { // JTree
      JTree tree = (JTree)component;
      int[] selectionRows = tree.getSelectionRows();
      for (int i = 0; selectionRows != null && i < selectionRows.length; i++) {
        int row = selectionRows[i];
        Rectangle rowBounds = tree.getRowBounds(row);
        if (visibleRect.y <= rowBounds.y && rowBounds.y <= visibleRect.y + visibleRect.height) {
          popupMenuPoint = new Point(visibleRect.x + visibleRect.width / 4, rowBounds.y + rowBounds.height);
          break;
        }
      }
    } else if (component instanceof PopupOwner){
      popupMenuPoint = ((PopupOwner)component).getBestPopupPosition();
    }
    // TODO[vova] add usability for JTable
    if (popupMenuPoint == null) {
      popupMenuPoint = new Point(visibleRect.x + visibleRect.width / 2, visibleRect.y + visibleRect.height / 2);
    }

    return new RelativePoint(component, popupMenuPoint);
  }

  public RelativePoint guessBestPopupLocation(Editor editor) {
    VisualPosition logicalPosition = editor.getCaretModel().getVisualPosition();
    Point p = editor.visualPositionToXY(new VisualPosition(logicalPosition.line + 1, logicalPosition.column));

    final Rectangle visibleArea = editor.getScrollingModel().getVisibleArea();
    if (!visibleArea.contains(p)) {
      p = new Point((visibleArea.x + visibleArea.width) / 2, (visibleArea.y + visibleArea.height) / 2);
    }

    return new RelativePoint(editor.getContentComponent(), p);
  }

  public Point getCenterOf(JComponent container, JComponent content) {
    return AbstractPopup.getCenterOf(container, content);
  }

  private static int fillModel(List<ActionItem> listModel,
                               ActionGroup actionGroup,
                               DataContext dataContext,
                               boolean showNumbers,
                               boolean showDisabled,
                               HashMap<AnAction, Presentation> action2presentation,
                               int startNumberingFrom, boolean prependWithSeparator, String separatorText,
                               final boolean honorActionMnemonics) {
    int n = startNumberingFrom;
    AnAction[] actions = actionGroup.getChildren(new AnActionEvent(null,
                                                                   dataContext,
                                                                   ActionPlaces.UNKNOWN,
                                                                   getPresentation(actionGroup, action2presentation),
                                                                   ActionManager.getInstance(),
                                                                   0));
    int maxWidth = -1;
    int maxHeight = -1;
    for (AnAction action : actions) {
      if (action == null) continue;

      Icon icon = action.getTemplatePresentation().getIcon();
      if (icon != null) {
        final int width = icon.getIconWidth();
        final int height = icon.getIconHeight();
        if (maxWidth < width) {
          maxWidth = width;
        }
        if (maxHeight < height) {
          maxHeight = height;
        }
      }
    }
    Icon emptyIcon = maxHeight != -1 && maxWidth != -1 ? new EmptyIcon(maxWidth, maxHeight) : null;

    String sepText = separatorText;
    for (AnAction action : actions) {
      if (action instanceof Separator) {
        prependWithSeparator = true;
        sepText = ((Separator)action).getText();
      }
      else {
        if (action instanceof ActionGroup) {

          ActionGroup group = (ActionGroup)action;
          n = group.isPopup()
              ? appendAction(group, action2presentation, dataContext, showDisabled, showNumbers, n, listModel, emptyIcon,
                             prependWithSeparator, sepText, honorActionMnemonics)
              : fillModel(listModel, group, dataContext, showNumbers, showDisabled, action2presentation, n, prependWithSeparator, separatorText, honorActionMnemonics);
        }
        else {
          n = appendAction(action, action2presentation, dataContext, showDisabled, showNumbers, n, listModel, emptyIcon,
                           prependWithSeparator, sepText, honorActionMnemonics);
        }
        prependWithSeparator = false;
        separatorText = null;
      }
    }

    return n;
  }

  private static int appendAction(AnAction action,
                                  HashMap<AnAction, Presentation> action2presentation,
                                  DataContext dataContext,
                                  boolean showDisabled,
                                  boolean showNumbers,
                                  int n,
                                  List<ActionItem> listModel,
                                  Icon emptyIcon,
                                  final boolean prependWithSeparator,
                                  final String separatorText,
                                  final boolean honorActionMnemonics) {
    Presentation presentation = getPresentation(action, action2presentation);
    AnActionEvent event = new AnActionEvent(null,
                                            dataContext,
                                            ActionPlaces.UNKNOWN,
                                            presentation,
                                            ActionManager.getInstance(),
                                            0);

    action.beforeActionPerformedUpdate(event);
    if ((showDisabled || presentation.isEnabled()) && presentation.isVisible()) {
      String text = presentation.getText();
      if (showNumbers) {
        if (n < 9) {
          text = "&" + (n + 1) + ". " + text;
        }
        else if (n == 9) {
          text = "&" + 0 + ". " + text;
        }
        else {
          text = "&" + (char)('A' + n - 10) + ". " + text;
        }
        n++;
      }
      else if (honorActionMnemonics) {
        text = Presentation.restoreTextWithMnemonic(text, action.getTemplatePresentation().getMnemonic());
      }

      Icon icon = presentation.getIcon();
      if (icon == null) {
        @NonNls final String actionId = ActionManager.getInstance().getId(action);
        if (actionId != null && actionId.startsWith("QuickList.")){
          icon = QUICK_LIST_ICON;
        }
        else {
          icon = emptyIcon;
        }

      }
      listModel.add(new ActionItem(action, text, presentation.isEnabled(), icon, prependWithSeparator, separatorText));
    }
    return n;
  }

  private static Presentation getPresentation(AnAction action, Map<AnAction, Presentation> action2presentation) {
    Presentation presentation = action2presentation.get(action);
    if (presentation == null) {
      presentation = (Presentation)action.getTemplatePresentation().clone();
      action2presentation.put(action, presentation);
    }
    return presentation;
  }

  private static class ActionItem {
    private AnAction myAction;
    private String myText;
    private boolean myIsEnabled;
    private Icon myIcon;
    private boolean myPrependWithSeparator;
    private String mySeparatorText;

    public ActionItem(AnAction action, @NotNull String text, boolean enabled, Icon icon, final boolean prependWithSeparator, String separatorText) {
      myAction = action;
      myText = text;
      myIsEnabled = enabled;
      myIcon = icon;
      myPrependWithSeparator = prependWithSeparator;
      mySeparatorText = separatorText;
    }

    public AnAction getAction() {
      return myAction;
    }

    @NotNull
    public String getText() {
      return myText;
    }

    public Icon getIcon() {
      return myIcon;
    }

    public boolean isPrependWithSeparator() {
      return myPrependWithSeparator;
    }

    public String getSeparatorText() {
      return mySeparatorText;
    }

    public boolean isEnabled() { return myIsEnabled; }
  }

  private static class ActionPopupStep implements ListPopupStep<ActionItem>, MnemonicNavigationFilter<ActionItem>, SpeedSearchFilter<ActionItem> {
    private final ArrayList<ActionItem> myItems;
    private final String myTitle;
    private Component myContext;
    private boolean myEnableMnemonics;
    private int myDefaultOptionIndex;
    private boolean myAutoSelectionEnabled;

    public ActionPopupStep(@NotNull final ArrayList<ActionItem> items,
                           final String title,
                           Component context,
                           boolean enableMnemonics,
                           final int defaultOptionIndex, final boolean autoSelection) {
      myItems = items;
      myTitle = title;
      myContext = context;
      myEnableMnemonics = enableMnemonics;
      myDefaultOptionIndex = defaultOptionIndex;
      myAutoSelectionEnabled = autoSelection;
    }

    @NotNull
    public List<ActionItem> getValues() {
      return myItems;
    }

    public boolean isSelectable(final ActionItem value) {
      return value.isEnabled();
    }

    public int getMnemonicPos(final ActionItem value) {
      final String text = getTextFor(value);
      int i = text.indexOf(UIUtil.MNEMONIC);
      if (i < 0) {
        i = text.indexOf('&');
      }
      if (i < 0) {
        i = text.indexOf('_');
      }
      return i;
    }

    public Icon getIconFor(final ActionItem aValue) {
      return aValue.getIcon();
    }

    @NotNull
    public String getTextFor(final ActionItem value) {
      return value.getText();
    }

    public ListSeparator getSeparatorAbove(final ActionItem value) {
      return value.isPrependWithSeparator() ? new ListSeparator(value.getSeparatorText()) : null;
    }

    public int getDefaultOptionIndex() {
      return myDefaultOptionIndex;
    }

    public String getTitle() {
      return myTitle;
    }

    public PopupStep onChosen(final ActionItem actionChoice, final boolean finalChoice) {
      if (!actionChoice.isEnabled()) return PopupStep.FINAL_CHOICE;
      final AnAction action = actionChoice.getAction();
      final DataContext dataContext = DataManager.getInstance().getDataContext(myContext);
      if (action instanceof ActionGroup) {
        return JBPopupFactory.getInstance().createActionsStep((ActionGroup)action, dataContext, myEnableMnemonics, false, null, myContext, false);
      }
      else {
        // invokeLater is required to get a chance for the popup to hide in case the action called displays modal dialog
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            action.actionPerformed(new AnActionEvent(null,
                                                     dataContext,
                                                     ActionPlaces.UNKNOWN,
                                                     (Presentation)action.getTemplatePresentation().clone(),
                                                     ActionManager.getInstance(),
                                                     0));
          }
        });
        return PopupStep.FINAL_CHOICE;
      }
    }

    public boolean hasSubstep(final ActionItem selectedValue) {
      return selectedValue != null && selectedValue.isEnabled() && selectedValue.getAction() instanceof ActionGroup;
    }

    public void canceled() {
    }

    public boolean isMnemonicsNavigationEnabled() {
      return myEnableMnemonics;
    }

    public MnemonicNavigationFilter<ActionItem> getMnemonicNavigationFilter() {
      return this;
    }

    public boolean canBeHidden(final ActionItem value) {
      return true;
    }

    public String getIndexedString(final ActionItem value) {
      return getTextFor(value);
    }

    public boolean isSpeedSearchEnabled() {
      return !myEnableMnemonics;
    }

    public boolean isAutoSelectionEnabled() {
      return myAutoSelectionEnabled;
    }

    public SpeedSearchFilter<ActionItem> getSpeedSearchFilter() {
      return this;
    }
  }

  public JBPopup getChildPopup(@NotNull final Component component) {
    return FocusTrackback.getChildPopup(component);
  }

}