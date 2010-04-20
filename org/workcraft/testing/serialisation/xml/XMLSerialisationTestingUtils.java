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

import org.workcraft.PluginInfo;
import org.workcraft.PluginProvider;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.serialisation.xml.AffineTransformDeserialiser;
import org.workcraft.plugins.serialisation.xml.AffineTransformSerialiser;
import org.workcraft.plugins.serialisation.xml.BooleanDeserialiser;
import org.workcraft.plugins.serialisation.xml.BooleanSerialiser;
import org.workcraft.plugins.serialisation.xml.ConnectionDeserialiser;
import org.workcraft.plugins.serialisation.xml.ConnectionSerialiser;
import org.workcraft.plugins.serialisation.xml.DoubleDeserialiser;
import org.workcraft.plugins.serialisation.xml.DoubleSerialiser;
import org.workcraft.plugins.serialisation.xml.EnumDeserialiser;
import org.workcraft.plugins.serialisation.xml.EnumSerialiser;
import org.workcraft.plugins.serialisation.xml.IntDeserialiser;
import org.workcraft.plugins.serialisation.xml.IntSerialiser;
import org.workcraft.plugins.serialisation.xml.StringDeserialiser;
import org.workcraft.plugins.serialisation.xml.StringSerialiser;
import org.workcraft.plugins.serialisation.xml.VisualConnectionDeserialiser;
import org.workcraft.plugins.serialisation.xml.VisualConnectionSerialiser;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcSerialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcDeserialiser;

public class XMLSerialisationTestingUtils {
	static class MockPluginManager implements PluginProvider {
		public PluginInfo[] getPluginsImplementing(String interfaceName) {

			if (interfaceName.equals(org.workcraft.serialisation.xml.XMLSerialiser.class.getName()))
			{
				return new PluginInfo[] {
						new PluginInfo (IntSerialiser.class),
						new PluginInfo (BooleanSerialiser.class),
						new PluginInfo (StringSerialiser.class),
						new PluginInfo (DoubleSerialiser.class),
						new PluginInfo (ConnectionSerialiser.class),
						new PluginInfo (IntSerialiser.class),
						new PluginInfo (EnumSerialiser.class),
						new PluginInfo (AffineTransformSerialiser.class),
						new PluginInfo (VisualConnectionSerialiser.class),
						new PluginInfo (ImplicitPlaceArcSerialiser.class)
				};
			} else if (interfaceName.equals(org.workcraft.serialisation.xml.XMLDeserialiser.class.getName()))
			{
				return new PluginInfo[] {
						new PluginInfo (IntDeserialiser.class),
						new PluginInfo (BooleanDeserialiser.class),
						new PluginInfo (StringDeserialiser.class),
						new PluginInfo (DoubleDeserialiser.class),
						new PluginInfo (ConnectionDeserialiser.class),
						new PluginInfo (IntDeserialiser.class),
						new PluginInfo (EnumDeserialiser.class),
						new PluginInfo (AffineTransformDeserialiser.class),
						new PluginInfo (VisualConnectionDeserialiser.class),
						new PluginInfo (ImplicitPlaceArcDeserialiser.class),
				};
			} else
				throw new RuntimeException ("Mock plugin manager doesn't know interface " + interfaceName);
		}

		@Override
		public Object getInstance(PluginInfo plugin) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getSingleton(PluginInfo plugin) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static PluginProvider createMockPluginManager() {
		return new MockPluginManager();
	}

	public static STG createTestSTG1() {
		try {		
			STG stg = new STG();

			Place p1 = stg.createPlace();
			Place p2 = stg.createPlace();

			SignalTransition t1 = stg.createSignalTransition();
			SignalTransition t2 = stg.createSignalTransition();


			stg.connect(t1, p1);

			stg.connect(p1, t2);
			stg.connect(t2, p2);
			stg.connect(p2, t1);

			return stg;
		}
		catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}		
	}
	
	public static STG createTestSTG2() {
		try {		
			STG stg = new STG();

			Place p1 = stg.createPlace();
			Place p2 = stg.createPlace();

			SignalTransition t1 = stg.createSignalTransition();
			SignalTransition t2 = stg.createSignalTransition();


			stg.connect(t1, p1);

			stg.connect(p1, t2);
			stg.connect(t2, p2);
			stg.connect(p2, t1);

			MathGroup g1 = new MathGroup();

			Place p3 = new Place();
			SignalTransition t3 = new SignalTransition();
			
			g1.add(p3); g1.add(t3);
			
			stg.connect(p3, t3);

			return stg;
		}
		catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}		
	}
	
	public static VisualSTG createTestSTG3() {
		try {		
			STG stg = new STG();

			SignalTransition t1 = stg.createSignalTransition();
			SignalTransition t2 = stg.createSignalTransition();
			SignalTransition t3 = stg.createSignalTransition();
			SignalTransition t4 = stg.createSignalTransition();
			
			VisualSTG visualSTG = new VisualSTG(stg);
			
			VisualSignalTransition vt1 = new VisualSignalTransition(t1);
			VisualSignalTransition vt2 = new VisualSignalTransition(t2);
			VisualSignalTransition vt3 = new VisualSignalTransition(t3);
			VisualSignalTransition vt4 = new VisualSignalTransition(t4);
			
			visualSTG.add(vt1);visualSTG.add(vt2);visualSTG.add(vt3);visualSTG.add(vt4);
			
			visualSTG.connect(vt1, vt2);
			visualSTG.connect(vt2, vt3);
			visualSTG.connect(vt3, vt4);
			visualSTG.connect(vt4, vt1);
			
			
			
			return visualSTG;
		}
		catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}		
	}
}