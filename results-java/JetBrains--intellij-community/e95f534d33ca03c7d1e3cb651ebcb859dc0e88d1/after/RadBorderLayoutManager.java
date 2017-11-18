/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.uiDesigner.radComponents;

import com.intellij.uiDesigner.XmlWriter;
import com.intellij.uiDesigner.UIFormXmlConstants;
import com.intellij.uiDesigner.propertyInspector.Property;
import com.intellij.uiDesigner.propertyInspector.properties.HGapProperty;
import com.intellij.uiDesigner.propertyInspector.properties.VGapProperty;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.designSurface.DropLocation;
import com.intellij.uiDesigner.designSurface.ComponentDragObject;
import com.intellij.uiDesigner.designSurface.FeedbackLayer;
import com.intellij.uiDesigner.designSurface.GuiEditor;
import com.intellij.openapi.project.Project;

import java.awt.*;

/**
 * @author yole
 */
public class RadBorderLayoutManager extends RadLayoutManager {
  public String getName() {
    return UIFormXmlConstants.LAYOUT_BORDER;
  }

  public LayoutManager createLayout() {
    return new BorderLayout();
  }

  public void writeLayout(final XmlWriter writer, final RadContainer radContainer) {
    BorderLayout layout = (BorderLayout) radContainer.getLayout();
    writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_HGAP, layout.getHgap());
    writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_VGAP, layout.getVgap());
  }

  public void addComponentToContainer(final RadContainer container, final RadComponent component, final int index) {
    container.getDelegee().add(component.getDelegee(), component.getCustomLayoutConstraints(), index);
  }

  public void writeChildConstraints(final XmlWriter writer, final RadComponent child) {
    writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_BORDER_CONSTRAINT, (String) child.getCustomLayoutConstraints());
  }

  @Override public DropLocation getDropLocation(RadContainer container, final Point location) {
    return new MyDropLocation(container, getQuadrantAt(container, location));
  }

  private static String getQuadrantAt(final RadContainer container, final Point location) {
    if (location == null) {
      return BorderLayout.CENTER;
    }

    Dimension size = container.getDelegee().getSize();
    if (location.x < size.width / 3) {
      return BorderLayout.WEST;
    }
    if (location.y < size.height / 3) {
      return BorderLayout.NORTH;
    }
    if (location.x > size.width * 2 / 3) {
      return BorderLayout.EAST;
    }
    if (location.y > size.height * 2 / 3) {
      return BorderLayout.SOUTH;
    }

    return BorderLayout.CENTER;
  }

  @Override public boolean canChangeLayout(final RadContainer container) {
    return container.getComponentCount() == 0;
  }

  @Override public Property[] getContainerProperties(final Project project) {
    return new Property[] {
      HGapProperty.getInstance(project),
      VGapProperty.getInstance(project)
    };
  }

  private static class MyDropLocation implements DropLocation {
    private RadContainer myContainer;
    private String myQuadrant;

    public MyDropLocation(final RadContainer container, final String quadrant) {
      myQuadrant = quadrant;
      myContainer = container;
    }

    public RadContainer getContainer() {
      return myContainer;
    }

    public boolean canDrop(ComponentDragObject dragObject) {
      return dragObject.getComponentCount() == 1 &&
             ((BorderLayout) myContainer.getLayout()).getLayoutComponent(myQuadrant) == null;
    }

    public void placeFeedback(FeedbackLayer feedbackLayer, ComponentDragObject dragObject) {
      feedbackLayer.putFeedback(myContainer.getDelegee(), getFeedbackRect(myQuadrant));
    }

    private Rectangle getFeedbackRect(final String quadrant) {
      Dimension size = myContainer.getDelegee().getSize();
      if (quadrant.equals(BorderLayout.WEST)) {
        return new Rectangle(0, 0, size.width/3, size.height);
      }
      if (quadrant.equals(BorderLayout.NORTH)) {
        return new Rectangle(0, 0, size.width, size.height/3);
      }
      if (quadrant.equals(BorderLayout.EAST)) {
        return new Rectangle(size.width*2/3, 0, size.width/3, size.height);
      }
      if (quadrant.equals(BorderLayout.SOUTH)) {
        return new Rectangle(0, size.height*2/3, size.width, size.height/3);
      }
      return new Rectangle(size.width/3, size.height/3, size.width/3, size.height/3);
    }

    public void processDrop(GuiEditor editor,
                            RadComponent[] components,
                            GridConstraints[] constraintsToAdjust,
                            ComponentDragObject dragObject) {
      components [0].setCustomLayoutConstraints(myQuadrant);
      myContainer.addComponent(components [0]);
    }
  }
}