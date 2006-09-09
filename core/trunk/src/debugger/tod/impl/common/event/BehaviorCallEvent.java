/*
 * Created on Nov 10, 2005
 */
package tod.impl.common.event;

import java.util.ArrayList;
import java.util.List;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;

public abstract class BehaviorCallEvent extends Event implements IBehaviorCallEvent
{
	private List<ILogEvent> itsChildren;
	private boolean itsDirectParent;
	private Object[] itsArguments;
	private IBehaviorInfo itsCalledBehavior;
	private IBehaviorInfo itsExecutedBehavior;
	private Object itsTarget;


	public List<ILogEvent> getChildren()
	{
		return itsChildren;
	}
	
	public int getChildrenCount()
	{
		return itsChildren == null ? 0 : itsChildren.size();
	}

	public void addChild (Event aEvent)
	{
		if (itsChildren == null) itsChildren = new ArrayList<ILogEvent>();
		itsChildren.add(aEvent);
	}

	public void addChild (int aIndex, Event aEvent)
	{
		if (itsChildren == null) itsChildren = new ArrayList<ILogEvent>();
		itsChildren.add(aIndex, aEvent);
	}
	
	public IBehaviorInfo getExecutedBehavior()
	{
		return itsExecutedBehavior;
	}

	public void setExecutedBehavior(IBehaviorInfo aExecutedBehavior)
	{
		itsExecutedBehavior = aExecutedBehavior;
	}
	
	public IBehaviorInfo getCalledBehavior()
	{
		return itsCalledBehavior;
	}

	public void setCalledBehavior(IBehaviorInfo aCalledBehavior)
	{
		itsCalledBehavior = aCalledBehavior;
	}

	public boolean isDirectParent()
	{
		return itsDirectParent;
	}

	public void setDirectParent(boolean aDirectParent)
	{
		itsDirectParent = aDirectParent;
	}

	public Object[] getArguments()
	{
		return itsArguments;
	}

	public void setArguments(Object[] aArguments)
	{
		itsArguments = aArguments;
	}

	public IBehaviorInfo getCallingBehavior()
	{
		if (getParent() == null) return null;
		else return getParent().isDirectParent() ? 
				getParent().getExecutedBehavior()
				: null;
	}

	public Object getTarget()
	{
		return itsTarget;
	}

	public void setTarget(Object aCurrentObject)
	{
		itsTarget = aCurrentObject;
	}


	
	public long getFirstTimestamp()
	{
		return getTimestamp();
	}

	public long getLastTimestamp()
	{
		return getExitEvent().getTimestamp();
	}

	public IBehaviorExitEvent getExitEvent()
	{
		if (getChildrenCount() > 0)
		{
			ILogEvent theLastEvent = getChildren().get(getChildrenCount()-1);
			if (theLastEvent instanceof IBehaviorExitEvent)
			{
				return (IBehaviorExitEvent) theLastEvent;
			}
		}
		
		throw new RuntimeException("Exit event not found");
	}
	
	


}
