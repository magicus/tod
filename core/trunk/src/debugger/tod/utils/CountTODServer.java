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
package tod.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import tod.agent.Output;
import tod.core.ILogCollector;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.server.CollectorTODServer;
import tod.core.server.ICollectorFactory;
import tod.core.server.TODServer;
import tod.core.session.AbstractSession;
import tod.core.session.ISessionMonitor;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.database.structure.standard.StructureDatabase;
import tod.impl.local.LocalBrowser;

/**
 * A tod server that counts received events
 * @author gpothier
 */
public class CountTODServer extends AbstractSession
{
	private TODServer itsServer;
	private IMutableStructureDatabase itsStructureDatabase;
	
	private LocalBrowser itsBrowser;
	private List<CountCollector> itsCollectors = new ArrayList<CountCollector>();
	
	public CountTODServer(URI aUri, TODConfig aConfig)
	{
		super(aUri, aConfig);
		itsStructureDatabase = StructureDatabase.create(aConfig);
		itsBrowser = new LocalBrowser(this, itsStructureDatabase);
		
		ASMDebuggerConfig theConfig = new ASMDebuggerConfig(aConfig);

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(
				itsStructureDatabase, 
				theConfig);
		
		itsServer = new CollectorTODServer(
				aConfig,
				theInstrumenter,
				itsStructureDatabase,
				new MyCollectorFactory())
		{
			@Override
			protected void disconnected()
			{
				super.disconnected();
				System.out.println("Event count: "+getCount());
			}
		};
		
	}
	
	public void disconnect()
	{
		itsServer.close();
	}
	
	public void flush()
	{
	}

	public ILogBrowser getLogBrowser()
	{
		return itsBrowser;
	}
	
	public ISessionMonitor getMonitor()
	{
		throw new UnsupportedOperationException();
	}

	public JComponent createConsole()
	{
		return null;
	}
	
	public boolean isAlive()
	{
		return true;
	}
	
	public long getCount()
	{
		long theCount = 0;
		for (CountCollector theCollector : itsCollectors)
		{
			theCount += theCollector.getCount();
		}
		
		return theCount;
	}

	private class MyCollectorFactory implements ICollectorFactory
	{
		public ILogCollector create()
		{
			CountCollector theCollector = new CountCollector(itsBrowser);
			
			itsCollectors.add(theCollector);
			return theCollector;
		}
	}
	
	private static class CountCollector implements ILogCollector //extends LocalCollector
	{
		private long itsCount = 0;
		private static final boolean CALL_SUPER = false;
		
		public CountCollector(LocalBrowser aBrowser)
		{
//			super(aBrowser, null);
		}

		public long getCount()
		{
			return itsCount;
		}

		public void arrayWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow, int aProbeId, Object aTarget, int aIndex, Object aValue)
		{
			itsCount++;
//			if (CALL_SUPER) super.arrayWrite(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aTarget, aIndex, aValue);
		}

		public void behaviorExit(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow, int aProbeId, int aBehaviorId, boolean aHasThrown, Object aResult)
		{
			itsCount++;
//			if (CALL_SUPER) super.behaviorExit(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aBehaviorId, aHasThrown, aResult);
		}

		public void exception(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow, String aMethodName, String aMethodSignature, String aMethodDeclaringClassSignature, int aOperationBytecodeIndex, Object aException)
		{
			itsCount++;
//			if (CALL_SUPER) super.exception(aThreadId, aParentTimestamp, aDepth, aTimestamp, aDepth, aOperationBytecodeIndex, aException);
		}

		public void fieldWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow, int aProbeId, int aFieldId, Object aTarget, Object aValue)
		{
			itsCount++;
//			if (CALL_SUPER) super.fieldWrite(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aFieldId, aTarget, aValue);
		}

		public void instantiation(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow, int aProbeId, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId, Object aTarget, Object[] aArguments)
		{
			itsCount++;
//			if (CALL_SUPER) super.instantiation(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
		}

		public void localWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow, int aProbeId, int aVariableId, Object aValue)
		{
			itsCount++;
//			if (CALL_SUPER) super.localWrite(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aVariableId, aValue);
		}

		public void methodCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow, int aProbeId, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId, Object aTarget, Object[] aArguments)
		{
			itsCount++;
//			if (CALL_SUPER) super.methodCall(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
		}

		public void newArray(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
				int aAdviceCFlow, int aProbeId, Object aTarget, int aBaseTypeId, int aSize)
		{
			itsCount++;
		}

		public void instanceOf(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow,
				int aProbeId, Object aObject, int aTypeId, boolean aResult)
		{
			itsCount++;
		}

		public void output(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow, Output aOutput, byte[] aData)
		{
			itsCount++;
//			if (CALL_SUPER) super.output(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOutput, aData);
		}

		public void superCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aAdviceCFlow, int aProbeId, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId, Object aTarget, Object[] aArguments)
		{
			itsCount++;
//			if (CALL_SUPER) super.superCall(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
		}

		public void register(long aObjectUID, Object aObject, long aTimestamp)
		{
//			if (CALL_SUPER) super.register(aObjectUID, aObject);
		}

		public void thread(int aThreadId, long aJVMThreadId, String aName)
		{
//			if (CALL_SUPER) super.thread(aThreadId, aJVMThreadId, aName);
		}

		public void clear()
		{
		}

		public int flush()
		{
			return 0;
		}
	}
	
	public static void main(String[] args)
	{
		TODConfig theConfig = new TODConfig();
		CountTODServer theServer = new CountTODServer(null, theConfig);
		
		try
		{
			BufferedReader theReader = new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
				String s = theReader.readLine();
				if ("q".equals(s)) return;
				System.out.println("Event count: "+theServer.getCount());				
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

	}

}