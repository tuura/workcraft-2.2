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

import org.workcraft.plugins.balsa.components.Loop;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class LoopStgBuilder extends ComponentStgBuilder<org.workcraft.plugins.balsa.components.Loop> {

	@Override
	public void buildStg(Loop component, Map<String, StgInterface> handshakes, StrictPetriBuilder builder) {
		PassiveSync activate = (PassiveSync)handshakes.get("activate");
		ActiveSync activateOut = (ActiveSync)handshakes.get("activateOut");
		
		StgPlace activated = builder.buildPlace(0);
		
		StgPlace never = builder.buildPlace(0);
		
		builder.connect(activate.go(), activated);
		builder.connect(activated, activateOut.go());
		builder.connect(activateOut.done(), activated);
		
		builder.connect(never, activate.done());
	}
  
}
