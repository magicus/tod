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

import org.junit.Before;
import org.junit.Test;

import tod.agent.DebugFlags;
import tod.impl.dbgrid.ConditionGenerator;
import tod.impl.dbgrid.EventGenerator;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.db.EventDatabase;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.dbgrid.queries.BehaviorCondition;
import tod.impl.dbgrid.queries.CompoundCondition;
import tod.impl.dbgrid.queries.Disjunction;
import tod.impl.dbgrid.queries.EventCondition;
import tod.impl.dbgrid.queries.TypeCondition;

public class TestDatabaseNode
{
	private EventDatabase itsDatabase;

	@Before public void fill()
	{
		itsDatabase = new EventDatabase(0, new File("test.bin"));
		EventGenerator theEventGenerator = createGenerator();
		
		System.out.println("filling...");
		Fixtures.fillDatabase(itsDatabase, theEventGenerator, 100000);
	}
	
	@Test public void check() 
	{
		System.out.println("checking...");
		
		// Check with fixed condition
		CompoundCondition theCondition = new Disjunction();
//		theCondition.addCondition(new BehaviorCondition(3, (byte) 0));
		theCondition.addCondition(new TypeCondition(MessageType.FIELD_WRITE));
		
		Fixtures.checkCondition(
				itsDatabase, 
				theCondition,
				createGenerator(),
				0,
				1000);

		// Check with random conditions
		ConditionGenerator theConditionGenerator = new ConditionGenerator(0, createGenerator());
//		for (int i=0;i<591;i++) theConditionGenerator.next();
		
		for (int i=0;i<1000;i++)
		{
			System.out.println(i+1);
			EventCondition theEventCondition = theConditionGenerator.next();
			System.out.println(theEventCondition);
			
			int theCount = Fixtures.checkCondition(
					itsDatabase, 
					theEventCondition,
					createGenerator(),
					5000,
					10000);
			
			if (theCount > 3)
			{
				Fixtures.checkIteration(
						itsDatabase, 
						theEventCondition, 
						createGenerator(), 
						theCount);
			}
		}
	}
	
	private EventGenerator createGenerator()
	{
		return new EventGenerator(100, 100, 100, 100, 100, 100, 100, 100);
	}
	
}
