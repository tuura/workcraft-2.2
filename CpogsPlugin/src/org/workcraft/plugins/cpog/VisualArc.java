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

package org.workcraft.plugins.cpog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.util.Geometry;

public class VisualArc extends VisualConnection
{
	private static Font labelFont;
	private Rectangle2D labelBB = null; 
	
	Arc mathConnection;
	
	static {
		try {
			labelFont = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public VisualArc(Arc mathConnection)
	{
		super();
		this.mathConnection = mathConnection;
	}
	
	public VisualArc(Arc mathConnection, VisualVertex first, VisualVertex second)
	{
		super(mathConnection, first, second);
		this.mathConnection = mathConnection;
	}

	public BooleanFormula getCondition()
	{
		return mathConnection.getCondition();
	}
	
	@Override
	public Stroke getStroke()
	{
		BooleanFormula value = evaluate();
		
		if (value == Zero.instance()) 
			return new BasicStroke((float) super.getLineWidth(), BasicStroke.CAP_BUTT,
		        BasicStroke.JOIN_MITER, 1.0f, new float[] {0.18f, 0.18f}, 0.00f);
		
		return super.getStroke();
	}
	
	@Override
	public Color getDrawColor()
	{
		BooleanFormula value = evaluate();
		
		if (value == Zero.instance() || value == One.instance()) return super.getDrawColor();
		
		return Color.LIGHT_GRAY;
	}

	private BooleanFormula evaluate()
	{
		BooleanFormula condition = getCondition();
		
		condition = BooleanOperations.and(condition, ((VisualVertex) getFirst()).evaluate());
		condition = BooleanOperations.and(condition, ((VisualVertex) getSecond()).evaluate());
		
		return condition.accept(
				new BooleanReplacer(new HashMap<BooleanVariable, BooleanFormula>())
				{
					@Override
					public BooleanFormula visit(BooleanVariable node) {
						switch(((Variable)node).getState())
						{
						case TRUE:
							return One.instance();
						case FALSE:
							return Zero.instance();
						default:
							return node;
						}
					}
				}
			);
	}

	public void setCondition(BooleanFormula condition)
	{
		mathConnection.setCondition(condition);
		sendNotification(new PropertyChangedEvent(this, "condition"));		
	}
	
	@Override
	public void draw(DrawRequest r)
	{
		labelBB = null;
		
		if (getCondition() == One.instance()) return;
		
		Graphics2D g = r.getGraphics();
		
		FormulaRenderingResult result = FormulaToGraphics.render(getCondition(), g.getFontRenderContext(), labelFont);
		
		labelBB = result.boundingBox;
		
		ConnectionGraphic graphic = getGraphic();
		
		Point2D p = graphic.getPointOnCurve(0.5);
		Point2D d = graphic.getDerivativeAt(0.5);
		Point2D dd = graphic.getSecondDerivativeAt(0.5);
		
		if (d.getX() < 0)
		{
			d = Geometry.multiply(d, -1);
			//dd = Geometry.multiply(dd, -1);
		}
		
		Point2D labelPosition = new Point2D.Double(labelBB.getCenterX(), labelBB.getMaxY());
		if (Geometry.crossProduct(d, dd) < 0) labelPosition.setLocation(labelPosition.getX(), labelBB.getMinY()); 

		AffineTransform oldTransform = g.getTransform();
		AffineTransform transform = AffineTransform.getTranslateInstance(p.getX() - labelPosition.getX(), p.getY() - labelPosition.getY()); 
		transform.concatenate(AffineTransform.getRotateInstance(d.getX(), d.getY(), labelPosition.getX(), labelPosition.getY()));
		
		g.transform(transform);		
		result.draw(g, Coloriser.colorise(Color.BLACK, r.getDecoration().getColorisation()));
		g.setTransform(oldTransform);
		
		labelBB = BoundingBoxHelper.transform(labelBB, transform);
	}
	
	@Override
	public Rectangle2D getBoundingBox()
	{
		return BoundingBoxHelper.union(super.getBoundingBox(), labelBB);
	}
	
	public Rectangle2D getLabelBoundingBox()
	{
		return labelBB;
	}
	
	@Override
	public boolean hitTest(Point2D pointInParentSpace)
	{
		if (labelBB != null && labelBB.contains(pointInParentSpace)) return true;
		
		return super.hitTest(pointInParentSpace);
	}
}
