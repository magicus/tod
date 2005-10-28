/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import java.util.HashMap;
import java.util.Map;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.BehaviourType;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.MemberInfo;
import tod.core.model.trace.IObjectInspector;

/**
 * A {@link tod.gui.eventsequences.SequenceViewsDock} specialized for displaying members of a type.
 * @author gpothier
 */
public class MembersDock extends SequenceViewsDock
{
	private Map<MemberInfo, Integer> itsMembersMap = new HashMap<MemberInfo, Integer>();
	
	public MembersDock(LogView aLogView)
	{
		super (aLogView);
	}

	protected IEventSequenceSeed createSeed (IObjectInspector aInspector, MemberInfo aMember)
	{
		if (aMember instanceof FieldInfo)
		{
			FieldInfo theField = (FieldInfo) aMember;
			return new FieldSequenceSeed(aInspector, theField);
		}
		else if (aMember instanceof BehaviorInfo)
		{
			BehaviorInfo theBehavior = (BehaviorInfo) aMember;
			switch (theBehavior.getBehaviourType())
			{
			case METHOD:
				return new MethodSequenceSeed(aInspector, theBehavior);
			default:
				throw new RuntimeException("Not handled: "+theBehavior.getBehaviourType());
			}
		}
		else 				
			throw new RuntimeException("Not handled: "+aMember);
	}
	
	public void addMember (IObjectInspector aInspector, MemberInfo aMember)
	{
		IEventSequenceSeed theSeed = createSeed(aInspector, aMember);
		int theIndex = pSeeds().size();
		pSeeds().add(theIndex, theSeed);
		itsMembersMap.put(aMember, theIndex);
	}

	public void removeMember (MemberInfo aMember)
	{
		Integer theIndex = itsMembersMap.get(aMember);
		if (theIndex == null) throw new RuntimeException("Seed not found for member: "+aMember);
		pSeeds().remove(theIndex.intValue());
	}
}
