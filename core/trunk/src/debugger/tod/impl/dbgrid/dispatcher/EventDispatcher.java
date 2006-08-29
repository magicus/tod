/*
 * Created on Jul 21, 2006
 */
package tod.impl.dbgrid.dispatcher;

import java.util.ArrayList;
import java.util.List;

import tod.impl.common.event.BehaviorCallEvent;
import tod.impl.common.event.Event;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.dbnode.RIDatabaseNode;
import zz.utils.Utils;

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
	
	public void addNode(RIDatabaseNode aNode) 
	{
		DBNodeProxy theProxy = new DBNodeProxy(aNode, itsMaster);
		itsNodes.add(theProxy);
	}

	public void dispatchEvent(Event aEvent)
	{
		assert ! itsFlushed;
		
		// Choose a node and send the event
		DBNodeProxy theProxy = itsNodes.get(itsCurrentNode);
		aEvent.putAttribute(EVENT_ATTR_NODE, itsCurrentNode+1);
		theProxy.pushEvent(aEvent);
		
		// Send an add child message to the node that contains the parent
		BehaviorCallEvent theParent = aEvent.getParent();
		Object theAttribute = theParent != null ? theParent.getAttribute(EVENT_ATTR_NODE) : null;
		if (theAttribute != null)
		{
			int theParentNode = (Integer) theAttribute;
			DBNodeProxy theParentProxy = itsNodes.get(theParentNode-1);
			theParentProxy.pushChildEvent(theParent, aEvent);
		}
		
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
