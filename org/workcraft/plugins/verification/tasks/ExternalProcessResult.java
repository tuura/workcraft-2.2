package org.workcraft.plugins.verification.tasks;

import java.util.HashMap;
import java.util.Map;

public class ExternalProcessResult {
	private byte[] output;
	private byte[] errors;
	private int returnCode;
	Map<String, byte[]> outputFiles;
	
	public ExternalProcessResult(int returnCode, byte[] output, byte[] errors) {
		this(returnCode, output, errors, new HashMap<String, byte[]>());
	}
	
	public ExternalProcessResult(int returnCode, byte[] output, byte[] errors, Map<String, byte[]> outputFiles) {
		this.output = output;
		this.errors = errors;
		this.returnCode = returnCode;
		this.outputFiles = outputFiles;
	}

	public byte[] getOutput() {
		return output;		
	}
	
	public byte[] getErrors() {
		return errors;		
	}
	
	public int getReturnCode() {
		return returnCode;
	}
	
	public byte[] getOutputFile(String name)
	{
		return outputFiles.get(name);
	}
}