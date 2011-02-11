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

package org.workcraft.testing.serialisation.xml;

import org.junit.Test;
import org.workcraft.PluginProvider;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.plugins.layout.RandomLayout;
import org.workcraft.plugins.serialisation.XMLModelDeserialiser;
import org.workcraft.plugins.serialisation.XMLModelSerialiser;
import org.workcraft.plugins.stg.DefaultStorageManager;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.serialisation.DeserialisationResult;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.testing.serialisation.SerialisationTestingUtils;
import org.workcraft.util.DataAccumulator;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class VisualModelSerialisation {

	@Test
	public void SimpleSaveLoad() throws Exception {

		STG stg = XMLSerialisationTestingUtils.createTestSTG1();
		VisualSTG visualstg = new VisualSTG(stg, new HistoryPreservingStorageManager());

		RandomLayout layout = new RandomLayout();
		WorkspaceEntry we = new WorkspaceEntry(null);
		
		we.setModelEntry(new ModelEntry(new STGModelDescriptor(), visualstg, new HistoryPreservingStorageManager()));
		
		layout.run(we);

		// serialise
		PluginProvider mockPluginManager = XMLSerialisationTestingUtils.createMockPluginManager();

		XMLModelSerialiser serialiser = new XMLModelSerialiser(mockPluginManager);

		DataAccumulator mathData = new DataAccumulator();
		ReferenceProducer mathModelReferences = serialiser.serialise(stg, mathData, null);

		DataAccumulator visualData = new DataAccumulator();
		serialiser.serialise(visualstg, visualData, mathModelReferences);

		System.out.println (new String (mathData.getData()));
		System.out.println ("---------------"); 
		System.out.println (new String (visualData.getData()));

		// deserialise
		XMLModelDeserialiser deserialiser = new XMLModelDeserialiser(mockPluginManager);

		StorageManager storage = new DefaultStorageManager();
		
		DeserialisationResult mathResult = deserialiser.deserialise(mathData.getInputStream(), storage, null);
		DeserialisationResult visualResult = deserialiser.deserialise(visualData.getInputStream(), storage, mathResult.referenceResolver);

		SerialisationTestingUtils.compareNodes(visualstg, visualstg.getRoot(), visualResult.model, visualResult.model.getRoot());
	}
}
