package com.intellij.refactoring.inline;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiCall;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightCodeInsightTestCase;
import com.intellij.usageView.UsageInfo;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;

/**
 * @author yole
 */
public class InlineToAnonymousClassTest extends LightCodeInsightTestCase {
  private LanguageLevel myPreviousLanguageLevel;

  protected void setUp() throws Exception {
    super.setUp();
    myPreviousLanguageLevel = getPsiManager().getEffectiveLanguageLevel();
    getPsiManager().setEffectiveLanguageLevel(LanguageLevel.JDK_1_5);
  }

  protected void tearDown() throws Exception {
    getPsiManager().setEffectiveLanguageLevel(myPreviousLanguageLevel);
    super.tearDown();
  }

  protected ProjectJdk getProjectJDK() {
    return JavaSdkImpl.getMockJdk15("java 1.5");
  }

  public void testSimple() throws Exception {
    doTest(false);
  }

  public void testChangeToSuperType() throws Exception {
    doTest(false);
  }

  public void testImplementsInterface() throws Exception {
    doTest(false);
  }

  public void testClassInitializer() throws Exception {
    doTest(false);
  }

  public void testConstructor() throws Exception {
    doTest(false);
  }

  public void testConstructorWithArguments() throws Exception {
    doTest(false);
  }

  public void testConstructorWithArgumentsInExpression() throws Exception {
    doTest(false);
  }

  public void testMultipleConstructors() throws Exception {
    doTest(false);
  }

  public void testMethodUsage() throws Exception {
    doTest(false);
  }

  public void testConstructorArgumentToField() throws Exception {
    doTest(false);
  }

  public void testField() throws Exception {
    doTest(false);
  }

  public void testStaticConstantField() throws Exception {
    doTest(false);
  }

  public void testWritableInitializedField() throws Exception {
    doTest(false);
  }

  public void testNullInitializedField() throws Exception {
    doTest(false);
  }

  public void testInnerClass() throws Exception {
    doTest(false);
  }

  public void testConstructorToInstanceInitializer() throws Exception {
    doTest(false);
  }

  public void testNewExpressionContext() throws Exception {
    doTest(false);
  }

  public void testWritableFieldInitializedWithParameter() throws Exception {
    doTest(false);
  }

  public void testFieldInitializedWithVar() throws Exception {
    doTest(false);
  }

  public void testFieldVsLocalConflict() throws Exception {
    doTest(false);
  }

  public void testFieldVsParameterConflict() throws Exception {
    doTest(false);
  }

  public void testGenerics() throws Exception {
    doTest(false);
  }

  public void testGenericsSubstitute() throws Exception {
    doTest(false);
  }

  public void testGenericsFieldDeclaration() throws Exception {
    doTest(false);
  }

  public void testGenericsRawType() throws Exception {
    doTest(false);
  }

  public void testGenericsInTypeParameter() throws Exception {
    doTest(false);
  }

  public void testQualifyInner() throws Exception {
    doTest(false);
  }

  public void testQualifiedNew() throws Exception {
    doTest(false);
  }

  public void testChainedConstructors() throws Exception {
    doTest(false);
  }

  public void testInlineThisOnly() throws Exception {
    doTest(true);
  }

  public void testArrayType() throws Exception {
    doTest(false);
  }

  public void testArrayTypeWithGenerics() throws Exception {
    doTest(false);
  }

  public void testArrayInitializer() throws Exception {
    doTest(false);
  }

  public void testVarargs() throws Exception {
    doTest(false);
  }

  public void _testOuterClassFieldAccess() throws Exception {
    doTest(false);
  }

  public void testPrivateFieldUsedFromInnerClass() throws Exception {
    doTest(false);
  }

  public void testOverwriteInitializer() throws Exception {
    doTest(false);
  }

  public void testMultipleInnerClasses() throws Exception {
    doTest(false);
  }

  public void testNoInlineAbstract() throws Exception {
    doTestNoInline("Abstract classes cannot be inlined");
  }

  public void testNoInlineInterface() throws Exception {
    doTestNoInline("Interfaces cannot be inlined");
  }

  public void testNoInlineEnum() throws Exception {
    doTestNoInline("Enums cannot be inlined");
  }

  public void testNoInlineAnnotationType() throws Exception {
    doTestNoInline("Annotation types cannot be inlined");
  }

  public void testNoInlineWithSubclasses() throws Exception {
    doTestNoInline("Classes which have subclasses cannot be inlined");
  }

  public void testNoInlineMultipleInterfaces() throws Exception {
    doTestNoInline("Classes which implement multiple interfaces cannot be inlined");
  }

  public void testNoInlineSuperclassInterface() throws Exception {
    doTestNoInline("Classes which have a superclass and implement an interface cannot be inlined");
  }

  public void testNoInlineMethodUsage() throws Exception {
    doTestNoInline("Class cannot be inlined because it has usages of methods not inherited from its superclass or interface");
  }

  public void testNoInlineFieldUsage() throws Exception {
    doTestNoInline("Class cannot be inlined because it has usages of fields not inherited from its superclass");
  }

  public void testNoInlineStaticField() throws Exception {
    doTestNoInline("Class cannot be inlined because it has static fields with non-constant initializers");
  }

  public void testNoInlineStaticNonFinalField() throws Exception {
    doTestNoInline("Class cannot be inlined because it has static non-final fields");
  }

  public void testNoInlineStaticMethod() throws Exception {
    doTestNoInline("Class cannot be inlined because it has static methods");
  }

  public void testNoInlineStaticInitializer() throws Exception {
    doTestNoInline("Class cannot be inlined because it has static initializers");
  }

  public void testNoInlineClassLiteral() throws Exception {
    doTestPreprocessUsages("Class cannot be inlined because it has usages of its class literal");
  }

  public void testNoInlineCatchClause() throws Exception {
    doTestPreprocessUsages("Class cannot be inlined because it is used in a 'catch' clause");
  }

  public void testNoInlineThrowsClause() throws Exception {
    doTestPreprocessUsages("Class cannot be inlined because it is used in a 'throws' clause");
  }

  public void testNoInlineUnresolvedConstructor() throws Exception {
    doTestPreprocessUsages("Class cannot be inlined because a call to its constructor is unresolved");
  }

  public void testNoInlineUnresolvedConstructor2() throws Exception {
    doTestPreprocessUsages("Class cannot be inlined because a call to its constructor is unresolved");
  }

  public void testNoInlineStaticInnerClass() throws Exception {
    doTestNoInline("Class cannot be inlined because it has static inner classes");
  }

  public void testNoInlineReturnInConstructor() throws Exception {
    doTestNoInline("Class cannot be inlined because its constructor contains 'return' statements");
  }

  public void testNoInlineUnresolvedSuperclass() throws Exception {
    doTestNoInline("Class cannot be inlined because its superclass cannot be resolved");
  }

  public void testNoInlineUnresolvedInterface() throws Exception {
    doTestNoInline("Class cannot be inlined because an interface implemented by it cannot be resolved");
  }

  public void testNoInlineLibraryClass() throws Exception {
    doTestNoInline("Library classes cannot be inlined");
  }

  public void testNoInlineNoUsages() throws Exception {
    doTestPreprocessUsages("Class is never used");
  }

  public void testConflictInaccessibleOuterField() throws Exception {
    InlineToAnonymousClassProcessor processor = prepareProcessor();
    UsageInfo[] usages = processor.findUsages();
    ArrayList<String> conflicts = processor.getConflicts(usages);
    assertEquals(1, conflicts.size());
    assertEquals("Field <b><code>C2.a</code></b> that is used in inlined method is not accessible from call site(s) in method <b><code>C2User.test()</code></b>",
                 conflicts.get(0));
  }

  private void doTestNoInline(final String expectedMessage) throws Exception {
    String name = getTestName(false);
    @NonNls String fileName = "/refactoring/inlineToAnonymousClass/" + name + ".java";
    configureByFile(fileName);
    PsiElement element = TargetElementUtil.findTargetElement(myEditor,
            TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
    assertInstanceOf(element, PsiClass.class);

    String message = InlineToAnonymousClassHandler.getCannotInlineMessage((PsiClass) element);
    assertEquals(expectedMessage, message);
  }

  private void doTest(final boolean inlineThisOnly) throws Exception {
    String name = getTestName(false);
    @NonNls String fileName = "/refactoring/inlineToAnonymousClass/" + name + ".java";
    configureByFile(fileName);
    performAction(inlineThisOnly);
    checkResultByFile(null, fileName + ".after", true);
  }

  private void doTestPreprocessUsages(final String expectedMessage) throws Exception {
    final InlineToAnonymousClassProcessor processor = prepareProcessor();
    String message = processor.getPreprocessUsagesMessage(processor.findUsages());
    assertEquals(expectedMessage, message);
  }

  private InlineToAnonymousClassProcessor prepareProcessor() throws Exception {
    String name = getTestName(false);
    @NonNls String fileName = "/refactoring/inlineToAnonymousClass/" + name + ".java";
    configureByFile(fileName);
    PsiElement element = TargetElementUtil.findTargetElement(myEditor,
                                                             TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
    assertInstanceOf(element, PsiClass.class);

    assertEquals(null, InlineToAnonymousClassHandler.getCannotInlineMessage((PsiClass) element));
    return new InlineToAnonymousClassProcessor(getProject(), (PsiClass) element, null, false);
  }

  private void performAction(final boolean inlineThisOnly) {
    PsiElement element = TargetElementUtil.findTargetElement(myEditor,
            TargetElementUtil.ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
    PsiCall callToInline = InlineToAnonymousClassHandler.findCallToInline(myEditor);
    PsiClass classToInline = (PsiClass) element;
    assertEquals(null, InlineToAnonymousClassHandler.getCannotInlineMessage(classToInline));
    final InlineToAnonymousClassProcessor processor = new InlineToAnonymousClassProcessor(getProject(), classToInline, callToInline, inlineThisOnly);
    UsageInfo[] usages = processor.findUsages();
    ArrayList<String> conflicts = processor.getConflicts(usages);
    assertEquals(0, conflicts.size());
    processor.run();
  }
}