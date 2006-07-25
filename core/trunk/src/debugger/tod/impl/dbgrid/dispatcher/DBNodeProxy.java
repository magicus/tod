/*
 * Created on Jul 21, 2006
 */
package tod.impl.dbgrid.dispatcher;

import java.util.ArrayList;
import java.util.List;

import tod.impl.common.event.Event;
import tod.impl.dbgrid.ExternalPointer;
import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.messages.AddChildEvent;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridMessage;

/**
 * A proxy for database nodes. It collects messages in a 
 * buffer and sends them to the actual {@link DatabaseNode}
 * when there are enough, or after a certain time.
 * @author gpothier
 */
public class DBNodeProxy
{
	private static final int TRANSMIT_DELAY_MS = 1000;
	
	private DatabaseNode itsDatabaseNode;
	private MessageQueue itsMessageQueue = new MessageQueue();
	
	public DBNodeProxy(DatabaseNode aDatabaseNode)
	{
		itsDatabaseNode = aDatabaseNode;
	}

	/**
	 * Pushes an event so that it will be stored by the node behind this proxy
	 */
	public void pushEvent(Event aEvent)
	{
		byte[] theId = makeExternalPointer(aEvent);
		aEvent.putAttribute(EventDispatcher.EVENT_ATTR_ID, theId);
		
		itsMessageQueue.pushMessage(GridEvent.create(aEvent));
	}

	/**
	 * Indicates that a parent event stored by this proxy's node has a new child,
	 * possibly stored by another node. 
	 */
	public void pushChildEvent(Event aParentEvent, Event aChildEvent)
	{
		byte[] theParentId = (byte[]) aParentEvent.getAttribute(EventDispatcher.EVENT_ATTR_ID);
		byte[] theChildId = (byte[]) aChildEvent.getAttribute(EventDispatcher.EVENT_ATTR_ID);
		
		itsMessageQueue.pushMessage(new AddChildEvent(theParentId, theChildId));
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
	
	private class MessageQueue extends Thread
	{
		private List<GridMessage> itsQueuedEvents = new ArrayList<GridMessage>();
		
		public MessageQueue()
		{
			start();
		}

		public void send()
		{
			List<GridMessage> theEvents;
			
			synchronized (this)
			{
				theEvents = itsQueuedEvents;
				itsQueuedEvents = new ArrayList<GridMessage>();
			}
			
			itsDatabaseNode.push(theEvents);
		}
		
		public synchronized void pushMessage(GridMessage aMessage)
		{
			itsQueuedEvents.add(aMessage);
		}
		
		@Override
		public void run()
		{
			try
			{
				while(true)
				{
					long t0 = System.currentTimeMillis();
					itsMessageQueue.send();
					long t1 = System.currentTimeMillis();
					
					long dt = t1-t0;
					if (dt < TRANSMIT_DELAY_MS) sleep(TRANSMIT_DELAY_MS - dt);
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}

	}
}
