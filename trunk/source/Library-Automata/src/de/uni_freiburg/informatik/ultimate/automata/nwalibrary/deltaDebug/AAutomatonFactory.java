/*
 * Copyright (C) 2015 Christian Schilling <schillic@informatik.uni-freiburg.de>
 * Copyright (C) 2009-2015 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.deltaDebug;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingCallTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.transitions.OutgoingReturnTransition;

/**
 * Factory for {@link INestedWordAutomaton} objects.
 * 
 * NOTE: The automaton field is not updated during the shrinking process.
 * Use it with caution.
 * 
 * @author Christian Schilling <schillic@informatik.uni-freiburg.de>
 */
public abstract class AAutomatonFactory<LETTER, STATE> {
	final StateFactory<STATE> m_stateFactory;
	final INestedWordAutomaton<LETTER, STATE> m_automaton;
	
	public AAutomatonFactory(final StateFactory<STATE> stateFactory,
			final INestedWordAutomaton<LETTER, STATE> automaton) {
		this.m_stateFactory = stateFactory;
		this.m_automaton = automaton;
	}
	
	/**
	 * @return new {@link INestedWordAutomaton} object
	 */
	public INestedWordAutomaton<LETTER, STATE> create() {
		return create(null, null, null);
	}
	
	/**
	 * @param internalAlphabet internal alphabet, null uses original one
	 * @param callAlphabet call alphabet, null uses original one
	 * @param returnAlphabet return alphabet, null uses original one
	 * @return new {@link INestedWordAutomaton} object with specified alphabets
	 */
	public INestedWordAutomaton<LETTER, STATE> create(
			Set<LETTER> internalAlphabet, Set<LETTER> callAlphabet,
			Set<LETTER> returnAlphabet) {
		if (internalAlphabet == null) {
			internalAlphabet = m_automaton.getAlphabet();
		}
		if (callAlphabet == null) {
			callAlphabet = m_automaton.getCallAlphabet();
		}
		if (returnAlphabet == null) {
			returnAlphabet = m_automaton.getReturnAlphabet();
		}
		return createWithAlphabets(
				internalAlphabet, callAlphabet, returnAlphabet);
	}
	
	/**
	 * @param internalAlphabet internal alphabet
	 * @param callAlphabet call alphabet
	 * @param returnAlphabet return alphabet
	 * @return new {@link INestedWordAutomaton} object with specified alphabets
	 */
	protected abstract INestedWordAutomaton<LETTER, STATE> createWithAlphabets(
			final Set<LETTER> internalAlphabet, final Set<LETTER> callAlphabet,
			final Set<LETTER> returnAlphabet);
	
	/**
	 * @param automaton automaton
	 * @param state new state to add
	 */
	public abstract void addState(
			final INestedWordAutomaton<LETTER, STATE> automaton,
			final STATE state);
	
	/**
	 * @param automaton automaton
	 * @param pred predecessor state
	 * @param letter letter
	 * @param succ successor state
	 */
	public abstract void addInternalTransition(
			final INestedWordAutomaton<LETTER, STATE> automaton,
			final STATE pred, final LETTER letter, final STATE succ);
	
	/**
	 * @param automaton automaton
	 * @param pred predecessor state
	 * @param letter letter
	 * @param succ successor state
	 */
	public abstract void addCallTransition(
			final INestedWordAutomaton<LETTER, STATE> automaton,
			final STATE pred, final LETTER letter, final STATE succ);
	
	/**
	 * @param automaton automaton
	 * @param pred linear predecessor state
	 * @param hier hierarchical predecessor state
	 * @param letter letter
	 * @param succ successor state
	 */
	public abstract void addReturnTransition(
			final INestedWordAutomaton<LETTER, STATE> automaton,
			final STATE pred, final STATE hier, final LETTER letter,
			final STATE succ);
	
	/**
	 * @param automaton automaton
	 * @param states new states to add
	 */
	public void addStates(
			final INestedWordAutomaton<LETTER, STATE> automaton,
			final Collection<STATE> states) {
		for (final STATE state : states) {
			addState(automaton, state);
		}
	}
	
	/**
	 * @param automaton automaton
	 * @param transitions internal transitions
	 */
	public void addInternalTransitions(
			final INestedWordAutomaton<LETTER, STATE> automaton,
			final Collection<TypedTransition<LETTER, STATE>> transitions) {
		for (final TypedTransition<LETTER, STATE> trans : transitions) {
			assert(trans.m_letter.m_type == ELetterType.Internal) :
				"Wrong transition type.";
			addInternalTransition(automaton, trans.m_pred,
					trans.m_letter.m_letter, trans.m_succ);
		}
	}
	
	/**
	 * @param automaton automaton
	 * @param transitions internal transitions
	 */
	public void addCallTransitions(
			final INestedWordAutomaton<LETTER, STATE> automaton,
			final Collection<TypedTransition<LETTER, STATE>> transitions) {
		for (final TypedTransition<LETTER, STATE> trans : transitions) {
			assert(trans.m_letter.m_type == ELetterType.Call) :
				"Wrong transition type.";
			addCallTransition(automaton, trans.m_pred,
					trans.m_letter.m_letter, trans.m_succ);
		}
	}
	
	/**
	 * @param automaton automaton
	 * @param transitions return transitions
	 */
	public void addReturnTransitions(
			final INestedWordAutomaton<LETTER, STATE> automaton,
			final Collection<TypedTransition<LETTER, STATE>> transitions) {
		for (final TypedTransition<LETTER, STATE> trans : transitions) {
			assert(trans.m_letter.m_type == ELetterType.Return) :
				"Wrong transition type.";
			addReturnTransition(automaton, trans.m_pred,
					trans.m_hier, trans.m_letter.m_letter, trans.m_succ);
		}
	}
	
	/**
	 * @param automaton automaton
	 * @return all internal transitions
	 */
	public Set<TypedTransition<LETTER, STATE>> getInternalTransitions(
			final INestedWordAutomaton<LETTER, STATE> automaton) {
		final Set<TypedTransition<LETTER, STATE>> transitions =
				new HashSet<TypedTransition<LETTER, STATE>>();
		for (final STATE state : automaton.getStates()) {
			for (final OutgoingInternalTransition<LETTER, STATE> trans :
					automaton.internalSuccessors(state)) {
				transitions.add(new TypedTransition<LETTER, STATE>(state,
						trans.getSucc(), null,
						new TypedLetter<LETTER>(trans.getLetter(),
								ELetterType.Internal)));
			}
		}
		return transitions;
	}
	
	/**
	 * @param automaton automaton
	 * @return all call transitions
	 */
	public Set<TypedTransition<LETTER, STATE>> getCallTransitions(
			final INestedWordAutomaton<LETTER, STATE> automaton) {
		final Set<TypedTransition<LETTER, STATE>> transitions =
				new HashSet<TypedTransition<LETTER, STATE>>();
		for (final STATE state : automaton.getStates()) {
			for (final OutgoingCallTransition<LETTER, STATE> trans :
					automaton.callSuccessors(state)) {
				transitions.add(new TypedTransition<LETTER, STATE>(state,
						trans.getSucc(), null,
						new TypedLetter<LETTER>(trans.getLetter(),
								ELetterType.Call)));
			}
		}
		return transitions;
	}
	
	/**
	 * @param automaton automaton
	 * @return all return transitions
	 */
	public Set<TypedTransition<LETTER, STATE>> getReturnTransitions(
			final INestedWordAutomaton<LETTER, STATE> automaton) {
		final Set<TypedTransition<LETTER, STATE>> transitions =
				new HashSet<TypedTransition<LETTER, STATE>>();
		for (final STATE state : automaton.getStates()) {
			for (final OutgoingReturnTransition<LETTER, STATE> trans :
					automaton.returnSuccessors(state)) {
				transitions.add(new TypedTransition<LETTER, STATE>(state,
						trans.getSucc(), trans.getHierPred(),
						new TypedLetter<LETTER>(trans.getLetter(),
								ELetterType.Return)));
			}
		}
		return transitions;
	}
	
	/**
	 * Adds original internal transitions filtered by current states.
	 * 
	 * @param automatonTo automaton to add the transitions to
	 * @param automatonFrom automaton to take the transitions from
	 */
	public void addFilteredInternalTransitions(
			final INestedWordAutomaton<LETTER, STATE> automatonTo,
			final INestedWordAutomaton<LETTER, STATE> automatonFrom) {
		final Set<STATE> remainingStates = automatonTo.getStates();
		for (final STATE state : remainingStates) {
			for (final OutgoingInternalTransition<LETTER, STATE> trans :
					automatonFrom.internalSuccessors(state)) {
				final STATE succ = trans.getSucc();
				if (remainingStates.contains(succ)) {
					addInternalTransition(
							automatonTo, state, trans.getLetter(), succ);
				}
			}
		}
	}
	
	/**
	 * Adds original call transitions filtered by current states.
	 * 
	 * @param automatonTo automaton to add the transitions to
	 * @param automatonFrom automaton to take the transitions from
	 */
	public void addFilteredCallTransitions(
			final INestedWordAutomaton<LETTER, STATE> automatonTo,
			final INestedWordAutomaton<LETTER, STATE> automatonFrom) {
		final Set<STATE> remainingStates = automatonTo.getStates();
		for (final STATE state : remainingStates) {
			for (final OutgoingCallTransition<LETTER, STATE> trans :
					automatonFrom.callSuccessors(state)) {
				final STATE succ = trans.getSucc();
				if (remainingStates.contains(succ)) {
					addCallTransition(
							automatonTo, state, trans.getLetter(), succ);
				}
			}
		}
	}
	
	/**
	 * Adds original return transitions filtered by current states.
	 * 
	 * @param automatonTo automaton to add the transitions to
	 * @param automatonFrom automaton to take the transitions from
	 */
	public void addFilteredReturnTransitions(
			final INestedWordAutomaton<LETTER, STATE> automatonTo,
			final INestedWordAutomaton<LETTER, STATE> automatonFrom) {
		final Set<STATE> remainingStates = automatonTo.getStates();
		for (final STATE state : remainingStates) {
			
			for (final OutgoingReturnTransition<LETTER, STATE> trans :
					automatonFrom.returnSuccessors(state)) {
				final STATE succ = trans.getSucc();
				final STATE hier = trans.getHierPred();
				if ((remainingStates.contains(succ)) &&
						(remainingStates.contains(hier))) {
					addReturnTransition(
							automatonTo, state, hier, trans.getLetter(), succ);
				}
			}
		}
	}
	
	/**
	 * Adds original transitions filtered by current states.
	 * 
	 * @param automatonTo automaton to add the transitions to
	 * @param automatonFrom automaton to take the transitions from
	 */
	public void addFilteredTransitions(
			final INestedWordAutomaton<LETTER, STATE> automatonTo,
			final INestedWordAutomaton<LETTER, STATE> automatonFrom) {
		addFilteredInternalTransitions(automatonTo, automatonFrom);
		addFilteredCallTransitions(automatonTo, automatonFrom);
		addFilteredReturnTransitions(automatonTo, automatonFrom);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}