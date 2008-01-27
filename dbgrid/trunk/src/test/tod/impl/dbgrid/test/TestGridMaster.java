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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IMutableClassInfo;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.impl.database.structure.standard.HostInfo;
import tod.impl.database.structure.standard.PrimitiveTypeInfo;
import tod.impl.database.structure.standard.ThreadInfo;
import tod.impl.dbgrid.ConditionGenerator;
import tod.impl.dbgrid.EventGenerator;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.aggregator.GridEventBrowser;
import tod.impl.dbgrid.aggregator.RIQueryAggregator;
import tod.impl.dbgrid.messages.BitGridEvent;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;

public class TestGridMaster
{
	@Test public void test() throws RemoteException
	{
		GridMaster theMaster = Fixtures.setupLocalMaster();
		IMutableStructureDatabase theStructureDatabase = theMaster.getStructureDatabase();
//		theStructureDatabase.clear();
		
		for (int i=1;i<=100;i++) 
		{
			HostInfo theHostInfo = new HostInfo(i, ""+i);
			theMaster.registerHost(theHostInfo);
			
			for (int j=1;j<=100;j++)
			{
				theMaster.registerThread(new ThreadInfo(theHostInfo, j, j, ""+j));
			}
			
			IMutableClassInfo theClass = theStructureDatabase.getNewClass("C"+i);
			theClass.getNewBehavior("m"+i, "()V");
			theClass.getNewField("f"+i, PrimitiveTypeInfo.BOOLEAN);
		}
		GridLogBrowser theLogBrowser = GridLogBrowser.createLocal(null, theMaster);

		EventGenerator theEventGenerator = createGenerator();
		
		System.out.println("filling...");
		Fixtures.fillDatabase(theMaster, theEventGenerator, 1000000);
		
		System.out.println("checking...");
		ConditionGenerator theConditionGenerator = new ConditionGenerator(0, createGenerator());
		for (int i=0;i<2;i++) theConditionGenerator.next();
		
		for (int i=0;i<1000;i++)
		{
			System.out.println(i+1);
			EventCondition theEventCondition = theConditionGenerator.next();
			System.out.println(theEventCondition);
			
			int theCount = checkCondition(theMaster, theEventCondition, createGenerator(), 5000, 10000);
			
			GridEventBrowser theEventBrowser = new GridEventBrowser(theLogBrowser, theEventCondition);
			int theCount2 = checkCondition(theEventBrowser, theEventCondition, createGenerator(), 5000, 10000);
			
			Assert.assertTrue("Bad count", theCount == theCount2);
			
			if (theCount > 3)
			{
				checkIteration(
						theLogBrowser,
						theEventCondition, 
						createGenerator(), 
						theCount);
			}

		}
		
	}
	
	private int checkCondition(
			GridMaster aMaster, 
			EventCondition aCondition, 
			EventGenerator aReferenceGenerator,
			int aSkip,
			int aCount) throws RemoteException
	{
		GridEvent theEvent = null;
		for (int i=0;i<aSkip;i++) theEvent = aReferenceGenerator.next();
		
		long theTimestamp = theEvent != null ? theEvent.getTimestamp()+1 : 0;
		
		RIQueryAggregator theAggregator = aMaster.createAggregator(aCondition);
		theAggregator.setNextTimestamp(theTimestamp);
		
		int theMatched = 0;
		for (int i=0;i<aCount;i++)
		{
			BitGridEvent theRefEvent = aReferenceGenerator.next();
			if (aCondition._match(theRefEvent))
			{
				GridEvent[] theBuffer = theAggregator.next(1);
				BitGridEvent theTestedEvent = (BitGridEvent) theBuffer[0]; 
				Fixtures.assertEquals(""+i, theRefEvent, theTestedEvent);
				theMatched++;
			}
		}
		
		System.out.println("Matched: "+theMatched);
		return theMatched;
	}
	
	private int checkCondition(
			GridEventBrowser aBrowser, 
			EventCondition aCondition, 
			EventGenerator aReferenceGenerator,
			int aSkip,
			int aCount) 
	{
		GridEvent theEvent = null;
		for (int i=0;i<aSkip;i++) theEvent = aReferenceGenerator.next();
		
		long theTimestamp = theEvent != null ? theEvent.getTimestamp()+1 : 0;
		
		aBrowser.setNextTimestamp(theTimestamp);
		
		int theMatched = 0;
		for (int i=0;i<aCount;i++)
		{
			GridEvent theRefEvent = aReferenceGenerator.next();
			ILogEvent theLogEvent = theRefEvent.toLogEvent(aBrowser.getLogBrowser());
			
			if (aCondition._match(theRefEvent))
			{
				ILogEvent theTestedEvent = aBrowser.next();
				Fixtures.assertEquals(""+i, theLogEvent, theTestedEvent);
				theMatched++;
			}
		}
		
		System.out.println("Matched: "+theMatched);
		return theMatched;
	}
	
	public static void checkIteration(
			ILogBrowser aBrowser,
			EventCondition aCondition, 
			EventGenerator aReferenceGenerator,
			int aCount)
	{
		List<ILogEvent> theEvents = new ArrayList<ILogEvent>(aCount);

		IEventBrowser theEventBrowser = aBrowser.createBrowser(aCondition);
		theEventBrowser.setNextTimestamp(0);
		while (theEvents.size() < aCount)
		{
			GridEvent theRefEvent = aReferenceGenerator.next();
			if (aCondition._match(theRefEvent)) theEvents.add(theEventBrowser.next());
		}
		
		ILogEvent theFirstEvent = theEvents.get(0);
		theEventBrowser = aBrowser.createBrowser(aCondition);
		theEventBrowser.setNextTimestamp(theFirstEvent.getTimestamp());
		Fixtures.assertEquals("first.a", theFirstEvent, theEventBrowser.next());
		Fixtures.assertEquals("first.b", theFirstEvent, theEventBrowser.previous());
		
		ILogEvent theSecondEvent = theEvents.get(1);
		theEventBrowser = aBrowser.createBrowser(aCondition);
		theEventBrowser.setNextTimestamp(theFirstEvent.getTimestamp()+1);
		Fixtures.assertEquals("sec.a", theSecondEvent, theEventBrowser.next());
		Fixtures.assertEquals("sec.b", theSecondEvent, theEventBrowser.previous());
		
		theEventBrowser = aBrowser.createBrowser(aCondition);
		theEventBrowser.setNextTimestamp(0);
		
		int theIndex = 0;
		int theDelta = aCount;
		boolean theForward = true;
		while(theDelta > 1)
		{
			for (int i=0;i<theDelta;i++)
			{
				ILogEvent theRefEvent;
				ILogEvent theTestEvent;
				if (theForward)
				{
					theRefEvent = theEvents.get(theIndex);
					theTestEvent = theEventBrowser.next();
					theIndex++;
				}
				else
				{
					theTestEvent = theEventBrowser.previous();
					theIndex--;
					theRefEvent = theEvents.get(theIndex);
				}
				
				Fixtures.assertEquals("index: "+theIndex, theRefEvent, theTestEvent);
			}
			
			theDelta /= 2;
			theForward = ! theForward;
		}
	}


	
	private EventGenerator createGenerator()
	{
		return new EventGenerator(100, 100, 100, 100, 100, 100, 100, 100, 100);
	}
	
}
