/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid;

import java.util.Iterator;

import org.junit.Test;

import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.BehaviorCondition;
import tod.impl.dbgrid.queries.CompoundCondition;
import tod.impl.dbgrid.queries.Conjunction;
import tod.impl.dbgrid.queries.Disjunction;
import tod.impl.dbgrid.queries.EventCondition;
import tod.impl.dbgrid.queries.HostCondition;
import tod.impl.dbgrid.queries.ThreadCondition;

public class TestDatabaseNode
{
	@Test public void check()
	{
		DatabaseNode theNode = new DatabaseNode();
		EventGenerator theEventGenerator = createGenerator();
		
		System.out.println("filling...");
		Fixtures.fillNode(theNode, theEventGenerator, 100000);
		
		System.out.println("checking...");
		
		// Check with fixed condition
		CompoundCondition theCondition = new Disjunction();
//		theCondition.addCondition(new BehaviorCondition(5, (byte) 2));
		theCondition.addCondition(new BehaviorCondition(3, (byte) 0));
		
		checkCondition(
				theNode, 
				theCondition,
				createGenerator(),
				5000,
				10000);

		// Check with random conditions
		ConditionGenerator theConditionGenerator = new ConditionGenerator(0, createGenerator());
		theConditionGenerator.next();
		
		for (int i=0;i<100;i++)
		{
			System.out.println(i+1);
			EventCondition theEventCondition = theConditionGenerator.next();
			System.out.println(theEventCondition);
			
			checkCondition(
					theNode, 
					theEventCondition,
					createGenerator(),
					5000,
					10000);
		}
	}
	
	private EventGenerator createGenerator()
	{
		return new EventGenerator(0, 10, 10, 10, 10, 10, 10, 10);
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
		
		int theMatched = 0;
		long theTimestamp = theEvent != null ? theEvent.getTimestamp()+1 : 0;
		Iterator<GridEvent> theIterator = aNode.evaluate(aCondition, theTimestamp);
		for (int i=0;i<aCount;i++)
		{
			GridEvent theRefEvent = aReferenceGenerator.next();
			if (aCondition.match(theRefEvent))
			{
				GridEvent theTestedEvent = theIterator.next(); 
				Fixtures.assertEquals(theRefEvent, theTestedEvent);
				theMatched++;
//				System.out.println(i+"m");
			}
//			else System.out.println(i);
		}
		
		System.out.println("Matched: "+theMatched);
	}
}
