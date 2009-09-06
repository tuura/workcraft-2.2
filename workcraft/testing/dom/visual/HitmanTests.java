package org.workcraft.testing.dom.visual;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Touchable;

public class HitmanTests {
	class DummyNode implements Node
	{
		Collection<Node> children;
		public DummyNode()
		{
			children = Collections.emptyList();
		}
		public DummyNode(Node[] children)
		{
			this.children = new ArrayList<Node>(Arrays.asList(children));
		}
		public DummyNode(Collection<Node> children)
		{
			this.children = children;
		}
		
		public Collection<Node> getChildren() {
			return children;
		}

		public Node getParent() {
			throw new RuntimeException("Not Implemented");
		}

		public void setParent(Node parent) {
			throw new RuntimeException("Not Implemented");
		}
	}
	
	class HitableNode extends DummyNode implements Touchable
	{
		public Rectangle2D getBoundingBox() {
			return new Rectangle2D.Double(0, 0, 1, 1);
		}

		public boolean hitTest(Point2D point) {
			return true;
		}
	}
	
	@Test
	public void TestHitDeepestSkipNulls()
	{
		final HitableNode toHit = new HitableNode();
		Node node = new DummyNode(
			new Node[]{ 
					new DummyNode(new Node[]{ toHit }),
					new DummyNode(),
			}
		);
		assertSame(toHit, HitMan.hitDeepestNodeOfType(new Point2D.Double(0.5, 0.5), node, HitableNode.class));
	}
}
