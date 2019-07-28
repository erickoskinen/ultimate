/*
 * Copyright (C) 2019 Claus Schätzle (schaetzc@tf.uni-freiburg.de)
 * Copyright (C) 2019 University of Freiburg
 *
 * This file is part of the ULTIMATE Library-SymbolicInterpretation plug-in.
 *
 * The ULTIMATE Library-SymbolicInterpretation plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE Library-SymbolicInterpretation plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Library-SymbolicInterpretation plug-in. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Library-SymbolicInterpretation plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE Library-SymbolicInterpretation plug-in grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.lib.symbolicinterpretation.summarizers;

import java.util.HashMap;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.lib.symbolicinterpretation.DagInterpreter;
import de.uni_freiburg.informatik.ultimate.lib.symbolicinterpretation.ProcedureResourceCache;
import de.uni_freiburg.informatik.ultimate.lib.symbolicinterpretation.ProcedureResources;
import de.uni_freiburg.informatik.ultimate.lib.symbolicinterpretation.SymbolicTools;
import de.uni_freiburg.informatik.ultimate.lib.symbolicinterpretation.cfgpreprocessing.CallReturnSummary;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;

/**
 * Computes call summaries ignoring the actual call's input and using only true as an input.
 * Summaries computed once are cached and re-used.
 *
 * @author schaetzc@tf.uni-freiburg.de
 */
public class TopInputCallSummarizer implements ICallSummarizer {

	private final SymbolicTools mTools;
	private final ProcedureResourceCache mProcResCache;
	private final DagInterpreter mDagIpreter;

	private final Map<String, IPredicate> mProcToSummary = new HashMap<>();

	public TopInputCallSummarizer(final SymbolicTools tools, final ProcedureResourceCache procResCache,
			final DagInterpreter dagIpreter) {
		mTools = tools;
		mProcResCache = procResCache;
		mDagIpreter = dagIpreter;
	}

	@Override
	public IPredicate summarize(final CallReturnSummary callSumTrans, final IPredicate unusedInput) {
		return mProcToSummary.computeIfAbsent(callSumTrans.calledProcedure(), this::computeTopSummary);
	}

	private IPredicate computeTopSummary(final String procedure) {
		final ProcedureResources res = mProcResCache.resourcesOf(procedure);
		return mDagIpreter.interpret(res.getRegexDag(), res.getDagOverlayPathToReturn(), mTools.top());
	}
}