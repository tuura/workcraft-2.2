package org.workcraft.plugins.petri;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualComponentGroup;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

public class VisualPlace extends VisualComponent {
	protected static double size = 1;
	protected static float strokeWidth = 0.1f;
	protected static double singleTokenSize = size / 1.9;
	protected static double multipleTokenSeparation = strokeWidth / 8;
	
	protected static Color defaultBorderColor = Color.BLACK;
	protected static Color defaultFillColor = Color.WHITE;
	protected static Color defaultTokenColor = Color.BLACK;

	protected Color userBorderColor = defaultBorderColor;
	protected Color userFillColor = defaultFillColor;
	protected Color userTokenColor = defaultTokenColor;
	
	public Place getPlace() {
		return (Place)refComponent;
	}
	
	public int getTokens() {
		return getPlace().getTokens();
	}

	public void setTokens(int tokens) {
		getPlace().setTokens(tokens);
	}

	public VisualPlace(Place place, VisualComponentGroup parent) {
		super(place, parent);
		addPropertyDeclarations();
	}

	public VisualPlace(Place place, Element xmlElement, VisualComponentGroup parent) {
		super(place, xmlElement, parent);
		addPropertyDeclarations();
	}
	
	private void addPropertyDeclarations() {
		propertyDeclarations.add(new PropertyDeclaration ("Tokens", "getTokens", "setTokens", int.class));
	}

	@Override
	public void draw(Graphics2D g)
	{
		Shape shape = new Ellipse2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);

		g.setColor(Coloriser.colorise(userFillColor, colorisation));
		g.fill(shape);
		g.setColor(Coloriser.colorise(userBorderColor, colorisation));
		g.setStroke(new BasicStroke(strokeWidth));
		g.draw(shape);

		Place p = (Place)getReferencedComponent();

		if (p.tokens == 1)
		{
			shape = new Ellipse2D.Double(
					-singleTokenSize / 2,
					-singleTokenSize / 2,
					singleTokenSize,
					singleTokenSize);

			g.setColor(Coloriser.colorise(userTokenColor, colorisation));
			g.fill(shape);
		}
		else
			if (p.tokens > 1 && p.tokens < 8)
			{
				double al = Math.PI / p.tokens;
				if (p.tokens == 7) al = Math.PI / 6;

				double r = (size / 2 - strokeWidth - multipleTokenSeparation) / (1 + 1 / Math.sin(al));
				double R = r / Math.sin(al);

				r -= multipleTokenSeparation;

				for(int i = 0; i < p.tokens; i++)
				{
					if (i == 6)
						shape = new Ellipse2D.Double( -r, -r, r * 2, r * 2);
					else
						shape = new Ellipse2D.Double(
								-R * Math.sin(i * al * 2) - r,
								-R * Math.cos(i * al * 2) - r,
								r * 2,
								r * 2);

					g.setColor(Coloriser.colorise(userTokenColor, colorisation));
					g.fill(shape);
				}
			}
			else if (p.tokens > 7)
			{
				String out = Integer.toString(p.tokens);
				Font superFont = g.getFont().deriveFont((float)size/2);

				Rectangle2D rect = superFont.getStringBounds(out, g.getFontRenderContext());
				g.setFont(superFont);
				g.setColor(Coloriser.colorise(userTokenColor, colorisation));
				g.drawString(Integer.toString(p.tokens), (float)(-rect.getCenterX()), (float)(-rect.getCenterY()));
			}
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-size/2, -size/2, size, size);	}


	public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if (pointInLocalSpace.distanceSq(0, 0) < size*size/4)
			return 1;
		else
			return 0;
	}

	public Rectangle2D getBoundingBox() {
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}
}
