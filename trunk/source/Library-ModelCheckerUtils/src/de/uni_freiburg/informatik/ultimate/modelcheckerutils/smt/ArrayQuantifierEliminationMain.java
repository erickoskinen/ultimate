/*
 * Copyright (C) 2018-2019 Max Barth (Max.Barth95@gmx.de)
 * Copyright (C) 2018-2019 University of Freiburg
 *
 * This file is part of the ULTIMATE ModelCheckerUtils Library.
 *
 * The ULTIMATE ModelCheckerUtils Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE ModelCheckerUtils Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE ModelCheckerUtils Library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE ModelCheckerUtils Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE ModelCheckerUtils Library grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.Collections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.ConstantTerm;
import de.uni_freiburg.informatik.ultimate.logic.QuantifiedFormula;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.ModelCheckerUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.SimplificationTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.ArrayIndex;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalSelect;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalStore;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.NestedArrayStore;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearterms.PrenexNormalForm;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearterms.QuantifierPusher;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearterms.QuantifierPusher.PqeTechniques;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearterms.QuantifierSequence;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearterms.QuantifierSequence.QuantifiedVariables;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.normalforms.NnfTransformer;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.normalforms.NnfTransformer.QuantifierHandling;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.pqe.XnfDer;
import de.uni_freiburg.informatik.ultimate.util.datastructures.EqualityStatus;
import de.uni_freiburg.informatik.ultimate.util.datastructures.ThreeValuedEquivalenceRelation;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.Pair;

public class ArrayQuantifierEliminationMain {

	private final ManagedScript mMgdScript;
	private final IUltimateServiceProvider mServices;
	private final ILogger mLogger;
	private final SimplificationTechnique mSimplificationTechnique;
	private final int mRecursiveCallCounter = -1;
	private final Script mScript;
	private Set<TermVariable> mEliminatees;

	public ArrayQuantifierEliminationMain(final ManagedScript mgdScript, final IUltimateServiceProvider services,
			final SimplificationTechnique simplificationTechnique) {
		super();
		mMgdScript = mgdScript;
		mScript = mMgdScript.getScript();
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(ModelCheckerUtils.PLUGIN_ID);
		mSimplificationTechnique = simplificationTechnique;
		mEliminatees = new HashSet<TermVariable>();
	}

	/*
	 * Main Method of the Array Quantifier Elimination For each quantified Array
	 * variable - it eliminates one Store Terms - it eliminates all Select Terms -
	 * it checks the Input Term for Inconsistents Input/Output: is an
	 * EliminationTask
	 */
	public EliminationTask elimAllRec(EliminationTask eTask) {
		mEliminatees.addAll(eTask.getEliminatees());

		System.out.print("New Array Elimination USED\n");
		EliminationTask result = eTask;
		System.out.print("Zu eliminieren: " + eTask.getEliminatees() + "\n");
		System.out.print("Term: " + eTask.getTerm() + "\n");
		if (eTask.getTerm() instanceof ApplicationTerm) {
			final ApplicationTerm appterm = (ApplicationTerm) eTask.getTerm();

			final ThreeValuedEquivalenceRelation tVER = calcThreeValuedEquiRel(result); // TODO use in all Index
																						// comparisons
			if (SmtUtils.isFunctionApplication(eTask.getTerm(), "and") && (eTask.getQuantifier() == 0)) {
				// TODO QuantifierUtils nutzen
				if ((tVER.isInconsistent()) && (!tVER.getAllElements().isEmpty())) {
					result = new EliminationTask(result.getQuantifier(), result.getEliminatees(),
							mScript.term("false"));
				}
			} else if (SmtUtils.isFunctionApplication(eTask.getTerm(), "or") && (eTask.getQuantifier() == 1)) {
				if ((tVER.isTautological()) && (!tVER.getAllElements().isEmpty())) {
					result = new EliminationTask(result.getQuantifier(), result.getEliminatees(), mScript.term("true"));
				}
			}

			EliminationTask recTerm = new EliminationTask(eTask.getQuantifier(), eTask.getEliminatees(),
					eTask.getTerm());

			Term taskTerm = recTerm.getTerm();
			for (TermVariable array : arraySelection(eTask, tVER)) {

				taskTerm = selectOverStore(taskTerm, tVER, array);
				taskTerm = storeOverStore(taskTerm, tVER, array);
				result = new EliminationTask(eTask.getQuantifier(), mEliminatees, taskTerm);
				result = elim1StoreQuantifier(result, array);
			}

			final Term nnf = new NnfTransformer(mMgdScript, mServices, QuantifierHandling.KEEP)
					.transform(result.getTerm());
			final Term pushed = new QuantifierPusher(mMgdScript, mServices, true, PqeTechniques.ALL_LOCAL)
					.transform(nnf);
			final Term pnf = new PrenexNormalForm(mMgdScript).transform(pushed);
			final QuantifierSequence qs = new QuantifierSequence(mMgdScript.getScript(), pnf);
			final Term matrix = qs.getInnerTerm();
			final List<QuantifiedVariables> qvs = qs.getQuantifierBlocks();
			Term asdasdasd = matrix;
			for (int i = qvs.size() - 1; i >= 0; i--) {
				final QuantifiedVariables qv = qvs.get(i);
				final Set<TermVariable> eliminatees = new HashSet<>(qv.getVariables());
				asdasdasd = SmtUtils.simplify(mMgdScript, asdasdasd, mServices, mSimplificationTechnique);
				EliminationTask recresult = new EliminationTask(qv.getQuantifier(), eliminatees, asdasdasd);
				recresult = elimAllRec(recresult);
				asdasdasd = recresult.getTerm();
				asdasdasd = SmtUtils.quantifier(mMgdScript.getScript(), qv.getQuantifier(), eliminatees, asdasdasd);
				asdasdasd = new QuantifierPusher(mMgdScript, mServices, true, PqeTechniques.ONLY_DER)
						.transform(asdasdasd);

			}
			result = new EliminationTask(eTask.getQuantifier(), mEliminatees, asdasdasd);

		}
		assert !Arrays.asList(result.getTerm().getFreeVars())
				.contains(eTask.getEliminatees()) : "var is	 still there";

		return result;

	}

	private Set<TermVariable> arraySelection(EliminationTask eTask, ThreeValuedEquivalenceRelation tVER) {
		// Braucht quantifiedFormula
		// ArrayIndexBasedCostEstimation aibce = new ArrayIndexBasedCostEstimation();
		// ArrayIndexEqualityManager aiem = new ArrayIndexEqualityManager(tVER,
		// eTask.getTerm(), mRecursiveCallCounter, mLogger, mMgdScript);
		// TreeRelation<Integer, TermVariable> arrayTree =
		// aibce.computeCostEstimation(aiem , eTask.getEliminatees(), eTask.getTerm());
		Set<TermVariable> arrayOrder = eTask.getEliminatees();
		return arrayOrder;
	}

	/*
	 *
	 * TODO Store over Store rekursiv. Nur eliminieren, wenn Indexe Gleich.
	 * ThreeValueEq nutzen
	 */
	private Term storeOverStore(final Term term, final ThreeValuedEquivalenceRelation tVER, TermVariable qarray) {
		final MultiDimensionalStore mds = new MultiDimensionalStore(term);
		final List<MultiDimensionalStore> Storeterms = mds.extractArrayStoresShallow(term);
		for (final MultiDimensionalStore storeOuter : Storeterms) {

			NestedArrayStore nas = NestedArrayStore.convert(storeOuter.getStoreTerm());
			if (nas.getIndices().size() > 1) {

				if (qarray.equals(nas.getArray())) {

					if (testIndexVarTerm(nas.getIndices().get(0), nas.getIndices().get(1), tVER)) {
						if (tVER.getEqualityStatus(nas.getIndices().get(0),
								nas.getIndices().get(1)) == (EqualityStatus.EQUAL)) {

							final Term newStore = SmtUtils.store(mScript, nas.getArray(), nas.getIndices().get(0),
									nas.getValues().get(1));

							// Substitude newStore
							final Substitution sub = new Substitution(mMgdScript,
									Collections.singletonMap(storeOuter.getStoreTerm(), newStore));
							final Term noSOSterm = sub.transform(term);

							return storeOverStore(noSOSterm, tVER, qarray);
						}
					}
				}

				TermVariable newarrayvar = mMgdScript.constructFreshTermVariable("a_sos",
						storeOuter.getArray().getSort());
				Term innerStore = SmtUtils.store(mScript, nas.getArray(), nas.getIndices().get(0),
						nas.getValues().get(0));
				final Substitution sub = new Substitution(mMgdScript,
						Collections.singletonMap(innerStore, newarrayvar));

				Term noSOSterm = sub.transform(term);
				mEliminatees.add(newarrayvar);
				// Index comparison Equality
				Term factorisedStore = SmtUtils.binaryEquality(mScript, newarrayvar, innerStore);
				Term newTerm = SmtUtils.and(mScript, factorisedStore, noSOSterm);

				return storeOverStore(newTerm, tVER, qarray);

			}

		}

		return term;

	}

	private Term selectOverStore(final Term term, final ThreeValuedEquivalenceRelation tVER, TermVariable qarray) {
		final MultiDimensionalSelect mds = new MultiDimensionalSelect(term);
		final List<MultiDimensionalSelect> Selectterms = mds.extractSelectDeep(term, false);
		for (final MultiDimensionalSelect select : Selectterms) {
			// if Array is BasicArray, its no SelectOverStore
			if (!SmtUtils.isBasicArrayTerm(select.getArray())) {
				final MultiDimensionalStore innerStore = new MultiDimensionalStore(select.getArray());
				if (innerStore != null) {
					NestedArrayStore nas = NestedArrayStore.convert(innerStore.getStoreTerm());

					if (qarray.equals(nas.getArray())) {
						Term disjunction = mScript.term("false");
						Term allindexnoteq = mScript.term("true");
						for (int i = nas.getIndices().size() - 1; i >= 0; i--) {
							Term index = nas.getIndices().get(i);
							Term indexeq = SmtUtils.binaryEquality(mScript, index, select.getIndex().get(0));

							if (testIndexVarTerm(index, select.getIndex().get(0), tVER)) {

								if (tVER.getDisequalities().containsPair(index, select.getIndex().get(0))) {
									indexeq = mScript.term("false");

								} else if (tVER.getEqualityStatus(index, select.getIndex().get(0))
										.compareTo(EqualityStatus.EQUAL) == 0) {
									indexeq = mScript.term("true");

								}
							}

							Term indexnoteq = SmtUtils.not(mScript, indexeq);

							final Substitution sub = new Substitution(mMgdScript, Collections.singletonMap(
									select.getSelectTerm(), nas.getValues().get(nas.getIndices().indexOf(index))));
							final Term subtermlhs = sub.transform(term);
							final Term lhs = SmtUtils.and(mScript, indexeq, subtermlhs, allindexnoteq);
							disjunction = SmtUtils.or(mScript, lhs, disjunction);
							allindexnoteq = SmtUtils.and(mScript, indexnoteq, allindexnoteq);
						}

						final Substitution sub = new Substitution(mMgdScript,
								Collections.singletonMap(select.getSelectTerm(),
										SmtUtils.select(mScript, nas.getArray(), select.getIndex().get(0))));
						final Term subtermrhs = sub.transform(term);

						final Term rhs = SmtUtils.and(mScript, allindexnoteq, subtermrhs);
						Term newterm = SmtUtils.or(mScript, disjunction, rhs);
						// newterm = SmtUtils.simplify(mMgdScript, newterm, mServices,
						// mSimplificationTechnique);
						return newterm;
					}
				}
			}
		}
		return term;
	}

	private boolean testIndexVarTerm(final Term index1, final Term index2, ThreeValuedEquivalenceRelation tVER) {
		if (tVER.getAllElements().contains(index1)) {
			if (tVER.getAllElements().contains(index2)) {
				if (!(index1 instanceof ConstantTerm)) {
					if (!(index2 instanceof ConstantTerm)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/*
	 * Stores all Equalities / Disequalities of an conjunction and Checks for
	 * Inconsistens. Equalitys in the Input Formula are always valid. Equalitys only
	 * count in one Disjunction
	 */

	private ThreeValuedEquivalenceRelation calcThreeValuedEquiRel(final EliminationTask eTask) {
		final ApplicationTerm appterm = (ApplicationTerm) eTask.getTerm();
		final ThreeValuedEquivalenceRelation<Term> tVER = new ThreeValuedEquivalenceRelation<Term>();
		if (SmtUtils.isFunctionApplication(eTask.getTerm(), "and")) {
			for (final Term term : appterm.getParameters()) {
				if (term.getSort().getName().equals("Bool")) {
					final ApplicationTerm boolterm = (ApplicationTerm) term;
					if (boolterm.getFunction().getName().equals("=")) {
						// Add Equality
						tVER.addElement(boolterm.getParameters()[0]);
						tVER.addElement(boolterm.getParameters()[1]);
						tVER.reportEquality(boolterm.getParameters()[0], boolterm.getParameters()[1]);

					} else if (boolterm.getFunction().getName().equals("not")) {
						// Add Disequality
						final ApplicationTerm eqterm = (ApplicationTerm) boolterm.getParameters()[0];
						tVER.addElement(eqterm.getParameters()[0]);
						tVER.addElement(eqterm.getParameters()[1]);
						tVER.reportDisequality(eqterm.getParameters()[0], eqterm.getParameters()[1]);
					}
				}
			}
		} // todo "or" ??
		return tVER;
	}

	/*
	 * Array elimination with at least 2 select terms
	 */
	public EliminationTask elimMultiSelectNaiv(Term eTerm, final TermVariable eliminate, final int quantifier) {

		if (eTerm instanceof ApplicationTerm) {
			// Get Select Terms
			Set<ApplicationTerm> selectterms = new ApplicationTermFinder("select", false).findMatchingSubterms(eTerm);

			System.out.print("Selecttermssasas: " + selectterms + "\n");
			// Build Select Term Combinations without repetition, with quantified array as
			// argument.
			final Set<Pair<ApplicationTerm, ApplicationTerm>> IndexCombinations = new HashSet<Pair<ApplicationTerm, ApplicationTerm>>();
			for (final ApplicationTerm i : selectterms) {
				for (final ApplicationTerm j : selectterms) {

					if ((eliminate.equals(i.getParameters()[0])) && (eliminate.equals(j.getParameters()[0]))) {
						if (i != j) {
							if (!IndexCombinations.contains(new Pair<ApplicationTerm, ApplicationTerm>(j, i))) {
								IndexCombinations.add(new Pair<ApplicationTerm, ApplicationTerm>(i, j));
							}
						}
					}
				}
			}
			for (final ApplicationTerm i : selectterms) {

				if (eliminate.equals(i.getParameters()[0])) {
					if (IndexCombinations.isEmpty()) {
						IndexCombinations.add(new Pair<ApplicationTerm, ApplicationTerm>(i, i));
					}
				}
			}

			// Build Up Term: Implikation
			final Set<TermVariable> neweliminatees = new HashSet<TermVariable>();
			Term newTerm = mScript.term("true");
			final Map<Term, Term> submap = new HashMap();
			System.out.print("IndexCombinations: " + IndexCombinations + "\n");
			if (!IndexCombinations.isEmpty()) { // else elimiantion nicht möglich??!! TODO
				for (final Pair<ApplicationTerm, ApplicationTerm> comb : IndexCombinations) {
					// new Exists Quantified variables: si_counter / sj_counter
					TermVariable si = mMgdScript.constructFreshTermVariable("si", comb.getFirst().getSort());
					TermVariable sj = mMgdScript.constructFreshTermVariable("sj", comb.getFirst().getSort());

					if (!submap.containsKey(comb.getFirst())) {
						neweliminatees.add(si);
						submap.put(comb.getFirst(), si);
					} else {
						si = (TermVariable) submap.get(comb.getFirst());
					}
					if (!submap.containsKey(comb.getSecond())) {
						neweliminatees.add(sj);
						submap.put(comb.getSecond(), sj);
					} else {
						sj = (TermVariable) submap.get(comb.getSecond());
					}

					final ArrayList<Term> indexi = new ArrayList<Term>();
					indexi.add(comb.getFirst().getParameters()[1]);
					final ArrayList<Term> indexj = new ArrayList<Term>();
					indexj.add(comb.getSecond().getParameters()[1]);
					ArrayIndex indexii = new ArrayIndex(indexi);
					ArrayIndex indexjj = new ArrayIndex(indexj);

					Term iEvE = SmtUtils.indexEqualityImpliesValueEquality(mScript, indexii, indexjj, si, sj);
					if (quantifier == QuantifiedFormula.FORALL) {
						iEvE = SmtUtils.not(mScript, iEvE);
					}

					newTerm = SmtUtils.and(mScript, iEvE, newTerm);

				}

				final Substitution sub = new Substitution(mMgdScript, submap);
				final Term newTerm2 = sub.transform(eTerm);
				if (quantifier == QuantifiedFormula.FORALL) {
					newTerm = SmtUtils.or(mScript, newTerm2, newTerm);
				} else {
					newTerm = SmtUtils.and(mScript, newTerm2, newTerm);
				}

				System.out.print("newTerm: " + newTerm.toStringDirect() + "\n");

				neweliminatees.add(eliminate); // add all not elimiatet
				// quantified variables back

				newTerm = SmtUtils.quantifier(mScript, quantifier, neweliminatees, newTerm);

				final EliminationTask result = new EliminationTask(quantifier, neweliminatees, newTerm);
				return result;
			}
		}
		final Set<TermVariable> oldeliminatees = new HashSet<TermVariable>();
		oldeliminatees.add(eliminate);
		final EliminationTask result = new EliminationTask(quantifier, oldeliminatees, eTerm);
		return result;
	}

	/*
	 * Eliminates one Store operation (Store a i v) with an quantified Array "a". -
	 * It replaces the Store term (Store a i v) with a new Exists quantified Array
	 * Variable a_new and adds the Conjunct "a_new = (Store a i v)".
	 *
	 * - We collect all Indices of the Input Term - We Build a new SubTerm - We
	 * eliminate the new Index Forall Quantifier with the collected Indices. - We
	 * Replace "a_new = (Store a i v)" with the new SubTerm.
	 *
	 * Returns an EliminationTask. Its Term has no more Store Operation and no new
	 * Quantifiers.
	 *
	 */

	private EliminationTask elim1StoreQuantifier(final EliminationTask eTask, TermVariable qarray) {
		final MultiDimensionalStore mds = new MultiDimensionalStore(eTask.getTerm());
		final List<MultiDimensionalStore> storeterms = mds.extractArrayStoresDeep(eTask.getTerm());
		Term newterm = eTask.getTerm();
		EliminationTask newETask = eTask;
		final Set<TermVariable> neweliminatees2 = new HashSet<TermVariable>();
		// for store new exists array a1 quantifier
		for (final MultiDimensionalStore term : storeterms) {
			if (qarray.equals(term.getArray())) {
				System.out.print("Store Elim array: " + term.getArray().toStringDirect() + "\n");
				final TermVariable newarrayvar = mMgdScript.constructFreshTermVariable("a_new",
						term.getArray().getSort());

				// Substitute Store term with new Exist quantified Array Variable a_new

				Substitution sub = new Substitution(mMgdScript,
						Collections.singletonMap(term.getStoreTerm(), newarrayvar));

				newterm = sub.transform(newterm);
				// Add conjunct a1 = (eliminated store term)
				final Term eqterm = SmtUtils.binaryEquality(mScript, newarrayvar, term.getStoreTerm());
				newterm = SmtUtils.and(mScript, newterm, eqterm);
				System.out.print("Store Elim factor out store: " + newterm.toStringDirect() + "\n");
				final TermVariable newindexvar = mMgdScript.constructFreshTermVariable("j",
						term.getStoreTerm().getParameters()[1].getSort());
				neweliminatees2.add(newindexvar);
				// Build new Term forall Indices of indexSet
				// Term 1: ((i != j) => (a_new[i] = a[i]))
				final Term indexnoteq = SmtUtils.not(mScript,
						SmtUtils.binaryEquality(mScript, newindexvar, term.getStoreTerm().getParameters()[1]));
				final Term arrayeq = SmtUtils.binaryEquality(mScript,
						SmtUtils.select(mScript, newarrayvar, newindexvar),
						SmtUtils.select(mScript, term.getArray(), newindexvar));
				final Term elimtermlhs = SmtUtils.implies(mScript, indexnoteq, arrayeq);
				// Term 2: ((i = j) => (a_new[i] = v))
				final Term indexeq = SmtUtils.binaryEquality(mScript, newindexvar,
						term.getStoreTerm().getParameters()[1]);
				final Term selectvalue = SmtUtils.binaryEquality(mScript,
						SmtUtils.select(mScript, newarrayvar, term.getStoreTerm().getParameters()[1]),
						term.getStoreTerm().getParameters()[2]);
				final Term elimtermrhs = SmtUtils.implies(mScript, indexeq, selectvalue);
				// Term 3: Term 1 AND Term 2
				final Term elimterm = SmtUtils.and(mScript, elimtermlhs, elimtermrhs);
				// Substitute Store term equality with the new Term "elimForall"
				sub = new Substitution(mMgdScript, Collections.singletonMap(eqterm, elimterm));
				newterm = sub.transform(newterm);

				// newterm = QuantifierUtils.transformToXnf(mServices, mScript, quantifier,
				// freshTermVariableConstructor, newterm, xnfConversionTechnique)

				// DER on a_new. To eliminate the new Exist Quantifier: "Exists a_new"
				final XnfDer xnfDer = new XnfDer(mMgdScript, mServices);
				final Term[] oldParams = QuantifierUtils.getXjunctsOuter(0, newterm);
				final Term[] newParams = new Term[oldParams.length];
				final Set<TermVariable> eliminateesDER = new HashSet<TermVariable>();
				eliminateesDER.add(newarrayvar);
				for (int i = 0; i < oldParams.length; i++) {

					final Term[] oldAtoms = QuantifierUtils.getXjunctsInner(0, oldParams[i]);
					newParams[i] = QuantifierUtils.applyDualFiniteConnective(mScript, 0,
							Arrays.asList(xnfDer.tryToEliminate(0, oldAtoms, eliminateesDER)));
				}

				newterm = QuantifierUtils.applyCorrespondingFiniteConnective(mScript, 0, newParams);

				if (Arrays.asList(newterm.getFreeVars()).contains(newarrayvar)) {
					mEliminatees.add(newarrayvar);
					newETask = elimMultiSelectNaiv(newterm, newarrayvar, 0);
				}
				System.out.print("Store Elim Naiv: " + newterm.toStringDirect() + "\n");

			}
		}

		// SELECT ELIM
		newETask = elimMultiSelectNaiv(newterm, qarray, eTask.getQuantifier());

		// Add Forall Quantorzz
		final Term termWForall = SmtUtils.quantifier(mScript, 1, neweliminatees2, newETask.getTerm());

		final EliminationTask result = new EliminationTask(newETask.getQuantifier(), newETask.getEliminatees(),
				termWForall);

		return result;
	}

}