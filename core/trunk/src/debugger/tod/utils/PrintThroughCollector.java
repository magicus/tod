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
package tod.utils;

import java.io.PrintStream;

import tod.agent.AgentUtils;
import tod.agent.DebugFlags;
import tod.core.ILogCollector;
import tod.core.Output;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.ILocationsRepository;
import zz.utils.Utils;

/**
 * A collector that prints all the events it receives
 * @author gpothier
 */
public class PrintThroughCollector implements ILogCollector
{
	private static PrintStream itsPrintStream = DebugFlags.COLLECTOR_PRINT_STREAM;
	
	private IHostInfo itsHost;
	private ILogCollector itsCollector;
	private ILocationsRepository itsLocationsRepository;
	
	public PrintThroughCollector(
			IHostInfo aHost, 
			ILogCollector aCollector, 
			ILocationsRepository aLocationsRepository)
	{
		itsHost = aHost;
		itsCollector = aCollector;
		itsLocationsRepository = aLocationsRepository;
	}

	private IBehaviorInfo getBehavior(int aId)
	{
		return itsLocationsRepository.getBehavior(aId);
	}
	
	private IFieldInfo getField(int aId)
	{
		return itsLocationsRepository.getField(aId);
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
	

	public void exception(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp, 
			String aMethodName, 
			String aMethodSignature, 
			String aMethodDeclaringClassSignature, 
			int aOperationBytecodeIndex, 
			Object aException)
	{
		print(aDepth, String.format(
				"exception    (thread: %d, p.ts: %s, depth: %d, ts: %s, bid: %s, exc.: %s",
				aThreadId,
				formatTimestamp(aParentTimestamp),
				aDepth,
				formatTimestamp(aTimestamp),
				aMethodDeclaringClassSignature+"."+aMethodName+" "+aMethodSignature,
				aException));

		itsCollector.exception(aThreadId, aParentTimestamp, aDepth, aTimestamp, aMethodName, aMethodSignature, aMethodDeclaringClassSignature, aOperationBytecodeIndex, aException);
	}

	public void behaviorExit(
			int aThreadId,
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp,
			int aOperationBehaviorId,
			int aOperationBytecodeIndex, 
			int aBehaviorId,
			boolean aHasThrown, 
			Object aResult)
	{
		print(aDepth, String.format(
				"behaviorExit (thread: %d, p.ts: %s, depth: %d, ts: %s, bid: %s, thrown: %s, ret: %s",
				aThreadId,
				formatTimestamp(aParentTimestamp),
				aDepth,
				formatTimestamp(aTimestamp),
				formatBehavior(aBehaviorId),
				aHasThrown,
				aResult));

		itsCollector.behaviorExit(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aBehaviorId, aHasThrown, aResult);
	}

	public void fieldWrite(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp,
			int aOperationBehaviorId,
			int aOperationBytecodeIndex,
			int aFieldId,
			Object aTarget, 
			Object aValue)
	{
		print(aDepth, String.format(
				"fieldWrite   (thread: %d, p.ts: %s, depth: %d, ts: %s, fid: %s, target: %s, val: %s",
				aThreadId,
				formatTimestamp(aParentTimestamp),
				aDepth,
				formatTimestamp(aTimestamp),
				formatField(aFieldId),
				aTarget,
				aValue));

		itsCollector.fieldWrite(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aFieldId, aTarget, aValue);
	}
	
	public void arrayWrite(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			int aOperationBehaviorId,
			int aOperationBytecodeIndex,
			Object aTarget, 
			int aIndex, 
			Object aValue)
	{
		print(aDepth, String.format(
				"arrayWrite   (thread: %d, p.ts: %s, depth: %d, ts: %s, target: %s, ind: %d, val: %s",
				aThreadId,
				formatTimestamp(aParentTimestamp),
				aDepth,
				formatTimestamp(aTimestamp),
				aTarget,
				aIndex,
				aValue));
		
		itsCollector.arrayWrite(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aTarget, aIndex, aValue);
	}

	public void instantiation(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			int aOperationBehaviorId,
			int aOperationBytecodeIndex, 
			boolean aDirectParent, 
			int aCalledBehaviorId, 
			int aExecutedBehaviorId,
			Object aTarget, 
			Object[] aArguments)
	{
		print(aDepth, String.format(
				"instantiation(thread: %d, p.ts: %s, depth: %d, ts: %s, direct: %s, c.bid: %s, e.bid: %s, target: %s, args: %s",
				aThreadId,
				formatTimestamp(aParentTimestamp),
				aDepth,
				formatTimestamp(aTimestamp),
				aDirectParent,
				formatBehavior(aCalledBehaviorId),
				formatBehavior(aExecutedBehaviorId),
				aTarget,
				aArguments));

		itsCollector.instantiation(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
	}

	public void localWrite(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			int aOperationBehaviorId,
			int aOperationBytecodeIndex,
			int aVariableId, 
			Object aValue)
	{
		print(aDepth, String.format(
				"localWrite   (thread: %d, p.ts: %s, depth: %d, ts: %s, vid: %d, val: %s",
				aThreadId,
				formatTimestamp(aParentTimestamp),
				aDepth,
				formatTimestamp(aTimestamp),
				aVariableId,
				aValue));

		itsCollector.localWrite(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aVariableId, aValue);
	}

	public void methodCall(
			int aThreadId,
			long aParentTimestamp,
			short aDepth, 
			long aTimestamp,
			int aOperationBehaviorId,
			int aOperationBytecodeIndex, 
			boolean aDirectParent, 
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget,
			Object[] aArguments)
	{
		print(aDepth, String.format(
				"methodCall   (thread: %d, p.ts: %s, depth: %d, ts: %s, direct: %s, c.bid: %s, e.bid: %s, target: %s, args: %s, bci: %d)",
				aThreadId,
				formatTimestamp(aParentTimestamp),
				aDepth,
				formatTimestamp(aTimestamp),
				aDirectParent,
				formatBehavior(aCalledBehaviorId),
				formatBehavior(aExecutedBehaviorId),
				aTarget,
				aArguments,
				aOperationBytecodeIndex));

		itsCollector.methodCall(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
	}

	public void output(
			int aThreadId,
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			Output aOutput,
			byte[] aData)
	{
		print(aDepth, String.format(
				"output       (thread: %d, p.ts: %s, depth: %d, ts: %s, out: %s, data: %s",
				aThreadId,
				formatTimestamp(aParentTimestamp),
				aDepth,
				formatTimestamp(aTimestamp),
				aOutput,
				aData));

		itsCollector.output(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOutput, aData);
	}

	public void superCall(
			int aThreadId, 
			long aParentTimestamp,
			short aDepth,
			long aTimestamp,
			int aOperationBehaviorId,
			int aOperationBytecodeIndex,
			boolean aDirectParent, 
			int aCalledBehaviorId,
			int aExecutedBehaviorId,
			Object aTarget, 
			Object[] aArguments)
	{
		print(aDepth, String.format(
				"superCall    (thread: %d, p.ts: %s, depth: %d, ts: %s, direct: %s, c.bid: %s, e.bid: %s, target: %s, args: %s",
				aThreadId,
				formatTimestamp(aParentTimestamp),
				aDepth,
				formatTimestamp(aTimestamp),
				aDirectParent,
				formatBehavior(aCalledBehaviorId),
				formatBehavior(aExecutedBehaviorId),
				aTarget,
				aArguments));

		itsCollector.superCall(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
	}

	public void register(long aObjectUID, Object aObject)
	{
		itsCollector.register(aObjectUID, aObject);
	}

	public void thread(int aThreadId, long aJVMThreadId, String aName)
	{
		itsCollector.thread(aThreadId, aJVMThreadId, aName);
	}

	private static String formatTimestamp(long aTimestamp)
	{
		return DebugFlags.COLLECTOR_FORMAT_TIMESTAMPS ?
				AgentUtils.formatTimestamp(aTimestamp)
				: ""+aTimestamp;
	}
	
	private void print(int aDepth, String aString)
	{
		synchronized (itsPrintStream)
		{
			itsPrintStream.print(Utils.indent(
					"h" + itsHost.getId() + " - " + aString, 
					aDepth, 
					"  "));
		}
	}
	
}
