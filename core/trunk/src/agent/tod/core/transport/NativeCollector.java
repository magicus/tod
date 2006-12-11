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

import tod.core.HighLevelCollector;
import tod.core.Output;
import tod.core.EventInterpreter.ThreadData;

public class NativeCollector extends HighLevelCollector<NativeCollector.NativeThreadData>
{
	
	public NativeCollector(String aHostname, int aPort)
	{
		init(aHostname, aPort);
	}
	
	@Override
	public NativeThreadData createThreadData(int aId)
	{
		allocThreadData(aId);
		return new NativeThreadData(aId);
	}
	
	private static native void init(String aHostname, int aPort);
	
	private static native void allocThreadData(int aId);

	private static native void behaviorExit(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aBehaviorId, boolean aHasThrown, Object aResult);

	private static  native void exception(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			String aMethodName, String aMethodSignature, String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex, Object aException);

	private static  native void fieldWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aFieldId, Object aTarget, Object aValue);

	private static  native void instantiation(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments);

	private static  native void localWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aVariableId, Object aValue);

	private static  native void methodCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments);

	private static  native void output(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			Output aOutput, byte[] aData);

	private static  native void superCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorid, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments);

	private static  native void thread(int aThreadId, long aJVMThreadId, String aName);

	
	
	
	@Override
	protected void behaviorExit(NativeThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aBehaviorId, boolean aHasThrown, Object aResult)
	{
		behaviorExit(aThread.getId(), aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aBehaviorId, aHasThrown, aResult);
	}

	@Override
	protected void exception(NativeThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			String aMethodName, String aMethodSignature, String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex, Object aException)
	{
		exception(aThread.getId(), aParentTimestamp, aDepth, aTimestamp, aMethodName, aMethodSignature, aMethodDeclaringClassSignature, aOperationBytecodeIndex, aException);
	}

	@Override
	protected void fieldWrite(NativeThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aFieldId, Object aTarget, Object aValue)
	{
		fieldWrite(aThread.getId(), aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aFieldId, aTarget, aValue);
	}
	
	
	@Override
	protected void arrayWrite(NativeThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, Object aTarget, int aIndex, Object aValue)
	{
		throw new UnsupportedOperationException();
	}



	@Override
	protected void instantiation(NativeThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments)
	{
		instantiation(aThread.getId(), aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
	}

	@Override
	protected void localWrite(NativeThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aVariableId, Object aValue)
	{
		localWrite(aThread.getId(), aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aVariableId, aValue);
	}

	@Override
	protected void methodCall(NativeThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments)
	{
		methodCall(aThread.getId(), aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
	}

	@Override
	protected void output(NativeThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			Output aOutput, byte[] aData)
	{
		output(aThread.getId(), aParentTimestamp, aDepth, aTimestamp, aOutput, aData);
	}

	@Override
	protected void superCall(NativeThreadData aThread, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorid, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments)
	{
		superCall(aThread.getId(), aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aDirectParent, aCalledBehaviorid, aExecutedBehaviorId, aTarget, aArguments);
	}

	@Override
	protected void thread(NativeThreadData aThread, long aJVMThreadId, String aName)
	{
		thread(aThread.getId(), aJVMThreadId, aName);
	}


	static class NativeThreadData extends ThreadData
	{
		public NativeThreadData(int aId)
		{
			super(aId);
		}
	}
}
