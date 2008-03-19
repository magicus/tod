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
package tod.impl.dbgrid.dispatch;

import static tod.impl.dbgrid.DebuggerGridConfig.DISPATCH_BATCH_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.LOAD_BALANCING;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

import tod.agent.Output;
import tod.agent.transport.Commands;
import tod.agent.transport.LowLevelEventType;
import tod.core.DebugFlags;
import tod.core.ILogCollector;
import tod.core.transport.CollectorLogReceiver;
import tod.core.transport.HighLevelEventWriter;
import tod.core.transport.LogReceiver;
import tod.impl.database.structure.standard.HostInfo;
import tod.impl.database.structure.standard.ThreadInfo;
import tod.impl.dbgrid.GridMaster;

public class EventDispatcher extends AbstractEventDispatcher
implements RIDispatcher
{
	public EventDispatcher() throws RemoteException
	{
		super(true);
	}

	@Override
	protected DispatchNodeProxy createProxy(
			RIDispatchNode aConnectable,
			InputStream aInputStream,
			OutputStream aOutputStream,
			String aId)
	{
		try
		{
			DataOutputStream theOutStream = new DataOutputStream(aOutputStream);

			// LogReceiver expects host name
			theOutStream.writeUTF("InternalEventDispatcher"); 

			return new DispatcherProxy(aConnectable, aInputStream, aOutputStream, aId);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public LogReceiver createLogReceiver(
			HostInfo aHostInfo, 
			GridMaster aMaster, 
			InputStream aInStream,
			OutputStream aOutStream, 
			boolean aStartImmediately)
	{
		return new DispatchingLogReceiver(aMaster, aHostInfo, aInStream, aOutStream, aStartImmediately);
	}

	@Override
	protected void connectToDispatcher(Socket aSocket)
	{
		try
		{
			createLogReceiver(
					null, 
					null, 
					new BufferedInputStream(aSocket.getInputStream()), 
					new BufferedOutputStream(aSocket.getOutputStream()), 
					true);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private class DispatchingLogReceiver extends CollectorLogReceiver
	{
		/**
		 * Grid master, if we are the root dispatcher. In this case we forward
		 * thread registration to the master.
		 */
		private GridMaster itsMaster;
		
		/**
		 * Remaining packets to process before changing to another child
		 */
		private int itsPacketsBeforeChange = 0;
//		private int itsCurrentChild = 0;
		
		private DispatcherProxy itsCurrentChild;
		private int itsCurrentChildIndex;

		public DispatchingLogReceiver(
				GridMaster aMaster,
				HostInfo aHostInfo,
				InputStream aInStream,
				OutputStream aOutStream,
				boolean aStart)
		{
			super(aHostInfo, aInStream, aOutStream, false, aMaster.getStructureDatabase(), new TransmitterLogCollector());
			itsMaster = aMaster;
			if (aStart) start();
		}
		
		@Override
		public TransmitterLogCollector getCollector()
		{
			return (TransmitterLogCollector) super.getCollector();
		}
		
		private DispatcherProxy getNextChild()
		{
			if (itsPacketsBeforeChange == 0)
			{
				if (LOAD_BALANCING)
				{
					int theMinChild = Integer.MAX_VALUE;
					itsCurrentChild = null;
					for(int i=0;i<getChildrenCount();i++)
					{
						DispatcherProxy theProxy = (DispatcherProxy) getChild((i+itsCurrentChildIndex) % getChildrenCount());
						int theSize = theProxy.getQueueSize();
						if (theSize < theMinChild)
						{
							itsCurrentChild = theProxy;
							theMinChild = theSize;
							if (theSize == 0) break; // If the child is empty there is no need to continue searching
						}
					}
					// We need a bit of round robin in order to properly balance the
					// dispatch under light load.
					itsCurrentChildIndex++; 
				}
				else
				{
					itsCurrentChild = (DispatcherProxy) getChild(itsCurrentChildIndex);
					itsCurrentChildIndex = (itsCurrentChildIndex+1) % getChildrenCount();
				}
				
				itsPacketsBeforeChange = DISPATCH_BATCH_SIZE;
			}
			
			itsPacketsBeforeChange --;
			return itsCurrentChild;
		}
		
		@Override
		protected int processFlush()
		{
			int theTotal = 0;
			for (DispatchNodeProxy theProxy : getChildren()) 
			{
				theTotal += theProxy.flush();
			}
			
			return theTotal;
		}
		
		@Override
		protected void processClear()
		{
			for (DispatchNodeProxy theProxy : getChildren()) 
			{
				theProxy.clear();
			}
		}
		
		@Override
		protected void processRegister(DataInputStream aStream) throws IOException
		{
			DispatcherProxy theProxy = getNextChild();
			theProxy.forwardPacket((byte) (Commands.BASE + Commands.CMD_REGISTER.ordinal()), aStream);
			if (DebugFlags.DISPATCH_FAKE_1) theProxy.getOutStream().flush();
		}

		@Override
		protected void processEvent(LowLevelEventType aType, DataInputStream aStream) throws IOException
		{
			if (aType == LowLevelEventType.REGISTER_THREAD)
			{
				// Thread registration is not forwarded.
				readThread(aStream);
			}
			else
			{
				getCollector().setCurrentProxy(getNextChild());
				super.processEvent(aType, aStream);
			}
		}
		
		private void readThread(DataInputStream aStream) throws IOException
		{
			int theSize = aStream.readInt();
			int theId = aStream.readInt();
			long theJVMId = aStream.readLong();
			String theName = aStream.readUTF();

			if (itsMaster != null)
			{
				ThreadInfo theThreadInfo = new ThreadInfo(getHostInfo(), theId, theJVMId, theName);
				itsMaster.registerThread(theThreadInfo);
			}
		}
	}
	
	/**
	 * This log collector resends serialized the events.
	 * @author gpothier
	 */
	private static class TransmitterLogCollector implements ILogCollector
	{
		private final HighLevelEventWriter itsWriter = new HighLevelEventWriter(null);
		private DispatcherProxy itsCurrentProxy;
		
		public void setCurrentProxy(DispatcherProxy aCurrentProxy)
		{
			itsCurrentProxy = aCurrentProxy;
			itsWriter.setStream(itsCurrentProxy.getOutStream());
		}

		public void arrayWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow,
				int aProbeId, Object aTarget, int aIndex, Object aValue)
		{
			try
			{
				itsWriter.sendArrayWrite(aThreadId, aParentTimestamp, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aTarget, aIndex, aValue);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void instanceOf(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow,
				int aProbeId, Object aObject, int aTypeId, boolean aResult)
		{
			try
			{
				itsWriter.sendInstanceOf(aThreadId, aParentTimestamp, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aObject, aTypeId, aResult);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void behaviorExit(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow,
				int aProbeId, int aBehaviorId, boolean aHasThrown, Object aResult)
		{
			try
			{
				itsWriter.sendBehaviorExit(aThreadId, aParentTimestamp, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aBehaviorId, aHasThrown, aResult);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void clear()
		{
			throw new UnsupportedOperationException();
		}

		public void exception(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow,
				String aMethodName, String aMethodSignature, String aMethodDeclaringClassSignature,
				int aOperationBytecodeIndex, Object aException)
		{
			try
			{
				itsWriter.sendException(aThreadId, aParentTimestamp, aDepth, aTimestamp, aAdviceCFlow, aMethodName, aMethodSignature, aMethodDeclaringClassSignature, aOperationBytecodeIndex, aException);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void fieldWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow,
				int aProbeId, int aFieldId, Object aTarget, Object aValue)
		{
			try
			{
				itsWriter.sendFieldWrite(aThreadId, aParentTimestamp, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aFieldId, aTarget, aValue);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public int flush()
		{
			throw new UnsupportedOperationException();
		}

		public void instantiation(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow,
				int aProbeId, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
				Object aTarget, Object[] aArguments)
		{
			try
			{
				itsWriter.sendInstantiation(aThreadId, aParentTimestamp, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void localWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow,
				int aProbeId, int aVariableId, Object aValue)
		{
			try
			{
				itsWriter.sendLocalWrite(aThreadId, aParentTimestamp, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aVariableId, aValue);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void methodCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow,
				int aProbeId, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
				Object aTarget, Object[] aArguments)
		{
			try
			{
				itsWriter.sendMethodCall(aThreadId, aParentTimestamp, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void newArray(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow,
				int aProbeId, Object aTarget, int aBaseTypeId, int aSize)
		{
			try
			{
				itsWriter.sendNewArray(aThreadId, aParentTimestamp, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aTarget, aBaseTypeId, aSize);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void output(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow,
				Output aOutput, byte[] aData)
		{
			try
			{
				itsWriter.sendOutput(aThreadId, aParentTimestamp, aDepth, aTimestamp, aAdviceCFlow, aOutput, aData);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void register(long aObjectUID, Object aObject, long aTimestamp)
		{
			throw new UnsupportedOperationException();
		}

		public void superCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow,
				int aProbeId, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
				Object aTarget, Object[] aArguments)
		{
			try
			{
				itsWriter.sendSuperCall(aThreadId, aParentTimestamp, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public void thread(int aThreadId, long aJVMThreadId, String aName)
		{
			throw new UnsupportedOperationException();
		}
		
		
	}

}
