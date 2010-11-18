package org.workcraft.plugins.circuit.tools;

import java.awt.Color;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.circuit.stg.CircuitPetriNetGenerator;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetSettings;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.tools.STGSimulationTool;
import org.workcraft.util.Func;


public class CircuitSimulationTool extends STGSimulationTool {

	VisualCircuit circuit;
	GraphEditor editor;
	
	@Override
	public String getLabel() {
		return "Simulation";
	}
	
	@Override
	public void activated(GraphEditor editor) {
		this.editor = editor;
		this.circuit = (VisualCircuit)editor.getModel();
		
		visualNet = CircuitPetriNetGenerator.generate(circuit);
		net = (PetriNetModel)visualNet.getMathModel();
		
		initialMarking = readMarking();
		traceStep = 0;
		branchTrace = null;
		branchStep = 0;
		
		update();
		
	}
	
	@Override
	public void update() {
		super.update();
		editor.repaint();
	}

	// return first enabled transition
	public static SignalTransition isContactExcited(VisualContact c, PetriNetModel net) {
		boolean up=false;
		boolean down=false;
		
		SignalTransition st=null;
		if (c==null) return null;
		
		for (SignalTransition tr: c.getReferencedTransitions()) {
			if (net.isEnabled(tr)) {
				if (st==null) st = tr;
				if (tr.getDirection()==Direction.MINUS)
					down = true;
				if (tr.getDirection()==Direction.PLUS)
					up=true;
				if (up&&down) break;
			}
		}
		
		if (up&&down) return null;
		return st;
	}
	
	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Node node = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(), new Func<Node, Boolean>()
				{
					@Override
					public Boolean eval(Node node) {
						return node instanceof VisualContact;
					}
				});
		
		if (node==null) return;
		SignalTransition st = isContactExcited((VisualContact)node, net);
		if (st!=null) {
			executeTransition(st);
			update();
		}
	}
	
	@Override
	public Decorator getDecorator() {
		return new Decorator() {

			@Override
			public Decoration getDecoration(Node node) {
				if(node instanceof VisualContact) {
					
					VisualContact contact = (VisualContact)node;
					
					String transitionId = null;
					Node transition2 = null;
					
					if (branchTrace!=null&&branchStep<branchTrace.size()) {
						transitionId = branchTrace.get(branchStep);
						transition2 = net.getNodeByReference(transitionId);
					} else if (branchTrace==null&&trace!=null&&traceStep<trace.size()) {
						transitionId = trace.get(traceStep);
						transition2 = net.getNodeByReference(transitionId);
					}
					
					if (contact.getReferencedTransitions().contains(transition2)) {
						return new Decoration(){

							@Override
							public Color getColorisation() {
								return PetriNetSettings.getEnabledBackgroundColor();
							}

							@Override
							public Color getBackground() {
								return PetriNetSettings.getEnabledForegroundColor();
							}
						};
						
					}
					
						
					if (isContactExcited((VisualContact)node, net)!=null)
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return PetriNetSettings.getEnabledForegroundColor();
							}

							@Override
							public Color getBackground() {
								return PetriNetSettings.getEnabledBackgroundColor();
							}
						};
						
				} else if (node instanceof VisualJoint) {
					VisualJoint vj = (VisualJoint)node;
					
					if (vj.getReferencedOnePlace()==null||vj.getReferencedZeroPlace()==null) return null;
					
					boolean isOne = vj.getReferencedOnePlace().getTokens()==1;
					boolean isZero = vj.getReferencedZeroPlace().getTokens()==1;
					
					if (isOne&&!isZero)
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return CircuitSettings.getActiveWireColor();
							}
							@Override
							public Color getBackground() {
								return null;
							}
						};
					if (!isOne&&isZero)
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return CircuitSettings.getInactiveWireColor();
							}
							@Override
							public Color getBackground() {
								return null;
							}
						};
				} else if (node instanceof VisualCircuitConnection) {
					VisualCircuitConnection vc = (VisualCircuitConnection)node;
					
					if (vc.getReferencedOnePlace()==null||vc.getReferencedZeroPlace()==null) return null;
					
					boolean isOne = vc.getReferencedOnePlace().getTokens()==1;
					boolean isZero = vc.getReferencedZeroPlace().getTokens()==1;
					
					if (isOne&&!isZero)
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return CircuitSettings.getActiveWireColor();
							}
							@Override
							public Color getBackground() {
								return null;
							}
						};
					if (!isOne&&isZero)
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return CircuitSettings.getInactiveWireColor();
							}
							@Override
							public Color getBackground() {
								return null;
							}
						};
					
				}
				return null;
			} 

			
		};
	}
}
