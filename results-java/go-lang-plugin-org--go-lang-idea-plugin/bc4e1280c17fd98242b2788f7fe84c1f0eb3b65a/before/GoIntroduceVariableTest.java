/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Mihai Toader, Florin Patan
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

package com.goide.refactor;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class GoIntroduceVariableTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return "testData/refactor/introduce-variable";
  }

  protected void doTest() {
    String testName = getTestName(true);
    myFixture.configureByFile(testName + ".go");
    new GoIntroduceVariableHandler().invoke(myFixture.getProject(), myFixture.getEditor(), myFixture.getFile(), null);
    myFixture.checkResultByFile(testName + "-after.go");
  }

  public void testCaretAfterRightParenthesis()      { doTest(); }
  public void testCaretOnRightParenthesis()         { doTest(); }
  public void testNameSuggestOnGetterFunction()     { doTest(); }
  public void testNameSuggestOnDefinedImportAlias() { doTest(); }
  public void testNameSuggestOnDefaultName()        { doTest(); }
  public void testNameSuggestOnParamName()          { doTest(); }

  public void testMultipleValueResult() {
    try {
      doTest();
    }
    catch (RuntimeException e) {
      assertEquals("Cannot perform refactoring.\n" +
                   "Expression fmt.Println() returns multiple values.", e.getMessage());
    }
  }
}