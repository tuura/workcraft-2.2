package org.workcraft.framework;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.DocumentFormatException;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.framework.workspace.Workspace;
import org.workcraft.gui.MainWindow;
import org.xml.sax.SAXException;

public class Framework {
	public static final String FRAMEWORK_VERSION_MAJOR = "2";
	public static final String FRAMEWORK_VERSION_MINOR = "dev";

	class JavaScriptExecution implements ContextAction {
		Script script;
		Scriptable scope;
		String strScript;

		public void setScope (Scriptable scope) {
			this.scope = scope;
		}

		public void setScript (Script script) {
			this.script = script;
		}

		public void setScript (String strScript) {
			this.strScript = strScript;
		}

		public Object run(Context cx) {
			Object ret;
			if (this.script != null)
				ret = this.script.exec(cx, this.scope);
			else
				ret = cx.evaluateString(this.scope, this.strScript, "<string>", 1, null);
			this.script = null;
			this.scope = null;
			this.strScript = null;
			return ret;
		}
	}
	class JavaScriptCompilation implements ContextAction {
		String source, sourceName;
		BufferedReader reader;

		public void setSource (String source) {
			this.source = source;
		}

		public void setSource (BufferedReader reader) {
			this.reader = reader;
		}

		public void setSourceName (String sourceName) {
			this.sourceName = sourceName;

		}

		public Object run(Context cx) {
			Object ret;
			if (this.source!=null)
				ret = cx.compileString(this.source, this.sourceName, 1, null);
			else
				try {
					ret = cx.compileReader(this.reader, this.sourceName, 1, null);
				} catch (IOException e) {
					e.printStackTrace();
					ret = null;
				}
				this.source = null;
				this.sourceName = null;
				return ret;
		}
	}

	class SetArgs implements ContextAction {
		Object[] args;

		public void setArgs (Object[] args) {
			this.args = args;
		}

		public Object run(Context cx) {
			Object scriptable = Context.javaToJS(this.args, Framework.this.systemScope);
			ScriptableObject.putProperty(Framework.this.systemScope, "args", scriptable);
			Framework.this.systemScope.setAttributes("args", ScriptableObject.READONLY);
			return null;

		}
	}

	private PluginManager pluginManager;
	private ModelManager modelManager;
	private Config config ;
	private Workspace workspace;

	private ScriptableObject systemScope;
	private ScriptableObject globalScope;

	private JavaScriptExecution javaScriptExecution = new JavaScriptExecution();
	private JavaScriptCompilation javaScriptCompilation = new JavaScriptCompilation();

	private boolean inGUIMode = false;
	private boolean shutdownRequested = false;
	private boolean GUIRestartRequested = false;


	private boolean silent = false;

	private MainWindow mainWindow;

	public Framework() {
		this.pluginManager = new PluginManager(this);
		this.modelManager = new ModelManager();
		this.config = new Config();
		this.workspace = new Workspace(this);
		this.javaScriptExecution = new JavaScriptExecution();
		this.javaScriptCompilation = new JavaScriptCompilation();
	}


	/*	public void loadPlugins(String directory) {
		System.out.println("Loading plugin class manifest from \""+directory+"\"\t ...");

		pluginManager.loadManifest(directory);

		System.out.println("Verifying plugin classes\t ...");
		System.out.println("Models:");

		LinkedList<Class> models = pluginManager.getClassesBySuperclass(Document.class);
		for (Class cls : models) {
			modelManager.addModel(cls);
		}

		System.out.println("Components:");

		LinkedList<Class> components = pluginManager.getClassesBySuperclass(Component.class);
		for (Class cls : components) {
			modelManager.addComponent(cls);
		}

		LinkedList<Class> tools = pluginManager.getClassesByInterface(Tool.class);
		System.out.println("Tools:");
		for (Class cls : tools) {
			modelManager.addTool(cls, this);
		}
		System.out.println ("Load complete.\n");
	}*/

	public void loadConfig(String fileName) {
		this.config.load(fileName);
	}

	public void saveConfig(String fileName) {
		this.config.save(fileName);
	}

	public void setConfigVar (String key, String value) {
		this.config.set(key, value);
	}

	public void setConfigVar (String key, int value) {
		this.config.set(key, Integer.toString(value));
	}

	public void setConfigVar (String key, boolean value) {
		this.config.set(key, Boolean.toString(value));
	}

	public String getConfigVar (String key) {
		return this.config.get(key);
	}

	public int getConfigVarAsInt (String key, int defaultValue)  {
		String s = this.config.get(key);

		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public boolean getConfigVarAsBool (String key, boolean defaultValue)  {
		String s = this.config.get(key);

		if (s == null)
			return defaultValue;
		else
			return Boolean.parseBoolean(s);
	}

	public String[] getModelNames() {
		LinkedList<Class<?>> list = this.modelManager.getModelList();
		String a[] = new String[list.size()];
		int i=0;
		for (Class<?> cls : list)
			a[i++] = cls.getName();
		return a;
	}

	public void initJavaScript() {
		if (!this.silent)
			System.out.println ("Initialising javascript...");
		Context.call(new ContextAction() {
			public Object run(Context cx) {
				ImporterTopLevel importer = new ImporterTopLevel();
				importer.initStandardObjects(cx, false);
				Framework.this.systemScope = importer;

				//systemScope.initStandardObjects();
				//systemScope.setParentScope(

				Object frameworkScriptable = Context.javaToJS(Framework.this, Framework.this.systemScope);
				ScriptableObject.putProperty(Framework.this.systemScope, "framework", frameworkScriptable);
				//ScriptableObject.putProperty(systemScope, "importer", );
				Framework.this.systemScope.setAttributes("framework", ScriptableObject.READONLY);

				Framework.this.globalScope =(ScriptableObject) cx.newObject(Framework.this.systemScope);
				Framework.this.globalScope.setPrototype(Framework.this.systemScope);
				Framework.this.globalScope.setParentScope(null);

				return null;

			}
		});
	}

	public ScriptableObject getJavaScriptGlobalScope() {
		return this.globalScope;
	}
	
	public void setJavaScriptProperty (String name, Object object, ScriptableObject scope, boolean readOnly) {
		Object scriptable = Context.javaToJS(object, scope);
		ScriptableObject.putProperty(scope, name, scriptable);
		
		if (readOnly)
			scope.setAttributes(name, ScriptableObject.READONLY);
	}
	
	public void deleteJavaScriptProperty (String name, ScriptableObject scope) {
		ScriptableObject.deleteProperty(scope, name);
	}

	public Object execJavaScript(File file) throws FileNotFoundException {
		BufferedReader reader = new BufferedReader (new FileReader(file));
		return execJavaScript(compileJavaScript(reader, file.getPath()));
	}

	public Object execJavaScript(Script script) {
		return execJavaScript (script, this.globalScope);
	}

	public Object execJavaScript(Script script, Scriptable scope) {
		this.javaScriptExecution.setScript(script);
		this.javaScriptExecution.setScope(scope);
		return Context.call(this.javaScriptExecution);
	}

	public Object execJavaScript(String script, Scriptable scope) {
		this.javaScriptExecution.setScript(script);
		this.javaScriptExecution.setScope(scope);
		return Context.call(this.javaScriptExecution);
	}

	public Object execJavaScript (String script) {
		return execJavaScript(script, this.globalScope);
	}

	public Script compileJavaScript (String source, String sourceName) {
		this.javaScriptCompilation.setSource(source);
		this.javaScriptCompilation.setSourceName(sourceName);
		return (Script) Context.call(this.javaScriptCompilation);
	}

	public Script compileJavaScript (BufferedReader source, String sourceName) {
		this.javaScriptCompilation.setSource(source);
		this.javaScriptCompilation.setSourceName(sourceName);
		return (Script) Context.call(this.javaScriptCompilation);
	}

	public void startGUI() {
		if (this.inGUIMode) {
			System.out.println ("Already in GUI mode");
			return;
		}

		this.GUIRestartRequested = false;

		System.out.println ("Switching to GUI mode...");


		if (SwingUtilities.isEventDispatchThread()) {
			this.mainWindow = new MainWindow(Framework.this);
			this.mainWindow.startup();
		} else
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						Framework.this.mainWindow = new MainWindow(Framework.this);
						Framework.this.mainWindow.startup();
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			Context.call(new ContextAction() {
				public Object run(Context cx) {
					Object guiScriptable = Context.javaToJS(Framework.this.mainWindow, Framework.this.systemScope);
					ScriptableObject.putProperty(Framework.this.systemScope, "gui", guiScriptable);
					Framework.this.systemScope.setAttributes("gui", ScriptableObject.READONLY);
					return null;

				}
			});

			System.out.println ("Now in GUI mode.");
			this.inGUIMode = true;

	}

	public void shutdownGUI() {
		if (this.inGUIMode) {
			this.mainWindow.shutdown();
			this.mainWindow = null;
			this.inGUIMode = false;

			Context.call(new ContextAction() {
				public Object run(Context cx) {
					ScriptableObject.deleteProperty(Framework.this.systemScope, "gui");
					return null;
				}
			});

		}
		System.out.println ("Now in console mode.");
	}

	public void shutdown() {
		this.shutdownRequested = true;
	}

	public boolean shutdownRequested() {
		return this.shutdownRequested;
	}

	public MainWindow getMainWindow() {
		return this.mainWindow;
	}

	public ModelManager getModelManager() {
		return this.modelManager;
	}

	public PluginManager getPluginManager() {
		return this.pluginManager;
	}

	public Workspace getWorkspace() {
		return this.workspace;
	}

	public boolean isInGUIMode() {
		return this.inGUIMode;
	}

	public boolean isSilent() {
		return this.silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public void setArgs(List<String> args) {
		SetArgs setargs = new SetArgs();
		setargs.setArgs(args.toArray());
		Context.call(setargs);
	}

	public Model load(String path) throws ModelLoadFailedException, VisualModelConstructionException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			org.w3c.dom.Document xmldoc;
			MathModel model;
			DocumentBuilder db;

			db = dbf.newDocumentBuilder();
			xmldoc = db.parse(new File(path));

			Element xmlroot = xmldoc.getDocumentElement();

			if (xmlroot.getNodeName()!="workcraft")
				throw new ModelLoadFailedException("not a Workcraft document");

			String[] ver = xmlroot.getAttribute("version").split("\\.", 2);

			if (ver.length<2 || !ver[0].equals(FRAMEWORK_VERSION_MAJOR))
				throw new ModelLoadFailedException("document was created by another version of Workcraft");

			NodeList elements;
			elements =  xmlroot.getElementsByTagName("model");
			if (elements.getLength() != 1)
				throw new ModelLoadFailedException("<model> section missing or duplicated");

			Element modelElement = (Element)elements.item(0);
			String modelClassName = modelElement.getAttribute("class");

			Class<?> modelClass = Class.forName(modelClassName);
			Constructor<?> ctor = modelClass.getConstructor(Framework.class, Element.class, String.class);
			model = (MathModel)ctor.newInstance(this, modelElement, path);


			elements =  xmlroot.getElementsByTagName("visual-model");
			if (elements.getLength() > 1)
				throw new ModelLoadFailedException("<visual-model> section duplicated");

			if (elements.getLength() == 0)
				return model;

			Element visualModelElement = (Element)elements.item(0);
			String visualModelClassName = modelElement.getAttribute("class");

			Class<?> visualModelClass = Class.forName(visualModelClassName);
			ctor = visualModelClass.getConstructor(MathModel.class, Element.class);
			VisualModel visualModel = (VisualModel)ctor.newInstance(model, visualModelElement, path);

			return visualModel;

		} catch (ParserConfigurationException e) {
			throw new ModelLoadFailedException("XML Parser configuration error: "+ e.getMessage());
		} catch (IOException e) {
			throw new ModelLoadFailedException("IO error: "+ e.getMessage());
		} catch (SAXException e) {
			throw new ModelLoadFailedException("Parse error: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new ModelLoadFailedException("Cannot instantiate model: " + e.getMessage());
		} catch (SecurityException e) {
			throw new ModelLoadFailedException("Cannot instantiate model: " + e.getMessage());
		} catch (InstantiationException e) {
			throw new ModelLoadFailedException("Cannot instantiate model: " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ModelLoadFailedException("Cannot instantiate model: " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new ModelLoadFailedException("Cannot instantiate model: " + e.getTargetException().getMessage());
		} catch (NoSuchMethodException e) {
			throw new ModelLoadFailedException("Cannot instantiate model: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new ModelLoadFailedException("Cannot instatniate model: " + e.getMessage());
		}
	}

	public void save(Model model, String path) throws ModelSaveFailedException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document doc;
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			doc = db.newDocument();

			Element root = doc.createElement("workcraft");
			root.setAttribute("version", FRAMEWORK_VERSION_MAJOR+"."+FRAMEWORK_VERSION_MINOR);
			doc.appendChild(root);

			Element modelElement = doc.createElement("model");
			modelElement.setAttribute("class", model.getClass().getName());
			model.getMathModel().toXML(modelElement);
			root.appendChild(modelElement);

			VisualModel visualModel = model.getVisualModel();

			if (visualModel != null) {
				Element visualModelElement = doc.createElement("visual-model");
				visualModelElement.setAttribute("class", visualModel.getClass().getName());
				visualModel.toXML(visualModelElement);
				root.appendChild(visualModelElement);
			}

			TransformerFactory tFactory = TransformerFactory.newInstance();
			tFactory.setAttribute("indent-number", new Integer(2));
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			FileOutputStream fos = new FileOutputStream(path);

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new OutputStreamWriter(fos));

			transformer.transform(source, result);
			fos.close();
		} catch (ParserConfigurationException e) {
			throw new ModelSaveFailedException("XML Parser configuration error: "+ e.getMessage());
		} catch (TransformerConfigurationException e) {
			throw new ModelSaveFailedException("XML transformer configuration error: "+ e.getMessage());
		} catch (TransformerException e) {
			throw new ModelSaveFailedException("XML transformer error: "+ e.getMessage());
		} catch (FileNotFoundException e) {
			throw new ModelSaveFailedException("File not found: "+ e.getMessage());
		} catch (IOException e) {
			throw new ModelSaveFailedException("IO error: "+ e.getMessage());
		}
	}

	public void initPlugins() {
		if (!this.silent)
			System.out.println ("Loading plugins configuration...");

		try {
			this.pluginManager.loadManifest();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentFormatException e) {
			e.printStackTrace();
		}

	}

	public void restartGUI() {
		this.GUIRestartRequested = true;
		shutdownGUI();
	}

	public boolean isGUIRestartRequested() {
		return this.GUIRestartRequested;
	}
}