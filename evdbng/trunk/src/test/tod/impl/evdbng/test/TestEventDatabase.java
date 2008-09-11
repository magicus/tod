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
import tod.core.config.TODConfig;
import tod.impl.database.structure.standard.StructureDatabase;
import tod.impl.evdbng.ConditionGeneratorNG;
import tod.impl.evdbng.EventGeneratorNG;
import tod.impl.evdbng.FixturesNG;
import tod.impl.evdbng.db.EventDatabaseNG;
import tod.impl.evdbng.db.file.PagedFile;
import tod.impl.evdbng.queries.BehaviorCondition;
import tod.impl.evdbng.queries.CompoundCondition;
import tod.impl.evdbng.queries.Disjunction;
import tod.impl.evdbng.queries.EventCondition;

public class TestEventDatabase
{
	static
	{
		System.setProperty("page-buffer-size", "4m");
		System.setProperty("db-task-size", "10");
	}
	
	private EventDatabaseNG itsDatabase;
	private StructureDatabase itsStructureDatabase;

	@Before public void fill()
	{
		System.out.println("enabled: "+AgentReady.CAPTURE_ENABLED);
//		TOD.disableCapture();
		itsStructureDatabase = StructureDatabase.create(new TODConfig());
		itsDatabase = new EventDatabaseNG(
				itsStructureDatabase,
				0, 
				PagedFile.create(new File("indexes.bin")),
				PagedFile.create(new File("events.bin")));
		
		EventGeneratorNG theEventGenerator = createGenerator();
		theEventGenerator.fillStructureDatabase(itsStructureDatabase);
		
		theEventGenerator = createGenerator();
		
		System.out.println("filling...");
		FixturesNG.fillDatabase(itsDatabase, theEventGenerator, 2000);
	}
	
	@Test public void check() 
	{
		System.out.println("checking...");
		
		// Check with fixed condition
//		CompoundCondition theCondition = new Disjunction();
//		theCondition.addCondition(new BehaviorCondition(3, (byte) 0));
//		
//		FixturesNG.checkCondition(
//				itsDatabase, 
//				theCondition,
//				createGenerator(),
//				0,
//				1000);

		// Check with random conditions
		ConditionGeneratorNG theConditionGenerator = new ConditionGeneratorNG(0, createGenerator());
		
		for (int i=0;i<1000;i++)
		{
			System.out.println(i+1);
			EventCondition theEventCondition = theConditionGenerator.next();
			if (i<2) continue;
			System.out.println(theEventCondition);
			
			int theCount = FixturesNG.checkCondition(
					itsDatabase, 
					theEventCondition,
					createGenerator(),
					500,
					1000);
			
			if (theCount > 3)
			{
//				TOD.enableCapture();
				FixturesNG.checkIteration(
						itsDatabase, 
						theEventCondition, 
						createGenerator(), 
						theCount);
//				TOD.disableCapture();
			}
		}
	}
	
	private EventGeneratorNG createGenerator()
	{
		return new EventGeneratorNG(itsStructureDatabase, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100);
	}
	
}
