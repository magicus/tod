package tod.vbuilder;

import java.util.ArrayList;
import java.util.List;

import tod.core.model.structure.ObjectId;
import zz.utils.properties.IListProperty;
import zz.utils.tree.SimpleTreeNode;

class BaseTreeStructure
{
	public static final NodeAttribute<IObjectNode> _parent = 
		new NodeAttribute<IObjectNode>("parent");
	
	public static final NodeAttribute<List<IObjectNode>> _children = 
		new NodeAttribute<List<IObjectNode>>("children");
	
	public static final NodeAttribute<IObjectNode> _nextSibling = 
		new NodeAttribute<IObjectNode>("nextSibling");
	
	public static final NodeAttribute<IObjectNode> _prevSibling = 
		new NodeAttribute<IObjectNode>("prevSibling");
	
	/**
	 * Next node in a breadth-first traversal
	 */
	public static final NodeAttribute<IObjectNode> _nextBFT = 
		new NodeAttribute<IObjectNode>("nextBFT");
	
	/**
	 * Previous node in a breadth-first traversal
	 */
	public static final NodeAttribute<IObjectNode> _prevBFT = 
		new NodeAttribute<IObjectNode>("prevBFT");
	
	public static final NodeAttribute<IObjectNode> _firstChild = 
		new NodeAttribute<IObjectNode>("firstChild");
	
	public static final NodeAttribute<IObjectNode> _lastChild = 
		new NodeAttribute<IObjectNode>("lastChild");
	

	private Cell itsCell;
	
	public BaseTreeStructure (Cell aCell)
	{
		itsCell = aCell;
		
		SimpleTreeNode<ObjectId> root = TreeSamples.createTree1().getRoot();
		IObjectNode objNode = itsCell.importNode(root.pValue().get());
		add (objNode, root);
	}
	
	private void add(IObjectNode aObjectNode, SimpleTreeNode<ObjectId> aTreeNode)
	{
		int i = 0;
		IListProperty<SimpleTreeNode<ObjectId>> theChildren = aTreeNode.pChildren();
		if (theChildren != null) for (SimpleTreeNode<ObjectId> theChildNode : theChildren)
		{
			IObjectNode theChild = itsCell.importNode (theChildNode.pValue().get());
			addChild (aObjectNode, theChild, i++);
			add (theChild, theChildNode);
		}
	}

	public void addChild (IObjectNode aParent, IObjectNode aChild, int aIndex)
	{
		if (aChild.get(_parent) != null) throw new RuntimeException("Child already has a parent");
		if (aChild.get(_nextSibling) != null
			|| aChild.get(_prevSibling) != null ) throw new RuntimeException("Child already has a sibling(s)");
		
		aChild.set(_parent, aParent);
		
		List<IObjectNode> children = aParent.get(_children);
		if (children == null) 
		{
			children = new ArrayList<IObjectNode>();
			aParent.set(_children, children);
		}
		
		children.add(aIndex, aChild);
		
		int n = children.size();

		IObjectNode prev = aIndex > 0 ? children.get(aIndex-1) : null;
		IObjectNode next = aIndex < n-1 ? children.get(aIndex+1) : null;

		// Setup sibling relationships
		insert (aChild, prev, next, _prevSibling, _nextSibling);
		
		// Setup BFT relationships
		// 1. Determine next and previous nodes
		IObjectNode prevBFT = prev != null ? prev : (next != null ? next.get(_prevBFT) : findPrevBFT(aChild));
		IObjectNode nextBFT = next != null ? next : (prev != null ? prev.get(_nextBFT) : findNextBFT(aChild));
		
		// 2. Setup relations
		insert (aChild, prevBFT, nextBFT, _prevBFT, _nextBFT);

		if (aIndex == 0) aParent.set(_firstChild, aChild);
		if (aIndex == n-1) aParent.set(_lastChild, aChild);
	}
	
	/**
	 * Finds the nodes that preceeds the given node in a breadth-first traversal.
	 */
	private IObjectNode findPrevBFT(IObjectNode aNode)
	{
		IObjectNode theCurrentUncling = aNode.get(_parent);
		while (true)
		{
			theCurrentUncling = theCurrentUncling.get(_prevBFT);
			if (theCurrentUncling == null) return null;
			
			IObjectNode theNode = theCurrentUncling.get(_lastChild);
			if (theNode != null) return theNode;
		}
	}
	
	/**
	 * Finds the nodes that follows the given node in a breadth-first traversal.
	 */
	private IObjectNode findNextBFT(IObjectNode aNode)
	{
		IObjectNode theCurrentUncling = aNode.get(_parent);
		while (true)
		{
			theCurrentUncling = theCurrentUncling.get(_nextBFT);
			if (theCurrentUncling == null) return null;
			
			IObjectNode theNode = theCurrentUncling.get(_firstChild);
			if (theNode != null) return theNode;
		}		
	}
	
	/**
	 * Inserts the given node between two nodes.
	 * @param aNode The node to insert
	 * @param aPrev The previous node
	 * @param aNext The next node
	 * @param aPrevAttribute The attribute that defines the next-in-sequence relationship
	 * @param aNextAttribute The attribute that defines the previous-in-sequence relationship
	 */
	private void insert (
			IObjectNode aNode, 
			IObjectNode aPrev, 
			IObjectNode aNext, 
			NodeAttribute<IObjectNode> aPrevAttribute,
			NodeAttribute<IObjectNode> aNextAttribute)
	{
		if (aPrev != null)
		{
			aPrev.set(aNextAttribute, aNode);
			aNode.set(aPrevAttribute, aPrev);
		}
		
		if (aNext != null)
		{
			aNext.set(aPrevAttribute, aNode);
			aNode.set(aNextAttribute, aNext);
		}
		
	}
}
