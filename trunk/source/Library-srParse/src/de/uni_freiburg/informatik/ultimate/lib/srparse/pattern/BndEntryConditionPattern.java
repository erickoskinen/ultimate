package de.uni_freiburg.informatik.ultimate.lib.srparse.pattern;

import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.PhaseEventAutomata;
import de.uni_freiburg.informatik.ultimate.lib.pea.reqcheck.PatternToPEA;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;

public class BndEntryConditionPattern extends PatternType {

	public BndEntryConditionPattern(final SrParseScope scope, final String id, final List<CDD> cdds,
			final List<String> durations) {
		super(scope, id, cdds, durations);
	}

	@Override
	public PhaseEventAutomata transform(final PatternToPEA peaTrans, final Map<String, Integer> id2bounds) {
		final CDD p_cdd = getCdds().get(1);
		final CDD q_cdd = getScope().getCdd1();
		final CDD r_cdd = getScope().getCdd2();
		final CDD s_cdd = getCdds().get(0);

		return peaTrans.bndEntryConditionPattern(getId(), p_cdd, q_cdd, r_cdd, s_cdd,
				parseDuration(getDuration().get(0), id2bounds), getScope().toString());
	}

	@Override
	public String toString() {
		String res = new String();

		res = "it is always the case that after \"" + getCdds().get(1) + "\" holds for \"" + getDuration().get(0)
				+ "\" time units, then \"" + getCdds().get(0) + "\" holds";

		return res;
	}

	@Override
	public PatternType rename(final String newName) {
		return new BndEntryConditionPattern(getScope(), newName, getCdds(), getDuration());
	}
}
