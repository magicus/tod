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
package tod.impl.evdbng.test;

import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Test;

import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.evdbng.EventGeneratorNG;
import tod.impl.evdbng.FixturesNG;
import tod.impl.evdbng.db.EventList;
import tod.impl.evdbng.messages.GridEventNG;

public class TestEventList
{
	@Test public void writeAndCheck()
	{
		fillCheck(1000000);
	}
	
	private void fillCheck(long aCount)
	{
		EventList theEventList = FixturesNG.createEventList();
		FixturesNG.fillEventList(theEventList, new EventGeneratorNG(null, 0), aCount);
		System.out.println("Filled event list: "+theEventList.getPageCount()+" pages.");
		checkEventList(theEventList, new EventGeneratorNG(null, 0), aCount);
	}
	
	private void checkEventList(
			EventList aEventList, 
			EventGeneratorNG aGenerator,
			long aCount)
	{
		checkEvents(aEventList.getEventIterator(), aGenerator, aCount, true);
	}
	
	private void checkEvents(
			Iterator<GridEventNG> aIterator, 
			EventGeneratorNG aGenerator,
			long aCount,
			boolean aExhaust)
	{
		for (long i=0;i<aCount;i++)
		{
			GridEventNG theRefEvent = aGenerator.next();
			
			if (! aIterator.hasNext()) fail("No more tuples");
			GridEventNG theEvent = aIterator.next();
			FixturesNG.assertEquals(""+i, theRefEvent, theEvent);
			
			if (i % 1000000 == 0) System.out.println("v: "+i);
		}
		
		if (aExhaust && aIterator.hasNext()) fail("Too many events");
	}
	
	@Test public void testRandomAccess()
	{
		int theCount = 10000;
		EventList theEventList = FixturesNG.createEventList();
		EventGeneratorNG theGenerator = new EventGeneratorNG(null, 0);
		int[] theIds = FixturesNG.fillEventListReport(theEventList, theGenerator, theCount);
		
		theGenerator = new EventGeneratorNG(null, 0);
		
		for(int i=0;i<theCount;i++)
		{
			GridEventNG theRefEvent = theGenerator.next();
			
			GridEventNG theEvent = theEventList.getEvent(theIds[i]);
			FixturesNG.assertEquals(""+i, theRefEvent, theEvent);
		}
	}
	
}
