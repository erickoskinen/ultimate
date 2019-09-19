package de.uni_freiburg.informatik.ultimate.lib.srparse.pattern;

import java.util.List;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.CounterTrace;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScopeBefore;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScopeBetween;

/**
 * TODO: fix description
 *
 * {scope}, it is always the case that if "" holds and is succeeded by "P", then
 * "S" eventually holds after "T"
 *
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public class ResponseChain21Pattern extends PatternType {

	public ResponseChain21Pattern(final SrParseScope scope, final String id, final List<CDD> cdds,
			final List<String> durations) {
		super(scope, id, cdds, durations);
	}

	@Override
	public CounterTrace transform(final CDD[] cdds, final int[] durations) {
		final SrParseScope scope = getScope();
		// note: P and Q are reserved for scope, cdds are parsed in reverse order
		final CDD R = getCdds().get(2);
		final CDD S = getCdds().get(1);
		final CDD T = getCdds().get(0);

		if (scope instanceof SrParseScopeBefore) {
			final CDD P = getScope().getCdd1();
			final CounterTrace ct = counterTrace(phase(P.negate()), phase(S.and(P.negate()).and(T.negate())),
					phase(P.negate()), phase(T.and(P.negate())), phase(R.negate().and(P.negate())), phase(P), phaseT());

			return ct;
		} else if (scope instanceof SrParseScopeBetween) {
			final CDD P = getScope().getCdd1();
			final CDD Q = getScope().getCdd2();
			final CounterTrace ct = counterTrace(phaseT(), phase(P.and(Q.negate())), phase(Q.negate()),
					phase(S.and(Q.negate()).and(T.negate())), phase(Q.negate()), phase(T.and(Q.negate())),
					phase(R.negate().and(Q.negate())), phase(Q), phaseT());

			return ct;
		}
		throw new PatternScopeNotImplemented(scope.getClass(), getClass());
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (getId() != null) {
			sb.append(getId());
			sb.append(": ");
		}
		if (getScope() != null) {
			sb.append(getScope());
		}
		sb.append("it is always the case that if \"");
		sb.append(getCdds().get(3).toBoogieString());
		sb.append("\" holds and is succeeded by \"");
		sb.append(getCdds().get(2).toBoogieString());
		sb.append("\", then \"");
		sb.append(getCdds().get(1).toBoogieString());
		sb.append("\" eventually holds after \"");
		sb.append(getCdds().get(0).toBoogieString());
		sb.append("\"");
		return sb.toString();
	}

	@Override
	public PatternType rename(final String newName) {
		return new ResponseChain21Pattern(getScope(), newName, getCdds(), getDuration());
	}

	@Override
	public int getExpectedCddSize() {
		return 4;
	}

	@Override
	public int getExpectedDurationSize() {
		return 0;
	}
}
