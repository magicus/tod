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
package tod.experiments;

import java.io.FileInputStream;
import java.net.URI;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JFrame;

import tod.core.config.TODConfig;
import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.browser.IVariablesInspector;
import tod.core.database.event.ExternalPointer;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.core.database.structure.SourceRange;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.core.session.AbstractSession;
import tod.core.session.ISession;
import tod.core.session.ISessionMonitor;
import tod.gui.MinerUI;
import tod.gui.seed.StructureSeed;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.database.IBidiIterator;
import tod.impl.database.structure.standard.StructureDatabase;
import zz.utils.Utils;

public class ABCTags
{
	public static void main(String[] args) throws Exception
	{
		TODConfig theConfig = new TODConfig();
		StructureDatabase theStructureDatabase = StructureDatabase.create(theConfig, "abc");
		theConfig.set(TODConfig.SCOPE_TRACE_FILTER, "[+xys]");
		ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(theConfig);
		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theStructureDatabase, theDebuggerConfig);
		
		byte[] theBytecode = Utils.readInputStream_byte(new FileInputStream(args[0]));
		theInstrumenter.instrumentClass("x", theBytecode);

		ILogBrowser theLogBrowser = new MyLogBrowser(theStructureDatabase);
		
		MinerUI theUI = new MyMinerUI(theConfig, theLogBrowser);
		
		JFrame frame = new JFrame("ABC test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(theUI);
		frame.setVisible(true);
		frame.setSize(1300, 500);
		
		StructureSeed theSeed = new StructureSeed(theUI, theLogBrowser);
		theUI.openSeed(theSeed, false);
		
		System.out.println("ready");
	}
	
	private static class MyMinerUI extends MinerUI
	{
		public MyMinerUI(TODConfig aConfig, ILogBrowser aLogBrowser)
		{
			setSession(new DummySession(aConfig, URI.create("dummy:dummy"), aLogBrowser));
		}
		
		public void gotoSource(SourceRange aSourceRange)
		{
		}
	}
	
	private static class DummySession extends AbstractSession
	{
		private final ILogBrowser itsLogBrowser;

		public DummySession(TODConfig aConfig, URI aUri, ILogBrowser aLogBrowser)
		{
			super(aUri, aConfig);
			itsLogBrowser = aLogBrowser;
		}

		public JComponent createConsole()
		{
			throw new UnsupportedOperationException();
		}

		public void disconnect()
		{
			throw new UnsupportedOperationException();
		}

		public void flush()
		{
			throw new UnsupportedOperationException();
		}

		public ILogBrowser getLogBrowser()
		{
			return itsLogBrowser;
		}

		public ISessionMonitor getMonitor()
		{
			return null;
		}

		public boolean isAlive()
		{
			throw new UnsupportedOperationException();
		}
		
	}
	
	private static class MyLogBrowser implements ILogBrowser
	{
		private final IStructureDatabase itsStructureDatabase;
		
		public MyLogBrowser(IStructureDatabase aStructureDatabase)
		{
			itsStructureDatabase = aStructureDatabase;
		}

		public ISession getSession()
		{
			throw new UnsupportedOperationException();
		}

		public IStructureDatabase getStructureDatabase()
		{
			return itsStructureDatabase;
		}

		public void clear()
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createAdviceSourceIdFilter(int aAdviceSourceId)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createArgumentFilter(ObjectId aId)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createArrayWriteFilter()
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createBehaviorCallFilter()
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createBehaviorCallFilter(IBehaviorInfo aBehavior)
		{
			throw new UnsupportedOperationException();
		}

		public IEventBrowser createBrowser()
		{
			throw new UnsupportedOperationException();
		}

		public IEventBrowser createBrowser(IEventFilter aFilter)
		{
			throw new UnsupportedOperationException();
		}

		public IObjectInspector createClassInspector(IClassInfo aClass)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createDepthFilter(int aDepth)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createEventFilter(ILogEvent aEvent)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createExceptionGeneratedFilter()
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createFieldFilter(IFieldInfo aField)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createFieldWriteFilter()
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createHostFilter(IHostInfo aHost)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createInstantiationFilter(ObjectId aObjectId)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createInstantiationsFilter()
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createInstantiationsFilter(ITypeInfo aType)
		{
			throw new UnsupportedOperationException();
		}

		public ICompoundFilter createIntersectionFilter(IEventFilter... aFilters)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createLocationFilter(IBehaviorInfo aBehavior, int aBytecodeIndex)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createObjectFilter(ObjectId aId)
		{
			throw new UnsupportedOperationException();
		}

		public IObjectInspector createObjectInspector(ObjectId aObjectId)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createResultFilter(ObjectId aId)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createTargetFilter(ObjectId aId)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createThreadFilter(IThreadInfo aThread)
		{
			throw new UnsupportedOperationException();
		}

		public ICompoundFilter createUnionFilter(IEventFilter... aFilters)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createValueFilter(ObjectId aId)
		{
			throw new UnsupportedOperationException();
		}

		public IVariablesInspector createVariablesInspector(IBehaviorCallEvent aEvent)
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createVariableWriteFilter()
		{
			throw new UnsupportedOperationException();
		}

		public IEventFilter createVariableWriteFilter(LocalVariableInfo aVariable)
		{
			throw new UnsupportedOperationException();
		}

		public <O> O exec(Query<O> aQuery)
		{
			throw new UnsupportedOperationException();
		}

		public IParentEvent getCFlowRoot(IThreadInfo aThread)
		{
			throw new UnsupportedOperationException();
		}

		public ILogEvent getEvent(ExternalPointer aPointer)
		{
			throw new UnsupportedOperationException();
		}

		public long getEventsCount()
		{
			return 0;
		}

		public long getFirstTimestamp()
		{
			return 0;
		}

		public IHostInfo getHost(String aName)
		{
			throw new UnsupportedOperationException();
		}

		public Iterable<IHostInfo> getHosts()
		{
			throw new UnsupportedOperationException();
		}

		public long getLastTimestamp()
		{
			return 0;
		}

		public Object getRegistered(ObjectId aId)
		{
			throw new UnsupportedOperationException();
		}

		public Iterable<IThreadInfo> getThreads()
		{
			return Collections.EMPTY_LIST;
		}

		public IBidiIterator<Long> searchStrings(String aSearchText)
		{
			throw new UnsupportedOperationException();
		}
		
	}
}
