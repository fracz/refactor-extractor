/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.intellij.ide.ui.laf.intellij;

import com.intellij.ide.ui.laf.darcula.ui.DarculaButtonUI;
import com.intellij.ui.Gray;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class MacIntelliJButtonUI extends DarculaButtonUI {
  private static Rectangle viewRect = new Rectangle();
  private static Rectangle textRect = new Rectangle();
  private static Rectangle iconRect = new Rectangle();

  @SuppressWarnings({"MethodOverridesStaticMethodOfSuperclass", "UnusedDeclaration"})
  public static ComponentUI createUI(JComponent c) {
    return new MacIntelliJButtonUI();
  }

  @Override
  public void paint(Graphics g, JComponent c) {
    int w = c.getWidth();
    int h = c.getHeight();
    if (isHelpButton(c)) {
      Icon icon = MacIntelliJIconCache.getIcon("helpButton", false, c.hasFocus(), false);
      int x = (w - icon.getIconWidth()) / 2;
      int y = (h - icon.getIconHeight()) / 2;
      icon.paintIcon(c, g, x, y);
    } else {
      AbstractButton b = (AbstractButton) c;
      String text = layout(b, SwingUtilities2.getFontMetrics(b, g),
                           b.getWidth(), b.getHeight());

      boolean isDefault = b instanceof JButton && ((JButton)b).isDefaultButton();
      boolean isFocused = c.hasFocus();
      if (isSquare(c)) {
        g.setColor(Gray.xFF);
        g.fillRect(1, 1, w - 2, h - 2);
        g.setColor(Gray.xB4);
        g.drawRect(1, 1, w - 2, h - 2);
      } else {
        int x = isFocused ? 0 : 2;
        int y = isFocused ? 0 : (h - viewRect.height) / 2;
        Icon icon;
        icon = MacIntelliJIconCache.getIcon("buttonLeft", isDefault, isFocused, false);
        icon.paintIcon(b, g, x, y);
        x += icon.getIconWidth();
        int stop = w - (isFocused ? 0 : 2) - (MacIntelliJIconCache.getIcon("buttonRight", isDefault, isFocused, false).getIconWidth());
        Graphics gg = g.create(0, 0, w, h);
        gg.setClip(x, y, stop - x, h);
        icon = MacIntelliJIconCache.getIcon("buttonMiddle", isDefault, isFocused, false);
        while (x < stop) {
          icon.paintIcon(b, gg, x, y);
          x += icon.getIconWidth();
        }
        gg.dispose();
        icon = MacIntelliJIconCache.getIcon("buttonRight", isDefault, isFocused, false);
        icon.paintIcon(b, g, stop, y);

        clearTextShiftOffset();
      }

      // Paint the Icon
      if(b.getIcon() != null) {
        paintIcon(g,c,iconRect);
      }

      if (text != null && !text.isEmpty()){
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
          v.paint(g, textRect);
        } else {
          paintText(g, b, textRect, text);
        }
      }
    }
  }

  private String layout(AbstractButton b, FontMetrics fm,
                        int width, int height) {
    Insets i = b.getInsets();
    viewRect.x = i.left;
    viewRect.y = i.top;
    viewRect.width = width - (i.right + viewRect.x);
    viewRect.height = height - (i.bottom + viewRect.y);

    textRect.x = textRect.y = textRect.width = textRect.height = 0;
    iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;

    // layout the text and icon
    return SwingUtilities.layoutCompoundLabel(
      b, fm, b.getText(), b.getIcon(),
      b.getVerticalAlignment(), b.getHorizontalAlignment(),
      b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
      viewRect, iconRect, textRect,
      b.getText() == null ? 0 : b.getIconTextGap());
  }

  @Override
  public Dimension getPreferredSize(JComponent c) {
    Dimension size = super.getPreferredSize(c);
    return new Dimension(size.width + 16, 27);
  }
}