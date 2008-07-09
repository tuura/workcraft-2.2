package org.workcraft.framework;

import java.io.File;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DocumentFormatException;
import org.workcraft.gui.workspace.FileFilters;
import org.workcraft.util.XmlUtil;


public class Document {
	private String path;
	private String extension = null;
	private boolean exists = false;
	
	public Document(String path) {
		this.path = path;
		checkFile();
	}
	
	public Document(Element element) throws DocumentFormatException {
		path = XmlUtil.readStringAttr(element, "path");
		if(path.isEmpty())
			throw(new DocumentFormatException());
		checkFile();
	}
	
	public String getPath() {
		return path;
	}
	
	public String getName() {
		return (new File(path)).getName();
	}
	
	public String toString() {
		return getName(); // TODO more options?
	}
	
	public boolean checkFile() {
		File file = new File(path);
		path = file.getPath();
		exists = file.exists() && file.isFile();
		if(!exists)
			return false;
		extension = path.substring(path.lastIndexOf('.')+1);
		return true;
	}
	
	public boolean exists() {
		return exists;
	}
	
	public String getExtension() {
		return extension;
	}
	
	public boolean isWorkDocument() {
		return extension.equalsIgnoreCase(FileFilters.DOCUMENT_EXTENSION);
	}

	public void toXml(Element element) {
		XmlUtil.writeStringAttr(element, "path", path);
	}
}
