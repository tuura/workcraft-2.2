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
package org.workcraft.plugins.cpog.optimisation.tests;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.CleverCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.CpogSolver;
import org.workcraft.plugins.cpog.optimisation.DefaultCpogSolver;
import org.workcraft.plugins.cpog.optimisation.OneHotIntBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.OneHotNumberProvider;
import org.workcraft.plugins.cpog.optimisation.Optimiser;

public class OneHotSolver_CleverCnfGenerator_Tests extends SolverTests {
	public OneHotSolver_CleverCnfGenerator_Tests()
	{
	}

	protected CpogSolver createSolver() 
	{
		return new DefaultCpogSolver<BooleanFormula>(new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider()), new CleverCnfGenerator());
	}
	@Override
	protected CpogSolver createSolver(int[] levels) {
		return new DefaultCpogSolver<BooleanFormula>(new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider(), levels), new CleverCnfGenerator());
	}
}
