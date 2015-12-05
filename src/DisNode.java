import java.util.ArrayList;


public class DisNode extends Node {
	
	private ArrayList<Node> children;
	
	public DisNode(Node parent) {
		super(parent);
		children = new ArrayList<Node>();
	}
	
	// Copy Constructor
	public DisNode(DisNode other, Node parent, Regex re, Node changeNode, 
			Node changeRoot, Node changeRangeRoot) {
		super(other, parent, re, changeNode, changeRoot, changeRangeRoot);
		children = new ArrayList<Node>();
		for (Node ch : other.children) {
			if (ch.getClass().getName().equals("DisNode"))
				children.add(new DisNode((DisNode)ch, this, re, 
									changeNode, changeRoot, changeRangeRoot));
			else if (ch.getClass().getName().equals("DotNode")) 
				children.add(new DotNode((DotNode)ch, this, re,
									changeNode, changeRoot, changeRangeRoot));
			else if (ch.getClass().getName().equals("StarNode")) 
				children.add(new StarNode((StarNode)ch, this, re,
									changeNode, changeRoot, changeRangeRoot));
			else if (ch.getClass().getName().equals("AlphNode")) 
				children.add(new AlphNode((AlphNode)ch, this, re,
									changeNode, changeRoot, changeRangeRoot));	
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
			if (children.get(i).getClass().getName().equals("DisNode")) {
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
}
