/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 02.11.2006
 * Time: 22:07:51
 */
package com.intellij.openapi.vcs.changes.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vfs.LocalFileSystem;

import java.util.List;

public class RollbackDeletionAction extends AbstractMissingFilesAction {
  protected List<VcsException> processFiles(final CheckinEnvironment environment, final List<FilePath> files) {
    final List<VcsException> result = environment.rollbackMissingFileDeletion(files);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        LocalFileSystem.getInstance().refreshIoFiles(ChangesUtil.filePathsToFiles(files));
      }
    });
    return result;
  }
}