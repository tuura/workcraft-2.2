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

package org.workcraft.plugins.balsa;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.DrawableNew;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.ReflectiveTouchable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.layouts.MainLayouter;
import org.workcraft.util.Hierarchy;

public class VisualBreezeComponent extends VisualComponent implements DrawableNew, ReflectiveTouchable
{
	HandshakeComponentLayout layout;
	Map<String, VisualHandshake> visualHandshakes; // TODO: make auto-updated Expression
	Map<Handshake,BreezeHandshake> handshakeComponents;
	Map<String,Handshake> handshakes;
	HandshakeVisualLayout visualLayout;
	DynamicComponent balsaComponent;
	
	private static final double sideDoubleHandshakeAngle = Math.PI/4; // 45 degrees in radians
	private static final double componentRadius = 0.5;
	private static final double handshakeRadius = componentRadius/5;
	private final BreezeComponent refComponent;

	private void init() {
		balsaComponent = refComponent.getUnderlyingComponent();
		handshakeComponents = refComponent.getHandshakeComponents();
		handshakes = refComponent.getHandshakes();
		layout = MainLayouter.getLayout(balsaComponent, handshakes);
		buildVisualHandshakes();
		// TODO: makeProperties()
		
		//TODO : replace PropertyChangeListener with StateObserver
		/*addPropertyChangeListener(
				new PropertyChangeListener()
				{
					public void onPropertyChanged(String propertyName, Object sender) {
						buildVisualHandshakes();
					}
				}
			);*/
		
		//this.applyTransform(AffineTransform.getQuadrantRotateInstance(1));
	}
	
	public VisualBreezeComponent(BreezeComponent refComponent, StorageManager storage) {
		super(refComponent, storage);
		this.refComponent = refComponent;
		this.storage = storage;
		init();
	}

	Collection<Node> subNodes;
	private final StorageManager storage;
	
	@Override
	public Expression<Collection<Node>> children() {
		return Expressions.constant(Collections.<Node>unmodifiableCollection(visualHandshakes.values()));
	}
	
	public Collection<VisualHandshake> getHandshakes() {
		return Collections.<VisualHandshake>unmodifiableCollection(visualHandshakes.values());
	}
	
	public VisualHandshake getHandshake(String name) {
		return visualHandshakes.get(name);
	}
	
	private void buildVisualHandshakes() {
		Map<String, VisualHandshake> oldHandshakes = visualHandshakes;  
		
		visualLayout = new VisualLayouter().getVisualLayout(layout);
		visualHandshakes = new HashMap<String, VisualHandshake>();
		
		for(String name : handshakes.keySet())
		{
			Handshake handshake = handshakes.get(name);
			Ray ray = visualLayout.positions.get(handshake);
			VisualHandshake visual;
			if(oldHandshakes!=null && oldHandshakes.containsKey(name))
			{
				visual = oldHandshakes.get(name);
				MovableHelper.resetTransform(visual);
			}
			else
			{
				visual = new VisualHandshake(handshakeComponents.get(handshake), storage);
				visual.parent().setValue(this);
			}
			
			visualHandshakes.put(name, visual);
			
			Direction dir = ray.direction;
			int quadrants = 
				dir == Direction.Right ? 1 :
					dir == Direction.Down ? 2:
						dir == Direction.Left ? 3:
							0;
			
			visual.applyTransform(AffineTransform.getQuadrantRotateInstance(quadrants));
			visual.applyTransform(AffineTransform.getScaleInstance(handshakeRadius*2, handshakeRadius*2));
			visual.applyTransform(AffineTransform.getTranslateInstance(ray.position.x, ray.position.y));
		}
	}

	
	@Override
	public Expression<? extends Touchable> shape() {
		return Expressions.constant(new Touchable(){
			@Override
			public Rectangle2D getBoundingBox() {
				Rectangle2D result = new Rectangle2D.Double(-0.5, -0.5, 1, 1);

				Rectangle2D children = BoundingBoxHelper.mergeBoundingBoxes(Hierarchy.getChildrenOfType(VisualBreezeComponent.this, Touchable.class));
				
				if(children != null)
					result.add(children);
				
				return result;
			}

			@Override
			public boolean hitTest(Point2D point) {
				return point.distanceSq(0.0, 0.0) < 0.25;
			}
			
			public Point2D getCenter() {
				return new Point2D.Double(0,0);
			};
			
		});
	}
	

	enum Direction { Up, Right, Down, Left	};
	
	
	static class SideLine
	{
		public SideLine(double top, double bottom) {
			this.top = top;
			this.bottom = bottom;
		}
		double top;
		double bottom;
	}
	
	static class HandshakeVisualLayout
	{
		HashMap<Handshake, Ray> positions;
		SideLine left;
		SideLine right;
	}

	private void drawCircle(Graphics2D g, double x, double y, double r)
	{
		Ellipse2D.Double circle = new Ellipse2D.Double(x-r, y-r, r*2, r*2);
		g.setColor(Color.WHITE);
		g.fill(circle);
		g.setColor(Color.BLACK);
		g.draw(circle);
	}
	
	@Override
	public Expression<? extends ColorisableGraphicalContent> graphicalContent() {
		return Expressions.constant(new ColorisableGraphicalContent() {

			@Override
			public void draw(DrawRequest r) {
				Graphics2D g = r.getGraphics();
				g.setStroke(new BasicStroke(0.02f));
				g.setColor(Coloriser.colorise(Color.black, r.getColorisation().getColorisation()));

				drawSideLine(g, visualLayout.left, -1);
				drawSideLine(g, visualLayout.right, +1);
				
				drawCircle(g, 0, 0, componentRadius);
				

				/*AffineTransform t = g.getTransform();
				t.concatenate(AffineTransform.getQuadrantRotateInstance(-1));
				g.setTransform(t);*/

				Font font = g.getFont();
				font = font.deriveFont(0.2f);
				String symbol = balsaComponent.getClass().getSimpleName();
				if (balsaComponent instanceof DynamicComponent)
					symbol = ((DynamicComponent)balsaComponent).getSymbol();
				GlyphVector vec = font.createGlyphVector(g.getFontRenderContext(), symbol);
				Rectangle2D bounds = vec.getVisualBounds(); 
				g.drawGlyphVector(vec, (float)-bounds.getCenterX(), (float)-bounds.getCenterY());
			}
		});
	}
	
	private void drawSideLine(Graphics2D g, SideLine extent, int dir) {
		double x = componentRadius*dir;
		g.draw(new Line2D.Double(x, extent.top, x, extent.bottom));
	}

	private static class VisualLayouter
	{
		HashMap<Handshake, Ray> positions;
		
		public HandshakeVisualLayout getVisualLayout(HandshakeComponentLayout layout) {
			HandshakeVisualLayout result = new HandshakeVisualLayout();
			
			result.positions = positions = new HashMap<Handshake, Ray>();
			result.left = layoutSide(layout.getLeft(), -1);
			result.right = layoutSide(layout.getRight(), +1);
			layoutPole(layout.getTop(), -1);
			layoutPole(layout.getBottom(), +1);
			
			return result;
		}
	
		private void layoutPole(Handshake handshake, int direction) {
			if(handshake == null)
				return;
			Direction dir = direction > 0 ? Direction.Down : Direction.Up; 
			positions.put(handshake, new Ray(0, (componentRadius+handshakeRadius)*direction, dir));
		}

		private SideLine layoutSide(Handshake[][] handshakes, int direction) {
			if(handshakes.length == 0)
				;
			else if(handshakes.length == 1 && handshakes[0].length == 1)
				layoutSideSingleHandshake(handshakes[0][0], direction);
			else if(handshakes.length == 1 && handshakes[0].length == 2)
				layoutSideDoubleHandshake(handshakes[0][0], handshakes[0][1], direction);
			else if(handshakes.length == 2 && handshakes[0].length == 1 && handshakes[1].length == 1)
				layoutSideDoubleHandshake(handshakes[0][0], handshakes[1][0], direction);
			else
				return layoutSideMultiHandshake(handshakes, direction);
			return new SideLine(0, 0);
		}

		private static final double stepBetweenHandshakes = handshakeRadius*3; 
		private static final double stepBetweenGroups = handshakeRadius*5;
		
		double getHeight(Handshake[] handshakes)
		{
			return Math.max(0, (handshakes.length - 1) * stepBetweenHandshakes);
		}
		
		private SideLine layoutSideMultiHandshake(Handshake[][] handshakes, int direction) {
			
			double totalHeight = 0;
			double top;
			
			double x = (handshakeRadius + componentRadius) * direction;
			Direction dir = direction<0 ? Direction.Left : Direction.Right;
			
			for(int i=0;i<handshakes.length;i++)
			{
				if(i!=0)
					totalHeight += stepBetweenGroups;
				totalHeight += getHeight(handshakes[i]);
			}
			
			if(handshakes.length == 2)
				top = -getHeight(handshakes[0]) - stepBetweenGroups/2;
			else
				top = -totalHeight/2;
			
			double current = top;
			for(int i=0;i<handshakes.length;i++)
			{
				if(i!=0)
					current += stepBetweenGroups;
				for(int j=0;j<handshakes[i].length;j++)
				{
					if(j!=0)
						current += stepBetweenHandshakes;

					positions.put(handshakes[i][j], new Ray(x, current, dir));
				}
			}
			
			return new SideLine(top, top+totalHeight);
		}

		private void layoutSideDoubleHandshake(Handshake handshake1,
				Handshake handshake2, int direction) {
			
			new Point2D.Double(direction*(componentRadius+handshakeRadius), 0);
			double y = Math.sin(sideDoubleHandshakeAngle);
			double x = Math.sin(sideDoubleHandshakeAngle);
			x *= (componentRadius+handshakeRadius);
			y *= (componentRadius+handshakeRadius);
			
			x *= direction;
			
			Direction dir = direction<0 ? Direction.Left : Direction.Right;

			positions.put(handshake1, new Ray(x, -y, dir));
			positions.put(handshake2, new Ray(x, y, dir));
		}

		private void layoutSideSingleHandshake(Handshake handshake, int direction) {
			positions.put(handshake, 
					new Ray(
							direction*(componentRadius+handshakeRadius), 0,
							direction<0 ? Direction.Left : Direction.Right));
		}
	}

	public BreezeComponent getRefComponent() {
		return refComponent;
	}

	@Override
	public Collection<MathNode> getMathReferences() {
		List<MathNode> result = new ArrayList<MathNode>();
		result.add(getReferencedComponent());
		return result;
	}
}
