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
package org.workcraft.plugins.cpog.optimisation;

import java.util.ArrayList;
import java.util.List;

public class TwoHotRange extends ArrayList<CnfLiteral> {

	private final List<CnfLiteral> thermometer;

	public TwoHotRange(List<CnfLiteral> literals, List<CnfLiteral> thermometer) {
		super(literals);
		this.thermometer = thermometer;
	}

	public List<CnfLiteral> getThermometer() {
		return thermometer;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4084655691544414217L;

}
