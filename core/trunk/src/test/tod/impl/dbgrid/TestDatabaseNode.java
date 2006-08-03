/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid;

import java.util.Iterator;

import org.junit.Test;

import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;
import tod.impl.dbgrid.queries.ThreadCondition;

public class TestDatabaseNode
{
	@Test public void check()
	{
		DatabaseNode theNode = new DatabaseNode();
		EventGenerator theEventGenerator = new EventGenerator(0);
		
		System.out.println("filling...");
		Fixtures.fillNode(theNode, theEventGenerator, 10000);
		
		ConditionGenerator theConditionGenerator = new ConditionGenerator(0);

		System.out.println("checking...");
		for (int i=0;i<10;i++)
		{
			checkCondition(
					theNode, 
//					theConditionGenerator.next(),
					new ThreadCondition(5),
					new EventGenerator(0),
					0,
					1000);
		}
	}
	
	private void checkCondition(
			DatabaseNode aNode, 
			EventCondition aCondition, 
			EventGenerator aReferenceGenerator,
			int aSkip,
			int aCount)
	{
		GridEvent theEvent = null;
		for (int i=0;i<aSkip;i++)
		{
			theEvent = aReferenceGenerator.next();
		}
		
		long theTimestamp = theEvent != null ? theEvent.getTimestamp()+1 : 0;
		Iterator<GridEvent> theIterator = aNode.evaluate(aCondition, theTimestamp);
		for (int i=0;i<aCount;i++)
		{
			GridEvent theRefEvent = aReferenceGenerator.next();
			if (aCondition.match(theRefEvent))
			{
				GridEvent theTestedEvent = theIterator.next(); 
				Fixtures.assertEquals(theRefEvent, theTestedEvent);
			}
		}
	}
}
