/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.uiDesigner.radComponents;

import com.intellij.uiDesigner.XmlWriter;
import org.jetbrains.annotations.NonNls;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Design-time support for a layout manager.
 *
 * @author yole
 */
public abstract class RadLayoutManager {
  @NonNls private static Map<String, Class<? extends RadLayoutManager>> ourLayoutManagerRegistry = new HashMap<String, Class<? extends RadLayoutManager>>();

  static {
    ourLayoutManagerRegistry.put("GridLayoutManager", RadGridLayoutManager.class);
    ourLayoutManagerRegistry.put("GridBagLayout", RadGridBagLayoutManager.class);
  }

  public static String[] getLayoutManagerNames() {
    return ourLayoutManagerRegistry.keySet().toArray(new String[0]);
  }

  public static RadLayoutManager createLayoutManager(String name) throws IllegalAccessException, InstantiationException {
    Class<? extends RadLayoutManager> cls = ourLayoutManagerRegistry.get(name);
    if (cls == null) {
      throw new IllegalArgumentException("Unknown layout manager " + name);
    }
    return cls.newInstance();
  }

  public abstract @NonNls String getName();

  public abstract LayoutManager createLayout();

  public boolean canChangeLayout(final RadContainer container) {
    return true;
  }

  public abstract void writeChildConstraints(final XmlWriter writer, final RadComponent child);

  public abstract void writeLayout(final XmlWriter writer, final RadContainer radContainer);
}