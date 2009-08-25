package org.workcraft.plugins.balsa;

import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.framework.exceptions.ImportException;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.util.XmlUtil;

public class HandshakeComponent extends Component {
	private BreezeComponent owner;
	private String handshakeName;
	
	private void initSerialization() {
		this.addXMLSerialiser(new XMLSerialiser(){
			public String getTagName() {
				return "Handshake";
			}

			public void deserialise(Element element,
					ReferenceResolver refResolver) throws ImportException {

				Element handshakeElement = XmlUtil.getChildElement("Handshake", element);
				handshakeName = handshakeElement.getAttribute("name");
				owner = (BreezeComponent) refResolver.getObject(handshakeElement.getAttribute("owner"));
			}

			
			public void serialise(Element element,
					ExternalReferenceResolver refResolver) {
				element.setAttribute("name", handshakeName);
				element.setAttribute("owner", owner.getID()+"");
			}
		});
	}

	public HandshakeComponent(BreezeComponent owner, String handshakeName)
	{
		this.owner = owner;
		this.handshakeName = handshakeName;
		initSerialization();
	}

	public BreezeComponent getOwner() {
		return owner;
	}

	public Handshake getHandshake() {
		return owner.getHandshakes().get(handshakeName);
	}

	public Connection getConnection() {
		Set<Connection> connections = getConnections();
		if(connections.size() > 1)
			throw new RuntimeException("Handshake can't have more than 1 connection!");
		if(connections.size() == 0)
			return null;
		return connections.iterator().next();
	}

	public final HandshakeComponent getConnectedHandshake() {
		Connection connection = getConnection();
		if (connection == null)
			return null;
		if (connection.getFirst() == this)
			return (HandshakeComponent) connection.getSecond();
		if (connection.getSecond() == this)
			return (HandshakeComponent) connection.getFirst();
		throw new RuntimeException("Invalid connection");
	}

	public String getHandshakeName() {
		return handshakeName;
	}
}
