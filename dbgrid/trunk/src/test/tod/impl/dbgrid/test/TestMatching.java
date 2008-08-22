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
import tod.impl.database.structure.standard.BehaviorInfo;
import tod.impl.database.structure.standard.ClassInfo;
import tod.impl.database.structure.standard.StructureDatabase;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.GridMaster;

public class TestMatching
{
	/**
	 * Test what happens when an event matches a condition several times.
	 * Eg, match is on object id x and call event has arguments [x, x] 
	 */
	@Test
	public void testMultimatch()
	{
		GridMaster theMaster = Fixtures.setupLocalMaster();

		// Fill structure database
		StructureDatabase theStructureDatabase = (StructureDatabase) theMaster.getStructureDatabase();
		ClassInfo theClass = theStructureDatabase.addClass(1, "c");
		IMutableBehaviorInfo theBehavior = theClass.addBehavior(1, "b", "()V", false);

		// Fill event database
		ILogCollector theCollector = theMaster._getCollector();
		theCollector.thread(0, 0, "test");
		theCollector.methodCall(0, 0, (short) 0, 0, null, 0, false, 1, 1, null, new Object[] {new ObjectId(5), new ObjectId(5)});
		theCollector.flush();
		
		GridLogBrowser theLogBrowser = theMaster._getLocalLogBrowser();
//		IEventFilter theFilter = theLogBrowser.createObjectFilter(new ObjectId(5));
		IEventFilter theFilter = theLogBrowser.createBehaviorCallFilter(theBehavior);
		
		IEventBrowser theEventBrowser = theLogBrowser.createBrowser(theFilter);
		theEventBrowser.setNextTimestamp(0);
		
		ILogEvent theFirst = theEventBrowser.next();
		if (theEventBrowser.hasNext())
		{
			ILogEvent theSecond = theEventBrowser.next();
			Assert.fail("There should be only one event");
		}
	}
}
