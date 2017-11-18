package com.intellij.openapi.options.ex;

import com.intellij.CommonBundle;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.ide.ui.search.SearchableOptionsRegistrar;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionButtonLook;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.options.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ex.ActionToolbarEx;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HorizontalLabeledIcon;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.HashMap;
import com.intellij.util.ui.EmptyIcon;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Set;

public class ExplorerSettingsEditor extends DialogWrapper {
  /** When you visit the same editor next time you see the same selected configurable. */
  private static final TObjectIntHashMap<String> ourGroup2LastConfigurableIndex = new TObjectIntHashMap<String>();
  private static String ourLastGroup;

  private final Project myProject;
  private Configurable myKeySelectedConfigurable;
  private int myKeySelectedConfigurableIndex;

  private final ConfigurableGroup[] myGroups;

  /** Configurable which is currently selected. */
  private Configurable mySelectedConfigurable;
  private ConfigurableGroup mySelectedGroup;
  private JPanel myOptionsPanel;

  private final Map<Configurable, JComponent> myInitializedConfigurables2Component;
  private final Dimension myPreferredSize;
  private final Map<Configurable, Dimension> myConfigurable2PrefSize;
  private JButton myHelpButton;
  private JPanel myComponentPanel;
  private JTextField mySearchField = new JTextField();
  private Set<Configurable> myOptionContainers = null;
  private JPanel myLeftPane;

  public ExplorerSettingsEditor(Project project, ConfigurableGroup[] group) {
    super(project, true);
    myProject = project;
    myPreferredSize = new Dimension(800, 600);
    myGroups = group;

    if (myGroups.length == 0) {
      throw new IllegalStateException("number of configurables must be more then zero");
    }

    myInitializedConfigurables2Component = new HashMap<Configurable, JComponent>();
    myConfigurable2PrefSize = new HashMap<Configurable, Dimension>();

    init();
  }

  protected String getDimensionServiceKey() {
    return "#com.intellij.openapi.options.ex.ExplorerSettingsEditor";
  }

  protected final void init() {
    super.init();

    int lastGroup = 0;
    for (int i = 0; i < myGroups.length; i++) {
      ConfigurableGroup group = myGroups[i];
      if (Comparing.equal(group.getShortName(), ourLastGroup)) {
        lastGroup = i;
        break;
      }
    }

    selectGroup(lastGroup);
  }

  private void selectGroup(int groupIdx) {
    final String shortName = myGroups[groupIdx].getShortName();
    int lastIndex = ourGroup2LastConfigurableIndex.get(shortName);
    if (lastIndex == -1) lastIndex = 0;
    selectGroup(groupIdx,lastIndex);
  }
  private void selectGroup(int groupIdx, int indexToSelect) {
    rememberLastUsedPage();

    mySelectedGroup = myGroups[groupIdx];
    ourLastGroup = mySelectedGroup.getShortName();
    Configurable[] configurables = mySelectedGroup.getConfigurables();
    Configurable toSelect = configurables[indexToSelect];

    selectConfigurable(toSelect, indexToSelect);

    requestFocusForMainPanel();
  }

  private void rememberLastUsedPage() {
    if (mySelectedGroup != null) {
      Configurable[] configurables = mySelectedGroup.getConfigurables();
      int index = -1;
      for (int i = 0; i < configurables.length; i++) {
        Configurable configurable = configurables[i];
        if (configurable == mySelectedConfigurable) {
          index = i;
          break;
        }
      }
      ourGroup2LastConfigurableIndex.put(mySelectedGroup.getShortName(), index);
    }
  }

  private void updateTitle() {
    if (mySelectedConfigurable == null) {
      setTitle(OptionsBundle.message("settings.panel.title"));
    }
    else {
      String displayName = mySelectedConfigurable.getDisplayName();
      setTitle(mySelectedGroup.getDisplayName() + " - " + (displayName != null ? displayName.replace('\n', ' ') : ""));
      myHelpButton.setEnabled(mySelectedConfigurable.getHelpTopic() != null);
    }
  }

  /**
   * @return false if failed
   */
  protected boolean apply() {
    if (mySelectedConfigurable == null || !mySelectedConfigurable.isModified()) {
      return true;
    }

    try {
      mySelectedConfigurable.apply();
      return true;
    }
    catch (ConfigurationException e) {
      if (e.getMessage() != null) {
        Messages.showMessageDialog(e.getMessage(), e.getTitle(), Messages.getErrorIcon());
      }
      return false;
    }
  }

  protected final void dispose() {
    rememberLastUsedPage();

    for (ConfigurableGroup myGroup : myGroups) {
      Configurable[] configurables = myGroup.getConfigurables();
      for (Configurable configurable : configurables) {
        configurable.disposeUIResources();
      }
    }
    myInitializedConfigurables2Component.clear();
    super.dispose();
  }

  public JComponent getPreferredFocusedComponent() {
    return myComponentPanel;
  }

  protected final JComponent createCenterPanel() {
    myComponentPanel = new JPanel(new BorderLayout());

    // myOptionPanel contains all configurables. When it updates its UI we also need to update
    // UIs of all created but not currently visible configurables.

    myOptionsPanel = new JPanel(new BorderLayout()) {
      public void updateUI() {
        super.updateUI();
        for (Configurable configurable : myInitializedConfigurables2Component.keySet()) {
          if (configurable.equals(mySelectedConfigurable)) { // don't update visible component (optimization)
            continue;
          }
          JComponent component = myInitializedConfigurables2Component.get(configurable);
          SwingUtilities.updateComponentTreeUI(component);
        }
      }
    };

    myLeftPane = new JPanel(new BorderLayout());
    initToolbar();
    myLeftPane.setBorder(BorderFactory.createRaisedBevelBorder());
    myComponentPanel.add(myLeftPane, BorderLayout.WEST);

    myOptionsPanel.setBorder(BorderFactory.createEmptyBorder(15, 5, 2, 5));
    myComponentPanel.add(myOptionsPanel, BorderLayout.CENTER);

    myOptionsPanel.setPreferredSize(myPreferredSize);

    myComponentPanel.setFocusable(true);
    myComponentPanel.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        Configurable[] configurables = mySelectedGroup.getConfigurables();
        int index = myKeySelectedConfigurableIndex;
        if (index == -1) return;
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP) {
          index--;
        }
        else if (keyCode == KeyEvent.VK_DOWN) {
          index++;
        }
        else {
          Configurable configurableFromMnemonic = ControlPanelMnemonicsUtil.getConfigurableFromMnemonic(e, myGroups);
          if (configurableFromMnemonic == null) return;
          int keyGroupIndex = -1;
          ConfigurableGroup keyGroup = null;
          int keyIndexInGroup = 0;
          for (int i = 0; i < myGroups.length; i++) {
            ConfigurableGroup group = myGroups[i];
            int ingroupIdx = ArrayUtil.find(group.getConfigurables(), configurableFromMnemonic);
            if (ingroupIdx != -1) {
              keyGroupIndex = i;
              keyGroup = group;
              keyIndexInGroup = ingroupIdx;
              break;
            }
          }
          if (mySelectedGroup != keyGroup) {
            selectGroup(keyGroupIndex, keyIndexInGroup);
            return;
          }
          index = ControlPanelMnemonicsUtil.getIndexFromKeycode(keyCode, mySelectedGroup == myGroups[0]);
        }
        if (index == -1 || index == configurables.length) return;
        selectConfigurableLater(configurables[index], index);
      }
    });
    return myComponentPanel;
  }

  protected JComponent createNorthPanel() {
    final JPanel panel = new JPanel(new GridBagLayout());
    mySearchField.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(DocumentEvent e) {
        final SearchableOptionsRegistrar optionsRegistrar = SearchableOptionsRegistrar.getInstance();
        final @NonNls String searchPattern = mySearchField.getText();
        if (searchPattern != null && searchPattern.length() > 0) {
          final String[] searchOptions = searchPattern.split("[\\W&&[^_-]]");
          Set<Configurable> configurables = null;
          for (String option : searchOptions) {
            if (option != null && option.length() > 0){
              final Set<Configurable> optionConfigurables = optionsRegistrar.getConfigurables(myGroups, option);
              if (configurables == null){
                configurables = optionConfigurables;
              } else {
                configurables.retainAll(optionConfigurables);
              }
            }
          }
          myOptionContainers = configurables;
        } else {
          myOptionContainers = null;
        }
        initToolbar();
        if (mySelectedConfigurable instanceof SearchableConfigurable){
          selectOption((SearchableConfigurable)mySelectedConfigurable);
        }
        myComponentPanel.revalidate();
        myComponentPanel.repaint();
      }
    });
    final GridBagConstraints gc = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
    panel.add(Box.createHorizontalBox(), gc);

    gc.gridx++;
    gc.weightx = 0;
    gc.fill = GridBagConstraints.NONE;
    panel.add(new JLabel(IdeBundle.message("search.textfield.title")), gc);

    gc.gridx++;
    final int height = mySearchField.getPreferredSize().height;
    mySearchField.setPreferredSize(new Dimension(100, height));
    panel.add(mySearchField, gc);

    return panel;
  }

  private void requestFocusForMainPanel() {
    myComponentPanel.requestFocus();
  }

  private void initToolbar() {
    final DefaultActionGroup actionGroup = new DefaultActionGroup();
    char mnemonicStartChar = '1';
    for (ConfigurableGroup group : myGroups) {
      boolean firstActionInGroup = true;
      final Configurable[] configurables = group.getConfigurables();
      for (int i = 0; i < configurables.length; i++) {
        Configurable configurable = configurables[i];
        if (myOptionContainers == null || myOptionContainers.contains(configurable)){
          if (firstActionInGroup){
            actionGroup.add(new MyHorizontalSeparator(group));
            firstActionInGroup = false;
          }
          actionGroup.add(new MySelectConfigurableAction(configurable, i, (char)(mnemonicStartChar + i)));
        }
      }
      mnemonicStartChar = 'A';
    }

    final ActionToolbarEx toolbar = (ActionToolbarEx)ActionManager.getInstance().createActionToolbar(
      ActionPlaces.UNKNOWN,
      actionGroup,
      false);

    toolbar.adjustTheSameSize(true);

    toolbar.setButtonLook(new LeftAlignedIconButtonLook());

    JPanel toolbarPanel = new JPanel(new BorderLayout(2, 0));
    toolbarPanel.add(toolbar.getComponent(), BorderLayout.CENTER);
    final JScrollPane scrollPane =
      new JScrollPane(toolbarPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
        public Dimension getPreferredSize() {
          return actionGroup.getChildrenCount() > 2 ? new Dimension(toolbar.getComponent().getPreferredSize().width + getVerticalScrollBar().getPreferredSize().width + 5, 5) : new Dimension(150, -1);
        }

        public Dimension getMinimumSize() {
          return getPreferredSize();
        }
      };
    myLeftPane.removeAll();
    scrollPane.getVerticalScrollBar().setUnitIncrement(toolbar.getMaxButtonHeight());
    myLeftPane.add(scrollPane, BorderLayout.CENTER);
  }

  private final Alarm myAlarm = new Alarm();
  private void selectConfigurableLater(final Configurable configurable, final int index) {
    myAlarm.cancelAllRequests();
    myAlarm.addRequest(new Runnable() {
      public void run() {
        selectConfigurable(configurable, index);
      }
    }, 400);
    myKeySelectedConfigurable = configurable;
    myKeySelectedConfigurableIndex = index;

    myComponentPanel.repaint();
  }

  /**
   * Selects configurable with specified <code>class</code>. If there is no configurable of <code>class</code>
   * then the method does nothing.
   */
  private void selectConfigurable(Configurable configurable, int index) {
    // If nothing to be selected then clear panel with configurable's options.
    if (configurable == null) {
      mySelectedConfigurable = null;
      myKeySelectedConfigurableIndex = 0;
      myKeySelectedConfigurable = null;
      updateTitle();
      myOptionsPanel.removeAll();
      validate();
      repaint();
      return;
    }

    // Save changes if any
    Dimension currentOptionsSize = myOptionsPanel.getSize();

    if (mySelectedConfigurable != null && mySelectedConfigurable.isModified()) {
      int exitCode = Messages.showYesNoDialog(OptionsBundle.message("options.page.modified.save.message.text"),
                                              OptionsBundle.message("options.save.changes.message.title"),
                                              Messages.getQuestionIcon());
      if (exitCode == 0) {
        try {
          mySelectedConfigurable.apply();
        }
        catch (ConfigurationException exc) {
          if (exc.getMessage() != null) {
            Messages.showMessageDialog(exc.getMessage(), exc.getTitle(), Messages.getErrorIcon());
          }
          return;
        }
      }
    }

    if (mySelectedConfigurable != null) {
      Dimension savedPrefferedSize = myConfigurable2PrefSize.get(mySelectedConfigurable);
      if (savedPrefferedSize != null) {
        myConfigurable2PrefSize.put(mySelectedConfigurable, new Dimension(currentOptionsSize));
      }
    }

    // Show new configurable
    myComponentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    myOptionsPanel.removeAll();

    mySelectedConfigurable = configurable;
    myKeySelectedConfigurable = configurable;
    myKeySelectedConfigurableIndex = index;
    JComponent component = myInitializedConfigurables2Component.get(configurable);
    if (component == null) {
      component = configurable.createComponent();
      myInitializedConfigurables2Component.put(configurable, component);
    }

    Dimension compPrefSize;
    if (myConfigurable2PrefSize.containsKey(configurable)) {
      compPrefSize = myConfigurable2PrefSize.get(configurable);
    }
    else {
      compPrefSize = component.getPreferredSize();
      myConfigurable2PrefSize.put(configurable, compPrefSize);
    }
    int widthDelta = Math.max(compPrefSize.width - currentOptionsSize.width, 0);
    int heightDelta = Math.max(compPrefSize.height - currentOptionsSize.height, 0);
    myOptionsPanel.add(component, BorderLayout.CENTER);
    if (widthDelta > 0 || heightDelta > 0) {
      setSize(getSize().width + widthDelta, getSize().height + heightDelta);
      centerRelativeToParent();
    }

    configurable.reset();

    updateTitle();
    validate();
    repaint();

    requestFocusForMainPanel();
    myComponentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    if (configurable instanceof SearchableConfigurable){
      selectOption((SearchableConfigurable)configurable);
    }
  }

  private void selectOption(final SearchableConfigurable searchableConfigurable) {
    searchableConfigurable.clearSearch();
    if (myOptionContainers == null || myOptionContainers.isEmpty()) return; //do not highlight current editor when nothing can be selected
    @NonNls final String filter = mySearchField.getText();
    if (filter != null && filter.length() > 0 ){
      final String[] options = filter.split("[\\W]");
      for (String option : options) {
        final Runnable runnable = searchableConfigurable.showOption(option);
        if (runnable != null){
          runnable.run();
        }
      }
    }
  }

  protected final Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction(), new ApplyAction(), getHelpAction()};
  }

  protected JButton createJButtonForAction(Action action) {
    JButton button = super.createJButtonForAction(action);
    if (action == getHelpAction()) {
      myHelpButton = button;
    }
    return button;
  }

  protected Action[] createLeftSideActions() {
    return new Action[]{new SwitchToDefaultViewAction()};
  }

  protected final void doOKAction() {
    boolean ok = apply();
    if (ok) {
      super.doOKAction();
    }
  }

  protected final void doHelpAction() {
    if (mySelectedConfigurable != null) {
      String helpTopic = mySelectedConfigurable.getHelpTopic();
      if (helpTopic != null) {
        HelpManager.getInstance().invokeHelp(helpTopic);
      }
    }
  }

  private final class ApplyAction extends AbstractAction {
    private Alarm myUpdateAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

    public ApplyAction() {
      super(OptionsBundle.message("options.apply.button"));
      final Runnable updateRequest = new Runnable() {
        public void run() {
          if (!ExplorerSettingsEditor.this.isShowing()) return;
          ApplyAction.this.setEnabled(mySelectedConfigurable != null && mySelectedConfigurable.isModified());
          addUpdateRequest(this);
        }
      };

      addUpdateRequest(updateRequest);
    }

    private void addUpdateRequest(final Runnable updateRequest) {
      myUpdateAlarm.addRequest(updateRequest, 500, ModalityState.stateForComponent(getWindow()));
    }

    public void actionPerformed(ActionEvent e) {
      if (apply()) {
        setCancelButtonText(CommonBundle.getCloseButtonText());
      }
    }
  }

  private final class MySelectConfigurableAction extends ToggleAction {
    private final Configurable myConfigurable;
    private final int myIndex;

    private MySelectConfigurableAction(Configurable configurable, int index, char mnemonic) {
      myConfigurable = configurable;
      myIndex = index;
      Presentation presentation = getTemplatePresentation();
      String displayName = myConfigurable.getDisplayName();
      Icon icon = myConfigurable.getIcon();
      if (icon == null) {
        icon = IconLoader.getIcon("/general/configurableDefault.png");
      }
      Icon labeledIcon = new HorizontalLabeledIcon(icon, displayName, " ("+mnemonic+")");
      presentation.setIcon(labeledIcon);
      presentation.setText(null);
    }

    public boolean isSelected(AnActionEvent e) {
      return myConfigurable.equals(myKeySelectedConfigurable);
    }

    public void setSelected(AnActionEvent e, boolean state) {
      if (state) {
        selectConfigurableLater(myConfigurable, myIndex);
      }
    }
  }

  private class SwitchToDefaultViewAction extends AbstractAction {
    public SwitchToDefaultViewAction() {
      putValue(Action.NAME, OptionsBundle.message("explorer.panel.default.view.button"));
    }

    public void actionPerformed(ActionEvent e) {
      switchToDefaultView(null);
    }
  }
  private void switchToDefaultView(final Configurable preselectedConfigurable) {
    close(OK_EXIT_CODE);

    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        ((ShowSettingsUtilImpl)ShowSettingsUtil.getInstance()).showControlPanelOptions(myProject, myGroups, preselectedConfigurable);
      }
    }, ModalityState.NON_MMODAL);
  }

  private static class LeftAlignedIconButtonLook extends ActionButtonLook {
    private ActionButtonLook myDelegate = ActionButtonLook.IDEA_LOOK;

    public void paintIconAt(Graphics g, ActionButtonComponent button, Icon icon, int x, int y) {
      myDelegate.paintIconAt(g, button, icon, x, y);
    }

    public void paintBorder(Graphics g, JComponent component, int state) {
      myDelegate.paintBorder(g, component, state);
    }

    public void paintIcon(Graphics g, ActionButtonComponent actionButton, Icon icon) {
      int height = icon.getIconHeight();
      int x = 2;
      int y = (int)Math.ceil((actionButton.getHeight() - height) / 2);
      paintIconAt(g, actionButton, icon, x, y);
    }

    public void paintBackground(Graphics g, JComponent component, int state) {
      myDelegate.paintBackground(g, component, state);
    }
  }

  private static class MyHorizontalSeparator extends AnAction {

    public MyHorizontalSeparator(ConfigurableGroup group) {
     Presentation presentation = getTemplatePresentation();
      String displayName = group.getDisplayName();
      Icon labeledIcon = new HorizontalLabeledIcon(new EmptyIcon(16,16), displayName, "");
      presentation.setIcon(labeledIcon);
    }

    public void update(AnActionEvent e) {
      e.getPresentation().setEnabled(false);
    }

    public void actionPerformed(AnActionEvent e) {
      //do nothing
    }
  }
}