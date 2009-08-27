package org.workcraft.framework.serialisation.xml;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.util.XmlUtil;

class NodeDeserialiser {
	private DeserialiserFactory fac;

	public NodeDeserialiser(DeserialiserFactory factory) {
		this.fac = factory;
	}

	private void autoDeserialiseProperties (Element currentLevelElement, Object instance, Class<? > currentLevel) throws DeserialisationException {
		if (currentLevel.getAnnotation(NoAutoSerialisation.class) != null)
			return;

		try
		{
			List<Element> propertyElements = XmlUtil.getChildElements("property", currentLevelElement);
			HashMap<String, Element> nameMap = new HashMap<String, Element>();

			for (Element e : propertyElements)
				nameMap.put(e.getAttribute("name"), e);

			BeanInfo info = Introspector.getBeanInfo(currentLevel, currentLevel.getSuperclass());

			for (PropertyDescriptor desc : info.getPropertyDescriptors())
			{
				if (!nameMap.containsKey(desc.getName()))
					continue;

				if (desc.getPropertyType() == null)
					continue;

				if (desc.getWriteMethod() == null || desc.getReadMethod() == null)
					continue;

				// property explicitly requested to be excluded from auto serialisation
				if (
						desc.getReadMethod().getAnnotation(NoAutoSerialisation.class) != null ||
						desc.getWriteMethod().getAnnotation(NoAutoSerialisation.class) != null
				)
					continue;

				// the property is writable and is not of array type, try to get a deserialiser
				XMLDeserialiser deserialiser = fac.getDeserialiserFor(desc.getPropertyType().getName());

				if (!(deserialiser != null  && deserialiser instanceof BasicXMLDeserialiser))
				{
					// no deserialiser, try to use the special case enum deserialiser
					if (desc.getPropertyType().isEnum())
					{
						deserialiser = fac.getDeserialiserFor(Enum.class.getName());
						if (deserialiser == null)
							continue;
					} else
						continue;
				}

				Element element = nameMap.get(desc.getName());
				Object value = ((BasicXMLDeserialiser)deserialiser).deserialise(element);

				desc.getWriteMethod().invoke(instance, value);
			}
		} catch (IllegalArgumentException e) {
			throw new DeserialisationException(e);
		} catch (IllegalAccessException e) {
			throw new DeserialisationException(e);
		} catch (InvocationTargetException e) {
			throw new DeserialisationException(e);			
		} catch (InstantiationException e) {
			throw new DeserialisationException(e);			
		} catch (IntrospectionException e) {
			throw new DeserialisationException(e);
		}
	}

	public Object initInstance (Element element) throws DeserialisationException {
		String className = element.getAttribute("class");

		if (className == null || className.isEmpty())
			throw new DeserialisationException("Class name attribute is not set\n" + element.toString());

		try {
			XMLDeserialiser deserialiser  = fac.getDeserialiserFor(className);
			Object instance;

			if (deserialiser instanceof CustomXMLDeserialiser) {
				instance = ((CustomXMLDeserialiser)deserialiser).initInstance(element);
			} else if (deserialiser instanceof BasicXMLDeserialiser) {
				instance = ((BasicXMLDeserialiser)deserialiser).deserialise(element);
			} else {
				instance = Class.forName(className).newInstance();
			}

			doInitialisation(element, instance, instance.getClass());

			return instance;

		} catch (InstantiationException e) {
			throw new DeserialisationException(e);		
		} catch (IllegalAccessException e) {
			throw new DeserialisationException(e);
		} catch (ClassNotFoundException e) {
			throw new DeserialisationException(e);			
		}
	}

	private void doInitialisation (Element element, Object instance, Class<?> currentLevel) throws DeserialisationException {
		Element currentLevelElement = XmlUtil.getChildElement(currentLevel.getSimpleName(), element);

		if (currentLevelElement != null)
		{
			autoDeserialiseProperties(currentLevelElement, instance, currentLevel);
			
			try {
				XMLDeserialiser deserialiser = fac.getDeserialiserFor(currentLevel.getName());
				if (deserialiser instanceof AbstractXMLDeserialiser)
					((AbstractXMLDeserialiser)deserialiser).deserialise(currentLevelElement, instance);
			} catch (InstantiationException e) {
				throw new DeserialisationException(e);
			} catch (IllegalAccessException e) {
				throw new DeserialisationException(e);
			}
			
			if (currentLevel.getSuperclass() != Object.class)
				doInitialisation(element, instance, currentLevel.getSuperclass());	
		}
	}

	private void doFinalisation (Element element, Object instance, ReferenceResolver referenceResolver, Class<?> currentLevel) throws DeserialisationException	{
		Element currentLevelElement = XmlUtil.getChildElement(currentLevel.getSimpleName(), element);

		if (currentLevelElement != null)
		{
			try {
				XMLDeserialiser deserialiser = fac.getDeserialiserFor(currentLevel.getName());
				if (deserialiser instanceof CustomXMLDeserialiser)
					((CustomXMLDeserialiser)deserialiser).finaliseInstance(element, instance, referenceResolver);
				else if (deserialiser instanceof ReferencingXMLDeserialiser)
					((ReferencingXMLDeserialiser)deserialiser).deserialise(element, instance, referenceResolver);
			} catch (InstantiationException e) {
				throw new DeserialisationException(e);
			} catch (IllegalAccessException e) {
				throw new DeserialisationException(e);
			}
			
			if (currentLevel.getSuperclass() != Object.class)
				doInitialisation(element, instance, currentLevel.getSuperclass());	
		}
	}

	public void finaliseInstance (Element element, Object instance, ReferenceResolver referenceResolver) throws DeserialisationException {
		doFinalisation(element, instance, referenceResolver, instance.getClass());	
	}
}
