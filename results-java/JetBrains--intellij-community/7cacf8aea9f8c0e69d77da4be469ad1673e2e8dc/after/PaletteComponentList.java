package com.intellij.ide.palette.impl;

import com.intellij.ide.dnd.*;
import com.intellij.ide.palette.PaletteGroup;
import com.intellij.ide.palette.PaletteItem;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.PopupHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicListUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * @author yole
 */
public class PaletteComponentList extends JList {
  private Project myProject;
  private PaletteGroup myGroup;
  private int myHoverIndex = -1;
  private int myBeforeClickSelectedRow = -1;
  private int myDropTargetIndex = -1;
  private boolean myNeedClearSelection = false;

  public PaletteComponentList(Project project, PaletteGroup group) {
    myProject = project;
    myGroup = group;
    setModel(new AbstractListModel() {
      public int getSize() {
        return myGroup.getItems().length;
      }

      public Object getElementAt(int index) {
        return myGroup.getItems() [index];
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        setHoverIndex(locationToIndex(e.getPoint()));
      }

      @Override public void mouseExited(MouseEvent e) {
        setHoverIndex(-1);
      }

      @Override public void mousePressed(MouseEvent e) {
        myNeedClearSelection = (SwingUtilities.isLeftMouseButton(e) &&
                                myBeforeClickSelectedRow >= 0 &&
                                locationToIndex(e.getPoint()) == myBeforeClickSelectedRow &&
                                !e.isControlDown() && !e.isShiftDown());
      }

      @Override public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) &&
            myBeforeClickSelectedRow >= 0 &&
            locationToIndex(e.getPoint()) == myBeforeClickSelectedRow &&
            !e.isControlDown() && !e.isShiftDown() && myNeedClearSelection) {
          clearSelection();
        }
      }
    });

    addMouseListener(new PopupHandler() {
      public void invokePopup(final Component comp, final int x, final int y) {
        requestFocusInWindow();
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            int index = locationToIndex(new Point(x, y));
            PaletteItem[] items = myGroup.getItems();
            if (index >= 0 && index < items.length) {
              if (getSelectedIndex() != index) {
                addSelectionInterval(index, index);
              }
              PaletteItem item = items [index];
              ActionGroup group = item.getPopupActionGroup();
              if (group != null) {
                ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.UNKNOWN, group);
                popupMenu.getComponent().show(comp, x, y);
              }
            }
          }
        });
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        setHoverIndex(locationToIndex(e.getPoint()));
      }
    });

    setCellRenderer(new ComponentCellRenderer());

    setVisibleRowCount(0);
    setLayoutOrientation(HORIZONTAL_WRAP);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    final DnDManager dndManager = DnDManager.getInstance(project);
    dndManager.registerSource(new MyDnDSource(), this);
    dndManager.registerTarget(new MyDnDTarget(), this);

    initActions();
  }

  private void setHoverIndex(final int index) {
    if (index != myHoverIndex) {
      if (myHoverIndex >= 0) repaint(getCellBounds(myHoverIndex, myHoverIndex));
      myHoverIndex = index;
      if (myHoverIndex >= 0) repaint(getCellBounds(myHoverIndex, myHoverIndex));
    }
  }

  private void setDropTargetIndex(final int index) {
    if (index != myDropTargetIndex) {
      myDropTargetIndex = index;
      repaint();
    }
  }

  @Override public void updateUI() {
    setUI(new ComponentListUI());
    invalidate();
  }

  private void initActions() {
    @NonNls ActionMap map = getActionMap();
    map.put( "selectPreviousRow", new MoveFocusAction( map.get( "selectPreviousRow" ), false ) );
    map.put( "selectNextRow", new MoveFocusAction( map.get( "selectNextRow" ), true ) );
    map.put( "selectPreviousColumn", new MoveFocusAction( new ChangeColumnAction( map.get( "selectPreviousColumn" ), false ), false ) );
    map.put( "selectNextColumn", new MoveFocusAction( new ChangeColumnAction( map.get( "selectNextColumn" ), true ), true ) );
  }

  Integer myTempWidth;

  public int getWidth () {
      return (myTempWidth == null) ? super.getWidth () : myTempWidth.intValue ();
  }

  public int getPreferredHeight(final int width) {
    myTempWidth = new Integer(width);
    try {
      return getUI().getPreferredSize(this).height;
    }
    finally {
      myTempWidth = null;
    }
  }

  public void takeFocusFrom(PaletteGroupHeader paletteGroup, int indexToSelect) {
    if (indexToSelect == -1) {
      //this is not 'our' CategoryButton so we'll assume it's the one below this category list
      indexToSelect = getModel().getSize() - 1;
    }
    else if (getModel().getSize() == 0) {
      indexToSelect = -1;
    }
    requestFocus();
    setSelectedIndex(indexToSelect);
    if (indexToSelect >= 0) {
      ensureIndexIsVisible(indexToSelect);
    }
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (myDropTargetIndex >= 0) {
      int dropLineY;
      Rectangle rc;
      if (myDropTargetIndex == myGroup.getItems().length) {
        rc = getCellBounds(myDropTargetIndex-1, myDropTargetIndex-1);
        dropLineY = (int)rc.getMaxY()-1;
      }
      else {
        rc = getCellBounds(myDropTargetIndex, myDropTargetIndex);
        dropLineY = rc.y;
      }
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(Color.BLUE);
      g2d.setStroke(new BasicStroke(2.0f));
      g2d.drawLine(rc.x, dropLineY, rc.x+rc.width, dropLineY);
      g2d.drawLine(rc.x, dropLineY-2, rc.x, dropLineY+2);
      g2d.drawLine(rc.x+rc.width, dropLineY-2, rc.x+rc.width, dropLineY+2);
    }
  }

  class ComponentListUI extends BasicListUI {
    @Override protected void updateLayoutState() {
      super.updateLayoutState();

      if (list.getLayoutOrientation() == JList.HORIZONTAL_WRAP) {
        Insets insets = list.getInsets();
        int listWidth = list.getWidth() - (insets.left + insets.right);
        if (listWidth >= cellWidth) {
          int columnCount = listWidth / cellWidth;
          cellWidth = (columnCount == 0) ? 1 : listWidth / columnCount;
        }
      }
    }

    @Override protected void installListeners() {
      addMouseListener(new MouseAdapter() {
        @Override public void mousePressed(MouseEvent e) {
          myBeforeClickSelectedRow = list.getSelectedIndex();
        }
      });
      super.installListeners();
    }
  }

  private class ComponentCellRenderer extends ColoredListCellRenderer {
    private Border myHoverBorder = BorderFactory.createLineBorder(Color.BLUE);

    protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
      PaletteItem paletteItem = (PaletteItem) value;
      clear();
      //setBorder(index == myHoverIndex ? myHoverBorder : null);
      paletteItem.customizeCellRenderer(this, selected, hasFocus);
    }
  }

  private class MoveFocusAction extends AbstractAction {
    private Action defaultAction;
    private boolean focusNext;

    public MoveFocusAction(Action defaultAction, boolean focusNext) {
      this.defaultAction = defaultAction;
      this.focusNext = focusNext;
    }

    public void actionPerformed(ActionEvent e) {
      int selIndexBefore = getSelectedIndex();
      defaultAction.actionPerformed(e);
      int selIndexCurrent = getSelectedIndex();
      if (selIndexBefore != selIndexCurrent) return;

      if (focusNext && 0 == selIndexCurrent) return;

      KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
      Container container = kfm.getCurrentFocusCycleRoot();
      FocusTraversalPolicy policy = container.getFocusTraversalPolicy();
      if (null == policy) policy = kfm.getDefaultFocusTraversalPolicy();
      Component next = focusNext
                       ? policy.getComponentAfter(container, PaletteComponentList.this)
                       : policy.getComponentBefore(container, PaletteComponentList.this);
      if (null != next && next instanceof PaletteGroupHeader) {
        clearSelection();
        next.requestFocus();
        ((PaletteGroupHeader)next).scrollRectToVisible(next.getBounds());
      }
    }
  }

  private class ChangeColumnAction extends AbstractAction {
    private Action defaultAction;
    private boolean selectNext;

    public ChangeColumnAction(Action defaultAction, boolean selectNext) {
      this.defaultAction = defaultAction;
      this.selectNext = selectNext;
    }

    public void actionPerformed(ActionEvent e) {
      int selIndexBefore = getSelectedIndex();
      defaultAction.actionPerformed(e);
      int selIndexCurrent = getSelectedIndex();
      if ((selectNext && selIndexBefore < selIndexCurrent) || (!selectNext && selIndexBefore > selIndexCurrent)) return;

      if (selectNext) {
        if (selIndexCurrent == selIndexBefore + 1) selIndexCurrent++;
        if (selIndexCurrent < getModel().getSize() - 1) {
          setSelectedIndex(selIndexCurrent + 1);
          scrollRectToVisible(getCellBounds(selIndexCurrent + 1, selIndexCurrent + 1));
        }
      }
      else {
        if (selIndexCurrent > 0) {
          setSelectedIndex(selIndexCurrent - 1);
          scrollRectToVisible(getCellBounds(selIndexCurrent - 1, selIndexCurrent - 1));
        }
      }
    }
  }

  private class MyDnDTarget implements DnDTarget {

    public boolean update(DnDEvent aEvent) {
      setHoverIndex(-1);
      if (aEvent.getAttachedObject() instanceof PaletteItem) {
        setDropTargetIndex(locationToTargetIndex(aEvent.getPoint()));
        aEvent.setDropPossible(true, null);
      }
      else {
        setDropTargetIndex(-1);
        aEvent.setDropPossible(false, null);
      }
      return false;
    }

    public void drop(DnDEvent aEvent) {
      setDropTargetIndex(-1);
      if (aEvent.getAttachedObject() instanceof PaletteItem) {
        int index = locationToTargetIndex(aEvent.getPoint());
        if (index >= 0) {
          myGroup.handleDrop(myProject, (PaletteItem) aEvent.getAttachedObject(), index);
        }
      }
    }

    public void cleanUpOnLeave() {
      setDropTargetIndex(-1);
    }

    private int locationToTargetIndex(Point location) {
      int row = locationToIndex(location);
      if (row < 0) {
        return -1;
      }
      Rectangle rc = getCellBounds(row, row);
      return location.y < rc.getCenterY() ? row : row + 1;
    }

  }

  private class MyDnDSource implements DnDSource {
    public boolean canStartDragging(DnDAction action, Point dragOrigin) {
      int index = locationToIndex(dragOrigin);
      return index >= 0;
    }

    public DnDDragStartBean startDragging(DnDAction action, Point dragOrigin) {
      int index = locationToIndex(dragOrigin);
      if (index < 0) return null;
      return myGroup.getItems() [index].startDragging();
    }

    @Nullable
    public Pair<Image, Point> createDraggedImage(DnDAction action, Point dragOrigin) {
      return null;
    }

  }
}