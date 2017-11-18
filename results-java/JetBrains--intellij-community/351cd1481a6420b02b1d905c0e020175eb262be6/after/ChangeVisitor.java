package com.intellij.localvcs.core.changes;

import java.io.IOException;

public abstract class ChangeVisitor {
  public void visit(ChangeSet c) throws IOException, StopVisitingException {
    visit((Change)c);
  }

  public void visit(CreateFileChange c) throws IOException, StopVisitingException {
    visit((Change)c);
  }

  public void visit(CreateDirectoryChange c) throws IOException, StopVisitingException {
    visit((Change)c);
  }

  public void visit(ChangeFileContentChange c) throws IOException, StopVisitingException {
    visit((Change)c);
  }

  public void visit(RenameChange c) throws IOException, StopVisitingException {
    visit((Change)c);
  }

  public void visit(MoveChange c) throws IOException, StopVisitingException {
    visit((Change)c);
  }

  public void visit(DeleteChange c) throws IOException, StopVisitingException {
    visit((Change)c);
  }

  public void visit(Change c) throws IOException, StopVisitingException {
  }

  protected void stop() throws StopVisitingException {
    throw new StopVisitingException();
  }

  public static class StopVisitingException extends Exception {
  }
}