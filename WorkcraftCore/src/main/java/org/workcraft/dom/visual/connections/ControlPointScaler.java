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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.IExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionImpl;
import org.workcraft.dependencymanager.util.listeners.Listener;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.exceptions.NotImplementedException;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

import static org.workcraft.util.Geometry.*;

public class ControlPointScaler extends Expression<List<ModifiableExpression<AffineTransform>>> {
	private static double THRESHOLD = 0.00001;
	private Point2D oldC1, oldC2;
	private final IExpression<AffineTransform> originalTransform;
	private final Expression<Point2D> p1;
	private final Expression<Point2D> p2;
	private final IExpression<? extends Collection<? extends ControlPoint>> controlPoints;
	private final Expression<ScaleMode> scaleMode;

	public ControlPointScaler(IExpression<AffineTransform> transform, Expression<Point2D> p1, Expression<Point2D> p2, Expression<ScaleMode> scaleMode) {
		this.p1 = p1;
		this.p2 = p2;
		this.scaleMode = scaleMode;
	}

	public Expression<List<ModifiableExpression<AffineTransform>>> scale (Point2D oldC1, Point2D oldC2, Point2D newC1, Point2D newC2, Collection<? extends ControlPoint> controlPoints, VisualConnection.ScaleMode mode) {
		if (mode == VisualConnection.ScaleMode.NONE)
			return;
		
		if (mode == VisualConnection.ScaleMode.LOCK_RELATIVELY)
		{
			Point2D dC1 = subtract(newC1, oldC1);
			Point2D dC2 = subtract(newC2, oldC2);
			
			int n = controlPoints.size();
			int i=0;
			for (ControlPoint cp : controlPoints)
			{
				Point2D delta;
				if(i<n/2)
					delta = dC1;
				else
					if(i>(n-1)/2)
						delta = dC2;
					else
						delta = multiply(add(dC1, dC2), 0.5);

				cp.position().setValue(add(eval(cp.position()), delta));

				i++;	
			}
			return;
		}

		Point2D v0 = subtract(oldC2, oldC1);

		if (v0.distanceSq(0, 0) < THRESHOLD)
			v0.setLocation(0.001, 0);

		Point2D up0 = getUpVector(mode, v0);

		Point2D v = subtract(newC2, newC1);

		if (v.distanceSq(0, 0) < THRESHOLD)
			v.setLocation(0.001, 0);

		Point2D up = getUpVector(mode, v);

		for (ControlPoint cp : controlPoints) {
			Point2D p = subtract(eval(cp.position()), oldC1);

			Point2D dp = changeBasis (p, v0, up0);

			cp.setPosition(
					add(
							add(
									multiply (v, dp.getX()),
									multiply (up, dp.getY())),
									newC1
					));
		}
	}

	private Point2D getUpVector(VisualConnection.ScaleMode mode, Point2D v0) {
		switch (mode) {
		case SCALE:
			return rotate90CCW(v0);
		case STRETCH:
			return normalize(rotate90CCW(v0));
		case ADAPTIVE:
			return reduce(rotate90CCW(v0));
		default:
			throw new RuntimeException ("Unexpected value of scale mode");
		}
	}

	@Override
	protected void simpleSetValue(AffineTransform newValue) {
		
	}

	private static boolean almostEqual(Point2D p1, Point2D p2) {
		Point2D diff = subtract(p1, p2);
		double sq = dotProduct(diff, diff);
		return sq<0.001;
	}
	
	@Override
	protected AffineTransform evaluate(EvaluationContext context) {
		
		Point2D point1 = context.resolve(p1);
		Point2D point2 = context.resolve(p2);
		
		if (oldC1==null || oldC2 == null) {
			oldC1 = point1;
			oldC2 = point2;
		}
		
		if(!almostEqual(point1, oldC1) || !almostEqual(point2, oldC2)) {
			scale(oldC1, oldC2, point1, point2, context.resolve(controlPoints), context.resolve(scaleMode));
		}
		
		return null;
	}
}