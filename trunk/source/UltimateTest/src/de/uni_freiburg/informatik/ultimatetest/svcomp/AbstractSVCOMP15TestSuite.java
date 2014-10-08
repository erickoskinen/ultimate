package de.uni_freiburg.informatik.ultimatetest.svcomp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.TraceAbstractionBenchmarks;
import de.uni_freiburg.informatik.ultimatetest.TraceAbstractionTestSummary;
import de.uni_freiburg.informatik.ultimatetest.UltimateRunDefinition;
import de.uni_freiburg.informatik.ultimatetest.UltimateStarter;
import de.uni_freiburg.informatik.ultimatetest.UltimateTestCase;
import de.uni_freiburg.informatik.ultimatetest.UltimateTestSuite;
import de.uni_freiburg.informatik.ultimatetest.decider.SafetyCheckTestResultDecider;
import de.uni_freiburg.informatik.ultimatetest.summary.CsvConcatenator;
import de.uni_freiburg.informatik.ultimatetest.summary.IIncrementalLog;
import de.uni_freiburg.informatik.ultimatetest.summary.ITestSummary;
import de.uni_freiburg.informatik.ultimatetest.traceabstraction.TestSummaryWithBenchmarkResults;
import de.uni_freiburg.informatik.ultimatetest.util.Util;

/**
 * Test suite for SVCOMP15.
 * 
 * @author dietsch@informatik.uni-freiburg.de
 * 
 */
public abstract class AbstractSVCOMP15TestSuite extends UltimateTestSuite {

	private ArrayList<UltimateTestCase> mTestCases;

	@Override
	public Collection<UltimateTestCase> createTestCases() {
		if (mTestCases == null) {
			List<TestDefinition> testDefs = getTestDefinitions();
			File svcompRootDir = getSVCOMP15RootDirectory();

			Collection<File> setFiles = getAllSetFiles(svcompRootDir);
			Collection<File> allInputFiles = getAllPotentialInputFiles(svcompRootDir);

			if (testDefs == null || testDefs.isEmpty()) {
				System.err.println("No test definitions given. Did you implement getTestDefinitions correctly?");
				return new ArrayList<>();
			}

			if (allInputFiles == null || allInputFiles.isEmpty() || setFiles == null || setFiles.isEmpty()) {
				System.err
						.println("inputFiles or setFiles are null: did you specify the svcomp root directory correctly? Currently it is: "
								+ svcompRootDir);
				return new ArrayList<>();
			}

			mTestCases = new ArrayList<>();

			for (TestDefinition def : testDefs) {
				String setFilename = def.getSetName() + ".set";
				for (File set : setFiles) {
					if (setFilename.equals(set.getName())) {
						addTestCases(def, set, allInputFiles, mTestCases, svcompRootDir);
					}
				}
			}
		}
		return mTestCases;
	}

	private void addTestCases(TestDefinition def, File setFile, Collection<File> possibleInputFiles,
			List<UltimateTestCase> testcases, File svcompRootDir) {

		Collection<File> inputFiles = getFilesForSetFile(possibleInputFiles, setFile);
		// use this for testing
		// inputFiles = Util.firstN(inputFiles, 3);
		for (File input : inputFiles) {

			try {
				String name = createTestCaseName(svcompRootDir, input, def);
				UltimateRunDefinition urd = new UltimateRunDefinition(input, def.getSettings(), def.getToolchain());
				UltimateStarter starter = new UltimateStarter(urd, def.getTimeout());

				UltimateTestCase testCase = new UltimateTestCase(name, new SafetyCheckTestResultDecider(urd, true),
						starter, urd, super.getSummaries(), super.getIncrementalLogs());

				testcases.add(testCase);
			} catch (Exception ex) {
				System.err.println("Exception while creating test case, skipping this one: " + input.getAbsolutePath());
				ex.printStackTrace();
			}
		}

	}

	private String createTestCaseName(File svcompRootDir, File input, TestDefinition def) {
		StringBuilder sb = new StringBuilder();
		sb.append(def.getSetName());
		sb.append(" ");
		sb.append(def.getToolchain().getName());
		sb.append(" ");
		sb.append(def.getSettings().getName());
		sb.append(": ");
		sb.append(input.getAbsolutePath().substring(svcompRootDir.getAbsolutePath().length(),
				input.getAbsolutePath().length()));
		return sb.toString();
	}

	protected IIncrementalLog[] constructIncrementalLog() {
		return new IIncrementalLog[] { new TestSummaryWithBenchmarkResults(this.getClass()) };
	}

	@Override
	protected ITestSummary[] constructTestSummaries() {
		return new ITestSummary[] {

		new SVCOMP15TestSummary(this.getClass()), new TraceAbstractionTestSummary(this.getClass()),
				new CsvConcatenator(this.getClass(), TraceAbstractionBenchmarks.class) };
	}

	/**
	 * Override this if you want to use some special place for your SVCOMP15
	 * repository. We default to trunk/examples/svcomp .
	 */
	protected File getSVCOMP15RootDirectory() {
		String svcompRootDir = Util.getFromMavenVariableSVCOMPRoot(Util.getPathFromTrunk("examples/svcomp"));
		return new File(svcompRootDir);
	}

	/**
	 * Supply your test definitions here
	 * 
	 * @return
	 */
	protected abstract List<TestDefinition> getTestDefinitions();

	private Collection<File> getFilesForSetFile(Collection<File> allFiles, File setFile) {
		ArrayList<File> currentFiles = new ArrayList<File>();

		try {
			DataInputStream in = new DataInputStream(new FileInputStream(setFile));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				String regex = ".*" + line.replace(".", "\\.").replace("*", ".*");
				currentFiles.addAll(Util.filter(allFiles, regex));
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return currentFiles;
	}

	private Collection<File> getAllSetFiles(File rootdir) {
		return getFilesRecursively(rootdir, new String[] { ".*\\.set" });
	}

	private Collection<File> getAllPotentialInputFiles(File rootdir) {
		return getFilesRecursively(rootdir, new String[] { ".*\\.c", ".*\\.i" });
	}

	private Collection<File> getFilesRecursively(File rootdir, String[] regex) {
		ArrayList<File> singleFiles = new ArrayList<File>();
		singleFiles.addAll(Util.getFilesRegex(rootdir, regex));
		return singleFiles;
	}

	/**
	 * @param setname
	 *            Case-sensitive name of the .set file without the suffix and
	 *            without the path, e.g. ControlFlowInteger.false-unreach-label
	 *            or Simple
	 * @param toolchain
	 *            Path to .xml file describing the toolchain relative to
	 *            trunk/examples/toolchains, e.g. "AutomizerBpl.xml"
	 * @param settings
	 *            Path to .xml file describing the toolchain relative to
	 *            trunk/examples/settings, e.g.
	 *            "automizer/BackwardPredicates.epf"
	 * @param timeout
	 *            Timeout in ms after which Ultimate should timeout. Overrides
	 *            timeout in settings. Values <= 0 disable the timeout (Timeout
	 *            in settings still applies).
	 */
	protected TestDefinition getTestDefinitionFromExamples(String setname, String toolchain, String settings,
			long timeout) {
		return new TestDefinition(setname, new File(Util.getPathFromTrunk("examples/toolchains/" + toolchain)),
				new File(Util.getPathFromTrunk("examples/settings/" + settings)), timeout);
	}

	public class TestDefinition {
		private final String mSetname;
		private final File mToolchain;
		private final File mSettings;
		private final long mTimeout;

		/**
		 * 
		 * @param setname
		 *            Case-sensitive name of the .set file without the suffix
		 *            and without the path, e.g.
		 *            ControlFlowInteger.false-unreach-label or Simple
		 * @param toolchain
		 *            Path to .xml file describing the toolchain.
		 * @param settings
		 *            Path to .epf file describing the settings.
		 * @param timeout
		 *            Timeout in ms after which Ultimate should timeout.
		 *            Overrides timeout in settings. Values <= 0 disable the
		 *            timeout (Timeout in settings still applies).
		 * 
		 * @author dietsch@informatik.uni-freiburg.de
		 */
		public TestDefinition(String setname, File toolchain, File settings, long timeout) {
			mSetname = setname;
			mToolchain = toolchain;
			mSettings = settings;
			mTimeout = timeout;
		}

		public String getSetName() {
			return mSetname;
		}

		public File getToolchain() {
			return mToolchain;
		}

		public File getSettings() {
			return mSettings;
		}

		public long getTimeout() {
			return mTimeout;
		}
	}
}
