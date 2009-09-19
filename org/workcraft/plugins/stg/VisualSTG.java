package org.workcraft.plugins.stg;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.annotations.DefaultCreateButtons;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.CustomToolButtons;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.SimulationTool;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.util.Hierarchy;

@DisplayName("Signal Transition Graph")
@DefaultCreateButtons ( { Place.class,  SignalTransition.class } )
@CustomToolButtons ( { SimulationTool.class } )  
public class VisualSTG extends VisualPetriNet {

	@Override
	public void validateConnection(Node first, Node second)
	throws InvalidConnectionException {
		if (first instanceof VisualPlace) {
			if (second instanceof VisualPlace)
				throw new InvalidConnectionException ("Arcs between places are not allowed");
			if (second instanceof VisualConnection)
				throw new InvalidConnectionException ("Arcs between places and implicit places are not allowed");
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
				throw new InvalidConnectionException ("Arcs between places are not allowed");
			if (second instanceof VisualPlace)
				throw new InvalidConnectionException ("Arcs between places are not allowed");

			ImplicitPlaceArc con = (ImplicitPlaceArc) first;
			if (con.getFirst() == second || con.getSecond() == second)
				throw new InvalidConnectionException ("Arc already exists");
		}
	}

	@Override
	public Connection connect(Node first,
			Node second) throws InvalidConnectionException {

		validateConnection(first, second);

		if (first instanceof VisualSignalTransition) {
			if (second instanceof VisualSignalTransition) {
				STG mathModel = (STG)getMathModel();

				VisualSignalTransition t1 = (VisualSignalTransition) first;
				VisualSignalTransition t2 = (VisualSignalTransition) second;

				Place implicitPlace = mathModel.createPlace();
				MathConnection con1 = (MathConnection) mathModel.connect(t1.getReferencedTransition(), implicitPlace);
				MathConnection con2 = (MathConnection) mathModel.connect(implicitPlace, t2.getReferencedTransition());

				ImplicitPlaceArc connection = new ImplicitPlaceArc((VisualComponent)first, (VisualComponent)second, con1, con2, implicitPlace);

				Container group =
					Hierarchy.getNearestAncestor(
							Hierarchy.getCommonParent(first, second),
							Container.class);

				group.add(connection);

				return connection;
			} else if (second instanceof ImplicitPlaceArc) {
				ImplicitPlaceArc con = (ImplicitPlaceArc)second;
				Container group = Hierarchy.getNearestAncestor(con, Container.class);

				Place implicitPlace = con.getImplicitPlace();

				VisualPlace place = new VisualPlace(implicitPlace);
				Point2D p = con.getPointOnConnection(0.5);
				place.setX(p.getX()); place.setY(p.getY());

				VisualConnection con1 = new VisualConnection(con.getRefCon1(), con.getFirst(), place);
				VisualConnection con2 = new VisualConnection(con.getRefCon2(), place, con.getSecond());

				group.add(place);
				group.add(con1);
				group.add(con2);

				remove(con);

				return super.connect(first, place);
			}
		}

		if (first instanceof ImplicitPlaceArc)
			if (second instanceof VisualSignalTransition) {
				ImplicitPlaceArc con = (ImplicitPlaceArc)first;
				Container group = Hierarchy.getNearestAncestor(con, Container.class);

				Place implicitPlace = con.getImplicitPlace();

				VisualPlace place = new VisualPlace(implicitPlace);
				Point2D p = con.getPointOnConnection(0.5);
				place.setX(p.getX()); place.setY(p.getY());

				VisualConnection con1 = new VisualConnection(con.getRefCon1(), con.getFirst(), place);
				VisualConnection con2 = new VisualConnection(con.getRefCon2(), place, con.getSecond());

				group.add(place);
				group.add(con1);
				group.add(con2);

				remove(con);

				return super.connect(place, second);
			}

		return super.connect(first, second);
	}

	private VisualConnection maybeMakeImplicit (VisualPlace place) {
		if (getPreset(place).size() != 1 || getPostset(place).size() != 1)
			return null; // not an implicit place

		MathConnection refCon1 = null, refCon2 = null;

		VisualComponent first = (VisualComponent) getPreset(place).iterator().next();
		VisualComponent second = (VisualComponent) getPostset(place).iterator().next();

		Collection<Connection> connections = new ArrayList<Connection> (getConnections(place));
		for (Connection con: connections)
			if (con.getFirst() == place)
				refCon2 = ((VisualConnection)con).getReferencedConnection();
			else if (con.getSecond() == place)
				refCon1 = ((VisualConnection)con).getReferencedConnection();


		ImplicitPlaceArc con = new ImplicitPlaceArc(first, second, refCon1, refCon2, place.getReferencedPlace());

		Hierarchy.getNearestAncestor(
				Hierarchy.getCommonParent(first, second), Container.class)
				.add(con);

		remove(place);
		// connections will get removed automatically by the hanging connection remover

		return con;
	} 

	public VisualSTG(STG model) {
		this (model, null);
	}

	public VisualSTG(STG model, VisualGroup root) {
		super(model, root);

		Collection<VisualPlace> places = new ArrayList<VisualPlace>(Hierarchy.getDescendantsOfType(getRoot(), VisualPlace.class));
		for(VisualPlace place : places)
			maybeMakeImplicit(place);
	}

	@Override
	public void validate() throws ModelValidationException {
		getMathModel().validate();	
	}
}
