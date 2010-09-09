package org.workcraft.plugins.interop;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.plugins.shared.MpsatChainResultHandler;
import org.workcraft.plugins.shared.MpsatMode;
import org.workcraft.plugins.shared.MpsatSettings;
import org.workcraft.plugins.shared.MpsatSettings.SolutionMode;
import org.workcraft.plugins.shared.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

@DisplayName("Resolve CSC conflicts")
public class CscResolutionTool implements Tool {

	private final Framework framework;

	public CscResolutionTool(Framework framework)
	{
		this.framework = framework;
	}
	
	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public String getSection() {
		return "Encoding conflicts";
	}

	@Override
	public void run(WorkspaceEntry we) {
		
		MpsatSettings settings = new MpsatSettings(MpsatMode.RESOLVE_ENCODING_CONFLICTS, 4, MpsatSettings.SOLVER_MINISAT, SolutionMode.MINIMUM_COST, 1, null);
		MpsatChainTask mpsatTask = new MpsatChainTask(we, settings, framework);
		
		framework.getTaskManager().queue(mpsatTask, "CSC conflicts resolution", new MpsatChainResultHandler(mpsatTask));
	}
}
