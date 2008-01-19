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
package tod.impl.local;

import tod.agent.Output;
import tod.core.DebugFlags;
import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.impl.common.EventCollector;
import tod.impl.common.event.ArrayWriteEvent;
import tod.impl.common.event.BehaviorExitEvent;
import tod.impl.common.event.Event;
import tod.impl.common.event.ExceptionGeneratedEvent;
import tod.impl.common.event.FieldWriteEvent;
import tod.impl.common.event.LocalVariableWriteEvent;
import tod.impl.common.event.OutputEvent;
import tod.impl.database.structure.standard.ThreadInfo;
import tod.impl.local.event.BehaviorCallEvent;
import tod.impl.local.event.ConstructorChainingEvent;
import tod.impl.local.event.InstantiationEvent;
import tod.impl.local.event.MethodCallEvent;
import zz.utils.ArrayStack;
import zz.utils.Stack;


/**
 * This log collector stores all the events it receives,
 * and provides an API for accessing the recorded information
 * in a convenient way.
 * @author gpothier
 */
public class LocalCollector extends EventCollector
{
	private final LocalBrowser itsBrowser;
	
	public LocalCollector(LocalBrowser aBrowser, IHostInfo aHost)
	{
		super(aHost, aBrowser.getStructureDatabase());
		itsBrowser = aBrowser;
		itsBrowser.addHost(aHost);
	}

	private void addEvent(Event aEvent)
	{
		if (DebugFlags.LOCAL_COLLECTOR_STORE) itsBrowser.addEvent(aEvent);
	}
	
	private IBehaviorInfo getBehavior(int aId)
	{
		return itsBrowser.getStructureDatabase().getBehavior(aId, true);
	}
	
	private IFieldInfo getField(int aId)
	{
		return itsBrowser.getStructureDatabase().getField(aId, true);
	}
	
	private ITypeInfo getType(int aId)
	{
		return itsBrowser.getStructureDatabase().getType(aId, true);
	}
	
	@Override
	protected ThreadInfo createThreadInfo(IHostInfo aHost, int aId, long aJVMId, String aName)
	{
		return new LocalThreadInfo(aHost, aId, aJVMId, aName);
	}
	
	@Override
	public LocalThreadInfo getThread(int aId)
	{
		return (LocalThreadInfo) super.getThread(aId);
	}
	
	private void initEvent(
			Event aEvent, 
			LocalThreadInfo aThread, 
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp, 
			int aOperationBehaviorId,
			int aOperationBytecodeIndex)
	{
		BehaviorCallEvent theParentEvent = aThread.peekParent();
//		assert theParentEvent == null || theParentEvent.getTimestamp() == aParentTimestamp;
		
		aEvent.setThread(aThread);
		aEvent.setDepth(aDepth);
		
		aEvent.setParentTimestamp(theParentEvent != null ? 
				theParentEvent.getTimestamp() 
				: 0);
		
		aEvent.setTimestamp(aTimestamp);
		
		aEvent.setOperationBehavior(aOperationBehaviorId != -1 ? 
				getBehavior(aOperationBehaviorId) 
				: null);
		
		aEvent.setOperationBytecodeIndex(aOperationBytecodeIndex);
		
		if (DebugFlags.LOCAL_COLLECTOR_STORE && theParentEvent != null) 
		{
			theParentEvent.addChild(aEvent);
		}
	}

	private void initEvent(
			Event aEvent, 
			LocalThreadInfo aThread, 
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp, 
			int aProbeId)
	{
		ProbeInfo theProbeInfo = null;
		
		int theBehaviorId = -1;
		int theBytecodeIndex = -1;
		if (theProbeInfo != null)
		{
			theBehaviorId = theProbeInfo.behaviorId;
			theBytecodeIndex = theProbeInfo.bytecodeIndex;
		}
		
		initEvent(aEvent, aThread, aParentTimestamp, aDepth, aTimestamp, theBehaviorId, theBytecodeIndex);
	}
	
	@Override
	protected void exception(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp, 
			int aBehaviorId,
			int aOperationBytecodeIndex, 
			Object aException)
	{
		ExceptionGeneratedEvent theEvent = new ExceptionGeneratedEvent(itsBrowser);
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aBehaviorId, aOperationBytecodeIndex);
		
		theEvent.setException(aException);
		
		addEvent(theEvent);
	}

	public void behaviorExit(
			int aThreadId,
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp,
			int aProbeId, 
			int aBehaviorId,
			boolean aHasThrown, 
			Object aResult)
	{
		BehaviorExitEvent theEvent = new BehaviorExitEvent(itsBrowser);
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aProbeId);
		
		theEvent.setHasThrown(aHasThrown);
		theEvent.setResult(aResult);
		
		addEvent(theEvent);
		
		theThread.popParent();
	}

	public void fieldWrite(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp,
			int aProbeId,
			int aFieldId,
			Object aTarget, 
			Object aValue)
	{
		FieldWriteEvent theEvent = new FieldWriteEvent(itsBrowser);
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aProbeId);

		theEvent.setField(getField(aFieldId));
		theEvent.setTarget(aTarget);
		theEvent.setValue(aValue);
		
		addEvent(theEvent);
	}
	
	public void newArray(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp, 
			int aProbeId,
			Object aTarget, 
			int aBaseTypeId,
			int aSize)
	{
		InstantiationEvent theEvent = new InstantiationEvent(itsBrowser);
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aProbeId);

		theEvent.setTarget(aTarget);
		ITypeInfo theBaseType = getType(aBaseTypeId);
		IArrayTypeInfo theType = itsBrowser.getStructureDatabase().getArrayType(theBaseType, 1);
		theEvent.setType(theType);
		
		theEvent.setArguments(new Object[] { aSize });
		
		addEvent(theEvent);
	}

	public void arrayWrite(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			int aProbeId,
			Object aTarget, 
			int aIndex, 
			Object aValue)
	{
		ArrayWriteEvent theEvent = new ArrayWriteEvent(itsBrowser);
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aProbeId);

		theEvent.setTarget(aTarget);
		theEvent.setIndex(aIndex);
		theEvent.setValue(aValue);
		
		addEvent(theEvent);
	}

	public void instantiation(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			int aProbeId, 
			boolean aDirectParent, 
			int aCalledBehaviorId, 
			int aExecutedBehaviorId,
			Object aTarget, 
			Object[] aArguments)
	{
		InstantiationEvent theEvent = new InstantiationEvent(itsBrowser);
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aProbeId);

		theEvent.setDirectParent(aDirectParent);
		theEvent.setCalledBehavior(getBehavior(aCalledBehaviorId));
		theEvent.setExecutedBehavior(getBehavior(aExecutedBehaviorId));
		theEvent.setTarget(aTarget);
		theEvent.setArguments(aArguments);
		
		addEvent(theEvent);
		
		theThread.pushParent(theEvent);
	}

	public void localWrite(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			int aProbeId,
			int aVariableId, 
			Object aValue)
	{
		LocalVariableWriteEvent theEvent = new LocalVariableWriteEvent(itsBrowser);
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aProbeId);

		IBehaviorInfo theBehavior = theThread.peekParent().getExecutedBehavior();
		LocalVariableInfo theInfo = theBehavior.getLocalVariableInfo(theEvent.getOperationBytecodeIndex(), aVariableId); 
       	if (theInfo == null) theInfo = new LocalVariableInfo((short)-1, (short)-1, "$"+aVariableId, "", (short)-1);
        
		theEvent.setVariable(theInfo);
		
		theEvent.setValue(aValue);
		
		addEvent(theEvent);
	}

	public void methodCall(
			int aThreadId,
			long aParentTimestamp,
			short aDepth, 
			long aTimestamp,
			int aProbeId, 
			boolean aDirectParent, 
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments)
	{
		MethodCallEvent theEvent = new MethodCallEvent(itsBrowser);
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aProbeId);

		theEvent.setDirectParent(aDirectParent);
		theEvent.setCalledBehavior(getBehavior(aCalledBehaviorId));
		theEvent.setExecutedBehavior(getBehavior(aExecutedBehaviorId));
		theEvent.setTarget(aTarget);
		theEvent.setArguments(aArguments);
		
		addEvent(theEvent);
		theThread.pushParent(theEvent);
	}

	public void output(
			int aThreadId,
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			Output aOutput,
			byte[] aData)
	{
		OutputEvent theEvent = new OutputEvent(itsBrowser);
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, -1, -1);
		
		theEvent.setOutput(aOutput);
		theEvent.setData(null); //TODO: fix
		
		addEvent(theEvent);
	}

	public void superCall(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aProbeId,
			boolean aDirectParent, 
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget, 
			Object[] aArguments)
	{
		ConstructorChainingEvent theEvent = new ConstructorChainingEvent(itsBrowser);
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aProbeId);

		theEvent.setDirectParent(aDirectParent);
		theEvent.setCalledBehavior(getBehavior(aCalledBehaviorId));
		theEvent.setExecutedBehavior(getBehavior(aExecutedBehaviorId));
		theEvent.setTarget(aTarget);
		theEvent.setArguments(aArguments);
		
		addEvent(theEvent);
		theThread.pushParent(theEvent);
	}
	
	@Override
	protected void thread(ThreadInfo aThread)
	{
		itsBrowser.addThread(aThread);
	}
	
	public void register(long aObjectUID, Object aObject, long aTimestamp)
	{
		//timestamp is not necessary in local version
		itsBrowser.register(aObjectUID, aObject);
	}
	
	private static class LocalThreadInfo extends ThreadInfo
	{
		private Stack<BehaviorCallEvent> itsParentsStack = new ArrayStack<BehaviorCallEvent>();

		public LocalThreadInfo(IHostInfo aHost, int aId, long aJVMId, String aName)
		{
			super(aHost, aId, aJVMId, aName);
		}
		
		public void pushParent(BehaviorCallEvent aEvent)
		{
			itsParentsStack.push(aEvent);
		}
		
		public BehaviorCallEvent popParent()
		{
			return itsParentsStack.pop();
		}
		
		public BehaviorCallEvent peekParent()
		{
			return itsParentsStack.peek();
		}
	}
}
