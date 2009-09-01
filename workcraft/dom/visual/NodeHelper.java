package org.workcraft.dom.visual;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.HierarchyNode;

public class NodeHelper {
	@SuppressWarnings("unchecked")
	public static <T, O> Collection<T> filterByType(Collection<O> original, Class<T> type)
	{
		ArrayList<T> result = new ArrayList<T>();

		for(Object node : original)
			if(type.isInstance(node))
				result.add((T)node);
		return result;
	}
	
	public static <T> Collection<T> getChildrenOfType(HierarchyNode node, Class<T> type)
	{
		return filterByType(node.getChildren(), type);
	}
}
