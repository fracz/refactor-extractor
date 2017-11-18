/*
 * @author max
 */
package com.jetbrains.python;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubUpdatingIndex;
import com.intellij.testFramework.TestDataPath;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.python.fixtures.PyLightFixtureTestCase;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyFileImpl;
import com.jetbrains.python.psi.impl.PyQualifiedName;
import com.jetbrains.python.psi.stubs.PyClassStub;

import java.io.IOException;
import java.util.List;

@TestDataPath("$CONTENT_ROOT/../testData/stubs/")
public class PyStubsTest extends PyLightFixtureTestCase {
  private static final String PARSED_ERROR_MSG = "Operations should have been performed on stubs but caused file to be parsed";

  @Override
  protected String getTestDataPath() {
    return PythonTestUtil.getTestDataPath() + "/stubs/";
  }

  private static void assertNotParsed(PyFile file) {
    assertNull(PARSED_ERROR_MSG, ((PyFileImpl)file).getTreeElement());
  }

  public void testStubStructure() throws Exception {
    final PyFile file = getTestFile();
    final List<PyClass> classes = file.getTopLevelClasses();
    assertEquals(1, classes.size());
    PyClass pyClass = classes.get(0);
    assertEquals("FooClass", pyClass.getName());

    final PyTargetExpression[] attrs = pyClass.getClassAttributes();
    assertEquals(1, attrs.length);
    assertEquals("staticField", attrs [0].getName());

    final PyFunction[] methods = pyClass.getMethods();
    assertEquals(2, methods.length);
    assertEquals("__init__", methods [0].getName());
    assertEquals("fooFunction", methods [1].getName());

    // decorators
    PyFunction decorated = methods[1];
    PyDecoratorList decos = decorated.getDecoratorList();
    assertNotNull(decos);
    assertNotParsed(file);
    PyDecorator[] da = decos.getDecorators();
    assertNotNull(da);
    assertEquals(1, da.length);
    assertNotParsed(file);
    PyDecorator deco = da[0];
    assertNotNull(deco);
    assertEquals("deco", deco.getName());
    assertNotParsed(file);
    assertFalse(deco.isBuiltin());

    final PyTargetExpression[] instanceAttrs = pyClass.getInstanceAttributes();
    assertEquals(1, instanceAttrs.length);
    assertEquals("instanceField", instanceAttrs [0].getName());

    final List<PyFunction> functions = file.getTopLevelFunctions();
    assertEquals(2, functions.size()); // "deco" and "topLevelFunction"
    PyFunction func = functions.get(0);
    assertEquals("deco", func.getName());

    func = functions.get(1);
    assertEquals("topLevelFunction", func.getName());

    final List<PyTargetExpression> exprs = file.getTopLevelAttributes();
    assertEquals(2, exprs.size());
    assertEquals("top1", exprs.get(0).getName());
    assertEquals("top2", exprs.get(1).getName());

    assertNotParsed(file);
  }

  public void testLoadingDeeperTreeRemainsKnownPsiElement() throws Exception {
    final PyFile file = getTestFile();
    final List<PyClass> classes = file.getTopLevelClasses();
    assertEquals(1, classes.size());
    PyClass pyClass = classes.get(0);

    assertEquals("SomeClass", pyClass.getName());

    assertNotParsed(file);

    // load the tree now
    final PyStatementList statements = pyClass.getStatementList();
    assertNotNull(((PyFileImpl)file).getTreeElement());

    final PsiElement[] children = file.getChildren();

    assertEquals(1, children.length);
    assertSame(pyClass, children[0]);
  }

  public void testLoadingTreeRetainsKnownPsiElement() throws Exception {
    final PyFile file = getTestFile();
    final List<PyClass> classes = file.getTopLevelClasses();
    assertEquals(1, classes.size());
    PyClass pyClass = classes.get(0);

    assertEquals("SomeClass", pyClass.getName());

    assertNotParsed(file);

    final PsiElement[] children = file.getChildren(); // Load the tree

    assertNotNull(((PyFileImpl)file).getTreeElement());
    assertEquals(1, children.length);
    assertSame(pyClass, children[0]);
  }

  public void testRenamingUpdatesTheStub() throws Exception {
    final PyFile file = getTestFile("LoadingTreeRetainsKnownPsiElement.py");
    final List<PyClass> classes = file.getTopLevelClasses();
    assertEquals(1, classes.size());
    final PyClass pyClass = classes.get(0);

    assertEquals("SomeClass", pyClass.getName());

    // Ensure we haven't loaded the tree yet.
    final PyFileImpl fileImpl = (PyFileImpl)file;
    assertNull(fileImpl.getTreeElement());

    final PsiElement[] children = file.getChildren(); // Load the tree

    assertNotNull(fileImpl.getTreeElement());
    assertEquals(1, children.length);
    assertSame(pyClass, children[0]);

    new WriteCommandAction(myFixture.getProject(), fileImpl) {
      protected void run(final Result result) throws Throwable {
        pyClass.setName("RenamedClass");
        assertEquals("RenamedClass", pyClass.getName());
      }
    }.execute();

    StubElement fileStub = fileImpl.getStub();
    assertNull("There should be no stub if file holds tree element", fileStub);

    FileBasedIndex.getInstance().ensureUpToDate(StubUpdatingIndex.INDEX_ID, myFixture.getProject(), null);
    fileImpl.unloadContent();
    assertNull(fileImpl.getTreeElement()); // Test unload successed.

    fileStub = fileImpl.getStub();
    assertNotNull("After tree element have been unloaded we must be able to create updated stub", fileStub);

    final PyClassStub newclassstub = (PyClassStub)fileStub.getChildrenStubs().get(0);
    assertEquals("RenamedClass", newclassstub.getName());
  }

  public void testImportStatement() throws Exception {
    final PyFileImpl file = (PyFileImpl) getTestFile();

    final List<PyFromImportStatement> fromImports = file.getFromImports();
    assertEquals(1, fromImports.size());
    final PyFromImportStatement fromImport = fromImports.get(0);
    final PyImportElement[] importElements = fromImport.getImportElements();
    assertEquals(1, importElements.length);
    assertEquals("argv", importElements [0].getVisibleName());
    assertFalse(fromImport.isStarImport());
    assertEquals(0, fromImport.getRelativeLevel());
    final PyQualifiedName qName = fromImport.getImportSourceQName();
    assertSameElements(qName.getComponents(), "sys");

    final List<PyImportElement> importTargets = file.getImportTargets();
    assertEquals(1, importTargets.size());
    final PyImportElement importElement = importTargets.get(0);
    final PyQualifiedName importQName = importElement.getImportedQName();
    assertSameElements(importQName.getComponents(), "os", "path");

    assertNotParsed(file);
  }

  private PyFile getTestFile() throws Exception {
    return getTestFile(getTestName(false) + ".py");
  }

  private PyFile getTestFile(final String fileName) throws IOException {
    VirtualFile sourceFile = myFixture.copyFileToProject(fileName);
    assert sourceFile != null;
    PsiFile psiFile = myFixture.getPsiManager().findFile(sourceFile);
    return (PyFile)psiFile;
  }
}