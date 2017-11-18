package com.intellij.localvcs.core.changes;

import com.intellij.localvcs.core.IdPath;
import com.intellij.localvcs.core.storage.Content;
import com.intellij.localvcs.core.storage.Stream;
import com.intellij.localvcs.core.tree.Entry;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ChangeFileContentChange extends StructuralChange {
  private Content myNewContent; // transient
  private Content myOldContent;
  private long myNewTimestamp; // transient
  private long myOldTimestamp;

  public ChangeFileContentChange(String path, Content newContent, long timestamp) {
    super(path);
    myNewContent = newContent;
    myNewTimestamp = timestamp;
  }

  public ChangeFileContentChange(Stream s) throws IOException {
    super(s);
    myOldContent = s.readContent();
    myOldTimestamp = s.readLong();
  }

  @Override
  public void write(Stream s) throws IOException {
    super.write(s);
    s.writeContent(myOldContent);
    s.writeLong(myOldTimestamp);
  }

  public Content getOldContent() {
    return myOldContent;
  }

  public long getOldTimestamp() {
    return myOldTimestamp;
  }

  @Override
  protected IdPath doApplyTo(Entry root) {
    Entry e = root.getEntry(myPath);

    myOldContent = e.getContent();
    myOldTimestamp = e.getTimestamp();

    e.changeContent(myNewContent, myNewTimestamp);
    return e.getIdPath();
  }

  @Override
  public void revertOn(Entry root) {
    Entry e = root.getEntry(myAffectedIdPath);
    e.changeContent(myOldContent, myOldTimestamp);
  }

  @Override
  public List<Content> getContentsToPurge() {
    return Collections.singletonList(myOldContent);
  }

  @Override
  public void accept(ChangeVisitor v) throws IOException, ChangeVisitor.StopVisitingException {
    v.visit(this);
  }
}