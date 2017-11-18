package com.intellij.history.integration.ui.models;

import com.intellij.history.core.LocalVcs;
import com.intellij.history.integration.IdeaGateway;
import com.intellij.history.integration.revertion.FileReverter;
import com.intellij.history.integration.revertion.Reverter;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class FileHistoryDialogModel extends HistoryDialogModel {
  public FileHistoryDialogModel(IdeaGateway gw, LocalVcs vcs, VirtualFile f) {
    super(gw, vcs, f);
  }

  public abstract FileDifferenceModel getDifferenceModel();

  @Override
  protected Reverter createRevisionReverter() {
    return new FileReverter(myGateway, getLeftRevision(), getLeftEntry(), getRightEntry());
  }
}