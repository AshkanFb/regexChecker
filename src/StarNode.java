public class StarNode extends Node {

	private Node child;

	public StarNode(Node parent) {
		super(parent);
	}

	// Copy Constructor
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

	@Override
	public String toString() {
		return (child.getSize() > 1) ? ("(" + child.toString() + ")*")
				: (child.toString() + "*");
	}

	@Override
	public void eatChildren() {
		child.eatChildren();
		if (child.getClass().getName().equals("StarNode")) {
			StarNode sn = (StarNode)child;
			child = sn.child;
		}	
	}
	
	@Override
	public void replaceChild(Node prev, Node next) {
		child = next;;
	}

	public void setChild(Node child) {
		this.child = child;
	}
	
	public Node getChild() {
		return child;
	}
}
