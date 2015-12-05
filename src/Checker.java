import java.util.Scanner;

/**
 * 
 * Main class for the checker.
 * 
 * @author Ashkan Forouhi ashkanfb@cs.wisc.edu
 *
 */

public class Checker {
	
	public static void main(String[] args) {
		
		Scanner in = new Scanner (System.in);
		System.out.print("Enter Regex: ");
		
		String s = in.nextLine();
		Regex r = new Regex(s);
		
		Enumerator e = new Enumerator(s, s);
		int largestDistance = 0;
		
		// to see enumerations, uncomment below
		/*
		for (long i = 0; i < 1000 ; i++) {
			Regex re= e.getNext();
			if (re.getDistance() > largestDistance) {
				largestDistance = re.getDistance();
				System.out.println(largestDistance + " after " + i);
			}
			System.out.println(i + "- " + re + " : " + re.getDistance());
		}
		//*/
		
		// to just enumerate, uncomment below
		//*
		for (long i = 0; true; i++) {
			Regex re= e.getNext();
			
			if (re.getDistance() > largestDistance) {
				largestDistance = re.getDistance();
				System.out.println(largestDistance + " after " + i);
			}
		}
		//*/
			
	}

}
