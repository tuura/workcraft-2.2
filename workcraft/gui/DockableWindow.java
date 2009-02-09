package org.workcraft.gui;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTabbedPane;

import org.flexdock.docking.DockingPort;
import org.flexdock.docking.defaults.AbstractDockable;
import org.flexdock.docking.event.DockingEvent;

public class DockableWindow extends AbstractDockable {
	private DockableWindowContentPanel panel;
	private LinkedList<Component> dragSources = new LinkedList<Component>();
	private boolean closed = false;
	
	public boolean isMaximized() {
		return panel.isMaximized();
	}

	public void setMaximized(boolean maximized) {
		panel.setMaximized(maximized);
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public DockableWindow(DockableWindowContentPanel panel, String persistentID) {
		super(persistentID);
		this.panel = panel;
		setTabText(panel.getTitle());
		dragSources.add(panel);
	}

	public Component getComponent() {
		return panel;
	}
	
	public DockableWindowContentPanel getContentPanel() {
		return panel;
	}
	
	public static void updateHeaders(DockingPort port) {
		for (Object d : port.getDockables()) {
			DockableWindow dockable = (DockableWindow)d;

			boolean inTab = dockable.getComponent().getParent() instanceof JTabbedPane;
			
			if (inTab)
				dockable.getContentPanel().setHeaderVisible(false);
			else
				dockable.getContentPanel().setHeaderVisible(true);
		}
	}
	
	public String getTitle() {
		return panel.getTitle();
	}
	
	public int getID() {
		return panel.getID();
	}

	@Override
	public void dockingComplete(DockingEvent evt) {
//		System.out.println ("docked " + getTitle());
		updateHeaders(evt.getNewDockingPort());
		super.dockingComplete(evt);
		
	}
	
	@Override
	public void undockingComplete(DockingEvent evt) {
//		System.out.println ("undocked " + getTitle());		
		updateHeaders(evt.getOldDockingPort());
		super.undockingComplete(evt);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List getDragSources() {
		return dragSources;		
	}

}
