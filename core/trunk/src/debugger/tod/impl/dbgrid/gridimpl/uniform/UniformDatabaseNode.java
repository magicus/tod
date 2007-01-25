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
package tod.impl.dbgrid.gridimpl.uniform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.db.RINodeEventIterator;
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.gridimpl.AbstractEventDatabase;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridMessage;
import tod.impl.dbgrid.queries.EventCondition;
import tod.utils.NativeStream;
import zz.utils.Utils;
import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;

public class UniformDatabaseNode extends DatabaseNode
{
	/**
	 * This command pushes a list of events to the node.
	 * args:
	 *  count: int
	 *  events
	 * return: none
	 */
	public static final byte CMD_PUSH_EVENTS = 117;
	
	private AbstractEventDatabase itsDatabase;
	
	private final int[] itsBuffer = new int[DebuggerGridConfig.MASTER_EVENT_BUFFER_SIZE];
	private final byte[] itsByteBuffer = new byte[DebuggerGridConfig.MASTER_EVENT_BUFFER_SIZE*4];
	private final BitStruct itsStruct = new IntBitStruct(itsBuffer);
	private long itsReceivedMessages = 0;

	
	public UniformDatabaseNode() throws RemoteException
	{
	}
	
	public void clear() 
	{
		if (itsDatabase != null)
		{
			itsDatabase.unregister();
		}
		
		String thePrefix = DebuggerGridConfig.NODE_DATA_DIR;
		File theParent = new File(thePrefix);
		System.out.println("Using data directory: "+theParent);
		
		File theFile = new File(theParent, "events.bin");
		theFile.delete();
		itsDatabase = createDatabase(theFile);
	}
	
	protected AbstractEventDatabase createDatabase(File aFile)
	{
		int theNodeIndex = Integer.parseInt(getNodeId().substring(3));

		return new UniformEventDatabase(theNodeIndex, aFile);
	}
	
	
	public AbstractEventDatabase getDatabase()
	{
		return itsDatabase;
	}

	public int flush()
	{
		return itsDatabase.flush();
	}

	public long[] getEventCounts(
			EventCondition aCondition, 
			long aT1, 
			long aT2,
			int aSlotsCount,
			boolean aForceMergeCounts) throws RemoteException
	{
		return itsDatabase.getEventCounts(
				aCondition, 
				aT1, 
				aT2, 
				aSlotsCount,
				aForceMergeCounts);
	}

	public RINodeEventIterator getIterator(EventCondition aCondition) throws RemoteException
	{
		return itsDatabase.getIterator(aCondition);
	}
	
	

	@Override
	protected void processCommand(
			byte aCommand, 
			DataInputStream aInStream, 
			DataOutputStream aOutStream) throws IOException
	{
		switch (aCommand)
		{
		case CMD_PUSH_EVENTS:
			pushEvents(aInStream);
			break;
			
		default:
			super.processCommand(aCommand, aInStream, aOutStream);
				
		}
	}

	private void pushEvents(DataInputStream aStream) throws IOException
	{
		int theCount = aStream.readInt();
		
//		System.out.println(String.format(
//		"Received %d messages (already received %d)",
//		theCount,
//		itsReceivedMessages));
		
		Utils.readFully(aStream, itsByteBuffer);
		NativeStream.b2i(itsByteBuffer, itsBuffer);
		itsStruct.reset();
		
		itsReceivedMessages += theCount;

		for (int i=0;i<theCount;i++)
		{
			GridEvent theEvent = (GridEvent) GridMessage.read(itsStruct);
			itsDatabase.push(theEvent);
			eventStored(theEvent.getTimestamp());
		}
	}




}
