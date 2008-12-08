package org.workcraft.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MainMenu extends JMenuBar {
	private static final long serialVersionUID = 1L;

	private String[] lafCaptions = new String[] {
			"Java default",
			"Substance: Moderate",
			"Substance: Mist Silver",
			"Substance: Raven",
			"Substance: Business",
			"Substance: Creme"
	};
	private String[] lafClasses = new String[] {
			"javax.swing.plaf.metal.MetalLookAndFeel",
			"org.jvnet.substance.skin.SubstanceModerateLookAndFeel",
			"org.jvnet.substance.skin.SubstanceMistSilverLookAndFeel",
			"org.jvnet.substance.skin.SubstanceRavenLookAndFeel",
			"org.jvnet.substance.skin.SubstanceBusinessLookAndFeel",
			"org.jvnet.substance.skin.SubstanceCremeCoffeeLookAndFeel"
	};

	MainMenu(final MainWindow frame) {
		// File
		JMenu mnFile = new JMenu();
		mnFile.setText("File");

		JMenuItem miNewModel = new JMenuItem();
		miNewModel.setText("New work...");
		miNewModel.setMnemonic(KeyEvent.VK_N);
		miNewModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		miNewModel.addActionListener(frame.getDefaultActionListener());
		miNewModel.setActionCommand("gui.createWork()");

		JMenuItem miExit = new JMenuItem();
		miExit.setText("Exit");
		//smiExit.setMnemonic(KeyEvent.VK_O);
		miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		miExit.addActionListener(frame.getDefaultActionListener());
		miExit.setActionCommand("shutdown()");

		JMenuItem miShutdownGUI = new JMenuItem();
		miShutdownGUI.setText("Shutdown GUI");
		miShutdownGUI.addActionListener(frame.getDefaultActionListener());
		miShutdownGUI.setActionCommand("shutdowngui()");

		JMenuItem miOpenModel = new JMenuItem();
		miOpenModel.setText("Open work...");
		miOpenModel.setMnemonic(KeyEvent.VK_O);
		miOpenModel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		miOpenModel.addActionListener(frame.getDefaultActionListener());
		miOpenModel.setActionCommand("gui.openWork()");

		
		JMenuItem miSaveWorkspace = new JMenuItem();
		miSaveWorkspace.setText("Open work...");
		miSaveWorkspace.setMnemonic(KeyEvent.VK_O);
		miSaveWorkspace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		miSaveWorkspace.addActionListener(frame.getDefaultActionListener());
		miSaveWorkspace.setActionCommand("gui.openWork()");
		
		mnFile.add(miNewModel);
		mnFile.add(miOpenModel);

		mnFile.addSeparator();
		mnFile.add(miShutdownGUI);
		mnFile.add(miExit);

		// Preferences
		JMenu mnPreferences = new JMenu();
		mnPreferences.setText("Preferences");

		JMenu mnLAF = new JMenu();
		mnLAF.setText("Look and Feel");

		for(int i=0; i<this.lafClasses.length; i++) {
			JMenuItem miLAFItem = new JMenuItem();
			miLAFItem.setText(this.lafCaptions[i]);
			final String lafClass = this.lafClasses[i];
			miLAFItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.setLAF(lafClass);
				}
			});
			mnLAF.add(miLAFItem);
		}

		JMenuItem miReconfigure = new JMenuItem("Reconfigure plugins");
		miReconfigure.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				frame.framework.getPluginManager().reconfigure();
			}
		});

		mnPreferences.add(mnLAF);
		mnPreferences.addSeparator();
		mnPreferences.add(miReconfigure);

		add(mnFile);
		add(mnPreferences);
	}

}
