import java.util.ArrayList;


public class Enumerator {
	private Regex initial;
	private ArrayList<String> positiveTests;
	private ArrayList<String> negativeTests;
	private ArrayList<Regex> list;
	private int currentIndex;
	
	public Enumerator (String regex) {
		initial = new Regex(regex);
		positiveTests = new ArrayList<String>();
		negativeTests = new ArrayList<String>();
		list = new ArrayList<Regex>();
		list.add(initial);
		currentIndex = 0;
	}
	
	public void addPosTest (String test) {
		positiveTests.add(test);
	}
	
	public void addNegativeTest (String test) {
		negativeTests.add(test);
	}
	
	public Regex getNext() {
		if (currentIndex == list.size()) {
			enumerate();
			currentIndex--;
			return getNext();
		}
		else {
			return list.get(currentIndex++);
		}
	}

	private void enumerate() {
		Regex start = list.get(0);
		list.remove(0);
//		System.out.println(">> Enumerating " + start);
		list.addAll(start.enumeratePossibleChanges());
	}	

}
