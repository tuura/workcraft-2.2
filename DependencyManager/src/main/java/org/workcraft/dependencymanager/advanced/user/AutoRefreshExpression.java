package org.workcraft.dependencymanager.advanced.user;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.util.listeners.Listener;

/**
 * This class class is to be overridden by clients who wish to receive 
 * Observer-like notifications every time this expression is re-evaluated.
 * 
 * The notification is sent using the onEvaluate method, which has to process the event 
 * and re-establish the expression dependencies using the provided EvaluationContext.
 * 
 * @author Arseniy Alekseyev 
 */
public abstract class AutoRefreshExpression extends ExpressionBase<Null> {

	Listener handle; // to make sure it is not garbage collected
	Listener l = new Listener() {
		@Override
		public void changed() {
			eval();
		}
	};

	private void eval() {
		handle = AutoRefreshExpression.this.getValue(l).handle;
	}
	
	public AutoRefreshExpression() {
		eval();
	}
	
	protected abstract void onEvaluate(EvaluationContext context);
	
	@Override
	protected final Null evaluate(EvaluationContext context) {
		onEvaluate(context);
		return null;
	}
	
}

class Null {
	private Null(){};
}
