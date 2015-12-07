package regex;
import java.util.ArrayList;

/**
 * 
 * A disjunction node.
 * 
 * @author Ashkan Forouhi ashkanfb@cs.wisc.edu
 *
 */

public class DisNode extends Node {
	
	// The list of the children
	private ArrayList<Node> children;
	
	/**
	 * Basic Constructor
	 * @param parent
	 */
	public DisNode(Node parent) {
		super(parent);
		children = new ArrayList<Node>();
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param other the node we want to copy
	 * @param parent the parent of the new node we are creating
	 * @param re The regular expression tree this new node is in
	 * @param otherRE The regular expression the other node is in
	 */
	public DisNode(DisNode other, Node parent, Regex re, Regex otherRE) {
		super(other, parent, re, otherRE);
		children = new ArrayList<Node>();
		for (Node ch : other.children) {
			if (ch.getClass().getName().equals("regex.DisNode"))
				children.add(new DisNode((DisNode)ch, this, re, otherRE));
			else if (ch.getClass().getName().equals("regex.DotNode")) 
				children.add(new DotNode((DotNode)ch, this, re, otherRE));
			else if (ch.getClass().getName().equals("regex.StarNode")) 
				children.add(new StarNode((StarNode)ch, this, re, otherRE));
			else if (ch.getClass().getName().equals("regex.AlphNode")) 
				children.add(new AlphNode((AlphNode)ch, this, re, otherRE));	
		}	
	}
	
	@Override
	public String toString() {
		if (children.size() == 0)
			return "";
		String ret = children.get(0).toString();
		for (int i = 1; i < children.size(); i++)
			ret += ("|" + children.get(i));
		return ret;
	}
	
	@Override
	public int getSize() {
		return children.size();
	}
	
	@Override
	public void eatChildren() {
		for (int i = 0; i < children.size(); i++) {
			children.get(i).eatChildren();
			if (children.get(i).getClass().getName().equals("regex.DisNode")) {
				DisNode dn = (DisNode)children.get(i);
		
				for(int j = 0; j < dn.children.size(); j++) {
					children.add(i + j, dn.children.get(j));
				}
				
				i += dn.children.size();
				children.remove(i);
				i--;
			}
		}
	}
	
	@Override
	public void replaceChild(Node prev, Node next) {
		children.set(children.indexOf(prev), next);
	}
	
	public void addChild (Node n) {
		children.add(n);
	}
	
	public Node getChild(int i) {
		return children.get(i);
	}

	public int getChildIndex(Node child) {
		return children.indexOf(child);
	}

	public void removeChild(int i) {
		children.remove(i);
		
	}
}
