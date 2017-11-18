/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.openapi.vcs.changes.ui;

import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vcs.versionBrowser.ChangesBrowserSettingsEditor;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vcs.versionBrowser.DateFilterComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author yole
 */
public class CompositeCommittedChangesProvider implements CommittedChangesProvider<CommittedChangeList, CompositeCommittedChangesProvider.CompositeChangeBrowserSettings> {
  private List<AbstractVcs> myBaseVcss = new ArrayList<AbstractVcs>();

  public CompositeCommittedChangesProvider(final AbstractVcs... baseVcss) {
    myBaseVcss = new ArrayList<AbstractVcs>();
    Collections.addAll(myBaseVcss, baseVcss);
  }

  public CompositeCommittedChangesProvider.CompositeChangeBrowserSettings createDefaultSettings() {
    Map<AbstractVcs, ChangeBrowserSettings> map = new HashMap<AbstractVcs, ChangeBrowserSettings>();
    for(AbstractVcs vcs: myBaseVcss) {
      final CommittedChangesProvider provider = vcs.getCommittedChangesProvider();
      assert provider != null;
      map.put(vcs, provider.createDefaultSettings());
    }
    return new CompositeChangeBrowserSettings(map);
  }

  public ChangesBrowserSettingsEditor<CompositeCommittedChangesProvider.CompositeChangeBrowserSettings> createFilterUI(final boolean showDateFilter) {
    return new CompositeChangesBrowserSettingsEditor();
  }

  public List<CommittedChangeList> getAllCommittedChanges(CompositeCommittedChangesProvider.CompositeChangeBrowserSettings settings, final int maxCount) throws VcsException {
    LinkedHashSet<CommittedChangeList> result = new LinkedHashSet<CommittedChangeList>();
    for(AbstractVcs vcs: settings.getEnabledVcss()) {
      CommittedChangesProvider provider = vcs.getCommittedChangesProvider();
      assert provider != null;
      //noinspection unchecked
      result.addAll(provider.getAllCommittedChanges(settings.get(vcs), maxCount));
    }
    return new ArrayList<CommittedChangeList>(result);
  }

  public List<CommittedChangeList> getCommittedChanges(CompositeCommittedChangesProvider.CompositeChangeBrowserSettings settings,
                                                       VirtualFile root, final int maxCount) throws VcsException {
    throw new UnsupportedOperationException();
  }

  public ChangeListColumn[] getColumns() {
    Set<ChangeListColumn> columns = new LinkedHashSet<ChangeListColumn>();
    for(AbstractVcs vcs: myBaseVcss) {
      final CommittedChangesProvider provider = vcs.getCommittedChangesProvider();
      assert provider != null;
      ChangeListColumn[] providerColumns = provider.getColumns();
      for(ChangeListColumn col: providerColumns) {
        if (col == ChangeListColumn.DATE || col == ChangeListColumn.DESCRIPTION || col == ChangeListColumn.NAME ||
            col instanceof ChangeListColumn.ChangeListNumberColumn) {
          columns.add(col);
        }
      }
    }
    return columns.toArray(new ChangeListColumn[columns.size()]);
  }

  public static class CompositeChangeBrowserSettings extends ChangeBrowserSettings {
    private final Map<AbstractVcs, ChangeBrowserSettings> myMap;
    private final Set<AbstractVcs> myEnabledVcs = new HashSet<AbstractVcs>();

    public CompositeChangeBrowserSettings(final Map<AbstractVcs, ChangeBrowserSettings> map) {
      myMap = map;
      myEnabledVcs.addAll(map.keySet());
    }

    public void put(final AbstractVcs vcs, final ChangeBrowserSettings settings) {
      myMap.put(vcs, settings);
    }

    public ChangeBrowserSettings get(final AbstractVcs vcs) {
      return myMap.get(vcs);
    }

    public void setEnabledVcss(Collection<AbstractVcs> vcss) {
      myEnabledVcs.clear();
      myEnabledVcs.addAll(vcss);
    }

    public Collection<AbstractVcs> getEnabledVcss() {
      return myEnabledVcs;
    }
  }

  private class CompositeChangesBrowserSettingsEditor implements ChangesBrowserSettingsEditor<CompositeChangeBrowserSettings> {
    private JPanel myCompositePanel;
    private DateFilterComponent myDateFilter;
    private CompositeChangeBrowserSettings mySettings;
    private Map<AbstractVcs, ChangesBrowserSettingsEditor> myEditors = new HashMap<AbstractVcs, ChangesBrowserSettingsEditor>();
    private Map<AbstractVcs, JCheckBox> myEnabledCheckboxes = new HashMap<AbstractVcs, JCheckBox>();

    public CompositeChangesBrowserSettingsEditor() {
      myCompositePanel = new JPanel();
      myCompositePanel.setLayout(new BoxLayout(myCompositePanel, BoxLayout.Y_AXIS));
      myDateFilter = new DateFilterComponent();
      myCompositePanel.add(myDateFilter.getPanel());
      for(AbstractVcs vcs: myBaseVcss) {
        final CommittedChangesProvider provider = vcs.getCommittedChangesProvider();
        assert provider != null;
        final ChangesBrowserSettingsEditor editor = provider.createFilterUI(false);
        myEditors.put(vcs, editor);

        JPanel wrapperPane = new JPanel(new BorderLayout());
        wrapperPane.setBorder(BorderFactory.createTitledBorder(vcs.getDisplayName()));
        final JCheckBox checkBox = new JCheckBox(VcsBundle.message("composite.change.provider.include.vcs.checkbox", vcs.getDisplayName()), true);
        checkBox.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
            updateVcsEnabled(checkBox, editor);
          }
        });
        wrapperPane.add(checkBox, BorderLayout.NORTH);
        myEnabledCheckboxes.put(vcs, checkBox);
        wrapperPane.add(editor.getComponent(), BorderLayout.CENTER);
        myCompositePanel.add(wrapperPane);
      }
    }

    private void updateVcsEnabled(JCheckBox checkBox, ChangesBrowserSettingsEditor editor) {
      UIUtil.setEnabled(editor.getComponent(), checkBox.isSelected(), true);
      if (checkBox.isSelected()) {
        editor.updateEnabledControls();
      }
    }

    public JComponent getComponent() {
      return myCompositePanel;
    }

    public CompositeChangeBrowserSettings getSettings() {
      Set<AbstractVcs> enabledVcss = new HashSet<AbstractVcs>();
      for(AbstractVcs vcs: myEditors.keySet()) {
        ChangeBrowserSettings settings = myEditors.get(vcs).getSettings();
        myDateFilter.saveValues(settings);
        mySettings.put(vcs, settings);
        if (myEnabledCheckboxes.get(vcs).isSelected()) {
          enabledVcss.add(vcs);
        }
      }
      mySettings.setEnabledVcss(enabledVcss);
      return mySettings;
    }

    public void setSettings(CompositeChangeBrowserSettings settings) {
      mySettings = settings;
      boolean dateFilterInitialized = false;
      for(AbstractVcs vcs: myEditors.keySet()) {
        final ChangeBrowserSettings vcsSettings = mySettings.get(vcs);
        final ChangesBrowserSettingsEditor editor = myEditors.get(vcs);
        //noinspection unchecked
        editor.setSettings(vcsSettings);
        if (!dateFilterInitialized) {
          myDateFilter.initValues(vcsSettings);
          dateFilterInitialized = true;
        }
        final JCheckBox checkBox = myEnabledCheckboxes.get(vcs);
        checkBox.setSelected(settings.getEnabledVcss().contains(vcs));
        updateVcsEnabled(checkBox, editor);
      }
    }

    @Nullable
    public String validateInput() {
      for(ChangesBrowserSettingsEditor editor: myEditors.values()) {
        String result = editor.validateInput();
        if (result != null) return result;
      }
      return null;
    }

    public void updateEnabledControls() {
      for(ChangesBrowserSettingsEditor editor: myEditors.values()) {
        editor.updateEnabledControls();
      }
    }

    public String getDimensionServiceKey() {
      @NonNls StringBuilder result = new StringBuilder();
      result.append("Composite");
      for(AbstractVcs vcs: myBaseVcss) {
        result.append(".").append(vcs.getDisplayName());
      }
      return result.toString();
    }
  }
}