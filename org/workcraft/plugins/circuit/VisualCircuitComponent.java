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

package org.workcraft.plugins.circuit;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class VisualCircuitComponent extends VisualComponent {

	private HashSet<VisualContact> inputs = new HashSet<VisualContact>();
	private HashSet<VisualContact> outputs = new HashSet<VisualContact>();

	public VisualCircuitComponent(CircuitComponent component) {
		super(component);
		// testing...
		inputs.add(new VisualContact(component.addInput("A")));
		inputs.add(new VisualContact(component.addInput("B")));
		inputs.add(new VisualContact(component.addInput("C")));
		outputs.add(new VisualContact(component.addOutput("X")));
		outputs.add(new VisualContact(component.addOutput("Y")));
		outputs.add(new VisualContact(component.addOutput("Z")));

	}

	protected Rectangle2D getContactLabelBB(Graphics2D g) {
		double maxi, maxo;
		double ysumi, ysumo;
		Rectangle2D cur;
		ysumi=0;
		ysumo=0;
		maxi=0;
		maxo=0;
		for (VisualContact c: inputs) {
			GlyphVector gv = c.getLabelGlyphs(g);
			cur = gv.getVisualBounds();
			maxi=(cur.getWidth()>maxi)?cur.getWidth():maxi;
			ysumi=ysumi+cur.getHeight();
		}
		for (VisualContact c: outputs) {
			GlyphVector gv = c.getLabelGlyphs(g);
			cur = gv.getVisualBounds();
			maxo=(cur.getWidth()>maxo)?cur.getWidth():maxo;
			ysumo=ysumo+cur.getHeight();
		}
		double height = Math.max(ysumo, ysumi);
		double width  = maxo+0.1+maxi;
		
		return new Rectangle2D.Double(-width/2, -height/2, width, height);
	}
	
	protected void drawContactsInLocalSpace(Graphics2D g, Rectangle2D BBox) {
		double maxi=0;
		double ysumi, cury;
		Rectangle2D cur;
		ysumi=0;
	
		cury=-ysumi/2;
		for (VisualContact c: inputs) {
			GlyphVector gv = c.getLabelGlyphs(g);
			
			cur = gv.getVisualBounds();
			maxi=(cur.getWidth()>maxi)?cur.getWidth():maxi;
			cury=cury+cur.getHeight();
			
			g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
			
			
//			g.drawGlyphVector(gv, (float)labelPosition.getX(), (float)labelPosition.getY());
		}
		
		
	}
	
	
	@Override
	public void draw(Graphics2D g) {
		
		drawLabelInLocalSpace(g);
		
		Rectangle2D shape = getContactLabelBB(g);
		
		g.setColor(Coloriser.colorise(getFillColor(), getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
		g.draw(shape);
		
		drawContactsInLocalSpace(g, shape);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		double size = CommonVisualSettings.getSize();
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}


	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}

	@Override
	public Collection<MathNode> getMathReferences() {
		// TODO Auto-generated method stub
		return null;
	}

}
