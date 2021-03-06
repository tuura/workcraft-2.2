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
package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.parsers.breeze.ParameterScope;
import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public abstract class GeneratedComponentStgBuilder<Properties, HandshakesStg, Handshakes> extends ComponentStgBuilder<DynamicComponent> {
	abstract public void buildStg(Properties component, HandshakesStg h, StrictPetriBuilder b);
	
	abstract public Properties makeProperties(ParameterScope parameters);
	abstract public HandshakesStg makeHandshakesStg(Properties component, Map<String, StgInterface> handshakes);
	abstract public Handshakes makeHandshakes(Properties component, Map<String, Handshake> handshakes);
	
	abstract public HandshakeComponentLayout getLayout(Properties properties, Handshakes hs);

	@Override
	public void buildStg(DynamicComponent component, Map<String, StgInterface> handshakes, StrictPetriBuilder builder) {
		Properties properties = makeProperties(component.parameters());
		HandshakesStg hs = makeHandshakesStg(properties, handshakes);
		buildStg(properties, hs, builder);
	}
	
	@Override
	public HandshakeComponentLayout getLayout(DynamicComponent component, Map<String, Handshake> handshakes) {
		Properties properties = makeProperties(component.parameters());
		Handshakes hs = makeHandshakes(properties, handshakes);
		return getLayout(properties,  hs);
	}
}
