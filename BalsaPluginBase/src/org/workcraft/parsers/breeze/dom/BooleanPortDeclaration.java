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
package org.workcraft.parsers.breeze.dom;

import org.workcraft.parsers.breeze.expressions.Expression;

public class BooleanPortDeclaration extends PortDeclaration 
{
	private final boolean isInput;
	private final Expression<Integer> width;

	public BooleanPortDeclaration(String name, boolean isActive, boolean isInput, Expression<Integer> width) {
		super(name, isActive);
		this.isInput = isInput;
		this.width = width;
	}

	public boolean isInput() {
		return isInput;
	}

	public Expression<Integer> getWidth() {
		return width;
	}

	@Override public <T> T accept(PortVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
