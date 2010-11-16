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

package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.CachedHashSet;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class VisualConnection extends VisualNode implements
		Node, Drawable, Connection,
		DependentNode {
	
	private final class Properties extends ExpressionBase<VisualConnectionProperties> {
		@Override
		public VisualConnectionProperties evaluate(EvaluationContext resolver) {
			final Touchable firstShape = resolver.resolve(transformedShape1);
			final Touchable secondShape = resolver.resolve(transformedShape2);
			return new VisualConnectionProperties() {

				@Override
				public Color getDrawColor() {
					return color;
				}

				@Override
				public double getArrowWidth() {
					return VisualConnection.this.getArrowWidth();
				}

				@Override
				public boolean hasArrow() {
					return true;
				}

				@Override
				public Touchable getFirstShape() {
					return firstShape;
				}

				@Override
				public Touchable getSecondShape() {
					return secondShape;
				}

				@Override
				public Stroke getStroke()
				{
					return new BasicStroke((float)getLineWidth());
				}

				@Override
				public double getArrowLength() {
					return VisualConnection.this.getArrowLength();
				}

				@Override
				public ScaleMode getScaleMode() {
					return VisualConnection.this.getScaleMode();
				}
			};
		}
	}

	public enum ConnectionType 
	{
		POLYLINE,
		BEZIER
	};
	
	public enum ScaleMode
	{
		NONE,
		LOCK_RELATIVELY,
		SCALE,
		STRETCH,
		ADAPTIVE
	}
	
	private MathConnection refConnection;
	private VisualComponent first;
	private VisualComponent second;

	private ConnectionType connectionType = ConnectionType.POLYLINE;
	private ScaleMode scaleMode = ScaleMode.LOCK_RELATIVELY;
	
	private ConnectionGraphic graphic = null;

	private static double defaultLineWidth = 0.02;
	private static double defaultArrowWidth = 0.15;
	private static double defaultArrowLength = 0.4;
	public static double HIT_THRESHOLD = 0.2;
	private static Color defaultColor = Color.BLACK;

	private Color color = defaultColor;
	private double lineWidth = defaultLineWidth;
	private double arrowWidth = defaultArrowWidth;
	private double arrowLength = defaultArrowLength;
	
	private CachedHashSet<Node> children = new CachedHashSet<Node>();
	private ExpressionBase<Touchable> transformedShape1;
	private ExpressionBase<Touchable> transformedShape2;
	
	protected void initialise() {
		addPropertyDeclaration(new PropertyDeclaration(this, "Line width", "getLineWidth", "setLineWidth", double.class));
		addPropertyDeclaration(new PropertyDeclaration(this, "Arrow width", "getArrowWidth", "setArrowWidth", double.class));

		LinkedHashMap<String, Object> arrowLengths = new LinkedHashMap<String, Object>();
		arrowLengths.put("short", 0.2);
		arrowLengths.put("medium", 0.4);
		arrowLengths.put("long", 0.8);

		addPropertyDeclaration(new PropertyDeclaration(this, "Arrow length", "getArrowLength", "setArrowLength", double.class, arrowLengths));

		LinkedHashMap<String, Object> hm = new LinkedHashMap<String, Object>();

		hm.put("Polyline", ConnectionType.POLYLINE);
		hm.put("Bezier", ConnectionType.BEZIER);

		addPropertyDeclaration(new PropertyDeclaration(this, "Connection type", "getConnectionType", "setConnectionType", ConnectionType.class, hm));

		LinkedHashMap<String, Object> hm2 = new LinkedHashMap<String, Object>();

		hm2.put("Lock anchors", ScaleMode.NONE);
		hm2.put("Bind to components", ScaleMode.LOCK_RELATIVELY);
		hm2.put("Proportional", ScaleMode.SCALE);
		hm2.put("Stretch", ScaleMode.STRETCH);
		hm2.put("Adaptive", ScaleMode.ADAPTIVE);

		addPropertyDeclaration(new PropertyDeclaration(this, "Scale mode", "getScaleMode", "setScaleMode", ScaleMode.class, hm2));
		
		transformedShape1 = ComponentsTransformer.transform(first, this);
		transformedShape2 = ComponentsTransformer.transform(second, this);
		
		children.add(graphic);
	}

	public VisualConnection() {
		
	}
	
	public void setVisualConnectionDependencies(VisualComponent first, VisualComponent second, ConnectionGraphic graphic, MathConnection refConnection) {
		if(first == null)
			throw new NullPointerException("first");
		if(second == null)
			throw new NullPointerException("second");
		if(graphic == null)
			throw new NullPointerException("graphic");
	
		this.first = first;
		this.second = second;
		this.refConnection = refConnection;
		this.graphic = graphic;
		
		if (graphic instanceof Polyline)
			this.connectionType = ConnectionType.POLYLINE;
		else if (graphic instanceof Bezier)
			this.connectionType = ConnectionType.BEZIER;

		initialise();
	}
	
	public VisualConnection(MathConnection refConnection, VisualComponent first, VisualComponent second) {
		this.refConnection = refConnection;
		this.first = first;
		this.second = second;
		this.graphic = new Polyline(this);
		
		initialise();
	}

	@NoAutoSerialisation
	public ConnectionType getConnectionType() {
		return connectionType;
	}

	@NoAutoSerialisation
	public void setConnectionType(ConnectionType t) {
		if (connectionType!=t) {
			children.remove(graphic);
			
			if (t==ConnectionType.POLYLINE) { 
				graphic = new Polyline(this);
			}
			if (t==ConnectionType.BEZIER) { 
				Bezier b = new Bezier(this); 
				b.setDefaultControlPoints();
				graphic = b;
			}
			
			children.add(graphic);
			
			connectionType = t;
		}
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.visual.connections.VisualConnectionInfo#getColor()
	 */
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.visual.connections.VisualConnectionInfo#getLineWidth()
	 */
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

	/* (non-Javadoc)
	 * @see org.workcraft.dom.visual.connections.VisualConnectionInfo#getArrowWidth()
	 */
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

	public double getArrowLength()
	{
		//if (!hasArrow()) return 0.0;
		return arrowLength;
	}

	public void setArrowLength(double arrowLength) {
		if (arrowLength > 1)
			arrowLength = 1;
		if (arrowLength < 0.1)
			arrowLength = 0.1;
		this.arrowLength = arrowLength;
	}

	public Point2D getPointOnConnection(double t) {
		return GlobalCache.eval(graphic.curve()).getPointOnCurve(t);
	}

	public Point2D getNearestPointOnConnection(Point2D pt) {
		return GlobalCache.eval(graphic.curve()).getNearestPointOnCurve(pt);
	}
	
	@Override
	public void draw(DrawRequest r) {
		
	}

	public MathConnection getReferencedConnection() {
		return refConnection;
	}
	
	@Override
	public Expression<? extends Touchable> shape() {
		return graphic.shape();
	}
	
	@Override
	public VisualComponent getFirst() {
		return first;
	}

	@Override
	public VisualComponent getSecond() {
		return second;
	}

	@Override
	public Set<MathNode> getMathReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		ret.add(getReferencedConnection());
		return ret;
	}

	public ConnectionGraphic getGraphic() {
		return graphic;
	}
	
	@Override
	public ExpressionBase<? extends Collection<Node>> children() {
		return children;
	}

	public ScaleMode getScaleMode() {
		return scaleMode;
	}

	public void setScaleMode(ScaleMode scaleMode) {
		this.scaleMode = scaleMode;
	}
	
	public ExpressionBase<VisualConnectionProperties> properties() {
		return new Properties();
	}
}