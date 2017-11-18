/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.util.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.ScalableIcon;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.util.SystemProperties;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.UIResource;
import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class JBUI {
  private static final Logger LOG = Logger.getInstance("#com.intellij.util.ui.JBUI");

  /**
   * A default system scale factor.
   */
  public static final float SYSTEM_DEF_SCALE = getSystemDefScale();

  private static float scaleFactor;

  static {
    setScaleFactor(SYSTEM_DEF_SCALE);
  }

  private static float getSystemDefScale() {
    if (SystemInfo.isMac) {
      return 1.0f;
    }

    if (SystemProperties.has("hidpi") && !SystemProperties.is("hidpi")) {
      return 1.0f;
    }

    UIUtil.initSystemFontData();
    Pair<String, Integer> fdata = UIUtil.getSystemFontData();

    int size;
    if (fdata != null) {
      size = fdata.getSecond();
    } else {
      size = Fonts.label().getSize();
    }
    return size / UIUtil.DEF_SYSTEM_FONT_SIZE;
  }

  public static void setScaleFactor(float scale) {
    if (SystemProperties.has("hidpi") && !SystemProperties.is("hidpi")) {
      scaleFactor = 1.0f;
      LOG.info("UI scale factor: 1.0");
      return;
    }

    if (scale < 1.25f) scale = 1.0f;
    else if (scale < 1.5f) scale = 1.25f;
    else if (scale < 1.75f) scale = 1.5f;
    else if (scale < 2f) scale = 1.75f;
    else scale = 2.0f;

    if (SystemInfo.isLinux && scale == 1.25f) {
      //Default UI font size for Unity and Gnome is 15. Scaling factor 1.25f works badly on Linux
      scale = 1f;
    }
    if (scaleFactor == scale) {
      return;
    }
    LOG.info("UI scale factor: " + scale);

    scaleFactor = scale;
    IconLoader.setScale(scale);
  }

  public static int scale(int i) {
    return Math.round(scaleFactor * i);
  }

  public static int scaleFontSize(int fontSize) {
    if (scaleFactor == 1.25f) return (int)(fontSize * 1.34f);
    if (scaleFactor == 1.75f) return (int)(fontSize * 1.67f);
    return scale(fontSize);
  }

  public static JBDimension size(int width, int height) {
    return new JBDimension(width, height);
  }

  public static JBDimension size(int widthAndHeight) {
    return new JBDimension(widthAndHeight, widthAndHeight);
  }

  public static JBDimension size(Dimension size) {
    if (size instanceof JBDimension) {
      final JBDimension jbSize = (JBDimension)size;
      if (jbSize.originalScale == scale(1f)) {
        return jbSize;
      }
      final JBDimension newSize = new JBDimension((int)(jbSize.width / jbSize.originalScale), (int)(jbSize.height / jbSize.originalScale));
      return size instanceof UIResource ? newSize.asUIResource() : newSize;
    }
    return new JBDimension(size.width, size.height);
  }

  public static JBInsets insets(int top, int left, int bottom, int right) {
    return new JBInsets(top, left, bottom, right);
  }

  public static JBInsets insets(int all) {
    return insets(all, all, all, all);
  }

  public static JBInsets insets(int topBottom, int leftRight) {
    return insets(topBottom, leftRight, topBottom, leftRight);
  }

  public static JBInsets emptyInsets() {
    return new JBInsets(0, 0, 0, 0);
  }

  public static JBInsets insetsTop(int t) {
    return insets(t, 0, 0, 0);
  }

  public static JBInsets insetsLeft(int l) {
    return insets(0, l, 0, 0);
  }

  public static JBInsets insetsBottom(int b) {
    return insets(0, 0, b, 0);
  }

  public static JBInsets insetsRight(int r) {
    return insets(0, 0, 0, r);
  }

  /**
   * @deprecated use JBUI.scale(EmptyIcon.create(size)) instead
   */
  public static EmptyIcon emptyIcon(int size) {
    return scale(EmptyIcon.create(size));
  }

  public static <T extends JBIcon> T scale(T icon) {
    return (T)icon.withPreScaled(false);
  }

  public static JBDimension emptySize() {
    return new JBDimension(0, 0);
  }

  public static float scale(float f) {
    return f * scaleFactor;
  }

  public static JBInsets insets(Insets insets) {
    return JBInsets.create(insets);
  }

  public static boolean isHiDPI() {
    return scaleFactor > 1.0f;
  }

  public static class Fonts {
    public static JBFont label() {
      return JBFont.create(UIManager.getFont("Label.font"), false);
    }

    public static JBFont label(float size) {
      return label().deriveFont(scale(size));
    }

    public static JBFont smallFont() {
      return label().deriveFont(UIUtil.getFontSize(UIUtil.FontSize.SMALL));
    }

    public static JBFont miniFont() {
      return label().deriveFont(UIUtil.getFontSize(UIUtil.FontSize.MINI));
    }

    public static JBFont create(String fontFamily, int size) {
      return JBFont.create(new Font(fontFamily, Font.PLAIN, size));
    }
  }

  public static class Borders {
    public static JBEmptyBorder empty(int top, int left, int bottom, int right) {
      return new JBEmptyBorder(top, left, bottom, right);
    }

    public static JBEmptyBorder empty(int topAndBottom, int leftAndRight) {
      return empty(topAndBottom, leftAndRight, topAndBottom, leftAndRight);
    }

    public static JBEmptyBorder emptyTop(int offset) {
      return empty(offset, 0, 0, 0);
    }

    public static JBEmptyBorder emptyLeft(int offset) {
      return empty(0, offset,  0, 0);
    }

    public static JBEmptyBorder emptyBottom(int offset) {
      return empty(0, 0, offset, 0);
    }

    public static JBEmptyBorder emptyRight(int offset) {
      return empty(0, 0, 0, offset);
    }

    public static JBEmptyBorder empty() {
      return empty(0, 0, 0, 0);
    }

    public static Border empty(int offsets) {
      return empty(offsets, offsets, offsets, offsets);
    }

    public static Border customLine(Color color, int top, int left, int bottom, int right) {
      return new CustomLineBorder(color, insets(top, left, bottom, right));
    }

    public static Border customLine(Color color, int thickness) {
      return customLine(color, thickness, thickness, thickness, thickness);
    }

    public static Border customLine(Color color) {
      return customLine(color, 1);
    }

    public static Border merge(@Nullable Border source, @NotNull Border extra, boolean extraIsOutside) {
      if (source == null) return extra;
      return new CompoundBorder(extraIsOutside ? extra : source, extraIsOutside? source : extra);
    }
  }

  public static class Panels {
    public static BorderLayoutPanel simplePanel() {
      return new BorderLayoutPanel();
    }

    public static BorderLayoutPanel simplePanel(Component comp) {
      return simplePanel().addToCenter(comp);
    }

    public static BorderLayoutPanel simplePanel(int hgap, int vgap) {
      return new BorderLayoutPanel(hgap, vgap);
    }
  }

  public static class ComboBox {
    /**
     *        JComboBox<String> comboBox = new ComboBox<>(new String[] {"First", "Second", "Third"});
     *        comboBox.setEditable(true);
     *        comboBox.setEditor(JBUI.ComboBox.compositeComboboxEditor(new JTextField(), new JLabel(AllIcons.Icon_CE)));
     *
     *        @param components an array of JComponent objects. The first one is the editable text component.
     */
/*    public static ComboBoxCompositeEditor compositeComboboxEditor  (JComponent ... components) {
      return new ComboBoxCompositeEditor(components);
    }*/
  }

  /**
   * An Icon dynamically sticking to JBUI.scale to meet HiDPI.
   *
   * @author tav
   */
  public static abstract class JBIcon implements Icon {
    private float myInitialJBUIScale = scale(1f);

    /**
     * @return the scale factor aligning the icon size metrics to conform to JBUI.scale
     */
    private float getAligningJBUIScale() {
      return (myInitialJBUIScale == scale(1f)) ? 1f : scale(1f) / myInitialJBUIScale;
    }

    /**
     * @return whether the icon size metrics are pre-scaled or not
     */
    public boolean isPreScaled() {
      return myInitialJBUIScale != 1f;
    }

    /**
     * Sets the icon size metrics to {@code preScaled}
     */
    public void setPreScaled(boolean preScaled) {
      myInitialJBUIScale = preScaled ? scale(1f) : 1f;
    }

    /**
     * Sets the icon size metrics to {@code preScaled}
     *
     * @return the icon (this or new instance) with size metrics set to {@code preScaled}
     */
    public JBIcon withPreScaled(boolean preScaled) {
      setPreScaled(preScaled);
      return this;
    }

    /**
     * Scales the value to conform to JBUI.scale
     */
    public int scaleVal(int value) {
      return (int)scaleVal((float)value);
    }

    /**
     * Scales the value to conform to JBUI.scale
     */
    public float scaleVal(float value) {
      return value * getAligningJBUIScale();
    }
  }

  /**
   * An Icon supporting both JBUI.scale & arbitrary scale factors.
   *
   * @author tav
   */
  public static abstract class ScalableJBIcon extends JBIcon implements ScalableIcon {
    private float myScale = 1f;

    public enum Scale {
      JBUI,       // JBIcon's scale
      ARBITRARY,  // ScalableIcon's scale
      EFFECTIVE   // effective scale
    }

    public float getScale() {
      return myScale;
    }

    protected void setScale(float scale) {
      myScale = scale;
    }

    @Override
    public int scaleVal(int value) {
      return scaleVal(value, Scale.EFFECTIVE);
    }

    public int scaleVal(int value, Scale type) {
      switch (type) {
        case JBUI:
          return super.scaleVal(value);
        case ARBITRARY:
          return (int)(value * myScale);
        case EFFECTIVE:
        default:
          return (int)super.scaleVal(value * myScale);
      }
    }

    @Override
    public Icon scale(float scale) {
      setScale(scale);
      return this;
    }
  }

  /**
   * A ScalableJBIcon validating JBUI.scale change on demand.
   *
   * @author tav
   */
  public static abstract class ValidatingScalableJBIcon extends ScalableJBIcon {
    private float myCachedJBUIScale = JBUI.scale(1f);

    /**
     * @return cached JBUI.scale
     */
    public float getJBUIScale() {
      return myCachedJBUIScale;
    }

    /**
     * Validates cached JBUI.scale
     *
     * @return true if cached JBUI.scale was invalid (and the icon size metrics should be updated)
     */
    public boolean validateJBUIScale() {
      if (!isJBUIScaleValid()) {
        myCachedJBUIScale = JBUI.scale(1f);
        return true;
      }
      return false;
    }

    /**
     * @return true if cached JBUI.scale is valid
     */
    public boolean isJBUIScaleValid() {
      return myCachedJBUIScale == JBUI.scale(1f);
    }
  }
}