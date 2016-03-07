/*
 * Copyright (C) 2015 Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Claus Schaetzle (schaetzc@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE AbstractInterpretationV2 plug-in.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE AbstractInterpretationV2 plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE AbstractInterpretationV2 plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE AbstractInterpretationV2 plug-in grant you additional permission 
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.compound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.Util;
import de.uni_freiburg.informatik.ultimate.model.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.Boogie2SMT;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractState;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;

/**
 * A state in the {@link CompoundDomain}.
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * @author Claus Schaetzle (schaetzc@informatik.uni-freiburg.de)
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CompoundDomainState implements IAbstractState<CompoundDomainState, CodeBlock, IBoogieVar> {

	private final Logger mLogger;
	private final List<IAbstractState<?, CodeBlock, IBoogieVar>> mAbstractStates;
	private final List<IAbstractDomain> mDomainList;

	public CompoundDomainState(final Logger logger, final List<IAbstractDomain> domainList) {
		mLogger = logger;
		mDomainList = domainList;
		mAbstractStates = new ArrayList<>();
		for (final IAbstractDomain domain : mDomainList) {
			mAbstractStates.add(domain.createFreshState());
		}
	}

	public CompoundDomainState(final Logger logger, final List<IAbstractDomain> domainList,
	        final List<IAbstractState<?, CodeBlock, IBoogieVar>> abstractStateList) {
		if (domainList.size() != abstractStateList.size()) {
			throw new UnsupportedOperationException(
			        "The domain list size and the abstract state list size must be identical.");
		}

		mLogger = logger;
		mDomainList = domainList;
		mAbstractStates = abstractStateList;
	}

	@Override
	public CompoundDomainState addVariable(String name, IBoogieVar variable) {
		return performStateOperation(state -> state.addVariable(name, variable));
	}

	@Override
	public CompoundDomainState removeVariable(String name, IBoogieVar variable) {
		return performStateOperation(state -> state.removeVariable(name, variable));
	}

	@Override
	public CompoundDomainState addVariables(Map<String, IBoogieVar> variables) {
		return performStateOperation(state -> state.addVariables(variables));
	}

	@Override
	public CompoundDomainState removeVariables(Map<String, IBoogieVar> variables) {
		return performStateOperation(state -> state.removeVariables(variables));
	}

	@Override
	public IBoogieVar getVariableDeclarationType(String name) {
		return mAbstractStates.get(0).getVariableDeclarationType(name);
	}

	@Override
	public boolean containsVariable(String name) {
		return mAbstractStates.get(0).containsVariable(name);
	}

	@Override
	public Map<String, IBoogieVar> getVariables() {
		return mAbstractStates.get(0).getVariables();
	}

	@Override
	public CompoundDomainState patch(CompoundDomainState dominator) {
		assert mAbstractStates.size() == dominator.mAbstractStates.size();

		List<IAbstractState<?, CodeBlock, IBoogieVar>> returnList = new ArrayList<>();
		for (int i = 0; i < mAbstractStates.size(); i++) {
			returnList.add(patchInternally(mAbstractStates.get(i), dominator.mAbstractStates.get(i)));
		}

		return new CompoundDomainState(mLogger, mDomainList, returnList);
	}

	private static <T extends IAbstractState> T patchInternally(T current, T dominator) {
		return (T) current.patch(dominator);
	}

	@Override
	public boolean isEmpty() {
		return mAbstractStates.get(0).isEmpty();
	}

	@Override
	public boolean isBottom() {
		return mAbstractStates.stream().parallel().anyMatch(state -> state.isBottom());
	}

	@Override
	public boolean isEqualTo(CompoundDomainState other) {
		assert mAbstractStates.size() == other.mAbstractStates.size();

		for (int i = 0; i < mAbstractStates.size(); i++) {
			if (!isEqualToInternally(mAbstractStates.get(i), other.mAbstractStates.get(i))) {
				return false;
			}
		}

		return true;
	}

	private static <T extends IAbstractState> boolean isEqualToInternally(T current, T other) {
		return current.isEqualTo(other);
	}

	@Override
	public Term getTerm(Script script, Boogie2SMT bpl2smt) {
		return Util.and(script,
		        mAbstractStates.stream().map(state -> state.getTerm(script, bpl2smt)).toArray(i -> new Term[i]));
	}

	@Override
	public String toLogString() {
		final StringBuilder sb = new StringBuilder();

		for (final IAbstractState<?, CodeBlock, IBoogieVar> state : mAbstractStates) {
			sb.append("\n");
			sb.append(state.getClass().getSimpleName()).append(":\n").append(state.toLogString());
		}

		return sb.toString();
	}

	private CompoundDomainState performStateOperation(
	        Function<IAbstractState<?, CodeBlock, IBoogieVar>, IAbstractState<?, CodeBlock, IBoogieVar>> state) {
		return new CompoundDomainState(mLogger, mDomainList,
		        mAbstractStates.stream().map(state).collect(Collectors.toList()));
	}

	protected List<IAbstractDomain> getDomainList() {
		return mDomainList;
	}

	protected List<IAbstractState<?, CodeBlock, IBoogieVar>> getAbstractStatesList() {
		return mAbstractStates;
	}
}
