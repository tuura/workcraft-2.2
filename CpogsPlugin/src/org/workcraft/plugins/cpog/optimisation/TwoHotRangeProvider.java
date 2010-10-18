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

import static org.workcraft.plugins.cpog.optimisation.CnfOperations.not;
import static org.workcraft.plugins.cpog.optimisation.CnfOperations.or;

import java.util.ArrayList;
import java.util.List;

public class TwoHotRangeProvider
{
	private Cnf constraints = new Cnf();

	public Cnf getConstraints()
	{
		return constraints;
	}
	
	public TwoHotRange generate(String name, int range)
	{
		if(range<2)
			throw new RuntimeException("can't select 2 hot out of "+range);
		
		List<Literal> literals = createLiterals(name+"_sel", range);
		List<Literal> sort1 = createLiterals(name+"_sorta_", range);
		List<Literal> thermo = createLiterals(name+"_t_", range);
		List<Literal> sort2 = createLiterals(name+"_sortb_", range);
		
		constraints.add(CnfSorter.sortRound(sort1, thermo, literals));
		constraints.add(CnfSorter.sortRound(sort2, sort1));
		
		for(int i=0;i<range-2;i++)
			constraints.add(or(not(sort2.get(i))));

		for(int i=0;i<range-2;i+=2)
		{
			constraints.add(or(not(literals.get(i)), not(literals.get(i+1))));
		}

		constraints.add(or(sort2.get(range-1)));
		constraints.add(or(sort2.get(range-2)));
		
		return new TwoHotRange(literals, thermo);
	}

	private List<Literal> createLiterals(String name, int range) {
		List<Literal> literals = new ArrayList<Literal>();
		
		for(int i=0;i<range;i++)
			literals.add(new Literal(name+i));
		return literals;
	}
	

	public static List<CnfClause> selectAnd(Literal result, Literal[] vars, TwoHotRange code) {
		List<CnfClause> conditions = new ArrayList<CnfClause>();
		
		if(code.size() != vars.length)
			throw new RuntimeException("Lengths do not match: code="+code.size()+", vars="+vars.length);

		List<Literal> preResult = new ArrayList<Literal>();
		for(int i=0;i<vars.length;i++)
			preResult.add(new Literal(result.getVariable().getLabel() + (result.getNegation()?"i":"")+ "_sv"+i));
		
		for(int i=0;i<vars.length;i++)
		{
			Literal res = preResult.get(i);
			Literal sel = code.get(i);
			Literal var = vars[i];
			conditions.add(or(not(res), not(sel), var));
			conditions.add(or(res, sel));
			conditions.add(or(res, not(var)));

			conditions.add(or(not(result), res));
		}
		CnfClause resTrue = new CnfClause();
		resTrue.add(result);
		for(int i=0;i<vars.length;i++)
			resTrue.add(not(preResult.get(i)));
		conditions.add(resTrue);
		
		return conditions;
	}
	
	public static BooleanFormula selectAnd(BooleanFormula[] vars, TwoHotRange number) {
		throw new RuntimeException("incorrect");
		
		/*List<FreeVariable> params = new ArrayList<FreeVariable>();
		CnfLiteral []literals = new CnfLiteral[vars.length];
		
		for(int i=0;i<vars.length;i++)
		{
			FreeVariable var = new FreeVariable("param"+i);
			params.add(var);
			literals[i] = new CnfLiteral(var);
		}
		
		List<CnfClause> result = selectAnd(CnfLiteral.One, literals, number);
		
		Cnf cnf = new Cnf(result);
		BooleanFormula res = BooleanReplacer.replace(cnf, params, Arrays.asList(vars));
		return res;*/
	}
}
