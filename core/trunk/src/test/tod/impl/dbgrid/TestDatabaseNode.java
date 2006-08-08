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
//		CompoundCondition theCondition = new Disjunction();
//		theCondition.addCondition(new BehaviorCondition(3, (byte) 0));
//		
//		Fixtures.checkCondition(
//				theNode, 
//				theCondition,
//				createGenerator(),
//				5000,
//				10000);

		// Check with random conditions
		ConditionGenerator theConditionGenerator = new ConditionGenerator(0, createGenerator());
//		for (int i=0;i<449;i++) theConditionGenerator.next();
		
		for (int i=0;i<1000;i++)
		{
			System.out.println(i+1);
			EventCondition theEventCondition = theConditionGenerator.next();
			System.out.println(theEventCondition);
			
			Fixtures.checkCondition(
					theNode, 
					theEventCondition,
					createGenerator(),
					5000,
					10000);
		}
	}
	
	private EventGenerator createGenerator()
	{
		return new EventGenerator(0, 100, 100, 100, 100, 100, 100, 100);
	}
	
}
