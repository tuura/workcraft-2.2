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

package org.workcraft.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.util.Function;

public interface Touchable {
	Function<Touchable, Rectangle2D> boundingBoxGetter = new Function<Touchable, Rectangle2D>(){
		@Override
		public Rectangle2D apply(Touchable argument) {
			return argument.getBoundingBox();
		}
	};
	Function<Touchable, Point2D> centerGetter = new Function<Touchable, Point2D>() {

		@Override
		public Point2D apply(Touchable argument) {
			return argument.getCenter();
		}
	};
	public boolean hitTest(Point2D point);
	public Rectangle2D getBoundingBox();
	public Point2D getCenter();
}
