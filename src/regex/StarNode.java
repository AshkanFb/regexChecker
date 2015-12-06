package regex;

/**
 * 
 * A Kleene star node.
 * 
 * @author Ashkan Forouhi ashkanfb@cs.wisc.edu
 *
 */

public class StarNode extends Node {

	// The only child of the Kleene star
	private Node child;

	/**
	 * Basic constructor
	 * 
	 * @param parent parent of the node; null if no parent
	 */
	public StarNode(Node parent) {
		super(parent);
	}

	/**
	 * Copy constructor
	 * 
	 * @param other the node we want to copy
	 * @param parent the parent of the new node we are creating
	 * @param re The regular expression tree this new node is in
	 * @param otherRE The regular expression the other node is in
	 */
	public StarNode(StarNode other, Node parent, Regex re, Regex otherRE) {
		super(other, parent, re, otherRE);

		if (other.child.getClass().getName().equals("DisNode"))
			child = new DisNode((DisNode)other.child, this, re, otherRE);
		else if (other.child.getClass().getName().equals("DotNode")) 
			child = new DotNode((DotNode)other.child, this, re, otherRE);
		else if (other.child.getClass().getName().equals("StarNode")) 
			child = new StarNode((StarNode)other.child, this, re, otherRE);
		else if (other.child.getClass().getName().equals("AlphNode")) 
			child = new AlphNode((AlphNode)other.child, this, re, otherRE);

	}

	/**
	 * The string of the regex subtree, where this node is the root
	 */
	@Override
	public String toString() {
		return (child.getSize() > 1) ? ("(" + child.toString() + ")*")
				: (child.toString() + "*");
	}

	/**
	 * Removes The Kleene star child so we won't have (w*)*, and call 
	 * recursively for the child
	 */
	@Override
	public void eatChildren() {
		child.eatChildren();
		if (child.getClass().getName().equals("StarNode")) {
			StarNode sn = (StarNode)child;
			child = sn.child;
		}	
	}
	
	/**
	 * Replaces the child with another node
	 */
	@Override
	public void replaceChild(Node prev, Node next) {
		child = next;;
	}

	/**
	 * Sets the child to be a node
	 * @param child
	 */
	public void setChild(Node child) {
		this.child = child;
	}
	
	/**
	 * Gets the child of the node
	 */
	public Node getChild() {
		return child;
	}
}
