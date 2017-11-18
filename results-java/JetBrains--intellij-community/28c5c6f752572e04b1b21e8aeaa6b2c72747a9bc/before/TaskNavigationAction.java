package ru.compscicenter.edide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import ru.compscicenter.edide.StudyTaskManager;
import ru.compscicenter.edide.StudyUtils;
import ru.compscicenter.edide.course.Course;
import ru.compscicenter.edide.course.Lesson;
import ru.compscicenter.edide.course.Task;
import ru.compscicenter.edide.course.TaskFile;
import ru.compscicenter.edide.editor.StudyEditor;

import javax.swing.*;
import java.util.Map;

/**
 * author: liana
 * data: 7/21/14.
 */
abstract public class TaskNavigationAction extends DumbAwareAction {
  public void navigateTask(Project project) {
    Editor selectedEditor = StudyEditor.getSelectedEditor(project);
    FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
    VirtualFile openedFile = fileDocumentManager.getFile(selectedEditor.getDocument());
    StudyTaskManager taskManager = StudyTaskManager.getInstance(selectedEditor.getProject());
    TaskFile selectedTaskFile = taskManager.getTaskFile(openedFile);
    Task currentTask = selectedTaskFile.getTask();
    Task nextTask = getTargetTask(currentTask);
    if (nextTask == null) {
      BalloonBuilder balloonBuilder =
        JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(getNavigationFinishedMessage(), MessageType.INFO, null);
      Balloon balloon = balloonBuilder.createBalloon();
      StudyEditor selectedStudyEditor = StudyEditor.getSelectedStudyEditor(project);
      balloon.showInCenterOf(getButton(selectedStudyEditor));
      return;
    }
    for (VirtualFile file : FileEditorManager.getInstance(project).getOpenFiles()) {
      FileEditorManager.getInstance(project).closeFile(file);
    }
    int nextTaskIndex = nextTask.getIndex();
    int lessonIndex = nextTask.getLesson().getIndex();
    TaskFile nextFile = nextTask.getTaskFiles().values().iterator().next();
    if (nextFile != null) {
      VirtualFile projectDir = project.getBaseDir();
      VirtualFile courseDir = projectDir.findChild(Course.COURSE_DIR);
      String lessonDirName = Lesson.LESSON_DIR + String.valueOf(lessonIndex + 1);
      if (courseDir != null) {
        VirtualFile lessonDir = courseDir.findChild(lessonDirName);
        if (lessonDir != null) {
          String taskDirName = Task.TASK_DIR + String.valueOf(nextTaskIndex + 1);
          VirtualFile taskDir = lessonDir.findChild(taskDirName);
          if (taskDir != null) {
            Map.Entry<String, TaskFile> taskFile = StudyUtils.getFirst(nextTask.getTaskFiles().entrySet());
            VirtualFile virtualFile = taskDir.findChild(taskFile.getKey());
            if (virtualFile != null) {
              FileEditorManager.getInstance(project).openFile(virtualFile, true);
            }
          }
        }
      }
    }
  }

  protected abstract JButton getButton(StudyEditor selectedStudyEditor);

  @Override
  public void actionPerformed(AnActionEvent e) {
    navigateTask(e.getProject());
  }

  protected abstract String getNavigationFinishedMessage();

  protected abstract Task getTargetTask(Task sourceTask);
}