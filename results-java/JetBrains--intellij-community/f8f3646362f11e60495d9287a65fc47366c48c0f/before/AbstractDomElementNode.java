package com.intellij.util.xml.tree;

import jetbrains.fabrique.ui.treeStructure.SimpleNode;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.openapi.util.Key;

import javax.swing.*;
import java.lang.reflect.Type;
import java.util.*;

abstract public class AbstractDomElementNode extends SimpleNode {
  public static final Key<Map<Class, Boolean>> TREE_NODES_HIDERS_KEY = Key.create("TREE_NODES_HIDERS_KEY");
  private boolean isExpanded;

  protected AbstractDomElementNode() {
    super();
  }

  public AbstractDomElementNode(final SimpleNode parent) {
    super(parent);
  }

  abstract public DomElement getDomElement();

  abstract public String getNodeName();

  abstract public String getTagName();

  public Icon getNodeIcon() {
    return getDomElement().getPresentation().getIcon();
  }

  protected String getPropertyName() {
    return getDomElement().getPresentation().getTypeName();
  }

  protected boolean shouldBeShowed(final Type type) {
    final Map<Class, Boolean> hiders = getDomElement().getRoot().getUserData(TREE_NODES_HIDERS_KEY);
    if (type == null || hiders == null || hiders.size() == 0) return true;

    final Class aClass = DomUtil.getRawType(type);

    List<Class> allParents = new ArrayList<Class>();
    for (Map.Entry<Class, Boolean> entry : hiders.entrySet()) {
      if (entry.getKey().isAssignableFrom(aClass)) {
        allParents.add(entry.getKey());
      }
      ;
    }
    if (allParents.size() == 0) return false;

    final Class[] classes = allParents.toArray(new Class[allParents.size()]);
    Arrays.sort(classes, new Comparator<Class>() {
      public int compare(final Class o1, final Class o2) {
        return o1.isAssignableFrom(o2) ? 1 : -1;
      }
    });

    return hiders.get(classes[0]).booleanValue();
  }

  public boolean isExpanded() {
    return isExpanded;
  }

  public void setExpanded(final boolean expanded) {
    isExpanded = expanded;
  }
}