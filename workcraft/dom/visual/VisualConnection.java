package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.framework.exceptions.NotAnAncestorException;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

public class VisualConnection extends VisualNode implements PropertyChangeListener  {
	protected Connection refConnection;

	protected VisualComponent first;
	protected VisualComponent second;

	protected Point2D firstCenter = new Point2D.Double();
	protected Point2D secondCenter = new Point2D.Double();
	protected Point2D lineStart = new Point2D.Double();
	protected Point2D lineEnd = new Point2D.Double();
	protected Point2D arrowHeadPosition = new Point2D.Double();
	protected double arrowOrientation = 0;

	protected static double defaultLineWidth = 0.02;
	protected static double defaultArrowWidth = 0.15;
	protected static double defaultArrowLength = 0.4;
	protected static double hitThreshold = 0.2;
	protected static Color defaultColor = Color.BLUE;
	
	protected Color color = defaultColor;
	protected double lineWidth = defaultLineWidth;
	protected double arrowWidth = defaultArrowWidth;
	protected double arrowLength = defaultArrowLength;

	public VisualConnection(Connection refConnection, VisualComponent first, VisualComponent second) {
		first.addListener(this);

		this.refConnection = refConnection;
		this.first = first;
		this.second = second;

		first.addListener(this);
		second.addListener(this);

		update();
		
		propertyDeclarations.add(new PropertyDeclaration("Line width", "getLineWidth", "setLineWidth", double.class));
		propertyDeclarations.add(new PropertyDeclaration("Arrow width", "getArrowWidth", "setArrowWidth", double.class));
		propertyDeclarations.add(new PropertyDeclaration("Arrow length", "getArrowLength", "setArrowLength", double.class));
	}
	
	public VisualConnection(Connection refConnection, Element xmlElement, VisualComponent first, VisualComponent second) {
		this(refConnection, first, second);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(double lineWidth) {
		if (lineWidth < 0.01)
			lineWidth = 0.01;
		if (lineWidth > 0.5)
			lineWidth = 0.5;
		this.lineWidth = lineWidth;
	}

	public double getArrowWidth() {
		return arrowWidth;
	}

	public void setArrowWidth(double arrowWidth) {
		if (arrowWidth > 1)
			arrowWidth = 1;
		if (arrowWidth < 0.1)
			arrowWidth = 0.1;
		this.arrowWidth = arrowWidth;
	}

	public double getArrowLength() {
		return arrowLength;
	}

	public void setArrowLength(double arrowLength) {
		if (arrowLength > 1)
			arrowLength = 1;
		if (arrowLength < 0.1)
			arrowLength = 0.1;
		this.arrowLength = arrowLength;
		update();
	}

	protected Point2D getPointOnCenterLine (double t) {
		return new Point2D.Double(firstCenter.getX() * (1-t) + secondCenter.getX() * t, firstCenter.getY() * (1-t) + secondCenter.getY() * t);
	}
	
	protected Point2D getPointOnConnection(double t) {
		return new Point2D.Double(lineStart.getX() * (1-t) + arrowHeadPosition.getX() * t, lineStart.getY() * (1-t) + arrowHeadPosition.getY() * t);
	}

	public void update() {
		AffineTransform t1, t2;
		try {
			t1 = first.getParentToAncestorTransform(parent);
			t2 = second.getParentToAncestorTransform(parent);
		} catch (NotAnAncestorException e) {
			e.printStackTrace();
			throw new RuntimeException("qwe");
		//	return;
		}
		
		// get centres of the two components in this connection's parent space 
		Rectangle2D firstBB = first.getBoundingBoxInParentSpace();
		Rectangle2D secondBB = second.getBoundingBoxInParentSpace();
		
		firstCenter.setLocation(firstBB.getCenterX(), firstBB.getCenterY());
		secondCenter.setLocation(secondBB.getCenterX(), secondBB.getCenterY());

		t1.transform(firstCenter, firstCenter);
		t2.transform(secondCenter, secondCenter);

		// create transforms from this connection's parent space to
		// components' parent spaces, for hit testing
		AffineTransform it1, it2;
		try {
			it1 = t1.createInverse();
			it2 = t2.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
			return;
		}
		
		Point2D pt = new Point2D.Double();
		
		// find connection line starting point
		double t = 1.0, dt = 1.0;
		
		while(dt > 1e-6)
		{
			dt /= 2.0;
			t -= dt;
			pt = getPointOnCenterLine(t);
			lineStart.setLocation(pt);
			it1.transform(pt, pt);
			if (first.hitTestInParentSpace(pt) != 0)
				t+=dt;
		}
		
		// find arrowHeadPosition
		t = 0.0; dt = 1.0;

		while(dt > 1e-6)
		{
			dt /= 2.0;
			t += dt;
			pt = getPointOnCenterLine(t);
			arrowHeadPosition.setLocation(pt);

			it2.transform(pt, pt);
			if (second.hitTestInParentSpace(pt) != 0)
				t-=dt;
		}

		//  find connection line ending point
		t = 0.0; dt = 1.0;
		while(dt > 1e-6)
		{
			dt /= 2.0;
			t += dt;
			pt = getPointOnCenterLine(t);
			if (arrowHeadPosition.distanceSq(pt) < arrowLength*arrowLength)
				t-=dt;
		}
		
		lineEnd = pt;
		arrowOrientation = Math.atan2(arrowHeadPosition.getY() - lineEnd.getY() , arrowHeadPosition.getX() - lineEnd.getX());
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Coloriser.colorise(color, colorisation));
		g.setStroke(new BasicStroke((float)lineWidth));
		
		Line2D line = new Line2D.Double(lineStart, lineEnd);
		g.draw(line);

		g.translate(arrowHeadPosition.getX(), arrowHeadPosition.getY());
		g.rotate(arrowOrientation);

		Path2D.Double arrowShape = new Path2D.Double();
		arrowShape.moveTo(-arrowLength, -arrowWidth / 2);
		arrowShape.lineTo(-arrowLength, arrowWidth / 2);
		arrowShape.lineTo(0,0);
		arrowShape.closePath();

		g.fill(arrowShape);
	}

	public void toXML(Element vconElement) {

	}

	public Connection getReferencedConnection() {
		return refConnection;
	}
	
	private double distanceToConnection (Point2D pointInParentSpace) {
		Point2D v = new Point2D.Double();
		v.setLocation(arrowHeadPosition.getX() - lineStart.getX(), arrowHeadPosition.getY() - lineStart.getY());
		Point2D vv = new Point2D.Double();
		vv.setLocation(pointInParentSpace.getX() - lineStart.getX(), pointInParentSpace.getY() - lineStart.getY());
		Point2D pt = new Point2D.Double();
		pt.setLocation(pointInParentSpace);
		
		double c1 = v.getX() * vv.getX() + v.getY() * vv.getY();
		if(c1<=0) {
			pt.setLocation(pt.getX() - lineStart.getX(), pt.getY() - lineStart.getY());
			return pt.distance(0, 0);
		}
		
		double c2 = v.getX() * v.getX() + v.getY() * v.getY();
		
		if(c2<=c1) {
			pt.setLocation(pt.getX() - arrowHeadPosition.getX(), pt.getY() - arrowHeadPosition.getY());
			return pt.distance(0, 0);
		}
		
		double b = c1/c2;
		
		pt.setLocation(pt.getX() - v.getX() *b - lineStart.getX(), pt.getY() - v.getY() * b - lineStart.getY());
		return pt.distance(0, 0);
	}

	public int hitTestInParentSpace(Point2D pointInParentSpace) {
		if (distanceToConnection(pointInParentSpace) < hitThreshold)
			return 1;
		else
			return 0;
	}

	public Rectangle2D getBoundingBoxInParentSpace() {
		Rectangle2D bb = new Rectangle2D.Double(lineStart.getX(), lineStart.getY(), 0, 0);
		bb.add(arrowHeadPosition);
		Point2D lineVec = new Point2D.Double(arrowHeadPosition.getX() - lineStart.getX(), arrowHeadPosition.getY() - lineStart.getY());
		
		double mag = lineVec.distance(0, 0);

		if (mag==0)
			return bb;
		
		lineVec.setLocation(lineVec.getY()*hitThreshold/mag, -lineVec.getX()*hitThreshold/mag);
		bb.add(lineStart.getX() + lineVec.getX(), lineStart.getY() + lineVec.getY());
		bb.add(arrowHeadPosition.getX() + lineVec.getX(), arrowHeadPosition.getY() + lineVec.getY());
		bb.add(lineStart.getX() - lineVec.getX(), lineStart.getY() - lineVec.getY());
		bb.add(arrowHeadPosition.getX() - lineVec.getX(), arrowHeadPosition.getY() - lineVec.getY());
				
		return bb;
	}

	public void setColorisation (Color color) {
		colorisation = color;
	}

	public Color getColorisation (Color color) {
		return colorisation;
	}

	public void clearColorisation() {
		setColorisation(null);
	}

	public void propertyChanged(String propertyName, Object sender) {
		if (propertyName.equals("transform"))
			update();		
	}
	
	@Override
	public void setParent(VisualGroup parent) {
		super.setParent(parent);
		update();
	}

	public VisualComponent getFirst() {
		return first;
	}
	
	public VisualComponent getSecond() {
		return second;
	}
}