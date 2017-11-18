package com.jetbrains.python.refactoring.classes.extractSuperclass;

import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.refactoring.classes.PyMemberInfoStorage;
import com.jetbrains.python.refactoring.classes.PyPresenterTestMemberEntry;
import com.jetbrains.python.refactoring.classes.PyRefactoringPresenterTestCase;
import com.jetbrains.python.refactoring.classes.membersManager.PyMemberInfo;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Tests presenter for "extract superclass" refactoring
 *
 * @author Ilya.Kazakevich
 */
public class PyExtractSuperclassPresenterTest
  extends PyRefactoringPresenterTestCase<PyExtractSuperclassInitializationInfo, PyExtractSuperclassView> {

  public PyExtractSuperclassPresenterTest() {
    super(PyExtractSuperclassView.class, "extractsuperclass");
  }

  /**
   * Tests that static methods could be moved, but "extends object" is not in list
   */
  public void testStaticNoObject() {
    final Collection<PyPresenterTestMemberEntry> members = launchAndGetMembers("StaticOnly");

    final Matcher<Iterable<? extends PyPresenterTestMemberEntry>> matcher =
      Matchers.containsInAnyOrder(new PyPresenterTestMemberEntry("static_method()", true, true));
    compareMembers(members, matcher);
  }

  /**
   * Tests that if no members selected, presenter shows error
   */
  public void testNoSelectedMembersLeadsToError() {
    EasyMock.expect(myView.getSelectedMemberInfos()).andReturn(Collections.<PyMemberInfo>emptyList()).anyTimes();

    final Capture<String> errorMessageCapture = configureViewToCaptureError();


    final PyExtractSuperclassPresenterImpl sut = configureByClass("Child");
    myMocksControl.replay();
    sut.launch();

    sut.okClicked();

    Assert.assertTrue("No error displayed empty list of selected members", errorMessageCapture.hasCaptured());

  }

  /**
   * Checks that presenter displays error if user enters invalid name for new class
   */
  public void testInvalidSuperClassNameLeadsToError() {
    final String className = "Child";
    final PyClass aClass = getClassByName(className);
    final List<PyMemberInfo> classMemberInfos = new PyMemberInfoStorage(aClass).getClassMemberInfos(aClass);
    assert !classMemberInfos.isEmpty() : "No member infos for " + className;
    final PyMemberInfo pyMemberInfo = classMemberInfos.get(0);
    EasyMock.expect(myView.getSelectedMemberInfos()).andReturn(Collections.singletonList(pyMemberInfo)).anyTimes();
    EasyMock.expect(myView.getSuperClassName()).andReturn("INVALID CLASS NAME").anyTimes();
    final Capture<String> errorMessageCapture = configureViewToCaptureError();


    final PyExtractSuperclassPresenterImpl sut = configureByClass(className);
    myMocksControl.replay();
    sut.launch();

    sut.okClicked();

    Assert.assertTrue("No error displayed for invalid class name", errorMessageCapture.hasCaptured());

  }

  /**
   * Creates capture ready to capture error message and configures view to return it
   *
   * @return
   */
  @NotNull
  private Capture<String> configureViewToCaptureError() {
    final Capture<String> errorMessageCapture = new Capture<String>();
    myView.showError(EasyMock.capture(errorMessageCapture));
    return errorMessageCapture;
  }


  /**
   * Checks that class fields could be moved while "extends object" is not in list
   */
  public void testFieldsAndNoObject() {
    final Collection<PyPresenterTestMemberEntry> members = launchAndGetMembers("Child");

    final Matcher<Iterable<? extends PyPresenterTestMemberEntry>> matcher = Matchers
      .containsInAnyOrder(new PyPresenterTestMemberEntry("CLASS_VAR", true, true),
                          new PyPresenterTestMemberEntry("eggs(self)", true, false),
                          new PyPresenterTestMemberEntry("__init__(self)", true, false),
                          new PyPresenterTestMemberEntry("self.artur", true, false),
                          new PyPresenterTestMemberEntry("extends date", true, false));
    compareMembers(members, matcher);
  }

  /**
   * launches presenter and returns member it displayed to user
   *
   * @param className name of class to configure presenter by
   * @return displayed members
   */
  @NotNull
  private Collection<PyPresenterTestMemberEntry> launchAndGetMembers(@NotNull final String className) {
    final PyExtractSuperclassPresenterImpl sut = configureByClass(className);
    myMocksControl.replay();
    sut.launch();
    return getMembers();
  }


  /**
   * Configures presenter by class
   *
   * @param name name of class
   * @return presenter
   */
  @NotNull
  private PyExtractSuperclassPresenterImpl configureByClass(@NotNull final String name) {
    final PyClass childClass = getClassByName(name);
    final PyMemberInfoStorage storage = new PyMemberInfoStorage(childClass);
    return new PyExtractSuperclassPresenterImpl(myView, childClass, storage);
  }

}