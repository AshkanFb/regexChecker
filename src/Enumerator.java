import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import dk.brics.automaton.*;
import regex.Regex;


/**
 * This class is the regular expression enumerator. We start with an initial
 * regex and a target regex, and try to enumerate different changes to initial
 * in order to get to target.
 * 
 * @author Ashkan Forouhi ashkanfb@cs.wisc.edu
 *
 */

public class Enumerator {
	// The initial regex we want to start from
	private Regex initial;
	// The target regex we wish to reach
	private Regex target;
	// The target regex, converted into a DFA
	private Automaton targetDFA;
	// The language alphabet
	private ArrayList<Character> alphabet;
	// The list of positive tests (words that are in target language)
	private HashSet<String> positiveTests;
	// The list of positive tests (words that are not in target language)
	private HashSet<String> negativeTests;
	// Our queue to keep track of enumerations
	private ArrayList<Regex> list;
	// Regexes we have checked so far
	private int currentIndex;
	// How many regexes have we enumerated?
	private int testingCounter;
	// How many regexes passed the tests and needed to be valiated?
	private int validationCounter;
	
	/**
	 * Enumerator Constructor
	 * 
	 * @param targetRegex Our target regular expression
	 * @param inputRegex Our initial regular expression
	 */
	public Enumerator (String targetRegex, String inputRegex) {
		System.setProperty("dk.brics.automaton.debug", "true");
		initial = new Regex(inputRegex);
		System.out.println(initial);
		target = new Regex(targetRegex);
		alphabet = initial.getAlphabet();
		for (char c : target.getAlphabet())
			if (!alphabet.contains(c))
				alphabet.add(c);
		String targetString = targetRegex;
		RegExp targetRE = new RegExp(targetString.replace(Regex.EPS+"", "()"));
		targetDFA = targetRE.toAutomaton();
		setTests();
		System.out.println("Positive Tests:");
		System.out.println(positiveTests);
		System.out.println("Negative Tests:");
		System.out.println(negativeTests);
		list = new ArrayList<Regex>();
		list.add(initial);
		currentIndex = 0;
		testingCounter = 0;
		validationCounter = 0;
	}
	
	/**
	 * Initially fills in positive & negative test, using Myhill-Nerode
	 */
	private void setTests() {
		
		Automaton tDFA = targetDFA.clone();
		positiveTests = new HashSet<String>();
		negativeTests = new HashSet<String>();
		int stateCount = tDFA.getNumberOfStates();
		
		String[] a = new String[stateCount];
		String[][] b = new String[stateCount][stateCount];
		
		int i = 0;
		for (State s : tDFA.getStates()) {
			for (State f : tDFA.getAcceptStates())
				f.setAccept(false);
			s.setAccept(true);
			a[i] = tDFA.getShortestExample(true);
			i++;
		}
		
		i = 0;
		tDFA = targetDFA.clone();
		for (State s: tDFA.getStates()) {
			int j = 0;
			tDFA.setInitialState(s);
			Automaton tDFA2 =  targetDFA.clone();
			for (State s2 : tDFA2.getStates()) {
				tDFA2.setInitialState(s2);

				Automaton minus = tDFA.minus(tDFA2);
				b[i][j] = minus.getShortestExample(true);
				j++;
			}
			i++;
		}
		
		tDFA = targetDFA.clone();
		ArrayList<State> states = new ArrayList<State> (tDFA.getStates());
		for (i = 0; i < stateCount; i++) {
			for (int j = 0; j < stateCount; j++) {
				if (b[i][j] != null)
					positiveTests.add(a[i] + b[i][j]);
				if (b[j][i] != null)
					negativeTests.add(a[i] + b[j][i]);
				for (char c : alphabet) 
					if (c != Regex.EPS) {
						State s = states.get(i).step(c);
						int k = states.indexOf(s);
						if (k != -1 && b[k][j] != null)
							positiveTests.add(a[i] + c + b[k][j]);
						if (k != -1 && b[j][k] != null)
							negativeTests.add(a[i] + c + b[j][k]);
					}
			}
		}
		
	}
	
	/**
	 * Gets the next regular expression in queue to test. If none left, 
	 * enumerate some
	 * 
	 * @return the next candidate regex
	 */
	public Regex getNextInQueue() {
		testingCounter++;
		if (currentIndex == list.size()) {
			enumerate();
			currentIndex--;
			return getNextInQueue();
		}
		else {
			return list.get(currentIndex++);
		}
	}
	
	/**
	 * Gets the next regular expression from the queue that passes all tests
	 * 
	 * @return the next candidate regex to validate
	 */
	public Regex getNextTestPasser() {
		validationCounter++;
		Regex answer = null;
		while (answer == null) {
			Regex re = getNextInQueue();
			Pattern p = Pattern.compile(re.toString().replace(Regex.EPS+"", "()"));
			if (passesPositiveTests(p)) {
				re.setReadyToRefineFlag(true);
				if (passesNegativeTests(p)) {
					answer = re;
				}
			}
			else {
				re.setReadyToRefineFlag(false);
			}
		}
		return answer;
	}
	
	/**
	 * Gets test-passing candidate regexes and 
	 * 
	 * @return the regex with minimum # of changes that is equivalent to target
	 */
	public Regex getNext() {
		Regex answer = null;
		while (answer == null) {
			Regex re = getNextTestPasser();
			if (validateEquivalence(re))
				answer = re;
		}
		return answer;
	}
	
	/**
	 * validates the equivalence of a regex to the target regex. If not 
	 * equivalent, adds counter examples to the set of positive/negative tests 
	 *  
	 * @param re The regular expression we want to validate
	 * @return whether or not the two are equivalent
	 */
	public boolean validateEquivalence(Regex re) {
		RegExp test = new RegExp(re.toString().replace(Regex.EPS+"", "()"));
		Automaton testDFA = test.toAutomaton();
		String posEx = targetDFA.minus(testDFA).getShortestExample(true);
		if (posEx != null){
			positiveTests.add(posEx);
			return false;
		}
		else {
			String negEx = testDFA.minus(targetDFA).getShortestExample(true);
			if (negEx != null) {
				negativeTests.add(negEx);
				return false;
			}
		}
		return true;
	}

	/**
	 * Pops one regex from the beginning of the queue and adds all possible 
	 * changes to the end of the queue
	 */
	private void enumerate() {
		Regex start = list.get(0);
		list.remove(0);
//		System.out.println(">> Enumerating " + start);
		if (start.toString().equals("a*"))
			System.out.println("Hi");
		list.addAll(start.enumeratePossibleChanges());
	}
	
	/**
	 * Checks if a regex passes positive tests
	 * 
	 * @param re The regex we want to check as a java.regex pattern
	 * @return whether or not it passes all positive tests
	 */
	private boolean passesPositiveTests (Pattern re) {
		for (String test : positiveTests)
			if (!re.matcher(test).matches())
				return false;
		return true;
	}
	
	/**
	 * Checks if a regex passes negative tests
	 * 
	 * @param re The regex we want to check as a java.regex pattern
	 * @return whether or not it passes all negative tests
	 */
	private boolean passesNegativeTests (Pattern re) {
		for (String test : negativeTests)
			if (re.matcher(test).matches())
				return false;
		return true;
	}
	
	/**
	 * How many regexes have we tested so far?
	 */
	public int getTestingCounter() {
		return testingCounter;
	}
	
	/**
	 * How many regexes have we validated so far?
	 */
	public int getValidationCounter() {
		return validationCounter;
	}

}
