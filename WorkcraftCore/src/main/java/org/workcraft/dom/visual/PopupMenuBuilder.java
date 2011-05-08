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

import java.util.LinkedList;

import javax.swing.JPopupMenu;

import org.workcraft.gui.actions.ScriptedActionListener;

public class PopupMenuBuilder {
	public static interface PopupMenuSegment {
		public void addItems (JPopupMenu menu, ScriptedActionListener actionListener);
	}
	
	LinkedList<PopupMenuSegment> segments = new LinkedList<PopupMenuSegment>();
	
	public void addSegment (PopupMenuSegment segment) {
		segments.add(segment);
	}
	
	public JPopupMenu build(ScriptedActionListener actionListener) {
		JPopupMenu menu = new JPopupMenu();
		
		for (PopupMenuSegment segment : segments)
			segment.addItems(menu, actionListener);
		
		return menu;
	}
}
