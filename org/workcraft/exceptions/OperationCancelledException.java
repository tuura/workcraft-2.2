package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class OperationCancelledException extends Exception {
	public OperationCancelledException(String message) {
		super(message);
	}
}
