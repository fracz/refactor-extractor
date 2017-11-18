/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.application.options.codeStyle;

import com.intellij.application.options.DefaultSchemeActions;
import com.intellij.application.options.SaveSchemeDialog;
import com.intellij.application.options.SchemesToImportPopup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.options.*;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.psi.codeStyle.CodeStyleScheme;
import com.intellij.psi.impl.source.codeStyle.CodeStyleSchemesImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

abstract class CodeStyleSchemesActions extends DefaultSchemeActions<CodeStyleScheme> {
  private final CodeStyleSchemesModel mySchemesModel;

  private final static String SHARED_IMPORT_SOURCE = ApplicationBundle.message("import.scheme.shared");

  CodeStyleSchemesActions(@NotNull CodeStyleSchemesModel model) {
    mySchemesModel = model;
  }

  @Override
  protected void addAdditionalActions(@NotNull List<AnAction> defaultActions) {
    defaultActions.add(1, new CopyToProjectAction());
  }

  private class CopyToProjectAction extends AnAction {

    public CopyToProjectAction() {
      super("Copy To Project");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      CodeStyleScheme currentScheme = getCurrentScheme();
      if (currentScheme != null && !mySchemesModel.isProjectScheme(currentScheme)) {
        copyToProject(currentScheme);
      }
    }

    @Override
    public void update(AnActionEvent e) {
      Presentation p = e.getPresentation();
      CodeStyleScheme currentScheme = getCurrentScheme();
      p.setEnabled(currentScheme != null && !mySchemesModel.isProjectScheme(currentScheme));
    }
  }

  @Override
  protected void doReset() {
    CodeStyleScheme selectedScheme = getCurrentScheme();
    if (selectedScheme != null) {
      if (Messages
            .showOkCancelDialog(ApplicationBundle.message("settings.code.style.reset.to.defaults.message"),
                                ApplicationBundle.message("settings.code.style.reset.to.defaults.title"), Messages.getQuestionIcon()) ==
          Messages.OK) {
        selectedScheme.resetToDefaults();
        mySchemesModel.fireSchemeChanged(selectedScheme);
      }
    }
  }

  @Override
  protected void doSaveAs() {
    CodeStyleScheme scheme = getCurrentScheme();
    if (mySchemesModel.isProjectScheme(scheme)) {
      exportProjectScheme();
    }
    else {
      CodeStyleScheme currentScheme = getCurrentScheme();
      if (currentScheme != null) {
        String selectedName = currentScheme.getName();
        Collection<String> names = CodeStyleSchemesImpl.getSchemeManager().getAllSchemeNames();
        SaveSchemeDialog saveDialog =
          new SaveSchemeDialog(getParentComponent(), ApplicationBundle.message("title.save.code.style.scheme.as"), names, selectedName);
        if (saveDialog.showAndGet()) {
          CodeStyleScheme newScheme = mySchemesModel.createNewScheme(saveDialog.getSchemeName(), getCurrentScheme());
          mySchemesModel.addScheme(newScheme, true);
        }
      }
    }
  }

  @Override
  protected void doDelete() {
    mySchemesModel.removeScheme(getCurrentScheme());
  }

  @Override
  protected boolean isDeleteAvailable(@NotNull CodeStyleScheme scheme) {
    return !mySchemesModel.isProjectScheme(scheme) && !scheme.isDefault();
  }

  @Override
  protected void doImport(@NotNull String importerName) {
    CodeStyleScheme currentScheme = getCurrentScheme();
    if (currentScheme != null) {
      chooseAndImport(currentScheme, importerName);
    }
  }

  private void exportProjectScheme() {
    String name = Messages.showInputDialog("Enter new scheme name:", "Copy Project Scheme to Global List", Messages.getQuestionIcon());
    if (name != null && !CodeStyleSchemesModel.PROJECT_SCHEME_NAME.equals(name)) {
      CodeStyleScheme newScheme = mySchemesModel.exportProjectScheme(name);
      int switchToGlobal = Messages
        .showYesNoDialog("Project scheme was copied to global scheme list as '" + newScheme.getName() + "'.\n" +
                         "Switch to this created scheme?",
                         "Copy Project Scheme to Global List", Messages.getQuestionIcon());
      if (switchToGlobal == Messages.YES) {
        mySchemesModel.setUsePerProjectSettings(false);
        mySchemesModel.selectScheme(newScheme, null);
      }
    }
  }

  @NotNull
  protected abstract JComponent getParentComponent();

  private void chooseAndImport(@NotNull CodeStyleScheme currentScheme, @NotNull String importerName) {
    if (importerName.equals(SHARED_IMPORT_SOURCE)) {
      new SchemesToImportPopup<CodeStyleScheme>(getParentComponent()) {
        @Override
        protected void onSchemeSelected(CodeStyleScheme scheme) {
          if (scheme != null) {
            mySchemesModel.addScheme(scheme, true);
          }
        }
      }.show(mySchemesModel.getSchemes());
    }
    else {
      final SchemeImporter<CodeStyleScheme> importer = SchemeImporterEP.getImporter(importerName, CodeStyleScheme.class);
      if (importer == null) return;
      try {
        final CodeStyleScheme scheme = importExternalCodeStyle(importer, currentScheme);
        if (scheme != null) {
          final String additionalImportInfo = StringUtil.notNullize(importer.getAdditionalImportInfo(scheme));
          SchemeImportUtil
            .showStatus(getParentComponent(),
                        ApplicationBundle.message("message.code.style.scheme.import.success", importerName, scheme.getName(),
                                                  additionalImportInfo),
                        MessageType.INFO);
        }
      }
      catch (SchemeImportException e) {
        if (e.isWarning()) {
          SchemeImportUtil.showStatus(getParentComponent(), e.getMessage(), MessageType.WARNING);
          return;
        }
        final String message = ApplicationBundle.message("message.code.style.scheme.import.failure", importerName, e.getMessage());
        SchemeImportUtil.showStatus(getParentComponent(), message, MessageType.ERROR);
      }
    }
  }

  @Nullable
  private CodeStyleScheme importExternalCodeStyle(final SchemeImporter<CodeStyleScheme> importer, @NotNull CodeStyleScheme currentScheme)
    throws SchemeImportException {
    final VirtualFile selectedFile = SchemeImportUtil
      .selectImportSource(importer.getSourceExtensions(), getParentComponent(), CodeStyleSchemesUIConfiguration.Util.getRecentImportFile());
    if (selectedFile != null) {
      CodeStyleSchemesUIConfiguration.Util.setRecentImportFile(selectedFile);
      final SchemeCreator schemeCreator = new SchemeCreator();
      final CodeStyleScheme
        schemeImported = importer.importScheme(mySchemesModel.getProject(), selectedFile, currentScheme, schemeCreator);
      if (schemeImported != null) {
        if (schemeCreator.isSchemeWasCreated()) {
          mySchemesModel.fireSchemeListChanged();
        }
        else {
          mySchemesModel.fireSchemeChanged(schemeImported);
        }
        return schemeImported;
      }
    }
    return null;
  }

  private class SchemeCreator implements SchemeFactory<CodeStyleScheme> {
    private boolean mySchemeWasCreated;

    @Override
    public CodeStyleScheme createNewScheme(@Nullable String targetName) {
      mySchemeWasCreated = true;
      if (targetName == null) targetName = ApplicationBundle.message("code.style.scheme.import.unnamed");
      CodeStyleScheme newScheme = mySchemesModel.createNewScheme(targetName, getCurrentScheme());
      mySchemesModel.addScheme(newScheme, true);
      return newScheme;
    }

    public boolean isSchemeWasCreated() {
      return mySchemeWasCreated;
    }
  }

  @Override
  protected Class<CodeStyleScheme> getSchemeType() {
    return CodeStyleScheme.class;
  }

  public void copyToProject(CodeStyleScheme scheme) {
    mySchemesModel.copyToProject(scheme);
    int switchToProject = Messages
      .showYesNoDialog("Scheme '" + scheme.getName() + "' was copied to be used as the project scheme.\n" +
                       "Switch to this created scheme?",
                       "Copy Scheme to Project", Messages.getQuestionIcon());
    if (switchToProject == Messages.YES) {
      mySchemesModel.setUsePerProjectSettings(true, true);
    }
  }

  @SuppressWarnings("Duplicates")
  @Override
  protected void doExport(@NotNull CodeStyleScheme scheme, @NotNull String exporterName) {
    SchemeExporter<CodeStyleScheme> exporter = SchemeExporterEP.getExporter(exporterName, CodeStyleScheme.class);
    if (exporter != null) {
      String ext = exporter.getExtension();
      FileSaverDialog saver =
        FileChooserFactory.getInstance()
          .createSaveFileDialog(new FileSaverDescriptor(
            ApplicationBundle.message("scheme.exporter.ui.file.chooser.title"),
            ApplicationBundle.message("scheme.exporter.ui.file.chooser.message"),
            ext), getParentComponent());
      VirtualFileWrapper target = saver.save(null, scheme.getName() + "." + ext);
      if (target != null) {
        VirtualFile targetFile = target.getVirtualFile(true);
        String message;
        MessageType messageType;
        if (targetFile != null) {
          try {
            WriteAction.run(() -> {
              OutputStream outputStream = targetFile.getOutputStream(this);
              try {
                exporter.exportScheme(scheme, outputStream);
              }
              finally {
                outputStream.close();
              }
            });
            message = ApplicationBundle
              .message("scheme.exporter.ui.code.style.exported.message", scheme.getName(), targetFile.getPresentableUrl());
            messageType = MessageType.INFO;
          }
          catch (Exception e) {
            message = ApplicationBundle.message("scheme.exporter.ui.export.failed", e.getMessage());
            messageType = MessageType.ERROR;
          }
        }
        else {
          message = ApplicationBundle.message("scheme.exporter.ui.cannot.write.message");
          messageType = MessageType.ERROR;
        }
        SchemeImportUtil.showStatus(getParentComponent(), message, messageType);
      }
    }
  }
}