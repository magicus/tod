/*
 * Created on Oct 18, 2005
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
