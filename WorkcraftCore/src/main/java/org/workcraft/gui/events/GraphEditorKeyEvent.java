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

package org.workcraft.gui.events;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class GraphEditorKeyEvent {
	char keyChar;
	int keyCode;
	int modifiers;
	public GraphEditorKeyEvent(KeyEvent event)
	{
		keyChar = event.getKeyChar();
		keyCode = event.getKeyCode();
		modifiers = event.getModifiersEx();
	}
	
	public char getKeyChar()
	{
		return keyChar;
	}
	
	public int getKeyCode()
	{
		return keyCode;
	}

	private boolean isMaskHit(int mask)
	{
		return (modifiers&mask) == mask; 
	}
	
	public boolean isCtrlDown()
	{
		return isMaskHit(InputEvent.CTRL_DOWN_MASK);
	}
	
	public boolean isShiftDown()
	{
		return isMaskHit(InputEvent.SHIFT_DOWN_MASK);
	}
	
	public boolean isAltDown()
	{
		return isMaskHit(InputEvent.ALT_DOWN_MASK);
	}
	
	public int getModifiers()
	{
		return modifiers;
	}
}
