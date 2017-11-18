package com.intellij.uiDesigner.propertyInspector.properties;

import com.intellij.uiDesigner.propertyInspector.Property;
import com.intellij.uiDesigner.propertyInspector.PropertyEditor;
import com.intellij.uiDesigner.propertyInspector.PropertyRenderer;
import com.intellij.uiDesigner.propertyInspector.renderers.InsetsPropertyRenderer;
import com.intellij.uiDesigner.radComponents.RadComponent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.Insets;

/**
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public abstract class AbstractInsetsProperty<T extends RadComponent> extends Property<T, Insets> {
  private final Property[] myChildren;
  private final InsetsPropertyRenderer myRenderer;

  public AbstractInsetsProperty(@NonNls final String name){
    super(null, name);
    myChildren=new Property[]{
      new IntFieldProperty(this, "top", 0),
      new IntFieldProperty(this, "left", 0),
      new IntFieldProperty(this, "bottom", 0),
      new IntFieldProperty(this, "right", 0),
    };
    myRenderer=new InsetsPropertyRenderer();
  }

  @NotNull
  public final Property[] getChildren(final RadComponent component){
    return myChildren;
  }

  @NotNull
  public final PropertyRenderer<Insets> getRenderer(){
    return myRenderer;
  }

  public final PropertyEditor<Insets> getEditor(){
    return null;
  }
}