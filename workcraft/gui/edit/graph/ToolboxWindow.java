package org.workcraft.gui.edit.graph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.workcraft.dom.Component;
import org.workcraft.dom.Model;
import org.workcraft.framework.Framework;
import org.workcraft.gui.DisabledPanel;
import org.workcraft.gui.edit.tools.ComponentCreationTool;
import org.workcraft.gui.edit.tools.ConnectionTool;
import org.workcraft.gui.edit.tools.GraphEditorKeyListener;
import org.workcraft.gui.edit.tools.GraphEditorTool;
import org.workcraft.gui.edit.tools.SelectionTool;
import org.workcraft.gui.edit.tools.ToolProvider;
import org.workcraft.gui.events.GraphEditorKeyEvent;

@SuppressWarnings("serial")
public class ToolboxWindow extends JPanel implements ToolProvider, GraphEditorKeyListener {
	Framework framework;

	SelectionTool selectionTool;
	ConnectionTool connectionTool;

	GraphEditorTool selectedTool;

	HashMap<JToggleButton, GraphEditorTool> map = new HashMap<JToggleButton, GraphEditorTool>();
	HashMap<GraphEditorTool, JToggleButton> reverseMap = new HashMap<GraphEditorTool, JToggleButton>();

	public void addTool (GraphEditorTool tool, boolean selected) {
		JToggleButton button = new JToggleButton();

		button.setSelected(selected);
		button.setFont(button.getFont().deriveFont(9.0f));
		button.setToolTipText(tool.getName());
		button.setText(tool.getName());

		button.setPreferredSize(new Dimension(120,20));
		button.setMinimumSize(new Dimension(120,20));
		button.setMaximumSize(new Dimension(120,20));

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JToggleButton button = (JToggleButton)e.getSource();
				GraphEditorTool tool = map.get(button);
				selectTool(tool);

			}
		});

		map.put(button, tool);
		reverseMap.put(tool, button);

		this.add(button);
	}

	public void selectTool(GraphEditorTool tool) {
		if (selectedTool != null) {
			selectedTool.deactivated(framework.getMainWindow().getCurrentEditor());
			framework.getMainWindow().getCurrentEditor().getModel().getVisualModel().clearColorisation();
			reverseMap.get(selectedTool).setSelected(false);
		}
		tool.activated(framework.getMainWindow().getCurrentEditor());
		reverseMap.get(tool).setSelected(true);
		selectedTool = tool;
		framework.getMainWindow().repaintCurrentEditor();
	}

	public void addCommonTools() {
		addTool(selectionTool, true);
		addTool(connectionTool, false);
	}

	public void setToolsForModel (Model model) {
		map.clear();
		reverseMap.clear();
		removeAll();
		setLayout(new FlowLayout (FlowLayout.LEFT, 5, 5));
		
		addCommonTools();

		for (Class<? extends Component> cls : model.getMathModel().getSupportedComponents()) {
			ComponentCreationTool tool = new ComponentCreationTool(cls);
			addTool(tool, false);
		}
		
		for (Class<?> cls : model.getVisualModel().getAdditionalToolClasses()) {
			if (GraphEditorTool.class.isAssignableFrom(cls)) {
				try {
					addTool((GraphEditorTool)cls.newInstance(), false);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		selectedTool = selectionTool;

		doLayout();
		this.repaint();
	}

	public void clearTools() {
		removeAll();
		setLayout(new BorderLayout());
		this.add(new DisabledPanel(), BorderLayout.CENTER);
		this.repaint();
	}

	public ToolboxWindow(Framework framework) {
		super();
		this.framework = framework;

		selectionTool = new SelectionTool();
		connectionTool = new ConnectionTool();
		selectedTool = null;

		clearTools();
	}

	public GraphEditorTool getTool() {
		return selectedTool;
	}

	public void keyPressed(GraphEditorKeyEvent event) {
		selectedTool.keyPressed(event);
	}

	public void keyReleased(GraphEditorKeyEvent event) {
		selectedTool.keyReleased(event);
		
	}

	public void keyTyped(GraphEditorKeyEvent event) {
		selectedTool.keyTyped(event);
	}
}