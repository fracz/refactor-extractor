/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 17.11.2006
 * Time: 17:08:11
 */
package com.intellij.openapi.vcs.changes.patch;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.ApplyPatchException;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.ApplyPatchStatus;
import com.intellij.openapi.diff.DiffRequestFactory;
import com.intellij.openapi.diff.ActionButtonPresentation;
import com.intellij.openapi.diff.MergeRequest;
import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.fileTypes.ex.FileTypeChooser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.peer.PeerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ApplyPatchAction extends AnAction {
  private static final Logger LOG = Logger.getInstance("#com.intellij.openapi.vcs.changes.patch.ApplyPatchAction");

  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getData(DataKeys.PROJECT);
    final ApplyPatchDialog dialog = new ApplyPatchDialog(project);
    dialog.show();
    if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
      return;
    }
    final List<FilePatch> patches = dialog.getPatches();
    for(FilePatch patch: patches) {
      VirtualFile fileToPatch = patch.findFileToPatch(dialog.getBaseDirectory(), dialog.getStripLeadingDirectories());
      if (fileToPatch != null) {
        FileType fileType = fileToPatch.getFileType();
        if (fileType == StdFileTypes.UNKNOWN) {
          fileType = FileTypeChooser.associateFileType(fileToPatch.getPresentableName());
          if (fileType == null) {
            return;
          }
        }
      }
    }
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
          public void run() {
            ApplyPatchStatus status = null;
            for(FilePatch patch: patches) {
              final ApplyPatchStatus patchStatus = applySinglePatch(project, patch, dialog.getBaseDirectory(), dialog.getStripLeadingDirectories());
              status = ApplyPatchStatus.and(status, patchStatus);
            }
            if (status == ApplyPatchStatus.ALREADY_APPLIED) {
              Messages.showInfoMessage(project, "All of the changes in the specified patch are already contained in the code", "Apply Patch");
            }
            else if (status == ApplyPatchStatus.PARTIAL) {
              Messages.showInfoMessage(project, "Some of the changes in the specified patch were skipped because are already contained in the code", "Apply Patch");
            }
          }
        }, "apply patch", null);
      }
    });
  }

  private static ApplyPatchStatus applySinglePatch(final Project project, final FilePatch patch, final VirtualFile baseDirectory,
                                                   final int stripLeadingDirectories) {
    VirtualFile file = patch.findFileToPatch(baseDirectory, stripLeadingDirectories);
    if (file == null) {
      Messages.showErrorDialog(project, "Cannot find file to patch: " + patch.getBeforeName(), "Apply Patch");
      return ApplyPatchStatus.FAILURE;
    }

    try {
      return patch.apply(file);
    }
    catch(ApplyPatchException ex) {
      if (!patch.isNewFile() && !patch.isDeletedFile()) {
        CharSequence content = findMatchingContent(project, patch, file);
        if (content != null) {
          try {
            StringBuilder newText = new StringBuilder();
            ApplyPatchStatus status = patch.applyModifications(content, newText);
            if (status != ApplyPatchStatus.ALREADY_APPLIED) {
              return showMergeDialog(project, file, content, newText.toString());
            }
            else {
              return status;
            }
          }
          catch (ApplyPatchException e) {
            // ignore
          }
        }
      }
      Messages.showErrorDialog(project, "Failed to apply patch because of conflicts: " + patch.getBeforeName(),
                               VcsBundle.message("patch.apply.dialog.title"));
    }
    catch (Exception ex) {
      LOG.error(ex);
    }
    return ApplyPatchStatus.FAILURE;
  }

  private static ApplyPatchStatus showMergeDialog(Project project, VirtualFile file, CharSequence content, final String patchedContent) {
    final DiffRequestFactory diffRequestFactory = PeerFactory.getInstance().getDiffRequestFactory();
    CharSequence fileContent = LoadTextUtil.loadText(file);
    final MergeRequest request = diffRequestFactory.createMergeRequest(fileContent.toString(), patchedContent, content.toString(), file,
                                                                       project, ActionButtonPresentation.createApplyButton());
    request.setVersionTitles(new String[] {
      VcsBundle.message("patch.apply.conflict.local.version"),
      VcsBundle.message("patch.apply.conflict.merged.version"),
      VcsBundle.message("patch.apply.conflict.patched.version")
    });
    request.setWindowTitle(VcsBundle.message("patch.apply.conflict.title", file.getPresentableUrl()));
    DiffManager.getInstance().getDiffTool().show(request);
    if (request.getResult() == DialogWrapper.OK_EXIT_CODE) {
      return ApplyPatchStatus.SUCCESS;
    }
    return ApplyPatchStatus.FAILURE;
  }

  @Nullable
  private static CharSequence findMatchingContent(final Project project, final FilePatch patch, final VirtualFile file) {
    final PatchBaseVersionProvider[] baseVersionProviders = project.getComponents(PatchBaseVersionProvider.class);
    for(PatchBaseVersionProvider provider: baseVersionProviders) {
      final CharSequence content = provider.getBaseVersionContent(file, patch.getBeforeVersionId());
      if (content != null) {
        return content;
      }
    }
    return null;
  }

  @Override
  public void update(AnActionEvent e) {
    Project project = e.getData(DataKeys.PROJECT);
    e.getPresentation().setEnabled(project != null);
  }
}