package org.workcraft.framework;
import java.util.EventListener;

public interface HistoryListener extends EventListener {
	public void eventAdded (HistoryEvent event);
	public void movedToState (int index);
	public void redoHistoryDiscarded();
}