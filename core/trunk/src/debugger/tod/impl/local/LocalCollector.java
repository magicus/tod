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
package tod.impl.local;

import tod.agent.AgentUtils;
import tod.core.Output;
import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.ThreadInfo;
import tod.impl.common.EventCollector;
import tod.impl.common.event.ArrayWriteEvent;
import tod.impl.common.event.BehaviorCallEvent;
import tod.impl.common.event.BehaviorExitEvent;
import tod.impl.common.event.ConstructorChainingEvent;
import tod.impl.common.event.Event;
import tod.impl.common.event.ExceptionGeneratedEvent;
import tod.impl.common.event.FieldWriteEvent;
import tod.impl.common.event.InstantiationEvent;
import tod.impl.common.event.LocalVariableWriteEvent;
import tod.impl.common.event.MethodCallEvent;
import tod.impl.common.event.OutputEvent;
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
	private static final boolean LOG = false;
	
	private final LocalBrowser itsBrowser;
	
	public LocalCollector(LocalBrowser aBrowser, IHostInfo aHost)
	{
		super(aHost, aBrowser.getLocationsRepository());
		itsBrowser = aBrowser;
		itsBrowser.addHost(aHost);
	}

	private void addEvent(Event aEvent)
	{
		itsBrowser.addEvent(aEvent);
	}
	
	private IBehaviorInfo getBehavior(int aId)
	{
		return itsBrowser.getLocationsRepository().getBehavior(aId);
	}
	
	private IFieldInfo getField(int aId)
	{
		return itsBrowser.getLocationsRepository().getField(aId);
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
	
	private String formatBehavior(int aId)
	{
		IBehaviorInfo theBehavior = getBehavior(aId);
		return theBehavior != null ? 
				String.format("%d (%s.%s)", aId, theBehavior.getType().getName(), theBehavior.getName())
				: ""+aId;
	}
	
	private String formatField(int aId)
	{
		IFieldInfo theField = getField(aId);
		return theField != null ?
				String.format("%d (%s)", aId, theField.getName())
				: ""+aId;
	}
	
	private void initEvent(
			Event aEvent, 
			LocalThreadInfo aThread, 
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex)
	{
		BehaviorCallEvent theParentEvent = aThread.peekParent();
//		assert theParentEvent == null || theParentEvent.getTimestamp() == aParentTimestamp;
		
		aEvent.setHost(getHost());
		aEvent.setThread(aThread);
		aEvent.setDepth(aDepth);
		aEvent.setParent(theParentEvent);
		aEvent.setTimestamp(aTimestamp);
		aEvent.setOperationBytecodeIndex(aOperationBytecodeIndex);
		
		if (theParentEvent != null) theParentEvent.addChild(aEvent);
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
		if (LOG) System.out.println(String.format(
				"exception    (thread: %d, p.ts: %s, depth: %d, ts: %s, bid: %s, exc.: %s",
				aThreadId,
				AgentUtils.formatTimestamp(aParentTimestamp),
				aDepth,
				AgentUtils.formatTimestamp(aTimestamp),
				formatBehavior(aBehaviorId),
				aException));
		
		ExceptionGeneratedEvent theEvent = new ExceptionGeneratedEvent();
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex);
		
		theEvent.setException(aException);
		theEvent.setThrowingBehavior(getBehavior(aBehaviorId));
		
		addEvent(theEvent);
	}

	public void behaviorExit(
			int aThreadId,
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp,
			int aOperationBytecodeIndex, 
			int aBehaviorId,
			boolean aHasThrown, 
			Object aResult)
	{
		if (LOG) System.out.println(String.format(
				"behaviorExit (thread: %d, p.ts: %s, depth: %d, ts: %s, bid: %s, thrown: %s, ret: %s",
				aThreadId,
				AgentUtils.formatTimestamp(aParentTimestamp),
				aDepth,
				AgentUtils.formatTimestamp(aTimestamp),
				formatBehavior(aBehaviorId),
				aHasThrown,
				aResult));
		
		BehaviorExitEvent theEvent = new BehaviorExitEvent();
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex);
		
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
			int aOperationBytecodeIndex,
			int aFieldId,
			Object aTarget, 
			Object aValue)
	{
		if (LOG) System.out.println(String.format(
				"fieldWrite   (thread: %d, p.ts: %s, depth: %d, ts: %s, fid: %s, target: %s, val: %s",
				aThreadId,
				AgentUtils.formatTimestamp(aParentTimestamp),
				aDepth,
				AgentUtils.formatTimestamp(aTimestamp),
				formatField(aFieldId),
				aTarget,
				aValue));
		
		FieldWriteEvent theEvent = new FieldWriteEvent();
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex);

		theEvent.setField(getField(aFieldId));
		theEvent.setTarget(aTarget);
		theEvent.setValue(aValue);
		
		addEvent(theEvent);
	}
	
	public void arrayWrite(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			int aOperationBytecodeIndex,
			Object aTarget, 
			int aIndex, 
			Object aValue)
	{
		if (LOG) System.out.println(String.format(
				"arrayWrite   (thread: %d, p.ts: %s, depth: %d, ts: %s, target: %s, ind: %d, val: %s",
				aThreadId,
				AgentUtils.formatTimestamp(aParentTimestamp),
				aDepth,
				AgentUtils.formatTimestamp(aTimestamp),
				aTarget,
				aIndex,
				aValue));
		
		ArrayWriteEvent theEvent = new ArrayWriteEvent();
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex);

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
			int aOperationBytecodeIndex, 
			boolean aDirectParent, 
			int aCalledBehaviorId, 
			int aExecutedBehaviorId,
			Object aTarget, 
			Object[] aArguments)
	{
		if (LOG) System.out.println(String.format(
				"instantiation(thread: %d, p.ts: %s, depth: %d, ts: %s, direct: %s, c.bid: %s, e.bid: %s, target: %s, args: %s",
				aThreadId,
				AgentUtils.formatTimestamp(aParentTimestamp),
				aDepth,
				AgentUtils.formatTimestamp(aTimestamp),
				aDirectParent,
				formatBehavior(aCalledBehaviorId),
				formatBehavior(aExecutedBehaviorId),
				aTarget,
				aArguments));
		
		InstantiationEvent theEvent = new InstantiationEvent();
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex);

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
			int aOperationBytecodeIndex,
			int aVariableId, 
			Object aValue)
	{
		if (LOG) System.out.println(String.format(
				"localWrite   (thread: %d, p.ts: %s, depth: %d, ts: %s, vid: %d, val: %s",
				aThreadId,
				AgentUtils.formatTimestamp(aParentTimestamp),
				aDepth,
				AgentUtils.formatTimestamp(aTimestamp),
				aVariableId,
				aValue));
		
		LocalVariableWriteEvent theEvent = new LocalVariableWriteEvent();
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex);

		IBehaviorInfo theBehavior = theThread.peekParent().getExecutedBehavior();
		LocalVariableInfo theInfo = theBehavior.getLocalVariableInfo(aOperationBytecodeIndex+35, aVariableId); // 35 is the size of our instrumentation
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
			int aOperationBytecodeIndex, 
			boolean aDirectParent, 
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments)
	{
		if (LOG) System.out.println(String.format(
				"methodCall   (thread: %d, p.ts: %s, depth: %d, ts: %s, direct: %s, c.bid: %s, e.bid: %s, target: %s, args: %s, bci: %d)",
				aThreadId,
				AgentUtils.formatTimestamp(aParentTimestamp),
				aDepth,
				AgentUtils.formatTimestamp(aTimestamp),
				aDirectParent,
				formatBehavior(aCalledBehaviorId),
				formatBehavior(aExecutedBehaviorId),
				aTarget,
				aArguments,
				aOperationBytecodeIndex));
		
		MethodCallEvent theEvent = new MethodCallEvent();
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex);

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
		if (LOG) System.out.println(String.format(
				"output       (thread: %d, p.ts: %s, depth: %d, ts: %s, out: %s, data: %s",
				aThreadId,
				AgentUtils.formatTimestamp(aParentTimestamp),
				aDepth,
				AgentUtils.formatTimestamp(aTimestamp),
				aOutput,
				aData));
		
		OutputEvent theEvent = new OutputEvent();
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, -1);
		
		theEvent.setOutput(aOutput);
		theEvent.setData(null); //TODO: fix
		
		addEvent(theEvent);
	}

	public void superCall(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aOperationBytecodeIndex,
			boolean aDirectParent, 
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget, 
			Object[] aArguments)
	{
		if (LOG) System.out.println(String.format(
				"superCall    (thread: %d, p.ts: %s, depth: %d, ts: %s, direct: %s, c.bid: %s, e.bid: %s, target: %s, args: %s",
				aThreadId,
				AgentUtils.formatTimestamp(aParentTimestamp),
				aDepth,
				AgentUtils.formatTimestamp(aTimestamp),
				aDirectParent,
				formatBehavior(aCalledBehaviorId),
				formatBehavior(aExecutedBehaviorId),
				aTarget,
				aArguments));
		
		ConstructorChainingEvent theEvent = new ConstructorChainingEvent();
		LocalThreadInfo theThread = getThread(aThreadId);
		initEvent(theEvent, theThread, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex);

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
	
	public void register(long aObjectUID, Object aObject)
	{
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
