package org.workcraft.plugins.interop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.dom.Component;
import org.workcraft.dom.Model;
import org.workcraft.framework.Exporter;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;

public class DotGExporter implements Exporter {
	
	private String getTypeLetter(SignalTransition st) {
		switch (st.getType()) {
		case INTERNAL:
			return "i";
		case INPUT:
			return "I";
		case OUTPUT:
			return "O";
		case DUMMY:
			return "d"; // except dummy, those are not normal cases
		}
		return "";
	}
	
	// returns non-empty name for a transition 
	private String getName(HashSet<String> names, Component st) {
		
		String sname = "_";
		
		if (st instanceof Place) {
			sname = "p" + st.getID();
		} else if (st instanceof SignalTransition) {
			
			sname = ((SignalTransition)st).getSignalName();
			
			if (sname.equals("")) {
				sname=getTypeLetter((SignalTransition)st)+st.getID();
			} else return sname;
		}
		
		// is sname busy?
		while (names.contains(sname)) {
			sname = "_"+sname;
		}
		
		return sname;
	}
	
	private String getTransitionName(HashSet<String> names, SignalTransition st) {
		
		String sname = getName(names, st);
		if (st.getType()!= SignalTransition.Type.DUMMY) {
			switch (st.getDirection()) {
			case PLUS: 
				sname+="+";
				break;
			case MINUS: 
				sname+="-";
				break;
			}
			if (st.getInstance()>1) sname+="/"+st.getInstance();
		}
		
		return sname;
	}
	
	
	
	public void exportToFile(Model model, File fname) {
		
		if (!(model.getMathModel() instanceof STG)) return;
		
		STG stg = (STG)model.getMathModel();
		
		stg.assignInstances();
		
		//////////////////////////////////////////////////
		// create the lists of all of the transition types
		ArrayList<String> internal	= new ArrayList<String>();
		ArrayList<String> inputs	= new ArrayList<String>();
		ArrayList<String> outputs	= new ArrayList<String>();
		ArrayList<String> dummy		= new ArrayList<String>();
		HashSet<String> allnames = new HashSet<String>();
		
		Collection<SignalTransition> transitions = stg.getSignalTransitions();
		Collection<Place> places = stg.getPlaces();
		
		//Pattern p = Pattern.compile(STG.signalPattern);

		// add all names to the exception list
		allnames.add("");
		for (SignalTransition st: transitions) {
			allnames.add(st.getSignalName());
		}
		
		String sname;
		// sort out all the transitions
		for (SignalTransition st: transitions) {
			sname = getName(allnames, st);
			
			switch (st.getType()) {
			case INTERNAL:
				if (!internal.contains(sname)) internal.add(sname);
				break;
			case INPUT:
				if (!inputs.contains(sname)) inputs.add(sname);
				break;
			case OUTPUT:
				if (!outputs.contains(sname)) outputs.add(sname);
				break;
			case DUMMY: 
				if (!dummy.contains(sname)) dummy.add(sname);
				break;
			}
		}
		
		Collections.sort(inputs);
		Collections.sort(outputs);
		Collections.sort(internal);
		Collections.sort(dummy);
		
		////////////////////////////////////////////////////
		// prepare connection lists

		List<String> connections1 = new ArrayList<String>(); // connections from transitions
		List<String> connections2 = new ArrayList<String>(); // connections from places
		String tokens = ""; // all the token markings, separated with space
		String capacity = ""; // each token capacity
		
		for (SignalTransition st : transitions) {
			List<String> ts = new ArrayList<String>();
			for (Component c : st.getPostset()) {
				if (c instanceof Place) {
					ts.add(getName(allnames, (Place)c));
				}
			}
			Collections.sort(ts);
			
			String ts2 = "";
			for (String s: ts) ts2+=" "+s;
			connections1.add(getTransitionName(allnames, st)+ts2);
		}

		
		
		for (Place p :  places) {
			List<String> ts = new ArrayList<String>();
			
			for (Component c : p.getPostset()) {
				if (c instanceof SignalTransition) {
					ts.add(getTransitionName(allnames, (SignalTransition)c));
				}
			}
			Collections.sort(ts);
			
			String ts2 = "";
			for (String s: ts) ts2+=" "+s;
			connections2.add(getName(allnames, p)+ts2);
			
			if (p.getTokens()>0) {
				tokens+=" "+getName(allnames, p);
				if (p.getTokens()>1)
					tokens+="="+p.getTokens();
			}
			
			if (p.getCapacity()!=1) {
				capacity+=" "+getName(allnames, p)+"="+p.getCapacity();
			}
			
		}
		
		////////////////////////////////////////////////////
		// save everything now
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter(fname));
			out.print("# STG file generated by Workcraft.\n");
			

			if (internal.size()>0) {
				out.print(".internal ");
				for (String s : internal) out.print(" "+s);
				out.print("\n");
			}

			if (inputs.size()>0) {
				Collections.sort(inputs);
				out.print(".inputs ");
				for (String s : inputs) out.print(" "+s);
				out.print("\n");
			}
			
			if (outputs.size()>0) {
				Collections.sort(outputs);
				out.print(".outputs ");
				for (String s : outputs) out.print(" "+s);
				out.print("\n");
			}
			
			if (dummy.size()>0) {
				Collections.sort(dummy);
				out.print(".dummy ");
				for (String s : dummy) out.print(" "+s);
				out.print("\n");
			}
			
			out.print(".graph\n");

			Collections.sort(connections1);
			for (String s : connections1) out.print(s+"\n");
			
			Collections.sort(connections2);
			for (String s : connections2) out.print(s+"\n");

			out.print(".marking { "+tokens+" }\n");
			if (!capacity.equals(""))
				out.print(".capacity "+capacity+"\n");
			
			out.print(".end\n");
			out.close();

		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "File could not be opened for writing.");
			return;
	    }
		
	}

	public boolean isApplicableTo(Model model) {
		if (model.getClass().equals(STG.class))
			return true;
		if (model.getClass().equals(VisualSTG.class))
			return true;
		if (model.getClass().equals(PetriNet.class))
			return true;
		if (model.getClass().equals(VisualPetriNet.class))
			return true;

		return false;
	}

	public String getExtenstion() {
		return ".g";
	}

	public String getDescription() {
		return ".g (Petrify, PUNF)";
	}
}