package regex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * 
 * This class represents a regular expression as a tree.
 * 
 * @author Ashkan Forouhi ashkanfb@cs.wisc.edu
 *
 */

public class Regex {
	// TODO avoid having epsilon in concatenations
	// TODO avoid having disjunction with similar children
	// TODO avoid re-modifying already-modified characters (are we doing this?)

	// I pretend that 235 is epsilon :)
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
	// shows whether or not the regex passes all the current positive tests
	private boolean readyToRefineFlag;
	// shows whether or not we have entered the refining process
	private boolean refiningStartedFlag;

	/**
	 * Constructor from a string
	 * 
	 * @param regex regular expression string
	 */
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
		readyToRefineFlag = false;
		refiningStartedFlag = false;
	}

	/**
	 * Copy Constructor
	 * 
	 * @param re
	 */
	public Regex (Regex re) {

		alphabet = new ArrayList<Character> ();
		for (char ch : re.alphabet)
			alphabet.add(ch);
		distance = re.distance;
		readyToRefineFlag = re.readyToRefineFlag;
		refiningStartedFlag = re.refiningStartedFlag;

		switch (re.root.getClass().getName()) {
			case "regex.DisNode":
				root = new DisNode((DisNode)re.root, null, this, re);
				break;
			case "regex.DotNode":
				root = new DotNode((DotNode)re.root, null, this, re);
				break;
			case "regex.StarNode":
				root = new StarNode((StarNode)re.root, null, this, re);
				break;
			case "regex.AlphNode":
				root = new AlphNode((AlphNode)re.root, null, this, re);
				break;
		}
	}

	/**
	 * adds dots for concatenation to the string (used for initial parsing)
	 * 
	 * @param regex a regular expression
	 * @return the input regex with dots inserted in it 
	 */
	private static String addDots (String regex) {
		for (int i = 1; i < regex.length(); i++) {
			char c = regex.charAt(i);
			char c1= regex.charAt(i - 1);
			if ((Character.isAlphabetic(c) || c == '(' || c == EPS) 
					&& (Character.isAlphabetic(c1) || c1 == ')' || c1 == '*' 
					|| c1 == EPS)) {
				regex = regex.substring(0, i) + "." + regex.substring(i);
			}
		}

		return regex;
	}

	/**
	 * changes the regex to infix (for initial parsing)
	 * 
	 * @param regex a regular expression
	 * @return infix version of the input
	 */
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

	/**
	 * Recursively parses a string regular expression and converts it to a tree
	 * 
	 * @param regex a string infix regular expression
	 * @param parent parent of the root node
	 * @param alphabet the regex alphabet (will be updated by method)
	 * @return the root of the generated tree
	 */
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

	/**
	 * enumerates regular expressions that can be created in one step from this
	 * 
	 * @return an Arraylis of generated regexes
	 */
	public ArrayList<Regex> enumeratePossibleChanges() {
		Regex base = new Regex(this);	// we don't want to change 'this'!
		ArrayList<Regex> ret = new ArrayList<Regex>();

		base.distance++;
		if (!base.refiningStartedFlag) {
			if (base.readyToRefineFlag) {
				base.refiningStartedFlag = true;
				ret.addAll(base.refEnum());
			}
			base.refiningStartedFlag = false;
			base.changeRoot = base.modRangeRoot;
			ret.addAll(base.modEnum());
			ret.addAll(base.starEnum());

			base.changeRoot = base.root;
			ret.addAll(base.expEnum());
		}
		else if (base.readyToRefineFlag) {
			ret.addAll(base.refEnum());
		}
		

		return ret;
	}

	/**
	 * Enumerates possible modifying changes
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> modEnum() {
		switch (changeRoot.getClass().getName()) {
			case "regex.DisNode":
				return disNodeModEnum();
			case "regex.DotNode":
				return dotNodeModEnum(null);
			case "regex.StarNode":
				return starNodeModEnum();
			case "regex.AlphNode":
				return alphNodeModEnum();
			default:
				return null;
		}
	}

	/**
	 * Enumerates possible modifying changes (change root is disjunction)
	 * 
	 * @return an Arraylist of generated regexes
	 */
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
				!changeRoot.getParent().getClass().getName()
				.equals("regex.DotNode")) {
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

	/**
	 * Enumerates possible modifying changes (change root is concatenation)
	 * 
	 * @return an Arraylist of generated regexes
	 */
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

	/**
	 * Enumerates possible modifying changes (change root is Kleene star)
	 * 
	 * @return an Arraylist of generated regexes
	 */
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
				!changeRoot.getParent().getClass().getName()
				.equals("regex.DotNode")) {
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

	/**
	 * Enumerates possible modifying changes (change root is an alphabet char)
	 * 
	 * @return an Arraylist of generated regexes
	 */
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
				!changeRoot.getParent().getClass().getName().equals("regex.DotNode")) {
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

	/**
	 * Enumerates possible Star changes
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> starEnum() {
		switch (changeRoot.getClass().getName()) {
			case "regex.DisNode":
				return disNodeStarEnum();
			case "regex.DotNode":
				return dotNodeStarEnum();
			case "regex.StarNode":
				return new ArrayList<Regex> ();
			case "regex.AlphNode":
				return alphNodeStarEnum();
			default:
				return null;
		}
	}

	/**
	 * Enumerates possible modifying changes (change root is disjunction)
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> disNodeStarEnum() {
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
			// Recursively call for all children
			Regex temp = new Regex(this);
			temp.changeRoot = ((DisNode)(temp.changeRoot)).getChild(i);
			if (i != startPos || changeRoot == changeNode)
				temp.changeNode = temp.changeRoot;
			ret.addAll(temp.starEnum());

		}

		// Add star to the node itself
		if (changeRoot.getParent() == null || 
				!changeRoot.getParent().getClass().getName()
				.equals("regex.StarNode")) {
			Regex temp = new Regex(this);
			Node oldNode = temp.changeRoot;
			StarNode node = new StarNode(oldNode.getParent());
			node.setChild(oldNode);
			temp.replaceNode(oldNode, node);
			oldNode.setParent(node);
			ret.add(temp);
		}

		return ret;
	}

	/**
	 * Enumerates possible modifying changes (change root is concatenation)
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> dotNodeStarEnum() {
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

		for (int i = startPos; i < changeRoot.getSize(); i++) {
			// Recursively call for all children
			Regex temp = new Regex(this);
			temp.changeRoot = ((DotNode)(temp.changeRoot)).getChild(i);
			if (i != startPos || changeRoot == changeNode)
				temp.changeNode = temp.changeRoot;
			ret.addAll(temp.starEnum());

		}

		// Add star to the node itself
		if (changeRoot.getParent() == null || 
				!changeRoot.getParent().getClass().getName()
				.equals("regex.StarNode")) {
			Regex temp = new Regex(this);
			Node oldNode = temp.changeRoot;
			StarNode node = new StarNode(oldNode.getParent());
			node.setChild(oldNode);
			temp.replaceNode(oldNode, node);
			oldNode.setParent(node);
			ret.add(temp);
		}

		return ret;
	}


	/**
	 * Enumerates possible modifying changes (change root is an alphabet char)
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> alphNodeStarEnum() {
		ArrayList<Regex> ret = new ArrayList<Regex> ();

		// No point in adding star to epsilon!
		if (((AlphNode)changeRoot).getChar() == EPS)
			return ret;

		if (changeRoot.getParent() == null || 
				!changeRoot.getParent().getClass().getName()
				.equals("regex.StarNode")) {
			Regex temp = new Regex(this);
			Node oldNode = temp.changeRoot;
			StarNode node = new StarNode(oldNode.getParent());
			node.setChild(oldNode);
			temp.replaceNode(oldNode, node);
			oldNode.setParent(node);
			ret.add(temp);
		}

		return ret;
	}

	/**
	 * Enumerates possible expanding changes
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> expEnum() {
		switch (changeRoot.getClass().getName()) {
			case "regex.DisNode":
				return disNodeExpEnum(null);
			case "regex.DotNode":
				return dotNodeExpEnum();
			case "regex.StarNode":
				return starNodeExpEnum();
			case "regex.AlphNode":
				return alphNodeExpEnum();
			default:
				return null;
		}
	}

	/**
	 * Enumerates possible expanding changes (change root is disjunction)
	 * 
	 * @return an Arraylist of generated regexes
	 */
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

	/**
	 * Enumerates possible expanding changes (change root is concatenation)
	 * 
	 * @return an Arraylist of generated regexes
	 */
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
				!changeRoot.getParent().getClass().getName().equals("regex.DisNode")) {
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

	/**
	 * Enumerates possible expanding changes (change root is Kleene Star)
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> starNodeExpEnum() {
		ArrayList<Regex> ret = new ArrayList<Regex> ();

		Regex temp = new Regex(this);
		temp.changeRoot = ((StarNode)(temp.changeRoot)).getChild();
		if (changeRoot == expChangeNode)
			temp.expChangeNode = temp.changeRoot;
		ret.addAll(temp.expEnum());

		if (changeRoot.getParent() == null || 
				!changeRoot.getParent().getClass().getName().equals("regex.DisNode")) {
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

	/**
	 * Enumerates possible expanding changes (change root is an alphabet char)
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> alphNodeExpEnum() {
		ArrayList<Regex> ret = new ArrayList<Regex> ();

		if (changeRoot.getParent() == null || 
				!changeRoot.getParent().getClass().getName().equals("regex.DisNode")) {
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
	
	/**
	 * Enumerates possible refining changes
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> refEnum() {
		switch (changeRoot.getClass().getName()) {
			case "regex.DisNode":
				return disNodeRefEnum();
			case "regex.DotNode":
				return dotNodeRefEnum();
			case "regex.StarNode":
				return starNodeRefEnum();
			case "regex.AlphNode":
				return alphNodeRefEnum();
			default:
				return null;
		}
	}

	/**
	 * Enumerates possible refining changes (change root is disjunction)
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> disNodeRefEnum() {
		ArrayList<Regex> ret = new ArrayList<Regex>();
		
		for (int i = 0; i < ((DisNode)changeRoot).getSize(); i++) {
			Regex temp = new Regex(this);
			temp.changeRoot = ((DisNode)(temp.changeRoot)).getChild(i);
			ret.addAll(temp.refEnum());
			((DisNode)(temp.changeRoot)).removeChild(i);
			ret.add(temp);
		}
		
		return ret;
	}

	/**
	 * Enumerates possible refining changes (change root is concatenation)
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> dotNodeRefEnum() {
		ArrayList<Regex> ret = new ArrayList<Regex>();
		
		for (int i = 0; i < ((DotNode)changeRoot).getSize(); i++) {
			Regex temp = new Regex(this);
			temp.changeRoot = ((DotNode)(temp.changeRoot)).getChild(i);
			ret.addAll(temp.refEnum());
		}
		
		return ret;
	}

	/**
	 * Enumerates possible refining changes (change root is Kleene star)
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> starNodeRefEnum() {
		ArrayList<Regex> ret = new ArrayList<Regex>();
		
		Regex temp = new Regex(this);
		temp.changeRoot = ((StarNode)(temp.changeRoot)).getChild();
		ret.addAll(temp.refEnum());
		
		temp = new Regex(this);
		Node child = ((StarNode)(temp.changeRoot)).getChild();
		temp.replaceNode(temp.changeRoot, child);
		child.setParent(child.getParent().getParent());
		ret.add(temp);
		
		return ret;
	}

	/**
	 * Enumerates possible refining changes (change root is an alphabet char)
	 * 
	 * @return an Arraylist of generated regexes
	 */
	private ArrayList<Regex> alphNodeRefEnum() {
		return new ArrayList<Regex>();
	}

	/**
	 * Setter for changeNode
	 * 
	 * @param changeNode
	 */
	public void setChangeNode(Node changeNode) {
		this.changeNode = changeNode;
	}

	/**
	 * Setter for ExpChangeNode
	 * 
	 * @param expChangeNode
	 */
	public void setExpChangeNode(Node expChangeNode) {
		this.expChangeNode = expChangeNode;
	}

	/**
	 * Setter for changeRoot
	 * 
	 * @param changeRoot
	 */
	public void setChangeRoot(Node changeRoot) {
		this.changeRoot = changeRoot;
	}

	/** 
	 * Setter for modRangeRoot
	 * 
	 * @param modRangeRoot
	 */
	public void setModRangeRoot(Node modRangeRoot) {
		this.modRangeRoot = modRangeRoot;
	}

	/**
	 * getter for changeNode
	 * 
	 * @return changeNode
	 */
	public Node getChangeNode() {
		return changeNode;
	}

	/**
	 * Getter for ExpChangeNode
	 * 
	 * @return ExpChangeNode
	 */
	public Node getExpChangeNode() {
		return expChangeNode;
	}

	/**
	 * Getter for changeRoot
	 * 
	 * @return changeRoot
	 */
	public Node getChangeRoot() {
		return changeRoot;
	}

	/**
	 * Getter for modRangeRoot
	 * 
	 * @return modRangeRoot
	 */
	public Node getModRangeRoot() {
		return modRangeRoot;
	}

	/**
	 * Replaces a node in the tree with a new one
	 * 
	 * @param oldNode the old node
	 * @param newNode the new node
	 */
	private void replaceNode (Node oldNode, Node newNode) {
		if (oldNode.getParent() != null) 
			oldNode.getParent().replaceChild(oldNode, newNode);
		else
			root = newNode;
		if (changeNode == oldNode)
			changeNode = newNode;
		if (expChangeNode == oldNode)
			expChangeNode = newNode;
		if (changeRoot == oldNode)
			changeRoot = newNode;
		if (modRangeRoot == oldNode)
			modRangeRoot = newNode;
	}

	/**
	 * Gets the no of change made to this regex so far
	 * 
	 * @return no of change made to this regex so far
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * Have we started refining?
	 * 
	 * @return the value of the flag
	 */
	public boolean isRefiningStarted() {
		return refiningStartedFlag;
	}

	/**
	 * Is this regex ready to start refining?
	 * 
	 * @return value of the flag
	 */
	public boolean isReadyToRefine() {
		return readyToRefineFlag;
	}

	/**
	 * Sets if we are ready to refine
	 * 
	 * @param readyToRefineFlag the flag value
	 */
	public void setReadyToRefineFlag(boolean readyToRefineFlag) {
		this.readyToRefineFlag = readyToRefineFlag;
	}

	/**
	 * Returns the regex alphabet
	 * 
	 * @return
	 */
	public ArrayList<Character> getAlphabet() {
		return alphabet;
	}

	/**
	 * The regex as a string (this might be a bit different from the 
	 * constructor input!)
	 */
	@Override
	public String toString() {
		return root.toString();
	}

}
