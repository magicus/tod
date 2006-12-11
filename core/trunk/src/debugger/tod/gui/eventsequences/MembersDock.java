/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.gui.eventsequences;

import java.util.HashMap;
import java.util.Map;

import tod.core.database.browser.IObjectInspector;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMemberInfo;
import tod.gui.view.LogView;

/**
 * A {@link tod.gui.eventsequences.SequenceViewsDock} specialized for displaying members of a type.
 * @author gpothier
 */
public class MembersDock extends SequenceViewsDock
{
	private Map<IMemberInfo, Integer> itsMembersMap = new HashMap<IMemberInfo, Integer>();
	
	public MembersDock(LogView aLogView)
	{
		super (aLogView);
	}

	protected IEventSequenceSeed createSeed (IObjectInspector aInspector, IMemberInfo aMember)
	{
		if (aMember instanceof IFieldInfo)
		{
			IFieldInfo theField = (IFieldInfo) aMember;
			return new FieldSequenceSeed(aInspector, theField);
		}
		else if (aMember instanceof IBehaviorInfo)
		{
			IBehaviorInfo theBehavior = (IBehaviorInfo) aMember;
			switch (theBehavior.getBehaviourKind())
			{
			case METHOD:
				return new MethodSequenceSeed(aInspector, theBehavior);
			default:
				throw new RuntimeException("Not handled: "+theBehavior.getBehaviourKind());
			}
		}
		else 				
			throw new RuntimeException("Not handled: "+aMember);
	}
	
	public void addMember (IObjectInspector aInspector, IMemberInfo aMember)
	{
		IEventSequenceSeed theSeed = createSeed(aInspector, aMember);
		int theIndex = pSeeds().size();
		pSeeds().add(theIndex, theSeed);
		itsMembersMap.put(aMember, theIndex);
	}

	public void removeMember (IMemberInfo aMember)
	{
		Integer theIndex = itsMembersMap.get(aMember);
		if (theIndex == null) throw new RuntimeException("Seed not found for member: "+aMember);
		pSeeds().remove(theIndex.intValue());
	}
}
