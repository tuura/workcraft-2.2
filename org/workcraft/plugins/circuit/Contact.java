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

package org.workcraft.plugins.circuit;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;

@DisplayName("Contact")
@VisualClass("org.workcraft.plugins.circuit.VisualContact")

public class Contact extends MathNode {

	public enum IOType {input, output};
	private IOType iotype;
	//private boolean invertSignal = false;
	
	public Contact() {
	}
	
	public Contact(String label, IOType iot) {
		super();
		
		setLabel(label);
		setIOType(iot);
	}
	
	public void setIOType(IOType t) {
		this.iotype = t;
	}
	
	public IOType getIOType() {
		return iotype;
	}
}
