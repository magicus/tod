/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.core.database.structure.tree;

import java.util.Collections;

import tod.Util;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationInfo;
import zz.utils.tree.SimpleTree;

public class ClassNode extends LocationNode
{
	private final boolean itsShowFields;
	private final boolean itsShowBehaviors;

	public ClassNode(
			SimpleTree<ILocationInfo> aTree, 
			IClassInfo aClass, 
			boolean aShowFields,
			boolean aShowBehaviors)
	{
		super(aTree, ! (aShowFields || aShowBehaviors), aClass);
		itsShowFields = aShowFields;
		itsShowBehaviors = aShowBehaviors;
	}

	public IClassInfo getClassInfo()
	{
		return (IClassInfo) getLocation();
	}
	
	@Override
	protected void init()
	{
		System.out.println("Init for "+getClassInfo());
		
		if (itsShowFields) for(IFieldInfo theField : getClassInfo().getFields())
			addFieldNode(theField);

		if (itsShowBehaviors) for(IBehaviorInfo theBehavior : getClassInfo().getBehaviors())
			addBehaviorNode(theBehavior);
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