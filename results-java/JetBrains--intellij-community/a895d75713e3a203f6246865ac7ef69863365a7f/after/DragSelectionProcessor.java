package com.intellij.uiDesigner;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.uiDesigner.core.GridConstraints;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import gnu.trove.TIntArrayList;

/**
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public final class DragSelectionProcessor extends EventProcessor{
  private static final Logger LOG = Logger.getInstance("#com.intellij.uiDesigner.DragSelectionProcessor");

  /**
   * We have not start drag/cancel drop if mouse pointer trembles in small area
   */
  private static final int TREMOR = 3;

  private final GuiEditor myEditor;
  private MyPreviewer myPreviewer;
  private Point myLastPoint;

  private Point myPressPoint;

  /**
   * Arrays of components to be dragged
   */
  private ArrayList<RadComponent> mySelection;
  private GridConstraints[] myOriginalConstraints;
  private Rectangle[] myOriginalBounds;
  private RadContainer[] myOriginalParents;

  /*
   * Required to undo drop
   */
  private DropInfo myDropInfo;
  private boolean myDragStarted;

  private final GridInsertProcessor myGridInsertProcessor;

  public DragSelectionProcessor(@NotNull final GuiEditor editor){
    //noinspection ConstantConditions
    LOG.assertTrue(editor!=null);

    myEditor = editor;
    myGridInsertProcessor = new GridInsertProcessor(editor);
  }

  private void processStartDrag(final MouseEvent e) {
    myPreviewer = new MyPreviewer();

    // Store selected components
    mySelection = FormEditingUtil.getSelectedComponents(myEditor);

    // Store original constraints and parents. This information is required
    // to restore initial state if drag is canceled.
    myOriginalConstraints = new GridConstraints[mySelection.size()];
    myOriginalBounds = new Rectangle[mySelection.size()];
    myOriginalParents = new RadContainer[mySelection.size()];
    for (int i = 0; i < mySelection.size(); i++) {
      final RadComponent component = mySelection.get(i);
      myOriginalConstraints[i] = component.getConstraints().store();
      myOriginalBounds[i] = component.getBounds();
      myOriginalParents[i] = component.getParent();
    }

    // It's very important to get component under mouse before the components are
    // removed from their parents.
    final RadComponent componentUnderMouse = FormEditingUtil.getRadComponentAt(myEditor, myPressPoint.x, myPressPoint.y);
    int dx = 0;
    int dy = 0;

    // Remove components from their parents.
    for (final RadComponent c : mySelection) {
      c.getParent().removeComponent(c);
    }

    // Place selected components to the drag layer.
    for (int i = 0; i < mySelection.size(); i++) {
      final RadComponent c = mySelection.get(i);
      final JComponent delegee = c.getDelegee();
      final Point point = SwingUtilities.convertPoint(
        myOriginalParents[i].getDelegee(),
        delegee.getLocation(),
        myEditor.getDragLayer()
      );
      delegee.setLocation(point);
      myEditor.getDragLayer().add(delegee);

      // make sure mouse cursor is inside the component being dragged
      if (c == componentUnderMouse) {
        if (delegee.getX() > e.getX() || delegee.getX() + delegee.getWidth() < e.getX()) {
          dx = e.getX() - (delegee.getX() + delegee.getWidth() / 2);
        }
        if (delegee.getY() > e.getY() || delegee.getY() + delegee.getHeight() < e.getY()) {
          dy = e.getY() - (delegee.getY() + delegee.getHeight() / 2);
        }
      }
    }

    for (RadComponent aMySelection : mySelection) {
      aMySelection.shift(dx, dy);
    }

    myEditor.refresh();
    myLastPoint=e.getPoint();
  }

  /**
   * "Drops" selection at the specified <code>point</code>.
   *
   * @param point point in coordinates of drag layer (it's convenient here).
   *
   * @param copyOnDrop
   * @return true if the selected components were successfully dropped
   */
  private boolean dropSelection(@NotNull final Point point, boolean mouseReleased, final boolean copyOnDrop) {
    //noinspection ConstantConditions
    LOG.assertTrue(point!=null);

    GridInsertProcessor.GridInsertLocation location = null;
    if (mouseReleased) {
      myGridInsertProcessor.removeFeedbackPainter();
      location = myGridInsertProcessor.getGridInsertLocation(point.x, point.y);
      if (!myGridInsertProcessor.isDropInsertAllowed(location, mySelection.size())) {
        location = null;
      }
    }

    if (copyOnDrop) {
      final String serializedComponents = CutCopyPasteSupport.serializeForCopy(myEditor, mySelection);
      cancelDrag();

      TIntArrayList xs = new TIntArrayList();
      TIntArrayList ys = new TIntArrayList();
      mySelection.clear();
      CutCopyPasteSupport.loadComponentsToPaste(myEditor, serializedComponents, xs, ys, mySelection);
    }

    if (!FormEditingUtil.canDrop(myEditor, point.x, point.y, mySelection.size()) &&
        (location == null || location.getMode() == GridInsertProcessor.GridInsertMode.None)) {
      return false;
    }

    final int[] dx = new int[mySelection.size()];
    final int[] dy = new int[mySelection.size()];
    for (int i = 0; i < mySelection.size(); i++) {
      final RadComponent component = mySelection.get(i);
      dx[i] = component.getX() - point.x;
      dy[i] = component.getY() - point.y;
    }

    final RadComponent[] components = mySelection.toArray(new RadComponent[mySelection.size()]);
    if (location != null && location.getMode() != GridInsertProcessor.GridInsertMode.None) {
      myDropInfo = myGridInsertProcessor.processGridInsertOnDrop(location, components, myOriginalConstraints);
    }
    else {
      myDropInfo = FormEditingUtil.drop(
        myEditor,
        point.x,
        point.y,
        components,
        dx,
        dy
      );
    }

    if (copyOnDrop) {
      FormEditingUtil.clearSelection(myEditor.getRootContainer());
      for(RadComponent component: mySelection) {
        component.setSelected(true);
      }
    }

    if (mouseReleased) {
      for(int i=0; i<myOriginalConstraints.length; i++) {
        if (myOriginalParents [i].isGrid()) {
          FormEditingUtil.deleteEmptyGridCells(myOriginalParents [i], myOriginalConstraints [i]);
        }
      }
    }

    return true;
  }

  private void cancelDrop(){
    // Remove dropped preview
    for (final RadComponent component : mySelection) {
      final RadContainer parent = component.getParent();
      LOG.assertTrue(parent != null);
      parent.removeComponent(component);
    }

    if (myDropInfo != null && myDropInfo.myTopmostContainer!=null) {
      myDropInfo.myTopmostContainer.setSize(myDropInfo.myTopmostContainerSize);
    }
    myGridInsertProcessor.removeFeedbackPainter();
  }

  private void processMouseReleased(final boolean copyOnDrop){
    // Cancel all pending requests for preview.
    myPreviewer.dispose();

    // Try to drop selection at the point of mouse event.
    if(dropSelection(myLastPoint, true, copyOnDrop)){
      myEditor.refreshAndSave(true);
    } else {
      cancelDrag();
    }

    myEditor.repaintLayeredPane();
  }

  protected boolean cancelOperation(){
    if (!myDragStarted) {
      return true;
    }
    // Cancel all pending requests for preview.
    myPreviewer.dispose();
    // Try to drop selection at the point of mouse event.
    cancelDrag();
    myGridInsertProcessor.removeFeedbackPainter();
    myEditor.repaintLayeredPane();
    return true;
  }

  /**
   * Cancels drag of component. The method returns all dragged components
   * to their original places and returns them their original sizes.
   */
  private void cancelDrag() {
    for (int i = 0; i < mySelection.size(); i++) {
      final RadComponent c = mySelection.get(i);
      c.getConstraints().restore(myOriginalConstraints[i]);
      c.setBounds(myOriginalBounds[i]);
      final RadContainer originalParent = myOriginalParents[i];
      if (c.getParent() != originalParent) {
        originalParent.addComponent(c);
      }
    }
    myEditor.refresh();
  }

  protected void processKeyEvent(final KeyEvent e) { }

  protected void processMouseEvent(final MouseEvent e){
    if(e.getID()==MouseEvent.MOUSE_PRESSED){
      myPressPoint = e.getPoint();
    }
    else if(e.getID()==MouseEvent.MOUSE_RELEASED){
      if (myDragStarted) {
        processMouseReleased(e.isControlDown());
      }
      else if (e.isControlDown()) {
        RadComponent component = FormEditingUtil.getRadComponentAt(myEditor, e.getX(), e.getY());
        if (component != null) {
          component.setSelected(!component.isSelected());
        }
      }
    }
    else if(e.getID()==MouseEvent.MOUSE_DRAGGED){
      if (!myDragStarted) {
        if ((Math.abs(e.getX() - myPressPoint.getX()) > TREMOR || Math.abs(e.getY() - myPressPoint.getY()) > TREMOR)) {
          processStartDrag(e);
          myDragStarted = true;
        }
      }

      if (myDragStarted) {
        processMouseDragged(e);
      }
    }
  }

  private void processMouseDragged(final MouseEvent e){
    // Restart previewer.
    /*
    myPreviewer.processMouseDragged(e);
    if(myPreviewer.isPreview()){
      return;
    }
    */

    // Move components in the drag layer.
    final int dx = e.getX() - myLastPoint.x;
    final int dy = e.getY() - myLastPoint.y;
    for (RadComponent aMySelection : mySelection) {
      aMySelection.shift(dx, dy);
    }

    myLastPoint=e.getPoint();
    myEditor.getDragLayer().repaint();

    setCursor(myGridInsertProcessor.processMouseMoveEvent(e.getX(), e.getY(), mySelection.size(), e.isControlDown()));
  }

  /**
   * This class gets notifications from "gusture" timer. It's responsible for
   * drop preview.
   */
  private final class MyPreviewer implements ActionListener{
    private static final int DROP_DELAY = 500;
    /**
     * This timer is responsible for drop preview. The timer is restarted each time user
     * is dragging the component. When user stop dragging and drag isn't completed then
     * the timer fires event and user gets drop preview.
     */
    private final Timer myTimer;
    private boolean myPreview;
    private Point myDropPoint;
    /**
     * These are bounds of selection before dropping. This field
     * is required to bring the dropped selection back to the drad layer
     * if user is contining mouse drag.
     */
    private Rectangle[] myOriginalBounds;

    public MyPreviewer(){
      myTimer = new Timer(DROP_DELAY,this);
      myTimer.setRepeats(false);
      //myTimer.start();
    }

    public void actionPerformed(final ActionEvent e){
      LOG.assertTrue(!myPreview);

      // Store current bounds of the selection
      myOriginalBounds=new Rectangle[mySelection.size()];
      for(int i=0;i<mySelection.size();i++){
        final RadComponent component=mySelection.get(i);
        myOriginalBounds[i]=component.getBounds();
      }

      // Try to drop selection

      myPreview = dropSelection(myLastPoint, false, false);
      myDropPoint = myPreview ? myLastPoint : null;
      myEditor.refresh();
    }

    public boolean isPreview(){
      return myPreview;
    }

    /**
     * This method should be invoked if some mouse events come in.
     */
    public void processMouseDragged(final MouseEvent e){
      if(!myPreview){
        myTimer.restart();
        return;
      }

      if(
        (Math.abs(e.getX()-myDropPoint.getX()) > TREMOR || Math.abs(e.getY()-myDropPoint.getY()) > TREMOR)
      ){
        cancelPreview();
        myEditor.refresh();
      }
    }

    /**
     * Disposes the previewer. The method remove preview (if any).
     */
    public void dispose(){
      myTimer.stop();
      cancelPreview();
    }

    /**
     * Cancels preview: removes dropped selection form container and bring it back to
     * the drag layer.
     */
    private void cancelPreview(){
      if(myPreview){
        cancelDrop();

        // Bring selection back to the dragged layer
        for (int i = 0; i < mySelection.size(); i++) {
          final RadComponent component = mySelection.get(i);
          component.setBounds(myOriginalBounds[i]);
          myEditor.getDragLayer().add(component.getDelegee());
        }

        myPreview=false;
      }
    }
  }
}