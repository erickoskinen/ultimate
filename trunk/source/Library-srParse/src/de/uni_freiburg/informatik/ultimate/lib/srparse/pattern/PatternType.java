package de.uni_freiburg.informatik.ultimate.lib.srparse.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.lib.pea.BooleanDecision;
import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.PhaseEventAutomata;
import de.uni_freiburg.informatik.ultimate.lib.pea.reqcheck.PatternToPEA;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;

public class PatternType {

	// contains all CDDs from the pattern in reverse order
	// TODO: DD: Why in reverse order? Why a list?
	protected List<CDD> mCdds;

	protected static final CDD DEFAULT_Q = BooleanDecision.create("Q");
	protected static final CDD DEFAULT_R = BooleanDecision.create("R");
	protected String mDuration;
	protected PhaseEventAutomata mPea;
	protected int mEffectOffset;

	protected SrParseScope mScope;
	protected PatternToPEA mPeaTransformator;
	protected CDD mEffect;

	protected String mId;

	public PatternType() {
		this(null);
	}

	public PatternType(final SrParseScope scope) {
		mScope = scope;
	}

	public int getEffectOffset() {
		return mEffectOffset;
	}

	/***
	 * Determine if a variable name is in the set of variables that are affected by the requirement.
	 *
	 * @param ident
	 *            identifier of variable
	 * @return true if the Variable's value is determined by this requirements effect.
	 */
	public boolean isEffect(final String ident) {
		return mEffect.getIdents().contains(ident);
	}

	public Set<String> getEffectVariabels() {
		return mEffect.getIdents();
	}

	public CDD getEffect() {
		return mEffect;
	}

	public String getDuration() {
		return mDuration;
	}

	public int parseDuration(final String duration, final Map<String, Integer> id2bounds) {
		if (duration == null) {
			throw new IllegalArgumentException("Duration cannot be null");
		}
		try {
			return Integer.parseInt(duration);
		} catch (final NumberFormatException nfe) {
			if (id2bounds == null) {
				throw new IllegalArgumentException("Cannot parse duration and no alternative bounds are given");
			}
			final Integer actualDuration = id2bounds.get(duration);
			if (actualDuration == null) {
				throw new IllegalArgumentException(
						"Cannot parse duration and alternative bounds do not contain " + duration);
			}
			return actualDuration;
		}
	}

	public List<CDD> getCdds() {
		return mCdds;
	}

	public void setDuration(final String duration) {
		mDuration = duration;
	}

	public void transform(final Map<String, Integer> id2bounds) {
		throw new UnsupportedOperationException();
	}

	public void mergeCDDs(final List<CDD> cdds) {
		if (cdds == null) {
			return;
		}
		if (mCdds == null) {
			mCdds = new ArrayList<>();
		}
		mCdds.addAll(cdds);
	}

	public PhaseEventAutomata transformToPea(final Map<String, Integer> id2bounds) {
		transform(id2bounds);
		return mPea;
	}

	public void setPeaTransformator(final PatternToPEA peaTransformator) {
		mPeaTransformator = peaTransformator;
	}

	public SrParseScope getScope() {
		return mScope;
	}

	public void setScope(final SrParseScope scope) {
		mScope = scope;
	}

	@Override
	public String toString() {
		assert mScope != null || this instanceof InitializationPattern;
		if (mScope == null) {
			return getClass().toString();
		}
		return mScope.toString() + this.getClass().toString();
	}

	public void setId(final String id) {
		mId = id;
	}

	public String getId() {
		return mId;
	}
}
