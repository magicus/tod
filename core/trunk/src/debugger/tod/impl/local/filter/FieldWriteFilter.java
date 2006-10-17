/*
 * Created on Nov 8, 2004
 */
package tod.impl.local.filter;

import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IFieldInfo;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalBrowser;

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
	public FieldWriteFilter(LocalBrowser aBrowser)
	{
		this (aBrowser, null);
	}

	/**
	 * Creates a filter that accepts only the field write events 
	 * for a particular field.
	 */
	public FieldWriteFilter(LocalBrowser aBrowser, IFieldInfo aFieldInfo)
	{
		super (aBrowser);
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
