/*
 * Created on Nov 10, 2004
 */
package reflex.lib.logging.miner.api;

import java.util.HashMap;
import java.util.Map;

import reflex.lib.logging.miner.impl.common.AbstractEventBrowser;
import tod.core.model.event.ILogEvent;
import zz.utils.ArrayStack;
import zz.utils.Stack;
import zz.utils.references.IRef;
import zz.utils.references.WeakRef;

/**
 * Permits to implement an {@link tod.core.model.trace.IEventBrowser} that
 * doesn't natively support random access to events.
 * @author gpothier
 */
public abstract class PagedBrowser extends AbstractEventBrowser
{
	private static final int PAGE_SIZE = 50;
	private static final int MRU_SIZE = 5;
	
	/**
	 * Maps page indices to page contents.
	 */
	private Map<Integer, IRef<ILogEvent[]>> itsPages = new HashMap<Integer, IRef<ILogEvent[]>>();
	
	/**
	 * Direct refs to most recently used pages.
	 */
	private Stack<ILogEvent[]> itsMRUPages = new ArrayStack<ILogEvent[]>(MRU_SIZE);
	
	public PagedBrowser()
	{
	}
	
	/**
	 * Subclasses implement page retrieving here
	 * @param aStartIndex Index of the first item of the page
	 * @param aSize Number of items in the page
	 * @return Page's content
	 */
	protected abstract ILogEvent[] loadPage(int aStartIndex, int aSize);
	
	private ILogEvent[] getPageFor(int aIndex)
	{
		int thePageIndex = aIndex / PAGE_SIZE;
		IRef<ILogEvent[]> theRef = itsPages.get(thePageIndex);
		ILogEvent[] theEvents = theRef != null ? theRef.get() : null;
		
		if (theEvents == null)
		{
			theEvents = loadPage (thePageIndex * PAGE_SIZE, PAGE_SIZE);
			itsPages.put (thePageIndex, new WeakRef<ILogEvent[]>(theEvents));
		}
		
		itsMRUPages.push(theEvents);
		return theEvents;
	}
	
	public ILogEvent getEvent (int aIndex)
	{
		if (aIndex < 0 || aIndex > getEventCount())
			throw new IndexOutOfBoundsException();

		return getPageFor(aIndex)[aIndex - ((aIndex / PAGE_SIZE) * PAGE_SIZE)];
	}
	
}
