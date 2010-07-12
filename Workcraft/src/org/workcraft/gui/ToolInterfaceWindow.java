package org.workcraft.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.workcraft.gui.graph.tools.GraphEditorTool;

@SuppressWarnings("serial")
public class ToolInterfaceWindow extends JPanel {
	private JScrollPane content;
	
	public ToolInterfaceWindow() {
		super(new BorderLayout());
		content = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		content.setBorder(null);
		content.setViewportView(new DisabledPanel());
		add(content, BorderLayout.CENTER);
	}
	
	public void setTool(GraphEditorTool tool) {
		if (tool==null || tool.getInterfacePanel() == null)
			content.setViewportView(new DisabledPanel());
		else
			content.setViewportView(tool.getInterfacePanel());
		
		content.revalidate();
	}
}
