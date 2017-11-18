package com.jetbrains.python.refactoring.classes.pullUp;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.refactoring.RefactoringBundle;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.refactoring.classes.PyClassRefactoringUtil;
import com.jetbrains.python.refactoring.classes.PyMemberInfo;

import java.util.*;

/**
 * @author Dennis.Ushakov
 */
public class PyPullUpHelper {
  private static final Logger LOG = Logger.getInstance(PyPullUpHelper.class.getName());
  private PyPullUpHelper() {}

  public static PyElement pullUp(final PyClass clazz, final Collection<PyMemberInfo> selectedMemberInfos, final PyClass superClass) {
    final Set<String> superClasses = new HashSet<String>();
    final List<PyFunction> methods = new ArrayList<PyFunction>();
    for (PyMemberInfo member : selectedMemberInfos) {
      final PyElement element = member.getMember();
      if (element instanceof PyFunction) methods.add((PyFunction)element);
      else if (element instanceof PyClass) superClasses.add(element.getName());
      else LOG.error("unmatched member class " + element.getClass());
    }
    CommandProcessor.getInstance().executeCommand(clazz.getProject(), new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            // move methods
            PyClassRefactoringUtil.moveMethods(methods, superClass);

            // move superclasses declarations
            PyClassRefactoringUtil.moveSuperclasses(clazz, superClasses, superClass);

            PyClassRefactoringUtil.insertPassIfNeeded(clazz);
          }
        });
      }
    }, RefactoringBundle.message("pull.members.up.title"), null);

    return superClass;
  }

}