package com.intellij.uiDesigner.designSurface;

import com.intellij.CommonBundle;
import com.intellij.ide.palette.impl.PaletteManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.uiDesigner.*;
import com.intellij.uiDesigner.compiler.CodeGenerationException;
import com.intellij.uiDesigner.compiler.Utils;
import com.intellij.uiDesigner.core.Util;
import com.intellij.uiDesigner.make.PsiNestedFormLoader;
import com.intellij.uiDesigner.palette.ComponentItem;
import com.intellij.uiDesigner.palette.ComponentItemDialog;
import com.intellij.uiDesigner.palette.Palette;
import com.intellij.uiDesigner.quickFixes.CreateFieldFix;
import com.intellij.uiDesigner.radComponents.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public final class InsertComponentProcessor extends EventProcessor {
  private static final Logger LOG = Logger.getInstance("#com.intellij.uiDesigner.designSurface.InsertComponentProcessor");

  private PaletteManager myPaletteManager;
  private final GuiEditor myEditor;
  private boolean mySticky;
  private RadComponent myInsertedComponent;
  private GridInsertProcessor myGridInsertProcessor;
  private ComponentItem myComponentToInsert;
  private DropLocation myLastLocation;

  public InsertComponentProcessor(@NotNull final GuiEditor editor) {
    myEditor = editor;
    myGridInsertProcessor = new GridInsertProcessor(editor);
    myPaletteManager = PaletteManager.getInstance(editor.getProject());
  }

  public boolean isSticky() {
    return mySticky;
  }

  public void setSticky(final boolean sticky) {
    mySticky = sticky;
  }

  public void setComponentToInsert(final ComponentItem componentToInsert) {
    myComponentToInsert = componentToInsert;
  }

  public void setLastLocation(final DropLocation location) {
    if (location.canDrop(getComponentToInsert())) {
      myLastLocation = location;
    }
    else {
      DropLocation locationToRight = location.getAdjacentLocation(DropLocation.Direction.RIGHT);
      DropLocation locationToBottom = location.getAdjacentLocation(DropLocation.Direction.DOWN);
      if (locationToRight != null && locationToRight.canDrop(getComponentToInsert())) {
        myLastLocation = locationToRight;
      }
      else if (locationToBottom != null && locationToBottom.canDrop(getComponentToInsert())) {
        myLastLocation = locationToBottom;
      }
      else {
        myLastLocation = location;
      }
    }

    if (myLastLocation.canDrop(getComponentToInsert())) {

      myLastLocation.placeFeedback(myEditor.getActiveDecorationLayer(), getComponentToInsert());
    }
  }

  protected void processKeyEvent(final KeyEvent e) {
    if (e.getID() == KeyEvent.KEY_PRESSED) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        if (myLastLocation != null) {
          myEditor.getMainProcessor().stopCurrentProcessor();
          processComponentInsert(getComponentToInsert(), myLastLocation);
        }
      }
      else {
        myLastLocation = moveDropLocation(myEditor, myLastLocation, getComponentToInsert(), e);
      }
    }
  }

  @NotNull
  public static String suggestBinding(final GuiEditor editor, @NotNull final String componentClassName){
    String shortClassName = getShortClassName(componentClassName);

    LOG.assertTrue(shortClassName.length() > 0);

    return getUniqueBinding(editor.getRootContainer(), shortClassName);
  }

  public static String getShortClassName(@NonNls final String componentClassName) {
    final int lastDotIndex = componentClassName.lastIndexOf('.');
    String shortClassName = componentClassName.substring(lastDotIndex + 1);

    // Here is euristic. Chop first 'J' letter for standard Swing classes.
    // Without 'J' bindings look better.
    if(
      shortClassName.length() > 1 && Character.isUpperCase(shortClassName.charAt(1)) &&
      componentClassName.startsWith("javax.swing.") &&
      StringUtil.startsWithChar(shortClassName, 'J')
    ){
      shortClassName = shortClassName.substring(1);
    }
    shortClassName = StringUtil.decapitalize(shortClassName);
    return shortClassName;
  }

  public static String getUniqueBinding(RadRootContainer root, final String baseName) {
    // Generate member name based on current code style
    //noinspection ForLoopThatDoesntUseLoopVariable
    for(int i = 0; true; i++){
      final String nameCandidate = baseName + (i + 1);
      final String binding = CodeStyleManager.getInstance(root.getModule().getProject()).propertyNameToVariableName(
        nameCandidate,
        VariableKind.FIELD
      );

      if (FormEditingUtil.findComponentWithBinding(root, binding) == null) {
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
    final ComponentItem item = Palette.getInstance(editor.getProject()).getItem(insertedComponent.getComponentClassName());
    if ((item != null && item.isAutoCreateBinding()) || insertedComponent.isCustomCreateRequired()) {
      doCreateBindingWhenDrop(editor, insertedComponent);
    }
  }

  private static void doCreateBindingWhenDrop(final GuiEditor editor, final RadComponent insertedComponent) {
    // Now if the inserted component is a input control, we need to automatically create binding
    final String binding = suggestBinding(editor, insertedComponent.getComponentClassName());
    insertedComponent.setBinding(binding);
    insertedComponent.setDefaultBinding(true);

    // Try to create field in the corresponding bound class
    final String classToBind = editor.getRootContainer().getClassToBind();
    if(classToBind != null){
      final PsiClass aClass = FormEditingUtil.findClassToBind(editor.getModule(), classToBind);
      if(aClass != null){
        ApplicationManager.getApplication().runWriteAction(
          new Runnable() {
            public void run() {
              CreateFieldFix.runImpl(editor.getProject(),
                                     editor.getRootContainer(),
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

  protected void processMouseEvent(final MouseEvent e){
    if (e.getID() == MouseEvent.MOUSE_PRESSED) {
      final ComponentItem componentItem = getComponentToInsert();
      if (componentItem != null) {
        processComponentInsert(e.getPoint(), null, componentItem);
      }
    }
    else if (e.getID() == MouseEvent.MOUSE_MOVED) {
      final ComponentItem componentToInsert = getComponentToInsert();
      if (componentToInsert != null) {
        myLastLocation = myGridInsertProcessor.processDragEvent(e.getPoint(), componentToInsert);
        if (myLastLocation.canDrop(getComponentToInsert())) {
          setCursor(FormEditingUtil.getCopyDropCursor());
        }
        else {
          setCursor(FormEditingUtil.getMoveNoDropCursor());
        }
      }
    }
  }

  private ComponentItem getComponentToInsert() {
    return (myComponentToInsert != null)
           ? myComponentToInsert
           : myPaletteManager.getActiveItem(ComponentItem.class);
  }

  // either point or targetContainer is null
  public void processComponentInsert(@Nullable final Point point, @Nullable final RadContainer targetContainer, final ComponentItem item) {
    final DropLocation location = (point != null)
                                  ? GridInsertProcessor.getDropLocation(myEditor.getRootContainer(), point)
                                  : new GridDropLocation(targetContainer, 0, 0);

    processComponentInsert(item, location);
  }

  public void processComponentInsert(final ComponentItem item, final DropLocation location) {
    myEditor.getActiveDecorationLayer().removeFeedback();
    myEditor.setDesignTimeInsets(2);

    if (!validateNestedFormInsert(item)) {
      return;
    }

    if (!myEditor.ensureEditable()) {
      return;
    }

    myInsertedComponent = createInsertedComponent(myEditor, item);
    if (myInsertedComponent == null) {
      return;
    }

    if (location.canDrop(item)) {
      CommandProcessor.getInstance().executeCommand(
        myEditor.getProject(),
        new Runnable(){
          public void run(){
            createBindingWhenDrop(myEditor, myInsertedComponent);

            final RadComponent[] components = new RadComponent[]{myInsertedComponent};
            location.processDrop(myEditor, components, null, item);

            FormEditingUtil.selectSingleComponent(myInsertedComponent);

            if (location.getContainer() != null && location.getContainer().isXY()) {
              Dimension newSize = myInsertedComponent.getPreferredSize();
              Util.adjustSize(myInsertedComponent.getDelegee(), myInsertedComponent.getConstraints(), newSize);
              myInsertedComponent.setSize(newSize);
            }

            if (!GuiDesignerConfiguration.getInstance(myEditor.getProject()).IRIDA_LAYOUT_MODE &&
                myInsertedComponent.getParent() instanceof RadRootContainer &&
                myInsertedComponent instanceof RadAtomicComponent) {
              GridBuildUtil.convertToGrid(myEditor);
              FormEditingUtil.selectSingleComponent(myInsertedComponent);
            }

            checkBindTopLevelPanel();

            if (!mySticky) {
              PaletteManager.getInstance(myEditor.getProject()).clearActiveItem();
            }

            myEditor.refreshAndSave(false);
          }

        },
        null,
        null
      );
    }
    myComponentToInsert = null;
  }

  private boolean validateNestedFormInsert(final ComponentItem item) {
    PsiFile boundForm = item.getBoundForm();
    if (boundForm != null) {
      try {
        Utils.validateNestedFormLoop(FormEditingUtil.buildResourceName(boundForm), new PsiNestedFormLoader(myEditor.getModule()));
      }
      catch(CodeGenerationException ex) {
        Messages.showErrorDialog(myEditor, ex.getMessage(), CommonBundle.getErrorTitle());
        return false;
      }
    }
    return true;
  }

  @Nullable
  public static RadComponent createInsertedComponent(GuiEditor editor, ComponentItem item) {
    if (item.isAnyComponent()) {
      ComponentItem newItem = item.clone();
      ComponentItemDialog dlg = new ComponentItemDialog(editor.getProject(), editor, newItem, true);
      dlg.setTitle(UIDesignerBundle.message("palette.non.palette.component.title"));
      dlg.show();
      if(!dlg.isOK()) {
        return null;
      }

      item = newItem;
    }

    RadComponent result;
    final String id = FormEditingUtil.generateId(editor.getRootContainer());

    if (JScrollPane.class.getName().equals(item.getClassName())) {
      result = new RadScrollPane(editor.getModule(), id);
    }
    else if (item == Palette.getInstance(editor.getProject()).getPanelItem()) {
      result = new RadContainer(editor.getModule(), id);
    }
    else {
      if (VSpacer.class.getName().equals(item.getClassName())) {
        result = new RadVSpacer(editor.getModule(), id);
      }
      else if (HSpacer.class.getName().equals(item.getClassName())) {
        result = new RadHSpacer(editor.getModule(), id);
      }
      else if (JTabbedPane.class.getName().equals(item.getClassName())) {
        result = new RadTabbedPane(editor.getModule(), id);
      }
      else if (JSplitPane.class.getName().equals(item.getClassName())) {
        result = new RadSplitPane(editor.getModule(), id);
      }
      else if (JToolBar.class.getName().equals(item.getClassName())) {
        result = new RadToolBar(editor.getModule(), id);
      }
      else {
        PsiFile boundForm = item.getBoundForm();
        if (boundForm != null) {
          try {
            result = new RadNestedForm(editor.getModule(), FormEditingUtil.buildResourceName(boundForm), id);
          }
          catch(Exception ex) {
            result = RadErrorComponent.create(
              editor.getModule(),
              id,
              item.getClassName(),
              null,
              ex.getMessage()
            );
          }
        }
        else {
          final ClassLoader loader = LoaderFactory.getInstance(editor.getProject()).getLoader(editor.getFile());
          try {
            final Class aClass = Class.forName(item.getClassName(), true, loader);
            result = new RadAtomicComponent(editor.getModule(), aClass, id);
          }
          catch (final Exception exc) {
            //noinspection NonConstantStringShouldBeStringBuffer
            String errorDescription = Utils.validateJComponentClass(loader, item.getClassName(), true);
            if (errorDescription == null) {
              errorDescription = UIDesignerBundle.message("error.class.cannot.be.instantiated", item.getClassName());
              final String message = FormEditingUtil.getExceptionMessage(exc);
              if (message != null) {
                errorDescription += ": " + message;
              }
            }
            result = RadErrorComponent.create(
              editor.getModule(),
              id,
              item.getClassName(),
              null,
              errorDescription
            );
          }
        }
      }
    }
    result.init(editor, item);
    return result;
  }

  private void checkBindTopLevelPanel() {
    if (myEditor.getRootContainer().getComponentCount() == 1) {
      final RadComponent component = myEditor.getRootContainer().getComponent(0);
      if (component.getBinding() == null) {
        if (component == myInsertedComponent ||
            (component instanceof RadContainer && ((RadContainer) component).getComponentCount() == 1 &&
             component == myInsertedComponent.getParent())) {
          doCreateBindingWhenDrop(myEditor, component);
        }
      }
    }
  }

  protected boolean cancelOperation() {
    myEditor.setDesignTimeInsets(2);
    myEditor.getActiveDecorationLayer().removeFeedback();
    return true;
  }

  public Cursor processMouseMoveEvent(final MouseEvent e) {
    final ComponentItem componentItem = myPaletteManager.getActiveItem(ComponentItem.class);
    if (componentItem != null) {
      return myGridInsertProcessor.processMouseMoveEvent(e.getPoint(), false, componentItem);
    }
    return FormEditingUtil.getMoveNoDropCursor();
  }

  @Override public boolean needMousePressed() {
    return true;
  }
}