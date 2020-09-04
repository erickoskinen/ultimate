/*
 * Copyright (C) 2019 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2019 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.tracehandling;

import de.uni_freiburg.informatik.ultimate.automata.IAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.IRun;
import de.uni_freiburg.informatik.ultimate.automata.statefactory.IEmptyStackStateFactory;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.lib.mcr.IInterpolantProvider;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.CfgSmtToolkit;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IIcfgTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.hoaretriple.IHoareTripleChecker;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.IPredicateUnifier;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.PredicateFactory;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.tracecheck.ITraceCheckPreferences.AssertCodeBlockOrder;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.taskidentifier.TaskIdentifier;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.TermClassifier;
import de.uni_freiburg.informatik.ultimate.lib.tracecheckerutils.singletracecheck.InterpolationTechnique;
import de.uni_freiburg.informatik.ultimate.lib.tracecheckerutils.singletracecheck.TraceCheckUtils;
import de.uni_freiburg.informatik.ultimate.logic.Logics;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.PathProgramCache;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.PredicateFactoryForInterpolantAutomata;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.TraceAbstractionUtils;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TAPreferences;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TraceAbstractionPreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.preferences.TraceAbstractionPreferenceInitializer.InterpolantAutomaton;

/**
 *
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 * @param <LETTER>
 */
public class StrategyModuleFactory<LETTER extends IIcfgTransition<?>> {

	private final TaskIdentifier mTaskIdentifier;
	private final IUltimateServiceProvider mServices;
	private final TaCheckAndRefinementPreferences<LETTER> mPrefs;
	private final TAPreferences mTaPrefs;
	private final IRun<LETTER, ?> mCounterexample;
	private final IPredicateUnifier mPredicateUnifier;
	private final IPredicate mPrecondition;
	private final IPredicate mPostcondition;
	private final PredicateFactory mPredicateFactory;
	private final IAutomaton<LETTER, IPredicate> mAbstraction;
	private final IEmptyStackStateFactory<IPredicate> mEmptyStackFactory;
	private final ILogger mLogger;

	private final CfgSmtToolkit mCsToolkit;
	private final PredicateFactoryForInterpolantAutomata mPredFacInterpolAut;
	private final PathProgramCache<LETTER> mPathProgramCache;

	public StrategyModuleFactory(final TaskIdentifier taskIdentifier, final IUltimateServiceProvider services,
			final ILogger logger, final TaCheckAndRefinementPreferences<LETTER> prefs, final TAPreferences taPrefs,
			final IRun<LETTER, ?> counterExample, final IPredicate precondition, final IPredicate postcondition,
			final IPredicateUnifier predicateUnifier, final PredicateFactory predicateFactory,
			final IAutomaton<LETTER, IPredicate> abstraction,
			final IEmptyStackStateFactory<IPredicate> emptyStackFactory, final CfgSmtToolkit csToolkit,
			final PredicateFactoryForInterpolantAutomata predFacInterpolAut,
			final PathProgramCache<LETTER> pathProgramCache) {
		mServices = services;
		mLogger = logger;
		mPrefs = prefs;
		mTaPrefs = taPrefs;
		mCounterexample = counterExample;
		mPredicateUnifier = predicateUnifier;
		mPredicateFactory = predicateFactory;
		mPrecondition = precondition;
		mPostcondition = postcondition;
		mTaskIdentifier = taskIdentifier;
		mAbstraction = abstraction;
		mEmptyStackFactory = emptyStackFactory;
		mCsToolkit = csToolkit;
		mPredFacInterpolAut = predFacInterpolAut;
		mPathProgramCache = pathProgramCache;
	}

	public StrategyModuleMcr<LETTER> createStrategyModuleMcr(final StrategyFactory<LETTER> strategyFactory) {
		isOnlyDefaultPrePostConditions();
		final boolean useInterpolantConsolidation = mPrefs.getUseInterpolantConsolidation();
		if (useInterpolantConsolidation) {
			throw new UnsupportedOperationException("Interpolant consolidation and MCR cannot be combined");
		}
		return new StrategyModuleMcr<>(mLogger, mPrefs, mPredicateUnifier, mEmptyStackFactory, strategyFactory,
				mCounterexample, mAbstraction, mTaskIdentifier, createMcrInterpolantProvider());
	}

	public IIpTcStrategyModule<?, LETTER> createIpTcStrategyModuleSmtInterpolCraig(final boolean useTimeout,
			final InterpolationTechnique technique, final AssertCodeBlockOrder... order) {
		return createModuleWrapperIfNecessary(new IpTcStrategyModuleSmtInterpolCraig<>(mTaskIdentifier, mServices,
				mPrefs, mCounterexample, mPrecondition, mPostcondition,
				new AssertionOrderModulation<>(mPathProgramCache, mLogger, order), mPredicateUnifier, mPredicateFactory,
				useTimeout, technique));
	}

	public IIpTcStrategyModule<?, LETTER> createIpTcStrategyModuleSmtInterpolSpWp(final boolean useTimeout,
			final InterpolationTechnique technique, final AssertCodeBlockOrder... order) {
		return createModuleWrapperIfNecessary(new IpTcStrategyModuleSmtInterpolSpWp<>(mTaskIdentifier, mServices,
				mPrefs, mCounterexample, mPrecondition, mPostcondition,
				new AssertionOrderModulation<>(mPathProgramCache, mLogger, order), mPredicateUnifier, mPredicateFactory,
				useTimeout, technique));
	}

	public IIpTcStrategyModule<?, LETTER> createIpTcStrategyModuleZ3(final boolean useTimeout,
			final InterpolationTechnique technique, final AssertCodeBlockOrder... order) {
		return createModuleWrapperIfNecessary(
				new IpTcStrategyModuleZ3<>(mTaskIdentifier, mServices, mPrefs, mCounterexample, mPrecondition,
						mPostcondition, new AssertionOrderModulation<>(mPathProgramCache, mLogger, order),
						mPredicateUnifier, mPredicateFactory, useTimeout, technique));
	}

	public IIpTcStrategyModule<?, LETTER> createIpTcStrategyModuleMathsat(final InterpolationTechnique technique,
			final AssertCodeBlockOrder... order) {
		return createModuleWrapperIfNecessary(
				new IpTcStrategyModuleMathsat<>(mTaskIdentifier, mServices, mPrefs, mCounterexample, mPrecondition,
						mPostcondition, new AssertionOrderModulation<>(mPathProgramCache, mLogger, order),
						mPredicateUnifier, mPredicateFactory, technique));
	}

	public IIpTcStrategyModule<?, LETTER> createIpTcStrategyModuleCVC4(final boolean useTimeout,
			final InterpolationTechnique technique, final Logics logic, final AssertCodeBlockOrder... order) {
		return createModuleWrapperIfNecessary(
				new IpTcStrategyModuleCvc4<>(mTaskIdentifier, mServices, mPrefs, mCounterexample, mPrecondition,
						mPostcondition, new AssertionOrderModulation<>(mPathProgramCache, mLogger, order),
						mPredicateUnifier, mPredicateFactory, useTimeout, technique, logic));
	}

	public IIpTcStrategyModule<?, LETTER> createIpTcStrategyModuleAbstractInterpretation() {
		isOnlyDefaultPrePostConditions();
		return createModuleWrapperIfNecessary(new IpTcStrategyModuleAbstractInterpretation<>(mCounterexample,
				mPredicateUnifier, mServices, mPrefs.getIcfgContainer(), mPathProgramCache, mTaPrefs));
	}

	public IIpTcStrategyModule<?, LETTER> createIpTcStrategyModuleSifa() {
		isOnlyDefaultPrePostConditions();
		return createModuleWrapperIfNecessary(new IpTcStrategyModuleSifa<>(mServices, mLogger,
				mPrefs.getIcfgContainer(), mCounterexample, mPredicateUnifier));
	}

	public IIpTcStrategyModule<?, LETTER> createIpTcStrategyModulePdr() {
		return createModuleWrapperIfNecessary(new IpTcStrategyModulePdr<>(mLogger, mPrecondition, mPostcondition,
				mCounterexample, mPredicateUnifier, mPrefs));
	}

	public IIpTcStrategyModule<?, LETTER> createIpTcStrategyModulePreferences() {
		return createModuleWrapperIfNecessary(new IpTcStrategyModulePreferences<>(mTaskIdentifier, mServices, mPrefs,
				mCounterexample, mPrecondition, mPostcondition,
				new AssertionOrderModulation<>(mPathProgramCache, mLogger, mPrefs.getAssertCodeBlockOrder()),
				mPredicateUnifier, mPredicateFactory));
	}

	private IIpTcStrategyModule<?, LETTER>
			createModuleWrapperIfNecessary(final IIpTcStrategyModule<?, LETTER> trackStrategyModule) {
		final boolean useInterpolantConsolidation = mPrefs.getUseInterpolantConsolidation();
		if (useInterpolantConsolidation) {
			isOnlyDefaultPrePostConditions();
			return new IpTcStrategyModuleInterpolantConsolidation<>(mServices, mLogger, mPrefs, mPredicateFactory,
					trackStrategyModule);
		}
		return trackStrategyModule;
	}

	public IIpAbStrategyModule<LETTER> createInterpolantAutomatonBuilderStrategyModulePreferences(
			final IIpTcStrategyModule<?, LETTER> preferenceIpTc) {
		return createInterpolantAutomatonBuilderStrategyModulePreferences(mTaPrefs.interpolantAutomaton(),
				preferenceIpTc);
	}

	@SuppressWarnings("unchecked")
	private IIpAbStrategyModule<LETTER> createInterpolantAutomatonBuilderStrategyModulePreferences(
			final InterpolantAutomaton setting, final IIpTcStrategyModule<?, LETTER> preferenceIpTc) {
		final InterpolantAutomaton realSetting =
				mTaPrefs.overrideInterpolantAutomaton() ? mTaPrefs.interpolantAutomaton() : setting;
		switch (realSetting) {
		case STRAIGHT_LINE:
			return new IpAbStrategyModuleStraightlineAll<>(mServices, mAbstraction, mCounterexample,
					mEmptyStackFactory);
		case CANONICAL:
			return new IpAbStrategyModuleCanonical<>(mServices, mLogger, mAbstraction, mCounterexample,
					mEmptyStackFactory, mPredicateUnifier);
		case TOTALINTERPOLATION2:
			return new IpAbStrategyModuleTotalInterpolation<>(mServices, mAbstraction, mCounterexample,
					mPredicateUnifier, mPrefs, mCsToolkit, mPredFacInterpolAut);
		case ABSTRACT_INTERPRETATION:
			final IIpTcStrategyModule<?, LETTER> strategy =
					preferenceIpTc == null ? createIpTcStrategyModulePreferences() : preferenceIpTc;
			return new IpAbStrategyModuleAbstractInterpretation<>(mAbstraction, mCounterexample, mPredicateUnifier,
					(IpTcStrategyModuleAbstractInterpretation<LETTER>) strategy, mEmptyStackFactory);
		case MCR:
			final IHoareTripleChecker htc = TraceAbstractionUtils.constructEfficientHoareTripleChecker(mServices,
					TraceAbstractionPreferenceInitializer.HoareTripleChecks.MONOLITHIC, mCsToolkit, mPredicateUnifier);
			return new IpAbStrategyModuleMcr<>(mCounterexample.getWord().asList(), mPredicateUnifier,
					mEmptyStackFactory, mServices, mLogger, mAbstraction.getAlphabet(), createMcrInterpolantProvider(),
					htc);
		case TOTALINTERPOLATION:
		default:
			throw new IllegalArgumentException("Setting " + mTaPrefs.interpolantAutomaton() + " is unsupported");
		}
	}

	private IInterpolantProvider<LETTER> createMcrInterpolantProvider() {
		if (mTaPrefs.useInterpolationForMcr()) {
			return new IpInterpolantProvider<>(mPrefs, mPredicateUnifier, mPredicateFactory,
					new AssertionOrderModulation<>(mPathProgramCache, mLogger), mTaskIdentifier);
		}
		return new WpInterpolantProvider<>(mPrefs.getUltimateServices(), mLogger,
				mPrefs.getCfgSmtToolkit().getManagedScript(), mPrefs.getSimplificationTechnique(),
				mPrefs.getXnfConversionTechnique(), mPredicateUnifier);
	}

	public IIpAbStrategyModule<LETTER> createIpAbStrategyModuleStraightlineAll() {
		return createInterpolantAutomatonBuilderStrategyModulePreferences(InterpolantAutomaton.STRAIGHT_LINE, null);
	}

	public IIpAbStrategyModule<LETTER> createIpAbStrategyModuleAbstractInterpretation(
			final IpTcStrategyModuleAbstractInterpretation<LETTER> ipTcStrategyModuleAbsInt) {
		return createInterpolantAutomatonBuilderStrategyModulePreferences(InterpolantAutomaton.ABSTRACT_INTERPRETATION,
				null);
	}

	public IIpAbStrategyModule<LETTER> createIpAbStrategyModuleTotalInterpolation() {
		return createInterpolantAutomatonBuilderStrategyModulePreferences(InterpolantAutomaton.TOTALINTERPOLATION,
				null);
	}

	public IIpAbStrategyModule<LETTER> createIpAbStrategyModuleCanonical() {
		return createInterpolantAutomatonBuilderStrategyModulePreferences(InterpolantAutomaton.CANONICAL, null);
	}

	public IIpAbStrategyModule<LETTER> createIpAbStrategyModuleMcr() {
		return createInterpolantAutomatonBuilderStrategyModulePreferences(InterpolantAutomaton.MCR, null);
	}

	public TermClassifier getTermClassifierForTrace() {
		return TraceCheckUtils.classifyTermsInTrace(mCounterexample.getWord(), mCsToolkit.getSmtFunctionsAndAxioms());
	}

	public IPredicateUnifier getDefaultPredicateUnifier() {
		return mPredicateUnifier;
	}

	private void isOnlyDefaultPrePostConditions() {
		if (!SmtUtils.isTrueLiteral(mPrecondition.getFormula())) {
			throw new UnsupportedOperationException("Currently, only precondition true is supported");
		}
		if (!SmtUtils.isFalseLiteral(mPostcondition.getFormula())) {
			throw new UnsupportedOperationException("Currently, only postcondition false is supported");
		}
	}

}
