package org.workcraft.testing.plugins.balsa.components;

import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Test;
import org.workcraft.framework.ModelSaveFailedException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.balsa.components.FourPhaseProtocol;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.HandshakeNameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;


public class WhileTests {

	@Test
	public void Test1() throws ModelSaveFailedException, VisualModelInstantiationException
	{
		final While wh = new While();
		
		final STG stg = new STG();
		StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, new HandshakeNameProvider()
		{
			HashMap<Handshake, String> names;
			
			{
				names = new HashMap<Handshake, String>();
				for(Entry<String, Handshake> entry : wh.getHandshakes().entrySet())
				{
					names.put(entry.getValue(), entry.getKey());
				}
			}
			
			@Override
			public String getName(Handshake handshake) {
				return names.get(handshake);
			}
		});
		FourPhaseProtocol handshakeBuilder = new FourPhaseProtocol(stgBuilder);
		wh.buildStg(handshakeBuilder);
		new org.workcraft.framework.Framework().save(new VisualSTG(stg), "while.stg.work");
	}
}
