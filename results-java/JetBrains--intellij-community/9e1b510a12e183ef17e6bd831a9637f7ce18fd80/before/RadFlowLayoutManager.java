/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.uiDesigner.radComponents;

import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.UIDesignerBundle;
import com.intellij.uiDesigner.UIFormXmlConstants;
import com.intellij.uiDesigner.XmlWriter;
import com.intellij.uiDesigner.designSurface.DropLocation;
import com.intellij.uiDesigner.propertyInspector.Property;
import com.intellij.uiDesigner.propertyInspector.PropertyEditor;
import com.intellij.uiDesigner.propertyInspector.PropertyRenderer;
import com.intellij.uiDesigner.propertyInspector.editors.IntEnumEditor;
import com.intellij.uiDesigner.propertyInspector.properties.HGapProperty;
import com.intellij.uiDesigner.propertyInspector.properties.VGapProperty;
import com.intellij.uiDesigner.propertyInspector.renderers.IntEnumRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Point;

/**
 * @author yole
 */
public class RadFlowLayoutManager extends RadLayoutManager {
  private static final MyAlignProperty ALIGN_PROPERTY = new MyAlignProperty();

  public String getName() {
    return UIFormXmlConstants.LAYOUT_FLOW;
  }

  public LayoutManager createLayout() {
    return new FlowLayout();
  }

  public void writeChildConstraints(final XmlWriter writer, final RadComponent child) {
  }

  public void writeLayout(final XmlWriter writer, final RadContainer radContainer) {
    FlowLayout layout = (FlowLayout) radContainer.getLayout();
    writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_HGAP, layout.getHgap());
    writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_VGAP, layout.getVgap());
    writer.addAttribute(UIFormXmlConstants.ATTRIBUTE_FLOW_ALIGN, layout.getAlignment());
  }

  public void addComponentToContainer(final RadContainer container, final RadComponent component, final int index) {
    container.getDelegee().add(component.getDelegee(), index);
  }

  @Override public boolean canChangeLayout(final RadContainer container) {
    return container.getComponentCount() == 0;
  }

  @NotNull @Override
  public DropLocation getDropLocation(RadContainer container, final Point location) {
    FlowLayout flowLayout = (FlowLayout) container.getLayout();
    return new FlowDropLocation(container, location, (flowLayout.getHgap()+1)/2, (flowLayout.getVgap()+1)/2, false);
  }

  @Override public Property[] getContainerProperties(final Project project) {
    return new Property[] {
      ALIGN_PROPERTY,
      HGapProperty.getInstance(project),
      VGapProperty.getInstance(project) };
  }


  @Override
  public void addSnapshotComponent(final JComponent parent,
                                   final JComponent child,
                                   final RadContainer container,
                                   final RadComponent component) {
    FlowLayout sourceLayout = (FlowLayout) parent.getLayout();
    FlowLayout targetLayout = (FlowLayout) container.getDelegee().getLayout();
    targetLayout.setAlignment(sourceLayout.getAlignment());
    targetLayout.setHgap(sourceLayout.getHgap());
    targetLayout.setVgap(sourceLayout.getVgap());
    container.addComponent(component);
  }

  private static class MyAlignProperty extends Property<RadContainer> {
    private IntEnumRenderer myRenderer;
    private IntEnumEditor myEditor;
    private IntEnumEditor.Pair[] myPairs;

    public MyAlignProperty() {
      super(null, "Alignment");
      myPairs = new IntEnumEditor.Pair[] {
        new IntEnumEditor.Pair(FlowLayout.CENTER, UIDesignerBundle.message("property.center")),
        new IntEnumEditor.Pair(FlowLayout.LEFT, UIDesignerBundle.message("property.left")),
        new IntEnumEditor.Pair(FlowLayout.RIGHT, UIDesignerBundle.message("property.right")),
        new IntEnumEditor.Pair(FlowLayout.LEADING, UIDesignerBundle.message("property.leading")),
        new IntEnumEditor.Pair(FlowLayout.TRAILING, UIDesignerBundle.message("property.trailing"))
      };
    }

    public Object getValue(final RadContainer component) {
      FlowLayout flowLayout = (FlowLayout) component.getLayout();
      return flowLayout.getAlignment();
    }

    protected void setValueImpl(final RadContainer component, final Object value) throws Exception {
      FlowLayout flowLayout = (FlowLayout) component.getLayout();
      flowLayout.setAlignment(((Integer) value).intValue());
    }

    @NotNull public PropertyRenderer getRenderer() {
      if (myRenderer == null) {
        myRenderer = new IntEnumRenderer(myPairs);
      }
      return myRenderer;
    }

    @NotNull public PropertyEditor getEditor() {
      if (myEditor == null) {
        myEditor = new IntEnumEditor(myPairs);
      }
      return myEditor;
    }

    @Override public boolean isModified(final RadContainer component) {
      FlowLayout flowLayout = (FlowLayout) component.getLayout();
      return flowLayout.getAlignment() != FlowLayout.CENTER;
    }
  }
}