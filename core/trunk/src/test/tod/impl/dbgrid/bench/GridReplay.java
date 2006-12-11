/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.impl.dbgrid.bench;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import tod.core.ILogCollector;
import tod.core.Output;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.Fixtures;
import tod.impl.dbgrid.GridMaster;
import tod.utils.StoreTODServer;

/**
 * Reads events stored on the disk by a {@link StoreTODServer}
 * and sends it to a grid master.
 * @author gpothier
 */
public class GridReplay
{
	public static void main(String[] args) throws Exception
	{
		Registry theRegistry = LocateRegistry.createRegistry(1099);
		
		String theFileName = DebuggerGridConfig.STORE_EVENTS_FILE;
		File theFile = new File(theFileName);
		
		GridMaster theMaster = Fixtures.setupMaster(theRegistry, args);
		
		ILogCollector theCollector = theMaster.createCollector(1);
//		ILogCollector theCollector = new DummyCollector();
		
		long t0 = System.currentTimeMillis();
		long theCount = Fixtures.replay(theFile, theMaster, theCollector);
		long t1 = System.currentTimeMillis();
		float dt = (t1-t0)/1000f;
		float theEpS = theCount/dt;
		System.out.println("Events: "+theCount+" time: "+dt+"s rate: "+theEpS+"ev/s");
//		System.exit(0);
	}
	
	private static class DummyCollector implements ILogCollector
	{

		public void behaviorExit(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
				int aOperationBytecodeIndex, int aBehaviorId, boolean aHasThrown, Object aResult)
		{
		}

		public void exception(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, String aMethodName,
				String aMethodSignature, String aMethodDeclaringClassSignature, int aOperationBytecodeIndex,
				Object aException)
		{
		}

		public void fieldWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
				int aOperationBytecodeIndex, int aFieldId, Object aTarget, Object aValue)
		{
		}

		public void arrayWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
				int aOperationBytecodeIndex, Object aTarget, int aIndex, Object aValue)
		{
		}

		public void instantiation(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
				int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
				Object aTarget, Object[] aArguments)
		{
		}

		public void localWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
				int aOperationBytecodeIndex, int aVariableId, Object aValue)
		{
		}

		public void methodCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
				int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
				Object aTarget, Object[] aArguments)
		{
		}

		public void output(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, Output aOutput,
				byte[] aData)
		{
		}

		public void superCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
				int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorid, int aExecutedBehaviorId,
				Object aTarget, Object[] aArguments)
		{
		}

		public void thread(int aThreadId, long aJVMThreadId, String aName)
		{
		}

		public void register(long aObjectUID, Object aObject)
		{
		}
		
	}

}
