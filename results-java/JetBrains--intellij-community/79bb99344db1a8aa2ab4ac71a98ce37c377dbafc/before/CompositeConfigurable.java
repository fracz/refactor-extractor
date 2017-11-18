package org.jetbrains.idea.maven.runner;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CompositeConfigurable implements Configurable {
  private final List<Configurable> configurables = new ArrayList<Configurable>();
  private JTabbedPane tabbedPane;
  private int selectedTabIndex = 0;

  public CompositeConfigurable (Configurable ... configurables){
    for (Configurable configurable : configurables) {
      registerConfigurable(configurable);
    }
  }

  public void registerConfigurable(Configurable configurable) {
    configurables.add(configurable);
  }

  public JComponent createComponent() {
    tabbedPane = new JTabbedPane();
    for (Configurable configurable : configurables) {
      JComponent component = configurable.createComponent();
      component.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
      tabbedPane.add(configurable.getDisplayName(), component);
    }
    return tabbedPane;
  }

  public boolean isModified() {
    for (Configurable configurable : configurables) {
      if (configurable.isModified()) return true;
    }
    return false;
  }

  public void apply() throws ConfigurationException {
    for (Configurable configurable : configurables) {
      configurable.apply();
    }
    selectedTabIndex = tabbedPane.getSelectedIndex();
  }

  public void reset() {
    for (Configurable configurable : configurables) {
      configurable.reset();
    }
    tabbedPane.setSelectedIndex(selectedTabIndex);
  }

  public void disposeUIResources() {
    for (Configurable configurable : configurables) {
      configurable.disposeUIResources();
    }
    tabbedPane = null;
  }

  @Nullable
  @NonNls
  public String getHelpTopic() {
    return selectedTabIndex < configurables.size() ? configurables.get(selectedTabIndex).getHelpTopic() : null;
  }

  @Nls
  public String getDisplayName() {
    return null;
  }

  @Nullable
  public Icon getIcon() {
    return null;
  }
}