/*
 * Created on Jun 10, 2005
 */
package tod.vbuilder;

import tod.core.database.structure.ObjectId;
import zz.utils.tree.SimpleTree;
import zz.utils.tree.SimpleTreeBuilder;

/**
 * A few simple sample trees.
 * @author gpothier
 */
public class TreeSamples
{
	public static SimpleTree<ObjectId> createTree0()
	{
		SimpleTreeBuilder<ObjectId> theBuilder = new SimpleTreeBuilder<ObjectId>();
		
		theBuilder.root(
				new ObjectId.ObjectUID(1),
				theBuilder.leaf(new ObjectId.ObjectUID(11)),
				theBuilder.leaf(new ObjectId.ObjectUID(12)),
				theBuilder.leaf(new ObjectId.ObjectUID(13)));
		
		return theBuilder.getTree();
	}
	
	public static SimpleTree<ObjectId> createTree1()
	{
		SimpleTreeBuilder<ObjectId> theBuilder = new SimpleTreeBuilder<ObjectId>();
		
		theBuilder.root(
				new ObjectId.ObjectUID(1),
				theBuilder.node(
						new ObjectId.ObjectUID(11),
						theBuilder.leaf(new ObjectId.ObjectUID(111)),
						theBuilder.leaf(new ObjectId.ObjectUID(112)),
						theBuilder.leaf(new ObjectId.ObjectUID(113))),
				theBuilder.leaf(new ObjectId.ObjectUID(12)),
				theBuilder.node(
						new ObjectId.ObjectUID(13),
						theBuilder.leaf(new ObjectId.ObjectUID(131)),
						theBuilder.leaf(new ObjectId.ObjectUID(132))));
		
		return theBuilder.getTree();
	}
	
	public static SimpleTree<ObjectId> createTree2()
	{
		SimpleTreeBuilder<ObjectId> theBuilder = new SimpleTreeBuilder<ObjectId>();
		
		theBuilder.root(
				new ObjectId.ObjectUID(1),
				theBuilder.node(
						new ObjectId.ObjectUID(11),
						theBuilder.leaf(new ObjectId.ObjectUID(111)),
						theBuilder.leaf(new ObjectId.ObjectUID(112)),
						theBuilder.leaf(new ObjectId.ObjectUID(113))),
				theBuilder.leaf(new ObjectId.ObjectUID(12)),
				theBuilder.leaf(new ObjectId.ObjectUID(13)),
				theBuilder.leaf(new ObjectId.ObjectUID(14)),
				theBuilder.node(
						new ObjectId.ObjectUID(15),
						theBuilder.leaf(new ObjectId.ObjectUID(151)),
						theBuilder.leaf(new ObjectId.ObjectUID(152))));
		
		return theBuilder.getTree();
	}
	
	public static SimpleTree<ObjectId> createTree3()
	{
		SimpleTreeBuilder<ObjectId> theBuilder = new SimpleTreeBuilder<ObjectId>();
		
		theBuilder.root(
				new ObjectId.ObjectUID(1),
				theBuilder.node(
						new ObjectId.ObjectUID(11),
						theBuilder.leaf(new ObjectId.ObjectUID(111)),
						theBuilder.leaf(new ObjectId.ObjectUID(112)),
						theBuilder.leaf(new ObjectId.ObjectUID(113)),
						theBuilder.leaf(new ObjectId.ObjectUID(114))),
				theBuilder.node(
						new ObjectId.ObjectUID(12),
						theBuilder.leaf(new ObjectId.ObjectUID(121)),
						theBuilder.leaf(new ObjectId.ObjectUID(122)),
						theBuilder.leaf(new ObjectId.ObjectUID(123))));
		
		return theBuilder.getTree();
	}
}
