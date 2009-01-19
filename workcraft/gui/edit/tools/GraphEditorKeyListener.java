package org.workcraft.gui.edit.tools;

import org.workcraft.gui.events.GraphEditorKeyEvent;

public interface GraphEditorKeyListener {
	public void keyTyped(GraphEditorKeyEvent event);
	public void keyPressed(GraphEditorKeyEvent event);
	public void keyReleased(GraphEditorKeyEvent event);
}
