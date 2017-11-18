package ru.compscicenter.edide;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * User: lia
 * Date: 23.05.14
 * Time: 20:33
 */
class CheckAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getProject();
    FileDocumentManager.getInstance().saveAllDocuments();
    TaskManager tm = TaskManager.getInstance();
    if (!(project != null && project.isOpen())) {
      return;
    }
    String basePath = project.getBasePath();
    if (basePath == null) return;
    Editor editor = StudyEditor.getRecentOpenedEditor(project);
    if (editor == null) {
      return;
    }
    VirtualFile vfOpenedFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
    //TODO: replace with platform independent path join
    String testFile = basePath +
                      "/.idea/" + tm.getTest(tm.getTaskNumForFile(vfOpenedFile != null ? vfOpenedFile.getName() : null));
    GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setWorkDirectory(basePath + "/.idea");
    cmd.setExePath("python");
    cmd.addParameter(testFile);
    try {
      Process p = cmd.createProcess();
      InputStream is_err = p.getErrorStream();
      InputStream is = p.getInputStream();
      BufferedReader bf = new BufferedReader(new InputStreamReader(is));
      BufferedReader bf_err = new BufferedReader(new InputStreamReader(is_err));
      String line;
      String testResult = "test failed";
      while ((line = bf.readLine()) != null) {
        if (line.equals("OK")) {
          testResult = "test passed";
        }
        System.out.println(line);
      }
      while ((line = bf_err.readLine()) != null) {
        if (line.equals("OK")) {
          testResult = "test passed";
        }
        System.out.println(line);
      }
      JOptionPane.showMessageDialog(null, testResult, "", JOptionPane.INFORMATION_MESSAGE);
    }
    catch (ExecutionException e1) {
      e1.printStackTrace();
    }
    catch (IOException e1) {
      e1.printStackTrace();
    }
  }
}