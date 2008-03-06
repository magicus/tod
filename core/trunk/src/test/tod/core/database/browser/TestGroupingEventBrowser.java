/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.core.database.browser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

import tod.core.database.EventGenerator;
import tod.core.database.browser.GroupingEventBrowser.EventGroup;
import tod.core.database.browser.GroupingEventBrowser.IGroupDefinition;
import tod.core.database.event.ILogEvent;
import tod.impl.local.EventBrowser;
import zz.utils.ITask;


public class TestGroupingEventBrowser
{
	private static final int COUNT = 1000;
	
	private GroupingEventBrowser<Integer> createBrowser()
	{
		// Create event list
		List<ILogEvent> theEvents = new ArrayList<ILogEvent>();
		final Map<ILogEvent, Integer> theGroups = new HashMap<ILogEvent, Integer>();
		
		EventGenerator theGenerator = new EventGenerator(12);
		
		Random theRandom = new Random(12);
		for(int i=0;i<COUNT;i++)
		{
			ILogEvent theEvent = theGenerator.next();
			theEvents.add(theEvent);
			
			int theGroup = theRandom.nextInt(10);
			
			theGroups.put(theEvent, theGroup > 4 ? theGroup : null);
		}
		
		IEventBrowser theSourceBrowser = new EventBrowser(null, theEvents, null);
		
		IGroupDefinition<Integer> theGroupDefinition = new IGroupDefinition<Integer>()
		{
			public Integer getGroupKey(ILogEvent aEvent)
			{
				return theGroups.get(aEvent);
			}
		};
		
		return new GroupingEventBrowser<Integer>(theSourceBrowser, theGroupDefinition, false);
	}
	
	@Test public void testForward()
	{
		System.out.println("TestGroupingEventBrowser.testForward()");
		GroupingEventBrowser<Integer> theBrowser = createBrowser();
		testSequential(theBrowser, GroupingEventBrowser.Direction.FORWARD);
	}
	
	@Test public void testBackward()
	{
		System.out.println("TestGroupingEventBrowser.testBackward()");
		GroupingEventBrowser<Integer> theBrowser = createBrowser();
		theBrowser.setPreviousTimestamp(Long.MAX_VALUE);
		testSequential(theBrowser, GroupingEventBrowser.Direction.BACKWARD);
	}
	
	private void testSequential(
			GroupingEventBrowser<Integer> aBrowser, 
			GroupingEventBrowser.Direction aDirection)
	{
		IGroupDefinition<Integer> theGroupDefinition = aBrowser.getGroupDefinition();
		
		Integer theCurrentKey = null;
		int theTotalSize = 0;
		while(aDirection.hasMore(aBrowser))
		{
			ILogEvent theNext = aDirection.more(aBrowser);
			Integer theNextKey = theGroupDefinition.getGroupKey(theNext);
			
			if (theNext instanceof EventGroup)
			{
				EventGroup<Integer> theEventGroup = (EventGroup) theNext;
				
				Integer theInnerKey = null;
				int theSize = 0;
				for (ILogEvent theGroupEvent : theEventGroup.getEvents())
				{
					Integer theCurrentKey2 = theGroupDefinition.getGroupKey(theGroupEvent);
					Assert.assertTrue(theCurrentKey2 != null);
					
					if (theInnerKey == null) theInnerKey = theCurrentKey2;
					else Assert.assertTrue(theInnerKey.equals(theCurrentKey2));
					
					theSize++;
				}
				theNextKey = theInnerKey;
				
				theTotalSize += theSize;
				System.out.println("Found group of "+theSize+" events: "+theInnerKey);
			}
			else
			{
				Assert.assertTrue(theNextKey == null || ! theNextKey.equals(theCurrentKey));
				theTotalSize++;
			}
			
			theCurrentKey = theNextKey;
		}
		
		Assert.assertEquals(COUNT, theTotalSize);
	}
}
