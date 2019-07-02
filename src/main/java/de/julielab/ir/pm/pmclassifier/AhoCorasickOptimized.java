package de.julielab.ir.pm.pmclassifier;

// Java Collections are used only during the building of the automaton.
// The automaton itself uses only the primitive data types 
// and does not produce garbage during the matching.

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * TAKEN FROM:
 * https://github.com/lagodiuk/aho-corasick-optimized/blob/master/src/main/java/com/lahodiuk/ahocorasick/AhoCorasickOptimized.java
 * 
 * Implementation of the Aho-Corasick string matching algorithm, described in
 * the paper "Efficient String Matching: An Aid to Bibliographic Search",
 * written by Alfred V. Aho and Margaret J. Corasick, Bell Laboratories, 1975
 *
 * This implementation takes into account the specificities of the HotSpot JVM,
 * and supposed to be the Garbage Collector friendly. The automaton is based
 * only on the primitive data types in order to avoid Autoboxing and Unboxing
 * conversions.
 *
 * @author of the implementation is Yurii Lahodiuk (yura.lagodiuk@gmail.com)
 * @author 2017-11-11: Erik Faessler from the JULIE Lab added the method
 *         {@link #isEntryPrefix(String)}.
 */
public class AhoCorasickOptimized implements Serializable {

	private static final int INITIAL_STATE = 0;
	private static final int FAIL = -1;

	// the sorted array of the unique characters (alphabet)
	// every character from the alphabet is "mapped" to it's own index inside
	// this array
	// mapping: "character" -> "character index"
	private char[] charToIntMapping;
	// every character, which is not inside the alphabet is mapped to this
	// special index
	private int absentCharInt;

	// the automaton transitions table
	// mapping: "current state AND input character index" -> "new state"
	private int[][] goTo;
	// table of the outputs of every state
	// mapping: "state" -> "matched patterns"
	private List<String>[] output;
	// table of the fail transitions of the automaton
	// mapping: "state" -> "new state"
	private int[] fail;

	public AhoCorasickOptimized(String... patterns) {
		this(Arrays.asList(patterns));
	}
	public AhoCorasickOptimized(List<String> patterns) {

		this.initializeCharToIntMapping(patterns.stream());
		this.absentCharInt = this.charToIntMapping.length;

		int maxAmountOfStates = this.getMaxPossibleAmountOfStates(patterns.stream());

		this.initializeTransitionsTable(maxAmountOfStates);
		this.initializeOutputTable(maxAmountOfStates);
		this.initializeFailureTransitions(maxAmountOfStates);

		int actualStatesCount = this.calculateTransitionsTable(patterns.stream());

		this.adjustTransitionsTableSize(actualStatesCount);
		this.adjustOutputTableSize(actualStatesCount);
		this.adjustFailureTransitionsSize(actualStatesCount);

		this.makeInitialStateNeverFail();
		this.calculateFailureTransitions();
	}

	public void adjustFailureTransitionsSize(int actualStatesCount) {
		if (actualStatesCount == this.fail.length) {
			return;
		}
		int[] adjustedFail = new int[actualStatesCount];
		System.arraycopy(this.fail, 0, adjustedFail, 0, actualStatesCount);
		this.fail = adjustedFail;
	}

	public void adjustOutputTableSize(int actualStatesCount) {
		if (actualStatesCount == this.output.length) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<String>[] adjustedOutput = new List[actualStatesCount];
		System.arraycopy(this.output, 0, adjustedOutput, 0, actualStatesCount);
		this.output = adjustedOutput;
	}

	public void adjustTransitionsTableSize(int actualStatesCount) {
		if (actualStatesCount == this.goTo.length) {
			return;
		}
		int[][] adjustedGoTo = new int[actualStatesCount][this.charToIntMapping.length + 1];
		for (int i = 0; i < actualStatesCount; i++) {
			adjustedGoTo[i] = this.goTo[i];
		}
		this.goTo = adjustedGoTo;
	}

	public final void match(final String text, MatchCallback callback) {

		int state = INITIAL_STATE;

		for (int ci = 0; ci < text.length(); ci++) {

			char chr = text.charAt(ci);
			int char2IntMappingIndex = Arrays.binarySearch(this.charToIntMapping, chr);
			int chrInt = char2IntMappingIndex < 0 ? this.absentCharInt : char2IntMappingIndex;

			while (this.goTo[state][chrInt] == FAIL) {
				state = this.fail[state];
			}

			state = this.goTo[state][chrInt];

			List<String> matched = this.output[state];
			for (int j = 0; j < matched.size(); j++) {
				String found = matched.get(j);
				callback.onMatch((ci - found.length()) + 1, ci, found);
			}
		}
	}

	/**
	 * Added by Erik Faessler, 2017-11-11: This method does not find dictionary
	 * entries in the given string but just checks if the given string is a
	 * strict prefix of at least one dictionary entry. That is, the method
	 * returns false for strings that are completely contained in the dictionary
	 * and not a prefix of another entry.
	 * 
	 * @param text
	 * @return
	 */
	public final boolean isEntryPrefix(final String text) {

		int state = INITIAL_STATE;

		for (int ci = 0; ci < text.length(); ci++) {

			char chr = text.charAt(ci);
			int char2IntMappingIndex = Arrays.binarySearch(this.charToIntMapping, chr);
			int chrInt = char2IntMappingIndex < 0 ? this.absentCharInt : char2IntMappingIndex;

			if (this.goTo[state][chrInt] == FAIL) {
				state = FAIL;
				break;
			}

			state = this.goTo[state][chrInt];
		}
		if (state >= 0 && !this.output[state].isEmpty()) {
			// The input string is contained in the dictionary. Now we must
			// check if there are further transitions for longer entries
			for (int i = 0; i < this.goTo[state].length; ++i) {
				if (this.goTo[state][i] != FAIL)
					return true;
			}
		}
		// The string is not contained in the dictionary. We must check if it
		// was completely consumed without failing. A fail would mean that the
		// input string was not a prefix but couldn't be matched at some point
		// to a dictionary entry.
		return state != INITIAL_STATE && state != FAIL && this.output[state].isEmpty();
	}

	@SuppressWarnings("unchecked")
	private void initializeOutputTable(int maxAmountOfStates) {
		this.output = new List[maxAmountOfStates];
		for (int i = 0; i < this.output.length; i++) {
			this.output[i] = new ArrayList<>();
		}
	}

	private void initializeFailureTransitions(int maxAmountOfStates) {
		this.fail = new int[maxAmountOfStates];
		Arrays.fill(this.fail, FAIL);
		this.fail[INITIAL_STATE] = INITIAL_STATE;
	}

	private void initializeTransitionsTable(int maxAmountOfStates) {
		this.goTo = new int[maxAmountOfStates][this.charToIntMapping.length + 1];
		for (int[] row : this.goTo) {
			Arrays.fill(row, FAIL);
		}
	}

	private void makeInitialStateNeverFail() {
		for (int i = 0; i < this.goTo[INITIAL_STATE].length; i++) {
			if (this.goTo[INITIAL_STATE][i] == FAIL) {
				this.goTo[INITIAL_STATE][i] = INITIAL_STATE;
			}
		}
	}

	private int getMaxPossibleAmountOfStates(Stream<String> patterns) {
		return 1 + patterns.mapToInt(String::length).sum();
	}

	private void initializeCharToIntMapping(Stream<String> patterns) {
		Set<Character> uniqueChars = new HashSet<>();
		patterns.forEach(s -> {
			for (char c : s.toCharArray()) {
				uniqueChars.add(c);
			}
		});
		this.charToIntMapping = new char[uniqueChars.size()];
		int charToIntMappingIdx = 0;
		for (char c : uniqueChars) {
			this.charToIntMapping[charToIntMappingIdx] = c;
			charToIntMappingIdx++;
		}
		Arrays.sort(this.charToIntMapping);
	}

	// Calculation of the failure transitions using BFS
	private void calculateFailureTransitions() {

		Queue<Integer> queue = new LinkedList<>();

		// all states of depth 1 (counting from the initial state)
		// have failure transition to the initial state
		for (int stateReachableFromInitial : this.goTo[INITIAL_STATE]) {
			if (stateReachableFromInitial != INITIAL_STATE) {
				queue.add(stateReachableFromInitial);
				this.fail[stateReachableFromInitial] = INITIAL_STATE;
			}
		}

		while (!queue.isEmpty()) {
			int curr = queue.remove();

			for (int chrInt = 0; chrInt < this.goTo[curr].length; chrInt++) {

				int stateReachableFromCurr = this.goTo[curr][chrInt];

				if (stateReachableFromCurr != FAIL) {
					queue.add(stateReachableFromCurr);

					int state = this.fail[curr];
					while (this.goTo[state][chrInt] == FAIL) {
						state = this.fail[state];
					}

					this.fail[stateReachableFromCurr] = this.goTo[state][chrInt];
					this.output[stateReachableFromCurr].addAll(this.output[this.fail[stateReachableFromCurr]]);
				}
			}
		}
	}

	private int calculateTransitionsTable(Stream<String> patterns) {

		int newState = 0;
		for (String s : (Iterable<String>) () -> patterns.iterator()) {

			int state = INITIAL_STATE;

			// index of the current character
			int ci = 0;

			// traversal through the states, which are already created
			while (ci < s.length()) {
				char chr = s.charAt(ci);
				int chrInt = Arrays.binarySearch(this.charToIntMapping, chr);

				if (this.goTo[state][chrInt] != FAIL) {
					state = this.goTo[state][chrInt];
					ci++;
				} else {
					break;
				}
			}

			// creation of the new states
			while (ci < s.length()) {
				char chr = s.charAt(ci);
				int chrInt = Arrays.binarySearch(this.charToIntMapping, chr);

				newState = newState + 1;
				this.goTo[state][chrInt] = newState;
				state = newState;

				ci++;
			}

			// remember current pattern as the output for the last processed
			// state
			this.output[state].add(s);
		}

		return newState + 1;
	}

	public String generateGraphvizAutomatonRepresentation(boolean displayEdgesToInitialState) {
		return Util.generateGraphvizAutomatonRepresentation(this, displayEdgesToInitialState);
	}

	public static interface MatchCallback {

		void onMatch(int startPosition, int endPosition, String matched);
	}

	public static class Util {

		private static final String STYLE_FAILURE_TRANSITION = " [style=dashed, color=gray, constraint=false];";
		private static final String STYLE_STATE_WITHOUT_OUTPUT = " [shape=circle];";
		private static final String STYLE_STATE_WITH_OUTPUT = " [shape=doublecircle];";
		private static final char TAB = '\t';
		private static final char NEW_LINE = '\n';

		public static String generateGraphvizAutomatonRepresentation(AhoCorasickOptimized automaton,
				boolean displayEdgesToInitialState) {

			StringBuilder sb = new StringBuilder();
			sb.append("digraph automaton {").append(NEW_LINE);

			sb.append(TAB).append("graph [rankdir=LR];").append(NEW_LINE);
			Queue<Integer> queue = new LinkedList<>();
			queue.add(INITIAL_STATE);

			List<Integer> visitedStates = new ArrayList<>();

			// BFS traversal of the automaton
			while (!queue.isEmpty()) {
				int state = queue.remove();
				visitedStates.add(state);

				for (int charInt = 0; charInt < automaton.charToIntMapping.length; charInt++) {

					if ((automaton.goTo[state][charInt] != FAIL) && (automaton.goTo[state][charInt] != INITIAL_STATE)) {

						queue.add(automaton.goTo[state][charInt]);

						appendAutomatonTransitionGraphviz(automaton, sb, state, charInt);
					}
				}
			}

			appendFailureTransitionsToGraphviz(automaton, displayEdgesToInitialState, sb, visitedStates);

			displayStatesInGraphviz(automaton, sb, visitedStates);

			sb.append("}");
			return sb.toString();
		}

		public static void appendAutomatonTransitionGraphviz(AhoCorasickOptimized automaton, StringBuilder sb,
				int state, int charInt) {

			sb.append(TAB).append(state).append(" -> ").append(automaton.goTo[state][charInt]).append(" [label=")
					.append(automaton.charToIntMapping[charInt]).append(", weight=100, style=bold];").append(NEW_LINE);
		}

		private static void displayStatesInGraphviz(AhoCorasickOptimized automaton, StringBuilder sb,
				List<Integer> visitedStates) {

			for (int state : visitedStates) {
				if (!automaton.output[state].isEmpty()) {
					sb.append(TAB).append(state).append(STYLE_STATE_WITH_OUTPUT).append(NEW_LINE);
				} else {
					sb.append(TAB).append(state).append(STYLE_STATE_WITHOUT_OUTPUT).append(NEW_LINE);
				}
			}
		}

		private static void appendFailureTransitionsToGraphviz(AhoCorasickOptimized automaton,
				boolean displayEdgesToInitialState, StringBuilder sb, List<Integer> states) {

			for (int state : states) {
				if (displayEdgesToInitialState
						|| ((automaton.fail[state] != INITIAL_STATE) || (state == INITIAL_STATE))) {

					sb.append(TAB).append(state).append(" -> ").append(automaton.fail[state])
							.append(STYLE_FAILURE_TRANSITION).append(NEW_LINE);
				}
			}
		}
	}
}