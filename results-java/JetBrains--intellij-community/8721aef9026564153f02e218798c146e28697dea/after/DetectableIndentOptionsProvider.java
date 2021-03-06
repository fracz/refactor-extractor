/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
package com.intellij.psi.codeStyle.autodetect;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.*;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.containers.WeakList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.List;

import static com.intellij.psi.codeStyle.EditorNotificationInfo.*;

/**
 * @author Rustam Vishnyakov
 */
public class DetectableIndentOptionsProvider extends FileIndentOptionsProvider {
  private boolean myIsEnabledInTest;
  private final List<VirtualFile> myAcceptedFiles = new WeakList<VirtualFile>();
  private final List<VirtualFile> myDisabledFiles = new WeakList<VirtualFile>();

  @Nullable
  @Override
  public CommonCodeStyleSettings.IndentOptions getIndentOptions(@NotNull CodeStyleSettings settings, @NotNull PsiFile file) {
    return isEnabled(settings, file) ? new IndentOptionsDetectorImpl(file).getIndentOptions() : null;
  }

  @Override
  public boolean useOnFullReformat() {
    return false;
  }

  @TestOnly
  public void setEnabledInTest(boolean isEnabledInTest) {
    myIsEnabledInTest = isEnabledInTest;
  }

  private boolean isEnabled(@NotNull CodeStyleSettings settings, @NotNull PsiFile file) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return myIsEnabledInTest;
    }
    VirtualFile vFile = file.getVirtualFile();
    if (vFile == null || vFile instanceof LightVirtualFile || myDisabledFiles.contains(vFile)) return false;
    return settings.AUTODETECT_INDENTS;
  }

  @TestOnly
  @Nullable
  public static DetectableIndentOptionsProvider getInstance() {
    return FileIndentOptionsProvider.EP_NAME.findExtension(DetectableIndentOptionsProvider.class);
  }

  @Nullable
  @Override
  public EditorNotificationInfo getNotificationInfo(@NotNull final Project project,
                                                    @NotNull final VirtualFile file,
                                                    @NotNull final FileEditor fileEditor,
                                                    @NotNull CommonCodeStyleSettings.IndentOptions userOptions,
                                                    @NotNull CommonCodeStyleSettings.IndentOptions detectedOptions)
  {
    NotificationLabels labels = getNotificationLabels(userOptions, detectedOptions);
    final Editor editor = fileEditor instanceof TextEditor ? ((TextEditor)fileEditor).getEditor() : null;
    if (labels == null || editor == null) return null;

    LabelWithAction okAction = new LabelWithAction(
      ApplicationBundle.message("code.style.indents.detector.accept"),
      new Runnable() {
        @Override
        public void run() {
          setAccepted(file);
        }
      }
    ).setUpdateAllNotificationsOnActionEnd(true);

    LabelWithAction disableForSingleFile = new LabelWithAction(
      labels.revertToOldSettingsLabel,
      new Runnable() {
        @Override
        public void run() {
          disableForFile(file);
          if (editor instanceof EditorEx) {
            ((EditorEx)editor).reinitSettings();
          }
        }
      }
    ).setUpdateAllNotificationsOnActionEnd(true);

    LabelWithAction showSettings = new LabelWithAction(
      ApplicationBundle.message("code.style.indents.detector.show.settings"),
      new Runnable() {
        @Override
        public void run() {
          ShowSettingsUtilImpl.showSettingsDialog(project, "preferences.sourceCode",
                                                  ApplicationBundle.message("settings.code.style.general.autodetect.indents"));
        }
      }
    ).setUpdateAllNotificationsOnActionEnd(true);

    return new EditorNotificationInfo(labels.title, okAction, disableForSingleFile, showSettings);
  }

  @Nullable
  private static NotificationLabels getNotificationLabels(@NotNull CommonCodeStyleSettings.IndentOptions userOptions,
                                                          @NotNull CommonCodeStyleSettings.IndentOptions detectedOptions) {
    if (userOptions.USE_TAB_CHARACTER) {
      if (!detectedOptions.USE_TAB_CHARACTER) {
        return new NotificationLabels(ApplicationBundle.message("code.style.space.indent.detected", detectedOptions.INDENT_SIZE),
                                                   ApplicationBundle.message("code.style.detector.use.tabs"));
      }
    }
    else {
      String restoreToSpaces = ApplicationBundle.message("code.style.detector.use.spaces", userOptions.INDENT_SIZE);
      if (detectedOptions.USE_TAB_CHARACTER) {
        return new NotificationLabels(ApplicationBundle.message("code.style.tab.usage.detected", userOptions.INDENT_SIZE),
                                                   restoreToSpaces);
      }
      if (userOptions.INDENT_SIZE != detectedOptions.INDENT_SIZE) {
        return new NotificationLabels(ApplicationBundle.message("code.style.different.indent.size.detected", detectedOptions.INDENT_SIZE, userOptions.INDENT_SIZE),
                                                   restoreToSpaces);
      }
    }
    return null;
  }

  private void disableForFile(@NotNull VirtualFile file) {
    myDisabledFiles.add(file);
  }

  @Override
  public void setAccepted(@NotNull VirtualFile file) {
    myAcceptedFiles.add(file);
  }

  @Override
  public boolean isAcceptedWithoutWarning(@NotNull VirtualFile file) {
    return myAcceptedFiles.contains(file);
  }

  private static class NotificationLabels {
    public final String title;
    public final String revertToOldSettingsLabel;

    public NotificationLabels(@NotNull String title, @NotNull String revertToOldSettingsLabel) {
      this.title = title;
      this.revertToOldSettingsLabel = revertToOldSettingsLabel;
    }
  }
}