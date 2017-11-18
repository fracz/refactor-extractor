/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: Jul 3, 2005
 * Time: 6:15:22 PM
 */
package com.theoryinpractice.testng.configuration;

import com.intellij.execution.junit.TestSearchScope;
import com.intellij.execution.junit2.configuration.BrowseModuleValueActionListener;
import com.intellij.execution.junit2.configuration.CommonJavaParameters;
import com.intellij.execution.junit2.configuration.ConfigurationModuleSelector;
import com.intellij.execution.junit2.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.ui.AlternativeJREPanel;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.table.TableView;
import com.theoryinpractice.testng.model.TestData;
import com.theoryinpractice.testng.model.TestNGConfigurationModel;
import com.theoryinpractice.testng.model.TestNGParametersTableModel;
import com.theoryinpractice.testng.model.TestType;
import com.theoryinpractice.testng.configuration.browser.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

public class TestNGConfigurationEditor extends SettingsEditor<TestNGConfiguration> {
  //private static final Logger LOGGER = Logger.getInstance("TestNG Runner");
  private Project project;

  private JPanel panel;

  private LabeledComponent<TextFieldWithBrowseButton> classField;
  private EnvironmentVariablesComponent envVariablesComponent;
  private LabeledComponent<JComboBox> moduleClasspath;
  private AlternativeJREPanel alternateJDK;
  private ConfigurationModuleSelector moduleSelector;
  private JRadioButton suiteTest;
  private JRadioButton packageTest;
  private JRadioButton classTest;
  private JRadioButton methodTest;
  private JRadioButton groupTest;
  private TestNGConfigurationModel model;
  private LabeledComponent<TextFieldWithBrowseButton> methodField;
  private LabeledComponent<TextFieldWithBrowseButton> packageField;
  private LabeledComponent<TextFieldWithBrowseButton> groupField;
  private LabeledComponent<TextFieldWithBrowseButton> suiteField;
  private JRadioButton packagesInProject;
  private JRadioButton packagesInModule;
  private JRadioButton packagesAcrossModules;
  private JPanel packagePanel;
  private TestNGParametersTableModel propertiesTableModel;
  private LabeledComponent<TextFieldWithBrowseButton> propertiesFile;
  private LabeledComponent<TextFieldWithBrowseButton> outputDirectory;
  private JButton myAddButton;
  private JButton myRemoveButton;
  private TableView propertiesTableView;
  private JPanel commonParametersPanel; //temp compilation problems
  private CommonJavaParameters commonJavaParameters = new CommonJavaParameters();
  private ArrayList<Map.Entry> propertiesList;

  public TestNGConfigurationEditor(Project project) {
    this.project = project;
    BrowseModuleValueActionListener[] browseListeners = new BrowseModuleValueActionListener[]{new PackageBrowser(project),
      new TestClassBrowser(project, this), new MethodBrowser(project, this), new GroupBrowser(project, this), new SuiteBrowser(project)};
    model = new TestNGConfigurationModel(project);
    model.setListener(this);
    createView();
    moduleSelector = new ConfigurationModuleSelector(project, getModulesComponent());
    registerListener(new JRadioButton[]{packageTest, classTest, methodTest, groupTest, suiteTest}, new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        ButtonModel buttonModel = (ButtonModel)e.getSource();
        if (buttonModel.isSelected()) {
          if (buttonModel == packageTest.getModel()) {
            model.setType(TestType.PACKAGE);
          }
          else if (buttonModel == classTest.getModel()) {
            model.setType(TestType.CLASS);
          }
          else if (buttonModel == methodTest.getModel()) {
            model.setType(TestType.METHOD);
          }
          else if (buttonModel == groupTest.getModel()) {
            model.setType(TestType.GROUP);
          }
          else if (buttonModel == suiteTest.getModel()) {
            model.setType(TestType.SUITE);
          }
          redisplay();
        }
      }
    });
    registerListener(new JRadioButton[]{packagesInProject, packagesInModule, packagesAcrossModules}, null);
    packagesInProject.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        evaluateModuleClassPath();
      }
    });

    LabeledComponent[] components = new LabeledComponent[]{packageField, classField, methodField, groupField, suiteField};
    for (int i = 0; i < components.length; i++) {
      TextFieldWithBrowseButton field = (TextFieldWithBrowseButton)components[i].getComponent();
      Document document = model.getDocument(i);
      field.getTextField().setDocument(document);
      browseListeners[i].setField(field);
    }
    model.setType(TestType.CLASS);
  }

  private void evaluateModuleClassPath() {
    moduleClasspath.setEnabled(!packagesInProject.isSelected());
  }

  private void redisplay() {
    if (packageTest.isSelected()) {
      packagePanel.setVisible(true);
      classField.setVisible(false);
      methodField.setVisible(false);
      groupField.setVisible(false);
      suiteField.setVisible(false);
    }
    else if (classTest.isSelected()) {
      packagePanel.setVisible(false);
      classField.setVisible(true);
      methodField.setVisible(false);
      groupField.setVisible(false);
      suiteField.setVisible(false);
    }
    else if (methodTest.isSelected()) {
      packagePanel.setVisible(false);
      classField.setVisible(true);
      methodField.setVisible(true);
      groupField.setVisible(false);
      suiteField.setVisible(false);
    }
    else if (groupTest.isSelected()) {
      packagePanel.setVisible(false);
      classField.setVisible(false);
      methodField.setVisible(false);
      groupField.setVisible(true);
      suiteField.setVisible(false);
    }
    else if (suiteTest.isSelected()) {
      packagePanel.setVisible(false);
      classField.setVisible(false);
      methodField.setVisible(false);
      groupField.setVisible(false);
      suiteField.setVisible(true);
    }
  }

  public String getClassName() {
    return classField.getComponent().getText();
  }

  public JComboBox getModulesComponent() {
    return moduleClasspath.getComponent();
  }

  @Override
  protected void resetEditorFrom(TestNGConfiguration config) {
    model.reset(config);
    commonJavaParameters.reset(config);
    getModuleSelector().reset(config);
    TestData data = config.getPersistantData();
    TestSearchScope scope = data.getScope();
    if (scope == TestSearchScope.SINGLE_MODULE) {
      packagesInModule.setSelected(true);
    }
    else if (scope == TestSearchScope.MODULE_WITH_DEPENDENCIES) {
      packagesAcrossModules.setSelected(true);
    }
    else {
      packagesInProject.setSelected(true);
    }
    alternateJDK.init(config.ALTERNATIVE_JRE_PATH, config.ALTERNATIVE_JRE_PATH_ENABLED);
    envVariablesComponent.setEnvs(
      config.getPersistantData().ENV_VARIABLES != null ? FileUtil.toSystemDependentName(config.getPersistantData().ENV_VARIABLES) : "");
    propertiesList = new ArrayList<Map.Entry>();
    propertiesList.addAll(data.TEST_PROPERTIES.entrySet());
    propertiesTableModel.setParameterList(propertiesList);

    propertiesFile.getComponent().getTextField().setDocument(model.getPropertiesFileDocument());
    outputDirectory.getComponent().getTextField().setDocument(model.getOutputDirectoryDocument());
  }

  @Override
  public void applyEditorTo(TestNGConfiguration config) {
    model.apply(getModuleSelector().getModule(), config);
    getModuleSelector().applyTo(config);
    TestData data = config.getPersistantData();
    if (packageTest.isSelected()) {
      if (packagesInProject.isSelected()) {
        data.setScope(TestSearchScope.WHOLE_PROJECT);
      }
      else if (packagesInModule.isSelected()) {
        data.setScope(TestSearchScope.SINGLE_MODULE);
      }
      else if (packagesAcrossModules.isSelected()) data.setScope(TestSearchScope.MODULE_WITH_DEPENDENCIES);
    }
    else {
      data.setScope(TestSearchScope.SINGLE_MODULE);
    }
    commonJavaParameters.applyTo(config);
    config.ALTERNATIVE_JRE_PATH = alternateJDK.getPath();
    config.ALTERNATIVE_JRE_PATH_ENABLED = alternateJDK.isPathEnabled();

    data.TEST_PROPERTIES.clear();
    for (Map.Entry<String, String> entry : propertiesList) {
      data.TEST_PROPERTIES.put(entry.getKey(), entry.getValue());
    }

    data.ENV_VARIABLES =
      envVariablesComponent.getEnvs().trim().length() > 0 ? FileUtil.toSystemIndependentName(envVariablesComponent.getEnvs()) : null;
  }

  public ConfigurationModuleSelector getModuleSelector() {
    return moduleSelector;
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return panel;
  }

  private static void registerListener(JRadioButton[] buttons, ChangeListener changelistener) {
    ButtonGroup buttongroup = new ButtonGroup();
    for (JRadioButton button : buttons) {
      button.getModel().addChangeListener(changelistener);
      buttongroup.add(button);
    }

    if (buttongroup.getSelection() == null) buttongroup.setSelected(buttons[0].getModel(), true);
  }

  private void createView() {
    commonParametersPanel.add(commonJavaParameters, BorderLayout.CENTER);

    packageTest.setSelected(false);
    suiteTest.setSelected(false);
    suiteTest.setEnabled(true);
    groupTest.setSelected(false);
    groupTest.setEnabled(true);
    classTest.setSelected(false);
    classTest.setEnabled(true);

    classField.setComponent(new TextFieldWithBrowseButton());
    methodField.setComponent(new TextFieldWithBrowseButton());
    groupField.setComponent(new TextFieldWithBrowseButton());
    suiteField.setComponent(new TextFieldWithBrowseButton());
    packageField.setVisible(true);
    packageField.setEnabled(true);
    packageField.setComponent(new TextFieldWithBrowseButton());


    TextFieldWithBrowseButton outputDirectoryButton = new TextFieldWithBrowseButton();
    outputDirectory.setComponent(outputDirectoryButton);
    outputDirectoryButton.addBrowseFolderListener("TestNG", "Select test output directory", project,
                                                  new FileChooserDescriptor(false, true, false, false, false, false));
    moduleClasspath.setEnabled(true);
    moduleClasspath.setComponent(new JComboBox());

    propertiesTableModel = new TestNGParametersTableModel();


    TextFieldWithBrowseButton textFieldWithBrowseButton = new TextFieldWithBrowseButton();
    propertiesFile.setComponent(textFieldWithBrowseButton);

    FileChooserDescriptor propertiesFileDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
      @Override
      public boolean isFileVisible(VirtualFile virtualFile, boolean showHidden) {
        if (!showHidden && virtualFile.getName().charAt(0) == '.') return false;
        return virtualFile.isDirectory() || "properties".equals(virtualFile.getExtension());
      }
    };

    textFieldWithBrowseButton
      .addBrowseFolderListener("TestNG", "Select .properties file for test properties", project, propertiesFileDescriptor);

    propertiesTableView.setModel(propertiesTableModel);
    propertiesTableView.setShowGrid(true);

    myAddButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        propertiesTableModel.addParameter();
      }
    });
    myRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        for (int row : propertiesTableView.getSelectedRows()) {
          propertiesTableModel.removeProperty(row);
        }
      }
    });
  }

  @Override
  protected void disposeEditor() {
  }

  public void onTypeChanged(TestType type) {
    //LOGGER.info("onTypeChanged with " + type);
    if (type != TestType.PACKAGE && type != TestType.SUITE) {
      moduleClasspath.setEnabled(true);
    }
    else {
      evaluateModuleClassPath();
    }
    if (type == TestType.PACKAGE) {
      packageTest.setSelected(true);
      packageField.setEnabled(true);
      classField.setEnabled(false);
      methodField.setEnabled(false);
      groupField.setEnabled(false);
      suiteField.setEnabled(false);
    }
    else if (type == TestType.CLASS) {
      classTest.setSelected(true);
      packageField.setEnabled(false);
      classField.setEnabled(true);
      methodField.setEnabled(false);
      groupField.setEnabled(false);
      suiteField.setEnabled(false);
    }
    else if (type == TestType.METHOD) {
      methodTest.setSelected(true);
      packageField.setEnabled(false);
      classField.setEnabled(true);
      methodField.setEnabled(true);
      groupField.setEnabled(false);
      suiteField.setEnabled(false);
    }
    else if (type == TestType.GROUP) {
      groupTest.setSelected(true);
      groupField.setEnabled(true);
      packageField.setEnabled(false);
      classField.setEnabled(false);
      methodField.setEnabled(false);
      suiteField.setEnabled(false);
    }
    else if (type == TestType.SUITE) {
      suiteTest.setSelected(true);
      suiteField.setEnabled(true);
      packageField.setEnabled(false);
      classField.setEnabled(false);
      methodField.setEnabled(false);
      groupField.setEnabled(false);
    }
  }

}