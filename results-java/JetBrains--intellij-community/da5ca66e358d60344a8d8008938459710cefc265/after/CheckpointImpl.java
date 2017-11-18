package com.intellij.history.integration;

import com.intellij.history.Checkpoint;
import com.intellij.history.core.LocalVcs;
import com.intellij.history.core.changes.Change;
import com.intellij.history.core.changes.ChangeVisitor;
import com.intellij.history.core.changes.ContentChange;
import com.intellij.history.core.changes.StructuralChange;
import com.intellij.history.core.tree.Entry;
import com.intellij.history.integration.revertion.ChangeRevertionVisitor;

import java.io.IOException;

public class CheckpointImpl implements Checkpoint {
  private final Change myLastChange;
  private final IdeaGateway myGateway;
  private final LocalVcs myVcs;

  public CheckpointImpl(IdeaGateway gw, LocalVcs vcs) {
    myGateway = gw;
    myVcs = vcs;
    myLastChange = myVcs.getLastChange();
  }

  public void revertToPreviousState() throws IOException {
    doRevert(true);
  }

  public void revertToThatState() throws IOException {
    doRevert(false);
  }

  private void doRevert(boolean revertLastChange) throws IOException {
    ChangeVisitor v = new ChangeRevertionVisitor(myGateway);
    myVcs.acceptWrite(new SelectiveChangeVisitor(v, revertLastChange));
  }

  private class SelectiveChangeVisitor extends ChangeVisitor {
    private final ChangeVisitor myVisitor;
    private final boolean myRevertLastChange;

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
      if (c instanceof ContentChange) return;
      c.accept(myVisitor);
    }

    @Override
    public void started(Entry r) throws IOException {
      myVisitor.started(r);
    }

    @Override
    public void finished() throws IOException {
      myVisitor.finished();
    }
  }
}