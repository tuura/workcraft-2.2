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

package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;
import org.workcraft.util.XmlUtil;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class BezierDeserialiser implements CustomXMLDeserialiser {
	@Override
	public String getClassName() {
		return Bezier.class.getName();
	}

	@Override
	public void finaliseInstance(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver,
			NodeFinaliser nodeFinaliser) throws DeserialisationException {
		for (BezierControlPoint cp : eval(((Bezier)instance).getControlPoints()))
			nodeFinaliser.finaliseInstance(cp);
		((Bezier)instance).finaliseControlPoints();
	}

	@Override
	public Object createInstance(Element element,
			ReferenceResolver externalReferenceResolver,
			Object... constructorParameters) {
		return new Bezier ((VisualConnection)constructorParameters[0], (StorageManager)constructorParameters[1]);
	}

	@Override
	public void initInstance(Element element, Object instance,
			ReferenceResolver externalReferenceResolver,
			NodeInitialiser nodeInitialiser) throws DeserialisationException {

		Element cp1e = XmlUtil.getChildElement("cp1", element);
		Element cp2e =  XmlUtil.getChildElement("cp2", element);

		Bezier bezier = (Bezier)instance;
		
		BezierControlPoint cp1 = (BezierControlPoint)nodeInitialiser.initInstance(cp1e, bezier.origin1(), bezier.storage);
		BezierControlPoint cp2 = (BezierControlPoint)nodeInitialiser.initInstance(cp2e, bezier.origin2(), bezier.storage);

		bezier.initControlPoints(cp1, cp2);
	}
}