/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.test;

import java.io.File;
import java.rmi.RemoteException;

import org.junit.Test;

import tod.impl.dbgrid.ConditionGenerator;
import tod.impl.dbgrid.EventGenerator;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.dbnode.EventDatabase;
import tod.impl.dbgrid.queries.EventCondition;

public class TestDatabaseNode
{
	@Test public void check() 
	{
		EventDatabase theDatabase = new EventDatabase(new File("test.bin"));
		EventGenerator theEventGenerator = createGenerator();
		
		System.out.println("filling...");
		Fixtures.fillDatabase(theDatabase, theEventGenerator, 1000000);
		
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
					theDatabase, 
					theEventCondition,
					createGenerator(),
					5000,
					10000);
		}
	}
	
	private EventGenerator createGenerator()
	{
		return new EventGenerator(0, 100, 100, 100, 100, 100, 100, 100, 100);
	}
	
}
