package com.intellij.uiDesigner;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.uiDesigner.compiler.Utils;
import com.intellij.uiDesigner.core.Util;
import com.intellij.uiDesigner.palette.ComponentItem;
import com.intellij.uiDesigner.palette.Palette;
import com.intellij.uiDesigner.palette.PalettePanel;
import com.intellij.uiDesigner.quickFixes.CreateFieldFix;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public final class InsertComponentProcessor extends EventProcessor{
  private static final Logger LOG = Logger.getInstance("#com.intellij.uiDesigner.InsertComponentProcessor");

  private final GuiEditor myEditor;
  private final PalettePanel myPalette;
  private boolean mySticky;
  private DropInfo myDropInfo;
  private RadComponent myInsertedComponent;
  private Point myInitialPoint;
  private Dimension myInitialSize;
  private boolean myShouldSetPreferredSizeIfNotResized;
  private GridInsertProcessor myGridInsertProcessor;

  public InsertComponentProcessor(final GuiEditor editor, final PalettePanel palette){
    myEditor = editor;
    myPalette = palette;
    myGridInsertProcessor = new GridInsertProcessor(editor);
  }

  public boolean isSticky() {
    return mySticky;
  }

  public void setSticky(final boolean sticky) {
    mySticky = sticky;
  }

  protected void processKeyEvent(final KeyEvent e) {}

  /**
   * TODO[vova] it would be fine to configure such "input" controls somewhere in palette
   * @return whether component is an input control or not
   */
  private static boolean isInputComponent(final RadComponent component){
    LOG.assertTrue(component != null);

    final Class aClass = component.getComponentClass();
    if(
      AbstractButton.class.isAssignableFrom(aClass) ||
      JComboBox.class.isAssignableFrom(aClass) ||
      JList.class.isAssignableFrom(aClass) ||
      JSpinner.class.isAssignableFrom(aClass) ||
      JTabbedPane.class.isAssignableFrom(aClass) ||
      JTable.class.isAssignableFrom(aClass) ||
      JTextComponent.class.isAssignableFrom(aClass) ||
      JTree.class.isAssignableFrom(aClass)
    ){
      return true;
    }

    return false;
  }

  @NotNull
  private static String suggestBinding(final GuiEditor editor, final String componentClassName){
    LOG.assertTrue(componentClassName != null);

    final int lastDotIndex = componentClassName.lastIndexOf('.');
    String shortClassName = componentClassName.substring(lastDotIndex + 1);

    // Here is euristic. Chop first 'J' letter for standard Swing classes.
    // Without 'J' bindings look better.
    //noinspection HardCodedStringLiteral
    if(
      shortClassName.length() > 1 && Character.isUpperCase(shortClassName.charAt(1)) &&
      componentClassName.startsWith("javax.swing.") &&
      StringUtil.startsWithChar(shortClassName, 'J')
    ){
      shortClassName = shortClassName.substring(1);
    }
    shortClassName = StringUtil.decapitalize(shortClassName);

    LOG.assertTrue(shortClassName.length() > 0);

    // Generate member name based on current code style
    //noinspection ForLoopThatDoesntUseLoopVariable
    for(int i = 0; true; i++){
      final String nameCandidate = shortClassName + (i + 1);
      final String binding = CodeStyleManager.getInstance(editor.getProject()).propertyNameToVariableName(
        nameCandidate,
        VariableKind.FIELD
      );

      if (!FormEditingUtil.bindingExists(editor.getRootContainer(), binding)) {
        return binding;
      }
    }
  }

  /**
   * Tries to create binding for {@link #myInsertedComponent}
   * @param editor
   * @param insertedComponent
   */
  public static void createBindingWhenDrop(final GuiEditor editor, final RadComponent insertedComponent) {
    if(isInputComponent(insertedComponent)){
      // Now if the inserted component is a input control, we need to automatically create binding
      final String binding = suggestBinding(editor, insertedComponent.getComponentClassName());
      insertedComponent.setBinding(binding);

      // Try to create field in the corresponding bound class
      final String classToBind = editor.getRootContainer().getClassToBind();
      if(classToBind != null){
        final PsiClass aClass = FormEditingUtil.findClassToBind(editor.getModule(), classToBind);
        if(aClass != null){
          ApplicationManager.getApplication().runWriteAction(
            new Runnable() {
              public void run() {
                CreateFieldFix.runImpl(editor,
                                       aClass,
                                       insertedComponent.getComponentClassName(),
                                       binding,
                                       false // silently skip all errors (if any)
                );
              }
            }
          );
        }
      }
    }
  }

  protected void processMouseEvent(final MouseEvent e){
    final int id = e.getID();
   switch (id) {
        case  MouseEvent.MOUSE_PRESSED:
          processMousePressed(e);
          break;
        case  MouseEvent.MOUSE_RELEASED:
          processMouseReleased();
          break;
        case  MouseEvent.MOUSE_DRAGGED:
          processMouseDragged(e);
          break;
    }
  }

  private void processMouseDragged(final MouseEvent e) {
    if (myDropInfo != null && myDropInfo.myTargetContainer.isXY()) {
      final int width = e.getX() - myInitialPoint.x;
      final int height = e.getY() - myInitialPoint.y;

      final Dimension newSize = myInsertedComponent.getSize();

      if (width >= myInitialSize.width) {
        newSize.width = width;
      }
      if (height >= myInitialSize.height) {
        newSize.height = height;
      }
      myInsertedComponent.setSize(newSize);
      myEditor.refresh();
    }
  }

  private void processMouseReleased() {
    if (!mySticky) {
      myPalette.clearActiveItem();
    }

    if (myDropInfo != null) {
      if (myDropInfo.myTargetContainer.isXY()) {
        Dimension newSize = myInsertedComponent.getSize();
        if (newSize.equals(myInitialSize) && myShouldSetPreferredSizeIfNotResized) {
          // if component dropped into XY and was not resized, make it preferred size
          newSize = myInsertedComponent.getPreferredSize();
        }
        Util.adjustSize(myInsertedComponent.getDelegee(), myInsertedComponent.getConstraints(), newSize);
        myInsertedComponent.setSize(newSize);
      }

      myEditor.refreshAndSave(true);
    }
  }

  private void processMousePressed(final MouseEvent e) {
    myEditor.getActiveDecorationLayer().removeFeedback();
    final ComponentItem item = myPalette.getActiveItem();
    final String id = myEditor.generateId();
    if (JScrollPane.class.getName().equals(item.getClassName())) {
      myInsertedComponent = new RadScrollPane(myEditor.getModule(), id);
    }
    else if (item == Palette.getInstance(myEditor.getProject()).getPanelItem()) {
      myInsertedComponent = new RadContainer(myEditor.getModule(), id);
    }
    else {
      if (VSpacer.class.getName().equals(item.getClassName())) {
        myInsertedComponent = new RadVSpacer(myEditor.getModule(), id);
      }
      else if (HSpacer.class.getName().equals(item.getClassName())) {
        myInsertedComponent = new RadHSpacer(myEditor.getModule(), id);
      }
      else if (JTabbedPane.class.getName().equals(item.getClassName())) {
        myInsertedComponent = new RadTabbedPane(myEditor.getModule(), id);
      }
      else if (JSplitPane.class.getName().equals(item.getClassName())) {
        myInsertedComponent = new RadSplitPane(myEditor.getModule(), id);
      }
      else {
        final ClassLoader loader = LoaderFactory.getInstance(myEditor.getProject()).getLoader(myEditor.getFile());
        try {
          final Class aClass = Class.forName(item.getClassName(), true, loader);
          myInsertedComponent = new RadAtomicComponent(myEditor.getModule(), aClass, id);
        }
        catch (final Exception exc) {
          String errorDescription = Utils.validateJComponentClass(loader, item.getClassName());
          if (errorDescription == null) {
            errorDescription = UIDesignerBundle.message("error.class.cannot.be.instantiated", item.getClassName());
            final String message = FormEditingUtil.getExceptionMessage(exc);
            if (message != null) {
              errorDescription += ": " + message;
            }
          }
          myInsertedComponent = RadErrorComponent.create(
            myEditor.getModule(),
            id,
            item.getClassName(),
            null,
            errorDescription
          );
        }
      }
    }
    myInsertedComponent.init(item);

    final GridInsertLocation location = GridInsertProcessor.getGridInsertLocation(myEditor, e.getX(), e.getY(), 0);
    if (FormEditingUtil.canDrop(myEditor, e.getX(), e.getY(), 1) || location.getMode() != GridInsertLocation.GridInsertMode.None) {
      CommandProcessor.getInstance().executeCommand(
        myEditor.getProject(),
        new Runnable(){
          public void run(){
            createBindingWhenDrop(myEditor, myInsertedComponent);

            final RadComponent[] components = new RadComponent[]{myInsertedComponent};
            if (location.getMode() == GridInsertLocation.GridInsertMode.None) {
              myDropInfo = FormEditingUtil.drop(myEditor, e.getX(), e.getY(), components, new int[]{0}, new int[]{0});
            }
            else {
              myDropInfo = myGridInsertProcessor.processGridInsertOnDrop(location, components, null);
              if (myDropInfo == null) {
                return;
              }
            }

            FormEditingUtil.clearSelection(myEditor.getRootContainer());
            myInsertedComponent.setSelected(true);

            myInitialSize = null;
            myShouldSetPreferredSizeIfNotResized = true;
            if (myDropInfo.myTargetContainer.isXY()) {
              setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
              myInitialSize = myInsertedComponent.getSize();
              if (myInitialSize.width > 0 && myInitialSize.height > 0) {
                myShouldSetPreferredSizeIfNotResized = false;
              }
              else {
                // size was not specified as initial value
                myInitialSize = new Dimension(7, 7);
              }
              Util.adjustSize(myInsertedComponent.getDelegee(), myInsertedComponent.getConstraints(), myInitialSize);
              myInsertedComponent.setSize(myInitialSize);
            }

            myEditor.refresh();

            myInitialPoint = e.getPoint();
          }
        },
        null,
        null
      );
    }
  }

  protected boolean cancelOperation() {
    myEditor.getActiveDecorationLayer().removeFeedback();
    return false;
  }

  public Cursor processMouseMoveEvent(final MouseEvent e) {
    return myGridInsertProcessor.processMouseMoveEvent(e.getX(), e.getY(), false, 1, 0);
  }
}