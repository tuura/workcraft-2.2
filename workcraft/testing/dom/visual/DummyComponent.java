package org.workcraft.testing.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualComponentGroup;

class SquareNode extends VisualComponent
{
	Rectangle2D.Double rectOuter;
	Rectangle2D.Double rectInner;
	int resultToReturn;
	public SquareNode(VisualComponentGroup parent, Rectangle2D.Double rectOuter, Rectangle2D.Double rectInner) {
		super(null, parent);
		this.rectOuter = rectOuter;
		this.rectInner = rectInner;
	}

	public SquareNode(VisualComponentGroup parent, Rectangle2D.Double rect) {
		this(parent, rect, rect);
	}

	@Override
	public void draw(Graphics2D g) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String toString() {
		return rectInner.toString();
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		return rectOuter;
	}

	@Override
	public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if(rectInner.contains(pointInLocalSpace))
			return 1;
		return 0;
	}
}

