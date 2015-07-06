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
package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt;

import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;

/**
 * Represents a boolean term where we explicitly store if the term is negated
 * or not. {@link http://en.wikipedia.org/wiki/Literal_%28mathematical_logic%29}
 * This data structure representa a term φ as a pair (φ_atom, plrty) where
 * atom is a Boolean term and plrty has either the value POSITIVE or NEGATIVE.
 * The pair (φ_atom, POSITIVE) represents the term φ_atom.
 * the pair (φ_atom, NEGATIVE) represents the term (not φ_atom).
 * We call φ_atom the atom of this literal.
 * We call plrty the polarity of this literal.
 * 
 * @author heizmann@informatik.uni-freiburg.de
 *
 */
public class Literal {
	
	public enum Polarity { POSITIVE, NEGATIVE };
	private final Term m_Atom;
	private final Polarity m_Polarity;
	
	/**
	 * Convert a Boolean term into this representation. If the input Term is
	 * negated several times, we strip all negation symbols. 
	 */
	public Literal(Term input) {
		super();
		if (!input.getSort().getName().equals("Bool")) {
			throw new IllegalArgumentException("only applicable to sort Bool");
		}
		Term withoutNegation = null;
		int removedNegationSymbols = 0;
		do {
			withoutNegation = getParameterOfNotTerm(input);
			if (withoutNegation != null) {
				input = withoutNegation;
				removedNegationSymbols++;
			}
			
		} while (withoutNegation != null);
		if (removedNegationSymbols % 2 == 0) {
			m_Polarity = Polarity.POSITIVE;
		} else {
			m_Polarity = Polarity.NEGATIVE;
		}
		m_Atom = input;
	}
	
	public Term getAtom() {
		return m_Atom;
	}

	public Polarity getPolarity() {
		return m_Polarity;
	}
	
	public Term toTerm(Script script) {
		if (m_Polarity == Polarity.POSITIVE) {
			return m_Atom;
		} else {
			return script.term("not", m_Atom);
		}
	}


	/**
	 * If term is a negation, i.e. of the form "(not φ)" return the argument
	 * of the negation φ, otherwise return null.
	 */
	public static Term getParameterOfNotTerm(Term term) {
		if (term instanceof ApplicationTerm) {
			ApplicationTerm appTerm = (ApplicationTerm) term;
			if (appTerm.getFunction().getName().equals("not")) {
				assert appTerm.getParameters().length == 1;
				return appTerm.getParameters()[0];
			}
		}
		return null;
	}

}