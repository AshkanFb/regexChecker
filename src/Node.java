
public class Node {
	
	private Node parent;
	
	public Node(Node parent) {
		this.parent = parent;
	}
	
	// Copy Constructor
	public Node (Node other, Node parent, Regex re, Node changeNode, 
						Node changeRoot, Node changeRangeRoot) {
		this.parent = parent;
		if (other == changeNode)
			re.setChangeNode(this);
		if (other == changeRoot)
			re.setChangeRoot(this);
		if (other == changeRangeRoot)
			re.setChangeRangeRoot(this);
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
