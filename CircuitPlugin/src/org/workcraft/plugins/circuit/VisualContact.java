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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.SignalTransition;


public class VisualContact extends VisualComponent implements StateObserver {
	public enum Direction {	NORTH, SOUTH, EAST, WEST};
	public static final Color inputColor = Color.RED;
	public static final Color outputColor = Color.BLUE;

	private static Font nameFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	
	private double size = 0.5;
	
	private GlyphVector nameGlyph = null;
	
	private Direction direction = Direction.WEST;
	
	private HashSet<SignalTransition> referencedTransitions=new HashSet<SignalTransition>();
	private Place referencedZeroPlace=null;
	private Place referencedOnePlace=null;
	
	public void resetNameGlyph() {
		nameGlyph = null;
	}
	
	static public AffineTransform getDirectionTransform(Direction dir) {
		AffineTransform at = new AffineTransform();
		at.setToIdentity();
		if (dir!=null) {
			switch (dir) {
			case NORTH:
				at.quadrantRotate(3);
				break;
			case SOUTH:
				at.quadrantRotate(1);
				break;
			case WEST:
				at.quadrantRotate(2);
				break;
			}
		}
		return at;
	}
	
	public VisualContact(Contact contact) {
		super(contact);
		
		contact.addObserver(this);
		addPropertyDeclarations();
	}
	
	public VisualContact(Contact component, VisualContact.Direction dir, String label) {
		super(component);
		
		component.addObserver(this);
		addPropertyDeclarations();
		
		setName(label);
		setDirection(dir);
	}
	
	private Shape getShape() {
		
		double size = getSize();
		
		if (getParent() instanceof VisualCircuitComponent) {
			if (CircuitSettings.getShowContacts()) {
				return new Rectangle2D.Double(
						-size / 2 + CircuitSettings.getCircuitWireWidth(),
						-size / 2 + CircuitSettings.getCircuitWireWidth(),
						size - CircuitSettings.getCircuitWireWidth()*2,
						size - CircuitSettings.getCircuitWireWidth()*2
						);
			} else {
				return new Line2D.Double(0,0,0,0);
			}
		} else {
			
			Path2D path = new Path2D.Double();
			
			path.moveTo(-(size / 2), -(size / 2));
			path.lineTo((size / 2), -(size / 2));
			path.lineTo(size, 0);
			path.lineTo((size / 2), (size / 2));
			path.lineTo(-(size / 2), (size / 2));
			path.closePath();
			
			return path;
		}
	}
	
	private void addPropertyDeclarations() {
		LinkedHashMap<String, Object> types = new LinkedHashMap<String, Object>();
		types.put("Input", Contact.IOType.INPUT);
		types.put("Output", Contact.IOType.OUTPUT);
		
		LinkedHashMap<String, Object> directions = new LinkedHashMap<String, Object>();
		directions.put("North", VisualContact.Direction.NORTH);
		directions.put("East", VisualContact.Direction.EAST);
		directions.put("South", VisualContact.Direction.SOUTH);
		directions.put("West", VisualContact.Direction.WEST);
		
		addPropertyDeclaration(new PropertyDeclaration(this, "Direction", "getDirection", "setDirection", VisualContact.Direction.class, directions));
		addPropertyDeclaration(new PropertyDeclaration(this, "I/O type", "getIOType", "setIOType", Contact.IOType.class, types));
		addPropertyDeclaration(new PropertyDeclaration(this, "Name", "getName", "setName", String.class));
		addPropertyDeclaration(new PropertyDeclaration(this, "Init to one", "getInitOne", "setInitOne", boolean.class));
	}
	
	public boolean getInitOne() {
		return getReferencedContact().getInitOne();
	}
	
	public void setInitOne(boolean value) {
		getReferencedContact().setInitOne(value);
	}
	
	@Override
	public void draw(DrawRequest r) {

		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();
		Color fillColor = r.getDecoration().getBackground();
		if (fillColor==null) fillColor=getFillColor();
		
		if (!(getParent() instanceof VisualCircuitComponent)) {
			
			AffineTransform at = new AffineTransform();
			
			switch (getDirection()) {
			case NORTH:
				at.quadrantRotate(-1);
				break;
			case SOUTH:
				at.quadrantRotate(1);
				break;
			case EAST:
				at.quadrantRotate(2);
				break;
			}
			
			g.transform(at);
			
		}
		
		g.setColor(fillColor);
		g.fill(getShape());
		g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
		
		g.setStroke(new BasicStroke((float)CircuitSettings.getCircuitWireWidth()));
		g.draw(getShape());
		
		if (!(getParent() instanceof VisualCircuitComponent)) {
			AffineTransform at = new AffineTransform();
			
			switch (getDirection()) {
			case SOUTH:
				at.quadrantRotate(2);
				break;
			case EAST:
				at.quadrantRotate(2);
				break;
			}
			
			g.transform(at);
			
			GlyphVector gv = getNameGlyphs(g);
			Rectangle2D cur = gv.getVisualBounds();
			g.setColor(Coloriser.colorise((getIOType()==IOType.INPUT)?inputColor:outputColor, colorisation));
			
			float xx = 0;
			
			if (getIOType()==IOType.INPUT) {
				xx = (float)(-cur.getWidth()-0.5);
			} else {
				xx = (float)0.5;
			}
			g.drawGlyphVector(gv, xx, -0.5f);
			
		}
		
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		double size = getSize();
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}

	private double getSize() {
		return size;
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		
		Point2D p2 = new Point2D.Double();
		p2.setLocation(pointInLocalSpace);
		
		if (!(getParent() instanceof VisualCircuitComponent)) {
			AffineTransform at = new AffineTransform();
			
			switch (getDirection()) {
			case NORTH:
				at.quadrantRotate(1);
				break;
			case SOUTH:
				at.quadrantRotate(-1);
				break;
			case EAST:
				at.quadrantRotate(2);
				break;
			}
			
			at.transform(pointInLocalSpace, p2);
		}
		
		return getShape().contains(p2);
	}	
	
	
/*	@Override
 * 	public Collection<MathNode> getMathReferences() {
		return Collections.emptyList();
	}

	*/
	
	/////////////////////////////////////////////////////////
	public GlyphVector getNameGlyphs(Graphics2D g) {
		if (nameGlyph == null) {
			if (getDirection()==VisualContact.Direction.NORTH||getDirection()==VisualContact.Direction.SOUTH) {
				AffineTransform at = new AffineTransform();
				at.quadrantRotate(1);
			}
			nameGlyph = nameFont.createGlyphVector(g.getFontRenderContext(), getName());
		}
		
		return nameGlyph;
	}
	
	public Rectangle2D getNameBB(Graphics2D g) {
		return getNameGlyphs(g).getVisualBounds();
	}
	
	public void setDirection(VisualContact.Direction dir) {
		
		if (dir==direction) return;
		
		this.direction=dir;
		
		nameGlyph = null;
		
		sendNotification(new PropertyChangedEvent(this, "direction"));
		sendNotification(new TransformChangedEvent(this));
	}	
	
	public VisualContact.Direction getDirection() {
		return direction;
	}
	
	public void setIOType(Contact.IOType type) {
		getReferencedContact().setIOType(type);
		sendNotification(new PropertyChangedEvent(this, "IOtype"));
		nameGlyph = null;
	}

	public Contact.IOType getIOType() {
		return getReferencedContact().getIOType();
	}

	
	public String getName() {
		return getReferencedContact().getName();
	}

	public void setName(String name) {
/*		if (name==null||name.equals("")&&((Contact)getReferencedComponent()).getIOType()==IOType.INPUT) 
			name=getNewName(
					((Contact)getReferencedComponent()).getParent(),
					"input");
		if (name==null||name.equals("")&&((Contact)getReferencedComponent()).getIOType()==IOType.OUTPUT) 
			name=getNewName(
				((Contact)getReferencedComponent()).getParent(),
					"output");*/
		
		getReferencedContact().setName(name);
		
		sendNotification(new PropertyChangedEvent(this, "name"));
		nameGlyph = null;
	}

	public Contact getReferencedContact() {
		return (Contact)getReferencedComponent();
	}

	public static boolean isDriver(Node contact) {
		if (!(contact instanceof VisualContact)) return false;
		
		return (((VisualContact)contact).getIOType() == IOType.OUTPUT) == (((VisualContact)contact).getParent() instanceof VisualComponent);
	}

	public static Direction flipDirection(Direction direction) {
		if (direction==Direction.EAST) return Direction.WEST;
		if (direction==Direction.WEST) return Direction.EAST;
		if (direction==Direction.SOUTH) return Direction.NORTH;
		if (direction==Direction.NORTH) return Direction.SOUTH;
		return null;
	}

	public HashSet<SignalTransition> getReferencedTransitions() {
		return referencedTransitions;
	}

	@Override
	public void notify(StateEvent e) {
	}

	public void setReferencedOnePlace(Place referencedOnePlace) {
		this.referencedOnePlace = referencedOnePlace;
	}

	public Place getReferencedOnePlace() {
		return referencedOnePlace;
	}

	public void setReferencedZeroPlace(Place referencedZeroPlace) {
		this.referencedZeroPlace = referencedZeroPlace;
	}

	public Place getReferencedZeroPlace() {
		return referencedZeroPlace;
	}
	
}
