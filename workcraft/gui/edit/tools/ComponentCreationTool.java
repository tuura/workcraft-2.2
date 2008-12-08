package org.workcraft.gui.edit.tools;

import java.awt.Graphics2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.framework.exceptions.DuplicateIDException;
import org.workcraft.framework.exceptions.InvalidComponentException;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.gui.edit.graph.GraphEditorPane;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class ComponentCreationTool implements GraphEditorTool {
	protected Class<?> componentClass;
	
	public ComponentCreationTool (Class<?> componentClass) {
		this.componentClass = componentClass;
	}

	public void drawInUserSpace(GraphEditorPane editor, Graphics2D g) {
		// TODO Auto-generated method stub

	}

	public String getIconPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		DisplayName name = componentClass.getAnnotation(DisplayName.class);
		if (name == null)
			return "Create " + componentClass.getSimpleName();
		else
			return "Create " + name.value();
	}

	public void mouseClicked(GraphEditorMouseEvent e) {
	
	}

	public void mouseEntered(GraphEditorMouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(GraphEditorMouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseMoved(GraphEditorMouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(GraphEditorMouseEvent e) {
		Constructor<?> ctor;
		try {
			ctor = componentClass.getConstructor();
			Component comp = (Component)ctor.newInstance();
			VisualComponent vComp = (VisualComponent)PluginManager.createVisualClassFor(comp, VisualComponent.class);
			
			vComp.setX(e.getX());
			vComp.setY(e.getY());
			
			e.getEditor().getModel().getMathModel().addComponent(comp);
			e.getEditor().getModel().getRoot().add(vComp);
			e.getEditor().getModel().fireModelStructureChanged();
			
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (VisualModelConstructionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidComponentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DuplicateIDException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public void mouseReleased(GraphEditorMouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawInScreenSpace(GraphEditorPane editor, Graphics2D g) {
		// TODO Auto-generated method stub
		
	}

}
