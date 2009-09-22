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

package org.workcraft.plugins.layout;

import java.util.Random;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.layout.Layout;

@DisplayName("Random")
public class RandomLayout implements Layout {
	Random r = new Random();
	public void doLayout(VisualModel model) {
		for (Node n : model.getRoot().getChildren()) {
			if (n instanceof VisualTransformableNode) {
				((VisualTransformableNode)n).setX(RandomLayoutSettings.startX + r.nextDouble()*RandomLayoutSettings.rangeX);
				((VisualTransformableNode)n).setY(RandomLayoutSettings.startY + r.nextDouble()*RandomLayoutSettings.rangeY);
			}
		}
	}

	public boolean isApplicableTo(VisualModel model) {
			return true;
	}
}