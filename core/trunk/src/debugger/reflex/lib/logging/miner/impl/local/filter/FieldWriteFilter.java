/*
 * Created on Nov 8, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.FieldInfo;

/**
 * Field write  filter.
 * @author gpothier
 */
public class FieldWriteFilter extends AbstractStatelessFilter
{
	private FieldInfo itsFieldInfo;
	
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
	public FieldWriteFilter(LocalCollector aCollector, FieldInfo aFieldInfo)
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
