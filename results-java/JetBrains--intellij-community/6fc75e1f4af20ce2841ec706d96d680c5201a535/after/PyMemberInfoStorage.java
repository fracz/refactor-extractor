package com.jetbrains.python.refactoring.classes;

import com.intellij.psi.PsiElement;
import com.intellij.refactoring.classMembers.AbstractMemberInfoStorage;
import com.intellij.refactoring.classMembers.MemberInfoBase;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.refactoring.PyRefactoringUtil;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Dennis.Ushakov
 */
public class PyMemberInfoStorage extends AbstractMemberInfoStorage<PyElement, PyClass, PyMemberInfo> {
  public PyMemberInfoStorage(PyClass aClass) {
    this(aClass, new MemberInfoBase.EmptyFilter<PyElement>());
  }

  public PyMemberInfoStorage(PyClass aClass, MemberInfoBase.Filter<PyElement> memberInfoFilter) {
    super(aClass, memberInfoFilter);
  }

  @Override
  protected boolean isInheritor(PyClass baseClass, PyClass aClass) {
    return getSubclasses(aClass).contains(baseClass);
  }

  @Override
  protected void buildSubClassesMap(PyClass aClass) {
    buildSubClassesMapImpl(aClass, new HashSet<PyClass>());
  }

  private void buildSubClassesMapImpl(PyClass aClass, HashSet<PyClass> visited) {
    visited.add(aClass);
    for (PyClass clazz : aClass.getSuperClasses()) {
      getSubclasses(clazz).add(aClass);
      if (!visited.contains(clazz)) {
        buildSubClassesMapImpl(clazz, visited);
      }
    }
  }

  @Override
  protected void extractClassMembers(PyClass aClass, ArrayList<PyMemberInfo> temp) {
    for (PyFunction function : aClass.getMethods()) {
      temp.add(new PyMemberInfo(function));
    }
  }

  @Override
  protected boolean memberConflict(PsiElement member1, PsiElement member) {
    return member1 instanceof PyFunction && member instanceof PyFunction &&
           PyRefactoringUtil.areConflictingMethods((PyFunction)member, (PyFunction)member1);
  }
}