/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.uiDesigner.radComponents;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.GridChangeUtil;
import com.intellij.uiDesigner.UIFormXmlConstants;
import com.intellij.uiDesigner.XmlWriter;
import com.intellij.uiDesigner.designSurface.*;
import com.intellij.uiDesigner.actions.*;
import com.intellij.uiDesigner.compiler.Utils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.lw.FormLayoutSerializer;
import com.intellij.uiDesigner.propertyInspector.Property;
import com.intellij.uiDesigner.propertyInspector.PropertyEditor;
import com.intellij.uiDesigner.propertyInspector.PropertyRenderer;
import com.intellij.uiDesigner.propertyInspector.editors.IntRegexEditor;
import com.intellij.uiDesigner.propertyInspector.properties.AlignPropertyProvider;
import com.intellij.uiDesigner.propertyInspector.properties.HorzAlignProperty;
import com.intellij.uiDesigner.propertyInspector.properties.IntFieldProperty;
import com.intellij.uiDesigner.propertyInspector.properties.VertAlignProperty;
import com.intellij.uiDesigner.propertyInspector.renderers.InsetsPropertyRenderer;
import com.intellij.uiDesigner.snapShooter.SnapshotContext;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ArrayUtil;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;

/**
 * @author yole
 */
public class RadFormLayoutManager extends RadGridLayoutManager implements AlignPropertyProvider {
  private FormLayoutColumnProperties myPropertiesPanel;
  private Map<RadComponent, MyPropertyChangeListener> myListenerMap = new HashMap<RadComponent, MyPropertyChangeListener>();

  @NonNls private static final String ENCODED_FORMSPEC_GROW = "d:grow";

  @Nullable public String getName() {
    return UIFormXmlConstants.LAYOUT_FORM;
  }

  @Override @Nullable
  public LayoutManager createLayout() {
    return new FormLayout(ENCODED_FORMSPEC_GROW, ENCODED_FORMSPEC_GROW);
  }

  @Override
  public void changeContainerLayout(RadContainer container) throws IncorrectOperationException {
    if (container.getLayout() instanceof GridLayoutManager) {
      GridLayoutManager grid = (GridLayoutManager) container.getLayout();

      List<RadComponent> contents = collectComponents(container);

      RowSpec[] rowSpecs = new RowSpec [grid.getRowCount() * 2 - 1];
      ColumnSpec[] colSpecs = new ColumnSpec [grid.getColumnCount() * 2 - 1];

      for(int i=0; i<grid.getRowCount(); i++) {
        rowSpecs [i*2] = grid.willGrow(true, i) ? new RowSpec(ENCODED_FORMSPEC_GROW) : FormFactory.DEFAULT_ROWSPEC;
        if (i*2+1 < rowSpecs.length) {
          rowSpecs [i*2+1] = FormFactory.RELATED_GAP_ROWSPEC;
        }
      }
      for(int i=0; i<grid.getColumnCount(); i++) {
        colSpecs [i*2] = grid.willGrow(false, i) ? new ColumnSpec(ENCODED_FORMSPEC_GROW) : FormFactory.DEFAULT_COLSPEC;
        if (i*2+1 < colSpecs.length) {
          colSpecs [i*2+1] = FormFactory.RELATED_GAP_COLSPEC;
        }
      }

      container.setLayoutManager(this, new FormLayout(colSpecs, rowSpecs));

      for(RadComponent c: contents) {
        GridConstraints gc = c.getConstraints();
        gc.setRow(gc.getRow() * 2);
        gc.setColumn(gc.getColumn() * 2);
        container.addComponent(c);
      }
    }
    else if (container.getLayoutManager().isIndexed()) {
      convertIndexedToForm(container);
    }
    else if (container.getComponentCount() == 0) {
      container.setLayoutManager(this, new FormLayout(ENCODED_FORMSPEC_GROW, ENCODED_FORMSPEC_GROW));
    }
    else {
      throw new IncorrectOperationException("Cannot change from " + container.getLayout() + " to grid layout");
    }
  }

  private void convertIndexedToForm(final RadContainer container) {
    List<RadComponent> components = collectComponents(container);
    int maxSizePolicy = 0;
    for(RadComponent c: components) {
      maxSizePolicy = Math.max(maxSizePolicy, c.getConstraints().getHSizePolicy());
    }
    ColumnSpec[] colSpecs = new ColumnSpec [components.size() * 2 - 1];
    for(int i=0; i<components.size(); i++) {
      colSpecs [i*2] = components.get(i).getConstraints().getHSizePolicy() == maxSizePolicy
                       ? new ColumnSpec(ENCODED_FORMSPEC_GROW)
                       : FormFactory.DEFAULT_COLSPEC;
      if (i*2+1 < colSpecs.length) {
        colSpecs [i*2+1] = FormFactory.RELATED_GAP_COLSPEC;
      }
    }
    container.setLayoutManager(this, new FormLayout(colSpecs, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC } ));
    for(int i=0; i<components.size(); i++) {
      GridConstraints gc = components.get(i).getConstraints();
      gc.setRow(0);
      gc.setColumn(i*2);
      gc.setRowSpan(1);
      gc.setColSpan(1);
      container.addComponent(components.get(i));
    }
  }

  private List<RadComponent> collectComponents(final RadContainer container) {
    List<RadComponent> contents = new ArrayList<RadComponent>();
    for(int i=container.getComponentCount()-1; i >= 0; i--) {
      final RadComponent component = container.getComponent(i);
      if (!(component instanceof RadHSpacer) && !(component instanceof RadVSpacer)) {
        contents.add(0, component);
      }
      container.removeComponent(component);
    }
    return contents;
  }

  @Override
  public void writeLayout(final XmlWriter writer, final RadContainer radContainer) {
    FormLayout layout = (FormLayout) radContainer.getLayout();
    for(int i=1; i<=layout.getRowCount(); i++) {
      RowSpec rowSpec = layout.getRowSpec(i);
      writer.startElement(UIFormXmlConstants.ELEMENT_ROWSPEC);
      try {
        writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_VALUE, Utils.getEncodedSpec(rowSpec));
      }
      finally {
        writer.endElement();
      }
    }
    for(int i=1; i<=layout.getColumnCount(); i++) {
      ColumnSpec columnSpec = layout.getColumnSpec(i);
      writer.startElement(UIFormXmlConstants.ELEMENT_COLSPEC);
      try {
        writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_VALUE, Utils.getEncodedSpec(columnSpec));
      }
      finally {
        writer.endElement();
      }
    }
    writeGroups(writer, UIFormXmlConstants.ELEMENT_ROWGROUP, layout.getRowGroups());
    writeGroups(writer, UIFormXmlConstants.ELEMENT_COLGROUP, layout.getColumnGroups());
  }

  private static void writeGroups(final XmlWriter writer, final String elementName, final int[][] groups) {
    for(int[] group: groups) {
      writer.startElement(elementName);
      try {
        for(int member: group) {
          writer.startElement(UIFormXmlConstants.ELEMENT_MEMBER);
          writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_INDEX, member);
          writer.endElement();
        }
      }
      finally {
        writer.endElement();
      }
    }
  }

  @Override
  public void addComponentToContainer(final RadContainer container, final RadComponent component, final int index) {
    MyPropertyChangeListener listener = new MyPropertyChangeListener(component);
    myListenerMap.put(component, listener);
    component.addPropertyChangeListener(listener);
    final CellConstraints cc = gridToCellConstraints(component);
    if (component.getCustomLayoutConstraints() instanceof CellConstraints) {
      CellConstraints customCellConstraints = (CellConstraints) component.getCustomLayoutConstraints();
      cc.insets = customCellConstraints.insets;
    }
    component.setCustomLayoutConstraints(cc);
    container.getDelegee().add(component.getDelegee(), cc, index);
  }

  @Override
  public void removeComponentFromContainer(final RadContainer container, final RadComponent component) {
    final MyPropertyChangeListener listener = myListenerMap.get(component);
    if (listener != null) {
      component.removePropertyChangeListener(listener);
      myListenerMap.remove(component);
    }
    super.removeComponentFromContainer(container, component);
  }

  private static CellConstraints gridToCellConstraints(final RadComponent component) {
    GridConstraints gc = component.getConstraints();
    CellConstraints.Alignment hAlign = ((gc.getHSizePolicy() & GridConstraints.SIZEPOLICY_WANT_GROW) != 0)
                                       ? CellConstraints.FILL
                                       : CellConstraints.DEFAULT;
    CellConstraints.Alignment vAlign = ((gc.getVSizePolicy() & GridConstraints.SIZEPOLICY_WANT_GROW) != 0)
                                       ? CellConstraints.FILL
                                       : CellConstraints.DEFAULT;
    CellConstraints cc = (CellConstraints) component.getCustomLayoutConstraints();
    if (cc != null) {
      hAlign = cc.hAlign;
      vAlign = cc.vAlign;
    }
    return new CellConstraints(gc.getColumn()+1, gc.getRow()+1, gc.getColSpan(), gc.getRowSpan(), hAlign, vAlign);
  }

  @Override
  public void writeChildConstraints(final XmlWriter writer, final RadComponent child) {
    super.writeChildConstraints(writer, child);
    if (child.getCustomLayoutConstraints() instanceof CellConstraints) {
      CellConstraints cc = (CellConstraints) child.getCustomLayoutConstraints();
      writer.startElement(UIFormXmlConstants.ELEMENT_FORMS);
      try {
        if (!cc.insets.equals(new Insets(0, 0, 0, 0))) {
          writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_TOP, cc.insets.top);
          writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_LEFT, cc.insets.left);
          writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_BOTTOM, cc.insets.bottom);
          writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_RIGHT, cc.insets.right);
        }
        if (cc.hAlign != CellConstraints.DEFAULT) {
          writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_DEFAULTALIGN_HORZ, false);
        }
        if (cc.vAlign != CellConstraints.DEFAULT) {
          writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_DEFAULTALIGN_VERT, false);
        }
      }
      finally {
        writer.endElement();
      }
    }
  }

  @Override public boolean isGrid() {
    return true;
  }

  private static FormLayout getFormLayout(final RadContainer container) {
    return (FormLayout) container.getLayout();
  }

  @Override public int getGridRowCount(RadContainer container) {
    return getFormLayout(container).getRowCount();
  }

  @Override public int getGridColumnCount(RadContainer container) {
    return getFormLayout(container).getColumnCount();
  }

  @Override public int[] getGridCellCoords(RadContainer container, boolean isRow) {
    final FormLayout.LayoutInfo layoutInfo = getFormLayout(container).getLayoutInfo(container.getDelegee());
    int[] origins = isRow ? layoutInfo.rowOrigins : layoutInfo.columnOrigins;
    int[] result = new int [origins.length-1];
    System.arraycopy(origins, 0, result, 0, result.length);
    return result;
  }

  @Override public int[] getGridCellSizes(RadContainer container, boolean isRow) {
    final FormLayout.LayoutInfo layoutInfo = getFormLayout(container).getLayoutInfo(container.getDelegee());
    int[] origins = isRow ? layoutInfo.rowOrigins : layoutInfo.columnOrigins;
    int[] result = new int [origins.length-1];
    for(int i=0; i<result.length; i++) {
      result [i] = origins [i+1] - origins [i];
    }
    return result;
  }

  @Override public int[] getHorizontalGridLines(RadContainer container) {
    final FormLayout.LayoutInfo layoutInfo = getFormLayout(container).getLayoutInfo(container.getDelegee());
    return layoutInfo.rowOrigins;
  }

  @Override public int[] getVerticalGridLines(RadContainer container) {
    final FormLayout.LayoutInfo layoutInfo = getFormLayout(container).getLayoutInfo(container.getDelegee());
    return layoutInfo.columnOrigins;
  }

  @Override public int getGridRowAt(RadContainer container, int y) {
    final FormLayout.LayoutInfo layoutInfo = getFormLayout(container).getLayoutInfo(container.getDelegee());
    return findCell(layoutInfo.rowOrigins, y);
  }

  @Override public int getGridColumnAt(RadContainer container, int x) {
    final FormLayout.LayoutInfo layoutInfo = getFormLayout(container).getLayoutInfo(container.getDelegee());
    return findCell(layoutInfo.columnOrigins, x);
  }

  private static int findCell(final int[] origins, final int coord) {
    for(int i=0; i<origins.length-1; i++) {
      if (coord >= origins [i] && coord < origins [i+1]) return i;
    }
    return -1;
  }

  @NotNull @Override
  public DropLocation getDropLocation(RadContainer container, @Nullable final Point location) {
    FormLayout formLayout = getFormLayout(container);
    final FormLayout.LayoutInfo layoutInfo = formLayout.getLayoutInfo(container.getDelegee());
    if (location.x > layoutInfo.getWidth()) {
      int row = findCell(layoutInfo.rowOrigins, location.y);
      if (row == -1) {
        return NoDropLocation.INSTANCE;
      }
      return new GridInsertLocation(container, row, getGridColumnCount(container)-1, GridInsertMode.ColumnAfter);
    }
    if (location.y > layoutInfo.getHeight()) {
      int column = findCell(layoutInfo.columnOrigins, location.x);
      if (column == -1) {
        return NoDropLocation.INSTANCE;
      }
      return new GridInsertLocation(container, getGridRowCount(container)-1, column, GridInsertMode.RowAfter);
    }

    if (container.getGridRowCount() == 1 && container.getGridColumnCount() == 1 &&
        getComponentAtGrid(container, 0, 0) == null) {
      final Rectangle rc = getGridCellRangeRect(container, 0, 0, 0, 0);
      if (location == null) {
        return new FormFirstComponentInsertLocation(container, 0, 0, rc, 0, 0);
      }
      return new FormFirstComponentInsertLocation(container, 0, 0, location, rc);
    }

    return super.getDropLocation(container, location);
  }

  @Override
  public RowColumnPropertiesPanel getRowColumnPropertiesPanel(RadContainer container, boolean isRow, int[] selectedIndices) {
    if (myPropertiesPanel == null) {
      myPropertiesPanel = new FormLayoutColumnProperties();
    }
    myPropertiesPanel.showProperties(container, isRow, selectedIndices);
    return myPropertiesPanel;
  }

  @Override
  public ActionGroup getCaptionActions() {
    DefaultActionGroup group = new DefaultActionGroup();
    group.add(new InsertBeforeAction());
    group.add(new InsertAfterAction());
    group.add(new SplitAction());
    group.add(new DeleteAction());
    group.add(new GroupRowsColumnsAction());
    group.add(new UngroupRowsColumnsAction());
    return group;
  }

  @Override
  public void paintCaptionDecoration(final RadContainer container, final boolean isRow, final int index, final Graphics2D g2d,
                                     final Rectangle rc) {
    // don't paint gap rows/columns with red background
    if (index % 2 == 1 && GridChangeUtil.canDeleteCell(container, index, isRow, true)) {
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.fillRect(rc.x, rc.y, rc.width, rc.height);
    }

    FormLayout layout = (FormLayout) container.getLayout();
    FormSpec spec = isRow ? layout.getRowSpec(index+1) : layout.getColumnSpec(index+1);
    if (spec.getResizeWeight() > 0.01d) {
      drawGrowMarker(isRow, g2d, rc);
    }

    int[][] groups = isRow ? layout.getRowGroups() : layout.getColumnGroups();
    //noinspection MultipleVariablesInDeclaration
    boolean haveTopLeft = false, haveTopRight = false, haveTopLine = false;
    //noinspection MultipleVariablesInDeclaration
    boolean haveBottomLeft = false, haveBottomRight = false, haveBottomLine = false;
    for(int i=0; i<groups.length; i++) {
      int minMember = Integer.MAX_VALUE;
      int maxMember = -1;
      for(int member: groups [i]) {
        minMember = Math.min(member-1, minMember);
        maxMember = Math.max(member-1, maxMember);
      }
      if (minMember <= index && index <= maxMember) {
        if (i % 2 == 0) {
          haveTopLeft = haveTopLeft || index > minMember;
          haveTopRight = haveTopRight || index < maxMember;
          haveTopLine = haveTopLine || index == minMember || index == maxMember;
        }
        else {
          haveBottomLeft = haveBottomLeft || index > minMember;
          haveBottomRight = haveBottomRight || index < maxMember;
          haveBottomLine = haveBottomLine || index == minMember || index == maxMember;
        }
      }
    }

    g2d.setColor(Color.BLUE);
    drawGroupLine(rc, isRow, g2d, true, haveTopLeft, haveTopRight, haveTopLine);
    drawGroupLine(rc, isRow, g2d, false, haveBottomLeft, haveBottomRight, haveBottomLine);
  }

  private static void drawGroupLine(final Rectangle rc, final boolean isRow, final Graphics2D g2d, boolean isTop,
                                    final boolean haveLeft, final boolean haveRight, final boolean haveLine) {

    int maxX = (int) rc.getMaxX();
    int maxY = (int) rc.getMaxY();
    Point linePos = isTop ? new Point(rc.x+1, rc.y+1) : new Point(rc.x+3, rc.y+3);
    Point markerPos = new Point(rc.x+6, rc.y+6);

    int midX = (int) rc.getCenterX();
    int midY = (int) rc.getCenterY();
    if (haveLine) {
      if (isRow) {
        g2d.drawLine(linePos.x, midY, markerPos.x, midY);
      }
      else {
        g2d.drawLine(midX, linePos.y, midX, markerPos.y);
      }
    }
    if (haveLeft) {
      if (isRow) {
        g2d.drawLine(linePos.x, rc.y, linePos.x, midY);
      }
      else {
        g2d.drawLine(rc.x, linePos.y, midX, linePos.y);
      }
    }
    if (haveRight) {
      if (isRow) {
        g2d.drawLine(linePos.x, midY, linePos.x, maxY);
      }
      else {
        g2d.drawLine(midX, linePos.y, maxX, linePos.y);
      }
    }
  }

  @Override
  public Property[] getContainerProperties(final Project project) {
    return Property.EMPTY_ARRAY; // TODO
  }

  @Override
  public Property[] getComponentProperties(final Project project, final RadComponent component) {
    return new Property[] {
      HorzAlignProperty.getInstance(project),
      VertAlignProperty.getInstance(project),
      new ComponentInsetsProperty()
    };
  }

  @Override
  public int insertGridCells(final RadContainer grid, final int cellIndex, final boolean isRow, final boolean isBefore, final boolean grow) {
    FormSpec formSpec;
    if (isRow) {
      formSpec = grow ? new RowSpec(ENCODED_FORMSPEC_GROW) : new RowSpec(new ConstantSize(10, ConstantSize.DLUY));
    }
    else {
      formSpec = grow ? new ColumnSpec(ENCODED_FORMSPEC_GROW) : new ColumnSpec(new ConstantSize(10, ConstantSize.DLUX));
    }
    insertGridCells(grid, cellIndex, isRow, isBefore, formSpec);
    return 2;
  }

  @Override
  public void copyGridRows(RadContainer grid, int rowIndex, int rowCount, int targetIndex) {
    FormLayout formLayout = getFormLayout(grid);
    insertOrAppendRow(formLayout, targetIndex+1, FormFactory.RELATED_GAP_ROWSPEC);
    targetIndex++;
    if (targetIndex < rowIndex) rowIndex++;
    for(int i=0; i < rowCount; i++) {
      RowSpec rowSpec = formLayout.getRowSpec(rowIndex + 1);
      insertOrAppendRow(formLayout, targetIndex+1, rowSpec);
      rowIndex += (targetIndex < rowIndex) ? 2 : 1;
      targetIndex++;
    }
  }

  @Override
  public int getGapCellCount() {
    return 1;
  }

  /**
   * @return index where new column or row was actually inserted (0-based)
   */
  private static int insertGridCells(final RadContainer grid,
                                     final int cellIndex,
                                     final boolean isRow,
                                     final boolean isBefore,
                                     final FormSpec formSpec) {
    FormLayout formLayout = (FormLayout) grid.getLayout();
    int index = isBefore ? cellIndex+1 : cellIndex+2;
    if (isRow) {
      insertOrAppendRow(formLayout, index, FormFactory.RELATED_GAP_ROWSPEC);
      if (!isBefore) index++;
      insertOrAppendRow(formLayout, index, (RowSpec) formSpec);
    }
    else {
      insertOrAppendColumn(formLayout, index, FormFactory.RELATED_GAP_COLSPEC);
      if (!isBefore) index++;
      insertOrAppendColumn(formLayout, index, (ColumnSpec)formSpec);
    }
    updateGridConstraintsFromCellConstraints(grid);
    return index-1;
  }

  private static void insertOrAppendRow(final FormLayout formLayout, final int index, final RowSpec rowSpec) {
    if (index == formLayout.getRowCount()+1) {
      formLayout.appendRow(rowSpec);
    }
    else {
      formLayout.insertRow(index, rowSpec);
    }
  }

  private static void insertOrAppendColumn(final FormLayout formLayout, final int index, final ColumnSpec columnSpec) {
    if (index == formLayout.getColumnCount()+1) {
      formLayout.appendColumn(columnSpec);
    }
    else {
      formLayout.insertColumn(index, columnSpec);
    }
  }

  @Override
  public int deleteGridCells(final RadContainer grid, final int cellIndex, final boolean isRow) {
    int result = 1;
    FormLayout formLayout = (FormLayout) grid.getLayout();
    if (isRow) {
      formLayout.removeRow(cellIndex+1);
      updateGridConstraintsFromCellConstraints(grid);
      if (formLayout.getRowCount() % 2 == 0) {
        int gapRowIndex = (cellIndex >= grid.getGridRowCount()) ? cellIndex-1 : cellIndex;
        if (GridChangeUtil.isRowEmpty(grid, gapRowIndex)) {
          formLayout.removeRow(gapRowIndex+1);
          updateGridConstraintsFromCellConstraints(grid);
          result++;
        }
      }
    }
    else {
      formLayout.removeColumn(cellIndex+1);
      updateGridConstraintsFromCellConstraints(grid);
      if (formLayout.getColumnCount() % 2 == 0) {
        int gapColumnIndex = (cellIndex >= grid.getGridColumnCount()) ? cellIndex-1 : cellIndex;
        if (GridChangeUtil.isColumnEmpty(grid, gapColumnIndex)) {
          formLayout.removeColumn(gapColumnIndex+1);
          updateGridConstraintsFromCellConstraints(grid);
          result++;
        }
      }
    }
    return result;
  }

  @Override
  public void processCellResized(RadContainer container, final boolean isRow, final int cell, final int newSize) {
    FormLayout formLayout = (FormLayout) container.getLayout();
    if (isRow) {
      RowSpec rowSpec = formLayout.getRowSpec(cell+1);
      RowSpec newSpec = new RowSpec(rowSpec.getDefaultAlignment(), new ConstantSize(newSize, ConstantSize.PIXEL), rowSpec.getResizeWeight());
      formLayout.setRowSpec(cell+1, newSpec);
    }
    else {
      ColumnSpec colSpec = formLayout.getColumnSpec(cell+1);
      ColumnSpec newSpec = new ColumnSpec(colSpec.getDefaultAlignment(), new ConstantSize(newSize, ConstantSize.PIXEL), colSpec.getResizeWeight());
      formLayout.setColumnSpec(cell+1, newSpec);
    }
  }

  @Override
  public void processCellsMoved(final RadContainer container, final boolean isRow, final int[] cellsToMove, int targetCell) {
    for(int i=0; i<cellsToMove.length; i++) {
      final int sourceCell = cellsToMove[i];
      moveCells(container, isRow, sourceCell, targetCell);
      if (sourceCell < targetCell) {
        for(int j=i+1; j<cellsToMove.length; j++) {
          cellsToMove [j] -= 2;
        }
      }
      else {
        targetCell += 2;
      }
    }
  }

  private void moveCells(final RadContainer container, final boolean isRow, final int cell, int targetCell) {
    if (targetCell >= cell && targetCell <= cell+2) {
      return;
    }
    FormLayout layout = (FormLayout) container.getLayout();
    List<RadComponent> componentsToMove = new ArrayList<RadComponent>();
    FormSpec oldSpec = isRow ? layout.getRowSpec(cell+1) : layout.getColumnSpec(cell+1);
    for(RadComponent c: container.getComponents()) {
      if (c.getConstraints().getCell(isRow) == cell) {
        componentsToMove.add(c);
        container.removeComponent(c);
      }
    }
    int count = deleteGridCells(container, cell, isRow);
    int insertCell = (targetCell > cell) ? targetCell - count - 1 : targetCell;
    final boolean isBefore = targetCell < cell;
    int newIndex = insertGridCells(container, insertCell, isRow, isBefore, oldSpec);
    for(RadComponent c: componentsToMove) {
      c.getConstraints().setCell(isRow, newIndex);
      container.addComponent(c);
    }
  }

  private static void updateGridConstraintsFromCellConstraints(RadContainer grid) {
    FormLayout layout = (FormLayout) grid.getLayout();
    for(RadComponent c: grid.getComponents()) {
      CellConstraints cc = layout.getConstraints(c.getDelegee());
      GridConstraints gc = c.getConstraints();
      copyCellToGridConstraints(gc, cc);
    }
  }

  private static void copyCellToGridConstraints(final GridConstraints gc, final CellConstraints cc) {
    gc.setColumn(cc.gridX-1);
    gc.setRow(cc.gridY-1);
    gc.setColSpan(cc.gridWidth);
    gc.setRowSpan(cc.gridHeight);
  }

  public int getAlignment(RadComponent component, boolean horizontal) {
    CellConstraints cc = (CellConstraints) component.getCustomLayoutConstraints();
    CellConstraints.Alignment al = horizontal ? cc.hAlign : cc.vAlign;
    if (al == CellConstraints.DEFAULT) {
      FormLayout formLayout = (FormLayout) component.getParent().getLayout();
      FormSpec formSpec = horizontal
                          ? formLayout.getColumnSpec(component.getConstraints().getColumn()+1)
                          : formLayout.getRowSpec(component.getConstraints().getRow()+1);
      final FormSpec.DefaultAlignment defaultAlignment = formSpec.getDefaultAlignment();
      if (defaultAlignment.equals(RowSpec.FILL)) {
        return GridConstraints.ALIGN_FILL;
      }
      if (defaultAlignment.equals(RowSpec.TOP) || defaultAlignment.equals(ColumnSpec.LEFT)) {
        return GridConstraints.ALIGN_LEFT;
      }
      if (defaultAlignment.equals(RowSpec.CENTER)) {
        return GridConstraints.ALIGN_CENTER;
      }
      return GridConstraints.ALIGN_RIGHT;
    }
    return Utils.alignFromConstraints(component.getConstraints(), horizontal);
  }

  public void setAlignment(RadComponent component, boolean horizontal, int alignment) {
    CellConstraints cc = (CellConstraints) component.getCustomLayoutConstraints();
    if (horizontal) {
      cc.hAlign = FormLayoutSerializer.ourHorizontalAlignments [alignment];
    }
    else {
      cc.vAlign = FormLayoutSerializer.ourVerticalAlignments [alignment];
    }
  }

  public void resetAlignment(RadComponent component, boolean horizontal) {
    CellConstraints cc = (CellConstraints) component.getCustomLayoutConstraints();
    if (horizontal) {
      cc.hAlign = CellConstraints.DEFAULT;
    }
    else {
      cc.vAlign = CellConstraints.DEFAULT;
    }
    updateConstraints(component);
  }

  public boolean isAlignmentModified(RadComponent component, boolean horizontal) {
    CellConstraints cc = (CellConstraints) component.getCustomLayoutConstraints();
    CellConstraints.Alignment al = horizontal ? cc.hAlign : cc.vAlign;
    return al != CellConstraints.DEFAULT;
  }

  private static void updateConstraints(final RadComponent component) {
    FormLayout layout = (FormLayout) component.getParent().getLayout();
    layout.setConstraints(component.getDelegee(), gridToCellConstraints(component));
    component.getParent().revalidate();
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  @Override
  public void createSnapshotLayout(final SnapshotContext context,
                                   final JComponent parent,
                                   final RadContainer container,
                                   final LayoutManager layout) {
    ColumnSpec[] colSpecs;
    RowSpec[] rowSpecs;
    int[][] rowGroups;
    int[][] columnGroups;
    try {
      Method method = layout.getClass().getMethod("getRowCount", ArrayUtil.EMPTY_CLASS_ARRAY);
      int rowCount = ((Integer)method.invoke(layout, ArrayUtil.EMPTY_OBJECT_ARRAY)).intValue();
      method = layout.getClass().getMethod("getColumnCount", ArrayUtil.EMPTY_CLASS_ARRAY);
      int columnCount = ((Integer)method.invoke(layout, ArrayUtil.EMPTY_OBJECT_ARRAY)).intValue();

      rowSpecs = new RowSpec[rowCount];
      colSpecs = new ColumnSpec[columnCount];

      method = layout.getClass().getMethod("getRowSpec", int.class);
      for (int i = 0; i < rowCount; i++) {
        rowSpecs[i] = (RowSpec)createSerializedCopy(method.invoke(layout, i + 1));
      }
      method = layout.getClass().getMethod("getColumnSpec", int.class);
      for (int i = 0; i < columnCount; i++) {
        colSpecs[i] = (ColumnSpec)createSerializedCopy(method.invoke(layout, i + 1));
      }

      method = layout.getClass().getMethod("getRowGroups", ArrayUtil.EMPTY_CLASS_ARRAY);
      rowGroups = (int[][])method.invoke(layout);

      method = layout.getClass().getMethod("getColumnGroups", ArrayUtil.EMPTY_CLASS_ARRAY);
      columnGroups = (int[][])method.invoke(layout);
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    final FormLayout formLayout = new FormLayout(colSpecs, rowSpecs);
    formLayout.setRowGroups(rowGroups);
    formLayout.setColumnGroups(columnGroups);
    container.setLayout(formLayout);
  }

  private static Object createSerializedCopy(final Object original) {
    // FormLayout may have been loaded with a different classloader, so we need to create a copy through serialization
    Object copy;
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream os = new ObjectOutputStream(baos);
      try {
        os.writeObject(original);
      }
      finally {
        os.close();
      }

      InputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream is = new ObjectInputStream(bais);
      try {
        copy = is.readObject();
      }
      finally {
        is.close();
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return copy;
  }

  @Override
  public void addSnapshotComponent(final JComponent parent,
                                   final JComponent child,
                                   final RadContainer container,
                                   final RadComponent component) {
    CellConstraints cc;
    try {
      LayoutManager layout = parent.getLayout();
      //noinspection HardCodedStringLiteral
      Method method = layout.getClass().getMethod("getConstraints", Component.class);
      cc = (CellConstraints)createSerializedCopy(method.invoke(layout, child));
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    copyCellToGridConstraints(component.getConstraints(), cc);
    component.setCustomLayoutConstraints(cc);
    container.addComponent(component);
  }

  private static class MyPropertyChangeListener implements PropertyChangeListener {
    private final RadComponent myComponent;

    public MyPropertyChangeListener(final RadComponent component) {
      myComponent = component;
    }

    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(RadComponent.PROP_CONSTRAINTS)) {
        updateConstraints(myComponent);
      }
    }
  }

  private static class ComponentInsetsProperty extends Property<RadComponent, Insets> {
    private InsetsPropertyRenderer myRenderer;
    private IntRegexEditor<Insets> myEditor;
    private Property[] myChildren;

    public ComponentInsetsProperty() {
      super(null, "Insets");
      myChildren=new Property[]{
        new IntFieldProperty(this, "top", 0),
        new IntFieldProperty(this, "left", 0),
        new IntFieldProperty(this, "bottom", 0),
        new IntFieldProperty(this, "right", 0),
      };
    }

    @NotNull @Override
    public Property[] getChildren(final RadComponent component) {
      return myChildren;
    }

    public Insets getValue(final RadComponent component) {
      if (component.getCustomLayoutConstraints() instanceof CellConstraints) {
        final CellConstraints cellConstraints = (CellConstraints)component.getCustomLayoutConstraints();
        return cellConstraints.insets;
      }
      return new Insets(0, 0, 0, 0);
    }

    protected void setValueImpl(final RadComponent component, final Insets value) throws Exception {
      if (component.getCustomLayoutConstraints() instanceof CellConstraints) {
        final CellConstraints cellConstraints = (CellConstraints)component.getCustomLayoutConstraints();
        cellConstraints.insets = value;

        FormLayout layout = (FormLayout) component.getParent().getLayout();
        CellConstraints cc = (CellConstraints)layout.getConstraints(component.getDelegee()).clone();
        cc.insets = value;
        layout.setConstraints(component.getDelegee(), cc);
      }
    }

    @NotNull
    public PropertyRenderer<Insets> getRenderer() {
      if (myRenderer == null) {
        myRenderer = new InsetsPropertyRenderer();
      }
      return myRenderer;
    }

    public PropertyEditor<Insets> getEditor() {
      if (myEditor == null) {
        myEditor = new IntRegexEditor<Insets>(Insets.class, myRenderer, new int[] { 0, 0, 0, 0 });
      }
      return myEditor;
    }
  }
}