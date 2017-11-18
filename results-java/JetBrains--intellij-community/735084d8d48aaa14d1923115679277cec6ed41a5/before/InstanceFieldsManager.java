package com.jetbrains.python.refactoring.classes.membersManager;

import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.refactoring.classes.PyClassRefactoringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Ilya.Kazakevich
 */
class InstanceFieldsManager extends FieldsManager {
  InstanceFieldsManager() {
    super(false);
  }


  @Override
  protected void moveMembers(@NotNull final PyClass from,
                             @NotNull final PyClass to,
                             @NotNull final Collection<PyTargetExpression> members) {
    //We need __init__ method, and if there is no any -- we need to create it
    PyFunction toInitMethod = to.findMethodByName(PyNames.INIT, false);
    if (toInitMethod == null) {
      toInitMethod = PyClassRefactoringUtil.createMethod(PyNames.INIT, to, null);
    }
    final PyStatementList statementList = toInitMethod.getStatementList();
    PyClassRefactoringUtil.moveFieldDeclarationToStatement(members, statementList);

    final PyFunction fromInitMethod = from.findMethodByName(PyNames.INIT, false);
    if (fromInitMethod !=null) {
      //We can't leave class constructor with empty body
      PyClassRefactoringUtil.insertPassIfNeeded(fromInitMethod);
    }
  }

  @Override
  protected boolean classHasField(@NotNull final PyClass pyClass, @NotNull final String fieldName) {
    return pyClass.findInstanceAttribute(fieldName, true) != null;
  }

  @NotNull
  @Override
  protected List<PyTargetExpression> getFieldsByClass(@NotNull final PyClass pyClass) {
    return pyClass.getInstanceAttributes();
  }
}