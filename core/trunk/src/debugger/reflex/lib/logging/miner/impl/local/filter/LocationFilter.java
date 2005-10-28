/*
 * Created on Nov 8, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.event.IEvent_Location;
import tod.core.model.event.IInstantiationEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.ObjectId;
import tod.core.model.structure.TypeInfo;

/**
 * A filter that accepts events that occured at a particular location
 * in the source code.
 * @author gpothier
 */
public class LocationFilter extends AbstractStatelessFilter
{
	private TypeInfo itsTypeInfo;
	private int aLineNumber;
	
	
	public LocationFilter(LocalCollector aCollector, TypeInfo aInfo, int aNumber)
	{
		super(aCollector);
		
		itsTypeInfo = aInfo;
		aLineNumber = aNumber;
	}


	public boolean accept(ILogEvent aEvent)
	{
		// TODO: reimplement with bytecode indices
		return false;
//		if (aEvent instanceof IEvent_Location)
//		{
//			IEvent_Location theEvent = (IEvent_Location) aEvent;
//			
//			BehaviorInfo theLocation = theEvent.getOperationLocation();
//			int theLineNumber = theEvent.getOperationLineNumber();
//			
//			TypeInfo theType = theLocation.getType();
//			
//			return theType == itsTypeInfo && theLineNumber == aLineNumber;
//		}
//		else return false;
	}

}
