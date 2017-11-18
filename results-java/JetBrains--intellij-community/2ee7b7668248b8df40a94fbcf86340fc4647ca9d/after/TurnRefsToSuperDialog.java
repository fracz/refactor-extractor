/*
 * Created by IntelliJ IDEA.
 * User: dsl
 * Date: 06.06.2002
 * Time: 11:30:13
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.intellij.refactoring.turnRefsToSuper;

import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.RefactoringSettings;
import com.intellij.refactoring.ui.ClassCellRenderer;
import com.intellij.refactoring.ui.RefactoringDialog;
import com.intellij.refactoring.util.RefactoringHierarchyUtil;
import com.intellij.ui.IdeBorderFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TurnRefsToSuperDialog extends RefactoringDialog {
  private final PsiClass mySubClass;
  private final List mySuperClasses;

  private JList mySuperClassesList = null;
  private final JCheckBox myCbReplaceInstanceOf = new JCheckBox();

  TurnRefsToSuperDialog(Project project, PsiClass subClass, List superClasses) {
    super(project, true);

    mySubClass = subClass;
    mySuperClasses = superClasses;

    setTitle(TurnRefsToSuperHandler.REFACTORING_NAME);
    init();
  }

  public PsiClass getSuperClass() {
    if(mySuperClassesList != null) {
      return (PsiClass) mySuperClassesList.getSelectedValue();
    }
    else {
      return null;
    }
  }

  public boolean isUseInInstanceOf() {
    return myCbReplaceInstanceOf.isSelected();
  }

  protected void doHelpAction() {
    HelpManager.getInstance().invokeHelp(HelpID.TURN_REFS_TO_SUPER);
  }

  public JComponent getPreferredFocusedComponent() {
    return mySuperClassesList;
  }


  protected JComponent createNorthPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(IdeBorderFactory.createBorder());

    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbConstraints = new GridBagConstraints();

    gbConstraints.insets = new Insets(4, 8, 4, 8);
    gbConstraints.weighty = 1;
    gbConstraints.weightx = 1;
    gbConstraints.gridy = 0;
    gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.anchor = GridBagConstraints.WEST;
    final JLabel classListLabel = new JLabel();
    panel.add(classListLabel, gbConstraints);

    mySuperClassesList = new JList(mySuperClasses.toArray());
    mySuperClassesList.setCellRenderer(new ClassCellRenderer());
    mySuperClassesList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    classListLabel.setText(RefactoringBundle.message("turnRefsToSuper.change.usages.to", mySubClass.getQualifiedName()));

    PsiClass nearestBase = RefactoringHierarchyUtil.getNearestBaseClass(mySubClass, true);
    int indexToSelect = 0;
    if(nearestBase != null) {
      indexToSelect = mySuperClasses.indexOf(nearestBase);
    }
    mySuperClassesList.setSelectedIndex(indexToSelect);
    gbConstraints.gridy++;
    panel.add(new JScrollPane(mySuperClassesList), gbConstraints);

    gbConstraints.gridy++;
    myCbReplaceInstanceOf.setText(RefactoringBundle.message("turnRefsToSuper.use.superclass.in.instanceof"));
    myCbReplaceInstanceOf.setSelected(false);
    myCbReplaceInstanceOf.setFocusable(false);
    panel.add(myCbReplaceInstanceOf, gbConstraints);

    return panel;
  }

  protected String getDimensionServiceKey() {
    return "#com.intellij.refactoring.turnRefsToSuper.TurnRefsToSuperDialog";
  }

  protected void doAction() {
    RefactoringSettings.getInstance().TURN_REFS_TO_SUPER_PREVIEW_USAGES = isPreviewUsages();
    final TurnRefsToSuperProcessor processor = new TurnRefsToSuperProcessor(
      getProject(), mySubClass, getSuperClass(), isUseInInstanceOf());
    invokeRefactoring(processor);
  }

  protected JComponent createCenterPanel() {
    return null;
  }
}