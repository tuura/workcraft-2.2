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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class ChoiceCellEditor extends AbstractCellEditor implements TableCellEditor, ItemListener {
	private JComboBox comboBox;
	private ChoiceWrapper[] wrappers;
	

	public ChoiceCellEditor(PropertyDescriptor decl) {
		comboBox = new JComboBox();
		comboBox.setEditable(false);
		comboBox.setFocusable(false);
		comboBox.addItemListener(this);
		
		wrappers = new ChoiceWrapper[decl.getChoice().size()];
		int j = 0;
		for (Object o : decl.getChoice().keySet()) {
			wrappers[j] = new ChoiceWrapper(decl.getChoice().get(o), o);
			comboBox.addItem(wrappers[j]);
			j++;
		}
		
	}

	public Object getCellEditorValue() {
		return ((ChoiceWrapper)comboBox.getSelectedItem()).value;
	}

	public Component getTableCellEditorComponent(JTable table,
			Object value,
			boolean isSelected,
			int row,
			int column) {
		for (ChoiceWrapper w : wrappers)
			if (w.text.equals(value))
				comboBox.setSelectedItem(w);
		return comboBox;
	}

	public void itemStateChanged(ItemEvent e) {
		fireEditingStopped();
	}
}

