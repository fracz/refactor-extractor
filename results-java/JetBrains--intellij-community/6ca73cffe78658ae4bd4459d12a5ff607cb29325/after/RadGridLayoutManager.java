/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.uiDesigner.radComponents;

import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.XmlWriter;
import com.intellij.uiDesigner.UIFormXmlConstants;

import java.awt.*;

/**
 * @author yole
 */
public class RadGridLayoutManager extends RadLayoutManager {
  public String getName() {
    return "GridLayoutManager";
  }

  public LayoutManager createLayout() {
    return new GridLayoutManager(1, 1);
  }

  public void writeLayout(final XmlWriter writer, final RadContainer radContainer) {
    GridLayoutManager layout = (GridLayoutManager) radContainer.getLayout();

    writer.addAttribute("row-count", layout.getRowCount());
    writer.addAttribute("column-count", layout.getColumnCount());

    writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_SAME_SIZE_HORIZONTALLY, layout.isSameSizeHorizontally());
    writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_SAME_SIZE_VERTICALLY, layout.isSameSizeVertically());

    RadXYLayoutManager.INSTANCE.writeLayout(writer, radContainer);
  }

  public void writeChildConstraints(final XmlWriter writer, final RadComponent child) {
    // Constraints in Grid layout
    writer.startElement("grid");
    try {
      final GridConstraints constraints = child.getConstraints();
      writer.addAttribute("row",constraints.getRow());
      writer.addAttribute("column",constraints.getColumn());
      writer.addAttribute("row-span",constraints.getRowSpan());
      writer.addAttribute("col-span",constraints.getColSpan());
      writer.addAttribute("vsize-policy",constraints.getVSizePolicy());
      writer.addAttribute("hsize-policy",constraints.getHSizePolicy());
      writer.addAttribute("anchor",constraints.getAnchor());
      writer.addAttribute("fill",constraints.getFill());
      writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_INDENT, constraints.getIndent());
      writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_USE_PARENT_LAYOUT, constraints.isUseParentLayout());

      // preferred size
      writer.writeDimension(constraints.myMinimumSize,"minimum-size");
      writer.writeDimension(constraints.myPreferredSize,"preferred-size");
      writer.writeDimension(constraints.myMaximumSize,"maximum-size");
    } finally {
      writer.endElement(); // grid
    }
  }
}