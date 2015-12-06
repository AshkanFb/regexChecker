package regex;
/**
 * 
 * An alphabet/epsilon node.
 * 
 * @author Ashkan Forouhi ashkanfb@cs.wisc.edu
 *
 */

public class AlphNode extends Node {
	
	// The alphabetic value (a character, or epsilon)
	private char val;

	/**
	 * Basic Constructor
	 * @param parent
	 */
	public AlphNode(Node parent, char c) {
		super(parent);
		val = c;
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param other the node we want to copy
	 * @param parent the parent of the new node we are creating
	 * @param re The regular expression tree this new node is in
	 * @param otherRE The regular expression the other node is in
	 */
	public AlphNode(AlphNode other, Node parent, Regex re, Regex otherRE) {
		super(other, parent, re, otherRE);
		val = other.val;	
	}

	
	public void setChar (char c) {
		val = c;
	}
	
	public char getChar() {
		return val;
	}
	
	@Override
	public String toString() {
		// here, choose whether you want to explicitly show epsilon
		return (val == Regex.EPS ? Regex.EPS : val) + "";
	}
	
}
