package org.workcraft.plugins.cpog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Encoding
{
	private Map<Variable, VariableState> states = new HashMap<Variable, VariableState>();

	public Map<Variable, VariableState> getStates()
	{
		return Collections.unmodifiableMap(states);
	}

	public void setState(Variable variable, VariableState state)
	{
		states.put(variable, state);
	}
	
	public String toString()
	{
		String result = "";
		Set<Variable> sortedVariables = new TreeSet<Variable>(states.keySet()); 
		for(Variable var : sortedVariables) result += getState(var).toString();
		return result;
	}	

	public void updateEncoding(String s)
	{
		int k = 0;
		Set<Variable> sortedVariables = new TreeSet<Variable>(states.keySet()); 
		for(Variable var : sortedVariables) states.put(var, VariableState.fromChar(s.charAt(k++)));
	}

	public VariableState getState(Variable var)
	{
		VariableState res = states.get(var);
		if (res == null) res = VariableState.UNDEFINED;
		return res; 
	}	
}
