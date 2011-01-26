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

package org.workcraft.testing.plugins.interop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.dom.Connection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.HistoryPreservingStorageManager;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.STGPlace;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Import;
import org.workcraft.workspace.ModelEntry;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class DotGImporterTests {
	@Test
	public void Test1() throws IOException, DeserialisationException
	{
		File tempFile = File.createTempFile("test", ".g");
		
		FileOutputStream fileStream = new FileOutputStream(tempFile);
		
		OutputStreamWriter writer = new OutputStreamWriter(fileStream);
		
		writer.write("\n");
		writer.write("   #test \n");
		writer.write("   # for DotGImporter\n");
		writer.write("\n");
		writer.write(".outputs  x\t y   z\n");
		writer.write("\n");
		writer.write(".inputs  a\tb \tc\n");
		writer.write("\n");
		writer.write(" \t.graph\n");
		writer.write("a+ p1 p2\n");
		writer.write("b+ p1 p2\n");
		writer.write(" c+  p1 \t p2\n");
		writer.write("\n");
		writer.write("p1 z+ y+ x+\n");
		writer.write("p2 z+ y+ x+\n");
		writer.write("\n");
		writer.write(".marking { }\n");
		writer.write(".end\n");
		
		writer.close();
		fileStream.close();
		
		ModelEntry importedEntry =  Import.importFromFile(new DotGImporter(), tempFile);
		STG imported = (STG)importedEntry.getModel();
		
		Assert.assertEquals(6, Hierarchy.getChildrenOfType(imported.getRoot(), Transition.class).size());
		Assert.assertEquals(2, Hierarchy.getChildrenOfType(imported.getRoot(), Place.class).size());
		Assert.assertEquals(12, Hierarchy.getChildrenOfType(imported.getRoot(), Connection.class).size());
	}
	
	@Test
	public void Test2() throws Throwable
	{
		final InputStream test = ClassLoader.getSystemClassLoader().getResourceAsStream("test2.g");
		STGModel imported = new DotGImporter().importSTG(test, new HistoryPreservingStorageManager());//DotGImporterTests.class.getClassLoader().getResourceAsStream("test2.g"));
		Assert.assertEquals(17, imported.getTransitions().size());
		Assert.assertEquals(0, imported.getDummies().size());
		
		int explicitPlaces = 0;
		for(Place p : imported.getPlaces())
		{
			if(!eval(((STGPlace)p).implicit())) explicitPlaces ++;
		}
		
		Assert.assertEquals(2, explicitPlaces);
		
		Assert.assertEquals(18, imported.getPlaces().size());
		
		for(Transition t : imported.getTransitions())
		{
			Assert.assertTrue(imported.getPreset(t).size()>0);
			Assert.assertTrue(imported.getPostset(t).size()>0);
		}
	}
}
