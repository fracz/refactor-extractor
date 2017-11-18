package com.jetbrains.python.refactoring;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.util.SystemProperties;
import com.jetbrains.python.PythonTestUtil;
import com.jetbrains.python.fixtures.PyTestCase;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.stubs.PyClassNameIndex;
import com.jetbrains.python.psi.stubs.PyFunctionNameIndex;
import com.jetbrains.python.refactoring.move.PyMoveClassOrFunctionProcessor;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @author vlan
 */
public class PyMoveClassOrFunctionTest extends PyTestCase {
  public void testFunction() {
    doTest("f", "b.py");
  }

  public void testClass() {
    doTest("C", "b.py");
  }

  // PY-3929
  // PY-4095
  public void testImportAs() {
    doTest("f", "b.py");
  }

  // PY-3929
  public void testQualifiedImport() {
    doTest("f", "b.py");
  }

  // PY-4074
  public void testNewModule() {
    SystemProperties.setTestUserName("user1");
    doTest("f", "b.py");
  }

  // PY-4098
  public void testPackageImport() {
    doTest("f", "b.py");
  }

  // PY-4130
  // PY-4131
  public void testDocstringTypes() {
    doTest("C", "b.py");
  }

  // PY-4182
  public void testInnerImports() {
    doTest("f", "b.py");
  }

  // PY-4545
  public void testBaseClass() {
    doTest("B", "b.py");
  }

  private void doTest(final String symbolName, final String toFileName) {
    String root = "/refactoring/moveClassOrFunction/" + getTestName(true);
    String rootBefore = root + "/before/src";
    String rootAfter = root + "/after/src";
    VirtualFile dir1 = myFixture.copyDirectoryToProject(rootBefore, "");
    PsiDocumentManager.getInstance(myFixture.getProject()).commitAllDocuments();

    PsiNamedElement element = findFirstNamedElement(symbolName);
    assertNotNull(element);

    VirtualFile toVirtualFile = dir1.findFileByRelativePath(toFileName);
    String path = toVirtualFile != null ? toVirtualFile.getPath() : (dir1.getPath() + "/" + toFileName);
    new PyMoveClassOrFunctionProcessor(myFixture.getProject(),
                                       new PsiNamedElement[] {element},
                                       path,
                                       false).run();

    VirtualFile dir2 = getVirtualFileByName(PythonTestUtil.getTestDataPath() + rootAfter);
    try {
      PlatformTestUtil.assertDirectoriesEqual(dir2, dir1, null);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  private static VirtualFile getVirtualFileByName(String fileName) {
    return LocalFileSystem.getInstance().findFileByPath(fileName.replace(File.separatorChar, '/'));
  }

  @Nullable
  private PsiNamedElement findFirstNamedElement(String name) {
    final Collection<PyClass> classes = PyClassNameIndex.find(name, myFixture.getProject(), false);
    if (classes.size() > 0) {
      return classes.iterator().next();
    }
    final Collection<PyFunction> functions = PyFunctionNameIndex.find(name, myFixture.getProject());
    if (functions.size() > 0) {
      return functions.iterator().next();
    }
    return null;
  }
}
