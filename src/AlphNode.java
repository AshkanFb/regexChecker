
public class AlphNode extends Node {
	
	private char val;

	public AlphNode(Node parent, char c) {
		super(parent);
		val = c;
	}
	
	// Copy Constructor
	public AlphNode(AlphNode other, Node parent, Regex re, Node changeNode, 
			Node changeRoot, Node changeRangeRoot) {
		super(other, parent, re, changeNode, changeRoot, changeRangeRoot);
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
		return val + "";
	}
	
}
