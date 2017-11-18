package com.intellij.uiDesigner.designSurface;

import com.intellij.uiDesigner.FormEditingUtil;
import com.intellij.uiDesigner.RadComponent;
import com.intellij.uiDesigner.RadContainer;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public final class Painter {
  /**
   * This color is used to paint decoration of non selected components
   */
  private static final Color NON_SELECTED_BOUNDARY_COLOR = new Color(114, 126, 143);
  /**
   * This color is used to paint decoration of selected components
   */
  private static final Color SELECTED_BOUNDARY_COLOR = new Color(8, 8, 108);
  /**
   * This color is used to paint grid cell for selected container
   */
  private static final Color SELECTED_GRID_COLOR = new Color(47, 67, 96);
  /**
   * This color is used to paint grid cell for non selected container
   */
  private static final Color NON_SELECTED_GRID_COLOR = new Color(130, 140, 155);

  public final static int WEST_MASK = 1;
  public final static int EAST_MASK = 2;
  public final static int NORTH_MASK = 4;
  public final static int SOUTH_MASK = 8;
  private final static int R = 2;
  private final static int GAP = R;
  private static final int NW = 0;
  private static final int N = 1;
  private static final int NE = 2;
  private static final int E = 3;
  private static final int SE = 4;
  private static final int S = 5;
  private static final int SW = 6;
  private static final int W = 7;

  private Painter() {
  }

  public static void paintComponentDecoration(final GuiEditor editor, final RadComponent component, final Graphics g){
    // Collect selected components and paint decoration for non selected components
    final ArrayList<RadComponent> selection = new ArrayList<RadComponent>();
    final Rectangle layeredPaneRect = editor.getLayeredPane().getVisibleRect();
    FormEditingUtil.iterate(
      component,
      new FormEditingUtil.ComponentVisitor<RadComponent>() {
        public boolean visit(final RadComponent component) {
          if(!component.getDelegee().isShowing()){ // Skip invisible components
            return true;
          }
          final Shape oldClip = g.getClip();
          final RadContainer parent = component.getParent();
          if(parent != null){
            final Point p = SwingUtilities.convertPoint(component.getDelegee(), 0, 0, editor.getLayeredPane());
            final Rectangle visibleRect = layeredPaneRect.intersection(new Rectangle(p.x, p.y, parent.getWidth(), parent.getHeight()));
            g.setClip(visibleRect);
          }
          if(component.isSelected()){ // we will paint selection later
            selection.add(component);
          }
          else{
            paintComponentBoundsImpl(editor, component, g);
          }
          paintGridOutline(editor, component, g);
          if(parent != null){
            g.setClip(oldClip);
          }
          return true;
        }
      }
    );

    // Let's paint decoration for selected components
    for(int i = selection.size() - 1; i >= 0; i--){
      final Shape oldClip = g.getClip();
      final RadComponent c = selection.get(i);
      final RadContainer parent = c.getParent();
      if(parent != null){
        final Point p = SwingUtilities.convertPoint(c.getDelegee(), 0, 0, editor.getLayeredPane());
        final Rectangle visibleRect = layeredPaneRect.intersection(new Rectangle(p.x, p.y, parent.getWidth(), parent.getHeight()));
        g.setClip(visibleRect);
      }
      paintComponentBoundsImpl(editor, c, g);
      if(parent != null){
        g.setClip(oldClip);
      }
    }
  }

  /**
   * Paints container border. For grids the method also paints vertical and
   * horizontal lines that indicate bounds of the rows and columns.
   * Method does nothing if the <code>component</code> is not an instance
   * of <code>RadContainer</code>.
   */
  private static void paintComponentBoundsImpl(final GuiEditor editor, final RadComponent component, final Graphics g){
    if (component == null) {
      //noinspection HardCodedStringLiteral
      throw new IllegalArgumentException("component cannot be null");
    }
    if (!(component instanceof RadContainer)){
      return;
    }
    final Point point = SwingUtilities.convertPoint(
      component.getDelegee(),
      0,
      0,
      editor.getRootContainer().getDelegee()
    );
    g.translate(point.x, point.y);
    try{
      if (component.isSelected()) {
        g.setColor(SELECTED_BOUNDARY_COLOR);
      }
      else {
        g.setColor(NON_SELECTED_BOUNDARY_COLOR);
      }
      g.drawRect(0, 0, component.getWidth() - 1, component.getHeight() - 1);
    }finally{
      g.translate(-point.x, -point.y);
    }
  }

  /**
   * This method paints grid bounds for "grid" containers
   */
  public static void paintGridOutline(final GuiEditor editor, @NotNull final RadComponent component, final Graphics g){
    if (component == null) {
      throw new IllegalArgumentException("component cannot be null");
    }
    if (!(component instanceof RadContainer)){
      return;
    }
    final RadContainer container = (RadContainer)component;
    if(!container.isGrid()){
      return;
    }

    final Point point = SwingUtilities.convertPoint(
      component.getDelegee(),
      0,
      0,
      editor.getRootContainer().getDelegee()
    );
    g.translate(point.x, point.y);
    try{
      // Paint grid
      final GridLayoutManager gridLayout = (GridLayoutManager)container.getLayout();
      if (component.isSelected()) {
        g.setColor(SELECTED_GRID_COLOR);
      }
      else {
        g.setColor(NON_SELECTED_GRID_COLOR);
      }

      // Horizontal lines
      final int width = component.getWidth();
      final int[] horzGridLines = gridLayout.getHorizontalGridLines();
      final int height = component.getHeight();
      final int[] vertGridLines = gridLayout.getVerticalGridLines();

      boolean[][] horzSkippedLineSegments = null;
      boolean[][] vertSkippedLineSegments = null;
      for(RadComponent childComponent: container.getComponents()) {
        final GridConstraints constraints = childComponent.getConstraints();
        if (constraints.getColSpan() > 1) {
          if (vertSkippedLineSegments == null) {
            vertSkippedLineSegments = new boolean[vertGridLines.length][height+4];
          }
          for(int col = constraints.getColumn()+1; col < constraints.getColumn() + constraints.getColSpan(); col++) {
            for(int y=horzGridLines [constraints.getRow()]+4;
                y<horzGridLines [constraints.getRow() + constraints.getRowSpan()]-4;
                y++) {
              vertSkippedLineSegments [col][y] = true;
            }
          }

        }
        if (constraints.getRowSpan() > 1) {
          if (horzSkippedLineSegments == null) {
            horzSkippedLineSegments = new boolean[horzGridLines.length][width+4];
          }
          for(int row = constraints.getRow()+1; row < constraints.getRow() + constraints.getRowSpan(); row++) {
            for(int x=vertGridLines [constraints.getColumn()]+4;
                x<vertGridLines [constraints.getColumn() + constraints.getColSpan()]-4;
                x++) {
              horzSkippedLineSegments [row][x] = true;
            }
          }
        }
      }


      for (int i = 1; i < horzGridLines.length - 1; i++) {
        final int y = horzGridLines [i];
        // Draw dotted horizontal line
        for(int x = 0; x < width; x+=4) {
          if (horzSkippedLineSegments == null || (!horzSkippedLineSegments[i][x] && !horzSkippedLineSegments[i][x+1] && !horzSkippedLineSegments[i][x+2])) {
            UIUtil.drawLine(g, x, y, Math.min(x + 2, width - 1), y);
          }
        }
      }

      // Vertical lines
      for (int i = 1; i < vertGridLines.length - 1; i++) {
        final int x = vertGridLines [i];
        // Draw dotted vertical line
        for(int y = 0; y < height; y+=4) {
          if (vertSkippedLineSegments == null || (!vertSkippedLineSegments[i][y] && !vertSkippedLineSegments[i][y+1] && !vertSkippedLineSegments[i][y+2])) {
            UIUtil.drawLine(g, x, y, x, Math.min(y + 2, height - 1));
          }
        }
      }
    }finally{
      g.translate(-point.x, -point.y);
    }
  }

  /**
   * Paints selection for the specified <code>component</code>.
   */
  public static void paintSelectionDecoration(final RadComponent component, final Graphics g){
    if (component == null) {
      //noinspection HardCodedStringLiteral
      throw new IllegalArgumentException("component cannot be null");
    }
    if (component.isSelected()) {
      g.setColor(Color.BLUE);
      final Point[] points = getPoints(component.getWidth(), component.getHeight());
      for (final Point point : points) {
        g.fillRect(point.x - R, point.y - R, 2 * R + 1, 2 * R + 1);
      }
    }
  }

  /**
   * @param x in component's coord system
   * @param y in component's coord system
   */
  public static int getResizeMask(@NotNull final RadComponent component, final int x, final int y) {
    //noinspection ConstantConditions
    if (component == null) {
      throw new IllegalArgumentException("component cannot be null");
    }
    if (component.getParent() == null || !component.isSelected()) {
      return 0;
    }

    // only components in XY can be resized...
    /*
    if (!component.getParent().isXY()) {
      return 0;
    }
    */

    final int width=component.getWidth();
    final int height=component.getHeight();

    final Point[] points = getPoints(width, height);

    if (isInside(x, y, points[SE])) {
      return EAST_MASK | SOUTH_MASK;
    }
    else if (isInside(x,y,points[NW])) {
      return WEST_MASK | NORTH_MASK;
    }
    else if(isInside(x,y,points[N])){
      return NORTH_MASK;
    }
    else if(isInside(x,y,points[NE])){
      return EAST_MASK | NORTH_MASK;
    }
    else if (isInside(x, y, points[W])){
      return WEST_MASK;
    }
    else if (isInside(x, y, points[E])){
      return EAST_MASK;
    }
    else if (isInside(x, y, points[SW])){
      return WEST_MASK | SOUTH_MASK;
    }
    else if (isInside(x, y, points[S])){
      return SOUTH_MASK;
    }
    else{
      return 0;
    }
  }

  private static boolean isInside(final int x, final int y, final Point r) {
    return x >= r.x - R && x <= r.x + R && y >= r.y - R && y <= r.y + R;
  }

  public static int getResizeCursor(final int resizeMask){
    if (resizeMask == (WEST_MASK | NORTH_MASK)) {
      return Cursor.NW_RESIZE_CURSOR;
    }
    else if (resizeMask == NORTH_MASK) {
      return Cursor.N_RESIZE_CURSOR;
    }
    else if (resizeMask == (EAST_MASK | NORTH_MASK)) {
      return Cursor.NE_RESIZE_CURSOR;
    }
    else if (resizeMask == WEST_MASK) {
      return Cursor.W_RESIZE_CURSOR;
    }
    else if (resizeMask == EAST_MASK) {
      return Cursor.E_RESIZE_CURSOR;
    }
    else if (resizeMask == (WEST_MASK | SOUTH_MASK)) {
      return Cursor.SW_RESIZE_CURSOR;
    }
    else if (resizeMask == SOUTH_MASK) {
      return Cursor.S_RESIZE_CURSOR;
    }
    else if (resizeMask == (EAST_MASK | SOUTH_MASK)) {
      return Cursor.SE_RESIZE_CURSOR;
    }
    else {
      //noinspection HardCodedStringLiteral
      throw new IllegalArgumentException("unknown resizeMask: " + resizeMask);
    }
  }

  public static Point[] getPoints(final int width, final int height) {
    final Point[] points = new Point[8];

    points[NW] = new Point(GAP, GAP); // NW
    points[N] = new Point(width / 2, GAP); // N
    points[NE] = new Point(width - GAP - 1, GAP); // NE
    points[E] = new Point(width - GAP - 1, height / 2); // E
    points[SE] = new Point(width - GAP - 1, height - GAP - 1); // SE
    points[S] = new Point(width / 2, height - GAP - 1); // S
    points[SW] = new Point(GAP, height - GAP - 1); // SW
    points[W] = new Point(GAP, height / 2); // W

    return points;
  }
}