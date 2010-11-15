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

package org.workcraft.dom;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.Variable;

public class DefaultGroupImpl extends AbstractGroup implements Container {
	Variable<LinkedHashSet<Node>> children = new Variable<LinkedHashSet<Node>>(new LinkedHashSet<Node>());
	
	public DefaultGroupImpl (Container groupRef) {
		super(groupRef);
	}

	public Collection<Node> getChildren() {
		return Collections.unmodifiableCollection(children.getValue());
	}

	@Override
	protected void addInternal(Node node) {
		LinkedHashSet<Node> newValue = new LinkedHashSet<Node>(getChildren());
		newValue.add(node);
		children.setValue(newValue);
	}

	@Override
	protected void removeInternal(Node node) {
		LinkedHashSet<Node> newValue = new LinkedHashSet<Node>(getChildren());
		if(newValue.remove(node))
			children.setValue(newValue);
	}

	@Override
	public Expression<? extends Collection<Node>> children() {
		return children;
	}

}
