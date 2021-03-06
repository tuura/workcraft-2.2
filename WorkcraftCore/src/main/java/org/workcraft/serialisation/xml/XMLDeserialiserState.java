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

/**
 * 
 */
package org.workcraft.serialisation.xml;

import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.References;
import org.workcraft.util.ListMap;
import org.workcraft.util.GeneralTwoWayMap;
import org.workcraft.util.TwoWayMap;

class XMLDeserialiserState implements References {
	private final ReferenceResolver externalReferences;
	
	GeneralTwoWayMap<String, Object> internalReferenceMap = new TwoWayMap<String, Object>();
	HashMap<Object, Element> instanceElements = new HashMap<Object, Element>();
	ListMap<Container, Node> children = new ListMap<Container, Node>();
	
	public final StorageManager storage;
	
	public XMLDeserialiserState(ReferenceResolver externalReferences, StorageManager storage) {
		this.externalReferences = externalReferences;
		this.storage = storage;
	}
	
	public void addChildNode (Container parent, Node child) {
		children.put(parent, child);
	}
	
	public List<Node> getChildren(Container parent) {
		return children.get(parent);
	}
	
	public void setInstanceElement (Object instance, Element element) {
		instanceElements.put(instance, element);
	}
	
	public Element getInstanceElement (Object instance) {
		return instanceElements.get(instance);
	}

	public void setObject (String reference, Object obj) {
		internalReferenceMap.put(reference, obj);
	}

	public Object getObject(String reference) {
		return internalReferenceMap.getValue(reference);
	}

	@Override
	public String getReference(Object obj) {
		return internalReferenceMap.getKey(obj);
	}

	public References getInternalReferences() {
		return this;
	}

	public ReferenceResolver getExternalReferences() {
		return externalReferences;
	}
}