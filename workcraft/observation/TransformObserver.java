package org.workcraft.observation;

import org.workcraft.dom.visual.TransformDispatcher;

public interface TransformObserver {
	public void subscribe (TransformDispatcher dispatcher);
	public void notify (TransformChangedEvent e);
}