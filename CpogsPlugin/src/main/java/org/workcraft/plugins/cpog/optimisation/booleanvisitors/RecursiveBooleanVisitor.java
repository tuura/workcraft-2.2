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
package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import org.workcraft.plugins.cpog.optimisation.BinaryBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;


public class RecursiveBooleanVisitor<T> implements BooleanVisitor<T> 
{
	protected T visitDefault(BooleanFormula node)
	{
		return null;
	}
	
	protected T visitBinaryAfterSubnodes(BinaryBooleanFormula node, T x, T y) {
		return visitDefault(node);
	}
	
	protected T visitBinary(BinaryBooleanFormula node) {
		return visitBinaryAfterSubnodes(node, node.getX().accept(this), node.getY().accept(this));
	}
	
	@Override
	public T visit(And node) {
		return visitBinary(node);
	}

	@Override
	public T visit(Iff node) {
		return visitBinary(node);
	}

	@Override
	public T visit(Zero node) {
		return visitDefault(node);
	}

	@Override
	public T visit(One node) {
		return visitDefault(node);
	}

	@Override
	public T visit(Not node) {
		node.getX().accept(this);
		return visitDefault(node);
	}

	@Override
	public T visit(Imply node) {
		return visitBinary(node);
	}

	@Override
	public T visit(BooleanVariable node) {
		return visitDefault(node);
	}

	@Override
	public T visit(Or node) {
		return visitBinary(node);
	}

	@Override
	public T visit(Xor node) {
		return visitBinary(node);
	}
}
