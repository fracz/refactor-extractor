package com.jetbrains.python.refactoring;

import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightPlatformTestCase;
import com.intellij.util.Consumer;
import com.jetbrains.python.fixtures.PyLightFixtureTestCase;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.refactoring.introduce.IntroduceHandler;
import com.jetbrains.python.refactoring.introduce.IntroduceOperation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author yole
 */
public abstract class PyIntroduceTestCase extends PyLightFixtureTestCase {
  protected void doTestSuggestions(Class<? extends PyExpression> parentClass, String... expectedNames) {
    final Collection<String> names = buildSuggestions(parentClass);
    for (String expectedName : expectedNames) {
      assertTrue(StringUtil.join(names, ", "), names.contains(expectedName));
    }
  }

  protected Collection<String> buildSuggestions(Class<? extends PyExpression> parentClass) {
    myFixture.configureByFile(getTestName(true) + ".py");
    IntroduceHandler handler = createHandler();
    PyExpression expr = PsiTreeUtil.getParentOfType(myFixture.getFile().findElementAt(myFixture.getEditor().getCaretModel().getOffset()),
                                                    parentClass);
    return handler.getSuggestedNames(expr);
  }

  protected abstract IntroduceHandler createHandler();

  protected void doTest() {
    doTest(null);
  }

  protected void doTest(@Nullable Consumer<IntroduceOperation> customization) {
    myFixture.configureByFile(getTestName(true) + ".py");
    boolean inplaceEnabled = myFixture.getEditor().getSettings().isVariableInplaceRenameEnabled();
    try {
      myFixture.getEditor().getSettings().setVariableInplaceRenameEnabled(false);
      IntroduceHandler handler = createHandler();
      final IntroduceOperation operation = new IntroduceOperation(myFixture.getProject(), myFixture.getEditor(), myFixture.getFile(),
                                                                  "a", false, false);
      operation.setReplaceAll(true);
      if (customization != null) {
        customization.consume(operation);
      }
      handler.performAction(operation);
      myFixture.checkResultByFile(getTestName(true) + ".after.py");
    }
    finally {
      myFixture.getEditor().getSettings().setVariableInplaceRenameEnabled(inplaceEnabled);
    }
  }

  protected void doTestInplace(@Nullable Consumer<IntroduceOperation> customization) {
    String name = getTestName(true);
    myFixture.configureByFile(name + ".py");
    final boolean enabled = myFixture.getEditor().getSettings().isVariableInplaceRenameEnabled();
    TemplateManagerImpl templateManager = (TemplateManagerImpl)TemplateManager.getInstance(LightPlatformTestCase.getProject());
    try {
      templateManager.setTemplateTesting(true);
      myFixture.getEditor().getSettings().setVariableInplaceRenameEnabled(true);

      IntroduceHandler handler = createHandler();
      final IntroduceOperation introduceOperation = new IntroduceOperation(myFixture.getProject(), myFixture.getEditor(), myFixture.getFile(), "a",
                                                                           false, false);
      introduceOperation.setReplaceAll(true);
      if (customization != null) {
        customization.consume(introduceOperation);
      }
      handler.performAction(introduceOperation);

      TemplateState state = TemplateManagerImpl.getTemplateState(myFixture.getEditor());
      assert state != null;
      state.gotoEnd(false);
      myFixture.checkResultByFile(name + ".after.py", true);
    }
    finally {
      myFixture.getEditor().getSettings().setVariableInplaceRenameEnabled(enabled);
      templateManager.setTemplateTesting(false);
    }
  }
}