
public class Node {
	
	private Node parent;
	
	public Node(Node parent) {
		this.parent = parent;
	}
	
	// Copy Constructor
	public Node (Node other, Node parent, Regex re, Regex otherRE) {
		this.parent = parent;
		if (other == otherRE.getChangeNode())
			re.setChangeNode(this);
		if (other == otherRE.getExpChangeNode())
			re.setExpChangeNode(this);
		if (other == otherRE.getChangeRoot())
			re.setChangeRoot(this);
		if (other == otherRE.getModRangeRoot())
			re.setModRangeRoot(this);
	}
	
	public int getSize() {
		return 0;
	}
	
	public void eatChildren() {
		return;
	}
	
	public Node getParent() {
		return parent;
	}
	
	public void setParent(Node parent) {
		this.parent = parent;
	}

	// will be overridden
	public void replaceChild(Node prev, Node next) {
		return;
	}
	
}
