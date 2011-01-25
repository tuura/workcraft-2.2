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

package org.workcraft.serialisation.xml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.annotations.Annotations;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.util.ConstructorParametersMatcher;
import org.workcraft.util.XmlUtil;

class DefaultNodeDeserialiser {
	private DeserialiserFactory fac;
	private NodeInitialiser initialiser;
	private NodeFinaliser finaliser;

	public DefaultNodeDeserialiser(DeserialiserFactory factory, NodeInitialiser initialiser, NodeFinaliser finaliser) {
		this.fac = factory;
		this.initialiser = initialiser;
		this.finaliser = finaliser;
	}

	private void autoDeserialiseProperties(Element currentLevelElement,
			Object instance, Class<?> type,
			ReferenceResolver externalReferenceResolver)
			throws DeserialisationException {
		
		// type explicitly requested to be auto-serialised
		boolean autoSerialisedClass = Annotations.doAutoSerialisation(type); 

		try
		{
			List<Element> propertyElements = XmlUtil.getChildElements("property", currentLevelElement);
			HashMap<String, Element> nameMap = new HashMap<String, Element>();

			for (Element e : propertyElements)
				nameMap.put(e.getAttribute("name"), e);

			for (Method property : DefaultNodeSerialiser.getProperties(type, autoSerialisedClass))
			{
				if (!nameMap.containsKey(property.getName()))
					continue;

				Class<?> propertyType = DefaultNodeSerialiser.getPropertyType(property);
				XMLDeserialiser deserialiser = fac.getDeserialiserFor(propertyType.getName());

				if (!(deserialiser instanceof BasicXMLDeserialiser))
				{
					// no deserialiser, try to use the special case enum deserialiser
					if (propertyType.isEnum())
					{
						deserialiser = fac.getDeserialiserFor(Enum.class.getName());
						if (deserialiser == null)
							continue;
					} else
						continue;
				}

				Element element = nameMap.get(property.getName());
				Object value = ((BasicXMLDeserialiser)deserialiser).deserialise(element);

				// we know that 'setValue' accepts propertyType
				@SuppressWarnings("unchecked")
				ModifiableExpression<Object> expr = (ModifiableExpression<Object>) property.invoke(instance);
				expr.setValue(propertyType.cast(value));
			}
		} catch (IllegalArgumentException e) {
			throw new DeserialisationException(e);
		} catch (IllegalAccessException e) {
			throw new DeserialisationException(e);
		} catch (InvocationTargetException e) {
			throw new DeserialisationException(instance.getClass().getName() + " " + type.getName() + " "+ e.getMessage(), e);			
		} catch (InstantiationException e) {
			throw new DeserialisationException(e);
		}
	}

	public Object initInstance (Element element, ReferenceResolver externalReferenceResolver, StorageManager storage, Object ... constructorParameters) throws DeserialisationException {
		String className = element.getAttribute("class");

		if (className == null || className.isEmpty())
			throw new DeserialisationException("Class name attribute is not set\n" + element.toString());
		
		//System.out.println ("Initialising " + className);

		try {
			Class<?> cls = Class.forName(className);
			String shortClassName = cls.getSimpleName();
			
			Element currentLevelElement = XmlUtil.getChildElement(shortClassName, element);
			
			Object instance;
			
			// Check for a custom deserialiser first
			XMLDeserialiser deserialiser  = fac.getDeserialiserFor(className);
			
			if (deserialiser instanceof CustomXMLDeserialiser) {
				//System.out.println ("Using custom deserialiser " + deserialiser);
				instance = ((CustomXMLDeserialiser)deserialiser).createInstance(currentLevelElement, externalReferenceResolver, constructorParameters);
			} else if (deserialiser instanceof BasicXMLDeserialiser) {
				//System.out.println ("Using basic deserialiser " + deserialiser);
				instance = ((BasicXMLDeserialiser)deserialiser).deserialise(currentLevelElement);
			} else {
				//System.out.println ("Using default deserialiser " + deserialiser);
				
				// Check for incoming parameters - these may be supplied when a custom deserialiser requests
				// a sub-node to be deserialised which should know how to construct this class and pass
				// the proper constructor arguments
				
				// FIXME: brain-dead check 
				// Let's see if it is a dependent node.
				if (constructorParameters.length == 1 && DependentNode.class.isAssignableFrom(cls))  {
					// Check for the simple case when there is only one reference to the underlying model.
					String ref = currentLevelElement.getAttribute("ref");
					if (ref.isEmpty())
						// Bad luck, we probably can't do anything.
						// But let's try a default constructor just in case.
						instance = ConstructorParametersMatcher.construct(cls, storage);
					else {
						// Hooray, we've got a reference, so there is likely an appropriate constructor.
						Object refObject = externalReferenceResolver.getObject(ref);
						instance = ConstructorParametersMatcher.construct(cls, refObject, storage);
					}
				}
				else {
					Class<?>[] parameterTypes = new Class<?>[constructorParameters.length];
					for (int i=0; i<constructorParameters.length; i++)
						parameterTypes[i] = constructorParameters[i].getClass();
					Constructor<?> ctor = new ConstructorParametersMatcher().match(Class.forName(className), parameterTypes);
					instance = ctor.newInstance(constructorParameters);
				}
			}
			
			//System.out.println ("Result = " + instance);

			doInitialisation(element, instance, instance.getClass(), externalReferenceResolver);

			return instance;
		} catch (InstantiationException e) {
			throw new DeserialisationException(e);		
		} catch (IllegalAccessException e) {
			throw new DeserialisationException(e);
		} catch (ClassNotFoundException e) {
			throw new DeserialisationException(e);			
		} catch (NoSuchMethodException e) {
			throw new DeserialisationException(e);
		} catch (IllegalArgumentException e) {
			throw new DeserialisationException(e);
		} catch (InvocationTargetException e) {
			throw new DeserialisationException(e);
		}
	}

	void doInitialisation (Element element, Object instance, Class<?> currentLevel, ReferenceResolver externalReferenceResolver) throws DeserialisationException {
		Element currentLevelElement = XmlUtil.getChildElement(currentLevel.getSimpleName(), element);

		if (currentLevelElement != null)
			autoDeserialiseProperties(currentLevelElement, instance, currentLevel, externalReferenceResolver);
		
		try {
			XMLDeserialiser deserialiser = fac.getDeserialiserFor(currentLevel.getName());

			if (deserialiser instanceof CustomXMLDeserialiser)
				((CustomXMLDeserialiser)deserialiser).initInstance(currentLevelElement, instance, externalReferenceResolver, initialiser);
		} catch (InstantiationException e) {
			throw new DeserialisationException(e);
		} catch (IllegalAccessException e) {
			throw new DeserialisationException(e);
		}
		
		if (currentLevel.getSuperclass() != Object.class)
			doInitialisation(element, instance, currentLevel.getSuperclass(), externalReferenceResolver);	
	}

	void doFinalisation(Element element, Object instance,
			ReferenceResolver internalReferenceResolver, 
			ReferenceResolver externalReferenceResolver,
			Class<?> currentLevel)
	throws DeserialisationException {
		
		//System.out.println ("Finalising " + instance);
		
		Element currentLevelElement = XmlUtil.getChildElement(currentLevel.getSimpleName(), element);

		if (currentLevelElement != null)
		{
			try {
				XMLDeserialiser deserialiser = fac.getDeserialiserFor(currentLevel.getName());
				if (deserialiser instanceof CustomXMLDeserialiser) {
					//System.out.println ("Using custom deserialiser " + deserialiser);
					((CustomXMLDeserialiser)deserialiser).finaliseInstance(currentLevelElement, instance, internalReferenceResolver, externalReferenceResolver, finaliser);
				}
			} catch (InstantiationException e) {
				throw new DeserialisationException(e);
			} catch (IllegalAccessException e) {
				throw new DeserialisationException(e);
			}

			if (currentLevel.getSuperclass() != Object.class)
				doFinalisation(element, instance, internalReferenceResolver, externalReferenceResolver, currentLevel.getSuperclass());	
		}
	}

	public void finaliseInstance (Element element, Object instance, ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver) throws DeserialisationException {
		doFinalisation(element, instance, internalReferenceResolver, externalReferenceResolver, instance.getClass());	
	}
}
