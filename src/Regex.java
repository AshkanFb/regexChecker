import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;


public class Regex {
	// TODO avoid having epsilon in concatenations
	// TODO avoid having disjunction with similar children

	public static final char EPS = 235;

	// the tree root
	private Node root;
	// We only need this for initial parsing
	private static int parserIndex;
	// Last Node that was changed by a modifying change
	private Node changeNode;
	// Last Node that was changed by an expanding change
	private Node expChangeNode;
	// The root of subtree we want to change now
	private Node changeRoot;
	// The root of all possible mod changes subtree. Usually this is the root,
	// unless we start expanding a section
	private Node modRangeRoot;
	// The regex alphabet
	private ArrayList<Character> alphabet;
	// Distance from initial regex
	private int distance;

	public Regex (String regex) {

		alphabet = new ArrayList<Character>();
		alphabet.add(EPS);

		regex = addDots(regex);
		regex = toInfix(regex);

		parserIndex = 0;		// start parsing
		root = treeMaker (regex, null, alphabet);
		root.eatChildren();

		changeNode = root;
		expChangeNode = root;
		changeRoot = root;
		modRangeRoot = root;
		distance = 0;
	}

	//Copy Constructor
	public Regex (Regex re) {

		alphabet = new ArrayList<Character> ();
		for (char ch : re.alphabet)
			alphabet.add(ch);
		distance = re.distance;

		switch (re.root.getClass().getName()) {
			case "DisNode":
				root = new DisNode((DisNode)re.root, null, this, re);
				break;
			case "DotNode":
				root = new DotNode((DotNode)re.root, null, this, re);
				break;
			case "StarNode":
				root = new StarNode((StarNode)re.root, null, this, re);
				break;
			case "AlphNode":
				root = new AlphNode((AlphNode)re.root, null, this, re);
				break;
		}
	}


	private static String addDots (String regex) {
		for (int i = 1; i < regex.length(); i++) {
			char c = regex.charAt(i);
			char c1= regex.charAt(i - 1);
			if ((Character.isAlphabetic(c) || c == '(') 
					&& (Character.isAlphabetic(c1) || c1 == ')' || c == '*')) {
				regex = regex.substring(0, i) + "." + regex.substring(i);
			}
		}

		return regex;
	}

	private static String toInfix (String regex) {
		Stack<Character> stack = new Stack<Character>();
		String infix = "";

		try {
			for (int i = regex.length() - 1; i >= 0; i--) {
				char c = regex.charAt(i);
				if (c == '|') {
					while (!stack.isEmpty() && (stack.peek() == '*' || 
							stack.peek() == '.'))
						infix = stack.pop() + infix;
					stack.push(c);
				}
				else if (c == '.') {
					while (!stack.isEmpty() && stack.peek() == '*')
						infix = stack.pop() + infix;
					stack.push(c);
				}
				else if (c == '*' || c == ')') {
					stack.push(c);
				}
				else if (c == '(') {
					while (stack.peek() != ')') {
						infix = stack.pop() + infix;
					}
					stack.pop();
				}
				else {
					infix = c + infix;				
				}
			}
			while (!stack.isEmpty())
				infix = stack.pop() + infix;
		}
		catch (EmptyStackException e) {
			System.err.println("Error: Input regex is not valid!");
		}

		return infix;
	}

	private static Node treeMaker (String regex, Node parent, 
			ArrayList<Character> alphabet) {
		char ch = regex.charAt(parserIndex);
		parserIndex++;

		Node current = null;

		if (ch == '|') {
			DisNode n = new DisNode(parent);
			Node child1 = treeMaker(regex, n, alphabet);
			Node child2 = treeMaker(regex, n, alphabet);
			n.addChild(child1);
			n.addChild(child2);
			current = n;
		}

		else if (ch == '.') {
			DotNode n = new DotNode(parent);
			Node child1 = treeMaker(regex, n, alphabet);
			Node child2 = treeMaker(regex, n, alphabet);
			n.addChild(child1);
			n.addChild(child2);
			current = n;
		}

		else if (ch == '*') {
			StarNode n = new StarNode(parent);
			Node child = treeMaker(regex, n, alphabet);
			n.setChild(child);
			current = n;
		}

		else {
			current = new AlphNode(parent, ch);
			if (!alphabet.contains(ch))
				alphabet.add(ch);
		}

		return current;
	}


	public ArrayList<Regex> enumeratePossibleChanges() {
		Regex base = new Regex(this);	// we don't want to change 'this'!
		ArrayList<Regex> ret;

		base.distance++;
		base.changeRoot = base.modRangeRoot;
		ret = base.modEnum();

		base.changeRoot = base.root;
		ret.addAll(base.expEnum());

		return ret;
	}

	private ArrayList<Regex> modEnum() {
		switch (changeRoot.getClass().getName()) {
			case "DisNode":
				return disNodeModEnum();
			case "DotNode":
				return dotNodeModEnum(null);
			case "StarNode":
				return starNodeModEnum();
			case "AlphNode":
				return alphNodeModEnum();
			default:
				return null;
		}
	}


	private ArrayList<Regex> disNodeModEnum() {
		ArrayList<Regex> ret = new ArrayList<Regex> ();

		//find the enumeration starting position, based on changeNode
		int startPos = 0;
		if (changeRoot != changeNode) {
			Node child = changeNode;
			Node parent = changeNode.getParent();
			while (parent != changeRoot) {
				child = parent;
				parent = child.getParent();
			}
			startPos = ((DisNode)changeRoot).getChildIndex(child);
		}

		for (int i = startPos; i < changeRoot.getSize(); i++) {	
			// Recursively call for all children, except changeNode itself.
			if (changeNode != ((DisNode)changeRoot).getChild(i)) {
				Regex temp = new Regex(this);
				temp.changeRoot = ((DisNode)(temp.changeRoot)).getChild(i);
				if (i > startPos || changeRoot == changeNode)
					temp.changeNode = temp.changeRoot;
				ret.addAll(temp.modEnum());
			}

		}

		if (changeRoot.getParent() == null || 
				!changeRoot.getParent().getClass().getName().equals("DotNode")) {
			Regex temp = new Regex(this);
			Node oldNode = temp.changeRoot;
			DotNode node = new DotNode(oldNode.getParent());
			node.addChild(oldNode);
			temp.replaceNode(oldNode, node);
			oldNode.setParent(node);
			ret.addAll(temp.dotNodeModEnum(oldNode));
		}

		return ret;
	}

	private ArrayList<Regex> dotNodeModEnum(Node alreadyEnumeratedChild) {
		ArrayList<Regex> ret = new ArrayList<Regex> ();

		//find the enumeration starting position, based on changeNode
		int startPos = 0;
		if (changeRoot != changeNode) {
			Node child = changeNode;
			Node parent = changeNode.getParent();
			while (parent != changeRoot) {
				child = parent;
				parent = child.getParent();
			}
			startPos = ((DotNode)changeRoot).getChildIndex(child);
		}
		else {
			// changeNode is same as changeRoot, so this branch has never 
			// been enumerated before. So first, initial epsilon.
			Regex temp = new Regex(this);
			AlphNode newNode = new AlphNode(temp.changeRoot, EPS);
			((DotNode)(temp.changeRoot)).addChild(0, newNode);
			temp.changeRoot = newNode;
			temp.changeNode = newNode;
			ret.addAll(temp.alphNodeModEnum());
		}

		for (int i = startPos; i < changeRoot.getSize(); i++) {
			// Recursively call for all children, except the one already 
			// enumerated (if any) or changeNode itself.
			if (alreadyEnumeratedChild != ((DotNode)changeRoot).getChild(i)
					&& changeNode != ((DotNode)changeRoot).getChild(i)) {
				Regex temp = new Regex(this);
				temp.changeRoot = ((DotNode)(temp.changeRoot)).getChild(i);
				if (i != startPos || changeRoot == changeNode)
					temp.changeNode = temp.changeRoot;
				ret.addAll(temp.modEnum());
			}

			// add epsilon & enumerate
			Regex temp = new Regex(this);
			AlphNode newNode = new AlphNode(temp.changeRoot, EPS);
			((DotNode)(temp.changeRoot)).addChild(i + 1, newNode);
			temp.changeRoot = newNode;
			temp.changeNode = newNode;
			ret.addAll(temp.alphNodeModEnum());
		}

		return ret;
	}

	private ArrayList<Regex> starNodeModEnum() {
		ArrayList<Regex> ret = new ArrayList<Regex> ();

		// Recursively call for the child, unless it is changeNode.
		if (changeNode != ((StarNode)changeRoot).getChild()) {
			Regex temp = new Regex(this);
			temp.changeRoot = ((StarNode)(temp.changeRoot)).getChild();
			if (changeRoot == changeNode)
				temp.changeNode = temp.changeRoot;
			ret.addAll(temp.modEnum());
		}

		if (changeRoot.getParent() == null || 
				!changeRoot.getParent().getClass().getName().equals("DotNode")) {
			Regex temp = new Regex(this);
			Node oldNode = temp.changeRoot;
			DotNode node = new DotNode(oldNode.getParent());
			node.addChild(oldNode);
			temp.replaceNode(oldNode, node);
			oldNode.setParent(node);
			ret.addAll(temp.dotNodeModEnum(oldNode));
		}

		return ret;
	}

	private ArrayList<Regex> alphNodeModEnum() {
		ArrayList<Regex> ret = new ArrayList<Regex> ();

		for (char ch : alphabet) {
			if (ch != ((AlphNode)changeRoot).getChar()) {
				Regex temp = new Regex(this);
				((AlphNode)(temp.changeRoot)).setChar(ch);
				temp.changeNode = temp.changeRoot;
				ret.add(temp);
			}
		}

		if (changeRoot.getParent() == null || 
				!changeRoot.getParent().getClass().getName().equals("DotNode")) {
			Regex temp = new Regex(this);
			Node oldNode = temp.changeRoot;
			DotNode node = new DotNode(oldNode.getParent());
			node.addChild(oldNode);
			temp.replaceNode(oldNode, node);
			oldNode.setParent(node);
			ret.addAll(temp.dotNodeModEnum(oldNode));
		}

		return ret;
	}

	private ArrayList<Regex> expEnum() {
		switch (changeRoot.getClass().getName()) {
			case "DisNode":
				return disNodeExpEnum(null);
			case "DotNode":
				return dotNodeExpEnum();
			case "StarNode":
				return starNodeExpEnum();
			case "AlphNode":
				return alphNodeExpEnum();
			default:
				return null;
		}
	}

	private ArrayList<Regex> disNodeExpEnum(Node alreadyEnumeratedChild) {
		ArrayList<Regex> ret = new ArrayList<Regex> ();

		//find the enumeration starting position, based on expChangeNode
		int startPos = 0;
		if (changeRoot != expChangeNode) {
			Node child = expChangeNode;
			Node parent = expChangeNode.getParent();
			while (parent != changeRoot) {
				child = parent;
				parent = child.getParent();
			}
			startPos = ((DisNode)changeRoot).getChildIndex(child);
		}

		for (int i = startPos; i < changeRoot.getSize(); i++) {	
			// Recursively call for all children, except the one already 
			// enumerated (if any). 
			if (alreadyEnumeratedChild != ((DisNode)changeRoot).getChild(i)) {
				Regex temp = new Regex(this);
				temp.changeRoot = ((DisNode)(temp.changeRoot)).getChild(i);
				if (i != startPos || changeRoot == expChangeNode)
					temp.expChangeNode = temp.changeRoot;
				ret.addAll(temp.expEnum());
			}
		}

		for (char ch : alphabet) {
			Regex temp = new Regex(this);
			AlphNode newNode = new AlphNode (temp.changeRoot, ch);
			((DisNode)(temp.changeRoot)).addChild(newNode);
			temp.modRangeRoot = newNode;
			temp.changeNode = newNode;
			temp.expChangeNode = newNode;
			ret.add(temp);
		}


		return ret;
	}

	private ArrayList<Regex> dotNodeExpEnum() {
		ArrayList<Regex> ret = new ArrayList<Regex> ();

		//find the enumeration starting position, based on expChangeNode
		int startPos = 0;
		if (changeRoot != expChangeNode) {
			Node child = expChangeNode;
			Node parent = expChangeNode.getParent();
			while (parent != changeRoot) {
				child = parent;
				parent = child.getParent();
			}
			startPos = ((DotNode)changeRoot).getChildIndex(child);
		}

		for (int i = startPos; i < changeRoot.getSize(); i++) {	
			Regex temp = new Regex(this);
			temp.changeRoot = ((DotNode)(temp.changeRoot)).getChild(i);
			if (i != startPos || changeRoot == expChangeNode)
				temp.expChangeNode = temp.changeRoot;
			ret.addAll(temp.expEnum());
		}

		if (changeRoot.getParent() == null || 
				!changeRoot.getParent().getClass().getName().equals("DisNode")) {
			Regex temp = new Regex(this);
			Node oldNode = temp.changeRoot;
			DisNode node = new DisNode(oldNode.getParent());
			node.addChild(oldNode);
			temp.replaceNode(oldNode, node);
			oldNode.setParent(node);
			ret.addAll(temp.disNodeExpEnum(oldNode));
		}

		return ret;
	}

	private ArrayList<Regex> starNodeExpEnum() {
		ArrayList<Regex> ret = new ArrayList<Regex> ();

		Regex temp = new Regex(this);
		temp.changeRoot = ((StarNode)(temp.changeRoot)).getChild();
		if (changeRoot == expChangeNode)
			temp.expChangeNode = temp.changeRoot;
		ret.addAll(temp.expEnum());

		if (changeRoot.getParent() == null || 
				!changeRoot.getParent().getClass().getName().equals("DisNode")) {
			temp = new Regex(this);
			Node oldNode = temp.changeRoot;
			DisNode node = new DisNode(oldNode.getParent());
			node.addChild(oldNode);
			temp.replaceNode(oldNode, node);
			oldNode.setParent(node);
			ret.addAll(temp.disNodeExpEnum(oldNode));
		}

		return ret;
	}

	private ArrayList<Regex> alphNodeExpEnum() {
		ArrayList<Regex> ret = new ArrayList<Regex> ();

		if (changeRoot.getParent() == null || 
				!changeRoot.getParent().getClass().getName().equals("DisNode")) {
			Regex temp = new Regex(this);
			Node oldNode = temp.changeRoot;
			DisNode node = new DisNode(oldNode.getParent());
			node.addChild(oldNode);
			temp.replaceNode(oldNode, node);
			oldNode.setParent(node);
			ret.addAll(temp.disNodeExpEnum(oldNode));
		}

		return ret;
	}

	public void setChangeNode(Node changeNode) {
		this.changeNode = changeNode;
	}

	public void setExpChangeNode(Node expChangeNode) {
		this.expChangeNode = expChangeNode;
	}

	public void setChangeRoot(Node changeRoot) {
		this.changeRoot = changeRoot;
	}

	public void setModRangeRoot(Node modRangeRoot) {
		this.modRangeRoot = modRangeRoot;
	}

	public Node getChangeNode() {
		return changeNode;
	}

	public Node getExpChangeNode() {
		return expChangeNode;
	}

	public Node getChangeRoot() {
		return changeRoot;
	}

	public Node getModRangeRoot() {
		return modRangeRoot;
	}

	private void replaceNode (Node prev, Node next) {
		if (prev.getParent() != null) 
			prev.getParent().replaceChild(prev, next);
		else
			root = next;
		if (changeNode == prev)
			changeNode = next;
		if (expChangeNode == prev)
			expChangeNode = next;
		if (changeRoot == prev)
			changeRoot = next;
		if (modRangeRoot == prev)
			modRangeRoot = next;
	}

	public int getDistance() {
		return distance;
	}

	@Override
	public String toString() {
		return root.toString();
	}

}
