import java.util.Scanner;

import regex.Regex;

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
		System.out.print("Enter Target Regex: ");
		String target = in.nextLine();
		System.out.print("Enter Regex: ");
		String s = in.nextLine();
		
		Enumerator e = new Enumerator(target , s);
		int largestDistance = 0;
		
		// Uncomment below for checker in practice
		/*
		Regex re = e.getNext();
		System.out.println(s + " can be changed to " + re + " in " 
							+ re.getDistance() + " steps.");
		System.out.println("Regexes enumerated: " + e.getTestingCounter());
		System.out.println("Regexes validated: " + e.getValidationCounter());
		//*/
		
		
		// to see first few enumerations, uncomment below
		/*
		for (long i = 0; i < 1000 ; i++) {
			Regex re= e.getNextInQueue();
			if (re.getDistance() > largestDistance) {
				largestDistance = re.getDistance();
				System.out.println(largestDistance + " after " + i);
			}
			System.out.println(i + "- " + re + " : " + re.getDistance());
		}
		//*/
		
		// to see enumeration count, uncomment below
		//*
		for (long i = 0; true; i++) {
			Regex re= e.getNextInQueue();
						
			if (re.getDistance() > largestDistance) {
				largestDistance = re.getDistance();
				System.out.println(largestDistance + " after " + i);
			}
		}
		//*/
	}

}
