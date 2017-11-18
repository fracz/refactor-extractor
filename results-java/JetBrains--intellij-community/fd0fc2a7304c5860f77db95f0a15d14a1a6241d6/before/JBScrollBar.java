/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
package com.intellij.ui.components;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.wm.IdeGlassPane.TopComponent;
import com.intellij.ui.ComponentSettings;
import com.intellij.ui.InputSource;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.RegionPainter;
import com.intellij.util.ui.UIUtil;
import org.intellij.lang.annotations.JdkConstants;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ScrollBarUI;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import static com.intellij.util.SystemProperties.isTrueSmoothScrollingEnabled;

/**
 * Our implementation of a scroll bar with the custom UI.
 * Also it provides a method to create custom UI for our custom L&Fs.
 *
 * @see #createUI(JComponent)
 */
public class JBScrollBar extends JScrollBar implements TopComponent, Interpolable, FinelyAdjustable {
  /**
   * This key defines a region painter, which is used by the custom ScrollBarUI
   * to draw additional paintings (i.e. error stripes) on the scrollbar's track.
   *
   * @see UIUtil#putClientProperty
   */
  public static final Key<RegionPainter<Object>> TRACK = Key.create("JB_SCROLL_BAR_TRACK");

  private static final double THRESHOLD = 1D + 1E-5D;
  private static final boolean SUPPORTED_JAVA = SystemInfo.isJetbrainsJvm || SystemInfo.isJavaVersionAtLeast("1.9");
  private final Interpolator myInterpolator = new Interpolator(this::getValue, this::setCurrentValue);
  private final Adjuster myAdjuster = new Adjuster(delta -> setValue(getTargetValue() + delta));
  private boolean isUnitIncrementSet;
  private boolean isBlockIncrementSet;

  public JBScrollBar() {
    this(VERTICAL);
  }

  public JBScrollBar(@JdkConstants.AdjustableOrientation int orientation) {
    this(orientation, 0, 10, 0, 100);
  }

  public JBScrollBar(@JdkConstants.AdjustableOrientation int orientation, int value, int extent, int min, int max) {
    super(orientation, value, extent, min, max);
    if (isTrueSmoothScrollingEnabled()) setModel(new Model(value, extent, min, max));
    putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE); // fast scrolling for JDK 6
  }

  @Override
  public void updateUI() {
    ScrollBarUI ui = getUI();
    if (ui instanceof DefaultScrollBarUI) return;
    setUI(createUI(this));
  }

  /**
   * Returns a new instance of {@link ScrollBarUI}.
   * Do not share it between different scroll bars.
   *
   * @param c a target component for this UI
   * @return a new instance of {@link ScrollBarUI}
   */
  @SuppressWarnings("UnusedParameters")
  public static ScrollBarUI createUI(JComponent c) {
    return SystemInfo.isMac ? new MacScrollBarUI() : new DefaultScrollBarUI();
  }

  /**
   * Computes the unit increment for scrolling if the viewport's view.
   * Otherwise returns {@code super.getUnitIncrement}.
   *
   * @param direction less than zero to scroll up/left, greater than zero for down/right
   * @return an integer, in pixels, containing the unit increment
   * @see Scrollable#getScrollableUnitIncrement
   */
  @Override
  public int getUnitIncrement(int direction) {
    JViewport viewport = getViewport();
    if (viewport != null) {
      Scrollable scrollable = getScrollableViewToCalculateIncrement(viewport.getView());
      if (scrollable != null) return scrollable.getScrollableUnitIncrement(viewport.getViewRect(), orientation, direction);
      if (!isUnitIncrementSet) {
        Font font = getViewFont(viewport);
        if (font != null) return font.getSize(); // increase default unit increment for fast scrolling
      }
    }
    return super.getUnitIncrement(direction);
  }

  @Override
  public void setUnitIncrement(int increment) {
    isUnitIncrementSet = true;
    super.setUnitIncrement(increment);
  }

  /**
   * Computes the block increment for scrolling if the viewport's view.
   * Otherwise returns {@code super.getBlockIncrement}.
   *
   * @param direction less than zero to scroll up/left, greater than zero for down/right
   * @return an integer, in pixels, containing the block increment
   * @see Scrollable#getScrollableBlockIncrement
   */
  @Override
  public int getBlockIncrement(int direction) {
    JViewport viewport = getViewport();
    if (viewport != null) {
      Scrollable scrollable = getScrollableViewToCalculateIncrement(viewport.getView());
      if (scrollable != null) return scrollable.getScrollableBlockIncrement(viewport.getViewRect(), orientation, direction);
      if (!isBlockIncrementSet) {
        Dimension size = viewport.getExtentSize();
        return orientation == HORIZONTAL ? size.width : size.height;
      }
    }
    return super.getBlockIncrement(direction);
  }

  @Override
  public void setBlockIncrement(int increment) {
    isBlockIncrementSet = true;
    super.setBlockIncrement(increment);
  }

  /**
   * Notifies glass pane that it should not process mouse event above the scrollbar's thumb.
   *
   * @param event the mouse event
   * @return {@code true} if glass pane can process the specified event, {@code false} otherwise
   */
  @Override
  public boolean canBePreprocessed(MouseEvent event) {
    return JBScrollPane.canBePreprocessed(event, this);
  }

  @Override
  public void setValue(int value) {
    int delay = 0;
    Component parent = getParent();
    if (parent instanceof JBScrollPane) {
      JBScrollPane pane = (JBScrollPane)parent;
      JViewport viewport = pane.getViewport();
      if (viewport != null) {
        ComponentSettings settings = ComponentSettings.getInstance();
        if (settings.isTrueSmoothScrollingEligibleFor(viewport.getView()) && settings.isInterpolationEligibleFor(this)) {
          delay = pane.getInitialDelay(getValueIsAdjusting());
        }
      }
    }
    if (delay > 0) {
      myInterpolator.setTarget(value, delay);
    }
    else {
      super.setValue(value);
    }
  }

  @Override
  public void setCurrentValue(int value) {
    super.setValue(value);
    myAdjuster.reset();
  }

  @Override
  public int getTargetValue() {
    return myInterpolator.getTarget();
  }

  @Override
  public void adjustValue(double delta) {
    myAdjuster.adjustValue(delta);
  }

  /**
   * Handles the mouse wheel events to scroll the scrollbar.
   *
   * @param event the mouse wheel event
   * @return {@code true} if the specified event is handled and consumed, {@code false} otherwise
   */
  public boolean handleMouseWheelEvent(MouseWheelEvent event) {
    if (MouseWheelEvent.WHEEL_UNIT_SCROLL != event.getScrollType()) return false;
    if (event.isShiftDown() == (orientation == VERTICAL)) return false;

    ComponentSettings settings = ComponentSettings.getInstance();
    if (!settings.isTrueSmoothScrollingEligibleFor(this)) return false;

    double delta = getDelta(event);
    if (delta == 0.0D || !Double.isFinite(delta)) return false;

    int value = getTargetValue();
    double minDelta = (double)getMinimum() - value;
    double maxDelta = (double)getMaximum() - getVisibleAmount() - value;
    adjustValue(Math.max(minDelta, Math.min(maxDelta, delta)));
    event.consume();
    return true;
  }

  private JViewport getViewport() {
    Component parent = getParent();
    if (parent instanceof JScrollPane) {
      JScrollPane pane = (JScrollPane)parent;
      return pane.getViewport();
    }
    return null;
  }

  private static Font getViewFont(JViewport viewport) {
    if (viewport == null) return null;
    Component view = viewport.getView();
    return view == null ? null : view.getFont();
  }

  private Scrollable getScrollableViewToCalculateIncrement(Component view) {
    return view instanceof JTable || (view instanceof Scrollable && orientation == VERTICAL) ? (Scrollable)view : null;
  }

  private static double boundDelta(double minDelta, double maxDelta, double delta) {
    return Math.max(minDelta, Math.min(maxDelta, delta));
  }

  /**
   * Indicates whether a scrolling delta can be calculated from the specified event.
   *
   * @param event the mouse wheel event
   * @return {@code true} if a scrolling delta can be calculated, {@code false} otherwise
   * @see #getDelta(MouseWheelEvent)
   */
  static boolean hasDelta(MouseWheelEvent event) {
    double rotation = event.getPreciseWheelRotation();
    if (rotation == 0.0D || !Double.isFinite(rotation)) return false;

    ComponentSettings settings = ComponentSettings.getInstance();
    if (SUPPORTED_JAVA && settings.isPixelPerfectScrollingEnabled()) {
      if (SystemInfo.isMac && Registry.is("ide.scroll.precise.rotation.mac")) return true;
      if (SystemInfo.isWindows && Registry.is("ide.scroll.precise.rotation.windows")) return true;
    }
    return settings.isHighPrecisionScrollingEnabled();
  }

  /**
   * Calculates a scrolling delta from the specified event.
   *
   * @param event the mouse wheel event
   * @return a scrolling delta for this scrollbar
   * @see #hasDelta(MouseWheelEvent)
   */
  private double getDelta(MouseWheelEvent event) {
    double rotation = event.getPreciseWheelRotation();
    ComponentSettings settings = ComponentSettings.getInstance();
    if (SUPPORTED_JAVA && settings.isPixelPerfectScrollingEnabled()) {
      // calculate an absolute delta if possible
      if (SystemInfo.isMac && Registry.is("ide.scroll.precise.rotation.mac")) {
        // Native code in our JDK for Mac uses 0.1 to convert pixels to units,
        // so we use 10 to restore amount of pixels to scroll.
        return 10 * rotation;
      }
      if (SystemInfo.isWindows && Registry.is("ide.scroll.precise.rotation.windows")) {
        JViewport viewport = getViewport();
        Font font = viewport == null ? null : getViewFont(viewport);
        int size = font == null ? JBUI.scale(10) : font.getSize(); // assume an unit size
        return size * rotation * event.getScrollAmount();
      }
    }
    if (settings.isHighPrecisionScrollingEnabled()) {
      // calculate a relative delta if possible
      int direction = rotation < 0 ? -1 : 1;
      int unitIncrement = getUnitIncrement(direction);
      double delta = unitIncrement * rotation * event.getScrollAmount();
      if (-THRESHOLD > delta && delta > THRESHOLD) return delta;
      // When the scrolling speed is set to maximum, it's possible to scroll by more units than will fit in the visible area.
      // To make for more accurate low-speed scrolling, we limit scrolling to the block increment
      // if the wheel was only rotated one click.
      double blockIncrement = getBlockIncrement(direction);
      return boundDelta(-blockIncrement, blockIncrement, delta);
    }
    return 0.0D;
  }

  private static final class Model extends DefaultBoundedRangeModel {
    private Model(int value, int extent, int min, int max) {
      super(value, extent, min, max);
    }

    /**
     * Implementation of {@link DefaultBoundedRangeModel#fireStateChanged()} with optional filtering.
     * It doesn't notify its scrollbar UI listener on value changes
     * while the scrollbar value is adjusted by dragging the scrollbar's thumb.
     */
    @Override
    protected void fireStateChanged() {
      if (getValueIsAdjusting() && ComponentSettings.getInstance().isInterpolationEnabledFor(InputSource.SCROLLBAR)) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
          if (listeners[i] == ChangeListener.class) {
            ChangeListener listener = (ChangeListener)listeners[i + 1];
            // ignore listeners declared in different implementations of ScrollBarUI
            if (!listener.getClass().getName().contains("ScrollBarUI")) {
              if (changeEvent == null) changeEvent = new ChangeEvent(this);
              listener.stateChanged(changeEvent);
            }
          }
        }
      }
      else {
        super.fireStateChanged();
      }
    }
  }
}