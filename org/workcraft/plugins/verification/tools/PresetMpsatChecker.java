package org.workcraft.plugins.verification.tools;

import org.workcraft.plugins.verification.MpsatPreset;
import org.workcraft.plugins.verification.MpsatPresetManager;
import org.workcraft.plugins.verification.MpsatSettings;

public abstract class PresetMpsatChecker extends AbstractMpsatChecker {
	protected abstract String getPresetName();
	
	@Override
	protected MpsatSettings getSettings() {
		MpsatPresetManager pmgr = new MpsatPresetManager();
		String presetName = getPresetName();
		MpsatPreset preset = pmgr.find(presetName);
		
		if (preset == null)
			throw new RuntimeException ("Built-in MPSat preset \"" + presetName + "\" not found.");
		
		return preset.getSettings();
	}
}
