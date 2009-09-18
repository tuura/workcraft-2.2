package org.workcraft.testing.plugins.balsa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.LayoutFailedException;
import org.workcraft.exceptions.ModelCheckingFailedException;
import org.workcraft.exceptions.ModelSaveFailedException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.HandshakeComponent;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakes.MainHandshakeMaker;
import org.workcraft.plugins.balsa.protocols.FourPhaseProtocol_NoDataPath;
import org.workcraft.plugins.balsa.stg.MainStgBuilder;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.HandshakeNameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.interop.BalsaToStgExporter_FourPhase;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.modelchecking.DeadlockChecker;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.Import;


public class WhileTests {

	@Test
	public void Test1() throws ModelSaveFailedException, VisualModelInstantiationException, ModelCheckingFailedException, ModelValidationException, SerialisationException, IOException
	{
		final While wh = new While();
		
		final STG stg = new STG();

		final Map<String, Handshake> handshakes = MainHandshakeMaker.getHandshakes(wh);
		
		StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, new HandshakeNameProvider()
		{
			HashMap<Object, String> names;
			
			{
				names = new HashMap<Object, String>();
				for(Entry<String, Handshake> entry : handshakes.entrySet())
				{
					names.put(entry.getValue(), entry.getKey());
				}
			}
			
			public String getName(Object handshake) {
				return names.get(handshake);
			}
		});
		FourPhaseProtocol_NoDataPath handshakeBuilder = new FourPhaseProtocol_NoDataPath();
		handshakeBuilder.setStgBuilder(stgBuilder);
		MainStgBuilder.buildStg(wh, handshakes, handshakeBuilder);
		
		new DeadlockChecker().run(stg);
		
		new org.workcraft.Framework().save(new VisualSTG(stg), "while.stg.work");
	}
	
	@Test
	public void TestCombine() throws ModelSaveFailedException, VisualModelInstantiationException, InvalidConnectionException, ModelCheckingFailedException, IOException, LayoutFailedException, ModelValidationException, SerialisationException, DeserialisationException
	{
		BalsaCircuit balsa = new BalsaCircuit(); 
		
		BreezeComponent while1 = new BreezeComponent();
		while1.setUnderlyingComponent(new While());
		BreezeComponent while2 = new BreezeComponent();
		while2.setUnderlyingComponent(new While());
		
		balsa.add(while1);
		balsa.add(while2);
		
		HandshakeComponent wh1Out = while1.getHandshakeComponents().get(while1.getHandshakes().get("activateOut"));
		HandshakeComponent wh2In = while2.getHandshakeComponents().get(while2.getHandshakes().get("activate"));
		
		balsa.connect(wh1Out, wh2In);
		
		File stgFile = new File("while_while.g");
		new BalsaToStgExporter_FourPhase().export(balsa, new FileOutputStream(stgFile));
		
		final STG stg = (STG) Import.importFromFile(new DotGImporter(), stgFile);
		
		VisualSTG visualStg = new VisualSTG(stg);
		
		new org.workcraft.Framework().save(visualStg, "while_while.stg.work");
	}
}
