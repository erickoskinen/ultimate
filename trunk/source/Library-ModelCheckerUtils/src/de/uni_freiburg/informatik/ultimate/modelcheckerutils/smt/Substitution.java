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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_freiburg.informatik.ultimate.logic.FormulaUnLet;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.logic.Util;


/**
 * Substitutes TermVariables by Terms. Takes care that no quantified 
 * TermVariable is substituted. 
 * 
 * @author Matthias Heizmann
 *
 */
public class Substitution {
	private final Map<TermVariable,Term> m_Mapping;
	private final Script m_Script;
	
	private final static boolean USE_SAFE_SUBSTITUTION = true;

	public Substitution(Map<TermVariable, Term> mapping, Script script) {
		m_Mapping = mapping;
		assert SmtUtils.neitherKeyNorValueIsNull(mapping) : "null in substitution";
		m_Script = script;
	}
	
	public Term transform(Term term) {
		Term result = withLet(term);
		if (USE_SAFE_SUBSTITUTION) {
			Term resultSS = withSS(term);
			assert (Util.checkSat(m_Script, 
					m_Script.term("distinct", result, resultSS)) != LBool.SAT) : 
						"Bug in safe substitution.";
			result = resultSS;
		}
		return result;
	}
	
	private Term withLet(Term term) {
		TermVariable[] vars = new TermVariable[m_Mapping.size()];
		Term[] values = new Term[m_Mapping.size()];
		int i=0;
		for (Entry<TermVariable, Term> entry : m_Mapping.entrySet()) {
			vars[i] = entry.getKey();
			assert vars[i] != null : "substitution of null";
			values[i] = entry.getValue(); 
			assert values[i] != null : "substitution by null";
			i++;
		}
		Term result = m_Script.let(vars, values, term);
		result = new FormulaUnLet().unlet(result);
		return result;
	}
	
	private Term withSS(Term term) {
		Map<Term, Term> mapping = new HashMap<Term, Term>();
		for (Entry<TermVariable, Term> entry : m_Mapping.entrySet()) {
			mapping.put(entry.getKey(), entry.getValue());
		}
		Term result = (new SafeSubstitution(m_Script, mapping)).transform(term);
		return result;
	}

}
