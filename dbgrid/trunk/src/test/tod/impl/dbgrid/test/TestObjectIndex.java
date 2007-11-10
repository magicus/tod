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

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import tod.agent.DebugFlags;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.GridMaster;
import tod.utils.TODUtils;

/**
 * Test for a bug with filtering on split object conditions.
 * @author gpothier
 */
public class TestObjectIndex
{
	@Test public void test() throws IOException
	{
		GridMaster theMaster = Fixtures.setupLocalMaster();
		DebugFlags.MAX_EVENTS = 2819;
		Fixtures.replay(new File("src/test/test-objects.bin"), theMaster);
		theMaster.flush();
		
		GridLogBrowser theLogBrowser = GridLogBrowser.createLocal(theMaster);
		IBehaviorInfo theBehavior = theMaster.getStructureDatabase().getBehavior(71, true);
		
		IEventFilter theFilter = TODUtils.getLocationFilter(theLogBrowser, theBehavior, 48);
		
		IEventBrowser theBrowser = theLogBrowser.createBrowser(theFilter);
		for (int i=0;i<7;i++) theBrowser.next();
		
		IFieldWriteEvent theEvent = (IFieldWriteEvent) theBrowser.next();
		ObjectId theTarget = (ObjectId) theEvent.getTarget();
		
		IObjectInspector theInspector = theLogBrowser.createObjectInspector(theTarget);
		ITypeInfo theType = theInspector.getType();
		
		System.out.println(theType);
		
		Assert.assertTrue(theType.getId() > 0);
		
		System.out.println("Done.");
	}
	
}
