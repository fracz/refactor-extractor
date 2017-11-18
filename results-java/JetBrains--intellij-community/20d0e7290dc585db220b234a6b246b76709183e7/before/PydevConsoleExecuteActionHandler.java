package com.jetbrains.python.console;

import com.intellij.execution.console.LanguageConsoleImpl;
import com.intellij.execution.console.LanguageConsoleViewImpl;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ConsoleExecuteActionHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.impl.source.codeStyle.HelperFactory;
import com.intellij.psi.impl.source.codeStyle.IndentHelper;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.console.pydev.ConsoleCommunication;
import com.jetbrains.python.console.pydev.ICallback;
import com.jetbrains.python.console.pydev.InterpreterResponse;

/**
 * @author traff
 */
public class PydevConsoleExecuteActionHandler extends ConsoleExecuteActionHandler {
  private static final String DOUBLE_QUOTE_MULTILINE = "\"\"\"";
  private static final String SINGLE_QUOTE_MULTILINE = "'''";

  private final LanguageConsoleViewImpl myConsoleView;

  private String myInMultilineStringState = null;
  private StringBuilder myInputBuffer;
  private int myCurrentIndentSize = -1;
  private IndentHelper myIndentHelper;

  private final ConsoleCommunication myConsoleCommunication;

  public PydevConsoleExecuteActionHandler(LanguageConsoleViewImpl consoleView,
                                          ProcessHandler myProcessHandler,
                                          ConsoleCommunication consoleCommunication) {
    super(myProcessHandler);
    myConsoleView = consoleView;
    myConsoleCommunication = consoleCommunication;
    myIndentHelper = HelperFactory.createHelper(PythonFileType.INSTANCE, consoleView.getConsole().getProject());
  }

  @Override
  public void processLine(final String line) {
    final LanguageConsoleImpl console = myConsoleView.getConsole();
    final Editor currentEditor = console.getCurrentEditor();

    if (myInputBuffer == null) {
      myInputBuffer = new StringBuilder();
    }
    myInputBuffer.append(line).append("\n");

    // multiline strings handling
    if (myInMultilineStringState != null) {
      if (line.contains(myInMultilineStringState)) {
        myInMultilineStringState = null;
        // restore language
        console.setLanguage(PythonLanguage.getInstance());
        console.setPrompt(PyConsoleHighlightingUtil.ORDINARY_PROMPT);
      }
      else {
        return;
      }
    }
    else {
      if (line.contains(DOUBLE_QUOTE_MULTILINE)) {
        myInMultilineStringState = DOUBLE_QUOTE_MULTILINE;
      }
      else if (line.contains(SINGLE_QUOTE_MULTILINE)) {
        myInMultilineStringState = SINGLE_QUOTE_MULTILINE;
      }
      if (myInMultilineStringState != null) {
        // change language
        console.setLanguage(PlainTextLanguage.INSTANCE);
        console.setPrompt(PyConsoleHighlightingUtil.INDENT_PROMPT);
        return;
      }
    }

    // Process line continuation
    if (line.endsWith("\\")) {
      console.setPrompt(PyConsoleHighlightingUtil.INDENT_PROMPT);
      return;
    }

    if (myCurrentIndentSize != -1) {
      final int indent = myIndentHelper.getIndent(line, false);
      if (indent >= myCurrentIndentSize) {
        indentEditor(currentEditor, indent);
        scrollDown(currentEditor);
        return;
      }
    }

    if (myConsoleCommunication != null) {
      final boolean waitedForInputBefore = myConsoleCommunication.isWaitingForInput();
      myConsoleCommunication.execInterpreter(myInputBuffer.toString(), new ICallback<Object, InterpreterResponse>() {
        public Object call(final InterpreterResponse interpreterResponse) {
          // clear
          myInputBuffer = null;
          // Handle prompt
          if (interpreterResponse.need_input) {
            if (!PyConsoleHighlightingUtil.INPUT_PROMPT.equals(console.getPrompt())) {
              console.setPrompt(PyConsoleHighlightingUtil.INPUT_PROMPT);
              scrollDown(currentEditor);
            }
            myCurrentIndentSize = -1;
          }
          else if (interpreterResponse.more) {
            if (!PyConsoleHighlightingUtil.INDENT_PROMPT.equals(console.getPrompt())) {
              console.setPrompt(PyConsoleHighlightingUtil.INDENT_PROMPT);
              scrollDown(currentEditor);
            }
            if (myCurrentIndentSize == -1) {
              // compute current indentation
              myCurrentIndentSize = myIndentHelper.getIndent(line, false) + getPythonIndent();
              // In this case we can insert indent automatically
              indentEditor(currentEditor, myCurrentIndentSize);
            }
          }
          else {
            if (!PyConsoleHighlightingUtil.ORDINARY_PROMPT.equals(console.getPrompt())) {
              console.setPrompt(PyConsoleHighlightingUtil.ORDINARY_PROMPT);
              scrollDown(currentEditor);
            }
            myCurrentIndentSize = -1;
          }

          // Handle output
          if (!StringUtil.isEmpty(interpreterResponse.err)) {
            PyConsoleHighlightingUtil.processOutput(console, interpreterResponse.err, ProcessOutputTypes.STDERR);
          }
          else if (!StringUtil.isEmpty(interpreterResponse.out)) {
            PyConsoleHighlightingUtil.processOutput(console, interpreterResponse.out, ProcessOutputTypes.STDOUT);
          }
          scrollDown(currentEditor);
          return null;
        }
      });
      // After requesting input we got no call back to change prompt, change it manually
      if (waitedForInputBefore && !myConsoleCommunication.isWaitingForInput()) {
        console.setPrompt(PyConsoleHighlightingUtil.ORDINARY_PROMPT);
        scrollDown(currentEditor);
      }
    }
  }

  private int getPythonIndent() {
    return CodeStyleSettingsManager.getSettings(getProject()).getIndentSize(PythonFileType.INSTANCE);
  }

  private void indentEditor(final Editor editor, final int indentSize) {
    new WriteCommandAction(getProject()) {
      @Override
      protected void run(Result result) throws Throwable {
        EditorModificationUtil.insertStringAtCaret(editor, myIndentHelper.fillIndent(indentSize));
      }
    }.execute();
  }

  private Project getProject() {
    return myConsoleView.getConsole().getProject();
  }

  private void scrollDown(final Editor currentEditor) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        currentEditor.getCaretModel().moveToOffset(currentEditor.getDocument().getTextLength());
      }
    });
  }
}