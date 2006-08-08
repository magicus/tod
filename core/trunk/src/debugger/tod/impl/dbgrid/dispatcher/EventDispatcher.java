/*
 * Created on Jul 21, 2006
 */
package tod.impl.dbgrid.dispatcher;

import java.util.ArrayList;
import java.util.List;

import tod.impl.common.event.BehaviorCallEvent;
import tod.impl.common.event.Event;

public class EventDispatcher
{
	/**
	 * Additional {@link Event} attribute for node id.
	 * Type: Integer
	 */
	public static final Object EVENT_ATTR_NODE = new Object();
	
	/**
	 * Additional {@link Event} attribute for external pointer.
	 * Type: byte[]
	 */
	public static final Object EVENT_ATTR_ID = new Object();
	
	private List<DBNodeProxy> itsNodes = new ArrayList<DBNodeProxy>();
	private int itsCurrentNode = 0;
	
	private boolean itsFlushed = false;
	
	public void dispatchEvent(Event aEvent)
	{
		assert ! itsFlushed;
		
		DBNodeProxy theProxy = itsNodes.get(itsCurrentNode);
		aEvent.putAttribute(EVENT_ATTR_NODE, itsCurrentNode);
		theProxy.pushEvent(aEvent);
		
		BehaviorCallEvent theParent = aEvent.getParent();
		int theParentNode = (Integer) theParent.getAttribute(EVENT_ATTR_NODE);
		DBNodeProxy theParentProxy = itsNodes.get(theParentNode);
		theParentProxy.pushChildEvent(theParent, aEvent);
		
		itsCurrentNode = (itsCurrentNode+1) % itsNodes.size();
	}
	
	/**
	 * Flushes all buffers so that events are sent to the nodes 
	 * and stored.
	 */
	public void flush()
	{
		for (DBNodeProxy theProxy : itsNodes) theProxy.flush();
		itsFlushed = true;
	}
}
