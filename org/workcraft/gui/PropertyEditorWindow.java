package org.workcraft.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.workcraft.Framework;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.gui.propertyeditor.PropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;

@SuppressWarnings("serial")
public class PropertyEditorWindow extends JPanel implements PropertyChangeListener {
	private PropertyEditorTable propertyTable;
	private JScrollPane scrollProperties;

	public PropertyEditorWindow (Framework framework) {
		propertyTable = new PropertyEditorTable();

		scrollProperties = new JScrollPane();
		scrollProperties.setViewportView(propertyTable);

		setLayout(new BorderLayout(0,0));
		add(new DisabledPanel(), BorderLayout.CENTER);
		validate();

	}
	
	public PropertyEditable getObject () {
		return propertyTable.getObject();
	}

	public void setObject (PropertyEditable o) {
		removeAll();
		propertyTable.setObject(o);
		o.addPropertyChangeListener(this);
		add(scrollProperties, BorderLayout.CENTER);
		validate();
		repaint();
	}

	public void clearObject () {
		if (propertyTable.getObject() != null) {
			removeAll();
			propertyTable.getObject().removePropertyChangeListener(this);
			propertyTable.clearObject();
			add(new DisabledPanel(), BorderLayout.CENTER);
			validate();
			repaint();
		}
			
	}

	public void onPropertyChanged(String propertyName, Object sender) {
		repaint();
	}
}
