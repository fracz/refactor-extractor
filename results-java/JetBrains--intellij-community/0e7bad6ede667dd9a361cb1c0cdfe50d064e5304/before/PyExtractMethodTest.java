package com.jetbrains.python.refactoring;

import com.intellij.lang.LanguageRefactoringSupport;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.refactoring.RefactoringActionHandler;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.fixtures.LightMarkedTestCase;
import com.jetbrains.python.psi.LanguageLevel;
import com.jetbrains.python.refactoring.extractmethod.PyExtractMethodUtil;

/**
 * @author oleg
 */
public class PyExtractMethodTest extends LightMarkedTestCase {
  private void doTest(String newName, LanguageLevel level) {
    setLanguageLevel(level);
    try {
      doTest(newName);
    }
    finally {
      setLanguageLevel(null);
    }
  }

  private void doTest(String newName) {
    final String testName = getTestName(false);
    final String beforeName = testName + ".before.py";
    final String afterName = testName + ".after.py";
    final String dir = "refactoring/extractmethod/";

    myFixture.configureByFile(dir + beforeName);
    final RefactoringSupportProvider provider = LanguageRefactoringSupport.INSTANCE.forLanguage(PythonLanguage.getInstance());
    assertNotNull(provider);
    final RefactoringActionHandler handler = provider.getExtractMethodHandler();
    assertNotNull(handler);
    final Editor editor = myFixture.getEditor();
    assertInstanceOf(editor, EditorEx.class);
    System.setProperty(PyExtractMethodUtil.NAME, newName);
    try {
      handler.invoke(myFixture.getProject(), editor, myFixture.getFile(), ((EditorEx)editor).getDataContext());
    }
    finally {
      System.clearProperty(PyExtractMethodUtil.NAME);
    }
    myFixture.checkResultByFile(dir + afterName);
  }

  private void doFail(String newName, String message) {
    try {
      doTest(newName);
    }
    catch (Exception e) {
      assertEquals(message, e.getMessage());
      return;
    }
    fail("No exception was thrown");
  }

  public void testParameter() {
    doTest("bar");
  }

  public void testBreakAst() {
    doTest("bar");
  }

  public void testExpression() {
    doTest("plus");
  }

  public void testStatement() {
    doTest("foo");
  }

  public void testStatements() {
    doTest("foo");
  }

  public void testStatementReturn() {
    doTest("foo");
  }

  public void testBinaryExpression() {
    doTest("foo");
  }

  public void testWhileOutput() {
    doTest("bar");
  }

  public void testNameCollisionClass() {
    doFail("hello", "Method name clashes with already existing name");
  }

  public void testNameCollisionFile() {
    doFail("hello", "Method name clashes with already existing name");
  }

  public void testNameCollisionSuperClass() {
    doFail("hello", "Method name clashes with already existing name");
  }

  public void testOutNotEmptyStatements() {
    doTest("sum_squares");
  }

  public void testOutNotEmptyStatements2() {
    doTest("sum_squares");
  }

  // PY-2903
  public void _testComment() {
    doTest("bar");
  }

  public void testFile() {
    doTest("bar");
  }

  public void testMethodContext() {
    doTest("bar");
  }

  public void testMethodIndent() {
    doTest("bar");
  }

  public void testMethodReturn() {
    doTest("bar");
  }

  public void testWrongSelectionIfPart() {
    doFail("bar", "Cannot perform extract method using selected element(s)");
  }

  public void testWrongSelectionFromImportStar() {
    doFail("bar", "Cannot perform refactoring with from import statement inside code block");
  }

  public void testPy479() {
    doTest("bar");
  }

  public void testClassContext() {
    doTest("bar");
  }

  public void testConditionalReturn() {
    doFail("bar", "Cannot perform refactoring when execution flow is interrupted");
  }

  public void testReturnTuple() {
    doTest("bar");
  }

  public void testComment2() {
    doTest("baz");
  }

  public void testElseBody() {
    doTest("baz");
  }

  public void testClassMethod() {
    doTest("baz");
  }

  public void testStaticMethod() {
    doTest("baz");
  }

  // PY-5123
  public void testMethodInIf() {
    doTest("baz");
  }

  // PY-6081
  public void testLocalVarDefinedBeforeModifiedInside() {
    doTest("bar");
  }

  // PY-6391
  public void testDefinedBeforeAccessedAfter() {
    doTest("bar");
  }

  // PY-5865
  public void testSingleRaise() {
    doTest("bar");
  }

  // PY-4156
  public void testLocalFunction() {
    doTest("bar");
  }

  // PY-6413
  public void testTryFinally() {
    doTest("bar");
  }

  // PY-6414
  public void testTryContext() {
    doTest("bar");
  }

  // PY-6416
  public void testCommentAfterSelection() {
    doTest("bar");
  }

  // PY-6417
  public void testGlobalVarAssignment() {
    doTest("bar");
  }

  // PY-6619
  public void testGlobalToplevelAssignment() {
    doTest("bar");
  }

  // PY-6623
  public void testForLoopContinue() {
    doFail("bar", "Cannot perform refactoring when execution flow is interrupted");
  }

  // PY-6622
  public void testClassWithoutInit() {
    doTest("bar");
  }

  // PY-6625
  public void testNonlocal() {
    doTest("baz", LanguageLevel.PYTHON30);
  }

  // PY-7381
  public void testYield() {
    doFail("bar", "Cannot perform refactoring with 'yield' statement inside code block");
  }
}