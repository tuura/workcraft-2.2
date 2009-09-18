package org.workcraft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.Importer;

public class Import {
	static public Model importFromFile (Importer importer, File file) throws IOException, DeserialisationException {
		ReadableByteChannel ch = Channels.newChannel(new FileInputStream(file));
		Model model = importer.importFrom(ch);
		ch.close();
		return model;
	}
	
	static public Model importFromFile (Importer importer, String fileName) throws IOException, DeserialisationException {
		ReadableByteChannel ch = Channels.newChannel(new FileInputStream(new File(fileName)));
		Model model = importer.importFrom(ch);
		ch.close();
		return model;
	}
}
