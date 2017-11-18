package com.intellij.localvcs.core.changes;

import com.intellij.localvcs.core.storage.Content;
import com.intellij.localvcs.core.storage.Stream;
import com.intellij.localvcs.core.tree.Entry;
import com.intellij.localvcs.core.tree.RootEntry;
import com.intellij.localvcs.utils.Reversed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChangeSet extends Change {
  private String myName;
  private long myTimestamp;
  private List<Change> myChanges;

  public ChangeSet(long timestamp, String name, List<Change> changes) {
    myTimestamp = timestamp;
    myChanges = changes;
    myName = name;
  }

  public ChangeSet(Stream s) throws IOException {
    // todo get rid of null here
    myName = s.readStringOrNull();
    myTimestamp = s.readLong();

    int count = s.readInteger();
    myChanges = new ArrayList<Change>(count);
    while (count-- > 0) {
      myChanges.add(s.readChange());
    }
  }

  public void write(Stream s) throws IOException {
    s.writeStringOrNull(myName);
    s.writeLong(myTimestamp);

    s.writeInteger(myChanges.size());
    for (Change c : myChanges) {
      s.writeChange(c);
    }
  }

  @Override
  public String getName() {
    return myName;
  }

  @Override
  public long getTimestamp() {
    return myTimestamp;
  }

  @Override
  public List<Change> getChanges() {
    return myChanges;
  }

  @Override
  public void applyTo(RootEntry r) {
    for (Change c : myChanges) {
      c.applyTo(r);
    }
  }

  @Override
  public void revertOn(RootEntry e) {
    for (Change c : Reversed.list(myChanges)) c.revertOn(e);
  }

  @Override
  public boolean affects(Entry e) {
    for (Change c : myChanges) {
      if (c.affects(e)) return true;
    }
    return false;
  }

  @Override
  public boolean affectsOnlyInside(Entry e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCreationalFor(Entry e) {
    for (Change c : myChanges) {
      if (c.isCreationalFor(e)) return true;
    }
    return false;
  }

  @Override
  public List<Content> getContentsToPurge() {
    List<Content> result = new ArrayList<Content>();
    for (Change c : myChanges) {
      result.addAll(c.getContentsToPurge());
    }
    return result;
  }

  @Override
  public void accept(ChangeVisitor v) throws IOException, ChangeVisitor.StopVisitingException {
    v.visit(this);
    for (Change c : Reversed.list(myChanges)) {
      c.accept(v);
    }
  }
}