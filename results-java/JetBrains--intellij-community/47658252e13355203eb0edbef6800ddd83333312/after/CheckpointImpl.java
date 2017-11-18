package com.intellij.localvcs.integration;

import com.intellij.localvcs.core.ILocalVcs;
import com.intellij.localvcs.core.changes.Change;
import com.intellij.localvcs.core.changes.ChangeFileContentChange;
import com.intellij.localvcs.core.changes.ChangeVisitor;
import com.intellij.localvcs.core.changes.StructuralChange;
import com.intellij.localvcs.integration.revert.ChangeRevertionVisitor;

import java.io.IOException;

public class CheckpointImpl implements Checkpoint {
  private Change myLastChange;
  private IdeaGateway myGateway;
  private ILocalVcs myVcs;

  public CheckpointImpl(IdeaGateway gw, ILocalVcs vcs) {
    myGateway = gw;
    myVcs = vcs;
    myLastChange = myVcs.getLastChange();
  }

  public void revertToPreviousState() {
    doRevert(true);
  }

  public void revertToThatState() {
    doRevert(false);
  }

  private void doRevert(boolean revertLastChange) {
    try {
      ChangeVisitor v = new ChangeRevertionVisitor(myVcs, myGateway);
      myVcs.accept(new SelectiveChangeVisitor(v, revertLastChange));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private class SelectiveChangeVisitor extends ChangeVisitor {
    private ChangeVisitor myVisitor;
    private boolean myRevertLastChange;

    public SelectiveChangeVisitor(ChangeVisitor v, boolean revertLastChange) {
      myVisitor = v;
      myRevertLastChange = revertLastChange;
    }

    @Override
    public void visit(StructuralChange c) throws IOException, StopVisitingException {
      if (c == myLastChange) {
        if (myRevertLastChange) doVisit(c);
        stop();
      }
      doVisit(c);
    }

    private void doVisit(StructuralChange c) throws IOException, StopVisitingException {
      if (c instanceof ChangeFileContentChange) return;
      c.accept(myVisitor);
    }
  }
}