import java.util.Scanner;


public class Checker {
	
	public static void main(String[] args) {
		
		Scanner in = new Scanner (System.in);
		System.out.print("Enter Regex: ");
		
		String s = in.nextLine();
		Regex r = new Regex(s);
		
		Enumerator e = new Enumerator(s);
		int largestDistance = 0;
		
		for (long i = 0;  true; i++) {
			Regex re= e.getNext();
			if (re.getDistance() > largestDistance) {
				largestDistance = re.getDistance();
				System.out.println(largestDistance + " after " + i);
			}
		//	System.out.println(i + "- " + re + " : " + re.getDistance());
		}
			
	}

}
