package com.intellij.codeInsight.intention.impl;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.codeInsight.hint.QuestionAction;
import com.intellij.codeInsight.intention.EmptyIntentionAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.config.IntentionManagerSettings;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.LightweightHint;
import com.intellij.ui.RowIcon;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Alarm;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ConcurrentHashSet;
import com.intellij.xml.util.XmlUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * @author max
 * @author Mike
 * @author Valentin
 * @author Eugene Belyaev
 */
public class IntentionHintComponent extends JPanel {
  private static final Logger LOG = Logger.getInstance(
    "#com.intellij.codeInsight.intention.impl.IntentionHintComponent.ListPopupRunnable");

  private static final Icon ourIntentionIcon = IconLoader.getIcon("/actions/intentionBulb.png");
  private static final Icon ourQuickFixIcon = IconLoader.getIcon("/actions/quickfixBulb.png");
  private static final Icon ourIntentionOffIcon = IconLoader.getIcon("/actions/intentionOffBulb.png");
  private static final Icon ourQuickFixOffIcon = IconLoader.getIcon("/actions/quickfixOffBulb.png");
  private static final Icon ourArrowIcon = IconLoader.getIcon("/general/arrowDown.png");
  private static final Border INACTIVE_BORDER = null;
  private static final Insets INACTIVE_MARGIN = new Insets(0, 0, 0, 0);
  private static final Insets ACTIVE_MARGIN = new Insets(0, 0, 0, 0);

  private final Project myProject;
  private final Editor myEditor;

  private static Alarm myAlarm = new Alarm();

  private RowIcon myHighlightedIcon;
  private JButton myButton;

  private final Icon mySmartTagIcon;

  private static final int DELAY = 500;
  private MyComponentHint myComponentHint;
  private static final Color BACKGROUND_COLOR = new Color(255, 255, 255, 0);
  private boolean myPopupShown = false;
  private ListPopup myPopup;

  private class IntentionListStep implements ListPopupStep<IntentionActionWithTextCaching> {
    private final Set<IntentionActionWithTextCaching> myActions;
    private final IntentionManagerSettings mySettings;
    private final Set<IntentionAction> myQuickFixes = new ConcurrentHashSet<IntentionAction>();

    public IntentionListStep(List<HighlightInfo.IntentionActionDescriptor> quickFixes,
                             List<HighlightInfo.IntentionActionDescriptor> intentions) {
      mySettings = IntentionManagerSettings.getInstance();
      Set<HighlightInfo.IntentionActionDescriptor> allActions = new THashSet<HighlightInfo.IntentionActionDescriptor>(quickFixes);
      allActions.addAll(intentions);
      for (HighlightInfo.IntentionActionDescriptor pair : quickFixes) {
        myQuickFixes.add(pair.getAction());
        if (pair.getOptions() != null) {
          myQuickFixes.addAll(pair.getOptions());
        }
      }
      myActions = wrapActions(allActions);
    }

    private Set<IntentionActionWithTextCaching> wrapActions(Set<HighlightInfo.IntentionActionDescriptor> actions) {
      Set<IntentionActionWithTextCaching> compositeActions = new ConcurrentHashSet<IntentionActionWithTextCaching>(actions.size());
      for (HighlightInfo.IntentionActionDescriptor pair : actions) {
        if (pair.getAction() != null) {
          IntentionActionWithTextCaching action = new IntentionActionWithTextCaching(pair.getAction(), pair.getDisplayName());
          if (pair.getOptions() != null) {
            for (IntentionAction intentionAction : pair.getOptions()) {
              action.addAction(intentionAction, myQuickFixes.contains(intentionAction));
            }
          }
          compositeActions.add(action);
        }
      }
      return compositeActions;
    }

    public String getTitle() {
      return null;
    }

    public boolean isSelectable(final IntentionActionWithTextCaching action) {
      return true;
    }

    public PopupStep onChosen(final IntentionActionWithTextCaching action, final boolean finalChoice) {
      if (finalChoice && !(action.getAction() instanceof EmptyIntentionAction)) {
        applyAction(action.getAction());
        return PopupStep.FINAL_CHOICE;
      }

      if (hasSubstep(action)) {
        return getSubStep(action);
      }

      return FINAL_CHOICE;
    }

    private PopupStep getSubStep(final IntentionActionWithTextCaching action) {
      final ArrayList<HighlightInfo.IntentionActionDescriptor> intentions = new ArrayList<HighlightInfo.IntentionActionDescriptor>();
      final List<IntentionAction> optionIntentions = action.getOptionIntentions();
      for (final IntentionAction optionIntention : optionIntentions) {
        intentions.add(new HighlightInfo.IntentionActionDescriptor(optionIntention, null, action.getToolName()));
      }
      final ArrayList<HighlightInfo.IntentionActionDescriptor> quickFixes = new ArrayList<HighlightInfo.IntentionActionDescriptor>();
      final List<IntentionAction> optionFixes = action.getOptionFixes();
      for (final IntentionAction optionFix : optionFixes) {
        quickFixes.add(new HighlightInfo.IntentionActionDescriptor(optionFix, null, action.getToolName()));
      }

      return new IntentionListStep(quickFixes, intentions){
        public String getTitle() {
          return XmlUtil.escapeString(action.getToolName());
        }
      };
    }

    public boolean hasSubstep(final IntentionActionWithTextCaching action) {
      return action.getOptionIntentions().size() + action.getOptionFixes().size() > 0;
    }

    @NotNull
    public List<IntentionActionWithTextCaching> getValues() {
      ArrayList<IntentionActionWithTextCaching> result = new ArrayList<IntentionActionWithTextCaching>(myActions);
      Collections.sort(result, new Comparator<IntentionActionWithTextCaching>() {
        public int compare(final IntentionActionWithTextCaching o1, final IntentionActionWithTextCaching o2) {
          boolean isFix1 = myQuickFixes.contains(o1.getAction());
          boolean isFix2 = myQuickFixes.contains(o2.getAction());
          if (isFix1 != isFix2) {
            return isFix1 ? -1 : 1;
          }
          return Comparing.compare(o1.getText(), o2.getText());
        }
      });
      return result;
    }

    @NotNull
    public String getTextFor(final IntentionActionWithTextCaching action) {
      return action.getText();
    }

    public Icon getIconFor(final IntentionActionWithTextCaching value) {
      final IntentionAction action = value.getAction();

      if (mySettings.isShowLightBulb(action)) {
        if (myQuickFixes.contains(action)) {
          return ourQuickFixIcon;
        }
        else {
          return ourIntentionIcon;
        }
      }
      else {
        if (myQuickFixes.contains(action)) {
          return ourQuickFixOffIcon;
        }
        else {
          return ourIntentionOffIcon;
        }
      }
    }

    public void canceled() {
      if (myPopup.getListStep() == this) {
        // Root canceled. Create new popup. This one cannot be reused.
        myPopup = JBPopupFactory.getInstance().createListPopup(this);
      }
    }

    public int getDefaultOptionIndex() { return 0; }
    public ListSeparator getSeparatorAbove(final IntentionActionWithTextCaching value) { return null; }
    public boolean isMnemonicsNavigationEnabled() { return false; }
    public MnemonicNavigationFilter<IntentionActionWithTextCaching> getMnemonicNavigationFilter() { return null; }
    public boolean isSpeedSearchEnabled() { return false; }
    public boolean isAutoSelectionEnabled() { return false; }
    public SpeedSearchFilter<IntentionActionWithTextCaching> getSpeedSearchFilter() { return null; }

    public void updateActions(final List<HighlightInfo.IntentionActionDescriptor> quickFixes,
                              final List<HighlightInfo.IntentionActionDescriptor> intentions) {
      Set<HighlightInfo.IntentionActionDescriptor> allActions = new THashSet<HighlightInfo.IntentionActionDescriptor>(quickFixes);
      allActions.addAll(intentions);
      List<IntentionAction> actions = new ArrayList<IntentionAction>();
      for (HighlightInfo.IntentionActionDescriptor pair : quickFixes) {
        actions.add(pair.getAction());
        if (pair.getOptions() != null) {
          actions.addAll(pair.getOptions());
        }
      }
      myQuickFixes.addAll(actions);
      myActions.addAll(wrapActions(allActions));
      myPopup = JBPopupFactory.getInstance().createListPopup(this);
    }
  }

  private static class IntentionActionWithTextCaching {
    private ArrayList<IntentionAction> myOptionIntentions;
    private ArrayList<IntentionAction> myOptionFixes;
    private String myText = null;
    private IntentionAction myAction;
    private String myDisplayName;

    public IntentionActionWithTextCaching(IntentionAction action, String displayName) {
      myOptionIntentions = new ArrayList<IntentionAction>();
      myOptionFixes = new ArrayList<IntentionAction>();
      myText = action.getText();
      // needed for checking errors in user written actions
      //noinspection ConstantConditions
      LOG.assertTrue(myText != null, "action "+action.getClass()+" text returned null");
      myAction = action;
      myDisplayName = displayName;
    }

    String getText() {
      return myText;
    }

    public void addAction(final IntentionAction action, boolean isFix) {
      if (isFix) {
        myOptionFixes.add(action);
      }
      else {
        myOptionIntentions.add(action);
      }
    }

    public IntentionAction getAction() {
      return myAction;
    }

    public List<IntentionAction> getOptionIntentions() {
      return myOptionIntentions;
    }

    public List<IntentionAction> getOptionFixes() {
      return myOptionFixes;
    }

    public String getToolName() {
      return myDisplayName;
    }

    public String toString() {
      return getText();
    }
  }

  public static IntentionHintComponent showIntentionHint(Project project,
                                                         Editor view,
                                                         List<HighlightInfo.IntentionActionDescriptor> intentions,
                                                         List<HighlightInfo.IntentionActionDescriptor> quickFixes,
                                                         boolean showExpanded) {
    final IntentionHintComponent component = new IntentionHintComponent(project, view, intentions, quickFixes);

    if (showExpanded) {
      component.showIntentionHintImpl(false);
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          component.showPopup();
        }
      });
    }
    else {
      component.showIntentionHintImpl(true);
    }

    return component;
  }

  public void updateIfNotShowingPopup(List<HighlightInfo.IntentionActionDescriptor> quickfixes,
                                      List<HighlightInfo.IntentionActionDescriptor> intentions) {
    if (!myPopupShown) {
      if (myPopup.getListStep() instanceof IntentionListStep) {
        IntentionListStep step = (IntentionListStep)myPopup.getListStep();
        step.updateActions(quickfixes, intentions);
      }
      else {
       myPopup = JBPopupFactory.getInstance().createListPopup(new IntentionListStep(quickfixes, intentions));
      }
    }
  }

  private void showIntentionHintImpl(final boolean delay) {
    final int offset = myEditor.getCaretModel().getOffset();
    final HintManager hintManager = HintManager.getInstance();

    myComponentHint.setShouldDelay(delay);

    hintManager.showQuestionHint(myEditor,
                                 getHintPosition(myEditor, offset),
                                 offset,
                                 offset,
                                 myComponentHint,
                                 new QuestionAction() {
                                   public boolean execute() {
                                     showPopup();
                                     return true;
                                   }
                                 });
  }

  private static Point getHintPosition(Editor editor, int offset) {
    final LogicalPosition pos = editor.offsetToLogicalPosition(offset);
    int line = pos.line;


    final Point position = editor.logicalPositionToXY(new LogicalPosition(line, 0));
    final int yShift = (ourIntentionIcon.getIconHeight() - editor.getLineHeight() - 1) / 2 - 1;

    LOG.assertTrue(editor.getComponent().isDisplayable());
    Point location = SwingUtilities.convertPoint(editor.getContentComponent(),
                                                 new Point(editor.getScrollingModel().getVisibleArea().x, position.y + yShift),
                                                 editor.getComponent().getRootPane().getLayeredPane());

    return new Point(location.x, location.y);
  }

  public IntentionHintComponent(Project project,
                                Editor editor,
                                List<HighlightInfo.IntentionActionDescriptor> intentions,
                                List<HighlightInfo.IntentionActionDescriptor> quickFixes) {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    myProject = project;
    myEditor = editor;

    setLayout(new BorderLayout());
    setOpaque(false);

    boolean showFix = false;
    for (final HighlightInfo.IntentionActionDescriptor pairs : quickFixes) {
      IntentionAction fix = pairs.getAction();
      if (IntentionManagerSettings.getInstance().isShowLightBulb(fix)) {
        showFix = true;
        break;
      }
    }
    mySmartTagIcon = showFix ? ourQuickFixIcon : ourIntentionIcon;

    myHighlightedIcon = new RowIcon(2);
    myHighlightedIcon.setIcon(mySmartTagIcon, 0);
    myHighlightedIcon.setIcon(ourArrowIcon, 1);

    myButton = new JButton(mySmartTagIcon);
    myButton.setFocusable(false);
    myButton.setMargin(INACTIVE_MARGIN);
    myButton.setBorderPainted(false);
    myButton.setContentAreaFilled(false);

    add(myButton, BorderLayout.CENTER);
    setBorder(INACTIVE_BORDER);

    myButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showPopup();
      }
    });

    myButton.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        onMouseEnter();
      }

      public void mouseExited(MouseEvent e) {
        onMouseExit();
      }
    });

    myComponentHint = new MyComponentHint(this);
    myPopup = JBPopupFactory.getInstance().createListPopup(new IntentionListStep(quickFixes, intentions));
  }

  private void onMouseExit() {
    Window ancestor = SwingUtilities.getWindowAncestor(myPopup.getContent());
    if (ancestor == null) {
      myButton.setBackground(BACKGROUND_COLOR);
      myButton.setIcon(mySmartTagIcon);
      setBorder(INACTIVE_BORDER);
      myButton.setMargin(INACTIVE_MARGIN);
      updateComponentHintSize();
    }
  }

  private void onMouseEnter() {
    myButton.setBackground(HintUtil.QUESTION_COLOR);
    myButton.setIcon(myHighlightedIcon);
    setBorder(BorderFactory.createLineBorder(Color.black));
    myButton.setMargin(ACTIVE_MARGIN);
    updateComponentHintSize();

    String acceleratorsText = KeymapUtil.getFirstKeyboardShortcutText(
      ActionManager.getInstance().getAction(IdeActions.ACTION_SHOW_INTENTION_ACTIONS));
    if (acceleratorsText.length() > 0) {
      myButton.setToolTipText(CodeInsightBundle.message("lightbulb.tooltip", acceleratorsText));
    }
  }

  private void updateComponentHintSize() {
    Component component = myComponentHint.getComponent();
    component.setSize(getPreferredSize().width, getHeight());
  }

  public void closePopup() {
    if (myPopupShown) {
      myPopup.cancel();
      myPopupShown = false;
    }
  }

  private void showPopup() {
    if (isShowing()) {
      myPopup.show(RelativePoint.getSouthWestOf(this));
    }
    else {
      myPopup.showInBestPositionFor(myEditor);
    }

    myPopupShown = true;
  }

  private class MyComponentHint extends LightweightHint {
    private boolean myVisible = false;
    private boolean myShouldDelay;

    public MyComponentHint(JComponent component) {
      super(component);
    }

    public void show(final JComponent parentComponent, final int x, final int y, final JComponent focusBackComponent) {
      myVisible = true;
      if (myShouldDelay) {
        myAlarm.cancelAllRequests();
        myAlarm.addRequest(new Runnable() {
          public void run() {
            showImpl(parentComponent, x, y, focusBackComponent);
          }
        }, DELAY);
      }
      else {
        showImpl(parentComponent, x, y, focusBackComponent);
      }
    }

    private void showImpl(JComponent parentComponent, int x, int y, JComponent focusBackComponent) {
      if (!parentComponent.isShowing()) return;
      super.show(parentComponent, x, y, focusBackComponent);
    }

    public void hide() {
      myVisible = false;
      myAlarm.cancelAllRequests();
      super.hide();
    }

    public boolean isVisible() {
      return myVisible || super.isVisible();
    }

    public void setShouldDelay(boolean shouldDelay) {
      myShouldDelay = shouldDelay;
    }
  }

  private void applyAction(final IntentionAction action) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        HintManager hintManager = HintManager.getInstance();
        hintManager.hideAllHints();
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            final PsiFile file = PsiDocumentManager.getInstance(myProject).getPsiFile(myEditor.getDocument());
            PsiDocumentManager.getInstance(myProject).commitAllDocuments();

            if (action.isAvailable(myProject, myEditor, file)) {
              Runnable runnable = new Runnable() {
                public void run() {
                  try {
                    action.invoke(myProject, myEditor, file);
                  } catch (IncorrectOperationException e1) {
                    LOG.error(e1);
                  }
                  DaemonCodeAnalyzer.getInstance(myProject).updateVisibleHighlighters(myEditor);
                }
              };

              if (action.startInWriteAction()) {
                final Runnable _runnable = runnable;
                runnable = new Runnable() {
                  public void run() {
                    ApplicationManager.getApplication().runWriteAction(_runnable);
                  }
                };
              }

              CommandProcessor.getInstance().executeCommand(myProject, runnable, action.getText(), null);
            }
          }
        });
      }
    });
  }

  public static class EnableDisableIntentionAction implements IntentionAction{
    private String myActionFamilyName;
    private IntentionManagerSettings mySettings = IntentionManagerSettings.getInstance();

    public EnableDisableIntentionAction(IntentionAction action) {
      myActionFamilyName = action.getFamilyName();
      // needed for checking errors in user written actions
      //noinspection ConstantConditions
      LOG.assertTrue(myActionFamilyName != null, "action "+action.getClass()+" family returned null");
    }

    @NotNull
    public String getText() {
      return mySettings.isEnabled(myActionFamilyName) ?
             CodeInsightBundle.message("disable.intention.action", myActionFamilyName) :
             CodeInsightBundle.message("enable.intention.action", myActionFamilyName);
    }

    @NotNull
    public String getFamilyName() {
      return getText();
    }

    public boolean isAvailable(Project project, Editor editor, PsiFile file) {
      return true;
    }

    public void invoke(Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
      mySettings.setEnabled(myActionFamilyName, !mySettings.isEnabled(myActionFamilyName));
    }

    public boolean startInWriteAction() {
      return false;
    }
  }
}