/*
 * Copyright (C) 2016 Numair Mansur
 * Copyright (C) 2016 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2016 University of Freiburg
 * 
 * This file is part of the ULTIMATE TraceAbstraction plug-in.
 * 
 * The ULTIMATE TraceAbstraction plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE TraceAbstraction plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE TraceAbstraction plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE TraceAbstraction plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE TraceAbstraction plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction;

import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.IAction;
import de.uni_freiburg.informatik.ultimate.result.IRelevanceInformation;

/**
 * Implementation of IRelevanceInformation that supports the non-flow-sensitive
 * and the flow-sensitive criterion.
 * @author Numair Mansur
 * @author Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 *
 */
public class RelevanceInformation implements IRelevanceInformation 
{

	private final List<IAction> m_Actions;
	private final boolean m_Criterion1UC;
	private final boolean m_Criterion1GF;
	private final boolean m_Criterion2;
	
	
	
	public RelevanceInformation(List<IAction> actions, boolean criterion1uc, 
			boolean criterion1gf, boolean criterion2) {
		super();
		m_Actions = actions;
		m_Criterion1UC = criterion1uc;
		m_Criterion1GF = criterion1gf;
		m_Criterion2 = criterion2;
	}
	
	public List<IAction> getActions() {
		return m_Actions;
	}

	public boolean getCriterion1UC() {
		return m_Criterion1UC;
	}

	public boolean getCriterion1GF() {
		return m_Criterion1GF;
	}

	public boolean getCriterion2() {
		return m_Criterion2;
	}



	@Override
	public IRelevanceInformation merge(IRelevanceInformation... relevanceInformations) {
		boolean criterion1uc = false;
		boolean criterion1gf = false;
		boolean criterion2 = false;
		List<IAction> actions = new ArrayList<>();
		for (IRelevanceInformation iri : relevanceInformations) {
			RelevanceInformation ri = (RelevanceInformation) iri;
			criterion1uc |= ri.getCriterion1UC();
			criterion1gf |= ri.getCriterion1GF();
			criterion2 |= ri.getCriterion2();
			actions.addAll(ri.getActions());
		}
		return new RelevanceInformation(actions, criterion1uc, criterion1gf, criterion2);
	}

	@Override
	public String getShortString() {
		
		if (!getCriterion1UC() && !getCriterion1GF() && !getCriterion2()) {
			return "-";
		} else {
			final StringBuilder sb = new StringBuilder();
			if (getCriterion1UC()) {
				sb.append("*");
			}
			if (getCriterion1GF()) {
				sb.append("@");
			}
			if (getCriterion2()) {
				sb.append("#");
			}
			return sb.toString();
		}
	}

	@Override
	public String toString() {
		return "RelevanceInformation " + getShortString() + getActions();
	}
	
	

}
