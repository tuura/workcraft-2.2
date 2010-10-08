package org.workcraft.plugins.cpog.optimisation.dnf;

import java.util.Arrays;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.Literal;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

public class DnfGenerator {
	public static Dnf generate(BooleanFormula formula)
	{
		return formula.accept(new BooleanVisitor<Dnf>()
				{
					boolean negation = false;

					@Override
					public Dnf visit(And node) {
						Dnf left = node.getX().accept(this);
						Dnf right = node.getY().accept(this);
						return and(left, right);
					}

					private Dnf and(Dnf left,Dnf right) {
						return negation?addDnf(left,right):multiplyDnf(left, right);
					}

					private Dnf or(Dnf left,Dnf right) {
						return negation?multiplyDnf(left,right):addDnf(left, right);
					}

					@Override
					public Dnf visit(Iff node) {
						Dnf a = node.getX().accept(this);
						Dnf b = node.getY().accept(this);
						negation = !negation;
						Dnf na = node.getX().accept(this);
						Dnf nb = node.getY().accept(this);
						negation = !negation;
						return or(and(a,b), and(na, nb));
					}

					@Override
					public Dnf visit(Xor node) {
						Dnf a = node.getX().accept(this);
						Dnf b = node.getY().accept(this);
						negation = !negation;
						Dnf na = node.getX().accept(this);
						Dnf nb = node.getY().accept(this);
						negation = !negation;
						return or(and(a,nb), and(na, b));
					}

					private Dnf zero()
					{
						return negation ? new Dnf(new DnfClause()) : new Dnf(); 
					}
					
					@Override
					public Dnf visit(Zero node) {
						return zero();
					}

					@Override
					public Dnf visit(One node) {
						negation=!negation;
						Dnf result = zero();
						negation=!negation;
						return result;
					}

					@Override
					public Dnf visit(Not node) {
						negation = !negation;
						try{
						return node.getX().accept(this);
						}
						finally{negation=!negation;}
					}

					@Override
					public Dnf visit(Imply node) {
						negation=!negation;
						Dnf x = node.getX().accept(this);
						negation=!negation;
						Dnf y = node.getY().accept(this);
						return or(x,y);
					}

					@Override
					public Dnf visit(BooleanVariable variable) {
						return new Dnf(new DnfClause(new Literal(variable, negation)));
					}

					@Override
					public Dnf visit(Or node) {
						return or(node.getX().accept(this), node.getY().accept(this));
					}
					
				});
	}
	
	
	private static boolean compareClauses(DnfClause left, DnfClause right) {
		// returns 0 if clauses contain same literals (with same negation)
		/*for (Literal lleft: left.getLiterals()) {
			boolean found=false;
			for
		}*/
		
		return false;
	}
	
	private static Dnf addDnf(Dnf left, Dnf right) {
		Dnf result = new Dnf();
		
		result.add(left);
		
		for (DnfClause cright: right.getClauses()) {
			boolean foundSame = false;
			
			for (DnfClause cleft: left.getClauses()) {
				foundSame = compareClauses(cleft, cright);
				if (foundSame) break;
			}
			
			if (!foundSame) result.add(cright);
			
		}
		
		return result;
	}
	
	private static Dnf multiplyDnf(Dnf left, Dnf right) {
		Dnf result = new Dnf();
		for(DnfClause leftClause : left.getClauses()) {
			
			for(DnfClause rightClause : right.getClauses())
			{
				boolean foundSameLiteral;
				boolean clauseDiscarded = false;
				boolean sameNegation=false;
				
				DnfClause newClause = new DnfClause();
				
				newClause.add(leftClause.getLiterals());
				
				
				for(Literal rlit : rightClause.getLiterals()) {
					foundSameLiteral = false;
					
					for(Literal llit : leftClause.getLiterals()) {
						
						// TODO: work with 0 and 1 literals
						
						if (rlit.getVariable().getLabel().equals(
								llit.getVariable().getLabel())) {
							
							foundSameLiteral=true;
							sameNegation=llit.getNegation()==rlit.getNegation();
							break;
						}
					}

					if (!foundSameLiteral) newClause.add(rlit);
					else if (!sameNegation) {
						clauseDiscarded = true;
						break;
					}
				}
				
//				newClause.add(rightClause.getLiterals());
				if (!clauseDiscarded) result.add(newClause);
			}
			
		}
		return result;
	}

}
