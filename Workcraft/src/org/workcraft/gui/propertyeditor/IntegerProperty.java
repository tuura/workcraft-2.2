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

package org.workcraft.gui.propertyeditor;

import java.util.IllegalFormatException;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class IntegerProperty implements PropertyClass {

	public TableCellEditor getCellEditor() {
		GenericCellEditor dce = new GenericCellEditor();
		return dce;
	}

	public TableCellRenderer getCellRenderer() {
		return new DefaultTableCellRenderer();
	}

	public Object fromCellEditorValue(Object editorComponentValue) {
		try {
			return Integer.parseInt((String)editorComponentValue);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	public Object toCellRendererValue(Object value) {
		try {
			return String.format("%d", value);
		} catch (IllegalFormatException e) {
			return "#getter did not return an int";
		}
	}
	
}
