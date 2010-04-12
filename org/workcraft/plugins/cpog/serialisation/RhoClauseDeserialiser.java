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

package org.workcraft.plugins.cpog.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.cpog.RhoClause;
import org.workcraft.plugins.cpog.VisualScenario;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.plugins.cpog.optimisation.javacc.ParseException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;
import org.workcraft.util.Func;

public class RhoClauseDeserialiser implements CustomXMLDeserialiser
{
	private final class VariableResolver implements
			Func<String, BooleanVariable> {
		private final ReferenceResolver internalReferenceResolver;

		private VariableResolver(ReferenceResolver internalReferenceResolver) {
			this.internalReferenceResolver = internalReferenceResolver;
		}

		public BooleanVariable eval(String ref){ return (BooleanVariable) internalReferenceResolver.getObject(ref.substring("var_".length())); }
	}

	@Override
	public String getClassName()
	{
		return RhoClause.class.getName();
	}

	@Override
	public void finaliseInstance(Element element, Object instance, final ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) throws DeserialisationException
	{
		RhoClause rhoClause = (RhoClause) instance;

		String string = element.getAttribute("formula");
		
		BooleanFormula formula;
		try {
			formula = BooleanParser.parse(string, new VariableResolver(internalReferenceResolver));
		} catch (ParseException e) {
			throw new DeserialisationException(e);
		}
		rhoClause.setFormula(formula);
	}

	@Override
	public Object createInstance(Element element, ReferenceResolver externalReferenceResolver,
			Object... constructorParameters)
	{
		return new RhoClause();
	}

	@Override
	public void initInstance(Element element, Object instance, ReferenceResolver externalReferenceResolver,
			NodeInitialiser nodeInitialiser) throws DeserialisationException
	{

	}
}