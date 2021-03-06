package com.intellij.ide.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleScheme;
import com.intellij.psi.codeStyle.CodeStyleSchemes;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.impl.source.codeStyle.CodeStyleSchemesImpl;

import java.util.Collection;

/**
 * @author max
 */
public class QuickChangeCodeStyleSchemeAction extends QuickSwitchSchemeAction {
  protected void fillActions(Project project, DefaultActionGroup group) {
    final CodeStyleSettingsManager manager = CodeStyleSettingsManager.getInstance(project);
    if (manager.PER_PROJECT_SETTINGS != null) {
      //noinspection HardCodedStringLiteral
      group.add(new AnAction("<project>", "",
                             manager.USE_PER_PROJECT_SETTINGS ? ourCurrentAction : ourNotCurrentAction) {
        public void actionPerformed(AnActionEvent e) {
          manager.USE_PER_PROJECT_SETTINGS = true;
        }
      });
    }

    final CodeStyleScheme[] schemes = CodeStyleSchemes.getInstance().getSchemes();
    final CodeStyleScheme currentScheme = CodeStyleSchemes.getInstance().getCurrentScheme();

    for (final CodeStyleScheme scheme : schemes) {
      addScheme(group, manager, currentScheme, scheme);
    }

    group.add(Separator.getInstance());

    Collection<CodeStyleScheme> sharedSchemes =
        ((CodeStyleSchemesImpl)CodeStyleSchemes.getInstance()).getSchemesManager().loadScharedSchemes();

    for (CodeStyleScheme scheme : sharedSchemes) {
      addScheme(group, manager, currentScheme, scheme);
    }
  }

  private void addScheme(final DefaultActionGroup group, final CodeStyleSettingsManager manager, final CodeStyleScheme currentScheme,
                         final CodeStyleScheme scheme) {
    group.add(new AnAction(scheme.getName(), "",
                           scheme == currentScheme && !manager.USE_PER_PROJECT_SETTINGS ? ourCurrentAction : ourNotCurrentAction) {
      public void actionPerformed(AnActionEvent e) {
        CodeStyleSchemes.getInstance().setCurrentScheme(scheme);
        manager.USE_PER_PROJECT_SETTINGS = false;
        EditorFactory.getInstance().refreshAllEditors();
      }
    });
  }

  protected boolean isEnabled() {
    return CodeStyleSchemes.getInstance().getSchemes().length > 1;
  }

  public void update(AnActionEvent e) {
    super.update(e);
    e.getPresentation().setEnabled(e.getDataContext().getData(DataConstants.PROJECT) != null);
  }
}