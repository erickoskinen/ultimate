package de.uni_freiburg.informatik.ultimate.ultimatetest.suites.traceabstraction;

import java.util.Collection;

import de.uni_freiburg.informatik.ultimate.test.UltimateRunDefinition;
import de.uni_freiburg.informatik.ultimate.test.UltimateTestCase;
import de.uni_freiburg.informatik.ultimate.test.decider.ITestResultDecider;
import de.uni_freiburg.informatik.ultimate.test.decider.SafetyCheckTestResultDecider;

public class VMCAI2021ExampleSuite extends AbstractTraceAbstractionTestSuite {

	private static final String[] SETTINGS = {
			"automizer/concurrent/svcomp-Reach-32bit-Automizer_Default-noMmResRef-FA-NoLbe.epf",
			"automizer/concurrent/svcomp-Reach-32bit-Automizer_Default-noMmResRef-PN-NoLbe.epf" };

	private static final String TOOLCHAIN = "AutomizerBplInline.xml";

	private static final String BENCHMARK_FOLDER = "examples/concurrent/bpl/VMCAI2021";

	@Override
	protected ITestResultDecider constructITestResultDecider(UltimateRunDefinition ultimateRunDefinition) {
		return new SafetyCheckTestResultDecider(ultimateRunDefinition, false);
	}

	@Override
	protected long getTimeout() {
		return 120 * 1000;
	}

	@Override
	public Collection<UltimateTestCase> createTestCases() {
		for (final String setting : SETTINGS) {
			addTestCase(TOOLCHAIN, setting, new String[] { BENCHMARK_FOLDER }, new String[] { ".bpl" });
		}
		return super.createTestCases();
	}
}
