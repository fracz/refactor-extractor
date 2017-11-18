package com.intellij.history.core;

import com.intellij.history.core.revisions.Revision;
import com.intellij.history.core.storage.Content;
import com.intellij.history.core.storage.StoredContent;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LocalVcsPurgingTest extends LocalVcsTestCase {
  InMemoryLocalVcs vcs = new InMemoryLocalVcs(new PurgeLoggingStorage());
  List<Content> purgedContent = new ArrayList<Content>();

  @Before
  public void setUp() {
    setCurrentTimestamp(10);
    long timestamp = -1;
    vcs.createFile("file", cf("one"), timestamp, false);

    setCurrentTimestamp(20);
    vcs.changeFileContent("file", cf("two"), -1);

    setCurrentTimestamp(30);
    vcs.changeFileContent("file", cf("three"), -1);

    setCurrentTimestamp(40);
    vcs.changeFileContent("file", cf("four"), -1);
  }

  @Test
  public void testPurging() {
    assertEquals(4, vcs.getRevisionsFor("file").size());

    vcs.purgeObsoleteAndSave(5);

    List<Revision> rr = vcs.getRevisionsFor("file");
    assertEquals(2, rr.size());
    assertEquals(40L, rr.get(0).getTimestamp());
  }

  @Test
  public void testPurgingContents() {
    vcs.purgeObsoleteAndSave(5);

    assertEquals(2, purgedContent.size());
    assertEquals(c("one"), purgedContent.get(0));
    assertEquals(c("two"), purgedContent.get(1));
  }

  @Test
  public void testDoesNotPurgeLongContentFromContentStorage() {
    vcs = new InMemoryLocalVcs(new PurgeLoggingStorage());
    setCurrentTimestamp(10);
    long timestamp = -1;
    vcs.createFile("file", bigContentFactory(), timestamp, false);

    setCurrentTimestamp(20);
    vcs.changeFileContent("file", cf("one"), -1);

    setCurrentTimestamp(30);
    vcs.changeFileContent("file", cf("twoo"), -1);

    vcs.purgeObsoleteAndSave(5);

    assertTrue(purgedContent.isEmpty());
  }

  class PurgeLoggingStorage extends InMemoryStorage {
    @Override
    public void purgeContent(StoredContent c) {
      purgedContent.add(c);
    }
  }
}