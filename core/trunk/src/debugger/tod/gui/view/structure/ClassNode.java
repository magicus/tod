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
package tod.gui.view.structure;

import java.util.Collections;
import java.util.Comparator;

import tod.Util;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IMemberInfo;
import zz.utils.tree.SimpleTree;
import zz.utils.tree.SimpleTreeNode;

public class ClassNode extends LocationNode
{
	public ClassNode(SimpleTree<ILocationInfo> aTree, IClassInfo aClass)
	{
		super(aTree, false, aClass);
	}

	public IClassInfo getClassInfo()
	{
		return (IClassInfo) getLocation();
	}
	
	@Override
	protected void init()
	{
		System.out.println("Init for "+getClassInfo());
		for(IBehaviorInfo theBehavior : getClassInfo().getBehaviors())
			addBehaviorNode(theBehavior);

		for(IFieldInfo theField : getClassInfo().getFields())
			addFieldNode(theField);
	}
	
	/**
	 * Adds a new behavior node
	 */
	public BehaviorNode addBehaviorNode(IBehaviorInfo aBehavior)
	{
		int theIndex = Collections.binarySearch(
				pChildren().get(), 
				Util.getFullName(aBehavior),
				MemberComparator.BEHAVIOR);
		
		if (theIndex >= 0) throw new RuntimeException("Behavior already exists: "+aBehavior); 
		BehaviorNode theNode = new BehaviorNode(getTree(), aBehavior);

		pChildren().add(-theIndex-1, theNode);
		return theNode;
	}
	
	/**
	 * Adds a new field node
	 */
	public FieldNode addFieldNode(IFieldInfo aField)
	{
		int theIndex = Collections.binarySearch(
				pChildren().get(), 
				aField.getName(),
				MemberComparator.FIELD);
		
		if (theIndex >= 0) throw new RuntimeException("Field already exists: "+aField); 
		FieldNode theNode = new FieldNode(getTree(), aField);

		pChildren().add(-theIndex-1, theNode);
		return theNode;
	}

}