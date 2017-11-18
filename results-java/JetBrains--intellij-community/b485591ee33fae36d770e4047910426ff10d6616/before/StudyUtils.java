package ru.compscicenter.edide;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.ui.UIUtil;
import ru.compscicenter.edide.editor.StudyEditor;
import ru.compscicenter.edide.ui.StudyToolWindowFactory;

import java.io.*;

/**
 * author: liana
 * data: 7/15/14.
 */
public class StudyUtils {
  public static void closeSilently(Closeable stream) {
    if (stream != null) {
      try {
        stream.close();
      }
      catch (IOException e) {
        // close silently
      }
    }
  }

  public static boolean isZip(String fileName) {
    return fileName.contains(".zip");
  }

  public static <T> T getFirst(Iterable<T> container) {
    return container.iterator().next();
  }

  public static String getFileText(String parentDir, String fileName, boolean wrapHTML) {
    File inputFile = new File(parentDir, fileName);
    StringBuilder taskText = new StringBuilder();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
      String line;
      while ((line = reader.readLine()) != null) {
        taskText.append(line).append("\n");
        if (wrapHTML) {
          taskText.append("<br>");
        }
      }
      return wrapHTML ? UIUtil.toHtml(taskText.toString()) : taskText.toString();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      StudyUtils.closeSilently(reader);
    }
    return null;
  }

  public static void updateAction(AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    presentation.setEnabled(false);
    Project project = e.getProject();
    if (project != null) {
      FileEditor[] editors = FileEditorManager.getInstance(project).getAllEditors();
      for (FileEditor editor : editors) {
        if (editor instanceof StudyEditor) {
          presentation.setEnabled(true);
        }
      }
    }
  }

  public static void updateStudyToolWindow(Project project) {
    ToolWindowManager.getInstance(project).getToolWindow(StudyToolWindowFactory.STUDY_TOOL_WINDOW).getContentManager().removeAllContents(false);
    StudyToolWindowFactory factory =  new StudyToolWindowFactory();
    factory.createToolWindowContent(project, ToolWindowManager.getInstance(project).getToolWindow(StudyToolWindowFactory.STUDY_TOOL_WINDOW));
  }
}