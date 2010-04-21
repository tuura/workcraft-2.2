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
package org.workcraft.plugins.cpog.optimisation.expressions;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;

public class CleverBooleanWorker implements BooleanWorker 
{
	private static final BooleanFormula ZERO = Zero.instance();
	private static final BooleanFormula ONE = One.instance();

	@Override
	public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
		if(x==y)
			return x;
		if(x == ZERO || y == ZERO)
			return ZERO;
		if(x == ONE)
			return y;
		if(y == ONE)
			return x;
		return new And(x,y);
	}

	@Override
	public BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
		if(x==y)
			return x;
		if(x == ONE)
			return y;
		if(x == ZERO)
			return not(y);
		if(y == ONE)
			return x;
		if(y == ZERO)
			return not(x);
		return new Iff(x,y);
	}

	@Override
	public BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
		if(x==y)
			return ONE;
		if(x == ZERO || y == ONE)
			return ONE;
		if(x == ONE)
			return y;
		if(y == ZERO)
			return not(x);
		return new Imply(x,y);
	}

	@Override
	public BooleanFormula not(BooleanFormula x) {
		if(x == ONE)
			return ZERO;
		if(x == ZERO)
			return ONE;
		return new Not(x);
	}

	@Override
	public BooleanFormula one() {
		return One.instance();
	}

	@Override
	public BooleanFormula or(BooleanFormula x, BooleanFormula y) {
		if(x==y)
			return x;
		if(x == ONE || y == ONE)
			return ONE;
		if(x == ZERO)
			return y;
		if(y == ZERO)
			return x;
		return new Or(x,y);
	}

	@Override
	public BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
		if(x==y)
			return x;
		if(x == ONE)
			return not(y);
		if(x == ZERO)
			return y;
		if(y == ONE)
			return not(x);
		if(y == ZERO)
			return x;
		return new Xor(x,y);
	}

	@Override
	public BooleanFormula zero() {
		return Zero.instance();
	}
}
