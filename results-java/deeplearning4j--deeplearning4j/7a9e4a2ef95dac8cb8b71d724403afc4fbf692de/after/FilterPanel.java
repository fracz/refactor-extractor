package org.deeplearning4j.plot;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class FilterPanel extends JPanel {

	private BufferedImage image;

	public FilterPanel(BufferedImage image) {
		this.image = image;
		setVisible(true);
		validate();

	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters

	}



}