package org.workcraft.plugins.stg;

import java.util.Collection;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.references.UniqueNameManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.DuplicateIDException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Identifier;
import org.workcraft.util.ListMap;
import org.workcraft.util.Pair;
import org.workcraft.util.Triple;

public class STGReferenceManager extends HierarchySupervisor implements ReferenceManager {
	private InstanceManager<Node> instancedNameManager;
	private UniqueNameManager<Node> defaultNameManager;
	private References existingReferences;

	private ListMap<String, SignalTransition> transitions = new ListMap<String, SignalTransition>();

	private int signalCounter = 0;
	private int dummyCounter = 0;

	public STGReferenceManager(References existingReferences) {
		this.existingReferences = existingReferences;

		this.defaultNameManager = new UniqueNameManager<Node>(new Func<Node, String>() {
			@Override
			public String eval(Node arg) {
				if (arg instanceof STGPlace)
					return "p";
				if (arg instanceof Connection)
					return "con";
				if (arg instanceof Container)
					return "group";
				return "node";
			}
		});

		this.instancedNameManager = new InstanceManager<Node>(new Func<Node, String>() {
			@Override
			public String eval(Node arg) {
				if (arg instanceof SignalTransition) {
					return ((SignalTransition) arg).getSignalName() + ((SignalTransition) arg).getDirection();
				} else if (arg instanceof DummyTransition){
					return ((DummyTransition)arg).getName();
				} else
					throw new RuntimeException ("Unexpected class " + arg.getClass().getName());
			}
		});
	}

	@Override
	public void attach(Node root) {
		if (root == null)
			throw new NullPointerException();

		if (existingReferences != null) {
			setExistingReference(root);
			for (Node n: Hierarchy.getDescendantsOfType(root, Node.class))
				setExistingReference(n);
			existingReferences = null;
		}

		super.attach(root);
	}

	private void setExistingReference(Node n) {
		final String reference = existingReferences.getReference(n);
		if (reference != null) {
			if (n instanceof STGPlace) {
				if (! ((STGPlace) n).isImplicit())
					setName (n, reference);
			} else setName (n, reference);
		}
	}

	@Override
	public Node getNodeByReference(String reference) {
		try {
			Pair<String, Integer> instancedName = LabelParser.parse(reference);
			return instancedNameManager.getObject(instancedName);
		} catch (NotFoundException e) {
			return defaultNameManager.get(reference);
		} catch (ArgumentException e) {
			return defaultNameManager.get(reference);
		}
	}

	@Override
	public String getNodeReference(Node node) {
		if (node instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)node;
			final Integer instance = instancedNameManager.getInstance(st).getSecond();
			if (instance == 0)
				return st.getSignalName() + st.getDirection();
			else
				return st.getSignalName() + st.getDirection() + "/" + instance;
		} else if (node instanceof Transition) {
			final Transition t = (Transition)node;
			final Pair<String, Integer> name = instancedNameManager.getInstance(t);

			if (name.getSecond() == 0)
				return name.getFirst();
			else
				return name.getFirst() + "/" + name.getSecond();
		} else
			return defaultNameManager.getName(node);
	}

	public String getName (Node node) {
		if (node instanceof Transition) {
			Pair<String, Integer> instance = instancedNameManager.getInstance(node);
			return instance.getFirst() + "/" + instance.getSecond(); 
		} else
			return defaultNameManager.getName(node);
	}

	public Collection<SignalTransition> getSignalTransitions(String signalName) {
		return transitions.get(signalName);
	}

	public int getInstanceNumber (Node st) {
		return instancedNameManager.getInstance(st).getSecond();
	}

	public void setInstanceNumber (Node st, int number) {
		instancedNameManager.assign(st, number);
	}

	public void setName(Node node, String s) {
		if (node instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)node;

			try {
				final Triple<String, Direction, Integer> r = LabelParser.parseFull(s);
				instancedNameManager.assign(st, Pair.of(r.getFirst()+r.getSecond(), r.getThird()));

				transitions.remove(st.getSignalName(), st);
				transitions.put(r.getFirst(), st);

				st.setSignalName(r.getFirst());
				st.setDirection(r.getSecond());
			} catch (DuplicateIDException e) {
				throw new ArgumentException ("Instance number " + e.getId() + " is already taken.");
			} catch (ArgumentException e) {
				if (Identifier.isValid(s)) {
					instancedNameManager.assign(st, s+st.getDirection());
					
					transitions.remove(s, st);
					transitions.put(s, st);

					st.setSignalName(s);
				} else
					throw new ArgumentException ("\"" + s + "\" is not a valid signal transition label.");
			}
		} else if (node instanceof DummyTransition) {
			final DummyTransition dt = (DummyTransition)node;

			try {
				final Pair<String,Integer> r = LabelParser.parse(s);
				if (r.getSecond() != null)
					instancedNameManager.assign(dt, r);
				else
					instancedNameManager.assign(dt, r.getFirst());
				dt.setName(r.getFirst());
			} catch (DuplicateIDException e) {
				throw new ArgumentException ("Instance number " + e.getId() + " is already taken.");
			}
		}
		else
			defaultNameManager.setName(node, s);
	}


	@Override
	public void handleEvent(HierarchyEvent e) {
		if(e instanceof NodesDeletedEvent)
			for(Node node : e.getAffectedNodes()) {
				nodeRemoved(node);
				for (Node n : Hierarchy.getDescendantsOfType(node, Node.class))
					nodeRemoved(n);
			}
		if(e instanceof NodesAddedEvent)
			for(Node node : e.getAffectedNodes()) {
				setDefaultNameIfUnnamed(node);
				for (Node n : Hierarchy.getDescendantsOfType(node, Node.class))
					setDefaultNameIfUnnamed(n);
			}
	}

	private void setDefaultNameIfUnnamed(Node node) {
		if (node instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)node;

			if (instancedNameManager.contains(st))
				return;

			String name = "signal" + signalCounter++;
			st.setSignalName(name);
			transitions.put(name, st);
			instancedNameManager.assign(st);
		} else if (node instanceof DummyTransition) {
			final DummyTransition dt = (DummyTransition)node;

			if (instancedNameManager.contains(dt))
				return;

			String name = "dummy" + dummyCounter++;
			dt.setName(name);

			instancedNameManager.assign(dt);
		} else
			defaultNameManager.setDefaultNameIfUnnamed(node);
	}

	private void nodeRemoved(Node node) {
		if (node instanceof SignalTransition) {
			final SignalTransition st = (SignalTransition)node;
			transitions.remove(st.getSignalName(), st);
			instancedNameManager.remove(st);
		} 
		if (node instanceof DummyTransition) {
			final DummyTransition dt = (DummyTransition)node;
			instancedNameManager.remove(dt);
		} else
			defaultNameManager.remove(node);
	}
}