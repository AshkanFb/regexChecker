package regex;
/**
 * 
 * A general node in the regex tree. 
 * 
 * @author Ashkan Forouhi ashkanfb@cs.wisc.edu
 *
 */

public class Node {
	
	// The node parent
	private Node parent;
	
	/**
	 * Basic Constructor
	 * @param parent
	 */
	public Node(Node parent) {
		this.parent = parent;
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param other the node we want to copy
	 * @param parent the parent of the new node we are creating
	 * @param re The regular expression tree this new node is in
	 * @param otherRE The regular expression the other node is in
	 */
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
