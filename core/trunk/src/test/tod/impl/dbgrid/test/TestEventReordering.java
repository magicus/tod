/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.dbgrid.test;

import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_BEHAVIOR_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_BYTECODE_LOCS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_DEPTH_RANGE;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_FIELD_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_HOSTS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_OBJECT_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_THREADS_COUNT;
import static tod.impl.dbgrid.DebuggerGridConfig.STRUCTURE_VAR_COUNT;

import java.util.LinkedList;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

import tod.agent.DebugFlags;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.EventGenerator;
import tod.impl.dbgrid.db.EventReorderingBuffer;
import tod.impl.dbgrid.db.EventReorderingBuffer.ReorderingBufferListener;
import tod.impl.dbgrid.messages.GridEvent;

public class TestEventReordering implements ReorderingBufferListener 
{
	private long itsLastProcessedTimestamp;
	
	@Test public void test()
	{
		DebuggerGridConfig.DB_EVENT_BUFFER_SIZE = 50;
		
		EventReorderingBuffer theBuffer = new EventReorderingBuffer(this);
		EventGenerator theGenerator = new EventGenerator(
				0,
				2, 
				2,
				STRUCTURE_DEPTH_RANGE,
				STRUCTURE_BYTECODE_LOCS_COUNT,
				STRUCTURE_BEHAVIOR_COUNT,
				STRUCTURE_FIELD_COUNT,
				STRUCTURE_VAR_COUNT,
				STRUCTURE_OBJECT_COUNT);
		
		LinkedList<GridEvent> theOoOBuffer = new LinkedList<GridEvent>();
		
		Random theRandom = new Random(0);
		
		// Fill
		for (int i=0;i<10000000;i++)
		{
			GridEvent theEvent;
			float theFloat = theRandom.nextFloat();
			if (theFloat < 0.01) 
			{
				theEvent = theGenerator.next();
				theOoOBuffer.addLast(theEvent);
				continue;
			}
			
			if (theOoOBuffer.size() > 50)
			{
				theEvent = theOoOBuffer.removeFirst();
			}
			else
			{
				theEvent = theGenerator.next();
			}
			
			while (theBuffer.isFull()) processEvent(theBuffer.pop());
			theBuffer.push(theEvent);
			
			if (i % 100000 == 0)
			{
				System.out.println("i: "+i+" - "+theOoOBuffer.size());
			}
		}
		
		// Flush
		while (! theBuffer.isEmpty()) processEvent(theBuffer.pop());
	}

	private void processEvent(GridEvent aEvent)
	{
		long theTimestamp = aEvent.getTimestamp();
		if (theTimestamp < itsLastProcessedTimestamp)
		{
			eventDropped();
			return;
		}
		
		itsLastProcessedTimestamp = theTimestamp;
	}
	
	
	public void eventDropped()
	{
		Assert.fail("eventDropped");
	}
	
	
}
