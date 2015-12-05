import java.util.ArrayList;
import java.util.regex.Pattern;

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
	private ArrayList<String> positiveTests;
	private ArrayList<String> negativeTests;
	private ArrayList<Regex> list;
	private int currentIndex;
	
	public Enumerator (String targetRegex, String inputRegex) {
		initial = new Regex(inputRegex);
		target = new Regex(targetRegex);
		positiveTests = createPositiveTests();
		negativeTests = createNegativeTests();
		list = new ArrayList<Regex>();
		list.add(initial);
		currentIndex = 0;
	}
	
	private ArrayList<String> createPositiveTests() {
		// TODO Fill this out!
		ArrayList<String> ret = new ArrayList<String> ();
		return ret;
	}
	
	private ArrayList<String> createNegativeTests() {
		// TODO Fill this out!
		ArrayList<String> ret = new ArrayList<String> ();
		return ret;
	}
	
	public Regex getNextInQueue() {
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
		Regex answer = null;
		while (answer == null) {
			Regex re = getNextInQueue();
			Pattern p = Pattern.compile(re.toString());
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
		// TODO Fill this out! :)
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
		for (String test : positiveTests)
			if (re.matcher(test).matches())
				return false;
		return true;
	}

}
