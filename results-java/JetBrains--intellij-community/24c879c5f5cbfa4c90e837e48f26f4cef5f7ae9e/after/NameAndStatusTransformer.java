package com.jetbrains.python.refactoring.classes;

import com.google.common.base.Function;
import com.intellij.refactoring.classMembers.MemberInfoModel;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.refactoring.classes.membersManager.PyMemberInfo;
import org.jetbrains.annotations.NotNull;

/**
* @author Ilya.Kazakevich
*/
public class NameAndStatusTransformer implements Function<PyMemberInfo, PyPresenterTestMemberEntry> {
  @NotNull
  private final MemberInfoModel<PyElement, PyMemberInfo> myMemberInfoModel;

  public NameAndStatusTransformer(MemberInfoModel<PyElement, PyMemberInfo> memberInfoModel) {
    myMemberInfoModel = memberInfoModel;
  }

  @Override
  public PyPresenterTestMemberEntry apply(final PyMemberInfo input) {
    return new PyPresenterTestMemberEntry(input.getDisplayName(), myMemberInfoModel.isMemberEnabled(input), input.isStatic());
  }
}