/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.layout;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.exceptions.LayoutException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.layout.generated.DotParser;
import org.workcraft.plugins.layout.generated.ParseException;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.verification.tasks.ExternalProcessTask;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;

@DisplayName ("Layout using dot")
public class DotLayout implements Tool {
	
	private final Framework framework;

	public DotLayout(Framework framework) {
		this.framework = framework;
	}
	
	private void saveGraph(VisualModel model, File file) throws IOException, ModelValidationException, SerialisationException {
		Exporter exporter = Export.chooseBestExporter(framework.getPluginManager(), model, Format.DOT);
		if (exporter == null)
			throw new RuntimeException ("Cannot find a .dot exporter for the model " + model);
		FileOutputStream out = new FileOutputStream(file);
		exporter.export(model, out);
		out.close();
	}
	
	List<Point2D> parseConnectionSpline(String pos) throws ParseException
	{
		try
		{
			ArrayList<Point2D> result = new ArrayList<Point2D>();
			Point2D end = null;
			String [] split = pos.split(" ");
			
			for(String s : split)
			{
				String [] ss = s.split(",");
				if(ss.length <2 || ss.length>3)
					throw new ParseException("bad connection position format");
				double pointsToInches = 1.0/72;
				if(ss.length == 3)
				{
					double x = Double.parseDouble(ss[1])*pointsToInches ;
					double y = Double.parseDouble(ss[2])*pointsToInches;
					Point2D p = new Point2D.Double(x,y);
					if(ss[0].equals("s"))
						result.add(0,p);
					else
						if(ss[0].equals("e"))
							end = p;
						else
							throw new ParseException("bad connection position format");
				}
				else
				{
					double x = Double.parseDouble(ss[0])*pointsToInches;
					double y = Double.parseDouble(ss[1])*pointsToInches;
					result.add(0,new Point2D.Double(x,y));
				}
			}
			
			if(end!=null)
				result.add(0,end);
			return result;
		}
		catch(NumberFormatException ex)
		{
			throw new ParseException(ex.getMessage());
		}
	}
	
	private void applyLayout(String in, final VisualModel model) {
		
		DotParser parser = new DotParser(new StringReader(in.replace("\\\n", "")));
		
		try {
			parser.graph(new DotListener()
			{
				@Override
				public void node(String id, Map<String, String> properties) {
					Node comp = model.getNodeByReference(id);
					
					if(comp!=null && comp instanceof Movable)
					{
						Movable m = (Movable)comp;
						String posStr = properties.get("pos");
						if(posStr!=null)
						{
							String [] posParts = posStr.split(",");
							if(posParts.length==2)
							{
								MovableHelper.resetTransform(m);
								MovableHelper.translate(m, 
									Double.parseDouble(posParts[0])*1.0/72,
									Double.parseDouble(posParts[1])*1.0/72);
							}
							else
							{
								System.err.println("Dot graph parse error: node 'pos' attribute has value '"+posStr+"', which is not a comma-separated pair of integers");
							}
						}
					}
				}
				
				@Override
				public void arc(String from, String to, Map<String, String> properties) {
					
					if(DotLayoutSettings.getImportConnectionsShape())
					{
						Node comp1 = model.getNodeByReference(from);
						Node comp2 = model.getNodeByReference(to);
						Set<Connection> connections = model.getConnections(comp1);
						Connection con = null;
						for(Connection c : connections)
						{
							if(c.getSecond() == comp2)
								con = c;
						}
						if(con!=null)
						{
							VisualConnection vc = (VisualConnection)con;
							vc.setConnectionType(ConnectionType.POLYLINE);
							vc.setScaleMode(ScaleMode.ADAPTIVE);
							
							Polyline poly = (Polyline)vc.getGraphic();
							poly.remove(poly.getChildren());
							List<Point2D> points;
							try {
								points = parseConnectionSpline(properties.get("pos"));
								
								for(int i=points.size()-1;i>=0;i--)
								{
									Point2D p = points.get(i);
									ControlPoint cp = new ControlPoint();
									cp.setPosition(p);
									cp.setHidden(true);
									poly.add(cp);
								}
								
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
						else
						{
							System.err.println(String.format("Unable to find a connection from %s to %s", from, to));
						}
					}
				}
			});
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void run (Model model) {
		File original = null, layout = null;
		try {
			original = File.createTempFile("work", ".dot");
			layout = File.createTempFile("worklayout", ".dot");
			
			saveGraph((VisualModel)model, original);
			
			List<String> args = new ArrayList<String>();
			args.add(DotLayoutSettings.dotCommand);
			args.add("-Tdot");
			args.add("-o");
			args.add(layout.getAbsolutePath());
			args.add(original.getAbsolutePath());
			
			Task<ExternalProcessResult> task = new ExternalProcessTask(args, new File("."));
			Result<? extends ExternalProcessResult> res = framework.getTaskManager().execute(task, "Laying out the graph...");
			
			if(res.getOutcome() == Outcome.CANCELLED)
				return;
			if(res.getOutcome() == Outcome.FAILED)
				throw new LayoutException("Failed to execute external process:\n" + res.getCause());
			if(res.getReturnValue().getReturnCode() == 0) {
				String in = FileUtils.readAllText(layout);
				applyLayout(in, (VisualModel)model);
			}
			else
				throw new LayoutException("External process (dot) failed (code " + res.getReturnValue().getReturnCode() +")\n\n"+new String(res.getReturnValue().getOutput())+"\n\n"+new String(res.getReturnValue().getErrors()));
		} catch(IOException e) {
			throw new RuntimeException(e);
		} catch (ModelValidationException e) {
			throw new RuntimeException(e);
		} catch (SerialisationException e) {
			throw new RuntimeException(e);
		} finally {
			if(original!=null)
				original.delete();
			if(layout!=null)
				layout.delete();
		}
	}

	public boolean isApplicableTo(Model model) {
		if (model instanceof VisualModel)
			return true;
		return false;
	}

	@Override
	public String getSection() {
		return "Layout";
	}
}