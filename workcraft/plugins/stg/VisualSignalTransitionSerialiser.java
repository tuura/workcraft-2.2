package org.workcraft.plugins.stg;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.serialisation.ReferenceProducer;
import org.workcraft.framework.serialisation.xml.ReferencingXMLSerialiser;

public class VisualSignalTransitionSerialiser implements ReferencingXMLSerialiser {
	public void serialise(Element element, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences) throws SerialisationException {
		element.setAttribute("ref", externalReferences.getReference(((VisualSignalTransition)object).getReferencedTransition()));		
	}

	public String getClassName() {
		return VisualSignalTransition.class.getName();
	}

}
