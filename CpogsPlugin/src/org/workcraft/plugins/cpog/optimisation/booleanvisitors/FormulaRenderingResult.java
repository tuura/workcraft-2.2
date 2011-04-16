package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.workcraft.dom.visual.BoundedColorisableImage;
import org.workcraft.dom.visual.ColorisableGraphicalContent;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.util.Function;

public class FormulaRenderingResult
{
	public Rectangle2D boundingBox = null;
	
	public double visualTop = 0.0;
	
	public List<GlyphVector> glyphs = null;
	public List<Point2D> glyphCoordinates = null;
	public List<Line2D> inversionLines = null;
	
	public void add(FormulaRenderingResult summand)
	{
		for(GlyphVector glyph : summand.glyphs) glyphs.add(glyph);
		for(Point2D p : summand.glyphCoordinates)
			glyphCoordinates.add(new Point2D.Double(
					p.getX() + boundingBox.getWidth(), 
					p.getY()));
		
		for(Line2D line : summand.inversionLines)
			inversionLines.add(new Line2D.Double(
					line.getX1() + boundingBox.getWidth(), line.getY1(), 
					line.getX2() + boundingBox.getWidth(), line.getY2()));
		
		boundingBox.add(new Point2D.Double(
				boundingBox.getMaxX() + summand.boundingBox.getWidth(), 
				summand.boundingBox.getMinY()));
		
		visualTop = Math.min(visualTop, summand.visualTop);
	}
	
	public void draw(Graphics2D g, Color color)
	{
		g.setColor(color);
		
		int k = 0;
		for(GlyphVector glyph : glyphs)
		{
			Point2D pos = glyphCoordinates.get(k++);
			g.drawGlyphVector(glyph, (float) pos.getX(), (float) pos.getY());
		}
		
		g.setStroke(new BasicStroke(0.025f));
		for(Line2D line : inversionLines) g.draw(line);		
	}
	
	public BoundedColorisableImage asBoundedColorisableImage() {
		return new BoundedColorisableImage(new ColorisableGraphicalContent(){

			@Override
			public void draw(DrawRequest request) {
				FormulaRenderingResult.this.draw(request.getGraphics(), request.getColorisation().getColorisation());
			}
		}, boundingBox);
	}
	
	public static Function<FormulaRenderingResult, BoundedColorisableImage> asBoundedColorisableImage 
		= new Function<FormulaRenderingResult, BoundedColorisableImage>() {
		@Override
		public BoundedColorisableImage apply(FormulaRenderingResult argument) {
			return argument.asBoundedColorisableImage();
		}
	};
}
