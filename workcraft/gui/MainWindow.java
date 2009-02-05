package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.defaults.StandardBorderManager;
import org.flexdock.docking.drag.effects.EffectsManager;
import org.flexdock.docking.drag.preview.AlphaPreview;
import org.flexdock.docking.event.DockingEvent;
import org.flexdock.docking.event.DockingListener;
import org.flexdock.plaf.common.border.ShadowBorder;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.utils.SubstanceConstants.TabContentPaneBorderKind;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.Framework;
import org.workcraft.framework.ModelFactory;
import org.workcraft.framework.ModelSaveFailedException;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
import org.workcraft.framework.exceptions.PluginInstantiationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.framework.plugins.PluginInfo;
import org.workcraft.framework.workspace.WorkspaceEntry;
import org.workcraft.gui.actions.ScriptedAction;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.edit.graph.GraphEditorPanel;
import org.workcraft.gui.edit.graph.ToolboxWindow;
import org.workcraft.gui.workspace.WorkspaceWindow;

@SuppressWarnings("serial")
public class MainWindow extends JFrame implements DockingConstants{
	public static class Actions {
		public static final ScriptedAction CREATE_WORK_ACTION = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.createWork()";
			}
			public String getText() {
				return "Create work...";
			};
		};
		public static final ScriptedAction OPEN_WORK_ACTION = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.openWork()";
			}
			public String getText() {
				return "Open work...";
			};
		};
		public static final ScriptedAction SAVE_WORK_ACTION = new ScriptedAction() {
			public String getScript() {
				return "mainWindow.save()";
			}
			public String getText() {
				return "Save";
			};
		};
		public static final ScriptedAction EXIT_ACTION = new ScriptedAction() {
			public String getScript() {
				return "framework.shutdown()";
			}
			public String getText() {
				return "Exit";
			};
		};
		public static final ScriptedAction SHUTDOWN_GUI_ACTION = new ScriptedAction() {
			public String getScript() {
				return "framework.shutdownGUI()";
			}
			public String getText() {
				return "Shutdown GUI";
			};
		};
		public static final ScriptedAction RECONFIGURE_PLUGINS_ACTION = new ScriptedAction() {
			public String getScript() {
				return "framework.getPluginManager().reconfigure()";
			}
			public String getText() {
				return "Reconfigure plugins";
			};
		};
	}

	private final ScriptedActionListener defaultActionListener = new ScriptedActionListener() {
		public void actionPerformed(ScriptedAction e) {
			if (e.getScript() == null)
				System.out.println ("Scripted action \"" + e.getText()+"\": null action");
			else
				System.out.println ("Scripted action \"" + e.getText()+"\":\n"+e.getScript());
			
			if (e.getUndoScript() == null)
				System.out.println ("Action cannot be undone.");
			else {
				System.out.println ("Undo script:\n" +e.getUndoScript());
				if (e.getRedoScript() == null)
					System.out.println ("Action cannot be redone.");
				else
					System.out.println ("Redo script:\n"+e.getRedoScript());
			}
			
			if (e.getScript() != null)
				framework.execJavaScript(e.getScript());
		}
	};
	
	private Framework framework;

	WorkspaceWindow workspaceView;
	OutputView outputView;
	ErrorView errorView;
	JavaScriptView jsView;
	PropertyView propertyView;

	ToolboxWindow toolboxView;
	// MDIPane content;



	JPanel content;

	DefaultDockingPort rootDockingPort;
	Dockable outputDockable;
	Dockable lastEditorDockable;

	InternalWindow testDoc;

	GraphEditorPanel editorInFocus;

	private JMenuBar menuBar;

	private String lastSavePath = null;
	private String lastOpenPath = null;

	protected void createViews() {
		workspaceView  = new WorkspaceWindow(framework);
		framework.getWorkspace().addListener(workspaceView);
		workspaceView.setVisible(true);
		propertyView = new PropertyView(framework);

		outputView = new OutputView(framework);
		errorView = new ErrorView(framework);
		jsView = new JavaScriptView(framework);

		toolboxView = new ToolboxWindow(framework);

		lastEditorDockable = null;
		outputDockable = null;
		editorInFocus = null;
	}

	public MainWindow(final Framework framework) {
		super();
		this.framework = framework;

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				framework.shutdown();
			}
		});

	}

	public void setLAF(String laf) {
		if (JOptionPane.showConfirmDialog(this, "Changing Look and Feel requires GUI restart.\n\n" +
				"This will not affect the workspace (i.e. open documents will stay open),\n" +
				"but the visual editor windows will be closed.\n\nProceed?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
		if (laf == null)
			laf = UIManager.getSystemLookAndFeelClassName();

		framework.setConfigVar("gui.lookandfeel", laf);
		framework.restartGUI();
	}

	public WorkspaceWindow getWorkspaceView() {
		return workspaceView;
	}


	protected void attachDockableListener(Dockable dock) {
		dock.addDockingListener( new DockingListener() {

			public void dockingComplete(DockingEvent arg0) {
				for (Object d: arg0.getNewDockingPort().getDockables()) {
					Component comp = ((Dockable)d).getComponent();
					if ( comp instanceof DockableView) {
						DockableView wnd = (DockableView)comp;
						boolean inTab = arg0.getDockable().getComponent().getParent() instanceof JTabbedPane;
						//	System.out.println(inTab);
						wnd.setStandalone(!inTab);
					}
				}

				for (Object d: arg0.getOldDockingPort().getDockables()) {
					Component comp = ((Dockable)d).getComponent();
					if ( comp instanceof DockableView) {
						DockableView wnd = (DockableView)comp;
						boolean inTab = arg0.getDockable().getComponent().getParent() instanceof JTabbedPane;
						System.out.println(inTab);
						wnd.setStandalone(!inTab);
					}
				}
			}


			public void dragStarted(DockingEvent arg0) {
			}


			public void dropStarted(DockingEvent arg0) {
			}


			public void undockingComplete(DockingEvent arg0) {
			}


			public void undockingStarted(DockingEvent arg0) {
			}


			public void dockingCanceled(DockingEvent evt) {
			}
		});

	}

	public Dockable addView(JComponent view, String name, String region, float split) {
		DockableView dock = new DockableView(name, view);
		dock.setFocusable(false);
		Dockable dockable = DockingManager.registerDockable(dock, name);

		rootDockingPort.dock(dockable, region);
		DockingManager.setSplitProportion(dockable, split);

		for (Object d: dockable.getDockingPort().getDockables()) {
			Component comp = ((Dockable)d).getComponent();
			if ( comp instanceof DockableView) {
				DockableView wnd = (DockableView)comp;
				boolean inTab = comp.getParent() instanceof JTabbedPane;
				//	System.out.println(inTab);
				wnd.setStandalone(!inTab);
			}
		}

		attachDockableListener(dockable);
		return dockable;
	}

	public Dockable addView(JComponent view, String name, Dockable neighbour) {
		DockableView dock = new DockableView(name, view);
		dock.setFocusable(false);
		Dockable dockable = DockingManager.registerDockable(dock, name);

		neighbour.dock(dockable, DockingManager.CENTER_REGION);

		for (Object d: dockable.getDockingPort().getDockables()) {
			Component comp = ((Dockable)d).getComponent();
			if ( comp instanceof DockableView) {
				DockableView wnd = (DockableView)comp;
				boolean inTab = comp.getParent() instanceof JTabbedPane;
				//	System.out.println(inTab);
				wnd.setStandalone(!inTab);
			}
		}

		attachDockableListener(dockable);
		return dockable;

	}

	public Dockable addView(JComponent view, String name, Dockable neighbour, String relativeRegion, float split) {
		DockableView dock = new DockableView(name, view);
		dock.setFocusable(false);
		Dockable dockable = DockingManager.registerDockable(dock, name);

		//attachDockableListener(dock);
		DockingManager.dock(dockable, neighbour, relativeRegion);
		DockingManager.setSplitProportion(dockable, split);

		for (Object d: neighbour.getDockingPort().getDockables()) {
			Component comp = ((Dockable)d).getComponent();
			if ( comp instanceof DockableView) {
				DockableView wnd = (DockableView)comp;
				boolean inTab = comp.getParent() instanceof JTabbedPane;
				//		System.out.println(inTab);
				wnd.setStandalone(!inTab);
			}
		}

		attachDockableListener(dockable);
		return dockable;
	}

	public void addEditorView(WorkspaceEntry we) {
		if (we.getModel() == null) {
			JOptionPane.showMessageDialog(this, "The selected entry is not a Workcraft model, and cannot be edited.", "Cannot open editor", JOptionPane.ERROR_MESSAGE);
			return;
		}

		VisualModel visualModel = we.getModel().getVisualModel();

		if (visualModel == null)
			if (JOptionPane.showConfirmDialog(this, "The selected model does not have visual layout information. Do you want to create a default layout?",
					"No layout information", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
				try {
					visualModel = ModelFactory.createVisualModel(we.getModel().getMathModel());
					we.setModel(visualModel);

				} catch (VisualModelInstantiationException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "Error creating visual model", JOptionPane.ERROR_MESSAGE);
					return;
				}

				GraphEditorPanel editor = new GraphEditorPanel(this, we);
				String dockableTitle = we.getTitle() + " - " + visualModel.getDisplayName();
				Dockable dockable;

				if (lastEditorDockable == null)
					dockable = addView (editor, dockableTitle, outputDockable, NORTH_REGION, 0.8f);
				else
					dockable = addView (editor, dockableTitle, lastEditorDockable);

				lastEditorDockable = dockable;

				requestFocus(editor);
	}

	public void startup() {
		JDialog.setDefaultLookAndFeelDecorated(true);
		UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL);
		//SwingUtilities.updateComponentTreeUI(MainWindow.this);

		String laf = framework.getConfigVar("gui.lookandfeel");
		if (laf == null)
			laf = UIManager.getCrossPlatformLookAndFeelClassName();
		LAF.setLAF(laf);

		content = new JPanel(new BorderLayout(0,0));

		rootDockingPort = new DefaultDockingPort();
		// rootDockingPort.setBorderManager(new StandardBorderManager(new ShadowBorder()));
		//this.rootDockingPort.setSingleTabAllowed(true);
		content.add(rootDockingPort, BorderLayout.CENTER);

		setContentPane(content);

		boolean maximised = Boolean.parseBoolean(framework.getConfigVar("gui.main.maximised"));
		String w = framework.getConfigVar("gui.main.width");
		String h = framework.getConfigVar("gui.main.height");
		int width = (w==null)?800:Integer.parseInt(w);
		int height = (h==null)?600:Integer.parseInt(h);

		lastSavePath = framework.getConfigVar("gui.main.lastSavePath");
		lastOpenPath = framework.getConfigVar("gui.main.lastOpenPath");

		this.setSize(width, height);

		if (maximised)
			setExtendedState(MAXIMIZED_BOTH);

		menuBar = new MainMenu(this);
		setJMenuBar(menuBar);

		setTitle("Workcraft " + Framework.FRAMEWORK_VERSION_MAJOR+"."+Framework.FRAMEWORK_VERSION_MINOR);

		createViews();

		outputView.captureStream();
		errorView.captureStream();

		rootDockingPort.setBorderManager(new StandardBorderManager(new ShadowBorder()));

		outputDockable = addView (outputView, "Output", DockingManager.SOUTH_REGION, 0.8f);
		addView (errorView, "Problems", outputDockable);
		addView (jsView, "JavaScript", outputDockable);

		Dockable wsvd = addView (workspaceView, "Workspace", DockingManager.EAST_REGION, 0.8f);
		addView (propertyView, "Property Editor", wsvd, DockingManager.NORTH_REGION, 0.5f);
		addView (toolboxView, "Editor Tools", wsvd, DockingManager.NORTH_REGION, 0.5f);


		DockingManager.display(outputDockable);
		DockingManager.setFloatingEnabled(true);

		DockingManager.setFloatingEnabled(true);

		EffectsManager.setPreview(new AlphaPreview(Color.BLACK, Color.GRAY, 0.5f));

		workspaceView.startup();

		setVisible(true);

	}

	public ScriptedActionListener getDefaultActionListener() {
		return defaultActionListener;
	}

	public void shutdown() {
		framework.setConfigVar("gui.main.maximised", Boolean.toString((getExtendedState() & JFrame.MAXIMIZED_BOTH)!=0) );
		framework.setConfigVar("gui.main.width", Integer.toString(getWidth()));
		framework.setConfigVar("gui.main.height", Integer.toString(getHeight()));

		if (lastSavePath != null)
			framework.setConfigVar("gui.main.lastSavePath", lastSavePath);
		if (lastOpenPath != null)
			framework.setConfigVar("gui.main.lastOpenPath", lastOpenPath);

		outputView.releaseStream();
		errorView.releaseStream();

		workspaceView.shutdown();

		setVisible(false);
	}

	public void createWork() {
		CreateWorkDialog dialog = new CreateWorkDialog(MainWindow.this);
		dialog.setVisible(true);
		if (dialog.getModalResult() == 1) {
			PluginInfo info = dialog.getSelectedModel();
			try {
				MathModel mathModel = (MathModel)framework.getPluginManager().getInstance(info, MathModel.class);

				if (!dialog.getModelTitle().isEmpty())
					mathModel.setTitle(dialog.getModelTitle());

				if (dialog.createVisualSelected()) {
					VisualModel visualModel = ModelFactory.createVisualModel(mathModel);
					WorkspaceEntry we = framework.getWorkspace().add(visualModel);
					if (dialog.openInEditorSelected())
						addEditorView (we);
					//rootDockingPort.dock(new GraphEditorPane(visualModel), CENTER_REGION);
					//addView(new GraphEditorPane(visualModel), mathModel.getTitle() + " - " + mathModel.getDisplayName(), DockingManager.NORTH_REGION, 0.8f);
				} else
					framework.getWorkspace().add(mathModel);
			} catch (PluginInstantiationException e) {
				System.err.println(e.getMessage());
			} catch (VisualModelInstantiationException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	public void requestFocus (GraphEditorPanel sender) {
		if (editorInFocus == sender)
			return;

		editorInFocus = sender;

		toolboxView.setToolsForModel(editorInFocus.getModel());
		framework.deleteJavaScriptProperty("visualModel", framework.getJavaScriptGlobalScope());
		framework.setJavaScriptProperty("visualModel", sender.getModel(), framework.getJavaScriptGlobalScope(), true);
		
		framework.deleteJavaScriptProperty("model", framework.getJavaScriptGlobalScope());
		framework.setJavaScriptProperty("model", sender.getModel().getMathModel(), framework.getJavaScriptGlobalScope(), true);
		
		editorInFocus.requestFocusInWindow();
	}

	public ToolboxWindow getToolboxWindow() {
		return toolboxView;
	}

	public void openWork() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.OPEN_DIALOG);

		if (lastOpenPath != null)
			fc.setCurrentDirectory(new File(lastOpenPath));

		fc.setFileFilter(FileFilters.DOCUMENT_FILES);
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Open work file(s))");

		if (fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
			for (File f : fc.getSelectedFiles()) {
				try {
					WorkspaceEntry we = framework.getWorkspace().add(f.getPath());
					if (we.getModel() instanceof VisualModel)
						addEditorView(we);
				} catch (ModelLoadFailedException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot load " + f.getPath() , JOptionPane.ERROR_MESSAGE);
				}
			}
			lastOpenPath = fc.getCurrentDirectory().getPath();
		}
	}

	public void save(WorkspaceEntry we) {
		if (we.getFile() == null) {
			saveAs(we);
			return;
		}

		try {
			framework.save(we.getModel(), we.getFile().getPath());
			we.setUnsaved(false);
			lastSavePath = we.getFile().getParent();	

		} catch (ModelSaveFailedException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static String removeSpecialFileNameCharacters(String fileName)
	{
		return fileName
		.replace('\\', '_')
		.replace('/', '_')
		.replace(':', '_')
		.replace('"', '_')
		.replace('<', '_')
		.replace('>', '_')
		.replace('|', '_');
	}

	public void saveAs(WorkspaceEntry we) {
		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.SAVE_DIALOG);

		String title = we.getModel().getTitle();
		title = removeSpecialFileNameCharacters(title);

		fc.setSelectedFile(new File(title));
		fc.setFileFilter(FileFilters.DOCUMENT_FILES);

		if (lastSavePath != null)
			fc.setCurrentDirectory(new File(lastSavePath));

		if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			if (!fc.getSelectedFile().exists())
				if (!path.endsWith(FileFilters.DOCUMENT_EXTENSION))
					path += FileFilters.DOCUMENT_EXTENSION;

			File f = new File(path);
			if (f.exists())
				if (JOptionPane.showConfirmDialog(this, "The file \"" + f.getName()+"\" already exists. Do you want to overwrite it?", "Confirm", JOptionPane.YES_NO_OPTION)==JOptionPane.NO_OPTION)
					return;
			try {
				framework.save(we.getModel(), path);
				we.setFile(fc.getSelectedFile());
				we.setUnsaved(false);
				lastSavePath = fc.getCurrentDirectory().getPath();
			} catch (ModelSaveFailedException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public GraphEditorPanel getCurrentEditor() {
		return editorInFocus;
	}

	public void repaintCurrentEditor() {
		if (editorInFocus != null)
			editorInFocus.repaint();
	}

	public void togglePropertyEditor() {

	}

	public PropertyView getPropertyView() {
		return propertyView;
	}

	public Framework getFramework() {
		return framework;
	}

}