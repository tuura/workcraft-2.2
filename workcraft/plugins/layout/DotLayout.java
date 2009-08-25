package org.workcraft.plugins.layout;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.LayoutFailedException;
import org.workcraft.framework.interop.SynchronousExternalProcess;
import org.workcraft.layout.Layout;

@DisplayName ("Dot")
public class DotLayout implements Layout {
	
	private void saveGraph(VisualModel model) throws IOException {
		PrintStream out = new PrintStream(new File(DotLayoutSettings.tmpGraphFilePath));
		out.println("digraph work {");
		out.println("graph [nodesep=\"2.0\"];");
		out.println("node [shape=box];");
		
		
		for (HierarchyNode n : model.getRoot().getChildren()) {
			if (n instanceof VisualComponent) {
				VisualComponent comp = (VisualComponent) n;
				Integer id = comp.getID();
				if(id!=null) {
					Rectangle2D bb = comp.getBoundingBoxInLocalSpace();
					if(bb!=null) {
						double width = bb.getWidth();
						double height = bb.getHeight();
						out.println("\""+id+"\" [width=\""+width+"\", height=\""+height+"\"];");
					}
					
					Set<VisualComponent> postset = comp.getPostset();
					
					for(VisualComponent target : postset) {
						Integer targetId = target.getID();
						if(targetId!=null) {
							out.println("\""+id+"\" -> \""+targetId+"\";");
						}
					}
				}
			}
		}
		out.println("}");
		out.close();
	}
	
	private static String fileToString(String path) throws IOException {
		FileInputStream f = new FileInputStream(path);
		byte[] buf = new byte[f.available()];
		f.read(buf);
		return new String(buf);
	}
	
	private void applyLayout(String in, VisualModel model) {
		Pattern regexp = Pattern.compile("\\s*\\\"?(.+)\\\"?\\s+\\[width=\\\"?(-?\\d*\\.?\\d+)\\\"?\\s*,\\s*height=\\\"?(-?\\d*\\.?\\d+)\\\"?\\s*,\\s*pos=\\\"?(-?\\d+)\\s*,\\s*(-?\\d+)\\\"?\\];\\s*\\n");
		Matcher matcher = regexp.matcher(in);
		
		while(matcher.find()) {
			Integer id = Integer.parseInt(matcher.group(1));
			
			VisualComponent comp = model.getVisualComponentByID(id);
			
			if(comp==null)
				continue;
			comp.setX(Integer.parseInt(matcher.group(4))*DotLayoutSettings.dotPositionScaleFactor);
			comp.setY(-Integer.parseInt(matcher.group(5))*DotLayoutSettings.dotPositionScaleFactor);
		}
	}
	
	private void cleanUp() {
		(new File(DotLayoutSettings.tmpGraphFilePath)).delete();
	}
	
	public void doLayout(VisualModel model) throws LayoutFailedException {
		try {
			saveGraph(model);
			SynchronousExternalProcess p = new SynchronousExternalProcess(
					new String[] {DotLayoutSettings.dotCommand, "-Tdot", "-O", DotLayoutSettings.tmpGraphFilePath}, ".");
			p.start(10000);
			if(p.getReturnCode()==0) {
				String in = fileToString(DotLayoutSettings.tmpGraphFilePath+".dot");
				applyLayout(in, model);
				cleanUp();
			}
			else {
				cleanUp();
				throw new LayoutFailedException("External process (dot) failed (code " + p.getReturnCode() +")");
			}
		} catch(IOException e) {
			cleanUp();
			throw new LayoutFailedException(e);
		}
	}

	public boolean isApplicableTo(VisualModel model) {
		return true;
	}


}
