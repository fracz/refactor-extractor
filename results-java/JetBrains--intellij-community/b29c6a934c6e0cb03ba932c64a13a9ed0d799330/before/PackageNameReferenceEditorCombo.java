package com.intellij.refactoring.ui;

import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.ui.ReferenceEditorComboWithBrowseButton;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author yole
 */
public class PackageNameReferenceEditorCombo extends ReferenceEditorComboWithBrowseButton {
  public PackageNameReferenceEditorCombo(final String text, @NotNull final Project project,
                                         final String recentsKey, final String chooserTitle) {
    super(null, text, PsiManager.getInstance(project), false, recentsKey);
    addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PackageChooserDialog chooser = new PackageChooserDialog(chooserTitle, project);
        chooser.selectPackage(getText());
        chooser.show();
        PsiPackage aPackage = chooser.getSelectedPackage();
        if (aPackage != null) {
          setText(aPackage.getQualifiedName());
        }
      }
    });
  }
}