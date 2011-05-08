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

package org.workcraft.parsers.breeze.expressions;

import org.workcraft.parsers.breeze.ParameterScope;
import org.workcraft.parsers.breeze.expressions.visitors.Visitor;

public class Constant<T> implements Expression<T> {
	private final T value;
	
	public Constant (T value) {
		this.value = value;
	}

	@Override
	public T evaluate(ParameterScope parameters) {
		return getValue();
	}
	
	public String toString() {
		return getValue().toString();
	}
	
	@Override
	public <R> R accept(Visitor<R> visitor) {
		return visitor.visit(this);
	}

	public T getValue() {
		return value;
	}

	public static <T> Expression<T> create(T value) {
		return new Constant<T>(value);
	}

}
