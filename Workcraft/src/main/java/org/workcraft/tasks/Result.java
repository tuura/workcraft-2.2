package org.workcraft.tasks;


public class Result<T> {
	
	public enum Outcome {
		FINISHED,
		CANCELLED,
		FAILED
	}
	
	private Outcome outcome;
	private Throwable cause;
	private T result;
	
	public Result(Outcome outcome) {
		this.outcome = outcome;
		this.cause = null;
		this.result = null;
	}
	
	public Result (Outcome outcome, T result) {
		this.outcome = outcome;
		this.cause = null;
		this.result = result;
	}
	
	public Result(Throwable exception) {
		this.outcome = Outcome.FAILED;
		this.cause = exception;
		this.result = null;
	}
	
	public Result(T result) {
		this.outcome = Outcome.FINISHED;
		this.cause = null;
		this.result = result;
	}
	
	public Outcome getOutcome() {
		return outcome;
	}
	
	public Throwable getCause() {
		return cause;
	}
	
	public T getReturnValue() {
		return result;		
	}

	public static <R> Result<R> exception(Throwable e) {
		return new Result<R>(e);
	}
	
	public static <R> Result<R> cancelled(){
		return new Result<R>(Outcome.CANCELLED);
	}
	
	public static <R> Result<R> finished(R res){
		return new Result<R>(res);
	}

	public static <R> Result<R> failed(R res) {
		return new Result<R>(Outcome.FAILED, res);
	}
}