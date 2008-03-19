/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.core.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tod.agent.transport.LowLevelEventType;
import tod.core.ILogCollector;
import tod.core.database.structure.IStructureDatabase;
import tod.impl.database.structure.standard.HostInfo;

/**
 * A {@link LogReceiver} that uses an {@link EventInterpreter} to transform low-level events into
 * high-level events. Leaves the responsibility of processing high-level events
 * and value packets to subclasses.
 * @author gpothier
 */
public class CollectorLogReceiver extends LogReceiver
{
	private final ILogCollector itsCollector;
	private final EventInterpreter itsInterpreter;
	
	public CollectorLogReceiver(
			HostInfo aHostInfo, 
			InputStream aInStream, 
			OutputStream aOutStream, 
			boolean aStart,
			IStructureDatabase aStructureDatabase,
			ILogCollector aCollector)
	{
		super(aHostInfo, aInStream, aOutStream, false);
		itsCollector = aCollector;
		itsInterpreter = new EventInterpreter(aStructureDatabase, itsCollector);
		if (aStart) start();
	}
	
	public ILogCollector getCollector()
	{
		return itsCollector;
	}

	@Override
	protected void processEvent(LowLevelEventType aType, DataInputStream aStream) throws IOException
	{
		LowLevelEventReader.readEvent(aType, aStream, itsInterpreter);
	}
	
	@Override
	protected void processRegister(DataInputStream aStream) throws IOException
	{
		ValueReader.readRegistered(aStream, itsCollector);
	}

	@Override
	protected void processClear()
	{
		itsCollector.clear();
	}

	@Override
	protected int processFlush()
	{
		return itsCollector.flush();
	}
}
