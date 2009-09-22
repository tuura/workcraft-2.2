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
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditorMouseListener;

public class DefaultAnchorGenerator implements GraphEditorMouseListener {
	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		if (e.getClickCount()==2) {
			Node n = HitMan.hitTestForSelection(e.getPosition(), e.getModel());
			if (n instanceof VisualConnection) {
				VisualConnection con = (VisualConnection)n;
				if (con.getGraphic() instanceof Polyline)
					((Polyline)con.getGraphic()).addAnchorPoint(e.getPosition());
			}
		}
	}

	@Override
	public void mouseEntered(GraphEditorMouseEvent e) {
	}

	@Override
	public void mouseExited(GraphEditorMouseEvent e) {
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
	}

	@Override
	public void mouseReleased(GraphEditorMouseEvent e) {
	}
}