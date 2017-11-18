package com.intellij.localvcs.integration.ui.models;

import com.intellij.localvcs.core.LocalVcs;
import com.intellij.localvcs.core.LocalVcsTestCase;
import com.intellij.localvcs.core.TestLocalVcs;
import static com.intellij.localvcs.core.revisions.Difference.Kind.CREATED;
import static com.intellij.localvcs.core.revisions.Difference.Kind.NOT_MODIFIED;
import com.intellij.localvcs.integration.TestIdeaGateway;
import com.intellij.localvcs.integration.TestVirtualFile;
import org.junit.Test;

public class DirectoryHistoryDialogModelTest extends LocalVcsTestCase {
  private LocalVcs vcs = new TestLocalVcs();
  private DirectoryHistoryDialogModel m;

  @Test
  public void testTitle() {
    TestVirtualFile parent = new TestVirtualFile("parent", null, -1);
    TestVirtualFile file = new TestVirtualFile("file", null, -1);
    parent.addChild(file);

    m = new DirectoryHistoryDialogModel(file, vcs, null);

    assertEquals("parent/file", m.getTitle());
  }

  @Test
  public void testNoDifference() {
    vcs.createDirectory("dir");
    initModelFor("dir");

    assertEquals(1, m.getRevisions().size());

    m.selectRevisions(0, 0);
    DirectoryDifferenceModel nm = m.getRootDifferenceNodeModel();

    assertEquals(NOT_MODIFIED, nm.getDifferenceKind());
    assertEquals(0, nm.getChildren().size());
  }

  @Test
  public void testDifference() {
    vcs.createDirectory("dir");
    vcs.createFile("dir/file", null, -1);

    initModelFor("dir");

    assertEquals(2, m.getRevisions().size());

    m.selectRevisions(0, 1);
    DirectoryDifferenceModel nm = m.getRootDifferenceNodeModel();
    assertEquals(1, nm.getChildren().size());

    nm = nm.getChildren().get(0);
    assertEquals(CREATED, nm.getDifferenceKind());
  }

  private void initModelFor(String path) {
    m = new DirectoryHistoryDialogModel(new TestVirtualFile(path), vcs, new TestIdeaGateway());
  }
}