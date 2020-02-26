/*
 * Copyright (C) 2020 heizmann@informatik.uni-freiburg.de
 * Copyright (C) 2020 University of Freiburg
 *
 * This file is part of the ULTIMATE Automata Library.
 *
 * The ULTIMATE Automata Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE Automata Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Automata Library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE Automata Library grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.automata.petrinet.operations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.AutomataOperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.AutomataOperationStatistics;
import de.uni_freiburg.informatik.ultimate.automata.StatisticsType;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.INwaInclusionStateFactory;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.IPetriNet;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.ITransition;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.PetriNetNot1SafeException;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.UnaryNetOperation;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.netdatastructures.BoundedPetriNet;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.netdatastructures.PetriNetUtils;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.unfolding.BranchingProcess;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.unfolding.Condition;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.unfolding.Event;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.unfolding.FinitePrefix;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.unfolding.ICoRelation;
import de.uni_freiburg.informatik.ultimate.automata.statefactory.IPetriNet2FiniteAutomatonStateFactory;
import de.uni_freiburg.informatik.ultimate.automata.statefactory.IStateFactory;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.HashRelation;

/**
 * @author heizmann@informatik.uni-freiburg.de
 *
 * @param <LETTER>
 *            Type of letters in alphabet of Petri net
 * @param <PLACE>
 *            Type of places in Petri net
 * @param <CRSF>
 *            Type of factory needed to check the result of this operation in
 *            {@link #checkResult(CRSF)}
 */
public class RemoveRedundantFlow<LETTER, PLACE, CRSF extends IStateFactory<PLACE> & IPetriNet2FiniteAutomatonStateFactory<PLACE> & INwaInclusionStateFactory<PLACE>>
		extends UnaryNetOperation<LETTER, PLACE, CRSF> {

	private final IPetriNet<LETTER, PLACE> mOperand;
	private BranchingProcess<LETTER, PLACE> mFinPre;
	final HashRelation<ITransition<LETTER, PLACE>, PLACE> mRedundantSelfloopFlow = new HashRelation<>();
	private final BoundedPetriNet<LETTER, PLACE> mResult;
	private Set<PLACE> mRedundantPlaces;

	public RemoveRedundantFlow(final AutomataLibraryServices services, final IPetriNet<LETTER, PLACE> operand)
			throws AutomataOperationCanceledException, PetriNetNot1SafeException {
		this(services, operand, null);
	}

	public RemoveRedundantFlow(final AutomataLibraryServices services, final IPetriNet<LETTER, PLACE> operand,
			final BranchingProcess<LETTER, PLACE> finPre)
			throws AutomataOperationCanceledException, PetriNetNot1SafeException {
		super(services);
		mOperand = operand;
		printStartMessage();
		if (finPre != null) {
			mFinPre = finPre;
		} else {
			mFinPre = new FinitePrefix<LETTER, PLACE>(services, operand).getResult();
		}
		final HashRelation<ITransition<LETTER, PLACE>, PLACE> redundantFlow = new HashRelation<>();
		for (final ITransition<LETTER, PLACE> t : operand.getTransitions()) {
			for (final PLACE p : operand.getPredecessors(t)) {
				final boolean isFlowRedundant = isFlowRedundant(t, p, redundantFlow);
				if (isFlowRedundant) {
					redundantFlow.addPair(t, p);
					if (operand.getSuccessors(t).contains(p)) {
						mRedundantSelfloopFlow.addPair(t, p);
					}
				}
			}
		}
		mRedundantPlaces = operand.getPlaces().stream()
				.filter(x -> isRedundantPlace(x, operand, redundantFlow)).collect(Collectors.toSet());
		mResult = copy(mOperand, mRedundantSelfloopFlow, mRedundantPlaces);
		printExitMessage();
	}

	private boolean isRedundantPlace(final PLACE p, final IPetriNet<LETTER, PLACE> operand,
			final HashRelation<ITransition<LETTER, PLACE>, PLACE> redundantFlow) {
		if (operand.isAccepting(p)) {
			return false;
		}
		final Set<ITransition<LETTER, PLACE>> succTrans = operand.getSuccessors(p);
		if (succTrans.isEmpty()) {
			// TODO 20200225 Matthias: At the moment places without successor transitions
			// are not considered redundant. Otherwise we would produce transitions without
			// successor which are not yet supported by the unfolding.
			return false;
		}
		for (final ITransition<LETTER, PLACE> t : succTrans) {
			if (!redundantFlow.containsPair(t, p)) {
				return false;
			}
		}
		return true;
	}

	private BoundedPetriNet<LETTER, PLACE> copy(final IPetriNet<LETTER, PLACE> operand,
			final HashRelation<ITransition<LETTER, PLACE>, PLACE> redundantSelfloopFlow, final Set<PLACE> redundantPlaces) {
		final BoundedPetriNet<LETTER, PLACE> result = new BoundedPetriNet<>(mServices, operand.getAlphabet(), false);
		for (final PLACE p : operand.getPlaces()) {
			if (!redundantPlaces.contains(p)) {
				result.addPlace(p, operand.getInitialPlaces().contains(p), operand.isAccepting(p));
			}
		}
		for (final ITransition<LETTER, PLACE> t : operand.getTransitions()) {
			final HashSet<PLACE> preds = new HashSet<>(operand.getPredecessors(t));
			preds.removeAll(redundantSelfloopFlow.getImage(t));
			preds.removeAll(redundantPlaces);
			final HashSet<PLACE> succs = new HashSet<>(operand.getSuccessors(t));
			succs.removeAll(redundantSelfloopFlow.getImage(t));
			succs.removeAll(redundantPlaces);
			result.addTransition(t.getSymbol(), preds, succs);
		}
		return result;
	}

	private boolean isFlowRedundant(final ITransition<LETTER, PLACE> t, final PLACE redundancyCandidate,
			final HashRelation<ITransition<LETTER, PLACE>, PLACE> redundantFlow)
			throws AutomataOperationCanceledException {
		for (final PLACE p : mOperand.getPredecessors(t)) {
			if (!p.equals(redundancyCandidate)) {
				if (redundantFlow.containsPair(t, p)) {
					// do nothing
					// must not use flow that is already marked for removal
				} else {
					final boolean isRestrictorPlace = isRestrictorPlace(redundancyCandidate, p);
					if (isRestrictorPlace) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isRestrictorPlace(final PLACE redundancyCandidate, final PLACE restrictorCandidate)
			throws AutomataOperationCanceledException {
		for (final Condition<LETTER, PLACE> restrictorCondition : mFinPre.place2cond(restrictorCandidate)) {
			final boolean isRestrictorCondition = isRestrictorCondition(restrictorCondition, redundancyCandidate,
					mFinPre.getCoRelation());
			if (!isRestrictorCondition) {
				return false;
			}
		}
		return true;
	}

	private boolean isRestrictorCondition(final Condition<LETTER, PLACE> restrictorCandidateCondition,
			final PLACE redundancyCandidate, final ICoRelation<LETTER, PLACE> coRelation)
			throws AutomataOperationCanceledException {
		if (timeout()) {
			throw new AutomataOperationCanceledException(getClass());
		}
		final Optional<Condition<LETTER, PLACE>> redundancyCandidateCondition = restrictorCandidateCondition
				.getPredecessorEvent().getConditionMark().stream().filter(x -> x.getPlace().equals(redundancyCandidate))
				.findAny();
		if (!redundancyCandidateCondition.isPresent()) {
			return false;
		}
		final boolean isRestrictorCondition = redundancyCandidateCondition.get().getSuccessorEvents().stream()
				.allMatch(x -> !coRelation.isInCoRelation(restrictorCandidateCondition, x));
		return isRestrictorCondition;
	}

	private HashRelation<ITransition<LETTER, PLACE>, Event<LETTER, PLACE>> computeTransitionEventRelation(
			final Collection<Event<LETTER, PLACE>> events) {
		final HashRelation<ITransition<LETTER, PLACE>, Event<LETTER, PLACE>> result = new HashRelation<>();
		for (final Event<LETTER, PLACE> e : events) {
			result.addPair(e.getTransition(), e);
		}
		return result;
	}

	private boolean timeout() {
		return !mServices.getProgressAwareTimer().continueProcessing();
	}

	@Override
	public BoundedPetriNet<LETTER, PLACE> getResult() {
		return mResult;
	}

	@Override
	protected IPetriNet<LETTER, PLACE> getOperand() {
		return mOperand;
	}

	@Override
	public boolean checkResult(final CRSF stateFactory) throws AutomataLibraryException {
		if (mLogger.isInfoEnabled()) {
			mLogger.info("Testing correctness of " + getOperationName());
		}
		final boolean correct = PetriNetUtils.isEquivalent(mServices, stateFactory, mOperand, mResult);
		if (mLogger.isInfoEnabled()) {
			mLogger.info("Finished testing correctness of " + getOperationName());
		}
		return correct;
	}

	@Override
	public String exitMessage() {
		return "Finished " + getOperationName() + ", result has " + mResult.sizeInformation()
				+ ", removed " + mRedundantSelfloopFlow.size() + " selfloop flow, removed " + mRedundantPlaces.size()
				+ " redundant places.";
	}

	@Override
	public AutomataOperationStatistics getAutomataOperationStatistics() {
		final AutomataOperationStatistics statistics = new AutomataOperationStatistics();

		statistics.addKeyValuePair(StatisticsType.PETRI_REMOVED_PLACES,
				mOperand.getPlaces().size() - mResult.getPlaces().size());
		statistics.addKeyValuePair(StatisticsType.PETRI_REMOVED_TRANSITIONS,
				mOperand.getTransitions().size() - mResult.getTransitions().size());
		statistics.addKeyValuePair(StatisticsType.PETRI_REMOVED_FLOW, mOperand.flowSize() - mResult.flowSize());

		statistics.addKeyValuePair(StatisticsType.PETRI_ALPHABET, mResult.getAlphabet().size());
		statistics.addKeyValuePair(StatisticsType.PETRI_PLACES, mResult.getPlaces().size());
		statistics.addKeyValuePair(StatisticsType.PETRI_TRANSITIONS, mResult.getTransitions().size());
		statistics.addKeyValuePair(StatisticsType.PETRI_FLOW, mResult.flowSize());

		return statistics;
	}

}