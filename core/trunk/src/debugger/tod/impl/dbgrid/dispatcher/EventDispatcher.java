/*
 * Created on Jul 21, 2006
 */
package tod.impl.dbgrid.dispatcher;

import java.util.ArrayList;
import java.util.List;

import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.aggregator.Future;
import tod.impl.dbgrid.dbnode.RIDatabaseNode;
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

	/**
	 * Directly dispatches a grid event
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
	 * Flushes all buffers so that events are sent to the nodes 
	 * and stored.
	 */
	public void flush()
	{
		System.out.println("Event dispatcher: flushing...");
		
		// TODO: maybe use something else than Future...
		List<Future<Boolean>> theFutures = new ArrayList<Future<Boolean>>();
		for (DBNodeProxy theProxy : itsNodes)
		{
			final DBNodeProxy theProxy0 = theProxy;
			theFutures.add (new Future<Boolean>()
			{
				@Override
				protected Boolean fetch() throws Throwable
				{
					theProxy0.flush();
					return true;
				}
			});
		}
		
		for (Future<Boolean> theFuture : theFutures) theFuture.get();

		itsFlushed = true;
		System.out.println("Event dispatcher: flushed.");
	}
}
