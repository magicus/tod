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
		
		// Choose node
		DBNodeProxy theProxy = itsNodes.get(itsCurrentNode);
		int theNodeId = itsCurrentNode+1;

		// Transform event
		GridEvent theEvent = GridEvent.create(aEvent);
		theEvent.setParentPointer(makeExternalPointer(aEvent, theNodeId));

		// Send event
//		theProxy.pushEvent(theEvent);
		
		// The following code is 5 times faster than using a modulo.
		// (Pentium M 2ghz)
		itsCurrentNode++;
		if (itsCurrentNode >= itsNodes.size()) itsCurrentNode = 0;
	}
	
	/**
	 * Directly disptaches a grid event
	 */
	public void dispatchEvent(GridEvent aEvent)
	{
		DBNodeProxy theProxy = itsNodes.get(itsCurrentNode);
		theProxy.pushEvent(aEvent);
		
		// The following code is 5 times faster than using a modulo.
		// (Pentium M 2ghz)
		itsCurrentNode++;
		if (itsCurrentNode >= itsNodes.size()) itsCurrentNode = 0;
	}
	
	/**
	 * Computes the external pointer that permits to identify the given event.
	 */
	private byte[] makeExternalPointer(Event aEvent, int aNode)
	{
		return ExternalPointer.create(
				aNode, 
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
