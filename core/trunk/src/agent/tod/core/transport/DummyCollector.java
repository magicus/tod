/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.core.transport;

import tod.agent.AgentConfig;
import tod.agent.AgentReady;
import tod.core.HighLevelCollector;
import tod.core.Output;
import tod.core.EventInterpreter.ThreadData;

/**
 * A collector that does nothing
 * @author gpothier
 */
public class DummyCollector extends HighLevelCollector<ThreadData>
{

	public DummyCollector()
	{
		AgentReady.READY = true;
	}

	@Override
	protected void behaviorExit(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aBehaviorId, boolean aHasThrown, Object aResult)
	{
	}

	@Override
	public ThreadData createThreadData(int aId)
	{
		return new ThreadData(aId);
	}

	@Override
	protected void exception(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			String aMethodName, String aMethodSignature, String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex, Object aException)
	{
	}

	@Override
	protected void fieldWrite(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aFieldId, Object aTarget, Object aValue)
	{
	}
	
	@Override
	protected void arrayWrite(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, Object aTarget, int aIndex, Object aValue)
	{
	}

	@Override
	protected void instantiation(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments)
	{
	}

	@Override
	protected void localWrite(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aVariableId, Object aValue)
	{
	}

	@Override
	protected void methodCall(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments)
	{
	}

	@Override
	protected void output(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp, Output aOutput,
			byte[] aData)
	{
	}

	@Override
	protected void superCall(ThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorid, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments)
	{
	}

	@Override
	protected void thread(ThreadData aThread, long aJVMThreadId, String aName)
	{
	}
}
