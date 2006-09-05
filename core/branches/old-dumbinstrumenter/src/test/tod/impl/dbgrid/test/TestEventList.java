/*
 * Created on Jul 26, 2006
 */
package tod.impl.dbgrid.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Test;

import tod.impl.dbgrid.EventGenerator;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.dbnode.EventList;
import tod.impl.dbgrid.messages.GridEvent;
import zz.utils.bit.IntBitStruct;

public class TestEventList
{
	@Test public void writeAndCheck()
	{
		fillCheck(1000000);
	}
	
	private void fillCheck(long aCount)
	{
		EventList theEventList = Fixtures.createEventList();
		Fixtures.fillEventList(theEventList, new EventGenerator(0), aCount);
		checkEventList(theEventList, new EventGenerator(0), aCount);
	}
	
	private void checkEventList(
			EventList aEventList, 
			EventGenerator aGenerator,
			long aCount)
	{
		checkEvents(aEventList.getEventIterator(), aGenerator, aCount, true);
	}
	
	private void checkEvents(
			Iterator<GridEvent> aIterator, 
			EventGenerator aGenerator,
			long aCount,
			boolean aExhaust)
	{
		for (long i=0;i<aCount;i++)
		{
			GridEvent theRefEvent = aGenerator.next();
			
			if (! aIterator.hasNext()) fail("No more tuples");
			GridEvent theEvent = aIterator.next();
			Fixtures.assertEquals(theRefEvent, theEvent);
			
			if (i % 1000000 == 0) System.out.println("v: "+i);
		}
		
		if (aExhaust && aIterator.hasNext()) fail("Too many events");
	}
	
	@Test public void testRandomAccess()
	{
		int theCount = 10000;
		EventList theEventList = Fixtures.createEventList();
		EventGenerator theGenerator = new EventGenerator(0);
		long[] theIds = Fixtures.fillEventListReport(theEventList, theGenerator, theCount);
		
		theGenerator = new EventGenerator(0);
		
		for(int i=0;i<theCount;i++)
		{
			GridEvent theRefEvent = theGenerator.next();
			
			GridEvent theEvent = theEventList.getEvent(theIds[i]);
			Fixtures.assertEquals(theRefEvent, theEvent);
		}
	}
	
}
