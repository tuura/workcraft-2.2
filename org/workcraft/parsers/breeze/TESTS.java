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

package org.workcraft.parsers.breeze;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.workcraft.parsers.lisp.ParseException;


public class TESTS {
	@Test
	public void parseAbs() throws ParseException, org.workcraft.parsers.breeze.javacc.ParseException, IOException
	{
		BreezeLibrary lib = new BreezeLibrary();
		lib.registerPrimitives(new File("C:\\deleteMe\\"));
	}
	
	/*@Test
	public void viterbiToGates() throws Exception
	{
		File bzrFileName = new File("C:\\deleteMe\\viterbi\\BMU.breeze");
		File definitionsFolder = new File("C:\\deleteMe");
		
		BreezeLibrary lib = new BreezeLibrary();
	//	registerPrimitives(definitionsFolder, lib);
		
		registerParts(bzrFileName, lib);
		
		BalsaCircuit circuit = new BalsaCircuit();
		
		DefaultBreezeFactory factory = new DefaultBreezeFactory(circuit);
		
		//lib.get("BMU").instantiate(factory, EmptyValueList.instance());
	}

	private void registerParts(File file, BreezeLibrary lib) throws Exception {
		InputStream is = new FileInputStream(file);
		try
		{
		//	BreezeParser.registerBreezeParts(is, lib);
		}
		finally
		{
			is.close();
		}
	}*/
}
