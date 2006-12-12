/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
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
