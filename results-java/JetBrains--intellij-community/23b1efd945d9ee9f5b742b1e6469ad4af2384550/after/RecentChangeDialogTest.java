package com.intellij.localvcsintegr;

import com.intellij.localvcs.core.revisions.RecentChange;
import com.intellij.localvcs.integration.revertion.Reverter;
import com.intellij.localvcs.integration.ui.models.RecentChangeDialogModel;
import com.intellij.localvcs.integration.ui.views.RecentChangeDialog;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;

public class RecentChangeDialogTest extends IntegrationTestCase {
  public void testHistoryDialogsWork() throws IOException {
    getVcs().beginChangeSet();
    root.createChildData(null, "f.java");
    getVcs().endChangeSet("change");

    RecentChange c = getVcs().getRecentChanges().get(0);
    RecentChangeDialog d = null;

    try {
      d = new RecentChangeDialog(gateway, c);
    }
    finally {
      if (d != null) d.dispose();
    }
  }

  public void testRevertChange() throws IOException {
    getVcs().beginChangeSet();
    VirtualFile f = root.createChildData(null, "f1.java");
    getVcs().endChangeSet("change");

    getVcs().beginChangeSet();
    f.rename(null, "f2.java");
    getVcs().endChangeSet("another change");

    RecentChange c = getVcs().getRecentChanges().get(1);
    RecentChangeDialogModel m = new RecentChangeDialogModel(gateway, getVcs(), c);

    Reverter r = m.createReverter();
    r.revert();

    assertNull(root.findChild("f1.java"));
    assertNull(root.findChild("f2.java"));
  }
}