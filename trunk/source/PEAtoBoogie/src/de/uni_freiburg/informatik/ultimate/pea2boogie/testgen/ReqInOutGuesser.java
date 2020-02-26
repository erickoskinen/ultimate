package de.uni_freiburg.informatik.ultimate.pea2boogie.testgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.InitializationPattern;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.InitializationPattern.VariableCategory;
import de.uni_freiburg.informatik.ultimate.lib.srparse.pattern.PatternType;

public class ReqInOutGuesser {

	private final ILogger mLogger;
	private final Map<String, InitializationPattern> mVar2InitPattern;
	private final List<InitializationPattern> newInitPatterns;
	private final Map<String, Integer> mId2Bounds;

	public ReqInOutGuesser(final ILogger logger, final List<InitializationPattern> oldInitPatterns,
			final List<PatternType> reqPatterns) {
		mLogger = logger;
		mVar2InitPattern = new HashMap<>();
		mId2Bounds = new HashMap<>();
		final Set<InitializationPattern> constInitPatterns = new HashSet<>();
		for (final InitializationPattern p : oldInitPatterns) {
			if (p.getCategory() == VariableCategory.CONST) {
				// we do not care for the concrete value, but only for something we can build countertraces with
				mId2Bounds.put(p.getId(), 42);
				constInitPatterns.add(p);
			} else {
				mVar2InitPattern.put(p.getId(), p);
			}
		}
		if (isInputOnlyPattern(oldInitPatterns)) {
			mLogger.warn("No input/output assignment was chosen! We will make a very conservative guess.");
			newInitPatterns = generateNewInitializationPattern(mVar2InitPattern.values(), reqPatterns);
			newInitPatterns.addAll(constInitPatterns);
		} else {
			newInitPatterns = oldInitPatterns;
		}
	}

	private List<InitializationPattern> generateNewInitializationPattern(
			final Collection<InitializationPattern> oldInitPatterns, final List<PatternType> reqPatterns) {
		final Set<String> allVars = getAllVariables(oldInitPatterns);
		final Set<String> effectVars = getEffectVariables(reqPatterns);
		final Set<String> precondVars = getPreconditionVars(reqPatterns);
		// every variable, that is never influenced by a requirement has to be an input var
		final Set<String> inputVars = new HashSet<>(allVars);
		inputVars.removeAll(effectVars);
		mLogger.warn("Inputs: " + inputVars.toString());
		// output vars are all that are never used in a precondition
		final Set<String> outputVars = new HashSet<>(allVars);
		outputVars.removeAll(precondVars);
		mLogger.warn("Outputs: " + outputVars.toString());
		// calculate remaining vars for easy generation of init things
		final Set<String> remainingVars = new HashSet<>(allVars);
		remainingVars.removeAll(inputVars);
		remainingVars.removeAll(outputVars);
		// generate new pattern
		final List<InitializationPattern> newInitPattern = new ArrayList<>();
		for (final String var : inputVars) {
			newInitPattern
					.add(new InitializationPattern(var, mVar2InitPattern.get(var).getType(), VariableCategory.IN));
		}
		for (final String var : outputVars) {
			newInitPattern
					.add(new InitializationPattern(var, mVar2InitPattern.get(var).getType(), VariableCategory.OUT));
		}
		for (final String var : remainingVars) {
			newInitPattern
					.add(new InitializationPattern(var, mVar2InitPattern.get(var).getType(), VariableCategory.HIDDEN));
		}
		return newInitPattern;
	}

	private Set<String> getEffectVariables(final List<PatternType> oldPatterns) {
		final Set<String> effectVars = new HashSet<>();
		for (final PatternType pattern : oldPatterns) {
			effectVars.addAll(Req2CauseTrackingCDD.getEffectVariables(pattern, mId2Bounds));
		}
		return effectVars;
	}

	private Set<String> getPreconditionVars(final List<PatternType> oldPatterns) {
		final Set<String> precondVars = new HashSet<>();
		for (final PatternType pattern : oldPatterns) {
			final Set<String> vars = Req2CauseTrackingCDD.getAllVariables(pattern, mId2Bounds);
			vars.removeAll(Req2CauseTrackingCDD.getEffectVariables(pattern, mId2Bounds));
			precondVars.addAll(vars);
		}
		return precondVars;
	}

	private static Set<String> getAllVariables(final Collection<InitializationPattern> oldPatterns) {
		final Set<String> effectVars = new HashSet<>();
		for (final PatternType pattern : oldPatterns) {
			effectVars.add(((InitializationPattern) pattern).getId());
		}
		return effectVars;
	}

	private static boolean isInputOnlyPattern(final List<InitializationPattern> initPattern) {
		for (final InitializationPattern p : initPattern) {
			if (p.getCategory() != VariableCategory.IN && p.getCategory() != VariableCategory.CONST) {
				return false;
			}
		}
		return true;
	}

	public List<InitializationPattern> getInitializationPatterns() {
		return newInitPatterns;
	}

}