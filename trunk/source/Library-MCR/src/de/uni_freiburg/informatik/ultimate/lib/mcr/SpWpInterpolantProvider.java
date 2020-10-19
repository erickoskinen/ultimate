package de.uni_freiburg.informatik.ultimate.lib.mcr;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.transitions.OutgoingInternalTransition;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.structure.IIcfgTransition;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.cfg.transitions.TransFormula;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.MultiDimensionalSelectOverStoreEliminationUtils;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.IPredicateUnifier;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.PredicateTransformer;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.TermDomainOperationProvider;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.ManagedScript;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.QuantifierUtils;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils.SimplificationTechnique;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.SmtUtils.XnfConversionTechnique;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.arrays.ArrayIndexEqualityManager;
import de.uni_freiburg.informatik.ultimate.lib.smtlibutils.arrays.MultiDimensionalSelectOverNestedStore;
import de.uni_freiburg.informatik.ultimate.logic.QuantifiedFormula;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.util.datastructures.ThreeValuedEquivalenceRelation;
import de.uni_freiburg.informatik.ultimate.util.datastructures.poset.TopologicalSorter;

/**
 * IInterpolantProvider using sp or wp. For better interpolants, we abstract all variables away, that are not read
 * afterwards.
 *
 * @author Frank Schüssele (schuessf@informatik.uni-freiburg.de)
 */
public abstract class SpWpInterpolantProvider<LETTER extends IIcfgTransition<?>>
		implements IInterpolantProvider<LETTER> {

	private final ManagedScript mManagedScript;
	private final IUltimateServiceProvider mServices;
	private final ILogger mLogger;
	private final SimplificationTechnique mSimplificationTechnique;
	private final XnfConversionTechnique mXnfConversionTechnique;
	private final IPredicateUnifier mPredicateUnifier;
	private final ArrayIndexEqualityManager mAiem;

	protected final PredicateTransformer<Term, IPredicate, TransFormula> mPredicateTransformer;
	protected final Script mScript;

	public SpWpInterpolantProvider(final IUltimateServiceProvider services, final ILogger logger,
			final ManagedScript managedScript, final SimplificationTechnique simplificationTechnique,
			final XnfConversionTechnique xnfConversionTechnique, final IPredicateUnifier predicateUnifier) {
		mManagedScript = managedScript;
		mScript = mManagedScript.getScript();
		mServices = services;
		mLogger = logger;
		mSimplificationTechnique = simplificationTechnique;
		mXnfConversionTechnique = xnfConversionTechnique;
		mPredicateUnifier = predicateUnifier;
		mPredicateTransformer =
				new PredicateTransformer<>(mManagedScript, new TermDomainOperationProvider(mServices, mManagedScript));
		// ArrayIndexEqualityManager to eliminate stores with true as context (i.e. without any known equalities)
		mAiem = new ArrayIndexEqualityManager(new ThreeValuedEquivalenceRelation<>(), mScript.term("true"),
				QuantifiedFormula.EXISTS, mLogger, mManagedScript);
		mAiem.unlockSolver();
	}

	private static <STATE> List<STATE> revTopSort(final INestedWordAutomaton<?, STATE> automaton,
			final Map<STATE, ?> stateMap) {
		final Map<STATE, Set<STATE>> successors = new HashMap<>();
		for (final STATE state : automaton.getStates()) {
			if (stateMap.containsKey(state)) {
				continue;
			}
			final Set<STATE> succs = new HashSet<>();
			for (final OutgoingInternalTransition<?, STATE> edge : automaton.internalSuccessors(state)) {
				final STATE succ = edge.getSucc();
				if (!stateMap.containsKey(succ)) {
					succs.add(succ);
				}
			}
			successors.put(state, succs);
		}
		return new TopologicalSorter<>(successors::get).reversedTopologicalOrdering(successors.keySet()).get();
	}

	@Override
	public <STATE> void addInterpolants(final INestedWordAutomaton<LETTER, STATE> automaton,
			final Map<STATE, IPredicate> states2Predicates) {
		// Collect all variable from the interpolants to be read afterwards, all others can be ignored
		final Set<TermVariable> ipVars = new HashSet<>();
		final Map<STATE, Set<TermVariable>> liveIpVariables = new HashMap<>();
		for (final Entry<STATE, IPredicate> entry : states2Predicates.entrySet()) {
			final Set<TermVariable> vars = McrUtils.getTermVariables(entry.getValue().getVars());
			ipVars.addAll(vars);
			liveIpVariables.put(entry.getKey(), vars);
		}
		final List<STATE> order = revTopSort(automaton, states2Predicates);
		for (final STATE state : order) {
			final Set<TermVariable> vars = new HashSet<>();
			for (final OutgoingInternalTransition<LETTER, STATE> edge : automaton.internalSuccessors(state)) {
				vars.addAll(McrUtils.getTermVariables(edge.getLetter().getTransformula().getInVars().keySet()));
				vars.addAll(liveIpVariables.get(edge.getSucc()));
			}
			vars.retainAll(ipVars);
			liveIpVariables.put(state, vars);
		}
		if (!useReversedOrder()) {
			Collections.reverse(order);
		}
		// Caluculate sp/wp for all states in the given order
		for (final STATE state : order) {
			final Term term = calculateTerm(automaton, state, states2Predicates);
			if (term == null) {
				continue;
			}
			// Abstract all variables away that are not read afterwards and do not occur in the original interpolants
			Term result = McrUtils.abstractVariables(term, liveIpVariables.get(state), getQuantifier(), mServices,
					mLogger, mManagedScript, mSimplificationTechnique, mXnfConversionTechnique);
			// Ignore the interpolant, if it still contains quantifiers
			if (!QuantifierUtils.isQuantifierFree(result)) {
				continue;
			}
			// Eliminate all stores (using case distinction on index equalities)
			final List<MultiDimensionalSelectOverNestedStore> stores =
					MultiDimensionalSelectOverNestedStore.extractMultiDimensionalSelectOverStores(mScript, result);
			for (final MultiDimensionalSelectOverNestedStore m : stores) {
				result = MultiDimensionalSelectOverStoreEliminationUtils.replace(mManagedScript, mAiem, result, m);
			}
			states2Predicates.put(state, mPredicateUnifier.getOrConstructPredicate(result));
		}
	}

	protected abstract <STATE> Term calculateTerm(final INestedWordAutomaton<LETTER, STATE> automaton, STATE state,
			Map<STATE, IPredicate> stateMap);

	protected abstract int getQuantifier();

	protected abstract boolean useReversedOrder();
}
