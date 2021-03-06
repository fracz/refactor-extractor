package org.jetbrains.plugins.ipnb.editor.panels.code;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ipnb.editor.IpnbEditorUtil;
import org.jetbrains.plugins.ipnb.editor.panels.IpnbPanel;
import org.jetbrains.plugins.ipnb.format.cells.output.ImageCellOutput;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImagePanel extends IpnbPanel {
  private static final Logger LOG = Logger.getInstance(ImagePanel.class);
  @NotNull private final ImageCellOutput myCell;

  public ImagePanel(@NotNull final ImageCellOutput cell) {
    myCell = cell;
    myViewPanel = createViewPanel();
    add(myViewPanel);
  }

  @Override
  protected JComponent createViewPanel() {
    final String png = myCell.getBase64String();

    final JBLabel label = new JBLabel();
    try {
      byte[] btDataFile = new BASE64Decoder().decodeBuffer(png);
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(btDataFile));
      label.setIcon(new ImageIcon(image));
    }
    catch (IOException e) {
      LOG.error("Couldn't parse image. " + e.getMessage());
    }

    label.setBackground(IpnbEditorUtil.getBackground());
    label.setOpaque(true);

    return label;
  }
}