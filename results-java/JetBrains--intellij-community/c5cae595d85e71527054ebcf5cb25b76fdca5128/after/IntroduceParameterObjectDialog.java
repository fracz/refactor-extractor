package com.intellij.refactoring.introduceparameterobject;

import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.ide.util.TreeClassChooserDialog;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.RefactorJBundle;
import com.intellij.refactoring.ui.RefactoringDialog;
import com.intellij.refactoring.util.ParameterTablePanel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"OverridableMethodCallInConstructor"})
public class IntroduceParameterObjectDialog extends RefactoringDialog {

  private final PsiMethod sourceMethod;
  private final ParameterTablePanel.VariableData[] parameterInfo;
  private JTextField sourceMethodTextField;

  private JRadioButton useExistingClassButton;
  private TextFieldWithBrowseButton existingClassField;
  private JPanel myUseExistingPanel;

  private JRadioButton createNewClassButton;
  private JTextField classNameField;
  private TextFieldWithBrowseButton packageTextField;
  private JPanel myCreateNewClassPanel;

  private JRadioButton myCreateInnerClassRadioButton;
  private JTextField myInnerClassNameTextField;
  private JPanel myInnerClassPanel;

  private JPanel myWholePanel;
  private JPanel myParamsPanel;
  private JCheckBox keepMethodAsDelegate;

  public IntroduceParameterObjectDialog(PsiMethod sourceMethod) {
    super(sourceMethod.getProject(), true);
    this.sourceMethod = sourceMethod;
    setTitle(RefactorJBundle.message("introduce.parameter.object.title"));
    final DocumentListener docListener = new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        validateButtons();
      }
    };
    packageTextField.getTextField().getDocument().addDocumentListener(docListener);
    existingClassField.getTextField().getDocument().addDocumentListener(docListener);
    classNameField.getDocument().addDocumentListener(docListener);
    myInnerClassNameTextField.getDocument().addDocumentListener(docListener);
    final PsiParameterList parameterList = sourceMethod.getParameterList();
    final PsiParameter[] parameters = parameterList.getParameters();
    parameterInfo = new ParameterTablePanel.VariableData[parameters.length];
    for (int i = 0; i < parameterInfo.length; i++) {
      parameterInfo[i] = new ParameterTablePanel.VariableData(parameters[i]);
      parameterInfo[i].name = parameters[i].getName();
      parameterInfo[i].passAsParameter = true;
    }

    final PsiFile file = sourceMethod.getContainingFile();
    if (file instanceof PsiJavaFile) {
      packageTextField.setText(((PsiJavaFile)file).getPackageName());
    }
    final PsiClass containingClass = sourceMethod.getContainingClass();
    sourceMethodTextField.setText(containingClass.getName() + '.' + sourceMethod.getName());
    final ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(useExistingClassButton);
    buttonGroup.add(createNewClassButton);
    buttonGroup.add(myCreateInnerClassRadioButton);
    createNewClassButton.setSelected(true);
    init();
    final ActionListener listener = new ActionListener() {

      public void actionPerformed(ActionEvent actionEvent) {
        toggleRadioEnablement();
      }
    };
    useExistingClassButton.addActionListener(listener);
    createNewClassButton.addActionListener(listener);
    myCreateInnerClassRadioButton.addActionListener(listener);
    toggleRadioEnablement();
  }

  private void toggleRadioEnablement() {
    UIUtil.setEnabled(myUseExistingPanel, useExistingClassButton.isSelected(), true);
    UIUtil.setEnabled(myCreateNewClassPanel, createNewClassButton.isSelected(), true);
    UIUtil.setEnabled(myInnerClassPanel, myCreateInnerClassRadioButton.isSelected(), true);
    validateButtons();
  }

  protected String getDimensionServiceKey() {
    return "RefactorJ.IntroduceParameterObject";
  }

  protected void doAction() {
    final List<PsiParameter> params = getParametersToExtract();
    final boolean useExistingClass = useExistingClass();
    final boolean keepMethod = keepMethodAsDelegate();
    final String className;
    final String packageName;
    final List<String> getterNames;
    final boolean createInnerClass = myCreateInnerClassRadioButton.isSelected();
    if (createInnerClass) {
      className = getInnerClassName();
      packageName = "";
      getterNames = null;
    } else if (useExistingClass) {
      final String existingClassName = getExistingClassName();
      getterNames = new ArrayList<String>();
      className = StringUtil.getShortName(existingClassName);
      packageName = StringUtil.getPackageName(existingClassName);
    }
    else {
      packageName = getPackageName();
      className = getClassName();
      getterNames = null;
    }
    List<ParameterTablePanel.VariableData> parameters = new ArrayList<ParameterTablePanel.VariableData>();
    for (ParameterTablePanel.VariableData data : parameterInfo) {
      if (data.passAsParameter) {
        parameters.add(data);
      }
    }
    invokeRefactoring(new IntroduceParameterObjectProcessor(className, packageName, sourceMethod,
                                                            parameters.toArray(new ParameterTablePanel.VariableData[parameters.size()]), getterNames, keepMethod, useExistingClass,
                                                            createInnerClass));
  }

  @Override
  protected void canRun() throws ConfigurationException {
    final Project project = sourceMethod.getProject();
    final JavaPsiFacade manager = JavaPsiFacade.getInstance(project);
    final PsiNameHelper nameHelper = manager.getNameHelper();

    final List<PsiParameter> parametersToExtract = getParametersToExtract();
    if (parametersToExtract.isEmpty()) {
      throw new ConfigurationException("Nothing found to extract");
    }
    if (myCreateInnerClassRadioButton.isSelected()) {
      final String innerClassName = getInnerClassName().trim();
      if (!nameHelper.isIdentifier(innerClassName)) throw new ConfigurationException("\'" + StringUtil.first(innerClassName, 10, true) + "\' is invalid inner class name");
      if (sourceMethod.getContainingClass().findInnerClassByName(innerClassName, false) != null) throw new ConfigurationException("Inner class with name \'" + StringUtil.first(innerClassName, 10, true) + "\' already exist");
    } else if (!useExistingClass()) {
      final String className = getClassName();
      if (className.length() == 0 || !nameHelper.isIdentifier(className)) {
        throw new ConfigurationException("\'" + StringUtil.first(className, 10, true) + "\' is invalid parameter class name");
      }
      final String packageName = getPackageName();

      if (packageName.length() == 0 || !nameHelper.isQualifiedName(packageName)) {
        throw new ConfigurationException("\'" + StringUtil.last(packageName, 10, true) + "\' is invalid parameter class package name");
      }
    }
    else {
      final String className = getExistingClassName();
      if (className.length() == 0 || !nameHelper.isQualifiedName(className)) {
        throw new ConfigurationException("\'" + StringUtil.first(className, 10, true) + "\' is invalid qualified parameter class name");
      }
    }
  }

  private String getInnerClassName() {
    return  myInnerClassNameTextField.getText().trim();
  }

  @NotNull
  public String getPackageName() {
    return packageTextField.getText().trim();
  }

  @NotNull
  public String getExistingClassName() {
    return existingClassField.getText().trim();
  }

  @NotNull
  public String getClassName() {
    return classNameField.getText().trim();
  }

  @NotNull
  public List<PsiParameter> getParametersToExtract() {
    final List<PsiParameter> out = new ArrayList<PsiParameter>();
    for (ParameterTablePanel.VariableData info : parameterInfo) {
      if (info.passAsParameter) {
        out.add((PsiParameter)info.variable);
      }
    }
    return out;
  }

  protected JComponent createCenterPanel() {
    sourceMethodTextField.setEditable(false);

    existingClassField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final Project project = sourceMethod.getProject();
        final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        final TreeClassChooserDialog chooser =
          new TreeClassChooserDialog(RefactorJBundle.message("select.wrapper.class"), project, scope, null, null);
        final String classText = existingClassField.getText();
        final PsiClass currentClass = JavaPsiFacade.getInstance(project).findClass(classText, GlobalSearchScope.allScope(project));
        if (currentClass != null) {
          chooser.selectClass(currentClass);
        }
        chooser.show();
        final PsiClass selectedClass = chooser.getSelectedClass();
        if (selectedClass != null) {
          final String className = selectedClass.getQualifiedName();
          existingClassField.setText(className);
        }
      }
    });

    packageTextField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final Project project = sourceMethod.getProject();
        final PackageChooserDialog chooser = new PackageChooserDialog(RefactorJBundle.message("choose.destination.package.label"), project);
        final String packageText = packageTextField.getText();
        chooser.selectPackage(packageText);
        chooser.show();
        final PsiPackage aPackage = chooser.getSelectedPackage();
        if (aPackage != null) {
          final String packageName = aPackage.getQualifiedName();
          packageTextField.setText(packageName);
        }
      }
    });


    final ParameterTablePanel paramsPanel = new ParameterTablePanel(myProject, parameterInfo, sourceMethod) {
      protected void updateSignature() {}

      protected void doEnterAction() {}

      protected void doCancelAction() {
        IntroduceParameterObjectDialog.this.doCancelAction();
      }
    };
    myParamsPanel.add(paramsPanel, BorderLayout.CENTER);
    return myWholePanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return classNameField;
  }

  protected void doHelpAction() {
    final HelpManager helpManager = HelpManager.getInstance();
    helpManager.invokeHelp(HelpID.IntroduceParameterObject);
  }

  public boolean useExistingClass() {
    return useExistingClassButton.isSelected();
  }

  public boolean keepMethodAsDelegate() {
    return keepMethodAsDelegate.isSelected();
  }
}