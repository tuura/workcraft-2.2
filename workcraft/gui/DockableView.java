package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class DockableView extends JPanel {
	class DockableViewHeader extends JPanel {
		JButton btnMin, btnMax, btnDetach;

		public DockableViewHeader(String title) {
			super();
			this.setLayout(new BorderLayout());
			this.setBorder(null);
			btnMin = new JButton();
			btnMin.setPreferredSize(new Dimension(16,16));
			btnMin.setSize(10,10);
			
			btnMin.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
			btnMax = new JButton("max");
			btnDetach = new JButton("dtch");
			this.add(new JLabel(title), BorderLayout.WEST);
			this.add(btnMin, BorderLayout.EAST);
			//this.add(btnMax, BorderLayout.EAST);
		//	this.add(btnDetach, BorderLayout.EAST);
		}

	}
	String title;
	JComponent content;
	JPanel contentPane;
	DockableViewHeader header;
	boolean standalone = true;

	public DockableView (String title, JComponent content) {
		super();
		setLayout(new BorderLayout(0, 0));

		this.title = title;
		
		this.header = new DockableViewHeader(title);
		
		this.contentPane = new JPanel();
		this.contentPane.setLayout(new BorderLayout(0,0));
		this.contentPane.add(content,BorderLayout.CENTER);
		this.contentPane.add(header, BorderLayout.NORTH);
		
		add(this.contentPane, BorderLayout.CENTER);
	}

	public void setStandalone(boolean standalone) {
		this.standalone = standalone;

	}

	public String getTitle() {
		return this.title;
	}

	public JComponent getContent() {
		return this.content;
	}
	/*

	public boolean dock(Dockable dockable) {
		return DockingManager.dock(dockable, this);
	}


	public boolean dock(Dockable dockable, String relativeRegion) {
		return DockingManager.dock(dockable, this, relativeRegion);
	}


	public boolean dock(Dockable dockable, String relativeRegion, float ratio) {
		return DockingManager.dock(dockable, this, relativeRegion, ratio);
	}


	public Component getComponent() {
		return this;
	}


	public DockingPort getDockingPort() {
		return DockingManager.getDockingPort((Dockable)this);
	}


	public DockablePropertySet getDockingProperties() {
		return PropertyManager.getDockablePropertySet(this);
	}

	@SuppressWarnings("unchecked")

	public List getDragSources() {
		LinkedList q = new LinkedList();
		q.add(contentPane);
		return q;
	}

	@SuppressWarnings("unchecked")

	public Set getFrameDragSources() {
		return null;
	}


	public String getPersistentId() {
		return title;
	}


	public void dockingCanceled(DockingEvent evt) {
	}


	public void dockingComplete(DockingEvent evt) {
	}


	public void dragStarted(DockingEvent evt) {
	}


	public void dropStarted(DockingEvent evt) {
	}


	public void undockingComplete(DockingEvent evt) {
	}


	public void undockingStarted(DockingEvent evt) {
	}


	public void addDockingListener(DockingListener listener) {
		DockingEventHandler.addDockingListener(this, listener);
	}


	public DockingListener[] getDockingListeners() {
		return DockingEventHandler.getDockingListeners(this);
	}


	public void removeDockingListener(DockingListener listener) {
		DockingEventHandler.removeDockingListener(this, listener);
	}
	 */
}
