
public class AlphNode extends Node {
	
	private char val;

	public AlphNode(Node parent, char c) {
		super(parent);
		val = c;
	}
	
	// Copy Constructor
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
