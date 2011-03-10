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

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.DummyMouseListener;

public class DefaultAnchorGenerator extends DummyMouseListener {
	
	private final VisualModel model;

	public DefaultAnchorGenerator(VisualModel model) {
		this.model = model;
	}
	
	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		if (e.getClickCount()==2) {
			Node n = HitMan.hitTestForSelection(TouchableProvider.DEFAULT, e.getPosition(), model);
			if (n instanceof VisualConnection) {
				VisualConnection con = (VisualConnection)n;
				if (con.graphic() instanceof Polyline)
					((Polyline)eval(con.graphic())).createControlPoint(e.getPosition());
			}
		}
	}
}
