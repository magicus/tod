/*
 * Created on Jul 20, 2006
 */
package tod.impl.common.event;

import tod.core.model.event.IBehaviorExitEvent;

public class BehaviorExitEvent extends Event implements IBehaviorExitEvent
{
	private boolean itsHasThrown;
	private Object itsResult;

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
}
