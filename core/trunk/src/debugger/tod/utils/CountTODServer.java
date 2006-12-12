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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import tod.core.ILogCollector;
import tod.core.LocationRegistrer;
import tod.core.Output;
import tod.core.bci.IInstrumenter;
import tod.core.config.TODConfig;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.HostInfo;
import tod.core.server.ICollectorFactory;
import tod.core.server.TODServer;
import tod.core.session.AbstractSession;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalCollector;
import zz.utils.Utils;

/**
 * A tod server that counts received events
 * @author gpothier
 */
public class CountTODServer extends AbstractSession
{
	private TODServer itsServer;
	private DummyLocationRegistrer itsLocationRegistrer;
	
	private LocalBrowser itsBrowser;
	private List<CountCollector> itsCollectors = new ArrayList<CountCollector>();
	
	public CountTODServer(TODConfig aConfig, URI aUri)
	{
		super(aUri);
		itsLocationRegistrer = new DummyLocationRegistrer();
		itsBrowser = new LocalBrowser(itsLocationRegistrer);
		
		ASMDebuggerConfig theConfig = new ASMDebuggerConfig(
				aConfig,
				itsLocationRegistrer);

		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theConfig);
		
		itsServer = new TODServer(
				aConfig,
				new MyCollectorFactory(),
				theInstrumenter);
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
			CountCollector theCollector = new CountCollector();
			
			itsCollectors.add(theCollector);
			return theCollector;
		}
		
		public void flushAll()
		{
		}
	}
	
	private static class CountCollector implements ILogCollector
	{
		private long itsCount = 0;

		public long getCount()
		{
			return itsCount;
		}

		public void arrayWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBytecodeIndex, Object aTarget, int aIndex, Object aValue)
		{
			itsCount++;
		}

		public void behaviorExit(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBytecodeIndex, int aBehaviorId, boolean aHasThrown, Object aResult)
		{
			itsCount++;
		}

		public void exception(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, String aMethodName, String aMethodSignature, String aMethodDeclaringClassSignature, int aOperationBytecodeIndex, Object aException)
		{
			itsCount++;
		}

		public void fieldWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBytecodeIndex, int aFieldId, Object aTarget, Object aValue)
		{
			itsCount++;
		}

		public void instantiation(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId, Object aTarget, Object[] aArguments)
		{
			itsCount++;
		}

		public void localWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBytecodeIndex, int aVariableId, Object aValue)
		{
			itsCount++;
		}

		public void methodCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId, Object aTarget, Object[] aArguments)
		{
			itsCount++;
		}

		public void output(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, Output aOutput, byte[] aData)
		{
			itsCount++;
		}

		public void superCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId, Object aTarget, Object[] aArguments)
		{
			itsCount++;
		}

		public void thread(int aThreadId, long aJVMThreadId, String aName)
		{
		}

		public void register(long aObjectUID, Object aObject)
		{
		}
	}
}