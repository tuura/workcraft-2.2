/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JPanel;

import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class DocumentPlaceholder extends JPanel {
	static private BufferedImage logoImage;
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if (logoImage != null) {
			int w = logoImage.getWidth();
			int h = logoImage.getHeight();
		
			int x = (getWidth() - w)/2;
			int y = (getHeight() - h)/2;
		
			g.drawImage(logoImage, x, y, null);
		}
	}

	public DocumentPlaceholder() {
		super();
		logoImage = getImage();
		setBackground(new Color(255,255,255));
		setLayout(null);
		
	}
	
	private BufferedImage getImage() {
		if (logoImage == null) 
		{
			try {
				logoImage = GUI.loadImageFromResource("images/logo.png");
			} catch (IOException e) {
				logoImage = null;
			}
		}
		return logoImage;
	}

}
