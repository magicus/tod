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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import tod.core.ILogCollector;
import tod.core.LocationRegisterer;
import tod.core.Output;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.server.CollectorTODServer;
import tod.core.server.ICollectorFactory;
import tod.core.server.TODServer;
import tod.core.session.AbstractSession;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.local.LocalBrowser;

/**
 * A tod server that counts received events
 * @author gpothier
 */
public class CountTODServer extends AbstractSession
{
	private TODServer itsServer;
	private DummyLocationRegisterer itsLocationRegistrer;
	
	private LocalBrowser itsBrowser;
	private List<CountCollector> itsCollectors = new ArrayList<CountCollector>();
	
	public CountTODServer(TODConfig aConfig, URI aUri)
	{
		super(aUri);
		itsLocationRegistrer = new DummyLocationRegisterer();
		itsBrowser = new LocalBrowser(itsLocationRegistrer);
		
		ASMDebuggerConfig theConfig = new ASMDebuggerConfig(
				aConfig,
				itsLocationRegistrer);

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theConfig);
		
		itsServer = new CollectorTODServer(
				aConfig,
				theInstrumenter,
				new LocationRegisterer(),
				new MyCollectorFactory())
		{
			@Override
			protected void disconnected()
			{
				System.out.println("Event count: "+getCount());
			}
		};
		
		try
		{
			BufferedReader theReader = new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
				String s = theReader.readLine();
				if ("q".equals(s)) return;
				System.out.println("Event count: "+getCount());				
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void disconnect()
	{
		itsServer.disconnect();
	}
	
	public String getCachedClassesPath()
	{
		return null;
	}

	public ILogBrowser getLogBrowser()
	{
		return itsBrowser;
	}
	
	public JComponent createConsole()
	{
		return null;
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
		
		public void flushAll()
		{
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

		public void arrayWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBehaviorId, int aOperationBytecodeIndex, Object aTarget, int aIndex, Object aValue)
		{
			itsCount++;
//			if (CALL_SUPER) super.arrayWrite(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aTarget, aIndex, aValue);
		}

		public void behaviorExit(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBehaviorId, int aOperationBytecodeIndex, int aBehaviorId, boolean aHasThrown, Object aResult)
		{
			itsCount++;
//			if (CALL_SUPER) super.behaviorExit(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aBehaviorId, aHasThrown, aResult);
		}

		public void exception(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, String aMethodName, String aMethodSignature, String aMethodDeclaringClassSignature, int aOperationBytecodeIndex, Object aException)
		{
			itsCount++;
//			if (CALL_SUPER) super.exception(aThreadId, aParentTimestamp, aDepth, aTimestamp, aDepth, aOperationBytecodeIndex, aException);
		}

		public void fieldWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBehaviorId, int aOperationBytecodeIndex, int aFieldId, Object aTarget, Object aValue)
		{
			itsCount++;
//			if (CALL_SUPER) super.fieldWrite(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aFieldId, aTarget, aValue);
		}

		public void instantiation(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBehaviorId, int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId, Object aTarget, Object[] aArguments)
		{
			itsCount++;
//			if (CALL_SUPER) super.instantiation(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
		}

		public void localWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBehaviorId, int aOperationBytecodeIndex, int aVariableId, Object aValue)
		{
			itsCount++;
//			if (CALL_SUPER) super.localWrite(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aVariableId, aValue);
		}

		public void methodCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBehaviorId, int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId, Object aTarget, Object[] aArguments)
		{
			itsCount++;
//			if (CALL_SUPER) super.methodCall(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
		}

		public void output(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, Output aOutput, byte[] aData)
		{
			itsCount++;
//			if (CALL_SUPER) super.output(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOutput, aData);
		}

		public void superCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBehaviorId, int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId, Object aTarget, Object[] aArguments)
		{
			itsCount++;
//			if (CALL_SUPER) super.superCall(aThreadId, aParentTimestamp, aDepth, aTimestamp, aOperationBytecodeIndex, aDirectParent, aCalledBehaviorId, aExecutedBehaviorId, aTarget, aArguments);
		}

		public void register(long aObjectUID, Object aObject)
		{
//			if (CALL_SUPER) super.register(aObjectUID, aObject);
		}

		public void thread(int aThreadId, long aJVMThreadId, String aName)
		{
//			if (CALL_SUPER) super.thread(aThreadId, aJVMThreadId, aName);
		}
	}
	
	public static void main(String[] args)
	{
		TODConfig theConfig = new TODConfig();
		new CountTODServer(theConfig, null);
	}

}