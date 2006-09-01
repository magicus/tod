/*
 * Created on Jul 21, 2006
 */
package tod.impl.dbgrid.dispatcher;

import java.util.ArrayList;
import java.util.List;

import tod.impl.common.event.BehaviorCallEvent;
import tod.impl.common.event.Event;
import tod.impl.dbgrid.ExternalPointer;
import tod.impl.dbgrid.GridEventCollector;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.messages.GridEvent;

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
	
	private GridMaster itsMaster;
	
	private List<DBNodeProxy> itsNodes = new ArrayList<DBNodeProxy>();
	private int itsCurrentNode = 0;
	
	private boolean itsFlushed = false;
	
	public EventDispatcher(GridMaster aMaster)
	{
		itsMaster = aMaster;
	}
	
	public void addNode(DBNodeProxy aProxy) 
	{
		itsNodes.add(aProxy);
	}

	public void dispatchEvent(Event aEvent)
	{
		assert ! itsFlushed;
		
		byte[] theId = makeExternalPointer(aEvent);
		aEvent.putAttribute(EventDispatcher.EVENT_ATTR_ID, theId);
		GridEvent theEvent = GridEvent.create(aEvent);

		// Choose a node and send the event
		DBNodeProxy theProxy = itsNodes.get(itsCurrentNode);
		aEvent.putAttribute(EVENT_ATTR_NODE, itsCurrentNode+1);
		theProxy.pushEvent(theEvent);
		
		itsCurrentNode = (itsCurrentNode+1) % itsNodes.size();
	}
	
	/**
	 * Directly disptaches a grid event
	 */
	public void dispatchEvent(GridEvent aEvent)
	{
		DBNodeProxy theProxy = itsNodes.get(itsCurrentNode);
		theProxy.pushEvent(aEvent);
		itsCurrentNode = (itsCurrentNode+1) % itsNodes.size();
	}
	
	/**
	 * Computes the external pointer that permits to identify the given event.
	 */
	private byte[] makeExternalPointer(Event aEvent)
	{
		return ExternalPointer.create(
				(Integer) aEvent.getAttribute(EventDispatcher.EVENT_ATTR_NODE), 
				aEvent.getHost().getId(), 
				GridEventCollector.getThreadNumber(aEvent), 
				aEvent.getTimestamp());
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
