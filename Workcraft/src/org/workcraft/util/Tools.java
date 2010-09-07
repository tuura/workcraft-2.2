package org.workcraft.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Framework;
import org.workcraft.PluginInfo;
import org.workcraft.Tool;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.workspace.WorkspaceEntry;

public class Tools {
	public static ListMap<String, Pair<String, Tool>> getTools(WorkspaceEntry we, Framework framework) {
		PluginInfo[] toolsInfo = framework.getPluginManager().getPluginsImplementing(Tool.class.getName());
		ListMap<String, Pair<String, Tool>> toolSections = new ListMap<String, Pair<String, Tool>>();

		for (PluginInfo info : toolsInfo) {
			try {
				Tool tool = (Tool) framework.getPluginManager().getSingleton(info);

				if (!isApplicable(we, tool))
					continue;

				toolSections.put(tool.getSection(), new Pair <String,Tool> (info.getDisplayName(), tool));

			} catch (PluginInstantiationException e1) {
				throw new RuntimeException (e1);
			}
		}
		
		return toolSections;
	}

	private static boolean isApplicable(WorkspaceEntry we, Tool tool) {
		if (tool.isApplicableTo(we))
			return true;
		return false;
	}
	
	public static void run(WorkspaceEntry we, Tool tool) {
		if (tool.isApplicableTo(we))
			tool.run(we);
		else {
			String errorMessage = "Attempt to apply incompatible tool " + 
				tool.getClass().getName() + " to a workspace entry " + we.getWorkspacePath();
			
			throw new RuntimeException (errorMessage);
		}
	}
	
	public static List<String> getSections (ListMap<String, Pair<String, Tool>> tools) {
		LinkedList<String> list = new LinkedList<String>(tools.keySet());
		Collections.sort(list);
		return list;
	}
	
	public static List<Pair<String,Tool>> getSectionTools (String section, ListMap<String, Pair<String, Tool>> tools) {
		List<Pair<String,Tool>> sectionTools = new ArrayList<Pair<String, Tool>>(tools.get(section));

		Collections.sort(sectionTools, new Comparator<Pair<String,Tool>>() {
			@Override
			public int compare(Pair<String, Tool> o1,
					Pair<String, Tool> o2) {
				return (o1.getFirst().compareTo(o2.getFirst()));
			}
		});
		
		return sectionTools;
	}
}