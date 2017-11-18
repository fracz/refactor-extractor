package com.jetbrains.python.refactoring;

import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.testFramework.TestDataPath;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.refactoring.introduce.IntroduceHandler;
import com.jetbrains.python.refactoring.introduce.variable.PyIntroduceVariableHandler;

import java.util.Collection;

/**
 * @author yole
 */
@TestDataPath("$CONTENT_ROOT/../testData/refactoring/introduceVariable/")
public class PyIntroduceVariableTest extends PyIntroduceTestCase {
  public void testSimple() {
    doTest();
  }

  public void testPy995() {
    doTest();
  }

  public void testSkipLeadingWhitespace() {  // PY-1338
    doTest();
  }

  public void testPy2862() {
    doTest();
  }

  public void testMultilineString() {  // PY-4962
    doTest();
  }

  public void testSuggestKeywordArgumentName() {   // PY-1260
    doTestSuggestions(PyExpression.class, "extra_context");
  }

  public void testSuggestArgumentName() {   // PY-1260
    doTestSuggestions(PyExpression.class, "extra_context");
  }

  public void testSuggestTypeName() {  // PY-1336
    doTestSuggestions(PyCallExpression.class, "my_class");
  }

  public void testSuggestStringConstantValue() { // PY-1276
    doTestSuggestions(PyExpression.class, "foo_bar");
  }

  public void testDontSuggestBuiltinTypeNames() {  // PY-4474
    final Collection<String> strings = buildSuggestions(PyExpression.class);
    assertTrue(strings.contains("s"));
    assertFalse(strings.contains("str"));
  }

  public void testDontSuggestBuiltinTypeNames2() {  // PY-5626
    final Collection<String> strings = buildSuggestions(PyCallExpression.class);
    assertTrue(strings.contains("d"));
    assertFalse(strings.contains("dict"));
  }

  public void testSuggestNamesNotInScope() {  // PY-4605
    final Collection<String> strings = buildSuggestions(PyExpression.class);
    assertTrue(strings.contains("myfunc1"));
    assertFalse(strings.contains("myfunc"));
  }

  public void testIncorrectSelection() {  // PY-4455
    doTestCannotPerform();
  }

  public void testOneSidedSelection() {  // PY-4456
    doTestCannotPerform();
  }

  public void testFunctionOccurrences() {  // PY-5062
    doTest();
  }

  public void testBackslash() {  // PY-6908
    doTest();
  }

  public void testMultipartString() {  // PY-6698
    doTest();
  }

  // PY-3654
  public void testSimpleSubstring() {
    doTest();
  }

  // PY-3654
  public void testLeftSubstring() {
    doTest();
  }

  // PY-3654
  public void testRightSubstring() {
    doTest();
  }

  // PY-3654
  public void testMiddleSubstring() {
    doTest();
  }

  // PY-3654
  public void testLeftQuoteSubstring() {
    doTest();
  }

  // PY-3654
  public void testSubstringInExpression() {
    doTest();
  }

  // PY-3654
  public void testSubstringInStatement() {
    doTest();
  }

  // PY-3654
  public void testTripleQuotedSubstring() {
    doTest();
  }

  // PY-3654
  public void testSubstringInExpressionStatement() {
    doTest();
  }

  // PY-3654
  public void testBytesSubstring() {
    doTest();
  }

  // PY-3654
  public void testSubstringContainsFormatChars() {
    doTest();
  }

  // PY-3654
  public void testSubstringBreaksFormatChars() {
    doTestCannotPerform();
  }

  // PY-3654
  public void testSubstringContainsEscapes() {
    doTest();
  }

  // PY-3654
  public void testSubstringBreaksEscapes() {
    doTestCannotPerform();
  }

  private void doTestCannotPerform() {
    boolean thrownExpectedException = false;
    try {
      doTest();
    }
    catch (CommonRefactoringUtil.RefactoringErrorHintException e) {
      if (e.getMessage().equals("Cannot perform refactoring using selected element(s)")) {
        thrownExpectedException = true;
      }
    }
    assertTrue(thrownExpectedException);
  }

  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + "/refactoring/introduceVariable";
  }

  protected IntroduceHandler createHandler() {
    return new PyIntroduceVariableHandler();
  }
}