package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.dom.HierarchyController;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.MovableController;
import org.workcraft.util.Function;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class VisualModelTransformer {
	/**
	 * Only transforms node position (not orientation)
	 * @author Stanislav Golubtsov
	 *   
	 **/
	public static void transformNodePosition(Collection<? extends Node> nodes, AffineTransform t) {
		assert nodes!=null;
		for (Node node: nodes) {
			// do transformation group children
			if (node instanceof VisualGroup) {
				VisualGroup vg = (VisualGroup) node;
				
//				t.translate(selectionBB.getCenterX(), selectionBB.getCenterY());
				
				AffineTransform t2 = new AffineTransform();
				
				t2.translate(-eval(vg.x()), -eval(vg.y()));
				t2.concatenate(t);
				t2.translate(eval(vg.x()), eval(vg.y()));
				
				transformNodePosition(eval(vg.children()), t2);
			} else if (node instanceof VisualTransformableNode) {
				VisualTransformableNode vn = (VisualTransformableNode) node;
				
				Point2D np=eval(vn.position());
				t.transform(np, np);
				vn.position().setValue(np);
			}
		}
//			TransformHelper.applyTransform(node, t);
	}
	public static void translateNodes(Collection<? extends Node> nodes, double tx, double ty) {
		AffineTransform t = AffineTransform.getTranslateInstance(tx, ty);

		transformNodePosition(nodes, t);
	}

	public static void scaleNodes(VisualModel vm, double sx, double sy) {
		Rectangle2D selectionBB = getNodesCoordinateBox(eval(vm.selection()));
		
		AffineTransform t = new AffineTransform();

		t.translate(selectionBB.getCenterX(), selectionBB.getCenterY());
		t.scale(sx, sy);
		t.translate(-selectionBB.getCenterX(), -selectionBB.getCenterY());

		transformNodePosition(eval(vm.selection()), t);
	}

	public static void rotateNodes(Function<Point2D, Point2D> snap, MovableController<Node>mc, Collection<Node> selection, double theta) {
		Rectangle2D selectionBB = getNodesCoordinateBox(selection);

		AffineTransform t = new AffineTransform();
		
		//Point2D cp = (new Point2D.Double(selectionBB.getCenterX(), selectionBB.getCenterY()));
		Point2D cp = snap.apply(new Point2D.Double(selectionBB.getCenterX(), selectionBB.getCenterY()));

		t.translate(cp.getX(), cp.getY());
		t.rotate(theta);
		t.translate(-cp.getX(), -cp.getY());

		transformNodePosition(eval(vm.selection()), t);
	}

	private static Rectangle2D bbUnion(Rectangle2D bb1, Point2D bb2)
	{
		if(bb2 == null)
			return bb1;
		
		Rectangle2D r = new Rectangle2D.Double(bb2.getX(), bb2.getY(), 0, 0);
		
		if (bb1 == null) {
			bb1=r;
		} else Rectangle2D.union(bb1, r, bb1);
		
		return bb1;
	}
	
	public static Rectangle2D getNodesCoordinateBox(TouchableProvider<Node> tp, HierarchyProvider<Node> hp, Collection<? extends Node> nodes) {
		Rectangle2D selectionBB = null;


		for (Node vn: nodes) {
			if (vn instanceof VisualGroup) {
				Rectangle2D r = getNodesCoordinateBox(eval(((VisualGroup)vn).children()));
				Point2D p = eval(((VisualGroup)vn).position());
				r.setRect(r.getX()+p.getX(), r.getY()+p.getY(), r.getWidth(), r.getHeight());
				
//				System.out.printf("%f %f %f %f\n", r.getX(), r.getY(), r.getWidth(), r.getHeight());
				
				if (selectionBB==null)
					selectionBB = r;
				else if (r!=null)
					Rectangle2D.union(selectionBB, r, selectionBB);
			} else if(vn instanceof VisualTransformableNode)
				selectionBB = bbUnion(selectionBB, eval(((VisualTransformableNode)vn).position()));
		}
		return selectionBB;
	}
	
}
