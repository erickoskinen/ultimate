/*
 * Copyright (C) 2021 Dominik Klumpp (klumpp@informatik.uni-freiburg.de)
 * Copyright (C) 2021 University of Freiburg
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
package de.uni_freiburg.informatik.ultimate.automata.partialorder;

import java.util.function.Supplier;

import de.uni_freiburg.informatik.ultimate.automata.partialorder.IndependenceResultAggregator.Counter;
import de.uni_freiburg.informatik.ultimate.util.statistics.AbstractStatisticsDataProvider;
import de.uni_freiburg.informatik.ultimate.util.statistics.KeyType;
import de.uni_freiburg.informatik.ultimate.util.statistics.PrettyPrint;

/**
 * Collects statistics for independence relations. Implementors of {@link IIndependenceRelation} can use this class to
 * implement {@link IIndependenceRelation#getStatistics()}.
 *
 * This class collects data on how many queries were made, with what result, and whether or not they were conditional.
 * If more data should be collected, derive a subclass and add the additional fields using the mechanism described in
 * {@link AbstractStatisticsDataProvider#declare(String, java.util.function.Supplier, KeyType)}.
 *
 * @author Dominik Klumpp (klumpp@informatik.uni-freiburg.de)
 */
public class IndependenceStatisticsDataProvider extends AbstractStatisticsDataProvider {

	public static final String INDEPENDENCE_QUERIES = "Independence Queries";

	private final Counter mQueryCounter = new Counter();


	/**
	 * Create a new instance to collect data, with the default data fields.
	 */
	public IndependenceStatisticsDataProvider() {
		declareCounter(INDEPENDENCE_QUERIES, () -> mQueryCounter);
	}

	protected final void declareCounter(final String key, final Supplier<Counter> getter) {
		declare(key, getter::get, (x, y) -> Counter.sum((Counter) x, (Counter) y),
				(k, data) -> PrettyPrint.keyColonData(k, ((Counter) data).print(Object::toString)));
	}

	public Counter getQueries() {
		return mQueryCounter;
	}

	@Deprecated
	public long getPositiveQueries() {
		return mQueryCounter.getPositive();
	}

	@Deprecated
	public long getNegativeQueries() {
		return mQueryCounter.getNegative();
	}

	@Deprecated
	public long getUnknownQueries() {
		return mQueryCounter.getUnknown();
	}

	public void reportQuery(final boolean positive, final boolean conditional) {
		mQueryCounter.increment(positive, conditional);
	}

	public void reportPositiveQuery(final boolean conditional) {
		mQueryCounter.increment(true, conditional);
	}

	public void reportNegativeQuery(final boolean conditional) {
		mQueryCounter.increment(false, conditional);
	}

	public void reportUnknownQuery(final boolean conditional) {
		mQueryCounter.incrementUnknown(conditional);
	}
}
