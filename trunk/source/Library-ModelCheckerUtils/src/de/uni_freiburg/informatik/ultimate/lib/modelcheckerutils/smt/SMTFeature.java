/*
 * Copyright (C) 2019 Julian Löffler (loefflju@informatik.uni-freiburg.de), Breee@github
 * Copyright (C) 2012-2019 University of Freiburg
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

package de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * Struct to store a featurevector which contains several properties of a SMT Term.
 *
 * @author Julian Löffler
 *
 */
public class SMTFeature {
	public int numberOfFunctions = 0;
	public int numberOfQuantifiers = 0;
	public int numberOfVariables = 0;
	public int numberOfArrays = 0;
	public int dagsize = 0;
	public long treesize = 0;
	public int dependencyScore = 0;
	public ArrayList<Integer> variableEquivalenceClassSizes;
	public int biggestEquivalenceClass;
	public Set<String> occuringSorts = Collections.emptySet();
	public Set<String> occuringFunctions = Collections.emptySet();
	public Set<Integer> occuringQuantifiers = Collections.emptySet();
	public boolean containsArrays = false;
	public ArrayList<String> assertionStack = new ArrayList<>();
	public int assertionStackHashCode = 0;
	public String solverresult = "";
	public double solvertime = 0.0;

	@Override
	public String toString() {
		try {
			final StringBuilder sb = new StringBuilder();
			sb.append("\n" + getCsvHeader(";") + "\n");
			sb.append(toCsv(";"));
			return sb.toString();
		} catch (final IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public String toCsv(final String delimiter) throws IllegalAccessException {
		final StringBuilder sb = new StringBuilder();
		final Field[] fields = getClass().getDeclaredFields();
		final ArrayList<String> values = new ArrayList<>();
		for (final Field field : fields) {
			values.add(field.get(this).toString());
		}
		sb.append(String.join(delimiter,values));
		return sb.toString();
	}
	public static String getCsvHeader(final String delimiter) throws IllegalAccessException {
		final StringBuilder sb = new StringBuilder();
		final Field[] fields = SMTFeature.class.getFields();
		final ArrayList<String> names = new ArrayList<>();
		for (final Field field : fields) {
			names.add(field.getName());
		}
		sb.append(String.join(delimiter,names));
		return sb.toString();
	}

}
