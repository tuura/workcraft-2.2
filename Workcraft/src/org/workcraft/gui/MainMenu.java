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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.workcraft.Framework;
import org.workcraft.PluginInfo;
import org.workcraft.Tool;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionCheckBoxMenuItem;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.workspace.WorkspaceWindow;
import org.workcraft.interop.Exporter;
import org.workcraft.util.ListMap;
import org.workcraft.util.Pair;
import org.workcraft.util.Tools;

@SuppressWarnings("serial")
public class MainMenu extends JMenuBar {
	class ToolAction extends Action {
		Class<? extends Tool> toolClass;
		String text;

		public ToolAction(Pair<String, Tool> tool) {
			this.toolClass = tool.getSecond().getClass();
			this.text = tool.getFirst();
		}

		public String getText() {
			return text;
		}

		@Override
		public void run(Framework framework) {
			framework.getMainWindow().runTool(toolClass);
		}
	}	
	class ToggleWindowAction extends Action {
		private DockableWindow window;
		private String windowTitle;

		public ToggleWindowAction(DockableWindow window) {
			this.window = window;
			windowTitle = window.getTitle();
		}
		@Override
		public String getText() {
			return windowTitle;
		}
		@Override
		public void run(Framework framework) {
			framework.getMainWindow().toggleDockableWindow(window);
		}
	}
	class ExportAction extends Action {
		private Class<? extends Exporter> exporterClass;
		private String displayName;
		
		public ExportAction(Exporter exporter) {
			exporterClass = exporter.getClass();
			displayName = exporter.getDescription();
		}
		
		@Override
		public void run(Framework framework) {
			try {framework.getMainWindow().exportTo(exporterClass);} catch (OperationCancelledException e) {}
		}

		public String getText() {
			return displayName;
		}
	}
	
	private JMenu mnFile, mnEdit, mnView, mnSettings, mnHelp, mnWindows;
	private JMenu mnExport;

	private MainWindow mainWindow;
	private HashMap <Integer, ActionCheckBoxMenuItem> windowItems = new HashMap<Integer, ActionCheckBoxMenuItem>();
	
	private LinkedList<JMenu> toolMenus = new LinkedList<JMenu>();

	private String[] lafCaptions = new String[] {
			"Java default",
			"Windows",
			"Substance: Moderate",
			"Substance: Mist Silver",
			"Substance: Raven",
			"Substance: Business",
			"Substance: Creme"
	};
	private String[] lafClasses = new String[] {
			"javax.swing.plaf.metal.MetalLookAndFeel",
			"com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
			"org.jvnet.substance.skin.SubstanceModerateLookAndFeel",
			"org.jvnet.substance.skin.SubstanceMistSilverLookAndFeel",
			"org.jvnet.substance.skin.SubstanceRavenLookAndFeel",
			"org.jvnet.substance.skin.SubstanceBusinessLookAndFeel",
			"org.jvnet.substance.skin.SubstanceCremeCoffeeLookAndFeel"
	};

	MainMenu(final MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		// File
		mnFile = new JMenu();
		mnFile.setText("File");

		ActionMenuItem miNewModel = new ActionMenuItem(MainWindowActions.CREATE_WORK_ACTION);
		miNewModel.setMnemonic(KeyEvent.VK_N);
		miNewModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		miNewModel.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miExit = new ActionMenuItem(MainWindowActions.EXIT_ACTION);
		miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		miExit.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miShutdownGUI = new ActionMenuItem(MainWindowActions.SHUTDOWN_GUI_ACTION);
		miShutdownGUI.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miOpenModel = new ActionMenuItem(MainWindowActions.OPEN_WORK_ACTION);
		miOpenModel.setMnemonic(KeyEvent.VK_O);
		miOpenModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		miOpenModel.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miSaveWork = new ActionMenuItem(MainWindowActions.SAVE_WORK_ACTION);
		miSaveWork.setMnemonic(KeyEvent.VK_S);
		miSaveWork.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		miSaveWork.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miSaveWorkAs = new ActionMenuItem(MainWindowActions.SAVE_WORK_AS_ACTION);
		miSaveWorkAs.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miNewWorkspace = new ActionMenuItem(WorkspaceWindow.Actions.NEW_WORKSPACE_AS_ACTION);
		miNewWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());
		
		ActionMenuItem miOpenWorkspace = new ActionMenuItem(WorkspaceWindow.Actions.OPEN_WORKSPACE_ACTION);
		miOpenWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());
		
		ActionMenuItem miAddFiles = new ActionMenuItem(WorkspaceWindow.Actions.ADD_FILES_TO_WORKSPACE_ACTION);
		miAddFiles.addScriptedActionListener(mainWindow.getDefaultActionListener());
		
		
		ActionMenuItem miSaveWorkspace = new ActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_ACTION);
		miSaveWorkspace.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miSaveWorkspaceAs = new ActionMenuItem(WorkspaceWindow.Actions.SAVE_WORKSPACE_AS_ACTION);
		miSaveWorkspaceAs.addScriptedActionListener(mainWindow.getDefaultActionListener());

		ActionMenuItem miImport = new ActionMenuItem(MainWindowActions.IMPORT_ACTION);
		miImport.addScriptedActionListener(mainWindow.getDefaultActionListener());	

		ActionMenuItem miCloseAll = new ActionMenuItem(MainWindowActions.CLOSE_ALL_EDITORS_ACTION);
		miCloseAll.addScriptedActionListener(mainWindow.getDefaultActionListener());
		
		ActionMenuItem miCloseActive = new ActionMenuItem(MainWindowActions.CLOSE_ACTIVE_EDITOR_ACTION);
		miCloseActive.addScriptedActionListener(mainWindow.getDefaultActionListener());

		mnExport = new JMenu("Export");
		mnExport.setEnabled(false);

		mnFile.add(miNewModel);
		mnFile.add(miOpenModel);
		mnFile.add(miSaveWork);
		mnFile.add(miSaveWorkAs);
		mnFile.add(miCloseActive);
		mnFile.add(miCloseAll);
		mnFile.addSeparator();
		mnFile.add(miImport);
		mnFile.add(mnExport);
		

		mnFile.addSeparator();
		mnFile.add(miNewWorkspace);
		mnFile.add(miOpenWorkspace);
		mnFile.add(miAddFiles);
		mnFile.add(miSaveWorkspace);
		mnFile.add(miSaveWorkspaceAs);
		mnFile.addSeparator();
		mnFile.add(miShutdownGUI);
		mnFile.add(miExit);


		// View
		mnView = new JMenu();
		mnView.setText ("View");

		JMenu mnLAF = new JMenu();
		mnLAF.setText("Look and Feel");

		for(int i=0; i<lafClasses.length; i++) {
			JMenuItem miLAFItem = new JMenuItem();
			miLAFItem.setText(lafCaptions[i]);
			final String lafClass = lafClasses[i];
			miLAFItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						mainWindow.setLAF(lafClass);
					} catch (OperationCancelledException e1) {
					}
				}
			});
			mnLAF.add(miLAFItem);
		}

		mnWindows = new JMenu();
		mnWindows.setText("Windows");

		/*	ScriptedActionMenuItem miSaveLayout = new ScriptedActionMenuItem(MainWindow.Actions.SAVE_UI_LAYOUT);
		miSaveLayout.addScriptedActionListener(mainWindow.getDefaultActionListener());		
		mnView.add(miSaveLayout);

		ScriptedActionMenuItem miLoadLayout = new ScriptedActionMenuItem(MainWindow.Actions.LOAD_UI_LAYOUT);
		miLoadLayout.addScriptedActionListener(mainWindow.getDefaultActionListener());		
		mnView.add(miLoadLayout);*/

		mnView.add(mnWindows);
		mnView.add(mnLAF);

		// Edit
		mnEdit = new JMenu();
		mnEdit.setText("Edit");
		
		ActionMenuItem miProperties = new ActionMenuItem(MainWindowActions.EDIT_SETTINGS_ACTION);
		miProperties.addScriptedActionListener(mainWindow.getDefaultActionListener());

		mnEdit.add(miProperties);

		// Settings
		mnSettings = new JMenu();
		mnSettings.setText("Utility");
		
		ActionMenuItem miReconfigure = new ActionMenuItem(MainWindowActions.RECONFIGURE_PLUGINS_ACTION);
		miReconfigure.addScriptedActionListener(mainWindow.getDefaultActionListener());
		
		mnSettings.add(miReconfigure);

		// Help
		mnHelp = new JMenu();
		mnHelp.setText("Help");

		add(mnFile);
		add(mnEdit);
		add(mnView);
		add(mnSettings);
		add(mnHelp);
	}

	private void addExporter (Exporter exporter) {
		addExporter (exporter, null);
	}
	
	private void addExportSeparator (String text) {
		mnExport.add(new JLabel(text));
		mnExport.addSeparator();
	}
	
	private void addExporter (Exporter exporter, String additionalDescription) {
		ActionMenuItem miExport = new ActionMenuItem(new ExportAction(exporter), 
				additionalDescription == null? 
					exporter.getDescription() 
					: 
					exporter.getDescription()+ " " + additionalDescription);
		
		miExport.addScriptedActionListener(mainWindow.getDefaultActionListener());
		mnExport.add(miExport);
		mnExport.setEnabled(true);
	}

	final public void setMenuForModel(VisualModel model) {
		for (JMenu toolMenu : toolMenus)
			remove(toolMenu);
		
		toolMenus.clear();

		Framework framework = mainWindow.getFramework();
		
		ListMap<String, Pair<String, Tool>> tools = Tools.getTools(model, framework);
		List<String> sections = Tools.getSections(tools);
		
		for (String section : sections) {
			JMenu sectionMenu = new JMenu (section);
			toolMenus.add(sectionMenu);
			add(sectionMenu);
			
			for (Pair<String, Tool> tool : Tools.getSectionTools(section, tools)) {
				ActionMenuItem miTool = new ActionMenuItem(new ToolAction(tool));
				miTool.addScriptedActionListener(mainWindow.getDefaultActionListener());
				sectionMenu.add(miTool);
			}
		}

		mnExport.removeAll();
		mnExport.setEnabled(false);

		PluginInfo[] exportPluginInfo = framework.getPluginManager().getPluginsImplementing(Exporter.class.getName());
		
		boolean haveVisual = false;

		try {
			for (PluginInfo info : exportPluginInfo) { 
				Exporter exporter = (Exporter)framework.getPluginManager().getSingleton(info);

				if (exporter.getCompatibility(model) > Exporter.NOT_COMPATIBLE) {
					if (!haveVisual)
						addExportSeparator("Visual");
					addExporter(exporter);
					haveVisual = true;
				}
			}
		}  catch (PluginInstantiationException e) {
			System.err.println ("Could not instantiate export plugin class: " + e.getMessage() + " (skipped)");
		}
		
		boolean haveNonVisual = false;

		try {
			for (PluginInfo info : exportPluginInfo) { 
				Exporter exporter = (Exporter)framework.getPluginManager().getSingleton(info);

				if (exporter.getCompatibility(model.getMathModel()) > Exporter.NOT_COMPATIBLE) {
					if (!haveNonVisual)
						addExportSeparator("Non-visual");
					addExporter(exporter);
					haveNonVisual = true;
				}
			}
		}  catch (PluginInstantiationException e) {
			System.err.println ("Could not instantiate export plugin class: " + e.getMessage() + " (skipped)");
		}
	}

	final public void registerUtilityWindow(DockableWindow window) {
		ActionCheckBoxMenuItem miWindowItem = new ActionCheckBoxMenuItem(new ToggleWindowAction(window));
		miWindowItem.addScriptedActionListener(mainWindow.getDefaultActionListener());
		miWindowItem.setSelected(!window.isClosed());
		windowItems.put (window.getID(), miWindowItem);
		mnWindows.add(miWindowItem);
	}

	final public void utilityWindowClosed (int ID) {
		ActionCheckBoxMenuItem mi = windowItems.get(ID);
		if (mi!=null)
			mi.setSelected(false);
	}

	final public void utilityWindowDisplayed (int ID) {
		ActionCheckBoxMenuItem mi = windowItems.get(ID);
		if (mi!=null)
			mi.setSelected(true);
	}
}

