package org.workcraft.gui.edit.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JOptionPane;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.NotAnAncestorException;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class ConnectionTool extends AbstractTool {

	enum ConnectionState {
		NOTHING_SELECTED,
		FIRST_SELECTED
	}

	ConnectionState state;
	VisualComponent highlight;
	VisualComponent first;
	VisualComponent second;
	Point2D lastMouseCoords;

	public ConnectionTool () {
		state = ConnectionState.NOTHING_SELECTED;
		first = null;
		second = null;
		highlight = null;
		lastMouseCoords = new Point2D.Double();
	}

	@Override
	public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
		switch (state) {
		case NOTHING_SELECTED:
			break;
		case FIRST_SELECTED:
			Line2D line = new Line2D.Double(first.getX(), first.getY(), lastMouseCoords.getX(), lastMouseCoords.getY());
			g.setStroke(new BasicStroke((float) editor.getViewport().pixelSizeInUserSpace().getX()));
			g.setColor(Color.RED);
			g.draw(line);
			break;
		}

		g.setStroke(new BasicStroke((float)editor.getViewport().pixelSizeInUserSpace().getX()));

		if (highlight!=null) {
			g.setColor(Color.RED);
			try {
				g.draw(highlight.getBoundingBoxInAncestorSpace(editor.getModel().getRoot()));
			} catch (NotAnAncestorException e) {
				e.printStackTrace();
			}
		}

		if (first!=null) {
			g.setColor(Color.RED);
			try {
				g.draw(first.getBoundingBoxInAncestorSpace(editor.getModel().getRoot()));
			} catch (NotAnAncestorException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public String getIconPath() {
		return null;
	}

	@Override
	public String getName() {
		return "Connection tool";
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		lastMouseCoords = e.getPosition();

		highlight = e.getModel().getRoot().hitComponent(e.getPosition());

		if (highlight == first)
				highlight = null;

		e.getEditor().repaint();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {

		if (e.getButton() == MouseEvent.BUTTON1) {

			VisualComponent vc = e.getModel().getRoot().hitComponent(e.getPosition());

			switch (state) {
			case NOTHING_SELECTED:
				if (vc!=null) {
					first = vc;
					if (highlight == first)
						highlight = null;
					state = ConnectionState.FIRST_SELECTED;
				}
				break;
			case FIRST_SELECTED:
				if (vc!=null & vc!=first)
					try {
						e.getModel().connect(first, vc);

						if ((e.getModifiers() & MouseEvent.CTRL_DOWN_MASK) != 0) {
							first = vc;
							highlight = null;
						} else {
							first = null;
							state = ConnectionState.NOTHING_SELECTED;
						}
					} catch (InvalidConnectionException e1) {
						JOptionPane.showMessageDialog(null, e1.getMessage(), "Invalid connection", JOptionPane.ERROR_MESSAGE);
						first = null;
						state = ConnectionState.NOTHING_SELECTED;
					}
					break;
			}
		} else if (e.getButton() == MouseEvent.BUTTON3)
			switch (state) {
			case FIRST_SELECTED:
				state = ConnectionState.NOTHING_SELECTED;
				first = null;
				highlight = null;
				break;
			}


		e.getEditor().repaint();

	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		g.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
		String message = "";
		switch (state) {
		case NOTHING_SELECTED:
			message = "Click on the first component";
			break;
		case FIRST_SELECTED:
			message = "Click on the second component (control+click to connect continuously)";
			break;
		}
		Rectangle2D r = g.getFont().getStringBounds(message, g.getFontRenderContext());
		g.setColor(Color.BLUE);
		g.drawString (message, editor.getWidth()/2 - (int)r.getWidth()/2, editor.getHeight() - 20);
	}
}
