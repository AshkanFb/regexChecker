import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import dk.brics.automaton.*;


/**
 * This class is the regular expression enumerator. We start with an initial
 * regex and a target regex, and try to enumerate different changes to initial
 * in order to get to target.
 * 
 * @author Ashkan Forouhi ashkanfb@cs.wisc.edu
 *
 */

public class Enumerator {
	private Regex initial;
	private Regex target;
	private String targetString;
	private Automaton targetDFA;
	private ArrayList<Character> alphabet;
	private HashSet<String> positiveTests;
	private HashSet<String> negativeTests;
	private ArrayList<Regex> list;
	private int currentIndex;
	private int testingCounter;
	private int validationCounter;
	
	
	public Enumerator (String targetRegex, String inputRegex) {
		System.setProperty("dk.brics.automaton.debug", "true");
		initial = new Regex(inputRegex);
		target = new Regex(targetRegex);
		alphabet = initial.getAlphabet();
		for (char c : target.getAlphabet())
			if (!alphabet.contains(c))
				alphabet.add(c);
		targetString = targetRegex;
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
		}
		return answer;
	}
	
	public Regex getNext() {
		Regex answer = null;
		while (answer == null) {
			Regex re = getNextTestPasser();
			if (validateEquivalence(re))
				answer = re;
		}
		return answer;
	}
	
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

	private void enumerate() {
		Regex start = list.get(0);
		list.remove(0);
//		System.out.println(">> Enumerating " + start);
		list.addAll(start.enumeratePossibleChanges());
	}
	
	private boolean passesPositiveTests (Pattern re) {
		for (String test : positiveTests)
			if (!re.matcher(test).matches())
				return false;
		return true;
	}
	
	private boolean passesNegativeTests (Pattern re) {
		for (String test : negativeTests)
			if (re.matcher(test).matches())
				return false;
		return true;
	}
	
	public int getTestingCounter() {
		return testingCounter;
	}
	
	public int getValidationCounter() {
		return validationCounter;
	}

}
