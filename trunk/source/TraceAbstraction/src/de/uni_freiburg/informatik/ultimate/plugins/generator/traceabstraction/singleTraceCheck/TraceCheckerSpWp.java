/*
 * Copyright (C) 2013-2015 Betim Musa (musab@informatik.uni-freiburg.de)
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2013-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWord;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.QuantifiedFormula;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IAction;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transitions.UnmodifiableTransFormula;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.hoaretriple.IHoareTripleChecker.Validity;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.ContainsQuantifier;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.MonolithicImplicationChecker;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.SimplificationTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.XnfConversionTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearTerms.QuantifierPusher;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.PredicateTransformer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.CoverageAnalysis.BackwardCoveringInformation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.IterativePredicateTransformer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.IterativePredicateTransformer.PredicatePostprocessor;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TraceAbstractionPreferenceInitializer.AssertCodeBlockOrder;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TraceAbstractionPreferenceInitializer.InterpolationTechnique;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TraceAbstractionPreferenceInitializer.UnsatCores;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.TraceCheckerStatisticsGenerator.InterpolantType;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.TraceCheckerUtils.InterpolantsPreconditionPostcondition;
import de.uni_freiburg.informatik.ultimate.util.RunningTaskInfo;
import de.uni_freiburg.informatik.ultimate.util.ToolchainCanceledException;

/**
 * Use unsat core, predicate transformer and live variable analsysis to compute a sequence of interpolants.
 *
 * @author Betim Musa, Matthias Heizmann
 *
 */
public class TraceCheckerSpWp extends InterpolatingTraceChecker {

	// Forward relevant predicates
	protected List<IPredicate> mInterpolantsFp;
	// Backward relevant predicates
	protected List<IPredicate> mInterpolantsBp;

	private final UnsatCores mUnsatCores;
	private final boolean mLiveVariables;
	private final static boolean mUseLiveVariablesInsteadOfRelevantVariables = false;

	// We may post-process the forwards predicates, after the backwards predicates has been computed in order
	// to potentially eliminate quantifiers. The idea is the following:
	// If there is a predicate p in the forwards predicates that contains quantifiers and there is an equivalent
	// predicate p' in the backwards
	// predicates that is quantifier-free, then we may replace p by p'.
	private final static boolean mPostProcess_FP_Predicates = false;

	private final boolean mConstructForwardInterpolantSequence;
	private final boolean mConstructBackwardInterpolantSequence;

	private AnnotateAndAssertConjunctsOfCodeBlocks mAnnotateAndAsserterConjuncts;

	private int mNonLiveVariablesFp = 0;
	private int mNonLiveVariablesBp = 0;

	public TraceCheckerSpWp(final IPredicate precondition, final IPredicate postcondition,
			final SortedMap<Integer, IPredicate> pendingContexts, final NestedWord<? extends IAction> trace,
			final CfgSmtToolkit csToolkit, final AssertCodeBlockOrder assertCodeBlocksIncrementally,
			final UnsatCores unsatCores, final boolean useLiveVariables, final IUltimateServiceProvider services,
			final boolean computeRcfgProgramExecution, final PredicateUnifier predicateUnifier,
			final InterpolationTechnique interpolation, final ManagedScript mgdScriptTc,
			final XnfConversionTechnique xnfConversionTechnique, final SimplificationTechnique simplificationTechnique,
			final List<? extends Object> controlLocationSequence) {
		// superclass does feasibility check
		super(precondition, postcondition, pendingContexts, trace, csToolkit, assertCodeBlocksIncrementally, services,
				computeRcfgProgramExecution, predicateUnifier, mgdScriptTc, simplificationTechnique,
				xnfConversionTechnique, controlLocationSequence);
		mUnsatCores = unsatCores;
		mLiveVariables = useLiveVariables;
		switch (interpolation) {
		case ForwardPredicates:
			mConstructForwardInterpolantSequence = true;
			mConstructBackwardInterpolantSequence = false;
			break;
		case BackwardPredicates:
			mConstructForwardInterpolantSequence = false;
			mConstructBackwardInterpolantSequence = true;
			break;
		case FPandBP:
			mConstructForwardInterpolantSequence = true;
			mConstructBackwardInterpolantSequence = true;
			break;
		default:
			throw new UnsupportedOperationException("unsupportedInterpolation");
		}
		if (isCorrect() == LBool.UNSAT) {
			computeInterpolants(new AllIntegers(), interpolation);
		}
	}

	@Override
	public void computeInterpolants(final Set<Integer> interpolatedPositions,
			final InterpolationTechnique interpolation) {
		mTraceCheckerBenchmarkGenerator.start(TraceCheckerStatisticsDefinitions.InterpolantComputationTime.toString());
		try {
			computeInterpolantsUsingUnsatCore(interpolatedPositions);
		} catch (final ToolchainCanceledException tce) {
			mLogger.info("Timeout while computing interpolants");
			mToolchainCanceledException = tce;
		} finally {
			mTraceCheckerBenchmarkGenerator
					.stop(TraceCheckerStatisticsDefinitions.InterpolantComputationTime.toString());
		}
		mTraceCheckFinished = true;
	}

	public boolean forwardsPredicatesComputed() {
		return mConstructForwardInterpolantSequence;
	}

	public boolean backwardsPredicatesComputed() {
		return mConstructBackwardInterpolantSequence;
	}

	public List<IPredicate> getForwardPredicates() {
		assert mInterpolantsFp != null : "Forwards predicates not computed!";
		return mInterpolantsFp;
	}

	public InterpolantsPreconditionPostcondition getForwardIpp() {
		return new InterpolantsPreconditionPostcondition(getPrecondition(), getPostcondition(), getForwardPredicates());
	}

	public List<IPredicate> getBackwardPredicates() {
		assert mInterpolantsBp != null : "Backwards predicates not computed!";
		return mInterpolantsBp;
	}

	public InterpolantsPreconditionPostcondition getBackwardIpp() {
		return new InterpolantsPreconditionPostcondition(getPrecondition(), getPostcondition(),
				getBackwardPredicates());
	}

	/***
	 * Computes predicates (interpolants) for the statements of an infeasible trace specified by the given set.
	 * Depending on settings, there are only forward predicates, or only backward predicates or both of them computed
	 * {@see computeForwardRelevantPredicates, computeBackwardRelevantPredicates} <br>
	 * The predicates are computed using an unsatisfiable core of the corresponding infeasibility proof of the trace in
	 * the following way: <br>
	 * 1. Replace statements, which don't occur in the unsatisfiable core by the statement <code> assume(true) </code>
	 * <br>
	 * 2. Compute live variables. <br>
	 * 3. Compute predicates for the trace, where the non-relevant statements has been substituted by
	 * <code> assume(true) </code>.
	 *
	 * @see LiveVariables
	 * @see PredicateTransformer
	 */
	private void computeInterpolantsUsingUnsatCore(final Set<Integer> interpolatedPositions) {
		if (!(interpolatedPositions instanceof AllIntegers)) {
			throw new UnsupportedOperationException();
		}
		final Set<Term> unsatCore = new HashSet<>(Arrays.asList(mTcSmtManager.getScript().getUnsatCore()));
		// unsat core obtained. We now pop assertion stack of solver. This
		// allows us to use solver e.g. for simplifications
		unlockSmtManager();

		{
			final int numberOfConjunctsInTrace = mAnnotateAndAsserterConjuncts.getAnnotated2Original().keySet().size();
			final int numberOfConjunctsInUnsatCore;
			if (mUnsatCores == UnsatCores.IGNORE) {
				numberOfConjunctsInUnsatCore = 0;
			} else {
				numberOfConjunctsInUnsatCore = unsatCore.size();
			}
			mLogger.debug("Total number of conjuncts in trace: " + numberOfConjunctsInTrace);
			mLogger.debug("Number of conjuncts in unsatisfiable core: " + unsatCore.size());
			mTraceCheckerBenchmarkGenerator.setConjunctsInSSA(numberOfConjunctsInTrace, numberOfConjunctsInUnsatCore);
		}

		final NestedFormulas<UnmodifiableTransFormula, IPredicate> rtf = constructRelevantTransFormulas(unsatCore);
		assert stillInfeasible(rtf) : "incorrect Unsatisfiable Core! trace length " + mTrace.length()
				+ " unsat-core size " + unsatCore.size();

		final Set<IProgramVar>[] liveVariables;
		if (mUseLiveVariablesInsteadOfRelevantVariables) {
			// computation of live variables whose input is the original trace
			final LiveVariables lvar = new LiveVariables(mNsb.getVariable2Constant(), mNsb.getConstants2BoogieVar(),
					mNsb.getIndexedVarRepresentative(), mCsToolkit.getModifiableGlobals());
			liveVariables = lvar.getLiveVariables();
		} else {
			// computation of live variables whose input takes the unsat core into a account (if applicable)
			final RelevantVariables rvar = new RelevantVariables(rtf, mCsToolkit.getModifiableGlobals());
			liveVariables = rvar.getRelevantVariables();
		}

		if (mConstructForwardInterpolantSequence) {
			mLogger.debug("Computing forward predicates...");
			try {
				final List<PredicatePostprocessor> postprocs = new ArrayList<>();
				if (mLiveVariables) {
					postprocs.add(new LiveVariablesPostprocessor_Forward(liveVariables));
				}
				postprocs.add(new IterativePredicateTransformer.QuantifierEliminationPostprocessor(mServices, mLogger,
						mCfgManagedScript, mPredicateFactory, mSimplificationTechnique, mXnfConversionTechnique));
				postprocs.add(new UnifyPostprocessor());
				final IterativePredicateTransformer spt = new IterativePredicateTransformer(mPredicateFactory,
						mCfgManagedScript, mCsToolkit.getModifiableGlobals(), mServices, mTrace, mPrecondition,
						mPostcondition, mPendingContexts, null, mSimplificationTechnique, mXnfConversionTechnique,
						mBoogie2SmtSymbolTable);
				mInterpolantsFp = spt.computeStrongestPostconditionSequence(rtf, postprocs).getInterpolants();
			} catch (final ToolchainCanceledException tce) {
				final String taskDescription = "constructing forward predicates";
				tce.addRunningTaskInfo(new RunningTaskInfo(getClass(), taskDescription));
				throw tce;
			}
			assert TraceCheckerUtils.checkInterpolantsInductivityForward(mInterpolantsFp, mTrace, mPrecondition,
					mPostcondition, mPendingContexts, "FP", mCsToolkit, mLogger,
					mCfgManagedScript) : "invalid Hoare triple in FP";
		}

		if (mConstructBackwardInterpolantSequence) {
			mLogger.debug("Computing backward predicates...");
			try {
				final List<PredicatePostprocessor> postprocs = new ArrayList<>();
				if (mLiveVariables) {
					postprocs.add(new LiveVariablesPostprocessor_Backward(liveVariables));
				}
				postprocs.add(new IterativePredicateTransformer.QuantifierEliminationPostprocessor(mServices, mLogger,
						mCfgManagedScript, mPredicateFactory, mSimplificationTechnique, mXnfConversionTechnique));
				postprocs.add(new UnifyPostprocessor());
				final IterativePredicateTransformer spt = new IterativePredicateTransformer(mPredicateFactory,
						mCfgManagedScript, mCsToolkit.getModifiableGlobals(), mServices, mTrace, mPrecondition,
						mPostcondition, mPendingContexts, null, mSimplificationTechnique, mXnfConversionTechnique,
						mBoogie2SmtSymbolTable);
				mInterpolantsBp = spt.computeWeakestPreconditionSequence(rtf, postprocs, false).getInterpolants();
			} catch (final ToolchainCanceledException tce) {
				final String taskDescription = "constructing backward predicates";
				tce.addRunningTaskInfo(new RunningTaskInfo(getClass(), taskDescription));
				throw tce;
			}
			assert TraceCheckerUtils.checkInterpolantsInductivityBackward(mInterpolantsBp, mTrace, mPrecondition,
					mPostcondition, mPendingContexts, "BP", mCsToolkit, mLogger,
					mCfgManagedScript) : "invalid Hoare triple in BP";
		}

		if (mConstructForwardInterpolantSequence && mConstructBackwardInterpolantSequence) {
			// Post-process forwards predicates
			if (mPostProcess_FP_Predicates) {
				for (int i = 0; i < mInterpolantsFp.size(); i++) {
					final IPredicate p_old = mInterpolantsFp.get(i);
					final IPredicate p_new = mPredicateUnifier.getOrConstructPredicate(p_old.getFormula());
					mInterpolantsFp.set(i, p_new);
				}
			}
		}

		if (mConstructForwardInterpolantSequence) {
			mTraceCheckerBenchmarkGenerator.reportSequenceOfInterpolants(mInterpolantsFp, InterpolantType.Forward);
			mTraceCheckerBenchmarkGenerator.reportNumberOfNonLiveVariables(mNonLiveVariablesFp,
					InterpolantType.Forward);
			mTraceCheckerBenchmarkGenerator.reportInterpolantComputation();
			if (mControlLocationSequence != null) {
				final BackwardCoveringInformation bci = TraceCheckerUtils.computeCoverageCapability(mServices,
						getForwardIpp(), mControlLocationSequence, mLogger, mPredicateUnifier);
				final boolean perfectSequence =
						(bci.getPotentialBackwardCoverings() == bci.getSuccessfullBackwardCoverings());
				if (perfectSequence) {
					mTraceCheckerBenchmarkGenerator.reportPerfectInterpolantSequences();
				}
				mTraceCheckerBenchmarkGenerator.addBackwardCoveringInformation(bci);
			}
		}
		if (mConstructBackwardInterpolantSequence) {
			mTraceCheckerBenchmarkGenerator.reportSequenceOfInterpolants(mInterpolantsBp, InterpolantType.Backward);
			mTraceCheckerBenchmarkGenerator.reportNumberOfNonLiveVariables(mNonLiveVariablesBp,
					InterpolantType.Backward);
			mTraceCheckerBenchmarkGenerator.reportInterpolantComputation();
			if (mControlLocationSequence != null) {
				final BackwardCoveringInformation bci = TraceCheckerUtils.computeCoverageCapability(mServices,
						getBackwardIpp(), mControlLocationSequence, mLogger, mPredicateUnifier);
				final boolean perfectSequence =
						(bci.getPotentialBackwardCoverings() == bci.getSuccessfullBackwardCoverings());
				if (perfectSequence) {
					mTraceCheckerBenchmarkGenerator.reportPerfectInterpolantSequences();
				}
				mTraceCheckerBenchmarkGenerator.addBackwardCoveringInformation(bci);
			}

		}

		// Check the validity of the computed interpolants.
		// if (mConstructForwardInterpolantSequence && mConstructBackwardInterpolantSequence) {
		// checkSPImpliesWP(mInterpolantsFp, mInterpolantsBp);
		// }
		if (mConstructForwardInterpolantSequence && mConstructBackwardInterpolantSequence) {
			final boolean omitMixedSequence = true;
			if (omitMixedSequence) {
				mInterpolants = null;
			} else {
				selectListOFPredicatesFromBothTypes();
			}
		} else if (mConstructForwardInterpolantSequence) {
			mInterpolants = mInterpolantsFp.toArray(new IPredicate[mInterpolantsFp.size()]);
		} else if (mConstructBackwardInterpolantSequence) {
			mInterpolants = mInterpolantsBp.toArray(new IPredicate[mInterpolantsBp.size()]);
		} else {
			throw new AssertionError("illegal choice");
		}
	}

	/**
	 * Construct representation of the trace formula that contains only the conjuncts that occur in the unsat core.
	 */
	private NestedFormulas<UnmodifiableTransFormula, IPredicate>
			constructRelevantTransFormulas(final Set<Term> unsatCore) {
		final NestedFormulas<UnmodifiableTransFormula, IPredicate> rtf;
		if (mUnsatCores == UnsatCores.IGNORE) {
			rtf = new DefaultTransFormulas(mTrace, mPrecondition, mPostcondition, mPendingContexts,
					mCsToolkit.getModifiableGlobals(), false);
		} else if (mUnsatCores == UnsatCores.STATEMENT_LEVEL) {
			final boolean[] localVarAssignmentAtCallInUnsatCore = new boolean[mTrace.length()];
			final boolean[] oldVarAssignmentAtCallInUnsatCore = new boolean[mTrace.length()];
			// Filter out the statements, which doesn't occur in the unsat core.
			final Set<IAction> codeBlocksInUnsatCore = filterOutIrrelevantStatements(mTrace, unsatCore,
					localVarAssignmentAtCallInUnsatCore, oldVarAssignmentAtCallInUnsatCore);
			rtf = new RelevantTransFormulas(mTrace, mPrecondition, mPostcondition, mPendingContexts,
					codeBlocksInUnsatCore, mCsToolkit.getModifiableGlobals(), localVarAssignmentAtCallInUnsatCore,
					oldVarAssignmentAtCallInUnsatCore, mCfgManagedScript);
		} else if (mUnsatCores == UnsatCores.CONJUNCT_LEVEL) {
			rtf = new RelevantTransFormulas(mTrace, mPrecondition, mPostcondition, mPendingContexts, unsatCore,
					mCsToolkit.getModifiableGlobals(), mCfgManagedScript, mAAA, mAnnotateAndAsserterConjuncts);
		} else {
			throw new AssertionError("unknown case:" + mUnsatCores);
		}
		return rtf;
	}

	/***
	 * Selects a list of predicates from the predicates computed via strongest post-condition and the weakest
	 * precondition, such that the number of predicates containing quantifiers is minimized and a mix-up of the two
	 * types is avoided. (If the predicates are mixed-up, they may get non-inductive.) Predicates are selected in the
	 * following way: (TODO:)
	 *
	 */
	private void selectListOFPredicatesFromBothTypes() {
		assert mInterpolantsFp.size() == mInterpolantsBp.size();
		mInterpolants = new IPredicate[mInterpolantsBp.size()];
		int i = 0; // position of predicate computed by strongest post-condition
		int j = mInterpolantsBp.size(); // position of predicate computed by
		// weakest precondition
		final ContainsQuantifier containsQuantifier = new ContainsQuantifier();
		while (i != j) {
			if (!containsQuantifier.containsQuantifier(mInterpolantsBp.get(j - 1).getFormula())) {
				mInterpolants[j - 1] = mInterpolantsBp.get(j - 1);
				j--;
			} else if (!containsQuantifier.containsQuantifier(mInterpolantsFp.get(i).getFormula())) {
				mInterpolants[i] = mInterpolantsFp.get(i);
				i++;
			} else {
				throw new UnsupportedOperationException("removed in refactoring");
				// 2016-05-05 Matthias: I deleted BasicPredicateExplicitQuantifier, hence
				// the following code does not compile any more
				// fix: Count quantified variables
				// int numOfQuantifiedVarsInFp = ((BasicPredicateExplicitQuantifier) mInterpolantsFp.get(i))
				// .getQuantifiedVariables().size();
				// int numOfQuantifiedVarsInBp = ((BasicPredicateExplicitQuantifier) mInterpolantsBp.get(j - 1))
				// .getQuantifiedVariables().size();
				// if (numOfQuantifiedVarsInFp < numOfQuantifiedVarsInBp) {
				// mInterpolants[i] = mInterpolantsFp.get(i);
				// i++;
				// } else {
				// mInterpolants[j - 1] = mInterpolantsBp.get(j - 1);
				// j--;
				// }
			}
		}
	}

	/**
	 * Checks whether the trace consisting of only relevant statements is still infeasible. This check is desired, when
	 * we use unsatisfiable cores of finer granularity.
	 *
	 * @return true iff result of infeasiblity check is unsat or unknown
	 */
	private boolean stillInfeasible(final NestedFormulas<UnmodifiableTransFormula, IPredicate> rv) {
		final TraceChecker tc =
				new TraceChecker(rv.getPrecondition(), rv.getPostcondition(), new TreeMap<Integer, IPredicate>(),
						rv.getTrace(), mCsToolkit, rv, AssertCodeBlockOrder.NOT_INCREMENTALLY, mServices, false, true);
		if (tc.getToolchainCanceledExpection() != null) {
			throw tc.getToolchainCanceledExpection();
		}
		final boolean result = (tc.isCorrect() != LBool.SAT);
		return result;
	}

	/**
	 * Select the statements from the given trace, which are contained in the unsatisfiable core. These statements
	 * contribute to the unsatisfiability of the trace, and therefore only these are important for the computations done
	 * afterwards.
	 */
	private Set<IAction> filterOutIrrelevantStatements(final NestedWord<? extends IAction> trace,
			final Set<Term> unsat_coresAsSet, final boolean[] localVarAssignmentAtCallInUnsatCore,
			final boolean[] oldVarAssignmentAtCallInUnsatCore) {
		final Set<IAction> codeBlocksInUnsatCore = new HashSet<>();
		for (int i = 0; i < trace.length(); i++) {
			if (!trace.isCallPosition(i)
					&& unsat_coresAsSet.contains(mAAA.getAnnotatedSsa().getFormulaFromNonCallPos(i))) {
				codeBlocksInUnsatCore.add(trace.getSymbol(i));
			} else if (trace.isCallPosition(i)
					&& (unsat_coresAsSet.contains(mAAA.getAnnotatedSsa().getGlobalVarAssignment(i))
							|| unsat_coresAsSet.contains(mAAA.getAnnotatedSsa().getOldVarAssignment(i)))) {
				// The upper condition checks, whether the globalVarAssignments
				// is in unsat core, now check whether the local variable
				// assignments
				// is in unsat core, if it is Call statement
				if (unsat_coresAsSet.contains(mAAA.getAnnotatedSsa().getLocalVarAssignment(i))) {
					localVarAssignmentAtCallInUnsatCore[i] = true;
				}
				if (unsat_coresAsSet.contains(mAAA.getAnnotatedSsa().getOldVarAssignment(i))) {
					oldVarAssignmentAtCallInUnsatCore[i] = true;
				}
				// Add the globalVarAssignments to the unsat_core, if it is a
				// Call statement, otherwise it adds
				// the statement
				codeBlocksInUnsatCore.add(trace.getSymbol(i));
			} else {
				if (trace.getSymbol(i) instanceof Call) {
					if (unsat_coresAsSet.contains(mAAA.getAnnotatedSsa().getLocalVarAssignment(i))) {
						localVarAssignmentAtCallInUnsatCore[i] = true;
					}
				}
			}
		}
		return codeBlocksInUnsatCore;
	}

	public class LiveVariablesPostprocessor_Forward implements PredicatePostprocessor {

		private final Set<IProgramVar>[] mRelevantVars;

		public LiveVariablesPostprocessor_Forward(final Set<IProgramVar>[] relevantVars) {
			mRelevantVars = relevantVars;
		}

		@Override
		public IPredicate postprocess(final IPredicate pred, final int i) {
			assert mLiveVariables : "use this postprocessor only if mLiveVariables";
			final Set<TermVariable> nonLiveVars = computeIrrelevantVariables(mRelevantVars[i], pred);
			final Term projectedT = SmtUtils.quantifier(mCfgManagedScript.getScript(), QuantifiedFormula.EXISTS,
					nonLiveVars, pred.getFormula());
			final Term pushed = new QuantifierPusher(mCfgManagedScript, mServices).transform(projectedT);
			final IPredicate projected = mPredicateFactory.newPredicate(pushed);
			mNonLiveVariablesFp += nonLiveVars.size();
			return projected;
		}

	}

	public class LiveVariablesPostprocessor_Backward implements PredicatePostprocessor {

		private final Set<IProgramVar>[] mRelevantVars;

		public LiveVariablesPostprocessor_Backward(final Set<IProgramVar>[] relevantVars) {
			super();
			mRelevantVars = relevantVars;
		}

		@Override
		public IPredicate postprocess(final IPredicate pred, final int i) {
			assert mLiveVariables : "use this postprocessor only if mLiveVariables";
			final Set<TermVariable> nonLiveVars = computeIrrelevantVariables(mRelevantVars[i], pred);
			final Term projectedT = SmtUtils.quantifier(mCfgManagedScript.getScript(), QuantifiedFormula.FORALL,
					nonLiveVars, pred.getFormula());
			final Term pushed = new QuantifierPusher(mCfgManagedScript, mServices).transform(projectedT);
			final IPredicate projected = mPredicateFactory.newPredicate(pushed);
			mNonLiveVariablesBp += nonLiveVars.size();
			return projected;
		}
	}

	public class UnifyPostprocessor implements PredicatePostprocessor {

		@Override
		public IPredicate postprocess(final IPredicate pred, final int i) {
			final IPredicate unified = mPredicateUnifier.getOrConstructPredicate(pred.getFormula());
			return unified;
		}

	}

	/**
	 * Compute the irrelevant variables of the given predicate p. A variable is irrelevant, if it isn't contained in the
	 * given set of relevantVars.
	 *
	 * @see LiveVariables
	 */
	private static Set<TermVariable> computeIrrelevantVariables(final Set<IProgramVar> relevantVars,
			final IPredicate p) {
		final Set<TermVariable> result = new HashSet<>();
		for (final IProgramVar bv : p.getVars()) {
			if (!relevantVars.contains(bv)) {
				result.add(bv.getTermVariable());
			}
		}
		return result;
	}

	/***
	 * Check for each predicate computed via the strongest post-condition, whether it implies the predicate computed via
	 * weakest precondition. This check is desired, when predicates are computed twice (once via strongest post, and
	 * once via weakest pre-condition). It ensures the correctness of the predicates.
	 */
	private void checkSPImpliesWP(final IPredicate[] interpolantsSP, final IPredicate[] interpolantsWP) {
		mLogger.debug("Checking implication of SP and WP...");
		final MonolithicImplicationChecker mic = new MonolithicImplicationChecker(mServices, mCfgManagedScript);
		for (int i = 0; i < interpolantsSP.length; i++) {
			final Validity result = mic.checkImplication(interpolantsSP[i], false, interpolantsWP[i], false);
			mLogger.debug("SP {" + interpolantsSP[i] + "} ==> WP {" + interpolantsWP[i] + "} is "
					+ (result == Validity.VALID ? "valid" : (result == Validity.INVALID ? "not valid" : result)));
			assert (result == Validity.VALID || result == Validity.UNKNOWN) : "checkSPImpliesWP failed";
		}
	}

	@Override
	protected AnnotateAndAssertCodeBlocks getAnnotateAndAsserterCodeBlocks(final NestedFormulas<Term, Term> ssa) {
		if (mAnnotateAndAsserterConjuncts == null) {
			mAnnotateAndAsserterConjuncts = new AnnotateAndAssertConjunctsOfCodeBlocks(mTcSmtManager, mTraceCheckerLock,
					ssa, mNestedFormulas, mLogger, mCfgManagedScript);
		}
		return mAnnotateAndAsserterConjuncts;
	}

}
