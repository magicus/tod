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

import junit.framework.Assert;

import org.junit.Test;

import tod.core.ILogCollector;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.ObjectId;
import tod.impl.database.structure.standard.ClassInfo;
import tod.impl.database.structure.standard.StructureDatabase;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.GridMaster;

public class TestMatching
{
	static final GridMaster MASTER = Fixtures.setupLocalMaster(); 
	static ClassInfo CLASS;
	static IMutableBehaviorInfo BEHAVIOR1;
	static IMutableBehaviorInfo BEHAVIOR2;
	
	static
	{
		StructureDatabase theStructureDatabase = (StructureDatabase) MASTER.getStructureDatabase();
		CLASS = theStructureDatabase.addClass(1, "c");
		BEHAVIOR1 = CLASS.addBehavior(1, "b1", "()V", false);
		BEHAVIOR2 = CLASS.addBehavior(2, "b2", "()V", false);
	}
	
	/**
	 * Test what happens when an event matches a condition several times.
	 * Eg, match is on object id x and call event has arguments [x, x] 
	 */
	@Test
	public void testMultimatch()
	{
		MASTER.clear();
		
		// Fill event database
		ILogCollector theCollector = MASTER._getCollector();
		theCollector.thread(0, 0, "test");
		theCollector.methodCall(0, 0, (short) 0, 0, null, 0, false, 1, 1, null, new Object[] {new ObjectId(5), new ObjectId(5)});
		theCollector.flush();
		
		GridLogBrowser theLogBrowser = MASTER._getLocalLogBrowser();
//		IEventFilter theFilter = theLogBrowser.createObjectFilter(new ObjectId(5));
		IEventFilter theFilter = theLogBrowser.createBehaviorCallFilter(BEHAVIOR1);
		
		IEventBrowser theEventBrowser = theLogBrowser.createBrowser(theFilter);
		theEventBrowser.setNextTimestamp(0);
		
		ILogEvent theFirst = theEventBrowser.next();
		if (theEventBrowser.hasNext())
		{
			ILogEvent theSecond = theEventBrowser.next();
			Assert.fail("There should be only one event");
		}
	}
	
	/**
	 * Same as {@link #testMultimatch()}, but tests with the iterator going back and forth.
	 */
	@Test
	public void testMultimatch2()
	{
		MASTER.clear();

		// Fill event database
		ILogCollector theCollector = MASTER._getCollector();
		theCollector.thread(0, 0, "test");
		theCollector.methodCall(0, 0, (short) 0, 1, null, 0, false, 0, 0, null, new Object[] {new ObjectId(5), new ObjectId(5)});
		theCollector.methodCall(0, 0, (short) 0, 2, null, 0, false, 1, 1, null, new Object[] {});
		theCollector.methodCall(0, 0, (short) 0, 3, null, 0, false, 0, 0, null, new Object[] {new ObjectId(5), new ObjectId(5)});
		theCollector.methodCall(0, 0, (short) 0, 4, null, 0, false, 0, 0, null, new Object[] {new ObjectId(5), new ObjectId(5)});
		theCollector.methodCall(0, 0, (short) 0, 5, null, 0, false, 1, 1, null, new Object[] {});
		theCollector.methodCall(0, 0, (short) 0, 6, null, 0, false, 1, 1, null, new Object[] {new ObjectId(5), new ObjectId(5)});
		theCollector.flush();
		
		GridLogBrowser theLogBrowser = MASTER._getLocalLogBrowser();
		IEventFilter theFilter = theLogBrowser.createUnionFilter(
				theLogBrowser.createObjectFilter(new ObjectId(5)),
				theLogBrowser.createBehaviorCallFilter(BEHAVIOR1));
		
		IEventBrowser theEventBrowser = theLogBrowser.createBrowser(theFilter);
		theEventBrowser.setNextTimestamp(0);
		
		ILogEvent[] theEvents = {
				theEventBrowser.next(),
				theEventBrowser.next(),
				theEventBrowser.next(),
				theEventBrowser.next(),
				theEventBrowser.previous(),
				theEventBrowser.previous(),
				theEventBrowser.next(),
				theEventBrowser.previous(),
		};
		
		long[] theExpectedTimesamps = {1, 2, 3, 4, 4, 3, 3, 3};
		
		Assert.assertEquals(theEvents.length, theExpectedTimesamps.length);
		
		for(int i=0;i<theEvents.length;i++)
		{
			Assert.assertEquals(theExpectedTimesamps[i], theEvents[i].getTimestamp());
		}
		
	}
}
