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
import java.rmi.RemoteException;

import org.junit.Test;

import tod.impl.dbgrid.ConditionGenerator;
import tod.impl.dbgrid.EventGenerator;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.gridimpl.uniform.UniformEventDatabase;
import tod.impl.dbgrid.queries.EventCondition;

public class TestDatabaseNode
{
	@Test public void check() 
	{
		UniformEventDatabase theDatabase = new UniformEventDatabase(0, new File("test.bin"));
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
