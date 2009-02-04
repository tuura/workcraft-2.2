package org.workcraft.gui.edit.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.framework.exceptions.PasteException;
import org.workcraft.gui.edit.graph.GraphEditorPanel;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class SelectionTool extends AbstractTool implements ActionListener {
	private static final int DRAG_NONE = 0;
	private static final int DRAG_MOVE = 1;
	private static final int DRAG_SELECT = 2;

	private static final int SELECTION_ADD = 0;
	private static final int SELECTION_REMOVE = 1;

	protected Color selectionBorderColor = new Color(200, 200, 200);
	protected Color selectionFillColor = new Color(99, 130, 191, 32);
	protected Color selectionColor = selectionFillColor;
	protected Color grayOutColor = Color.LIGHT_GRAY; 

	private int drag = DRAG_NONE;
	private Point2D prevPosition;
	private Point2D startPosition;
	private Point2D snapOffset;
	private LinkedList<VisualNode> savedSelection = new LinkedList<VisualNode>();
	private int selectionMode;

	
	protected void clearSelection(VisualModel model) {
		for (VisualNode so : model.getSelection())
			so.clearColorisation();
		model.selectNone();
	}

	protected void addToSelection(VisualModel model, VisualNode so) {
		model.addToSelection(so);
		so.setColorisation(selectionColor);
	}

	protected void removeFromSelection(VisualModel model, VisualNode so) {
		model.removeFromSelection(so);
		so.clearColorisation();
	}

	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {
			if(drag!=DRAG_NONE)
				cancelDrag(e);
			if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0)
				clearSelection(model);
			VisualNode so = model.hitNode(e.getPosition());
			if(so!=null)
				if((e.getModifiers()&MouseEvent.ALT_DOWN_MASK)!=0)
					removeFromSelection(model, so);
				else
					addToSelection(model, so);
			model.fireSelectionChanged();
		}
		else if(e.getButton()==MouseEvent.BUTTON3) {
			VisualNode so = model.hitNode(e.getPosition());
			
			Point2D screenPoint = e.getEditor().getViewport().userToScreen(e.getPosition());
			
			JPopupMenu popup = so.createPopupMenu(this);

			if (popup != null)
				popup.show((GraphEditorPanel)e.getEditor(), (int)screenPoint.getX(), (int) screenPoint.getY());
		}
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(drag==DRAG_MOVE) {
			Point2D pos = new Point2D.Double(e.getX()+snapOffset.getX(), e.getY()+snapOffset.getY());
			e.getEditor().snap(pos);
			offsetSelection(e, pos.getX()-prevPosition.getX(), pos.getY()-prevPosition.getY());
			model.fireLayoutChanged();
			prevPosition = pos;
		}
		else if(drag==DRAG_SELECT) {
			LinkedList<VisualNode> hit = model.hitObjects(selectionRect(e.getPosition()));

			clearSelection(model);
			for (VisualNode so: savedSelection)
				addToSelection(model, so);

			for(VisualNode so : hit)
				if(selectionMode==SELECTION_ADD)
					addToSelection(model, so);
				else if(selectionMode==SELECTION_REMOVE)
					removeFromSelection(model, so);
			prevPosition = e.getPosition();
			e.getEditor().repaint();
		}
		else
			prevPosition = e.getPosition();
	}


	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {
			startPosition = e.getPosition();
			prevPosition = e.getPosition();
			VisualNode so = model.hitNode(e.getPosition());
			if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0 && so!=null) {
				if(!model.isObjectSelected(so)) {
					clearSelection(model);
					addToSelection(model, so);
					model.fireSelectionChanged();
				}
				drag = DRAG_MOVE;
				if(so instanceof VisualTransformableNode) {
					VisualTransformableNode node = (VisualTransformableNode) so;
					snapOffset = new Point2D.Double(node.getX()-e.getX(), node.getY()-e.getY());
					prevPosition = new Point2D.Double(node.getX(), node.getY());
				}
				else
					snapOffset = new Point2D.Double(0, 0);
			}
			else {
				savedSelection.clear();
				if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0 && so==null)
					clearSelection(model);
				if((e.getModifiers()&MouseEvent.ALT_DOWN_MASK)!=0)
					selectionMode = SELECTION_REMOVE;
				else
					selectionMode = SELECTION_ADD;
				savedSelection.addAll(model.selection());
				drag = DRAG_SELECT;
			}
		}
		else if(e.getButton()==MouseEvent.BUTTON3)
			if(drag!=DRAG_NONE)
				cancelDrag(e);
	}

	@Override
	public void mouseReleased(GraphEditorMouseEvent e) {
		VisualModel model = e.getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {
			if (drag == DRAG_SELECT)
				model.fireSelectionChanged();

			drag = DRAG_NONE;
		}
	}

	private void grayOutNotActive(VisualModel model)
	{
		model.getRoot().setColorisation(grayOutColor);
		model.getCurrentLevel().clearColorisation();
		for(VisualNode node : model.getSelection())
			node.setColorisation(selectionColor);
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			e.getModel().deleteSelection();
			e.getEditor().repaint();
		}

		if (!e.isCtrlDown())
		{
			switch (e.getKeyCode()) {
			case KeyEvent.VK_PAGE_UP:
				currentLevelUp(e);
				break;
			case KeyEvent.VK_PAGE_DOWN:
				currentLevelDown(e);
				break;
			}
		}

		if (e.isCtrlDown()) {
			switch(e.getKeyCode()){
			case KeyEvent.VK_G: 
				e.getModel().groupSelection();
				e.getEditor().repaint();
				break;
			case KeyEvent.VK_U:
				e.getModel().ungroupSelection();
				e.getEditor().repaint();
				break;
			case KeyEvent.VK_C: 
				e.getModel().copy(Toolkit.getDefaultToolkit().getSystemClipboard(), null);
				break;
			case KeyEvent.VK_X: 
				e.getModel().cut(Toolkit.getDefaultToolkit().getSystemClipboard(), null);
				break;
			case KeyEvent.VK_V:
				try {
					e.getModel().paste(Toolkit.getDefaultToolkit().getSystemClipboard(), prevPosition);
					e.getEditor().repaint();
				} catch (PasteException e1) {
					JOptionPane.showMessageDialog(null, "Paste failed:\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void currentLevelDown(GraphEditorKeyEvent e) {
		VisualNode[] selection = e.getModel().getSelection();
		if(selection.length == 1)
		{
			VisualNode selectedNode = selection[0];
			if(selectedNode instanceof VisualGroup)
				e.getModel().setCurrentLevel((VisualGroup)selectedNode);
		}
		grayOutNotActive(e.getModel());
	}

	private void currentLevelUp(GraphEditorKeyEvent e) {
		VisualGroup level = e.getModel().getCurrentLevel();
		VisualGroup parent = level.getParent();
		if(parent!=null)
		{
			e.getModel().setCurrentLevel(parent);
			e.getModel().addToSelection(level);
			grayOutNotActive(e.getModel());
		}
	}

	private void offsetSelection(GraphEditorMouseEvent e, double dx, double dy) {
		VisualModel model = e.getEditor().getModel().getVisualModel();

		for(VisualNode so : model.selection())
			if(so instanceof VisualTransformableNode) {
				VisualTransformableNode node = (VisualTransformableNode) so;
				node.setX(node.getX()+dx);
				node.setY(node.getY()+dy);
				node.firePropertyChanged("transform");
			}

		model.fireLayoutChanged();
	}

	private void cancelDrag(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(drag==DRAG_MOVE) {
			offsetSelection(e, startPosition.getX()-e.getX(), startPosition.getY()-e.getY());
		}
		else if(drag == DRAG_SELECT) {
			clearSelection(model);
			for (VisualNode so: savedSelection) 
				addToSelection(model, so);
			savedSelection.clear();
			model.fireSelectionChanged();
		}
		drag = DRAG_NONE;

	}

	private Rectangle2D selectionRect(Point2D currentPosition) {
		return new Rectangle2D.Double(
				Math.min(startPosition.getX(), currentPosition.getX()),
				Math.min(startPosition.getY(), currentPosition.getY()),
				Math.abs(startPosition.getX()-currentPosition.getX()),
				Math.abs(startPosition.getY()-currentPosition.getY())
		);
	}


	public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
		//VisualModel model = editor.getModel();
		g.setStroke(new BasicStroke((float) editor.getViewport().pixelSizeInUserSpace().getX()));

		Rectangle2D.Double rect = null;
		/*for(VisualNode vo : model.getSelection()) {

			if(vo==null)
				continue;


			Rectangle2D bb = vo.getBoundingBoxInUserSpace();

			if (bb == null) {
				System.err.println (vo.getClass().getName());
				return;
			}

			if(rect==null) {
				rect = new Rectangle2D.Double();
				rect.setRect(bb);
			}
			else
				rect.add(bb);
		}*/

		if(rect!=null) {
			g.setColor(new Color(255, 0, 0, 128));
			g.draw(rect);
		}

		if(drag==DRAG_SELECT) {
			Rectangle2D bb = selectionRect(prevPosition);
			g.setColor(selectionFillColor);
			g.fill(bb);
			g.setColor(selectionBorderColor);
			g.draw(bb);

		}
	}

	public String getIconPath() {
		return "org" + File.separator + "workcraft" + File.separator + "gui" + File.separator + "icons" + File.separator + "select.png";
	}

	public String getName() {
		return "Select";
	}

	public int getHotKeyCode() {
		return KeyEvent.VK_S;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

				
	}

}