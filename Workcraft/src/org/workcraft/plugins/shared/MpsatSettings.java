/**
 * 
 */
package org.workcraft.plugins.shared;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.util.FileUtils;

public class MpsatSettings {
	public static final int SOLVER_ZCHAFF = 0;
	public static final int SOLVER_MINISAT = 1;

	public enum SolutionMode {
		FIRST,
		ALL,
		MINIMUM_COST		
	}

	private MpsatMode mode = MpsatMode.DEADLOCK;
	private int verbosity = 0;
	private int satSolver = 0;
	private SolutionMode solutionMode = SolutionMode.FIRST;
	private int solutionNumberLimit = 0;
	private String reach = "";

	public MpsatSettings(MpsatMode mode, int verbosity, int satSolver,
			SolutionMode solutionMode, int solutionNumberLimit, String reach) {
		super();
		this.mode = mode;
		this.verbosity = verbosity;
		this.satSolver = satSolver;
		this.solutionMode = solutionMode;
		this.solutionNumberLimit = solutionNumberLimit;
		this.reach = reach;
	}
	
	public MpsatMode getMode() {
		return mode;
	}

	public int getVerbosity() {
		return verbosity;
	}

	public int getSatSolver() {
		return satSolver;
	}

	public String getReach() {
		return reach;
	}
	
	public SolutionMode getSolutionMode() {
		return solutionMode;
	}

	public int getSolutionNumberLimit() {
		return solutionNumberLimit;
	}

	public String[] getMpsatArguments() {
		ArrayList<String> args = new ArrayList<String>();
		args.add(getMode().getArgument());
		
		if (getMode().isReach())
			try {
				File reach = File.createTempFile("reach", null);
				reach.deleteOnExit();
				FileUtils.dumpString(reach, getReach());
				args.add("-d");
				args.add("@"+reach.getCanonicalPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		args.add(String.format("-v%d", getVerbosity()));
		args.add(String.format("-$%d", getSatSolver()));

		switch (getSolutionMode()) {
		case FIRST:
			break;
		case MINIMUM_COST:
			args.add("-f");
			break;
		case ALL:
			int solutionNumberLimit = getSolutionNumberLimit();
			if (solutionNumberLimit>0)
				args.add("-a" + Integer.toString(solutionNumberLimit));
			else
				args.add("-a");
		}

		return args.toArray(new String[args.size()]);
	}
}