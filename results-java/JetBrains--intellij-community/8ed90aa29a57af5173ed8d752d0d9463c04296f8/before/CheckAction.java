package ru.compscicenter.edide.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.jetbrains.python.sdk.PythonSdkType;
import org.jetbrains.annotations.NotNull;
import ru.compscicenter.edide.StudyDocumentListener;
import ru.compscicenter.edide.StudyTaskManager;
import ru.compscicenter.edide.StudyUtils;
import ru.compscicenter.edide.course.StudyStatus;
import ru.compscicenter.edide.course.Task;
import ru.compscicenter.edide.course.TaskFile;
import ru.compscicenter.edide.course.TaskWindow;
import ru.compscicenter.edide.editor.StudyEditor;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Map;

public class CheckAction extends DumbAwareAction {

  private static final Logger LOG = Logger.getInstance(CheckAction.class.getName());

  class StudyTestRunner {
    private static final String TEST_OK = "#study_plugin test OK";
    private static final String TEST_FAILED = "#study_plugin FAILED + ";
    private final Task myTask;
    private final VirtualFile myTaskDir;
    private int myTestPassed;

    StudyTestRunner(Task task, VirtualFile taskDir) {
      myTask = task;
      myTaskDir = taskDir;
    }

    public int getTestPassed() {
      return myTestPassed;
    }

    public void setTestPassed(int testPassed) {
      myTestPassed = testPassed;
    }

    Process launchTests(Project project, String executablePath) throws ExecutionException {
      Sdk sdk = PythonSdkType.findPythonSdk(ModuleManager.getInstance(project).getModules()[0]);
      File testRunner = new File(myTaskDir.getPath(), myTask.getTestFile());
      GeneralCommandLine commandLine = new GeneralCommandLine();
      commandLine.setWorkDirectory(myTaskDir.getPath());
      final Map<String, String> env = commandLine.getEnvironment();
      final VirtualFile courseDir = project.getBaseDir();
      if (courseDir != null)
        env.put("PYTHONPATH", courseDir.getPath());
      if (sdk != null) {
        String pythonPath = sdk.getHomePath();
        if (pythonPath != null) {
          commandLine.setExePath(pythonPath);
          commandLine.addParameter(testRunner.getPath());
          commandLine.addParameter(executablePath);
          return commandLine.createProcess();
        }
      }
      return null;
    }


    String getPassedTests(Process p) {
      InputStream testOutput = p.getInputStream();
      BufferedReader testOutputReader = new BufferedReader(new InputStreamReader(testOutput));
      String line;
      try {
        while ((line = testOutputReader.readLine()) != null) {
          if (line.equals(TEST_OK)) {
            myTestPassed++;
          }
          if (line.contains(TEST_FAILED)) {
             return line.substring(TEST_FAILED.length(), line.length());
          }
        }
      }
      catch (IOException e) {
        LOG.error(e);
      }
      finally {
        StudyUtils.closeSilently(testOutputReader);
      }
      return null;
    }

    boolean testsPassed(Process p) {
      return myTestPassed == myTask.getTestNum();
    }
  }

  public void check(@NotNull final Project project) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
          @Override
          public void run() {
            final Editor selectedEditor = StudyEditor.getSelectedEditor(project);
            if (selectedEditor != null) {
              final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
              final VirtualFile openedFile = fileDocumentManager.getFile(selectedEditor.getDocument());
              if (openedFile != null) {
                StudyTaskManager taskManager = StudyTaskManager.getInstance(project);
                final TaskFile selectedTaskFile = taskManager.getTaskFile(openedFile);
                if (selectedTaskFile != null) {
                  StudyUtils.flushWindows(selectedEditor.getDocument(),selectedTaskFile, openedFile);
                  FileDocumentManager.getInstance().saveAllDocuments();
                  final VirtualFile taskDir = openedFile.getParent();
                  Task currentTask = selectedTaskFile.getTask();
                  StudyRunAction runAction = (StudyRunAction)ActionManager.getInstance().getAction(StudyRunAction.ACTION_ID);
                  if (runAction != null) {
                    runAction.run(project);
                  }
                  final StudyTestRunner testRunner = new StudyTestRunner(currentTask, taskDir);
                  Process testProcess = null;
                  try {
                    testProcess = testRunner.launchTests(project, openedFile.getPath());
                  }
                  catch (ExecutionException e) {
                    LOG.error(e);
                  }
                  if (testProcess != null) {
                    final int testNum = currentTask.getTestNum();
                    String failedMessage = testRunner.getPassedTests(testProcess);
                    final int testPassed = testRunner.getTestPassed();
                    if (testPassed !=0 && failedMessage == null) {
                      currentTask.setStatus(StudyStatus.Solved);
                      StudyUtils.updateStudyToolWindow(project);
                      selectedTaskFile.drawAllWindows(selectedEditor);
                      ProjectView.getInstance(project).refresh();
                      createTestResultPopUp("Congratulations!", JBColor.GREEN, project);
                      return;
                    }

                    final TaskFile taskFileCopy = new TaskFile();
                    final VirtualFile copyWithAnswers = getCopyWithAnswers(taskDir, openedFile, selectedTaskFile, taskFileCopy);
                    for (final TaskWindow taskWindow : taskFileCopy.getTaskWindows()) {
                      check(project, taskWindow, copyWithAnswers, taskFileCopy, selectedTaskFile, selectedEditor.getDocument(), testRunner);
                    }
                    try {
                      copyWithAnswers.delete(this);
                    }
                    catch (IOException e) {
                      LOG.error(e);
                    }

                    if (failedMessage == null) {
                      failedMessage = "";
                    }
                    selectedTaskFile.drawAllWindows(selectedEditor);
                    String result = String.format("%d from %d tests failed\n %s", testNum - testPassed, testNum, failedMessage);
                    createTestResultPopUp(result, JBColor.RED, project);
                  }
                }
              }
            }
          }
        }, null, null);
      }
    });
  }

  private void check(Project project,
                     TaskWindow taskWindow,
                     VirtualFile answerFile,
                     TaskFile answerTaskFile,
                     TaskFile usersTaskFile,
                     Document usersDocument,
                     StudyTestRunner testRunner) {

    try {
      VirtualFile windowCopy = answerFile.copy(this, answerFile.getParent(), "window" + taskWindow.getIndex() + ".py");
      final FileDocumentManager documentManager = FileDocumentManager.getInstance();
      final Document windowDocument = documentManager.getDocument(windowCopy);
      if (windowDocument != null) {
        TaskFile windowTaskFile = new TaskFile();
        TaskFile.copy(answerTaskFile, windowTaskFile);
        StudyDocumentListener listener = new StudyDocumentListener(windowTaskFile);
        windowDocument.addDocumentListener(listener);
        int start = taskWindow.getRealStartOffset(windowDocument);
        int end = start + taskWindow.getLength();
        TaskWindow userTaskWindow = usersTaskFile.getTaskWindows().get(taskWindow.getIndex());
        int userStart = userTaskWindow.getRealStartOffset(usersDocument);
        int userEnd = userStart + userTaskWindow.getLength();
        String text = usersDocument.getText(new TextRange(userStart, userEnd));
        windowDocument.replaceString(start, end, text);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
          public void run() {
            documentManager.saveDocument(windowDocument);
          }
        });
        Process smartTestProcess = testRunner.launchTests(project, windowCopy.getPath());
        boolean res = testRunner.testsPassed(smartTestProcess);
        userTaskWindow.setStatus(res ? StudyStatus.Solved : StudyStatus.Failed);
        windowCopy.delete(this);
      }
    }
    catch (IOException e) {
      LOG.error(e);
    }
    catch (ExecutionException e) {
      e.printStackTrace();
    }
  }


  private VirtualFile getCopyWithAnswers(final VirtualFile taskDir,
                                         final VirtualFile file,
                                         final TaskFile source,
                                         TaskFile target) {
    VirtualFile copy = null;
    try {

      copy = file.copy(this, taskDir, "answers.py");
      final FileDocumentManager documentManager = FileDocumentManager.getInstance();
      final Document document = documentManager.getDocument(copy);
      if (document != null) {
        TaskFile.copy(source, target);
        StudyDocumentListener listener = new StudyDocumentListener(target);
        document.addDocumentListener(listener);
        for (TaskWindow taskWindow : target.getTaskWindows()) {
          final int start = taskWindow.getRealStartOffset(document);
          final int end = start + taskWindow.getLength();
          final String text = taskWindow.getPossibleAnswer();
          document.replaceString(start, end, text);
        }
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
          public void run() {
            documentManager.saveDocument(document);
          }
        });
      }
    }
    catch (IOException e) {
      LOG.error(e);
    }


    return copy;
  }

  private void createTestResultPopUp(final String text, Color color, @NotNull final Project project) {
    BalloonBuilder balloonBuilder =
      JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(text, null, color, null);
    Balloon balloon = balloonBuilder.createBalloon();
    StudyEditor studyEditor = StudyEditor.getSelectedStudyEditor(project);
    assert studyEditor != null;
    JButton checkButton = studyEditor.getCheckButton();
    balloon.showInCenterOf(checkButton);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getProject();
    if (project != null) {
      check(project);
    }
  }
}