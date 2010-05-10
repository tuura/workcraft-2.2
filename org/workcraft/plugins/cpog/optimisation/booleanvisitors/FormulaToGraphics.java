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
package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.BinaryBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;



public class FormulaToGraphics 
{
	public class Void{ private Void(){} }
	public static class PrinterSuite
	{
		public PrinterSuite()
		{
			iff = new IffPrinter();
			imply = new ImplyPrinter();
			or = new OrPrinter();
			xor = new XorPrinter();
			and = new AndPrinter();
			not = new NotPrinter(iff);
			constants = new ConstantPrinter();
			vars = new VariablePrinter();
			paren = new ParenthesesPrinter();
		}
		
		public void init(FontRenderContext fontRenderContext, Font font, boolean unicodeAllowed)
		{
			init(iff, imply, fontRenderContext, font, unicodeAllowed);
			init(imply, or, fontRenderContext, font, unicodeAllowed);
			init(or, xor, fontRenderContext, font, unicodeAllowed);
			init(xor, and, fontRenderContext, font, unicodeAllowed);
			init(and, not, fontRenderContext, font, unicodeAllowed);
			init(not, vars, fontRenderContext, font, unicodeAllowed);
			init(vars, constants, fontRenderContext, font, unicodeAllowed);
			init(constants, paren, fontRenderContext, font, unicodeAllowed);
			init(paren, iff, fontRenderContext, font, unicodeAllowed);
		}
		
		public void init(DelegatingPrinter printer, DelegatingPrinter next, FontRenderContext fontRenderContext, Font font, boolean unicodeAllowed)
		{
			printer.setNext(next);
			printer.setFontRenderContext(fontRenderContext);
			printer.setFont(font);
			printer.unicodeAllowed = unicodeAllowed;
		}
		
		public StringBuilder builder;
		public IffPrinter iff;
		public ImplyPrinter imply;
		public OrPrinter or;
		public XorPrinter xor;
		public AndPrinter and;
		public NotPrinter not;
		public ConstantPrinter constants;
		public VariablePrinter vars;
		public ParenthesesPrinter paren;
	}
	
	public static class DelegatingPrinter implements BooleanVisitor<FormulaRenderingResult>
	{
		public DelegatingPrinter next;
		public boolean unicodeAllowed = false;
		
		public FontRenderContext fontRenderContext;
		public Font font;
		
		public void setFontRenderContext(FontRenderContext fontRenderContext)
		{
			this.fontRenderContext = fontRenderContext;
		}

		public void setFont(Font font)
		{
			this.font = font;
		}
		
		public void setNext(DelegatingPrinter next)
		{
			this.next = next;
		}
		
		public FormulaRenderingResult print(String text)
		{
			FormulaRenderingResult result = new FormulaRenderingResult();
			
			final GlyphVector glyphs = font.createGlyphVector(fontRenderContext, text);
			
			result.boundingBox = glyphs.getLogicalBounds();
			result.visualTop = glyphs.getVisualBounds().getMinY();
			
			result.glyphs = new ArrayList<GlyphVector>();
			result.glyphs.add(glyphs);
			
			result.glyphCoordinates = new ArrayList<Point2D>();
			result.glyphCoordinates.add(new Point2D.Double(0, 0));
			
			result.inversionLines = new ArrayList<Line2D>();
			
			return result;
		}
		
		protected FormulaRenderingResult visitBinary(DelegatingPrinter printer, String opSymbol, BinaryBooleanFormula node)
		{
			FormulaRenderingResult res = node.getX().accept(printer);

			res.add(print(opSymbol));
			
			res.add(node.getY().accept(printer));
						
			return res;
		}
		
		@Override
		public FormulaRenderingResult visit(And node) {
			return next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Iff node) {
			return next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Zero node) {
			return next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(One node) {
			return next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Not node) {
			return next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Imply node) {
			return next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(BooleanVariable node) {
			return next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Or node) {
			return next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Xor node) {
			return next.visit(node);
		}
	}
	
	public static class IffPrinter extends DelegatingPrinter
	{
		@Override
		public FormulaRenderingResult visit(Iff node) {
			return visitBinary(this, " = ", node);
		}
	}
	
	public static class ImplyPrinter extends DelegatingPrinter
	{
		@Override
		public FormulaRenderingResult visit(Imply node) {
			return visitBinary(next, unicodeAllowed ? " \u21d2 " : " => ", node);
		}
	}
	
	public static class OrPrinter extends DelegatingPrinter
	{
		@Override
		public FormulaRenderingResult visit(Or node) {
			return visitBinary(this, " + ", node);
		}
	}
	
	public static class XorPrinter extends DelegatingPrinter
	{
		@Override
		public FormulaRenderingResult visit(Xor node) {
			return visitBinary(this, unicodeAllowed ? " \u2295 " : " ^ ", node);
		}
	}
	
	public static class AndPrinter extends DelegatingPrinter
	{
		@Override
		public FormulaRenderingResult visit(And node) {
			return visitBinary(this, unicodeAllowed ? " \u2022 " : " * ", node);
		}
	}
	
	public static class NotPrinter extends DelegatingPrinter
	{		
		private final IffPrinter iff;

		public NotPrinter(IffPrinter iff)
		{
			this.iff = iff;			
		}

		@Override
		public FormulaRenderingResult visit(Not node)
		{
			FormulaRenderingResult res = node.getX().accept(iff);
			
			res.inversionLines.add(new Line2D.Double(
				res.boundingBox.getMinX(), res.visualTop - font.getSize2D() / 8.0,
				res.boundingBox.getMaxX(), res.visualTop - font.getSize2D() / 8.0));
			
			res.visualTop -= font.getSize2D() / 8.0;
			
			res.boundingBox.add(new Point2D.Double(res.boundingBox.getMaxX(), res.boundingBox.getMinY() - font.getSize2D() / 8.0));
			
			return res;  
		}
	}
	
	public static class ConstantPrinter extends DelegatingPrinter
	{
		@Override
		public FormulaRenderingResult visit(One one)
		{
			return print("1");
		}
		@Override
		public FormulaRenderingResult visit(Zero zero) {
			return print("0");
		}
	}
	
	public static class VariablePrinter extends DelegatingPrinter
	{
		Map<String, BooleanVariable> varMap = new HashMap<String, BooleanVariable>();
		@Override
		public FormulaRenderingResult visit(BooleanVariable var) {
			String label = var.getLabel();
			BooleanVariable nameHolder = varMap.get(label);
			if(nameHolder == null)
				varMap.put(label, var);
			else
				if(nameHolder != var)
					throw new RuntimeException("name conflict! duplicate name " + label);
			
			return print(label);
		}
	}
	
	public static class ParenthesesPrinter extends DelegatingPrinter
	{
		@Override public FormulaRenderingResult visit(Zero node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(One node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(BooleanVariable node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(And node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(Or node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(Xor node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(Iff node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(Imply node) { return enclose(node); }
		FormulaRenderingResult enclose(BooleanFormula node)
		{
			FormulaRenderingResult res = print("(");
			res.add(node.accept(next));
			res.add(print(")"));
			return res;
		}
	}

	public static FormulaRenderingResult render(BooleanFormula formula, FontRenderContext fontRenderContext, Font font)
	{
		return render(formula, fontRenderContext, font, true);
	}	

	public static FormulaRenderingResult render(BooleanFormula formula, FontRenderContext fontRenderContext, Font font, boolean unicodeAllowed)
	{
		PrinterSuite ps = new PrinterSuite();
		ps.init(fontRenderContext, font, unicodeAllowed);
		
		return formula.accept(ps.iff);		
	}	
}
