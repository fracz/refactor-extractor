package ru.compscicenter.edide;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

/**
* User: lia
* Date: 23.05.14
* Time: 14:16
*/
public class StudyEditor implements FileEditor {
  private final FileEditor defaultEditor;
  private final JComponent comp;
  private String getTextForTask(VirtualFile file, Project project) {
    //int taskNum = TaskManager.getInstance(project).getTaskNumForFile(file.getName());
    //return TaskManager.getInstance(project).getTaskText(taskNum);
    return "Sample text";
  }

  public StudyEditor(Project project, VirtualFile file) {
    defaultEditor = TextEditorProvider.getInstance().createEditor(project, file);
    comp = defaultEditor.getComponent();
    JLabel taskText = new JLabel(getTextForTask(file, project));
    taskText.setFont(new Font("Arial", Font.PLAIN, 16));
    comp.add(taskText, BorderLayout.NORTH);
  }

  public FileEditor getDefaultEditor() {
    return defaultEditor;
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return comp;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return comp;
  }

  @NotNull
  @Override
  public String getName() {
    return "Study Editor";
  }

  @NotNull
  @Override
  public FileEditorState getState(@NotNull FileEditorStateLevel level) {
    return defaultEditor.getState(level);
  }

  @Override
  public void setState(@NotNull FileEditorState state) {
    defaultEditor.setState(state);
  }

  @Override
  public boolean isModified() {
    return defaultEditor.isModified();
  }

  @Override
  public boolean isValid() {
    return defaultEditor.isValid();
  }

  @Override
  public void selectNotify() {
    defaultEditor.selectNotify();
  }

  @Override
  public void deselectNotify() {
    defaultEditor.deselectNotify();
  }

  @Override
  public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    defaultEditor.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    defaultEditor.removePropertyChangeListener(listener);
  }

  @Nullable
  @Override
  public BackgroundEditorHighlighter getBackgroundHighlighter() {
    return defaultEditor.getBackgroundHighlighter();
  }

  @Nullable
  @Override
  public FileEditorLocation getCurrentLocation() {
    return defaultEditor.getCurrentLocation();
  }

  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder() {
    return defaultEditor.getStructureViewBuilder();
  }

  @Override
  public void dispose() {
    defaultEditor.dispose();
  }

  @Nullable
  @Override
  public <T> T getUserData(@NotNull Key<T> key) {
    return defaultEditor.getUserData(key);
  }

  @Override
  public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
    defaultEditor.putUserData(key, value);
  }

  public static Editor getSelectedEditor(Project project) {
    Editor selectedEditor = null;
    FileEditor fileEditor =
      FileEditorManagerImpl.getInstanceEx(project).getSplitters().getCurrentWindow().getSelectedEditor().getSelectedEditorWithProvider()
        .getFirst();
    if (fileEditor instanceof StudyEditor) {
      FileEditor defaultEditor = ((StudyEditor)fileEditor).getDefaultEditor();
      if (defaultEditor instanceof PsiAwareTextEditorImpl) {
        selectedEditor = ((PsiAwareTextEditorImpl)defaultEditor).getEditor();
      }
    }
    return selectedEditor;
  }

}