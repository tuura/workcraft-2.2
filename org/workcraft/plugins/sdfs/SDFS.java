package org.workcraft.plugins.sdfs;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;

@DisplayName ("Static Data Flow Structure")
@VisualClass ("org.workcraft.plugins.sdfs.VisualSDFS")
public class SDFS extends AbstractMathModel {

	public SDFS() {
		super(null);
		
		new DefaultHangingConnectionRemover(this, "SDFS").attach(getRoot());
	}

	public void validate() throws ModelValidationException {
	}


	public void validateConnection(Connection connection)	throws InvalidConnectionException {
	}

	final public Register createRegister() {
		Register newRegister = new Register();
		add(newRegister);
		return newRegister;
	}

	final public Logic createLogic() {
		Logic newLogic = new Logic();
		add(newLogic);
		return newLogic;
	}
	
	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
		if (first == second)
			throw new InvalidConnectionException ("Self-loops are not allowed");
	}
}
