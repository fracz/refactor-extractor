package com.intellij.openapi.roots.ui.configuration.packaging;

import com.intellij.openapi.deployment.ContainerElement;
import com.intellij.openapi.deployment.PackagingMethod;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author nik
 */
public class PackagingElementPropertiesComponent {
  private JPanel myMainPanel;
  private JComboBox myPackagingMethodBox;
  private JTextField myOutputPathField;
  private JLabel myPackagingMethodLabel;
  private JLabel myOutputPathLabel;
  private final ContainerElement myElement;
  private final PackagingEditorPolicy myEditorPolicy;
  private PackagingMethod myLastSelectedMethod;

  private PackagingElementPropertiesComponent(ContainerElement element, PackagingEditorPolicy editorPolicy) {
    myElement = element;
    myEditorPolicy = editorPolicy;
    if (editorPolicy.getAllowedPackagingMethods(element).length > 1) {
      for (PackagingMethod method : myEditorPolicy.getAllowedPackagingMethods(element)) {
        myPackagingMethodBox.addItem(method);
      }
      myPackagingMethodBox.setRenderer(new PackagingMethodListCellRenderer());
      myPackagingMethodBox.setSelectedItem(myLastSelectedMethod = element.getPackagingMethod());
      myPackagingMethodBox.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          updateOutputPath();
        }
      });
    }
    else {
      myPackagingMethodBox.setVisible(false);
      myPackagingMethodLabel.setVisible(false);
    }

    if (editorPolicy.isRelativePathCellEditable(element)) {
      myOutputPathField.setText(element.getURI());
    }
    else {
      myOutputPathField.setVisible(false);
      myOutputPathLabel.setVisible(false);
    }
  }

  private void updateOutputPath() {
    PackagingMethod method = getSelectedMethod();
    if (method != myLastSelectedMethod) {
      PackagingMethod oldMethod = myElement.getPackagingMethod();
      myElement.setPackagingMethod(method);
      myOutputPathField.setText(myEditorPolicy.suggestDefaultRelativePath(myElement));
      myElement.setPackagingMethod(oldMethod);
      myLastSelectedMethod = method;
    }
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public void applyChanges() {
    myElement.setPackagingMethod(getSelectedMethod());
    myElement.setURI(myOutputPathField.getText());
  }

  private PackagingMethod getSelectedMethod() {
    return (PackagingMethod)myPackagingMethodBox.getSelectedItem();
  }

  public static boolean isEnabled(ContainerElement element, PackagingEditorPolicy editorPolicy) {
    boolean showPackagingMethodBox = editorPolicy.getAllowedPackagingMethods(element).length > 1;
    boolean showOutputPathField = editorPolicy.isRelativePathCellEditable(element);
    return showOutputPathField || showPackagingMethodBox;
  }

  @Nullable
  public static PackagingElementPropertiesComponent createPropertiesComponent(ContainerElement element, PackagingEditorPolicy editorPolicy) {
    if (!isEnabled(element, editorPolicy)) return null;

    return new PackagingElementPropertiesComponent(element, editorPolicy);
  }

  public static boolean showDialog(final ContainerElement element, JPanel component, PackagingEditorPolicy policy) {
    PackagingElementPropertiesComponent propertiesComponent = createPropertiesComponent(element, policy);
    if (propertiesComponent == null) {
      return false;
    }

    PackagingElementPropertiesDialog dialog = new PackagingElementPropertiesDialog(component, element, propertiesComponent);
    dialog.show();
    return dialog.isOK();
  }

  private class PackagingMethodListCellRenderer extends DefaultListCellRenderer {
    @Override
      public Component getListCellRendererComponent(final JList list,
                                                  Object value,
                                                  final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus) {
      value = myElement.getDescriptionForPackagingMethod((PackagingMethod)value);
      return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
  }

  public static class PackagingElementPropertiesDialog extends DialogWrapper {
    private PackagingElementPropertiesComponent myPropertiesComponent;

    public PackagingElementPropertiesDialog(JComponent parent, final ContainerElement element,
                                            PackagingElementPropertiesComponent propertiesComponent) {
      super(parent, false);
      setTitle(ProjectBundle.message("dialog.title.packaging.edit.0.properties", element.getPresentableName()));
      myPropertiesComponent = propertiesComponent;
      init();
    }

    protected JComponent createCenterPanel() {
      return myPropertiesComponent.getMainPanel();
    }

    @Override
    protected void doOKAction() {
      myPropertiesComponent.applyChanges();
      super.doOKAction();
    }
  }
}