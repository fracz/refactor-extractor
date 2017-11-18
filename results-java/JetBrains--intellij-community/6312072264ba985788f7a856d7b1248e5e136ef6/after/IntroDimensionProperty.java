package com.intellij.uiDesigner.propertyInspector.properties;

import com.intellij.uiDesigner.XmlWriter;
import com.intellij.uiDesigner.radComponents.RadComponent;
import com.intellij.uiDesigner.propertyInspector.IntrospectedProperty;
import com.intellij.uiDesigner.propertyInspector.Property;
import com.intellij.uiDesigner.propertyInspector.PropertyEditor;
import com.intellij.uiDesigner.propertyInspector.PropertyRenderer;
import com.intellij.uiDesigner.propertyInspector.renderers.DimensionRenderer;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.lang.reflect.Method;

/**
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public final class IntroDimensionProperty extends IntrospectedProperty<Dimension> {
  private final Property[] myChildren;
  private final DimensionRenderer myRenderer;

  public IntroDimensionProperty(final String name, final Method readMethod, final Method writeMethod, final boolean storeAsClient){
    super(name, readMethod, writeMethod, storeAsClient);
    myChildren = new Property[]{
      new AbstractDimensionProperty.MyWidthProperty(this),
      new AbstractDimensionProperty.MyHeightProperty(this)
    };
    myRenderer = new DimensionRenderer();
  }

  public void write(final Dimension value, final XmlWriter writer){
    writer.addAttribute("width", value.width);
    writer.addAttribute("height", value.height);
  }

  @NotNull
  public Property[] getChildren(final RadComponent component){
    return myChildren;
  }

  @NotNull
  public PropertyRenderer<Dimension> getRenderer(){
    return myRenderer;
  }

  public PropertyEditor<Dimension> getEditor(){
    return null;
  }
}