package org.workcraft.testing.plugins.petri;

import java.awt.geom.Point2D;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;

public class VisualPlaceTests {
	@Test
	public void TestHitTest() {
		Place p = new Place();
		VisualPlace vp = new VisualPlace(p);
		
		Assert.assertEquals(vp, vp.hitTest(new Point2D.Double(0,0)));
		Assert.assertEquals(null, vp.hitTest(new Point2D.Double(5,5)));
		
		vp.setX(5);
		vp.setY(5);
		
		Assert.assertEquals(vp, vp.hitTest(new Point2D.Double(5,5)));
		Assert.assertEquals(null, vp.hitTest(new Point2D.Double(0,0)));
	}
}
