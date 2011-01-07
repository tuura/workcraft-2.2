package org.workcraft.plugins.shared.gui;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.shared.presets.Preset;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.plugins.shared.presets.SettingsToControlsMapper;
import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class PresetManagerPanel<T> extends JPanel {
	private JComboBox presetCombo;
	private JButton manageButton;

	private final PresetManager<T> presetManager;
	private final SettingsToControlsMapper<T> guiMapper;
	private JButton updatePresetButton;
	private JButton saveAsNewButton;
	private Window dialogOwner;

	@SuppressWarnings("unchecked")
	public PresetManagerPanel(PresetManager<T> presetManager_, List<Preset<T>> builtIn, SettingsToControlsMapper<T> guiMapper_, Window dialogOwner) {
		super();

		this.guiMapper = guiMapper_;
		this.presetManager = presetManager_;
		this.dialogOwner = dialogOwner;

		setLayout(new SimpleFlowLayout(15, 3));

		for (Preset<T> p : builtIn)
			presetManager.add(p);
		
		presetManager.sort();

		presetCombo = new JComboBox();
		for (Preset<T> p : presetManager.list())
			presetCombo.addItem(p);
		
		presetCombo.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Preset<T> p = (Preset<T>)presetCombo.getSelectedItem();
				
				if (p == null)
					return;
				
				if (p.isBuiltIn()) {
					updatePresetButton.setEnabled(false);
					updatePresetButton.setToolTipText("Cannot make changes to a built-in preset");
				}
				else {
					updatePresetButton.setEnabled(true);
					updatePresetButton.setToolTipText("Save these settings to the currently selected preset");
				}
				
				T settings = p.getSettings();
				guiMapper.applySettingsToControls(settings);
			}
		});

		manageButton = new JButton ("Manage presets...");
		manageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean haveCustomPresets = false;
				for (Preset<T> p : presetManager.list())
					if (!p.isBuiltIn()) {
						haveCustomPresets = true;
						break;
					}
				if (haveCustomPresets)
					managePresets();
				else
					JOptionPane.showMessageDialog(PresetManagerPanel.this, "There are no custom presets to manage.");

			}
		});


		updatePresetButton = new JButton ("Update preset");
		updatePresetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Preset<T> selected = (Preset<T>)presetCombo.getSelectedItem();
				presetManager.update(selected, guiMapper.getSettingsFromControls());
			}
		});

		saveAsNewButton = new JButton ("Save settings as new preset...");
		saveAsNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createPreset();
			}
		});

		add(GUI.createLabeledComponent(presetCombo, "Preset:"));
		add(new SimpleFlowLayout.LineBreak(3));
		add(updatePresetButton);
		add(saveAsNewButton);
		add(manageButton);
	}

	@SuppressWarnings("unchecked")
	public void managePresets()
	{	
		Preset<T> selected = (Preset<T>)presetCombo.getSelectedItem();

		PresetManagerDialog<T> dlg = new PresetManagerDialog<T>(dialogOwner, presetManager);
		dlg.setModalityType(ModalityType.APPLICATION_MODAL);
		GUI.centerAndSizeToParent(dlg, dialogOwner);
		dlg.setVisible(true);

		presetCombo.removeAllItems();
		List<Preset<T>> presets = presetManager.list();

		boolean haveOldSelection = false;

		for (Preset<T> p : presets) {
			presetCombo.addItem(p);
			if (p == selected)
				haveOldSelection = true;
		}

		if (haveOldSelection)
			presetCombo.setSelectedItem(selected);
		else
			presetCombo.setSelectedIndex(0);
	}

	public void createPreset()
	{		
		String desc = JOptionPane.showInputDialog(dialogOwner, "Please enter the description of the new preset:");

		if (! (desc == null || desc.isEmpty())) {
			T settings = guiMapper.getSettingsFromControls();
			Preset<T> preset = presetManager.save(settings, desc);
			presetCombo.addItem(preset);
			presetCombo.setSelectedItem(preset);
		}
	}
	
	public void selectFirst()
	{

		if (presetCombo.getItemCount()>0)
			presetCombo.setSelectedIndex(0);		
	}
}
