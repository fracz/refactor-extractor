package ru.compscicenter.edide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import ru.compscicenter.edide.StudyTaskManager;
import ru.compscicenter.edide.course.Task;
import ru.compscicenter.edide.course.TaskFile;
import ru.compscicenter.edide.editor.StudyEditor;

import javax.swing.*;
import java.awt.*;

public class WatchInputAction extends DumbAwareAction {

  private void initContentLabel(String header, String contentFileText, JPanel parentComponent, Font headerFont) {
    JLabel headerLabel = new JLabel(header);
    headerLabel.setFont(headerFont);
    parentComponent.add(headerLabel);
    parentComponent.add(new JSeparator(SwingConstants.HORIZONTAL));
    JLabel inputContentLabel = new JLabel(contentFileText);
    parentComponent.add(inputContentLabel);
  }

  private void initWatchInputComponent(String inputFileText, String outputFileText, JPanel component) {
    component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
    Font headerFont = new Font("Arial", Font.BOLD, 16);
    initContentLabel("input", inputFileText, component, headerFont);
    initContentLabel("output", outputFileText, component, headerFont);
  }

  public void showInput(Project project) {
    Editor selectedEditor = StudyEditor.getSelectedEditor(project);
    if (selectedEditor != null) {
      FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
      VirtualFile openedFile = fileDocumentManager.getFile(selectedEditor.getDocument());
      StudyTaskManager studyTaskManager = StudyTaskManager.getInstance(project);
      assert openedFile != null;
      TaskFile taskFile = studyTaskManager.getTaskFile(openedFile);
      assert taskFile != null;
      Task currentTask = taskFile.getTask();
      String inputFileText = currentTask.getResourceText(project, currentTask.getInput(), true);
      String outputFileText = currentTask.getResourceText(project, currentTask.getOutput(), true);
      JPanel myContentPanel = new JPanel();
      initWatchInputComponent(inputFileText, outputFileText, myContentPanel);
      final JBPopup hint =
        JBPopupFactory.getInstance().createComponentPopupBuilder(myContentPanel, myContentPanel)
          .setResizable(true)
          .setMovable(true)
          .setRequestFocus(true)
          .createPopup();
      StudyEditor selectedStudyEditor = StudyEditor.getSelectedStudyEditor(project);
      assert selectedStudyEditor != null;
      hint.showInCenterOf(selectedStudyEditor.getWatchInputButton());
    }
  }

  public void actionPerformed(AnActionEvent e) {
    showInput(e.getProject());
  }
}