package org.workcraft.plugins.stg;

import java.awt.geom.Point2D;
import java.util.HashSet;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualConnection;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModelEventListener;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;

public class VisualSTG extends VisualPetriNet  {
	class Listener implements VisualModelEventListener {
		public void onComponentAdded(VisualComponent component) {
			if (component instanceof VisualSignalTransition)
				transitions.add((VisualSignalTransition)component);
		}

		public void onComponentPropertyChanged(String propertyName,
				VisualComponent component) {
			if (component instanceof VisualSignalTransition && propertyName.equals("Signal type")) {
				VisualSignalTransition t = (VisualSignalTransition)component;
				String signalName = t.getSignalName();
				if (signalName.isEmpty())
					return;

				for (VisualSignalTransition tt : transitions) {
					if (signalName.equals(tt.getSignalName())) {
						tt.setType(t.getType());						
					}
				}
			}
		}

		public void onComponentRemoved(VisualComponent component) {
			if (component instanceof VisualSignalTransition)
				transitions.remove(component);
		}

		public void onConnectionAdded(VisualConnection connection) {
		}

		public void onConnectionPropertyChanged(String propertyName,
				VisualConnection connection) {
		}

		public void onConnectionRemoved(VisualConnection connection) {
		}

		public void onLayoutChanged() {
		}

		public void onSelectionChanged() {
		}
	}

	private HashSet<VisualSignalTransition> transitions = new HashSet<VisualSignalTransition>();

	@Override
	public void validateConnection(VisualNode first, VisualNode second)
	throws InvalidConnectionException {
		if (first instanceof VisualPlace) {
			if (second instanceof VisualPlace)
				throw new InvalidConnectionException ("Connections between places are not allowed");
			if (second instanceof VisualConnection)
				throw new InvalidConnectionException ("Connections between places and implicit places are not allowed");
		}

		if (first instanceof VisualSignalTransition) {
			if (second instanceof VisualConnection)
				if (! (second  instanceof ImplicitPlaceArc))
					throw new InvalidConnectionException ("Only connections with arcs having implicit places are allowed");
		}

		if (first instanceof VisualConnection) {
			if (!(first instanceof ImplicitPlaceArc))
				throw new InvalidConnectionException ("Only connections with arcs having implicit places are allowed");
			if (second instanceof VisualConnection)
				throw new InvalidConnectionException ("Connections between arcs are not allowed");
			if (second instanceof VisualPlace)
				throw new InvalidConnectionException ("Connections between places and implicit places are not allowed");

			ImplicitPlaceArc con = (ImplicitPlaceArc) first;
			if (con.getFirst() == second || con.getSecond() == second)
				throw new InvalidConnectionException ("Arc already exists");
		}
	}

	@Override
	public VisualConnection connect(VisualNode first,
			VisualNode second) throws InvalidConnectionException {

		validateConnection(first, second);

		if (first instanceof VisualSignalTransition) {
			if (second instanceof VisualSignalTransition) {
				STG mathModel = (STG)getMathModel();
				VisualSignalTransition t1 = (VisualSignalTransition) first;
				VisualSignalTransition t2 = (VisualSignalTransition) second;

				Place implicitPlace = mathModel.createPlace();
				Connection con1 = mathModel.connect(t1.getReferencedTransition(), implicitPlace);
				Connection con2 = mathModel.connect(implicitPlace, t2.getReferencedTransition());

				ImplicitPlaceArc connection = new ImplicitPlaceArc((VisualComponent)first, (VisualComponent)second, con1, con2, implicitPlace);

				VisualGroup group = VisualNode.getCommonParent(first, second);

				group.add(connection);
				addConnection(connection);

				return connection;
			} else if (second instanceof ImplicitPlaceArc) {
				ImplicitPlaceArc con = (ImplicitPlaceArc)second;
				VisualGroup group = con.getParent();

				Place implicitPlace = con.getImplicitPlace();

				VisualPlace place = new VisualPlace(implicitPlace);
				Point2D p = con.getPointOnConnection(0.5);
				place.setX(p.getX()); place.setY(p.getY());
				
				VisualConnection con1 = new VisualConnection(con.getRefCon1(), con.getFirst(), place);
				VisualConnection con2 = new VisualConnection(con.getRefCon2(), place, con.getSecond());
				
				addComponent(place);
				addConnection(con1);
				addConnection(con2);
				group.add(place);
				group.add(con1);
				group.add(con2);
				
				removeVisualConnectionOnly(con);
				
				return super.connect(first, place);
			}
		}
		
		if (first instanceof ImplicitPlaceArc)
			if (second instanceof VisualSignalTransition) {
				ImplicitPlaceArc con = (ImplicitPlaceArc)first;
				VisualGroup group = con.getParent();

				Place implicitPlace = con.getImplicitPlace();

				VisualPlace place = new VisualPlace(implicitPlace);
				Point2D p = con.getPointOnConnection(0.5);
				place.setX(p.getX()); place.setY(p.getY());
				
				VisualConnection con1 = new VisualConnection(con.getRefCon1(), con.getFirst(), place);
				VisualConnection con2 = new VisualConnection(con.getRefCon2(), place, con.getSecond());
				
				addComponent(place);
				addConnection(con1);
				addConnection(con2);
				group.add(place);
				group.add(con1);
				group.add(con2);
				
				removeVisualConnectionOnly(con);
				
				return super.connect(place, second);
				
			}
		
		
		
		return super.connect(first, second);			
	}

	private void removeVisualConnectionOnly(VisualConnection connection) {
		connection.getFirst().removeConnection(connection);
		connection.getSecond().removeConnection(connection);
		
		connection.getParent().remove(connection);
		selection().remove(connection);
		connection.removeListener(getPropertyChangeListener());
	}
	
	private void removeVisualComponentOnly(VisualComponent component) {
		component.getParent().remove(component);
		selection().remove(component);
		component.removeListener(getPropertyChangeListener());
	}
	
	private void makeImplicit (VisualPlace place) {
		Connection refCon1 = null, refCon2 = null;
		
		VisualComponent first = place.getPreset().iterator().next();
		VisualComponent second = place.getPostset().iterator().next();

		
		for (VisualConnection con:	place.getConnections()) {
			if (con.getFirst() == place)
				refCon1 = con.getReferencedConnection();
			else if (con.getSecond() == place)
				refCon2 = con.getReferencedConnection();
			
			removeVisualConnectionOnly(con);
		}
		
		removeVisualComponentOnly(place);
		
		ImplicitPlaceArc con = new ImplicitPlaceArc(first, second, refCon1, refCon2, place.getReferencedPlace());
		
		VisualNode.getCommonParent(first, second).add(con);
		addConnection(con);
	}
	
	@Override
	protected void removeConnection(VisualConnection connection) {
		if (connection instanceof ImplicitPlaceArc) {
			connection.getFirst().removeConnection(connection);
			connection.getSecond().removeConnection(connection);
			
			getMathModel().removeConnection(((ImplicitPlaceArc) connection).getRefCon1());
			getMathModel().removeConnection(((ImplicitPlaceArc) connection).getRefCon2());
			getMathModel().removeComponent(((ImplicitPlaceArc) connection).getImplicitPlace());

			connection.getParent().remove(connection);
			selection().remove(connection);

			connection.removeListener(getPropertyChangeListener());
			
			fireConnectionRemoved(connection);		
			
		} else {
			super.removeConnection(connection);
			
			VisualComponent c1 = connection.getFirst();
			VisualComponent c2 = connection.getSecond();
			VisualPlace place = null;
			
			if (c1 instanceof VisualPlace)
				place = (VisualPlace)c1;
			if (c2 instanceof VisualPlace)
				place = (VisualPlace)c2;
			
			if (place!=null) 
				if (place.getPreset().size() == 1 && place.getPostset().size() == 1)
					makeImplicit (place);
		}
	}
	
	public VisualSTG(STG model) throws VisualModelInstantiationException {
		super(model);
		addListener(new Listener());		
	}

	public VisualSTG(STG model, Element element) throws VisualModelInstantiationException {
		super(model, element);
		addListener(new Listener());
	}
}
