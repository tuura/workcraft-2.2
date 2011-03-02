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

package org.workcraft.gui.graph.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.util.GUI;
import org.workcraft.util.Function;

public class NodeGeneratorTool extends AbstractTool {
	private NodeGenerator generator;
	protected int hotKeyCode;
	private final Function<Point2D, Point2D> snap;

	public NodeGeneratorTool (NodeGenerator generator, Function<Point2D, Point2D> snap) {
		this.generator = generator;
		this.snap = snap;
	}
	
	public Icon getIcon() {
		return generator.getIcon();
	}

	public String getLabel() {
		return generator.getLabel();
	}

	public void mousePressed(GraphEditorMouseEvent e) {
		try {
			generator.generate(snap.apply(e.getPosition()));
		} catch (NodeCreationException e1) {
			throw new RuntimeException (e1);
		}
	}

	@Override
	public Expression<? extends GraphicalContent> userSpaceContent(Viewport viewport, Expression<Boolean> hasFocus) {
		return Expressions.constant(GraphicalContent.EMPTY);
	}

	@Override
	public Expression<? extends GraphicalContent> screenSpaceContent(final Viewport viewport, Expression<Boolean> hasFocus) {
		return new ExpressionBase<GraphicalContent>(){
			@Override
			protected GraphicalContent evaluate(final EvaluationContext context) {
				return new GraphicalContent(){
					@Override
					public void draw(Graphics2D g) {
						GUI.drawEditorMessage(viewport, g, Color.BLACK, "Click to create a " + generator.getLabel(), context);	
					}
				};
			}
		};
	}
	
	public int getHotKeyCode() {
		return generator.getHotKeyCode();
	}
}
