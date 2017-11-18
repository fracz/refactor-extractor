package com.intellij.openapi.options.newEditor;

import com.intellij.CommonBundle;
import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableGroup;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Map;

public class OptionsEditorDialog extends DialogWrapper {

  private Project myProject;
  private ConfigurableGroup[] myGroups;
  private Configurable myPreselected;
  private OptionsEditor myEditor;

  private ApplyAction myApplyAction;
  public static final String DIMENSION_KEY = "OptionsEditor";

  public OptionsEditorDialog(Project project, ConfigurableGroup[] groups, Configurable preselectedConfigurable) {
    super(project, true);
    init(project, groups, preselectedConfigurable != null ? preselectedConfigurable : findLastSavedConfigurable(groups, project));
  }

  public OptionsEditorDialog(Project project, ConfigurableGroup[] groups, @NotNull String preselectedConfigurableDisplayName) {
    super(project, true);
    init(project, groups, getPreselectedByDisplayName(groups, preselectedConfigurableDisplayName, project));
  }

  private void init(final Project project, final ConfigurableGroup[] groups, final Configurable preselected) {
    myProject = project;
    myGroups = groups;
    myPreselected = preselected;

    setTitle("Settings");

    init();
  }

  private static Configurable getPreselectedByDisplayName(final ConfigurableGroup[] groups, final String preselectedConfigurableDisplayName,
                                                   final Project project) {
    Configurable result = findPreselectedByDisplyName(preselectedConfigurableDisplayName, groups);

    return result == null ? findLastSavedConfigurable(groups, project) : result;
  }

  protected JComponent createCenterPanel() {
    myEditor = new OptionsEditor(myProject, myGroups, myPreselected);
    myEditor.getContext().addColleague(new OptionsEditorColleague.Adapter() {
      @Override
      public ActionCallback onModifiedAdded(final Configurable configurable) {
        updateStatus();
        return ActionCallback.DONE;
      }

      @Override
      public ActionCallback onModifiedRemoved(final Configurable configurable) {
        updateStatus();
        return ActionCallback.DONE;
      }

      @Override
      public ActionCallback onErrorsChanged() {
        updateStatus();
        return ActionCallback.DONE;
      }
    });
    Disposer.register(myDisposable, myEditor);
    return myEditor;
  }

  public boolean updateStatus() {
    myApplyAction.setEnabled(myEditor.canApply());

    final Map<Configurable,ConfigurationException> errors = myEditor.getContext().getErrors();
    if (errors.size() == 0) {
      setErrorText(null);
    } else {
      setErrorText("Changes were not applied because of an error");
    }

    return errors.size() == 0;
  }

  @Override
  protected String getDimensionServiceKey() {
    return DIMENSION_KEY;
  }

  @Override
  protected void doOKAction() {
    myEditor.flushModifications();

    if (myEditor.canApply()) {
      myEditor.apply();
      if (!updateStatus()) return;
    }

    saveCurrentConfigurable();

    super.doOKAction();
  }


  private void saveCurrentConfigurable() {
    final Configurable current = myEditor.getContext().getCurrentConfigurable();
    if (current == null) return;

    final PropertiesComponent props = PropertiesComponent.getInstance(myProject);

    if (current instanceof SearchableConfigurable) {
      props.setValue(OptionsEditor.LAST_SELECTED_CONFIGURABLE, ((SearchableConfigurable)current).getId());
    } else {
      props.setValue(OptionsEditor.LAST_SELECTED_CONFIGURABLE, current.getClass().getName());
    }
  }

  @Nullable
  private static Configurable findLastSavedConfigurable(ConfigurableGroup[] groups, final Project project) {
    final String id = PropertiesComponent.getInstance(project).getValue(OptionsEditor.LAST_SELECTED_CONFIGURABLE);
    if (id == null) return null;

    final java.util.List<Configurable> all = SearchUtil.expand(groups);
    for (Configurable each : all) {
      if (each instanceof SearchableConfigurable) {
        if (id.equals(((SearchableConfigurable)each).getId())) return each;
      }
      if (id.equals(each.getClass().getName())) return each;
    }

    return null;
  }

  @Nullable
  private static Configurable findPreselectedByDisplyName(final String preselectedConfigurableDisplayName,ConfigurableGroup[] groups) {
    final java.util.List<Configurable> all = SearchUtil.expand(groups);
    for (Configurable each : all) {
      if (preselectedConfigurableDisplayName.equals(each.getDisplayName())) return each;
    }

    return null;

  }



  @Override
  public void doCancelAction(final AWTEvent source) {
    if (source instanceof KeyEvent || source instanceof ActionEvent) {
      if (myEditor.isFilterFieldVisible() && myEditor.isSearchFieldFocused()) {
        if (myEditor.getContext().isHoldingFilter()) {
          myEditor.clearFilter();
        } else {
          myEditor.setFilterFieldVisible(false, false, false);
        }
        return;
      }
      if (myEditor.getContext().isHoldingFilter()) {
        myEditor.clearFilter();
        myEditor.setFilterFieldVisible(false, false, false);
        return;
      }
    }

    super.doCancelAction(source);
  }

  @Override
  public void doCancelAction() {
    saveCurrentConfigurable();
    super.doCancelAction();
  }

  @Override
  protected Action[] createActions() {
    myApplyAction = new ApplyAction();
    return new Action[] {getOKAction(), getCancelAction(), myApplyAction, getHelpAction()};
  }

  @Override
  protected void doHelpAction() {
    final String topic = myEditor.getHelpTopic();
    if (topic != null) {
      HelpManager.getInstance().invokeHelp(topic);
    }
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myEditor.getPreferredFocusedComponent();
  }

  private class ApplyAction extends AbstractAction {
    public ApplyAction() {
      super(CommonBundle.getApplyButtonText());
      setEnabled(false);
    }

    public void actionPerformed(final ActionEvent e) {
      myEditor.apply();
    }
  }

}