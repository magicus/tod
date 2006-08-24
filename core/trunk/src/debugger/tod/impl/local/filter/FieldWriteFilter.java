/*
 * Created on Nov 8, 2004
 */
package tod.impl.local.filter;

import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IFieldInfo;
import tod.impl.local.LocalCollector;

/**
 * Field write  filter.
 * @author gpothier
 */
public class FieldWriteFilter extends AbstractStatelessFilter
{
	private IFieldInfo itsFieldInfo;
	
	/**
	 * Creates a filter that accepts any field write event.
	 */
	public FieldWriteFilter(LocalCollector aCollector)
	{
		this (aCollector, null);
	}

	/**
	 * Creates a filter that accepts only the field write events 
	 * for a particular field.
	 */
	public FieldWriteFilter(LocalCollector aCollector, IFieldInfo aFieldInfo)
	{
		super (aCollector);
		itsFieldInfo = aFieldInfo;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		if (aEvent instanceof IFieldWriteEvent)
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;
			return itsFieldInfo == null 
				|| theEvent.getField() == itsFieldInfo;
		}
		else return false;
	}

}
