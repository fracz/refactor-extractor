package com.intellij.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.keymap.impl.KeymapImpl;
import com.intellij.openapi.options.SharedScheme;
import com.intellij.openapi.project.Project;

import java.util.Collection;

/**
 * @author max
 */
public class QuickChangeKeymapAction extends QuickSwitchSchemeAction {
  protected void fillActions(Project project, DefaultActionGroup group) {
    final KeymapManagerEx manager = (KeymapManagerEx) KeymapManager.getInstance();
    final Keymap current = manager.getActiveKeymap();

    for (final Keymap keymap : manager.getAllKeymaps()) {
      addKeymapAction(group, manager, current, keymap, false);
    }

    Collection<SharedScheme<KeymapImpl>> sharedSchemes = ((KeymapManagerEx)KeymapManagerEx.getInstance()).getSchemesManager().loadSharedSchemes();

    if (!sharedSchemes.isEmpty()) {
      group.add(Separator.getInstance());
      for (SharedScheme<KeymapImpl> sharedScheme : sharedSchemes) {
        addKeymapAction(group, manager,current, sharedScheme.getScheme(), true);
      }
    }

  }

  private void addKeymapAction(final DefaultActionGroup group, final KeymapManagerEx manager, final Keymap current, final Keymap keymap, final boolean addScheme) {
    group.add(new AnAction(keymap.getPresentableName(), "", keymap == current ? ourCurrentAction : ourNotCurrentAction) {
      public void actionPerformed(AnActionEvent e) {
        if (addScheme) {
          manager.getSchemesManager().addNewScheme(keymap, false);
        }
        manager.setActiveKeymap(keymap);
      }
    });
  }

  protected boolean isEnabled() {
    return ((KeymapManagerEx) KeymapManager.getInstance()).getAllKeymaps().length > 1;
  }
}