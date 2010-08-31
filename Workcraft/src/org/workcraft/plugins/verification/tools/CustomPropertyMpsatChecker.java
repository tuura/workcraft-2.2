package org.workcraft.plugins.verification.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Model;
import org.workcraft.plugins.shared.MpsatChainResultHandler;
import org.workcraft.plugins.shared.MpsatPresetManager;
import org.workcraft.plugins.shared.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.verification.gui.MpsatConfigurationDialog;
import org.workcraft.util.GUI;

@DisplayName("Check custom property (punf, MPSat)")
public class CustomPropertyMpsatChecker implements Tool {

	@Override
	public String getSection() {
		return "Verification";
	}

	@Override
	public boolean isApplicableTo(Model model) {
		if (model instanceof STG)
			return true;
		else
			return false;
	}

	@Override
	public void run(Model model, Framework framework) {
		MpsatPresetManager pmgr = new MpsatPresetManager();
		MpsatConfigurationDialog dialog = new MpsatConfigurationDialog(framework.getMainWindow(), pmgr);
		GUI.centerAndSizeToParent(dialog, framework.getMainWindow());
		dialog.setVisible(true);
		if (dialog.getModalResult() == 1)
		{
			framework.getTaskManager().queue(new MpsatChainTask(model, dialog.getSettings(), framework), "MPSat tool chain", 
					new MpsatChainResultHandler(framework));
		}
	}
}
