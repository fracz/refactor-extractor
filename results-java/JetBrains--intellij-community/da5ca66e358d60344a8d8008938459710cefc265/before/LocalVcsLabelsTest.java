package com.intellij.history.core;

import com.intellij.history.core.changes.PutLabelChange;
import com.intellij.history.core.changes.PutSystemLabelChange;
import com.intellij.history.core.revisions.Revision;
import org.junit.Test;

import java.util.List;

public class LocalVcsLabelsTest extends LocalVcsTestCase {
  LocalVcs vcs = new InMemoryLocalVcs();

  @Test
  public void testUserLabels() {
    long timestamp = -1;
    vcs.createFile("file", null, timestamp, false);
    vcs.putUserLabel("file", "1");
    vcs.changeFileContent("file", null, -1);
    vcs.putUserLabel("file", "2");

    List<Revision> rr = vcs.getRevisionsFor("file");
    assertEquals(4, rr.size());

    assertEquals("2", rr.get(0).getName());
    assertNull(rr.get(1).getName());
    assertEquals("1", rr.get(2).getName());
    assertNull(rr.get(3).getName());
  }

  @Test
  public void testDoesNotIncludeLabelsForAnotherEntry() {
    long timestamp = -1;
    vcs.createFile("one", null, timestamp, false);
    long timestamp1 = -1;
    vcs.createFile("two", null, timestamp1, false);
    vcs.putUserLabel("one", "one");
    vcs.putUserLabel("two", "two");

    List<Revision> rr = vcs.getRevisionsFor("one");
    assertEquals(2, rr.size());
    assertEquals("one", rr.get(0).getName());

    rr = vcs.getRevisionsFor("two");
    assertEquals(2, rr.size());
    assertEquals("two", rr.get(0).getName());
  }

  @Test
  public void testLabelTimestamps() {
    setCurrentTimestamp(10);
    long timestamp = -1;
    vcs.createFile("file", null, timestamp, false);
    setCurrentTimestamp(20);
    vcs.putUserLabel("file", "1");
    setCurrentTimestamp(30);
    vcs.putUserLabel("file", "1");

    List<Revision> rr = vcs.getRevisionsFor("file");
    assertEquals(30, rr.get(0).getTimestamp());
    assertEquals(20, rr.get(1).getTimestamp());
    assertEquals(10, rr.get(2).getTimestamp());
  }

  @Test
  public void testContent() {
    long timestamp = -1;
    vcs.createFile("file", cf("old"), timestamp, false);
    vcs.putUserLabel("file", "");
    vcs.changeFileContent("file", cf("new"), -1);
    vcs.putUserLabel("file", "");

    List<Revision> rr = vcs.getRevisionsFor("file");

    assertEquals(c("new"), rr.get(0).getEntry().getContent());
    assertEquals(c("old"), rr.get(2).getEntry().getContent());
  }

  @Test
  public void testLabelsAfterPurge() {
    setCurrentTimestamp(10);
    long timestamp = -1;
    vcs.createFile("file", null, timestamp, false);
    setCurrentTimestamp(20);
    vcs.putUserLabel("file", "l");

    vcs.purgeObsoleteAndSave(5);

    List<Revision> rr = vcs.getRevisionsFor("file");
    assertEquals(1, rr.size());
    assertEquals("l", rr.get(0).getName());
  }

  @Test
  public void testGlobalUserLabels() {
    long timestamp = -1;
    vcs.createFile("one", null, timestamp, false);
    vcs.putUserLabel("1");
    long timestamp1 = -1;
    vcs.createFile("two", null, timestamp1, false);
    vcs.putUserLabel("2");

    List<Revision> rr = vcs.getRevisionsFor("one");
    assertEquals(3, rr.size());
    assertEquals("2", rr.get(0).getName());
    assertEquals("1", rr.get(1).getName());

    rr = vcs.getRevisionsFor("two");
    assertEquals(2, rr.size());
    assertEquals("2", rr.get(0).getName());
  }

  @Test
  public void testGlobalLabelTimestamps() {
    setCurrentTimestamp(10);
    long timestamp = -1;
    vcs.createFile("file", null, timestamp, false);
    setCurrentTimestamp(20);
    vcs.putUserLabel("");

    List<Revision> rr = vcs.getRevisionsFor("file");
    assertEquals(20, rr.get(0).getTimestamp());
    assertEquals(10, rr.get(1).getTimestamp());
  }

  @Test
  public void testLabelsDuringChangeSet() {
    long timestamp = -1;
    vcs.createFile("f", null, timestamp, false);
    vcs.beginChangeSet();
    vcs.changeFileContent("f", null, -1);
    vcs.putUserLabel("label");
    vcs.endChangeSet("changeSet");

    List<Revision> rr = vcs.getRevisionsFor("f");
    assertEquals(2, rr.size());
    assertEquals("changeSet", rr.get(0).getCauseChangeName());
    assertEquals(null, rr.get(1).getCauseChangeName());
  }

  @Test
  public void testSystemLabels() {
    long timestamp = -1;
    vcs.createFile("f1", null, timestamp, false);
    long timestamp1 = -1;
    vcs.createFile("f2", null, timestamp1, false);

    setCurrentTimestamp(123);
    vcs.putSystemLabel("label", 456);

    List<Revision> rr1 = vcs.getRevisionsFor("f1");
    List<Revision> rr2 = vcs.getRevisionsFor("f2");
    assertEquals(2, rr1.size());
    assertEquals(2, rr2.size());

    assertEquals("label", rr1.get(0).getName());
    assertEquals("label", rr2.get(0).getName());

    PutLabelChange l = (PutLabelChange)rr1.get(0).getCauseChange();
    assertTrue(l.isSystemLabel());
    assertEquals(123, l.getTimestamp());
    assertEquals(456, ((PutSystemLabelChange)l).getColor());
  }
}