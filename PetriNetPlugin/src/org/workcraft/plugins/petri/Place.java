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

package org.workcraft.plugins.petri;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.Variable;
import org.workcraft.dom.math.MathNode;

@VisualClass("org.workcraft.plugins.petri.VisualPlace")
public class Place extends MathNode {
	protected Variable<Integer> tokens = new Variable<Integer>(0);
	
	public ModifiableExpression<Integer> tokens() {
		return tokens;
	}
	
	public int getTokens() {
		return GlobalCache.eval(tokens());
	}

	public void setTokens(int i) {
		GlobalCache.setValue(tokens(), i);
	}
}