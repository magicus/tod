/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.test;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import tod.agent.AgentReady;
import tod.agent.TOD;
import tod.core.config.TODConfig;
import tod.impl.database.structure.standard.StructureDatabase;
import tod.impl.evdbng.ConditionGenerator;
import tod.impl.evdbng.EventGenerator;
import tod.impl.evdbng.Fixtures;
import tod.impl.evdbng.db.EventDatabase;
import tod.impl.evdbng.db.file.PagedFile;
import tod.impl.evdbng.queries.BehaviorCondition;
import tod.impl.evdbng.queries.CompoundCondition;
import tod.impl.evdbng.queries.Disjunction;
import tod.impl.evdbng.queries.EventCondition;

public class TestEventDatabase
{
	private EventDatabase itsDatabase;
	private StructureDatabase itsStructureDatabase;

	@Before public void fill()
	{
		System.out.println("enabled: "+AgentReady.CAPTURE_ENABLED);
//		TOD.disableCapture();
		itsStructureDatabase = StructureDatabase.create(new TODConfig());
		itsDatabase = new EventDatabase(
				itsStructureDatabase,
				0, 
				PagedFile.create(new File("indexes.bin")),
				PagedFile.create(new File("events.bin")));
		
		EventGenerator theEventGenerator = createGenerator();
		
		System.out.println("filling...");
		Fixtures.fillDatabase(itsDatabase, theEventGenerator, 12000);
	}
	
	@Test public void check() 
	{
		System.out.println("checking...");
		
		// Check with fixed condition
		CompoundCondition theCondition = new Disjunction();
		theCondition.addCondition(new BehaviorCondition(3, (byte) 0));
		
		Fixtures.checkCondition(
				itsDatabase, 
				theCondition,
				createGenerator(),
				0,
				1000);

		// Check with random conditions
		ConditionGenerator theConditionGenerator = new ConditionGenerator(0, createGenerator());
		
		for (int i=0;i<1000;i++)
		{
			System.out.println(i+1);
			EventCondition theEventCondition = theConditionGenerator.next();
			System.out.println(theEventCondition);
			
			int theCount = Fixtures.checkCondition(
					itsDatabase, 
					theEventCondition,
					createGenerator(),
					500,
					10000);
			
			if (theCount > 3)
			{
//				TOD.enableCapture();
				Fixtures.checkIteration(
						itsDatabase, 
						theEventCondition, 
						createGenerator(), 
						theCount);
//				TOD.disableCapture();
			}
		}
	}
	
	private EventGenerator createGenerator()
	{
		return new EventGenerator(itsStructureDatabase, 100, 100, 100, 100, 100, 100, 100, 100, 100);
	}
	
}
