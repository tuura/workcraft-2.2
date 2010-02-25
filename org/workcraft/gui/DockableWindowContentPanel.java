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

package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.flexdock.docking.Dockable;
import org.workcraft.gui.actions.ScriptedAction;
import org.workcraft.gui.actions.ScriptedActionButton;
import org.workcraft.gui.actions.ScriptedActionListener;

@SuppressWarnings("serial")
public class DockableWindowContentPanel extends JPanel {
	static public class ViewAction extends ScriptedAction {
		public static final int CLOSE_ACTION = 1;
		public static final int MINIMIZE_ACTION = 2;
		public static final int MAXIMIZE_ACTION = 3;

		private int windowID;
		private int actionType; 

		public ViewAction (int windowID, int actionType) {
			this.actionType = actionType;
			this.windowID = windowID;
		}

		public String getScript() {
			switch (actionType) {
			case CLOSE_ACTION:
				return tryOperation("mainWindow.closeDockableWindow("+windowID+");");
			case MINIMIZE_ACTION:
				return "mainWindow.minimizeDockableWindow("+windowID+");";
			case MAXIMIZE_ACTION:
				return "mainWindow.toggleDockableWindowMaximized("+windowID+");";
			}
			return null;
		}

		public String getText() {
			return null;
		}	
	}

	class DockableViewHeader extends JPanel {
		private ScriptedActionButton btnMin, btnMax, btnClose;
		private JPanel buttonPanel = null;
		private boolean maximized = false;

		private ScriptedActionButton createHeaderButton(Icon icon, ScriptedAction action, ScriptedActionListener actionListener) {
			ScriptedActionButton button = new ScriptedActionButton(action);
			button.addScriptedActionListener(actionListener);
			button.setPreferredSize(new Dimension(icon.getIconWidth(),icon.getIconHeight()));
			button.setFocusable(false);
			button.setBorder(null);


			button.setIcon(icon);
			return button;
		}

		public DockableViewHeader(String title, int options) {
			super();
			setLayout(new BorderLayout());

			Color c;

			if (UIManager.getLookAndFeel().getName().contains("Substance")) {
				c = getBackground();
				c = new Color( (int)(c.getRed() * 0.9), (int)(c.getGreen() * 0.9), (int)(c.getBlue() * 0.9) );
			} else
				c = UIManager.getColor("InternalFrame.activeTitleBackground");

			setBackground(c);

			if  (options != 0) {
				buttonPanel = new JPanel();
				buttonPanel.setBackground(c);
				buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 4,2));
				buttonPanel.setFocusable(false);
				add(buttonPanel, BorderLayout.EAST);
			}
			
			int icons = 0;

			if ( (options & MINIMIZE_BUTTON) != 0) {
				btnMin = createHeaderButton(UIManager.getIcon("InternalFrame.minimizeIcon"),
						new ViewAction(ID, ViewAction.MINIMIZE_ACTION), mainWindow.getDefaultActionListener());
				btnMin.setToolTipText("Toggle minimized");
				buttonPanel.add(btnMin);
				icons ++;
			}

			if ( (options & MAXIMIZE_BUTTON) != 0) {
				btnMax = createHeaderButton(UIManager.getIcon("InternalFrame.maximizeIcon"),
						new ViewAction(ID, ViewAction.MAXIMIZE_ACTION), mainWindow.getDefaultActionListener());
				buttonPanel.add(btnMax);
				icons ++;
			}

			if ( (options & CLOSE_BUTTON) != 0) {
				//System.out.println (UIManager.getColor("InternalFrame.activeTitleGradient"));
				btnClose = createHeaderButton(UIManager.getIcon("InternalFrame.closeIcon"),
						new ViewAction(ID, ViewAction.CLOSE_ACTION), mainWindow.getDefaultActionListener());
				btnClose.setToolTipText("Close window");
				buttonPanel.add(btnClose);
				icons ++;
			}
			
			if (icons != 0) {
				buttonPanel.setPreferredSize(new Dimension((UIManager.getIcon("InternalFrame.closeIcon").getIconWidth()+4) * icons,UIManager.getIcon("InternalFrame.closeIcon").getIconHeight()+4));
				
			}


			JLabel label = new JLabel(" "+ title);
			label.setOpaque(false);
			label.setForeground(UIManager.getColor("InternalFrame.activeTitleForeground"));
			label.setFont(label.getFont().deriveFont(Font.BOLD));

			add(label, BorderLayout.WEST);
			
			setMaximized(false);
		}

		public boolean isMaximized() {
			return maximized;
		}

		public void setMaximized(boolean maximized) {
			this.maximized = maximized;

			if (btnMax != null)
				if (maximized) {
					btnMax.setIcon(UIManager.getIcon("InternalFrame.minimizeIcon"));
					btnMax.setToolTipText("Restore window");
				}
				else {
					btnMax.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
					btnMax.setToolTipText("Maximize window");					
				}
		}
	}

	public static final int CLOSE_BUTTON = 1;
	public static final int MINIMIZE_BUTTON = 2;
	public static final int MAXIMIZE_BUTTON = 4;
	public static final int HEADER = 8;

	private String title;
	private JComponent content;
	private JPanel contentPane;
	private DockableViewHeader header;
	private MainWindow mainWindow;
	private int ID;
	private int options;
	private Dockable dockable = null;

	public boolean isMaximized() {
		if (header != null)
			return header.isMaximized();
		else
			return false;
	}

	public void setMaximized(boolean maximized) {
		if (header != null)
			header.setMaximized(maximized);
	}

	public Dockable getDockable() {
		return dockable;
	}

	public void setDockable(Dockable dockable) {
		this.dockable = dockable;
	}

	public DockableWindowContentPanel (MainWindow mainWindow, int ID, String title, JComponent content, int options) {
		super();
		setLayout(new BorderLayout(0, 0));

		this.title = title;
		this.mainWindow = mainWindow;
		this.ID = ID;
		this.content = content;
		
		if ( (options & ~HEADER) > 0)
			this.options = options | HEADER;
		else
			this.options = options;

		header = new DockableViewHeader(title, options);

		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0,0));
		contentPane.add(content,BorderLayout.CENTER);
		contentPane.setBorder(BorderFactory.createLineBorder(contentPane.getBackground(), 2));
		
		if ((options & HEADER) > 0)
			contentPane.add(header, BorderLayout.NORTH);
		
		add(contentPane, BorderLayout.CENTER);
		
		setFocusable(false);
		
	}

	public void setHeaderVisible(boolean headerVisible) {
		if (headerVisible && ((options & HEADER) > 0)) {
			if (header.getParent() != contentPane) 
				contentPane.add(header, BorderLayout.NORTH);
		}
		else
			contentPane.remove(header);

		contentPane.doLayout();	
	}

	public String getTitle() {
		return title;
	}

	public int getID() {
		return ID;
	}

	public JComponent getContent() {
		return content;
	}

	public int getOptions() {
		return options;
	}
}
