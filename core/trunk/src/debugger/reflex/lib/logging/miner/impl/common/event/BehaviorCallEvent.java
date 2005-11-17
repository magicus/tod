/*
 * Created on Nov 10, 2005
 */
package reflex.lib.logging.miner.impl.common.event;

import java.util.ArrayList;
import java.util.List;

import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;

public abstract class BehaviorCallEvent extends Event implements IBehaviorCallEvent
{
	private List<ILogEvent> itsChildren;
	private boolean itsDirectParent;
	private boolean itsHasThrown;
	private Object itsResult;
	private Object[] itsArguments;
	private BehaviorInfo itsCalledBehavior;
	private BehaviorInfo itsExecutedBehavior;
	private Object itsTarget;
	private long itsLastTimestamp;


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
	
	public BehaviorInfo getExecutedBehavior()
	{
		return itsExecutedBehavior;
	}

	public void setExecutedBehavior(BehaviorInfo aExecutedBehavior)
	{
		itsExecutedBehavior = aExecutedBehavior;
	}
	
	public BehaviorInfo getCalledBehavior()
	{
		return itsCalledBehavior;
	}

	public void setCalledBehavior(BehaviorInfo aCalledBehavior)
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

	public boolean hasThrown()
	{
		return itsHasThrown;
	}

	public void setHasThrown(boolean aHasThrown)
	{
		itsHasThrown = aHasThrown;
	}

	public Object getResult()
	{
		return itsResult;
	}

	public void setResult(Object aResult)
	{
		itsResult = aResult;
	}

	public Object[] getArguments()
	{
		return itsArguments;
	}

	public void setArguments(Object[] aArguments)
	{
		itsArguments = aArguments;
	}

	public BehaviorInfo getCallingBehavior()
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
		return itsLastTimestamp;
	}

	public void setLastTimestamp(long aLastTimestamp)
	{
		itsLastTimestamp = aLastTimestamp;
	}


}
