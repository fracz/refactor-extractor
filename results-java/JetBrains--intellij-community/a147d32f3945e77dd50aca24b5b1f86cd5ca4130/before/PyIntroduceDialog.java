package com.jetbrains.python.refactoring.introduce;

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.EditorComboBoxEditor;
import com.intellij.ui.EditorComboBoxRenderer;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.StringComboboxEditor;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.psi.PyExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.*;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey.Ivanov
 * Date: Aug 18, 2009
 * Time: 8:43:28 PM
 */
public class PyIntroduceDialog extends DialogWrapper implements PyIntroduceSettings {
  private JPanel myContentPane;
  private JLabel myNameLabel;
  private ComboBox myNameComboBox;
  private JCheckBox myReplaceAll;
  private JRadioButton mySamePlace;
  private JRadioButton myConstructor;
  private JRadioButton mySetUp;
  private JPanel myPlaceSelector;

  private final Project myProject;
  private final int myOccurrencesCount;
  private final IntroduceValidator myValidator;
  private final PyExpression myExpression;
  private final String myHelpId;

  public PyIntroduceDialog(@NotNull final Project project,
                           @NotNull PyExpression expression,
                           @NotNull final String caption,
                           @NotNull final IntroduceValidator validator,
                           final int occurrencesCount,
                           final Collection<String> possibleNames,
                           final String helpId,
                           boolean hasConstructor,
                           boolean isTestClass) {
    super(project, true);
    myOccurrencesCount = occurrencesCount;
    myValidator = validator;
    myProject = project;
    myExpression = expression;
    myHelpId = helpId;
    setUpNameComboBox(possibleNames);

    setModal(true);
    setTitle(caption);
    init();
    setupDialog(hasConstructor, isTestClass);
    updateControls();
  }

  @Override
  protected String getHelpId() {
    return myHelpId;
  }

  private void setUpNameComboBox(Collection<String> possibleNames) {
    final EditorComboBoxEditor comboEditor = new StringComboboxEditor(myProject, PythonFileType.INSTANCE, myNameComboBox);

    myNameComboBox.setEditor(comboEditor);
    myNameComboBox.setRenderer(new EditorComboBoxRenderer(comboEditor));
    myNameComboBox.setEditable(true);
    myNameComboBox.setMaximumRowCount(8);

    myNameComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        updateControls();
      }
    });

    ((EditorTextField)myNameComboBox.getEditor().getEditorComponent()).addDocumentListener(new DocumentListener() {
      public void beforeDocumentChange(DocumentEvent event) {
      }

      public void documentChanged(DocumentEvent event) {
        updateControls();
      }
    });

    myContentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myNameComboBox.requestFocus();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

    for (String possibleName : possibleNames) {
      myNameComboBox.addItem(possibleName);
    }
  }

  private void setupDialog(boolean hasConstructor, boolean isTest) {
    myReplaceAll.setMnemonic(KeyEvent.VK_A);
    myNameLabel.setLabelFor(myNameComboBox);
    myPlaceSelector.setVisible(hasConstructor);
    mySetUp.setVisible(isTest);
    mySamePlace.setSelected(true);

    // Replace occurences check box setup
    if (myOccurrencesCount > 1) {
      myReplaceAll.setSelected(false);
      myReplaceAll.setEnabled(true);
      myReplaceAll.setText(myReplaceAll.getText() + " (" + myOccurrencesCount + " occurrences)");
    }
    else {
      myReplaceAll.setSelected(false);
      myReplaceAll.setEnabled(false);
    }
  }

  public JComponent getPreferredFocusedComponent() {
    return myNameComboBox;
  }

  protected JComponent createCenterPanel() {
    return myContentPane;
  }

  @Nullable
  public String getName() {
    final Object item = myNameComboBox.getEditor().getItem();
    if ((item instanceof String) && ((String)item).length() > 0) {
      return ((String)item).trim();
    }
    return null;
  }

  public Project getProject() {
    return myProject;
  }

  public PyExpression getExpression() {
    return myExpression;
  }

  public boolean doReplaceAllOccurrences() {
    return myReplaceAll.isSelected();
  }

  private void updateControls() {
    final boolean nameValid = myValidator.isNameValid(this);
    setOKActionEnabled(nameValid);
    setErrorText(!nameValid ? PyBundle.message("refactoring.introduce.name.error") : null);
  }

  public IntroduceHandler.InitPlace getInitPlace() {
    if (myConstructor.isSelected()) return IntroduceHandler.InitPlace.CONSTRUCTOR;
    if (mySetUp.isSelected())  return IntroduceHandler.InitPlace.SET_UP;
    return IntroduceHandler.InitPlace.SAME_METHOD;
  }
}