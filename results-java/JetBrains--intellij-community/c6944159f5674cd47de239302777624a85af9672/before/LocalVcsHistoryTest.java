package com.intellij.localvcs;

import java.util.List;

import org.junit.Test;

public class LocalVcsHistoryTest extends LocalVcsTestCase {
  @Test
  public void testRevertingToPreviousVersion() {
    myVcs.createFile(fn("file"), "");
    myVcs.commit();
    assertTrue(myVcs.hasEntry(fn("file")));

    myVcs.revert();
    assertFalse(myVcs.hasEntry(fn("file")));
  }

  @Test
  public void testRevertingClearsAllPendingChanges() {
    myVcs.createFile(fn("file1"), "");
    myVcs.commit();

    myVcs.createFile(fn("file2"), "");
    assertFalse(myVcs.isClean());

    myVcs.revert();
    assertTrue(myVcs.isClean());
  }

  @Test
  public void testRevertingWhenNoPreviousVersions() {
    try {
      myVcs.revert();
      myVcs.revert();
    } catch (Exception e) {
      fail(e.toString());
    }
  }

  @Test
  public void testClearingChangesAfterRevertWhenNoPreviousVersions() {
    myVcs.createFile(fn("file"), "");
    assertFalse(myVcs.isClean());

    myVcs.revert();
    assertTrue(myVcs.isClean());
  }

  @Test
  public void testGettingSnapshots() {
    myVcs.createFile(fn("file1"), "content1");
    myVcs.createFile(fn("file2"), "content2");
    myVcs.commit();

    myVcs.createFile(fn("file3"), "content3");
    myVcs.changeFile(fn("file1"), "new content1");
    myVcs.commit();

    Integer id1 = myVcs.getEntry(fn("file1")).getObjectId();
    Integer id2 = myVcs.getEntry(fn("file2")).getObjectId();
    Integer id3 = myVcs.getEntry(fn("file3")).getObjectId();

    List<Snapshot> snapshots = myVcs.getSnapshots();
    assertEquals(2, snapshots.size());

    assertElements(
        new Object[]{
            new FileEntry(id1, "file1", "new content1"),
            new FileEntry(id2, "file2", "content2"),
            new FileEntry(id3, "file3", "content3")},
        snapshots.get(0).getEntries());

    assertElements(
        new Object[]{
            new FileEntry(id1, "file1", "content1"),
            new FileEntry(id2, "file2", "content2")},
        snapshots.get(1).getEntries());
  }

  @Test
  public void testGettingSnapshotsOnCleanVcs() {
    assertTrue(myVcs.getSnapshots().isEmpty());
  }

  @Test
  public void testGettingLabeledSnapshot() {
    myVcs.createFile(fn("file"), "content");
    myVcs.commit();

    myVcs.putLabel("label");

    myVcs.changeFile(fn("file"), "new content");
    myVcs.commit();

    Snapshot s = myVcs.getSnapshot("label");
    assertNotNull(s);
    assertRevisionContent("content", s.getEntry(fn("file")));
  }

  @Test
  public void testGettingSnapshotWithUnknownLabel() {
    myVcs.createFile(fn("file"), "content");
    myVcs.commit();
    myVcs.putLabel("label");

    assertNull(myVcs.getSnapshot("unknown label"));
  }
}