package com.intellij.spellchecker.inspection;

import com.intellij.spellchecker.inspections.java.LocalVariableNameWithMistakesInspection;

/**
 * Created by IntelliJ IDEA.
 * User: Ekaterina Shliakhovetskaja
 */
public class LocalVariableWithMistakesInspectionTest extends SpellcheckerInspectionTestCase {

   protected String getBasePath() {
    return "/plugins/spellchecker/core/testData/inspection/localVariableNameWithMistakes";
  }

  public void testJava() throws Throwable {
    doTest("SPITest3.java", new LocalVariableNameWithMistakesInspection());
  }

}