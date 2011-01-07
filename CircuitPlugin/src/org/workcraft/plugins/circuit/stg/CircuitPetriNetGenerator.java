package org.workcraft.plugins.circuit.stg;

import static org.workcraft.util.Geometry.add;
import static org.workcraft.util.Geometry.subtract;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.MovableNew;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.Contact.IoType;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.cpog.optimisation.Literal;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.dnf.Dnf;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfClause;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfGenerator;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.util.Hierarchy;

import static org.workcraft.dependencymanager.advanced.core.GlobalCache.*;

public class CircuitPetriNetGenerator {
	
	static class ContactSTG
	{
		public ContactSTG(VisualPlace p0, VisualPlace p1)
		{
			this.p0 = p0;
			this.p1 = p1;
		}
		public final VisualPlace p0;
		public final VisualPlace p1;
	}

	private static final double xScaling = 4;
	private static final double yScaling = 4;
	
	static void setPosition(MovableNew node, Point2D point) {
		MovableHelper.applyTransform(node, AffineTransform.getTranslateInstance(point.getX(), point.getY()));
	}
	
	public static VisualContact findDriver(VisualCircuit circuit, VisualContact target) {
		
		Set<Node> neighbours = new HashSet<Node>(circuit.getPreset(target));
		
		while (neighbours.size()>=1) {
			
			if(neighbours.size() != 1) throw new RuntimeException("Found more than one potential driver for target "+getContactName(circuit, target)+"!");
			
			Node node = neighbours.iterator().next();
			
			if (VisualContact.isDriver(node)) {
				// if it is a driver, return it
				return (VisualContact)node;
			}
			
			// continue searching otherwise
			neighbours = new HashSet<Node>(circuit.getPreset(node));
		}
		
		return null;
	}
	
	private static ContactSTG generatePlaces(VisualCircuit circuit,
			VisualSTG stg, VisualContact contact) {
		
		String contactName = getContactName(circuit, contact);
		VisualPlace zeroPlace = stg.createPlace(contactName+"_0");
		zeroPlace.label().setValue(contactName+"=0");
		
		VisualPlace onePlace = stg.createPlace(contactName+"_1");
		onePlace.label().setValue(contactName+"=1");
		
		(eval(contact.initOne())?onePlace:zeroPlace).tokens().setValue(1);
		
		ContactSTG contactSTG = new ContactSTG(zeroPlace, onePlace);
		
		return contactSTG;
	}
	
	public static void attachConnections(VisualCircuit circuit, VisualComponent component, ContactSTG cstg) {
		if (component instanceof VisualContact) {
			VisualContact vc = (VisualContact)component;
			vc.setReferencedOnePlace(cstg.p1.getReferencedPlace());
			vc.setReferencedZeroPlace(cstg.p0.getReferencedPlace());
		}
		
		for (Connection c: circuit.getConnections(component)) {
			if (c.getFirst()==component&&c instanceof VisualCircuitConnection) {
				
				((VisualCircuitConnection)c).setReferencedOnePlace(cstg.p1.getReferencedPlace());
				((VisualCircuitConnection)c).setReferencedZeroPlace(cstg.p0.getReferencedPlace());
				
				if (c.getSecond() instanceof VisualJoint) {
					VisualJoint vj = (VisualJoint)c.getSecond();
					vj.setReferencedOnePlace(cstg.p1.getReferencedPlace());
					vj.setReferencedZeroPlace(cstg.p0.getReferencedPlace());
					
					attachConnections(circuit, (VisualJoint)c.getSecond(), cstg);
				}
				
				if (c.getSecond() instanceof VisualContact) {
					attachConnections(circuit, (VisualContact)c.getSecond(), cstg);
				}
			}
			
		}
	}
	
	public static VisualSTG generate(VisualCircuit circuit) {
		try {
			VisualSTG stg = new VisualSTG(new STG());
			
			Map<Contact, VisualContact> targetDrivers = new HashMap<Contact, VisualContact>();
			Map<VisualContact, ContactSTG> drivers = new HashMap<VisualContact, ContactSTG>(); 
			
			// generate all possible drivers and fill out the targets
			for(VisualContact contact : Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualContact.class)) {
				ContactSTG cstg;
				
				if(VisualContact.isDriver(contact)) {
					// if it is a driver, add it to the list of drivers
					cstg = generatePlaces(circuit, stg, contact);
					drivers.put(contact, cstg);
					
					// attach driven wires to the place
					attachConnections(circuit, contact, cstg);
					
					// put itself on a target list as well, so that it cab be addressed by other drivers
					targetDrivers.put(contact.getReferencedContact(), contact);
				} else {
					// if not a driver, find related driver, add to the map of targets
					VisualContact driver = findDriver(circuit, contact);
					
					if (driver==null) {
						// if target driver was not found, create artificial one that looks like input
						driver = contact;
						cstg = generatePlaces(circuit, stg, contact);
						
						drivers.put(driver, cstg);
						// attach driven wires to the place
						attachConnections(circuit, contact, cstg);
					}

					targetDrivers.put(contact.getReferencedContact(), driver);
				}
			}
			
			// generate implementation for each of the drivers
			for(VisualContact c : drivers.keySet())
			{
				if (c instanceof VisualFunctionContact) {
					// function based driver
					VisualFunctionContact contact = (VisualFunctionContact)c;
					Dnf set = DnfGenerator.generate(eval(contact.getFunction().setFunction()));
					Dnf reset = null;
					
					if (eval(contact.getFunction().resetFunction())!=null) 
						reset = DnfGenerator.generate(eval(contact.getFunction().resetFunction()));
					else {
						BooleanOperations.worker = new DumbBooleanWorker();
						reset = DnfGenerator.generate(BooleanOperations.worker.not(eval(contact.getFunction().setFunction())));
					}
					
					SignalTransition.Type ttype = SignalTransition.Type.OUTPUT;
					
					
					if (eval(contact.parent()) instanceof VisualCircuitComponent) {
						if (eval(((VisualCircuitComponent)eval(contact.parent())).isEnvironment())) 
								ttype = SignalTransition.Type.INPUT; 
						else if (eval(contact.ioType())==IoType.INPUT)
							ttype = SignalTransition.Type.INPUT;
					} else {
						if (eval(contact.ioType())==IoType.INPUT)
							ttype = SignalTransition.Type.INPUT;
					}
					
					implementDriver(circuit, stg, contact, drivers, targetDrivers, set, reset, ttype);
					
				} else {
					// some generic driver implementation otherwise
					Dnf set = new Dnf(new DnfClause());
					Dnf reset = new Dnf(new DnfClause());
					implementDriver(circuit, stg, c, drivers, targetDrivers, set, reset, SignalTransition.Type.INPUT);
				}
			}
			
			return stg;
		} catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}
	

	private static void implementDriver(VisualCircuit circuit, VisualSTG stg,
			VisualContact contact,
			Map<VisualContact, ContactSTG> drivers,
			Map<Contact, VisualContact> targetDrivers, Dnf set, Dnf reset, SignalTransition.Type ttype) throws InvalidConnectionException {
		
		AffineTransform transform = TransformHelper.getTransformToAncestor(contact, circuit.getRoot());
		Point2D center = new Point2D.Double(xScaling*(transform.getTranslateX()+eval(contact.x())), yScaling*(transform.getTranslateY()+eval(contact.y())));
		
		Point2D direction;
		Point2D pOffset;
		Point2D plusDirection;
		Point2D minusDirection;
		
//		int maxC = Math.max(set.getClauses().size(), reset.getClauses().size()); 
		
		VisualContact.Direction dir = eval(contact.direction());
		
		if (eval(contact.ioType())==IoType.INPUT) {
			dir = VisualContact.flipDirection(dir); 
		}
		
		switch(dir) {
			case WEST:
				direction		= new Point2D.Double( 6, 0);
				pOffset			= new Point2D.Double( 0, -1);
				plusDirection	= new Point2D.Double( 0, -2);
				minusDirection	= new Point2D.Double( 0,  2);
				break;
			case EAST:
				direction		= new Point2D.Double(-6, 0);
				pOffset			= new Point2D.Double( 0, -1);
				plusDirection	= new Point2D.Double( 0, -2);
				minusDirection	= new Point2D.Double( 0,  2);
				break;
			case NORTH:
				direction		= new Point2D.Double( 6, 0);
				pOffset			= new Point2D.Double( 0, -1);
				plusDirection	= new Point2D.Double( 0, -2);
				minusDirection	= new Point2D.Double( 0,  2);
				break;
			case SOUTH:
				direction		= new Point2D.Double(-6, 0);
				pOffset			= new Point2D.Double( 0, -1);
				plusDirection	= new Point2D.Double( 0, -2);
				minusDirection	= new Point2D.Double( 0,  2);
				break;
			default: throw new RuntimeException();
		}
		
		String signalName = getContactName(circuit, contact);
		
		ContactSTG p = drivers.get(contact);
		
		if (p == null)
			throw new RuntimeException("Places for driver " + signalName + " cannot be found.");
		
		Collection<Node> nodes = new LinkedList<Node>();
		
		setPosition(p.p1, add(center, pOffset));
		setPosition(p.p0, subtract(center, pOffset));
		
		nodes.add(p.p1);
		nodes.add(p.p0);
		
		contact.getReferencedTransitions().clear();
		
		nodes.addAll(buildTransitions(contact, stg, circuit, drivers, targetDrivers, 
				set, 
				add(add(center, direction), pOffset), plusDirection, 
				signalName, ttype, SignalTransition.Direction.PLUS, p.p0, p.p1));
		
		nodes.addAll(buildTransitions(contact, stg, circuit, drivers, targetDrivers, 
				reset, 
				subtract(add(center, direction), pOffset),  minusDirection, 
				signalName, ttype, SignalTransition.Direction.MINUS, p.p1, p.p0));
		
		stg.groupCollection(nodes);
		
		
	}

	private static LinkedList<VisualNode> buildTransitions(VisualContact parentContact,
			VisualSTG stg, VisualCircuit circuit, Map<VisualContact, ContactSTG> drivers, Map<Contact, VisualContact> targetDrivers,
			Dnf function, Point2D baseOffset, Point2D transitionOffset, 
			String signalName, SignalTransition.Type type, Direction transitionDirection,
			VisualPlace preset, VisualPlace postset) throws InvalidConnectionException {
		
		LinkedList<VisualNode> nodes = new LinkedList<VisualNode>();
		
		TreeSet<DnfClause> clauses = new TreeSet<DnfClause>(
				new Comparator<DnfClause>() {
					@Override
					public int compare(DnfClause arg0, DnfClause arg1) {
						String st1 = FormulaToString.toString(arg0);
						String st2 = FormulaToString.toString(arg1);
						return st1.compareTo(st2);
					}
				});
		
		clauses.addAll(function.getClauses());
		
		
		for(DnfClause clause : clauses)
		{	
			VisualSignalTransition transition = stg.createSignalTransition(signalName, type, transitionDirection);
			nodes.add(transition);
			parentContact.getReferencedTransitions().add(transition.getReferencedTransition());
			
			setPosition(transition, baseOffset);
			
			stg.connect(transition, postset);
			stg.connect(preset, transition);
			transition.label().setValue(FormulaToString.toString(clause));
			
			baseOffset = add(baseOffset, transitionOffset);
			
			HashSet<VisualPlace> placesToRead = new HashSet<VisualPlace>();
			
			for (Literal literal : clause.getLiterals()) {
				Contact targetContact = (Contact)literal.getVariable();
				
				VisualContact driverContact = targetDrivers.get(targetContact);
				
				ContactSTG source = drivers.get(driverContact);
				
				if(source == null)
					throw new RuntimeException("No source for " + eval(targetContact.name()) + " while generating " + signalName);
				
				VisualPlace p = literal.getNegation() ? source.p0 : source.p1;
				
				placesToRead.add(p);
			}
			
			if(placesToRead.remove(preset))
				System.out.println(String.format("warning: signal %s depends on itself", signalName));
			
			for(VisualPlace p : placesToRead) {
				stg.connect(p, transition);
				stg.connect(transition, p);
			}
		}
		
		return nodes;
	}

	private static String getContactName(VisualCircuit circuit, VisualContact contact) {
		String prefix = "";
		Node parent = eval(contact.parent());
		
		if (parent instanceof VisualFunctionComponent) {
			VisualFunctionComponent vc = (VisualFunctionComponent)parent;
			int cnt=0;
			for (Node n: eval(vc.children())) {
				if ((n instanceof VisualContact)&&
						eval(((VisualContact)n).ioType())!=IoType.INPUT) {
					cnt++;
				}
			}
			if (cnt==1) return eval(((VisualFunctionComponent)parent).name());
		}
		return prefix + eval(contact.name());
	}
}
