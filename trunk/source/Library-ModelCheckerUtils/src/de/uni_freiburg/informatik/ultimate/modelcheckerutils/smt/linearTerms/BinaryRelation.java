/*
 * Copyright (C) 2012-2015 University of Freiburg
 *
 * This file is part of the ULTIMATE Model Checker Utils Library.
 *
 * The ULTIMATE Model Checker Utils Library is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU Lesser General 
 * Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 *
 * The ULTIMATE Model Checker Utils Library is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Model Checker Utils Library. If not,
 * see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Model Checker Utils Library, or any covered work, 
 * by linking or combining it with Eclipse RCP (or a modified version of
 * Eclipse RCP), containing parts covered by the terms of the Eclipse Public
 * License, the licensors of the ULTIMATE Model Checker Utils Library grant you
 * additional permission to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearTerms;

import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;

/**
 * Helper class that can be used to detect if a relation has certain form.
 * @author Matthias Heizmann
 *
 */
public abstract class BinaryRelation {

	public enum RelationSymbol {
	    EQ("="),
	    DISTINCT("distinct"),
	    LEQ("<="),
	    GEQ(">="),
	    LESS("<"),
	    GREATER(">");
	    
	    private final String m_StringRepresentation;
	    
	    RelationSymbol(String stringRepresentation) {
	    	this.m_StringRepresentation = stringRepresentation;
	    }
	
	    @Override
	    public String toString() {
	        return m_StringRepresentation;
	    }
	}
	
	/**
	 * Given a relation symbol ▷, returns the relation symbol ◾ such that the
	 * relation ψ ◾ φ is equivalent to the relation ¬(ψ ▷ φ), which is the 
	 * negated relation.
	 */
	public static RelationSymbol negateRelation(RelationSymbol symb) {
		final RelationSymbol result;
		switch (symb) {
		case EQ:
			result = RelationSymbol.DISTINCT;
			break;
		case DISTINCT:
			result = RelationSymbol.EQ;
			break;
		case LEQ:
			result = RelationSymbol.GREATER;
			break;
		case GEQ:
			result = RelationSymbol.LESS;
			break;
		case LESS:
			result = RelationSymbol.GEQ;
			break;
		case GREATER:
			result = RelationSymbol.LEQ;
			break;
		default:
			throw new UnsupportedOperationException("unknown numeric relation");
		}
		return result;
	}
	
	/**
	 * Given a relation symbol ▷, returns the relation symbol ◾ such that the
	 * relation ψ ◾ φ is equivalent to the relation φ ▷ ψ, which is the relation
	 * where we swaped the parameters.
	 */
	public static RelationSymbol swapParameters(RelationSymbol symb) {
		final RelationSymbol result;
		switch (symb) {
		case EQ:
			result = RelationSymbol.EQ;
			break;
		case DISTINCT:
			result = RelationSymbol.DISTINCT;
			break;
		case LEQ:
			result = RelationSymbol.GEQ;
			break;
		case GEQ:
			result = RelationSymbol.LEQ;
			break;
		case LESS:
			result = RelationSymbol.GREATER;
			break;
		case GREATER:
			result = RelationSymbol.LESS;
			break;
		default:
			throw new UnsupportedOperationException("unknown numeric relation");
		}
		return result;
	}
	
	/**
	 * Returns the term (relationSymbol lhsTerm rhsTerm) if relationSymbol is
	 * not a greater-than relation symbol. Otherwise returns an equivalent
	 * term where relation symbol and parameters are swapped.
	 */
	public static Term constructLessNormalForm(Script script, 
			RelationSymbol relationSymbol, Term lhsTerm, Term rhsTerm)
			throws AssertionError {
		Term result;
		switch (relationSymbol) {
		case DISTINCT:
		case EQ:
		case LEQ:
		case LESS:
			result = script.term(relationSymbol.toString(), lhsTerm, rhsTerm);
			break;
		case GEQ:
		case GREATER:
			RelationSymbol swapped = BinaryRelation.swapParameters(relationSymbol);
			result = script.term(swapped.toString(), rhsTerm, lhsTerm);
			break;
		default:
			throw new AssertionError("unknown relation symbol");
		}
		return result;
	}
	
	

	protected final RelationSymbol m_RelationSymbol;
	protected final Term m_Lhs;
	protected final Term m_Rhs;
	

	protected BinaryRelation(RelationSymbol relationSymbol, Term lhs, Term rhs) {
		super();
		m_RelationSymbol = relationSymbol;
		m_Lhs = lhs;
		m_Rhs = rhs;
	}

	public BinaryRelation(Term term) throws NoRelationOfThisKindException {
		if (!(term instanceof ApplicationTerm)) {
			throw new NoRelationOfThisKindException("no ApplicationTerm");
		}
		ApplicationTerm appTerm = (ApplicationTerm) term;
		String functionSymbolName = appTerm.getFunction().getName();
		Term[] params = appTerm.getParameters();
		boolean isNegated;
		if (functionSymbolName.equals("not")) {
			assert params.length == 1;
			Term notTerm = params[0];
			if (!(notTerm instanceof ApplicationTerm)) {
				throw new NoRelationOfThisKindException("no ApplicationTerm");
			}
			isNegated = true;
			appTerm = (ApplicationTerm) notTerm;
			functionSymbolName = appTerm.getFunction().getName();
			params = appTerm.getParameters();
		} else {
			isNegated = false;
		}
		if (appTerm.getParameters().length != 2) {
			throw new NoRelationOfThisKindException("not binary");
		}
		checkSort(appTerm.getParameters());

		
		RelationSymbol relSymb = getRelationSymbol(functionSymbolName, isNegated);
		for (RelationSymbol symb : RelationSymbol.values()) {
			if (symb.toString().equals(functionSymbolName)) {
				relSymb = isNegated ? negateRelation(symb) : symb;
				break;
			}
		}
		if (relSymb == null) {
			throw new NoRelationOfThisKindException(
					"no binary numeric relation symbol");
		} else {
			m_RelationSymbol = relSymb;
			m_Lhs = params[0];
			m_Rhs = params[1];
		}
	}
	
	/**
	 * Check if Sort of parameters is compatible. Throw Exception if not.
	 * @throws NoRelationOfThisKindException
	 */
	abstract protected void checkSort(Term[] params) 
			throws NoRelationOfThisKindException;
	
	/**
	 * Return the RelationSymbol for this relation resolve negation
	 * @param functionSymbolName function symbol name of the original term
	 * @param isNegated true iff the original term is negated
	 * @throws NoRelationOfThisKindException
	 */
	abstract protected RelationSymbol getRelationSymbol(
			String functionSymbolName, boolean isNegated) 
					throws NoRelationOfThisKindException;

	public RelationSymbol getRelationSymbol() {
		return m_RelationSymbol;
	}

	public Term getLhs() {
		return m_Lhs;
	}

	public Term getRhs() {
		return m_Rhs;
	}
	
	public static class NoRelationOfThisKindException extends Exception {

		private static final long serialVersionUID = 1L;

		public NoRelationOfThisKindException(String message) {
			super(message);
		}
	}

}