/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.refactoring.move;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapperPeer;
import com.intellij.psi.PsiNamedElement;
import com.intellij.refactoring.classMembers.MemberInfoChange;
import com.intellij.refactoring.classMembers.MemberInfoModel;
import com.intellij.refactoring.ui.AbstractMemberSelectionTable;
import com.intellij.ui.HideableDecorator;
import com.intellij.ui.RowIcon;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Mikhail Golubev
 */
public class PyMoveModuleMembersDialog extends PyBaseMoveDialog {
  @NonNls private final static String BULK_MOVE_TABLE_VISIBLE = "python.move.module.members.dialog.show.table";

  /**
   * Instance to be injected to mimic this class in tests
   */
  private static PyMoveModuleMembersDialog ourInstanceToReplace = null;

  private final TopLevelSymbolsSelectionTable myMemberSelectionTable;
  private final PyModuleMemberInfoModel myModuleMemberModel;
  private final boolean mySeveralElementsSelected;

  /**
   * Either creates new dialog or return singleton instance initialized with {@link #setInstanceToReplace)}.
   * Singleton dialog is intended to be used in tests.
   *
   * @param project dialog project
   * @param elements elements to move
   * @param source
   *@param destination destination where elements have to be moved  @return dialog
   */
  public static PyMoveModuleMembersDialog getInstance(@NotNull Project project,
                                                      @NotNull List<PsiNamedElement> elements,
                                                      @NotNull String source,
                                                      @NotNull String destination) {
    return ourInstanceToReplace != null ? ourInstanceToReplace : new PyMoveModuleMembersDialog(project, elements, source, destination);
  }

  /**
   * Injects instance to be used in tests
   *
   * @param instanceToReplace instance to be used in tests
   */
  @TestOnly
  public static void setInstanceToReplace(@NotNull final PyMoveModuleMembersDialog instanceToReplace) {
    ourInstanceToReplace = instanceToReplace;
  }

  /**
   * @param project dialog project
   * @param elements elements to move
   * @param source
   * @param destination destination where elements have to be moved
   */
  protected PyMoveModuleMembersDialog(@NotNull Project project,
                                      @NotNull List<PsiNamedElement> elements,
                                      @NotNull String source,
                                      @NotNull String destination) {
    super(project, source, destination);

    assert !elements.isEmpty();
    final PsiNamedElement firstElement = elements.get(0);
    setTitle(PyBundle.message("refactoring.move.module.members.dialog.title"));

    final PyFile pyFile = (PyFile)firstElement.getContainingFile();
    myModuleMemberModel = new PyModuleMemberInfoModel(pyFile);

    final List<PyModuleMemberInfo> symbolsInfos = collectModuleMemberInfos(myModuleMemberModel.myPyFile);
    for (PyModuleMemberInfo info : symbolsInfos) {
      //noinspection SuspiciousMethodCalls
      info.setChecked(elements.contains(info.getMember()));
    }
    myModuleMemberModel.memberInfoChanged(new MemberInfoChange<>(symbolsInfos));
    myMemberSelectionTable = new TopLevelSymbolsSelectionTable(symbolsInfos, myModuleMemberModel);
    myMemberSelectionTable.addMemberInfoChangeListener(myModuleMemberModel);
    myMemberSelectionTable.getModel().addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent e) {
        validateButtons();
      }
    });
    mySeveralElementsSelected = elements.size() > 1;
    final boolean tableIsVisible = mySeveralElementsSelected || PropertiesComponent.getInstance().getBoolean(BULK_MOVE_TABLE_VISIBLE);
    final String description;
    if (!tableIsVisible && elements.size() == 1) {
      if (firstElement instanceof PyFunction) {
        description =  PyBundle.message("refactoring.move.module.members.dialog.description.function.$0", firstElement.getName());
      }
      else if (firstElement instanceof PyClass) {
        description = PyBundle.message("refactoring.move.module.members.dialog.description.class.$0", firstElement.getName());
      }
      else {
        description = PyBundle.message("refactoring.move.module.members.dialog.description.variable.$0", firstElement.getName());
      }
    }
    else {
      description = PyBundle.message("refactoring.move.module.members.dialog.description.selection");
    }
    myDescription.setText(description);
    final HideableDecorator decorator = new HideableDecorator(myExtraPanel, PyBundle.message("refactoring.move.module.members.dialog.table.title"), true) {
      @Override
      protected void on() {
        super.on();
        myDescription.setText(PyBundle.message("refactoring.move.module.members.dialog.description.selection"));
        PropertiesComponent.getInstance().setValue(BULK_MOVE_TABLE_VISIBLE, true);
      }

      @Override
      protected void off() {
        super.off();
        PropertiesComponent.getInstance().setValue(BULK_MOVE_TABLE_VISIBLE, false);
      }
    };
    decorator.setOn(tableIsVisible);
    decorator.setContentComponent(new JBScrollPane(myMemberSelectionTable) {
      @Override
      public Dimension getMinimumSize() {
        // Prevent growth of the dialog after several expand/collapse actions
        return new Dimension((int)super.getMinimumSize().getWidth(), 0);
      }
    });

    init();
  }

  @Override
  protected void doWhenFirstShown() {
    super.doWhenFirstShown();
    enlargeDialogHeightIfNecessary();
  }

  private void enlargeDialogHeightIfNecessary() {
    if (mySeveralElementsSelected && !PropertiesComponent.getInstance(getProject()).getBoolean(BULK_MOVE_TABLE_VISIBLE)) {
      final DialogWrapperPeer peer = getPeer();
      final Dimension realSize = peer.getSize();
      final double preferredHeight = peer.getPreferredSize().getHeight();
      if (realSize.getHeight() < preferredHeight) {
        peer.setSize((int)realSize.getWidth(), (int)preferredHeight);
      }
    }
  }

  @Nullable
  @Override
  protected String getDimensionServiceKey() {
    return "#com.jetbrains.python.refactoring.move.PyMoveModuleMembersDialog";
  }

  @Override
  protected String getHelpId() {
    return "python.reference.moveModuleMembers";
  }

  @Override
  protected boolean areButtonsValid() {
    return !myMemberSelectionTable.getSelectedMemberInfos().isEmpty();
  }

  /**
   * @return selected elements in the same order as they are declared in the original file
   */
  @NotNull
  public List<PyElement> getSelectedTopLevelSymbols() {
    final Collection<PyModuleMemberInfo> selectedMembers = myMemberSelectionTable.getSelectedMemberInfos();
    final List<PyElement> selectedElements = ContainerUtil.map(selectedMembers, info -> info.getMember());
    return ContainerUtil.sorted(selectedElements, (e1, e2) -> PyPsiUtils.isBefore(e1, e2) ? -1 : 1);
  }

  @NotNull
  private static List<PyModuleMemberInfo> collectModuleMemberInfos(@NotNull PyFile pyFile) {
    final List<PyElement> moduleMembers = PyMoveModuleMembersHelper.getTopLevelModuleMembers(pyFile);
    return ContainerUtil.mapNotNull(moduleMembers, element -> new PyModuleMemberInfo(element));
  }

  static class TopLevelSymbolsSelectionTable extends AbstractMemberSelectionTable<PyElement, PyModuleMemberInfo> {
    public TopLevelSymbolsSelectionTable(Collection<PyModuleMemberInfo> memberInfos,
                                         @Nullable MemberInfoModel<PyElement, PyModuleMemberInfo> memberInfoModel) {
      super(memberInfos, memberInfoModel, null);
    }

    @Nullable
    @Override
    protected Object getAbstractColumnValue(PyModuleMemberInfo memberInfo) {
      return null;
    }

    @Override
    protected boolean isAbstractColumnEditable(int rowIndex) {
      return false;
    }

    @Override
    protected void setVisibilityIcon(PyModuleMemberInfo memberInfo, RowIcon icon) {

    }

    @Override
    protected Icon getOverrideIcon(PyModuleMemberInfo memberInfo) {
      return null;
    }
  }
}