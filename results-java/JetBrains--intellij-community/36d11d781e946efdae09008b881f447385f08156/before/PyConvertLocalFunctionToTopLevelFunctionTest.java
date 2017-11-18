/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.refactoring;

import com.intellij.testFramework.TestActionEvent;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.fixtures.PyTestCase;
import com.jetbrains.python.psi.LanguageLevel;
import com.jetbrains.python.refactoring.convertTopLevelFunction.PyConvertLocalFunctionToTopLevelFunctionAction;
import org.jetbrains.annotations.Nullable;

/**
 * @author Mikhail Golubev
 */
public class PyConvertLocalFunctionToTopLevelFunctionTest extends PyTestCase {

  public void doTest(boolean enabled, @Nullable String message) {
    myFixture.configureByFile(getTestName(true) + ".py");
    final PyConvertLocalFunctionToTopLevelFunctionAction action = new PyConvertLocalFunctionToTopLevelFunctionAction();
    // Similar to com.intellij.testFramework.fixtures.CodeInsightTestFixture.testAction()
    final TestActionEvent event = new TestActionEvent(action);
    action.beforeActionPerformedUpdate(event);
    assertEquals(enabled, event.getPresentation().isEnabledAndVisible());
    if (enabled) {
      try {
        action.actionPerformed(event);
        myFixture.checkResultByFile(getTestName(true) + ".after.py");
      }
      catch (IncorrectOperationException e) {
        if (message == null) {
          fail("Refactoring wasn't expected to fail");
        }
        assertEquals(message, e.getMessage());
      }
    }
  }

  private void doTest() {
    doTest(true, null);
  }

  public boolean isActionEnabled() {
    final PyConvertLocalFunctionToTopLevelFunctionAction action = new PyConvertLocalFunctionToTopLevelFunctionAction();
    final TestActionEvent event = new TestActionEvent(action);
    action.beforeActionPerformedUpdate(event);
    return event.getPresentation().isEnabled();
  }

  // PY-6637
  public void testSimple() {
    doTest();
  }

  // PY-6637
  public void testLocalFunctionDetection() {
    myFixture.configureByFile(getTestName(true) + ".py");
    moveByText("func");
    assertFalse(isActionEnabled());
    moveByText("local");
    assertTrue(isActionEnabled());
  }

  // PY-6637
  public void testNonlocalReferenceToOuterScope() {
    runWithLanguageLevel(LanguageLevel.PYTHON30, new Runnable() {
      @Override
      public void run() {
        doTest(true, PyBundle.message("refactoring.make.function.top.level.error.nonlocal.writes"));
      }
    });
  }

  // PY-6637
  public void testReferenceToSelf() {
    doTest(true, PyBundle.message("refactoring.make.function.top.level.error.self.reads", "self"));
  }

  // PY-6637
  public void testNonlocalReferencesInInnerFunction() {
    runWithLanguageLevel(LanguageLevel.PYTHON30, new Runnable() {
      @Override
      public void run() {
        doTest();
      }
    });
  }

  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + "/refactoring/convertTopLevel/";
  }
}