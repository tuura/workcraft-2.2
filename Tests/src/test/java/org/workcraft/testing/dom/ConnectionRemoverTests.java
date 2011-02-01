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

package org.workcraft.testing.dom;

import java.util.LinkedList;

import org.junit.Test;
import org.workcraft.dependencymanager.advanced.core.GlobalCache;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;

import pcollections.HashTreePSet;

public class ConnectionRemoverTests {
	@Test
	public void removeMany() throws InvalidConnectionException {
		//PropertyConfigurator.configure("config/logging.properties");
		
		HistoryPreservingStorageManager storage = new HistoryPreservingStorageManager();
		STG stg = new STG(storage);
		
		SignalTransition t1 = stg.createSignalTransition();
		Place p1 = stg.createPlace();
		SignalTransition t2 = stg.createSignalTransition();
		Place p2 = stg.createPlace();
		SignalTransition t3 = stg.createSignalTransition();
		
		stg.connect(t3, p2);
		stg.connect(p2, t2);
		stg.connect(t2, p1);
		stg.connect(p1, t1);
		
		VisualSTG vstg = new VisualSTG(stg, storage);
		
		//System.out.println ("Created VSTG");
		
		LinkedList<Node> toDelete = new LinkedList<Node>();
		LinkedList<Node> toDeleteThen = new LinkedList<Node>();
		
		for (Node n : GlobalCache.eval(vstg.getRoot().children())) {
			DependentNode dn = (DependentNode)n;
			if (!dn.getMathReferences().contains(t1))
				toDelete.add(n);
			else
				toDeleteThen.add(n);
		}
		
		vstg.selection().setValue(HashTreePSet.from(toDelete));
		vstg.deleteSelection();
		
		//System.out.println ("O_O");
		
		vstg.selection().setValue(HashTreePSet.from(toDeleteThen));
		vstg.deleteSelection();
	}
}
