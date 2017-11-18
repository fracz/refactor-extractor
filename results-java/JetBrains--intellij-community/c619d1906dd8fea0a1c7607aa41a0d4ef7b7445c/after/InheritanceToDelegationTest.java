package com.intellij.refactoring;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.inheritanceToDelegation.InheritanceToDelegationProcessor;
import com.intellij.util.ArrayUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dsl
 */
public class InheritanceToDelegationTest extends MultiFileTestCase {
  protected String getTestRoot() {
    return "/refactoring/inheritanceToDelegation/";
  }

  public void testSimpleInsertion() throws Exception {
    doTest(createPerformAction("B", "myDelegate", "MyA", "A", new int[]{0, 1}, ArrayUtil.EMPTY_STRING_ARRAY, true, false));
  }

  public void testSimpleGenerics() throws Exception {
    doTest(createPerformAction("B", "myDelegate", "MyA", "A", new int[]{0, 1}, ArrayUtil.EMPTY_STRING_ARRAY, true, false));
  }

  public void testSuperCalls() throws Exception {
    doTest(createPerformAction("B", "myDelegate", "MyA", "A", new int[0], ArrayUtil.EMPTY_STRING_ARRAY, true, false));
  }

  public void testGetter() throws Exception {
    doTest(createPerformAction("B", "myDelegate", "MyA", "A", new int[]{0, 1}, ArrayUtil.EMPTY_STRING_ARRAY, true, true));
  }

  public void testSubClass() throws Exception {
    doTest(
      createPerformAction("A", "myDelegate", "MyDelegatedBase", "DelegatedBase", new int[]{0}, ArrayUtil.EMPTY_STRING_ARRAY, true,
          true));
  }

  public void testSubClassNoMethods() throws Exception {
    doTest(
      createPerformAction("A", "myDelegate", "MyDelegatedBase", "DelegatedBase", new int[0], ArrayUtil.EMPTY_STRING_ARRAY, true, true));
  }

  public void testInterfaces() throws Exception {
    doTest(createPerformAction("A", "myDelegate", "MyBase", "Base", new int[]{0}, new String[]{"I"}, true, true));
  }

  public void testInnerClass() throws Exception {
    doTest(createPerformAction("A", "myDelegate", "MyBase", "Base", new int[0], ArrayUtil.EMPTY_STRING_ARRAY, true, false));
  }

  public void testAbstractBase() throws Exception {
    doTest(createPerformAction("A", "myDelegate", "MyBase", "Base", new int[0], ArrayUtil.EMPTY_STRING_ARRAY, true, false));
  }

  public void testAbstractBase1() throws Exception {
    doTest(createPerformAction("A", "myDelegate", "MyBase", "Base", new int[0], ArrayUtil.EMPTY_STRING_ARRAY, false, false));
  }

  public void testHierarchy() throws Exception {
    doTest(createPerformAction("X", "myDelegate", "MyBase", "Base", new int[0], ArrayUtil.EMPTY_STRING_ARRAY, false, false));
  }

  public void testOverridenMethods() throws Exception {
    doTest(createPerformAction("A", "myDelegate", "MyBase", "Base", new int[]{0}, ArrayUtil.EMPTY_STRING_ARRAY, false, false));
  }


  public void testInnerClassForInterface() throws Exception {
    doTest(createPerformAction("A", "myBaseInterface", "MyBaseInterface", "BaseInterface",
        new int[]{0}, ArrayUtil.EMPTY_STRING_ARRAY, false, false));
  }

  public void testInnerClassForInterfaceAbstract() throws Exception {
    doTest(createPerformAction("A", "myBaseInterface", "MyBaseInterface", "BaseInterface",
        new int[]{0}, ArrayUtil.EMPTY_STRING_ARRAY, false, false));
  }

  public void testSubinterface() throws Exception {
    doTest(createPerformAction("A", "myDelegate", "MyJ", "J", new int[0], ArrayUtil.EMPTY_STRING_ARRAY, true, true));
  }

  public void testInterfaceDelegation() throws Exception {
    doTest(createPerformAction("A", "myDelegate", "MyIntf", "Intf", new int[]{0}, ArrayUtil.EMPTY_STRING_ARRAY, true, true));
  }

  public void testScr20557() throws Exception {
    doTest(createPerformAction2("xxx.SCR20557", "myResultSet", "MyResultSet", "java.sql.ResultSet",
        new String[]{"getDate"}, ArrayUtil.EMPTY_STRING_ARRAY, false, false));
  }


  private PerformAction createPerformAction(
    final String className, final String fieldName, final String innerClassName,
    final String baseClassName, final int[] methodIndices, final String[] delegatedInterfaceNames,
    final boolean delegateOtherMembers, final boolean generateGetter) {
    return new PerformAction() {
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) throws Exception {
        PsiClass aClass = myPsiManager.findClass(className);
        assertNotNull("Class " + className + " not found", aClass);
        PsiClass baseClass = myPsiManager.findClass(baseClassName);
        assertNotNull("Base class " + baseClassName + " not found", baseClass);
        final PsiMethod[] methods = baseClass.getMethods();
        final PsiMethod[] delegatedMethods = new PsiMethod[methodIndices.length];
        for (int i = 0; i < methodIndices.length; i++) {
          delegatedMethods[i] = methods[methodIndices[i]];
        }
        final PsiClass[] delegatedInterfaces = new PsiClass[delegatedInterfaceNames.length];
        for (int i = 0; i < delegatedInterfaceNames.length; i++) {
          String delegatedInterfaceName = delegatedInterfaceNames[i];
          PsiClass anInterface = myPsiManager.findClass(delegatedInterfaceName);
          assertNotNull(anInterface);
          delegatedInterfaces[i] = anInterface;
        }
        new InheritanceToDelegationProcessor(
          myProject,
          aClass, baseClass, fieldName, innerClassName, delegatedInterfaces, delegatedMethods, delegateOtherMembers,
          generateGetter).run();
        FileDocumentManager.getInstance().saveAllDocuments();
      }
    };
  }

  private PerformAction createPerformAction2(
    final String className, final String fieldName, final String innerClassName,
    final String baseClassName, final String[] methodNames, final String[] delegatedInterfaceNames,
    final boolean delegateOtherMembers, final boolean generateGetter) {
    return new PerformAction() {
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) throws Exception {
        PsiClass aClass = myPsiManager.findClass(className);
        assertNotNull("Class " + className + " not found", aClass);
        PsiClass baseClass = myPsiManager.findClass(baseClassName);
        assertNotNull("Base class " + baseClassName + " not found", baseClass);
        final PsiMethod[] delegatedMethods;
        final List<PsiMethod> methodsList = new ArrayList<PsiMethod>();
        for (int i = 0; i < methodNames.length; i++) {
          String name = methodNames[i];
          final PsiMethod[] methodsByName = baseClass.findMethodsByName(name, false);
          for (int j = 0; j < methodsByName.length; j++) {
            PsiMethod method = methodsByName[j];
            methodsList.add(method);
          }
        }
        delegatedMethods = methodsList.toArray(new PsiMethod[methodsList.size()]);

        final PsiClass[] delegatedInterfaces = new PsiClass[delegatedInterfaceNames.length];
        for (int i = 0; i < delegatedInterfaceNames.length; i++) {
          String delegatedInterfaceName = delegatedInterfaceNames[i];
          PsiClass anInterface = myPsiManager.findClass(delegatedInterfaceName);
          assertNotNull(anInterface);
          delegatedInterfaces[i] = anInterface;
        }
        new InheritanceToDelegationProcessor(
          myProject,
          aClass, baseClass, fieldName, innerClassName, delegatedInterfaces, delegatedMethods, delegateOtherMembers,
          generateGetter).run();
        FileDocumentManager.getInstance().saveAllDocuments();
      }
    };
  }

}