package com.intellij.application.options.codeStyle;

import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.psi.codeStyle.CodeStyleScheme;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CodeStyleMainPanel extends JPanel {
  private CardLayout myLayout = new CardLayout();
  private final JPanel mySettingsPanel = new JPanel(myLayout);

  private final Map<String, NewCodeStyleSettingsPanel> mySettingsPanels = new HashMap<String, NewCodeStyleSettingsPanel>();
  private final Collection<NewCodeStyleSettingsPanel> myResetPanels = new HashSet<NewCodeStyleSettingsPanel>();

  private Alarm myAlarm = new Alarm(Alarm.ThreadToUse.SHARED_THREAD);
  private final CodeStyleSchemesModel myModel;
  private final CodeStyleSettingsPanelFactory myFactory;

  @NonNls
  private static final String WAIT_CARD = "CodeStyleSchemesConfigurable.$$$.Wait.placeholder.$$$";


  public CodeStyleMainPanel(CodeStyleSchemesModel model, CodeStyleSettingsPanelFactory factory) {
    super(new BorderLayout());
    myModel = model;
    myFactory = factory;

    final CodeStyleSchemesPanel schemesPanel = new CodeStyleSchemesPanel(model);

    model.addListener(new CodeStyleSettingsListener(){
      public void currentSchemeChanged(final Object source) {
        if (source != schemesPanel) {
          schemesPanel.onSelectedSchemeChanged();
        }
        onCurrentSchemeChanged();
      }

      public void schemeListChanged() {
        schemesPanel.resetSchemesCombo();
      }

      public void currentSettingsChanged() {
        ensureCurrentPanel().onSomethingChanged();
      }

      public void usePerProjectSettingsOptionChanged() {
        schemesPanel.usePerProjectSettingsOptionChanged();
      }

      public void schemeChanged(final CodeStyleScheme scheme) {
        ensurePanel(scheme).resetFromClone();
      }
    });

    addWaitCard();

    add(schemesPanel.getPanel(), BorderLayout.NORTH);
    add(mySettingsPanel, BorderLayout.CENTER);

    schemesPanel.resetSchemesCombo();
    schemesPanel.onSelectedSchemeChanged();
    onCurrentSchemeChanged();

  }

  private void addWaitCard() {
    JPanel waitPanel = new JPanel(new BorderLayout());
    JLabel label = new JLabel(ApplicationBundle.message("label.loading.page.please.wait"));
    label.setHorizontalAlignment(SwingConstants.CENTER);
    waitPanel.add(label, BorderLayout.CENTER);
    label.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    waitPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    mySettingsPanel.add(WAIT_CARD, waitPanel);
  }

  public void onCurrentSchemeChanged() {
    myLayout.show(mySettingsPanel, WAIT_CARD);

    myAlarm.cancelAllRequests();
    final Runnable request = new Runnable() {
      public void run() {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            ensureCurrentPanel().onSomethingChanged();
            myLayout.show(mySettingsPanel, myModel.getSelectedScheme().getName());
          }
        });
      }
    };
    myAlarm.addRequest(request, 200);
  }

  public NewCodeStyleSettingsPanel[] getPanels() {
    final Collection<NewCodeStyleSettingsPanel> panels = mySettingsPanels.values();
    return panels.toArray(new NewCodeStyleSettingsPanel[panels.size()]);
  }

  public boolean isModified() {
    final NewCodeStyleSettingsPanel[] panels = getPanels();
    for (int i = 0; i < panels.length; i++) {
      NewCodeStyleSettingsPanel panel = panels[i];
      if (panel.isModified()) return true;
    }
    return false;
  }

  public void reset() {
    clearPanels();
    onCurrentSchemeChanged();
  }

  private void clearPanels() {
    mySettingsPanels.clear();
    myResetPanels.clear();
  }

  public void apply() {
    final NewCodeStyleSettingsPanel[] panels = getPanels();
    for (int i = 0; i < panels.length; i++) {
      NewCodeStyleSettingsPanel panel = panels[i];
      if (panel.isModified()) panel.apply();
    }
  }

  @NonNls
  public String getHelpTopic() {
    NewCodeStyleSettingsPanel selectedPanel = ensureCurrentPanel();
    if (selectedPanel == null) {
      return "reference.settingsdialog.IDE.globalcodestyle";
    }
    return selectedPanel.getHelpTopic();
  }

  private NewCodeStyleSettingsPanel ensureCurrentPanel() {

    return ensurePanel(myModel.getSelectedScheme());
  }

  private NewCodeStyleSettingsPanel ensurePanel(final CodeStyleScheme scheme) {
    String name = scheme.getName();
    if (!mySettingsPanels.containsKey(name)) {
      NewCodeStyleSettingsPanel panel = myFactory.createPanel(scheme);
      panel.reset();
      panel.setModel(myModel);
      mySettingsPanels.put(name, panel);
      mySettingsPanel.add(scheme.getName(), panel);
    }

    return mySettingsPanels.get(name);
  }

  public String getDisplayName() {
    return ensureCurrentPanel().getDisplayName();
  }

  public void disposeUIResources() {
    myAlarm.cancelAllRequests();
    for (NewCodeStyleSettingsPanel panel : mySettingsPanels.values()) {
      panel.dispose();
    }
    clearPanels();
  }

  public boolean isModified(final CodeStyleScheme scheme) {
    if (!mySettingsPanels.containsKey(scheme.getName())) {
      return false;
    }

    return mySettingsPanels.get(scheme.getName()).isModified();
  }
}