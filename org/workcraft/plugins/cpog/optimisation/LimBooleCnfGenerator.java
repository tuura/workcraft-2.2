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
package org.workcraft.plugins.cpog.optimisation;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.RecursiveBooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;


public class LimBooleCnfGenerator implements RawCnfGenerator<BooleanFormula> {

	private static final String limboolePath = "C:\\Cygwin\\bin\\limboole.exe";
	
	class VariableCollector extends RecursiveBooleanVisitor<Object>
	{
		private final Map<String, BooleanVariable> vars = new HashMap<String, BooleanVariable>();
		@Override
		public Object visit(BooleanVariable variable) {
			vars.put(variable.getLabel(), variable);
			return null;
		}
		public Map<String, BooleanVariable> getVars() {
			return vars;
		}
	}
	
	@Override
	public CnfTask getCnf(BooleanFormula formula) 
	{
		VariableCollector collector = new VariableCollector();
		formula.accept(collector);
		Map<String, BooleanVariable> vars = collector.getVars();

		return new CnfTask(ProcessIO.runViaStreams(FormulaToString.toString(BooleanOperations.not(formula))+"|0|!1", new String[]{limboolePath, "-d"}), vars);
	}

}
