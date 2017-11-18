package ru.compscicenter.edide;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorImpl;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.*;
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
    private FileEditor defaultEditor;
    final JComponent comp;

    private String getTextForTask(VirtualFile file) {
        int taskNum = TaskManager.getInstance().getTaskNumForFile(file.getName());
        return TaskManager.getInstance().getTaskText(taskNum);
    }

    public StudyEditor(Project project, VirtualFile file) {
        defaultEditor = TextEditorProvider.getInstance().createEditor(project, file);
        comp = defaultEditor.getComponent();
        JLabel taskText = new JLabel("<html>" + getTextForTask(file) + "<br>" + "some text </html>");
        taskText.setFont(new Font("Arial", Font.PLAIN, 16));
        comp.add(taskText, BorderLayout.NORTH);
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
}