package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWord;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ProgramPoint;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SmtManager;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.TraceChecker.TraceCheckerBenchmarkGenerator;
import de.uni_freiburg.informatik.ultimate.util.RelationWithTreeSet;

/**
 * This class implements the possibility to partially annotate and assert the statements of a trace in order
 * to get better interpolants.
 * @author musab@informatik.uni-freiburg.de
 *
 */
public class AnnotateAndAsserterWithStmtOrderPrioritization extends AnnotateAndAsserter {
	/**
	 * 1. Heuristic:
	 */
	boolean m_SkipFirstLoopAndThenConventionallyContinue = !true;
	
	/**
	 * 2. Heuristic: 
	 */
	boolean m_IterativelySkipLoopsUntilUnsatReached = true;

	public AnnotateAndAsserterWithStmtOrderPrioritization(
			SmtManager smtManager, NestedFormulas<Term, Term> nestedSSA,
			AnnotateAndAssertCodeBlocks aaacb, 
			TraceCheckerBenchmarkGenerator tcbg) {
		super(smtManager, nestedSSA, aaacb, tcbg);
	}

	/**
	 *  Returns a set of integer where each integer <i>i</i> corresponds to a statement <i>i</i> of the given trace, such that:
	 *  <ul>
	 *  <li> every integer <i>i</i>: <i>i</i> < l_1 or <i>i</i> >= l_2 
	 *  <li> l_1: the lowest index of a program point p, such that p occurs at least twice, and l_1 >= lowerIndex
	 *  <li> l_2: the greatest index of the same program point p, such that l_2 <= upperIndex
	 *  </ul>
	**/
	private Set<Integer> getSubTrace(NestedWord<CodeBlock> trace, int lowerIndex, int upperIndex) {
		assert lowerIndex >= 0 : "Lower index is negative";
		assert upperIndex > lowerIndex : "Upper index is <= lower index";
		assert upperIndex <= trace.length() : "Upper index is out of range";
		List<ProgramPoint> pps = TraceCheckerUtils.getSequenceOfProgramPoints(trace);
		RelationWithTreeSet<ProgramPoint, Integer> rwt = new RelationWithTreeSet<ProgramPoint, Integer>();
		for (int i = lowerIndex; i <= upperIndex; i++) {
			rwt.addPair(pps.get(i), i);
		}
		
		int lowestIndexOfPPThatOccursTwice = 0;
		int greatestIndexOfPPThatOccursTwice = trace.length();
		boolean existsPPThatOccursMoreThanOnce = false;
		for (int i = lowerIndex; i <= upperIndex; i++) {
			if (rwt.getImage(pps.get(i)).size() > 1) {
				// Get the first occurrence in the trace of that program-point, that occurs more than one time
				lowestIndexOfPPThatOccursTwice = ((TreeSet<Integer>) rwt.getImage(pps.get(i))).first();
				// Get the last occurrence of that program-point
				greatestIndexOfPPThatOccursTwice = ((TreeSet<Integer>) rwt.getImage(pps.get(i))).last();
				existsPPThatOccursMoreThanOnce = true;
				break;
			}
		}
		
		if (!existsPPThatOccursMoreThanOnce) {
			// If every program point occurs only once (no loops in the trace),
			// then return the set of integers {i | 0 <= i < trace.length}
			Set<Integer> allStmts = new HashSet<Integer>();
			for (int i = 0; i <  trace.length(); i++) {
				allStmts.add(i);
			}
			return allStmts;
		} else {
			// If there is a program point with more than one occurrences, then
			// put every statement before the index of the first occurrence and every statement 
			// after the index of the last occurrence into the set to be returned
			Set<Integer> stmtsWithOutLoop = new HashSet<Integer>();
			for (int i = lowerIndex; i < upperIndex; i++) {
				// Since a program-point represents the source node of a stmt, we have to
				// take here also greatestIndexOfPPThatOccursTwice-th element into our set stmtsWithOutLoop
				// that's why we have (i >= greatestIndexOfPPThatOccursTwice)
				if (i < lowestIndexOfPPThatOccursTwice || i >= greatestIndexOfPPThatOccursTwice) {
					stmtsWithOutLoop.add(i);
				}
			}
			return stmtsWithOutLoop;
		}
	}

	
	private Map.Entry<Integer, Integer> getIndicesOfStmThatOccursTwice(NestedWord<CodeBlock> trace, int lowerIndex, int upperIndex) {
		assert lowerIndex >= 0 : "Lower index is negative";
		assert upperIndex <= trace.length() : "Upper index is out of range";
		
		if (upperIndex < lowerIndex) {
			return new AbstractMap.SimpleEntry<Integer, Integer>(upperIndex, upperIndex);
		}
		List<ProgramPoint> pps = TraceCheckerUtils.getSequenceOfProgramPoints(trace);
		RelationWithTreeSet<ProgramPoint, Integer> rwt = new RelationWithTreeSet<ProgramPoint, Integer>();
		for (int i = lowerIndex; i <= upperIndex; i++) {
			rwt.addPair(pps.get(i), i);
		}
		
		int lowestIndexOfPPThatOccursTwice = lowerIndex;
		int greatestIndexOfPPThatOccursTwice = upperIndex;
		for (int i = lowerIndex; i <= upperIndex; i++) {
			if (rwt.getImage(pps.get(i)).size() > 1) {
				// Get the first occurrence in the trace of that program-point, that occurs more than one time
				lowestIndexOfPPThatOccursTwice = ((TreeSet<Integer>) rwt.getImage(pps.get(i))).first();
				// Get the last occurrence of that program-point
				greatestIndexOfPPThatOccursTwice = ((TreeSet<Integer>) rwt.getImage(pps.get(i))).last();
				break;
			}
		}
		return new AbstractMap.SimpleEntry<Integer, Integer>(lowestIndexOfPPThatOccursTwice, greatestIndexOfPPThatOccursTwice);
	}
	
	private Set<Integer> getSetOfIntegerForGivenInterval(int lowerBound, int upperBound) {
		Set<Integer> result = new HashSet<Integer>();
		for (int i = lowerBound; i < upperBound; i++) {
			result.add(i);
		}
		return result;
	}
	
	
	private Set<Integer> integerSetDifference(Set<Integer> firstSet, Set<Integer> secondSet) {
		Set<Integer> result = new HashSet<Integer>();
		for (Integer i : firstSet) {
			if (!secondSet.contains(i)) {
				result.add(i);
			}
		}
		return result;
	}
	
	@Override
	public void buildAnnotatedSsaAndAssertTerms() {
//		Set<Integer> stmtsOutsideOfLoop = getSubTrace(m_Trace, 0, m_Trace.length());
//		Set<Integer> stmtsWithinLoop = new HashSet<Integer>();
//		// Slice the part of integers, that doesn't occur in the set stmtsWithoutLoop
//		if (stmtsOutsideOfLoop.size() != m_Trace.length()) {
//			for (int i = 0; i < m_Trace.length(); i++) {
//				if (!stmtsOutsideOfLoop.contains(i)) {
//					stmtsWithinLoop.add(i);
//				}
//			}
//		}
		
		Map.Entry<Integer, Integer> indicesOfStmtThatOccursTwice = getIndicesOfStmThatOccursTwice(m_Trace, 0, m_Trace.length());
		int lowerIndexOfStmtThatOccursTwice = indicesOfStmtThatOccursTwice.getKey();
		int upperIndexOfStmtThatOccursTwice =  indicesOfStmtThatOccursTwice.getValue();
		
		Set<Integer> stmtsWithinLoop = getSetOfIntegerForGivenInterval(lowerIndexOfStmtThatOccursTwice, upperIndexOfStmtThatOccursTwice);
		Set<Integer> integersFromTrace = getSetOfIntegerForGivenInterval(0, m_Trace.length());
		Set<Integer> stmtsOutsideOfLoop = integerSetDifference(integersFromTrace, stmtsWithinLoop);
		
		m_AnnotSSA = new ModifiableNestedFormulas<Term, Term>(m_Trace, new TreeMap<Integer, Term>());
		
		m_AnnotSSA.setPrecondition(m_AnnotateAndAssertCodeBlocks.annotateAndAssertPrecondition());
		m_AnnotSSA.setPostcondition(m_AnnotateAndAssertCodeBlocks.annotateAndAssertPostcondition());
		Collection<Integer> callPositions = new ArrayList<Integer>();
		Collection<Integer> pendingReturnPositions = new ArrayList<Integer>();
		// Apply 1. heuristic
		if (m_SkipFirstLoopAndThenConventionallyContinue) {
			// First, annotate and assert the statements, which doesn't occur within a loop
			buildAnnotatedSsaAndAssertTermsWithPriorizedOrder(m_Trace, callPositions, pendingReturnPositions, stmtsOutsideOfLoop);


			m_Satisfiable = m_SmtManager.getScript().checkSat();
			// If the statements outside of a loop are not unsatisfiable, then annotate and assert also
			// the rest of the statements
			if (m_Satisfiable != LBool.UNSAT && stmtsOutsideOfLoop.size() != m_Trace.length()) {
				buildAnnotatedSsaAndAssertTermsWithPriorizedOrder(m_Trace, callPositions, pendingReturnPositions, stmtsWithinLoop);
				assert callPositions.containsAll(m_Trace.getCallPositions());
				assert m_Trace.getCallPositions().containsAll(callPositions);
				m_Satisfiable = m_SmtManager.getScript().checkSat();
			}
		} 
		// Apply 2. heuristic
		else if (m_IterativelySkipLoopsUntilUnsatReached) {
			m_Satisfiable = LBool.UNKNOWN;
			Set<Integer> stmtsAlreadyAsserted = stmtsOutsideOfLoop;
			m_Tcbg.reportnewCodeBlocks(m_Trace.length());
			while (m_Satisfiable != LBool.UNSAT) {
				// First, annotate and assert the statements, which doesn't occur within a loop
				buildAnnotatedSsaAndAssertTermsWithPriorizedOrder(m_Trace, callPositions, pendingReturnPositions, stmtsOutsideOfLoop);
				m_Satisfiable = m_SmtManager.getScript().checkSat();
				m_Tcbg.reportnewCheckSat();
				m_Tcbg.reportnewAssertedCodeBlocks(stmtsOutsideOfLoop.size());
				
				if (stmtsAlreadyAsserted.size() == m_Trace.length()) break;
				if (m_Satisfiable != LBool.UNSAT) {
					// TODO: if lowerIndexOfStat.. + 1 > upperIndex - 1 --> then break, and add all the remaining statements
					indicesOfStmtThatOccursTwice = getIndicesOfStmThatOccursTwice(m_Trace, lowerIndexOfStmtThatOccursTwice + 1, 
							upperIndexOfStmtThatOccursTwice-1);
					lowerIndexOfStmtThatOccursTwice = indicesOfStmtThatOccursTwice.getKey();
					upperIndexOfStmtThatOccursTwice =  indicesOfStmtThatOccursTwice.getValue();
					// update the set of statements which are already asserted
					Set<Integer> stmtsWithinLoopOld = stmtsWithinLoop; 
					stmtsWithinLoop = getSetOfIntegerForGivenInterval(lowerIndexOfStmtThatOccursTwice, upperIndexOfStmtThatOccursTwice);
					stmtsOutsideOfLoop = integerSetDifference(stmtsWithinLoopOld, stmtsWithinLoop);
					stmtsAlreadyAsserted.addAll(stmtsOutsideOfLoop);
				}
			}
		}
		s_Logger.info("Conjunction of SSA is " + m_Satisfiable);
	}
	
	/**
	 * Annotate and assert every statement <i>i</i> from the given trace, such that
	 * <i>i</i> is an element of the given integer set stmtsToAssert.
	 */
	private void buildAnnotatedSsaAndAssertTermsWithPriorizedOrder(NestedWord<CodeBlock> trace, 
			Collection<Integer> callPositions,
			Collection<Integer> pendingReturnPositions,
			Set<Integer> stmtsToAssert) {
		for (Integer i : stmtsToAssert) {
			if (trace.isCallPosition(i)) {
				callPositions.add(i);
				m_AnnotSSA.setGlobalVarAssignmentAtPos(i, m_AnnotateAndAssertCodeBlocks.annotateAndAssertGlobalVarAssignemntCall(i));
				m_AnnotSSA.setLocalVarAssignmentAtPos(i, m_AnnotateAndAssertCodeBlocks.annotateAndAssertLocalVarAssignemntCall(i));
				m_AnnotSSA.setOldVarAssignmentAtPos(i, m_AnnotateAndAssertCodeBlocks.annotateAndAssertOldVarAssignemntCall(i));
			} else {
				if (trace.isReturnPosition(i) && trace.isPendingReturn(i)) {
					pendingReturnPositions.add(i);
				}
				m_AnnotSSA.setFormulaAtNonCallPos(i, m_AnnotateAndAssertCodeBlocks.annotateAndAssertNonCall(i));
			}
		}
		

		// number that the pending context. The first pending context has
		// number -1, the second -2, ...
		int pendingContextCode = -1 - m_SSA.getTrace().getPendingReturns().size();
		for (Integer positionOfPendingReturn : m_SSA.getTrace().getPendingReturns().keySet()) {
			assert trace.isPendingReturn(positionOfPendingReturn);
			{
				Term annotated = m_AnnotateAndAssertCodeBlocks.annotateAndAssertPendingContext(
						positionOfPendingReturn, pendingContextCode);
				m_AnnotSSA.setPendingContext(positionOfPendingReturn, annotated);
			}
			{
				Term annotated = m_AnnotateAndAssertCodeBlocks.annotateAndAssertLocalVarAssignemntPendingContext(
						positionOfPendingReturn, pendingContextCode);
				m_AnnotSSA.setLocalVarAssignmentAtPos(positionOfPendingReturn, annotated);
			}
			{
				Term annotated = m_AnnotateAndAssertCodeBlocks.annotateAndAssertOldVarAssignemntPendingContext(
						positionOfPendingReturn, pendingContextCode);
				m_AnnotSSA.setOldVarAssignmentAtPos(positionOfPendingReturn, annotated);
			}
			pendingContextCode++;
		}
	}
	
}
