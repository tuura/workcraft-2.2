package org.workcraft.dom.visual;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.math.MathNode;
import org.workcraft.framework.observation.ObservableState;
import org.workcraft.framework.observation.StateEvent;
import org.workcraft.framework.observation.StateObserver;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.shared.CommonVisualSettings;

public abstract class VisualComponent extends VisualTransformableNode implements Drawable {
	private MathNode refNode = null;
	
	private String label = "";

	private static Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	private GlyphVector labelGlyphs = null;
	private Point2D labelPosition = null;

	private Color labelColor = CommonVisualSettings.getForegroundColor();
	private Color foregroundColor = CommonVisualSettings.getForegroundColor();
	private Color fillColor = CommonVisualSettings.getFillColor();
	
	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration("Label", "getLabel", "setLabel", String.class));
		addPropertyDeclaration(new PropertyDeclaration("Label color", "getLabelColor", "setLabelColor", Color.class));
		addPropertyDeclaration(new PropertyDeclaration("Foreground color", "getForegroundColor", "setForegroundColor", Color.class));
		addPropertyDeclaration(new PropertyDeclaration("Fill color", "getFillColor", "setFillColor", Color.class));
	}

	public VisualComponent(MathNode refNode) {
		super();
		this.refNode = refNode;
		
		if (refNode instanceof ObservableState)
			((ObservableState)refNode).addObserver( new StateObserver() {
				public void notify(StateEvent e) {
					observableStateImpl.sendNotification(e);
				}
			});

		addPropertyDeclarations();

		setFillColor (CommonVisualSettings.getFillColor());
		setForegroundColor(CommonVisualSettings.getForegroundColor());
		setLabelColor(CommonVisualSettings.getForegroundColor());
	}

	public VisualComponent() {
		addPropertyDeclarations();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
		labelGlyphs = null;
	}

	public GlyphVector getLabelGlyphs(Graphics2D g) {
		if (labelGlyphs == null) {
			labelGlyphs = labelFont.createGlyphVector(g.getFontRenderContext(), label);
		}

		return labelGlyphs;
	}

	public Rectangle2D getLabelBB(Graphics2D g) {
		if (labelGlyphs == null) {
			labelGlyphs = labelFont.createGlyphVector(g.getFontRenderContext(), label);
		}

		return labelGlyphs.getVisualBounds();
	}

	protected void drawLabelInLocalSpace(Graphics2D g) {
		if (labelGlyphs == null) {
			labelGlyphs = labelFont.createGlyphVector(g.getFontRenderContext(), label);
			Rectangle2D textBB = labelGlyphs.getVisualBounds();
			Rectangle2D bb = getBoundingBoxInLocalSpace();
			labelPosition = new Point2D.Double( bb.getMinX() + ( bb.getWidth() - textBB.getWidth() ) *0.5, bb.getMaxY() + textBB.getHeight() + 0.1);
		}

		g.setColor(Coloriser.colorise(labelColor, getColorisation()));
		g.drawGlyphVector(labelGlyphs, (float)labelPosition.getX(), (float)labelPosition.getY());
	}

	public Color getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	public void draw(java.awt.Graphics2D g) {
	}

	public MathNode getReferencedComponent() {
		return refNode;
	}
}
