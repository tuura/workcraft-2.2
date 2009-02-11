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
	

	public ChoiceCellEditor(PropertyDeclaration decl) {
		comboBox = new JComboBox();
		comboBox.setEditable(false);
		comboBox.setFocusable(false);
		comboBox.addItemListener(this);
		
		wrappers = new ChoiceWrapper[decl.predefinedValues.size()];
		int j = 0;
		for (String k : decl.predefinedValues.keySet()) {
			wrappers[j] = new ChoiceWrapper(k,decl.predefinedValues.get(k));
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

