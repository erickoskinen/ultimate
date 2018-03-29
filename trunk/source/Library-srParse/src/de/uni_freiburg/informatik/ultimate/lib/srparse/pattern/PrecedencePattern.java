package de.uni_freiburg.informatik.ultimate.lib.srparse.pattern;

import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.PhaseEventAutomata;
import de.uni_freiburg.informatik.ultimate.lib.pea.reqcheck.PatternToPEA;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;

public class PrecedencePattern extends PatternType {

	public PrecedencePattern(final SrParseScope scope, final String id, final List<CDD> cdds,
			final List<String> durations) {
		super(scope, id, cdds, durations);
	}

	@Override
	public PhaseEventAutomata transform(final PatternToPEA peaTrans, final Map<String, Integer> id2bounds) {
		final CDD p_cdd = getCdds().get(1);
		final CDD q_cdd = getScope().getCdd1();
		final CDD r_cdd = getScope().getCdd2();
		final CDD s_cdd = getCdds().get(0);

		return peaTrans.precedencePattern(getId(), p_cdd, q_cdd, r_cdd, s_cdd, getScope().toString());
	}

	@Override
	public String toString() {
		return "it is always the case that if \"" + getCdds().get(1) + "\" holds, then \"" + getCdds().get(0)
				+ "\" previously held";
	}

	@Override
	public PatternType rename(final String newName) {
		return new PrecedencePattern(getScope(), newName, getCdds(), getDuration());
	}
}
