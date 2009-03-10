package org.workcraft.dom.visual;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JPopupMenu;

import org.w3c.dom.Element;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.dom.XMLSerialisation;
import org.workcraft.dom.visual.PopupMenuBuilder.PopupMenuSegment;
import org.workcraft.framework.exceptions.NotAnAncestorException;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.PropertyEditable;


public abstract class VisualNode implements PropertyEditable {
	private List<PropertyDescriptor> propertyDeclarations = new LinkedList<PropertyDescriptor>();
	private LinkedList<PropertyChangeListener> propertyChangeListeners = new LinkedList<PropertyChangeListener>();
	
	private Color colorisation = null;
	private VisualGroup parent = null;
	
	private XMLSerialisation serialisation = new XMLSerialisation();
	private PopupMenuBuilder popupMenuBuilder = new PopupMenuBuilder();
	
	public abstract void draw (Graphics2D g);
	
	public abstract int hitTestInParentSpace(Point2D pointInParentSpace);
	
	public VisualGroup getParent() {
		return parent;
	}
	
	public void setParent(VisualGroup parent) {
		this.parent = parent;		
	}
	
	public int hitTestInAncestorSpace(Point2D pointInUserSpace, VisualGroup ancestor) throws NotAnAncestorException {
		
		if (ancestor != parent) {
			Point2D pt = new Point2D.Double();
			pt.setLocation(pointInUserSpace);
			AffineTransform t = getParentToAncestorTransform(ancestor);
			t.transform(pt,pt);
			return hitTestInParentSpace(pt);
		} else
			return hitTestInParentSpace(pointInUserSpace);
	}
	
	private AffineTransform optimisticInverse(AffineTransform transform)
	{
		try
		{
			return transform.createInverse();
		}
		catch(NoninvertibleTransformException ex)
		{
			throw new RuntimeException("Matrix inverse failed!");
		}
	}
	
	public final AffineTransform getAncestorToParentTransform(VisualGroup ancestor) throws NotAnAncestorException {
		return optimisticInverse(getParentToAncestorTransform(ancestor));
	}
	
	public final AffineTransform getParentToAncestorTransform(VisualGroup ancestor) throws NotAnAncestorException{
		AffineTransform t = new AffineTransform();
		
		VisualGroup next = parent;
		while (ancestor != next) {
			if (next == null)
				throw new NotAnAncestorException();
			t.concatenate(next.getLocalToParentTransform());
			next = next.getParent();
		}
		
		return t;
	}
	
	public abstract Rectangle2D getBoundingBoxInParentSpace();
	
	public final Rectangle2D getBoundingBoxInAncestorSpace(VisualGroup ancestor) throws NotAnAncestorException {
		Rectangle2D parentBB = getBoundingBoxInParentSpace();
		
		Point2D p0 = new Point2D.Double(parentBB.getMinX(), parentBB.getMinY()); 
		Point2D p1 = new Point2D.Double(parentBB.getMaxX(), parentBB.getMaxY());
		
		AffineTransform t = getParentToAncestorTransform(ancestor);
		t.transform(p0, p0);
		t.transform(p1, p1);

		return new Rectangle2D.Double (
				p0.getX(), p0.getY(),
				p1.getX()-p0.getX(),p1.getY() - p0.getY()
		);
	}

	final public List<PropertyDescriptor> getPropertyDeclarations() {
		return propertyDeclarations;
	}

	public void addListener(PropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}
	
	public void removeListener(PropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}
	
	public void firePropertyChanged(String propertyName) {
		for (PropertyChangeListener l : propertyChangeListeners) 
			l.onPropertyChanged(propertyName, this);
	}
	
	public void setColorisation (Color color) {
		colorisation = color;
	}
	
	public Color getColorisation () {
		return colorisation;
	}
	
	public void clearColorisation() {
		setColorisation(null);
	}
	
	public final boolean isDescendantOf(VisualGroup group) {
		VisualNode node = this;
		while(node != group)
		{
			if(node == null)
				return false;
			node = node.parent;
		}
		return true;
	}
	public VisualGroup [] getPath() {
		VisualGroup group = getParent();
		int i = 0;
		while(group!=null)
		{
			i++;
			group = group.getParent();
		}
		VisualGroup [] result = new VisualGroup[i];
		group = getParent();
		while(group!=null)
		{
			result[--i] = group;
			group = group.getParent();
		}
		
		return result;
	}
	
	public static VisualGroup getCommonParent(VisualNode first, VisualNode second) {
		VisualGroup [] path1 = first.getPath();
		VisualGroup [] path2 = second.getPath();
		int size = Math.min(path1.length, path2.length);
		VisualGroup result = null;
		for(int i=0;i<size;i++)
			if(path1[i]==path2[i])
				result = path1[i];
			else
				break;
		return result;
	}
	
	protected final void addPopupMenuSegment (PopupMenuSegment segment) {
		popupMenuBuilder.addSegment(segment);
	}
	
	public final JPopupMenu createPopupMenu(ScriptedActionListener actionListener) {
		return popupMenuBuilder.build(actionListener);
	}
	
	final protected void addPropertyDeclaration(PropertyDescriptor declaration) {
		propertyDeclarations.add(declaration);
	}

	public final void addXMLSerialiser(XMLSerialiser serialiser) {
		serialisation.addSerialiser(serialiser);
	}

	public final void serialiseToXML(Element componentElement) {
		serialisation.serialise(componentElement);
	}
	
	public abstract Set<MathNode> getReferences();
}