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
import java.util.LinkedList;
import java.util.Set;

import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesAddingEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.NodesReparentedEvent;
import org.workcraft.observation.NodesReparentingEvent;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.ObservableHierarchyImpl;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.StateObserver;

public class DefaultGroupImpl implements ObservableHierarchy, ObservableState, Container {
	private Node parent = null;
	private Set<Node> children = new LinkedHashSet<Node> ();
	private ObservableHierarchyImpl observableHierarchyImpl = new ObservableHierarchyImpl();
	private ObservableStateImpl observableStateImpl = new ObservableStateImpl();
	private Container groupRef;
	
	public DefaultGroupImpl (Container groupRef) {
		this.groupRef = groupRef;
	}

	public Collection<Node> getChildren() {
		return Collections.unmodifiableCollection(children);
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		if (parent != null && this.parent != null)
			throw new RuntimeException ("Cannot assign new parent to a node that already has a parent.");

		this.parent = parent;
	}

	private void addInternal(Node node, boolean notify) {
		if (node.getParent() == this)
			return;

		if (node.getParent() != null)
			throw new RuntimeException("Cannot attach someone else's node. Please detach from the old parent first.");

		if (notify)
			observableHierarchyImpl.sendNotification (new NodesAddingEvent(groupRef, node));

		children.add(node);
		node.setParent(groupRef);

		if (notify)
			observableHierarchyImpl.sendNotification (new NodesAddedEvent(groupRef, node));
	}
	
	public void add(Node node) {
		addInternal (node, true);
	}
	
	public void add(Collection<Node> nodes) {
		observableHierarchyImpl.sendNotification (new NodesAddingEvent(groupRef, nodes));
		
		for (Node node : nodes)
			addInternal(node, false);

		observableHierarchyImpl.sendNotification (new NodesAddedEvent(groupRef, nodes));
	}

	private void removeInternal (Node node, boolean notify) {
		if (notify)
			observableHierarchyImpl.sendNotification(new NodesDeletingEvent(groupRef, node));

		if (node.getParent() != groupRef)
			throw new RuntimeException 
			("Failed to remove a node frome a group because it is not a child of that group ("+node+", parent is " + node.getParent() +", expected " + groupRef + ")");
		
		children.remove(node);
		node.setParent(null);

		if (notify)
			observableHierarchyImpl.sendNotification (new NodesDeletedEvent(groupRef, node));
	}
	
	public void remove (Node node) {
		removeInternal (node, true);
	}

	public void remove (Collection<Node> nodes) {
		LinkedList<Node> nodesToRemove = new LinkedList<Node>(nodes);
		
		observableHierarchyImpl.sendNotification(new NodesDeletingEvent(groupRef, nodesToRemove));

		for (Node node : nodesToRemove)
			removeInternal(node, false);

		observableHierarchyImpl.sendNotification (new NodesDeletedEvent(groupRef, nodesToRemove));
	}
	
	public void reparent(Collection<Node> nodes, Container newParent) {
		observableHierarchyImpl.sendNotification(new NodesReparentingEvent(groupRef, newParent, nodes));
		
		for (Node node : nodes)
			removeInternal(node, false);

		newParent.reparent(nodes);
		
		observableHierarchyImpl.sendNotification(new NodesReparentedEvent(groupRef, newParent, nodes));
	}
	
	public void reparent (Collection<Node> nodes) {
		for (Node node : nodes)
			addInternal(node, false);
	}

	public void addObserver(HierarchyObserver obs) {
		observableHierarchyImpl.addObserver(obs);
	}

	public void removeObserver(HierarchyObserver obs) {
		observableHierarchyImpl.removeObserver(obs);
	}

	public void addObserver(StateObserver obs) {
		observableStateImpl.addObserver(obs);
	}

	public void removeObserver(StateObserver obs) {
		observableStateImpl.removeObserver(obs);
	}
}
