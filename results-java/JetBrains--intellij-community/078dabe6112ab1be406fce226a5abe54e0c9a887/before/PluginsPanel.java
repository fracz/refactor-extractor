package com.intellij.openapi.wm.impl.welcomeScreen;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: pti
 * Date: Mar 29, 2005
 * Time: 9:23:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class PluginsPanel extends JPanel implements Scrollable{

  private static final int VERTICAL_SCROLL_INCREMENT = UIManager.getFont("ToolTip.font").getSize() * 2;
  private static final int HORIZONTAL_SCROLL_INCREMENT = VERTICAL_SCROLL_INCREMENT;

  public PluginsPanel(LayoutManager layout) {
    super(layout);
  }

  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  public Dimension getPreferredScrollableViewportSize() {
    return this.getPreferredSize();
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (orientation == SwingConstants.VERTICAL) return visibleRect.height - VERTICAL_SCROLL_INCREMENT;
    else return visibleRect.width;
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (orientation == SwingConstants.VERTICAL) return VERTICAL_SCROLL_INCREMENT;
    else return HORIZONTAL_SCROLL_INCREMENT;
  }
}