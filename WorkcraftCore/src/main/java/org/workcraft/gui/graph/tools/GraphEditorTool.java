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

package org.workcraft.gui.graph.tools;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dom.visual.SimpleGraphicalContent;
import org.workcraft.gui.graph.Viewport;

public interface GraphEditorTool extends GraphEditorKeyListener, GraphEditorMouseListener {
	public void activated();
	public void deactivated();
	
	public Expression<? extends SimpleGraphicalContent> userSpaceContent();
	public Expression<? extends SimpleGraphicalContent> screenSpaceContent(Viewport viewport);
	
	public JPanel getInterfacePanel();

	public String getLabel();
	public Icon getIcon();
	public int getHotKeyCode();
	
	public Expression<? extends Decorator> getDecorator();
}
