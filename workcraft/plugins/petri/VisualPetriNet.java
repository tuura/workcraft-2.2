package org.workcraft.plugins.petri;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.VisualClassConstructionException;

public class VisualPetriNet extends VisualModel {

	public VisualPetriNet(PetriNet model)
			throws VisualClassConstructionException {
		super(model);
	}
	
	public VisualPetriNet(PetriNet model, Element visualElement) throws VisualClassConstructionException {
		super(model, visualElement);
	
	}
	
}