package com.intellij.localvcsintegr.ui;

import com.intellij.localvcs.integration.ui.actions.LocalHistoryAction;
import com.intellij.localvcs.integration.ui.actions.ShowHistoryAction;
import com.intellij.localvcs.integration.ui.actions.ShowSelectionHistoryAction;
import com.intellij.localvcsintegr.IntegrationTestCase;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class LocalHistoryActionsTest extends IntegrationTestCase {
  VirtualFile f;
  Editor editor;
  Document document;

  @Override
  protected void setUpInWriteAction() throws Exception {
    super.setUpInWriteAction();
    f = root.createChildData(null, "f.java");

    document = FileDocumentManager.getInstance().getDocument(f);
    document.setText("foo");

    editor = getEditorFactory().createEditor(document);
  }

  @Override
  protected void tearDown() throws Exception {
    getEditorFactory().releaseEditor(editor);
    super.tearDown();
  }

  private EditorFactory getEditorFactory() {
    return EditorFactory.getInstance();
  }

  public void testShowHistoryAction() throws IOException {
    ShowHistoryAction a = new ShowHistoryAction();
    assertStatus(a, root, true);
    assertStatus(a, f, true);
    assertStatus(a, null, false);

    VirtualFile ignored = root.createChildData(null, "f.xxx");
    VirtualFile notUnderContentRoot = root.createChildData(null, "CVS");

    assertStatus(a, ignored, false);
    assertStatus(a, notUnderContentRoot, false);
  }

  public void testLocalHistoryActionDisabledWithoutProject() throws IOException {
    LocalHistoryAction a = new LocalHistoryAction() {
      public void actionPerformed(AnActionEvent e) {
      }
    };
    assertStatus(a, root, myProject, true);
    assertStatus(a, root, null, false);
  }

  public void testShowSelectionHistoryActionForSelection() throws Exception {
    editor.getSelectionModel().setSelection(0, 2);

    ShowSelectionHistoryAction a = new ShowSelectionHistoryAction();
    AnActionEvent e = createEventFor(a, f, myProject);
    a.update(e);

    assertTrue(e.getPresentation().isEnabled());

    assertEquals("Show History for Selection", e.getPresentation().getText());
  }

  public void testShowSelectionHistoryActionDisabledForNonFiles() throws IOException {
    ShowSelectionHistoryAction a = new ShowSelectionHistoryAction();
    assertStatus(a, root, false);
    assertStatus(a, null, false);
  }

  public void testShowSelectionHistoryActionDisabledForEmptySelection() throws Exception {
    ShowSelectionHistoryAction a = new ShowSelectionHistoryAction();
    assertStatus(a, f, false);
  }

  private void assertStatus(AnAction a, VirtualFile f, boolean isEnabled) {
    assertStatus(a, f, myProject, isEnabled);
  }

  private void assertStatus(AnAction a, VirtualFile f, Project p, boolean isEnabled) {
    AnActionEvent e = createEventFor(a, f, p);
    a.update(e);
    assertEquals(isEnabled, e.getPresentation().isEnabled());
  }

  private AnActionEvent createEventFor(AnAction a, final VirtualFile f, final Project p) {
    DataContext dc = new DataContext() {
      @Nullable
      public Object getData(String id) {
        if (id.equals(DataKeys.VIRTUAL_FILE.getName())) return f;
        if (id.equals(DataKeys.EDITOR.getName())) return editor;
        if (id.equals(DataKeys.PROJECT.getName())) return p;
        return null;
      }
    };
    return new AnActionEvent(null, dc, "", a.getTemplatePresentation(), null, -1);
  }
}