/*
 * Copyright (C) 2021 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2021 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.icfgtransformer.loopacceleration.jordan;

import java.util.Collection;

import de.uni_freiburg.informatik.ultimate.util.statistics.IStatisticsDataProvider;
import de.uni_freiburg.informatik.ultimate.util.statistics.IStatisticsType;

public class JordanLoopAccelerationStatisticsGenerator implements IStatisticsDataProvider {

	private int mSequentialAcceleration;
	private int mQuantifierFreeResult;
	private int mAlternatingAcceleration;

	public JordanLoopAccelerationStatisticsGenerator() {
		super();
		mSequentialAcceleration = 0;
		mQuantifierFreeResult = 0;
		mAlternatingAcceleration = 0;
	}

	@Override
	public Object getValue(final String key) {
		final JordanLoopAccelerationDefinitions keyEnum = Enum.valueOf(JordanLoopAccelerationDefinitions.class, key);
		switch (keyEnum) {
		case SequentialAcceleration:
			return mSequentialAcceleration;
		case QuantifierFreeResult:
			return mQuantifierFreeResult;
		case AlternatingAcceleration:
			return mAlternatingAcceleration;
		default:
			throw new AssertionError("unknown data");
		}
	}

	@Override
	public IStatisticsType getBenchmarkType() {
		return JordanLoopAccelerationStatisticsType.getInstance();
	}

	@Override
	public Collection<String> getKeys() {
		return getBenchmarkType().getKeys();
	}

	public void reportSequentialAcceleration() {
		mSequentialAcceleration++;
	}

	public void reportAlternatingAcceleration() {
		mAlternatingAcceleration++;
	}

	public void reportQuantifierFreeResult() {
		mQuantifierFreeResult++;
	}

}
