package com.intellij.localvcs.core.changes;

import java.io.IOException;

public abstract class SelectiveChangeVisitor extends ChangeVisitor {
  private ChangeVisitor myVisitor;

  public SelectiveChangeVisitor(ChangeVisitor v) {
    myVisitor = v;
  }

  @Override
  public void visit(ChangeSet c) throws StopVisitingException {
    if (isFinished(c)) stop();
  }

  protected abstract boolean isFinished(ChangeSet c);

  @Override
  public void visit(StructuralChange c) throws IOException, StopVisitingException {
    if (!shouldProcess(c)) return;
    c.accept(myVisitor);
  }

  protected abstract boolean shouldProcess(StructuralChange c);
}