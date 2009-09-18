package org.workcraft.testing.plugins.interop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.interop.BalsaToGatesExporter;

public class BalsaToGatesExporterTests {
	@Test
	public void test() throws IOException, ModelValidationException, SerialisationException
	{
		BalsaCircuit circuit = new BalsaCircuit();
		BreezeComponent component = new BreezeComponent();
		component.setUnderlyingComponent(new While());
		circuit.add(component);
		File file = new File("balsaWhileToGates");
		FileOutputStream stream = new FileOutputStream(file);
		
		new BalsaToGatesExporter().export(circuit, stream);
		
		stream.close();
	}
}
