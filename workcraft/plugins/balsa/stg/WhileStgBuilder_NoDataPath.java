package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;

public class WhileStgBuilder_NoDataPath extends ComponentStgBuilder<While> {
	static interface WhileStgHandshakes
	{
		public ActivePullStg getGuard();
		public PassiveSyncStg getActivate();
		public ActiveSyncStg getActivateOut();
	}
	
	class WhileStgHandshakesFromCollection implements WhileStgHandshakes
	{
		private final Map<String, StgHandshake> map;

		public WhileStgHandshakesFromCollection(Map<String, StgHandshake> map)
		{
			this.map = map;
		}

		public PassiveSyncStg getActivate() {
			return (PassiveSyncStg)map.get("activate");
		}

		public ActiveSyncStg getActivateOut() {
			return (ActiveSyncStg)map.get("activateOut");
		}

		public ActivePullStg getGuard() {
			return (ActivePullStg)map.get("guard");
		}
	}
	
	static class WhileInternalStgBuilder
	{
		public static void buildStg(While component, WhileStgHandshakes handshakes, StgBuilder builder)
		{
			StgPlace activated = builder.buildPlace(); 
			StgPlace dataReady = builder.buildPlace(); 
			
			PassiveSyncStg activate = handshakes.getActivate();
			ActiveSyncStg activateOut = handshakes.getActivateOut();
			ActivePullStg guard = handshakes.getGuard();
			
			StgPlace guardChangeAllowed = builder.buildPlace(1);
			
			StgSignal guardSignal = builder.buildSignal(new SignalId(component, "dp"), false);
			final StgPlace guardOne = builder.buildPlace();
			final StgPlace guardZero = builder.buildPlace(1);
			builder.addConnection(guardOne, guardSignal.getMinus());
			builder.addConnection(guardSignal.getMinus(), guardZero);
			builder.addConnection(guardZero, guardSignal.getPlus());
			builder.addConnection(guardSignal.getPlus(), guardOne);

			builder.addConnection(guard.getDataReleaser(), guardChangeAllowed);
			builder.addConnection(guardChangeAllowed, guard.getDeactivationNotificator());
			builder.addReadArc(guardChangeAllowed, guardSignal.getMinus());
			builder.addReadArc(guardChangeAllowed, guardSignal.getPlus());
			
			// Call guard
			builder.addConnection(activate.getActivationNotificator(), activated);
			builder.addConnection(activated, guard.getActivator());
			builder.addConnection(guard.getDeactivationNotificator(), dataReady);
			
			// Activate and repeatedly call guard
			builder.addConnection(dataReady, activateOut.getActivator());
			builder.addReadArc(guardOne, activateOut.getActivator());
			builder.addConnection(activateOut.getDeactivationNotificator(), activated);
			
			// Return
			builder.addConnection(dataReady, activate.getDeactivator());
			builder.addReadArc(guardZero, activate.getDeactivator());
			
			StgTransition dataRelease = guard.getDataReleaser();
			if(dataRelease != null)
			{
				StgPlace releaseAllowed = builder.buildPlace();
				builder.addConnection(activateOut.getActivator(), releaseAllowed);
				builder.addConnection(activate.getDeactivator(), releaseAllowed);
				builder.addConnection(releaseAllowed, dataRelease); 
			}
		}
	}
	
	public void buildStg(While component, Map<String, StgHandshake> handshakes, StgBuilder builder)
	{
		WhileInternalStgBuilder.buildStg(component, new WhileStgHandshakesFromCollection(handshakes), builder);
	}
}
