package org.workcraft.annotations;

import org.workcraft.dom.visual.CustomToolButtons;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditorMouseListener;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class Annotations {
	public static boolean doAutoSerialisation(Class<?> cls) {
		return cls.getAnnotation(NoAutoSerialisation.class) != null;
	}

	public static String getDisplayName(Class<?> cls) {
		DisplayName dna =  cls.getAnnotation(DisplayName.class);
		if (dna == null)
			return cls.getSimpleName();
		return dna.value();
	}

	public static Class<?>[] getDefaultCreateButtons(Class<? extends VisualModel> cls) {
		DefaultCreateButtons dcba = cls.getAnnotation(DefaultCreateButtons.class);
		if (dcba == null)
			return new Class<?>[0];
		return dcba.value();
	}

	public static Class<?>[] getNoDefaultCreateButtons(Class<? extends VisualModel> cls) {
		NoDefaultCreateButtons ndcba = cls.getAnnotation(NoDefaultCreateButtons.class);
		if (ndcba == null)
			return new Class<?>[0];
		return ndcba.value();
	}

	public static int getHotKeyCode (Class <?> cls) {
		Hotkey hkd = cls.getAnnotation(Hotkey.class);
		if (hkd == null)
			return -1;
		else
			return hkd.value();
	}

	public static Class<?> getVisualClass(Class <?> cls) {
		VisualClass vcat = cls.getAnnotation(VisualClass.class);
		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;
		else
			try {
				return Class.forName(vcat.value());
			} catch (ClassNotFoundException e) {
				return null;
			}
	}
	
	public static Class<? extends CustomToolsProvider> getCustomToolsProvider(Class<?> cls) {
		CustomTools ctb = cls.getAnnotation(CustomTools.class);
		if (ctb == null)
			return null;
		else
			return ctb.value();	
	}		
	
	public static Class<? extends GraphEditorMouseListener>[] getMouseListeners(Class<?> cls) {
		MouseListeners ml = cls.getAnnotation(MouseListeners.class);
		if (ml == null)
			return null;
		else
			return ml.value();	
	}	
	
	@SuppressWarnings("unchecked")
	public static Class<? extends GraphEditorTool>[] getCustomTools(Class<?> cls) {
		CustomToolButtons ctb = cls.getAnnotation(CustomToolButtons.class);
		if (ctb == null)
			return (Class<? extends GraphEditorTool>[]) new Class<?>[0];
		else
			return ctb.value();	
	}
}