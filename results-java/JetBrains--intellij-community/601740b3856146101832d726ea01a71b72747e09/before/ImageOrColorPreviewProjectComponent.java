package com.intellij.html.preview;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.css.CssFile;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author spleaner
 */
public class ImageOrColorPreviewProjectComponent extends AbstractProjectComponent {

  public ImageOrColorPreviewProjectComponent(final Project project) {
    super(project);
  }

  public void projectOpened() {
    FileEditorManager.getInstance(myProject).addFileEditorManagerListener(new MyFileEditorManagerListener(), myProject);
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "ImageOrColorPreviewComponent";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  private static class MyFileEditorManagerListener implements FileEditorManagerListener {
    public void fileOpened(final FileEditorManager source, final VirtualFile file) {
      if (isSuitable(source.getProject(), file)) {
        final FileEditor[] fileEditors = source.getEditors(file);
        for (final FileEditor each : fileEditors) {
          if (each instanceof TextEditor) {
            Disposer.register(each, new ImageOrColorPreviewManager((TextEditor)each));
          }
        }
      }
    }

    private static boolean isSuitable(final Project project, final VirtualFile file) {
      final FileViewProvider provider = PsiManager.getInstance(project).findViewProvider(file);
      if (provider == null) return false;

      for (final PsiFile psiFile : provider.getAllFiles()) {
        if (psiFile instanceof XmlFile || psiFile instanceof CssFile || psiFile instanceof PsiJavaFile) return true;
      }

      return false;
    }

    public void fileClosed(final FileEditorManager source, final VirtualFile file) {
    }

    public void selectionChanged(final FileEditorManagerEvent event) {
    }
  }


}