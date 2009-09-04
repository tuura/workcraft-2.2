package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Case;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;

public class Case_NoDataPath_OneBit extends ComponentStgBuilder<Case> {
	
	public void buildStg(Case component, Map<String, StgHandshake> handshakes, StgBuilder builder)
	{
		//if(component.getInputWidth()!=1)
		//	throw new RuntimeException("Only input width of 1 is supported");
		
		PassivePushStg in = (PassivePushStg)handshakes.get("inp");
		
		StgPlace guardChangeAllowed = builder.buildPlace(1);
		
		StgSignal dataSignal = builder.buildSignal(new SignalId(component, "dp"), false);
		final StgPlace dataOne = builder.buildPlace();
		final StgPlace dataZero = builder.buildPlace(1);
		builder.addConnection(dataOne, dataSignal.getMinus());
		builder.addConnection(dataSignal.getMinus(), dataZero);
		builder.addConnection(dataZero, dataSignal.getPlus());
		builder.addConnection(dataSignal.getPlus(), dataOne);

		builder.addConnection(in.getDataReleased(), guardChangeAllowed);
		//TODO: Externalise the enviromnent specification
		builder.addConnection(guardChangeAllowed, (StgTransition)in.getActivate());
		builder.addReadArc(guardChangeAllowed, dataSignal.getMinus());
		builder.addReadArc(guardChangeAllowed, dataSignal.getPlus());
		
		StgPlace activationFinished = builder.buildPlace(); 

		StgPlace activated = builder.buildPlace(); 
		builder.addConnection(in.getActivate(), activated);
		
		for(int i=0;i<component.getOutputCount();i++)
		{
			ActiveSyncStg activateOut = (ActiveSyncStg)handshakes.get("activateOut"+i);
			
			builder.addConnection(activated, activateOut.getActivate());
			if(i == 1)
				builder.addReadArc(dataOne, activateOut.getActivate());
			else
				builder.addReadArc(dataZero, activateOut.getActivate());
			
			builder.addConnection(activateOut.getDeactivate(), activationFinished);
		}
		
		builder.addConnection(activationFinished, in.getDeactivate());
	}
}
