/*
 * Created on Nov 8, 2004
 */
package tod.impl.local.filter;

import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.impl.local.LocalCollector;

/**
 * Instantiation-related filter.
 * @author gpothier
 */
public class InstantiationFilter extends AbstractStatelessFilter
{
	private ITypeInfo itsTypeInfo;
	private ObjectId itsObject;
	
	/**
	 * Creates a filter that accepts any instantiation event.
	 */
	public InstantiationFilter(LocalCollector aCollector)
	{
		super (aCollector);
	}

	/**
	 * Creates a filer that accepts only the instantiation events
	 * for a specific type.
	 */
	public InstantiationFilter(LocalCollector aCollector, ITypeInfo aTypeInfo)
	{
		super (aCollector);
		itsTypeInfo = aTypeInfo;
	}
	
	/**
	 * Creates a filer that accepts only the instantiation events
	 * for a specific object.
	 */
	public InstantiationFilter(LocalCollector aCollector, ObjectId aObject)
	{
		super (aCollector);
		itsObject = aObject;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		if (aEvent instanceof IInstantiationEvent)
		{
			IInstantiationEvent theEvent = (IInstantiationEvent) aEvent;
			
			if (itsTypeInfo != null && theEvent.getType() != itsTypeInfo) return false;
			if (itsObject != null && ! itsObject.equals(theEvent.getInstance())) return false;
			return true;
		}
		else return false;
	}

}
