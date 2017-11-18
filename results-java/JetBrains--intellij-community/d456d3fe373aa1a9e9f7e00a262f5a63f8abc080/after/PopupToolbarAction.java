package com.intellij.ide.navigationToolbar;

import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

/**
 * User: anna
 * Date: 19-Dec-2005
 */
public class PopupToolbarAction extends AnAction implements DumbAware {
  public void actionPerformed(AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    if (project == null) return;
    if (UISettings.getInstance().SHOW_NAVIGATION_BAR){
      new SelectInNavBarTarget(project).select(null, false);
      return;
    }
    if (PlatformDataKeys.CONTEXT_COMPONENT.getData(dataContext) instanceof NavBarPanel) {
      return;
    }
    final Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
    final NavBarPanel toolbarPanel = new NavBarPanel(project);
    toolbarPanel.showHint(editor, dataContext);
  }

  @Override
  public void update(final AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    e.getPresentation().setEnabled(project != null);
  }
}