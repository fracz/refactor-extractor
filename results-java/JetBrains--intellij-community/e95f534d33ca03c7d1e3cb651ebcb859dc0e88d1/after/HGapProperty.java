package com.intellij.uiDesigner.propertyInspector.properties;

import com.intellij.uiDesigner.core.AbstractLayout;
import com.intellij.uiDesigner.radComponents.RadContainer;
import com.intellij.openapi.project.Project;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public final class HGapProperty extends AbstractIntProperty<RadContainer> {
  public static HGapProperty getInstance(Project project) {
    return project.getComponent(HGapProperty.class);
  }

  public HGapProperty(){
    super(null, "Horizontal Gap", -1);
  }

  public Object getValue(final RadContainer component){
    if (component.getLayout() instanceof BorderLayout) {
      BorderLayout layout = (BorderLayout) component.getLayout();
      return layout.getHgap();
    }
    if (component.getLayout() instanceof FlowLayout) {
      FlowLayout layout = (FlowLayout) component.getLayout();
      return layout.getHgap();
    }
    final AbstractLayout layoutManager=(AbstractLayout)component.getLayout();
    return layoutManager.getHGap();
  }

  protected void setValueImpl(final RadContainer component,final Object value) throws Exception{
    if (component.getLayout() instanceof BorderLayout) {
      BorderLayout layout = (BorderLayout) component.getLayout();
      layout.setHgap(((Integer) value).intValue());
    }
    else if (component.getLayout() instanceof FlowLayout) {
      FlowLayout layout = (FlowLayout) component.getLayout();
      layout.setHgap(((Integer) value).intValue());
    }
    else {
      final AbstractLayout layoutManager=(AbstractLayout)component.getLayout();
      layoutManager.setHGap(((Integer)value).intValue());
    }
  }
}