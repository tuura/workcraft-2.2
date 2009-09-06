package org.workcraft.dom;

import java.util.Collection;
import java.util.Set;

import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.observation.ObservableHierarchy;
import org.workcraft.framework.observation.ObservableState;


public interface Model extends ObservableHierarchy, ObservableState {
	/**
	 * Sets the title of this model.
	 * @param title -- the model title.
	 */
	public void setTitle(String title);

	/**
	 * @return the title of the model as specified by the user.  
	 */
	public String getTitle();
	
	/**
	 * @return a user-friendly display name for this model, which is either
	 * read from <type>DisplayName</type> annotation, or, if the annotation
	 * is missing, taken from the name of the model class.  
	 */
	public String getDisplayName();
	
	/**
	 * <p>Creates a connection between the two nodes and adds it to the model.</p>
	 * <p>This is the preferred method to use by client code, instead of manually adding
	 * connections to the model using <code>addConnection</code></p>
	 * @param first -- the component that the connection starts from.
	 * @param second -- the component that the connection goes to.
	 * @return the newly created <type>Connection</type> object that has been 
	 * added to the model.
	 * @throws InvalidConnectionException thrown when the connection between
	 * the specified components is not allowed by this model, or if the specified
	 * components are not contained in this model.
	 */
	public Connection connect(Node first, Node second)
			throws InvalidConnectionException;

	public Node getNodeByID(int ID);

	public int getNodeID(Node node);

	/**
	 * This method may be called to test that
	 * the model is in a valid state (which is defined by the inheriting
	 * types) before some operations take place.</p>
	 * <p>If a model is valid, then this method does nothing. Otherwise,
	 * a <type>ModelValidationException</type> is thrown.</p>
	 * @throws ModelValidationException an exception giving
	 * an explanation of the problem.
	 * 
	 */
	public void validate() throws ModelValidationException;
	
	/**
	 * This method may be called to test that
	 * the connection is allowed by the model (which is defined by the inheriting
	 * types) before it is added.</p>
	 * <p>If a connection is valid, then this method does nothing. Otherwise,
	 * an <type>InvalidConnectionException</type> is thrown.</p>
	 * @param connection
	 * @throws InvalidConnectionException
	 */
	abstract public void validateConnection(Node first, Node second)
		throws InvalidConnectionException;

	public void add (Node node);
	
	public void remove (Node node);
	
	public void remove (Collection<Node> nodes);

	public Container getRoot();
	
	public Set<Node> getPostset(Node node);
	
	public Set<Node> getPreset(Node node);
	
	public Set<Connection> getConnections(Node node);
}