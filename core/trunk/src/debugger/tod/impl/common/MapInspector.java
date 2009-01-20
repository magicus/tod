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
package tod.impl.common;

import java.util.ArrayList;
import java.util.List;

import tod.core.config.TODConfig;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.browser.IObjectInspector.IEntryInfo;
import tod.core.database.event.ICreationEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.INewArrayEvent;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.tools.interpreter.TODInterpreter;
import tod.tools.interpreter.TODInterpreter.TODInstance;
import zz.jinterp.JBehavior;
import zz.jinterp.JClass;
import zz.jinterp.JInstance;
import zz.jinterp.JObject;
import zz.jinterp.JPrimitive.JInt;

public class MapInspector implements IObjectInspector
{
	private final IObjectInspector itsOriginal;

	private final TODInterpreter itsInterpreter;
	
	private final JClass itsMapClass;
	private final JClass itsMapEntryClass;
	private final JClass itsSetClass;
	private final JClass itsIteratorClass;
	
	private final JBehavior itsNextMethod;
	private final JBehavior itsGetKeyMethod;
	private final JBehavior itsGetValueMethod;
	
	private final TODInstance itsInstance;
	
	private int itsMapSize = -1;
	private JInstance itsIterator;
	private List<EntryInfo> itsEntries = new ArrayList<EntryInfo>();
	
	public MapInspector(IObjectInspector aOriginal)
	{
		itsOriginal = aOriginal;
		
		TODConfig theConfig = getLogBrowser().getSession().getConfig();
		itsInterpreter = new TODInterpreter(theConfig, getLogBrowser());

		itsMapClass = itsInterpreter.getClass("java/util/Map");
		itsMapEntryClass = itsInterpreter.getClass("java/util/Map$Entry");
		itsSetClass = itsInterpreter.getClass("java/util/Set");
		itsIteratorClass = itsInterpreter.getClass("java/util/Iterator");
		
		itsNextMethod = itsIteratorClass.getVirtualBehavior("next", "()Ljava/lang/Object;");
		itsGetKeyMethod = itsMapEntryClass.getVirtualBehavior("getKey", "()Ljava/lang/Object;");
		itsGetValueMethod = itsMapEntryClass.getVirtualBehavior("getValue", "()Ljava/lang/Object;");
		
		itsInstance = itsInterpreter.newInstance(itsMapClass, itsOriginal);
	}
	
	public ILogBrowser getLogBrowser()
	{
		return itsOriginal.getLogBrowser();
	}

	public ObjectId getObject()
	{
		return itsOriginal.getObject();
	}

	public ILogEvent getReferenceEvent()
	{
		return itsOriginal.getReferenceEvent();
	}

	public void setReferenceEvent(ILogEvent aEvent)
	{
		itsOriginal.setReferenceEvent(aEvent);
	}

	public ITypeInfo getType()
	{
		return itsOriginal.getType();
	}

	public ICreationEvent getCreationEvent()
	{
		return itsOriginal.getCreationEvent();
	}

	public int getEntryCount()
	{
		if (itsMapSize == -1)
		{
			JBehavior theBehavior = itsMapClass.getVirtualBehavior("size", "()I");
			JInt theResult = (JInt) theBehavior.invoke(null, itsInstance, new JObject[] {});
			itsMapSize = theResult.v;
		}
		
		return itsMapSize;
	}

	private void ensureEntriesAvailable(int aCount)
	{
		getEntryCount(); // Ensure field count is computed
		assert aCount <= itsMapSize: aCount;
		
		if (itsIterator == null)
		{
			JBehavior theEntrySetBehavior = itsMapClass.getVirtualBehavior("entrySet", "()Ljava/util/Set;");
			JInstance theEntrySet = (JInstance) theEntrySetBehavior.invoke(null, itsInstance, new JObject[] {});
			JBehavior theIteratorBehavior = itsSetClass.getVirtualBehavior("iterator", "()Ljava/util/Iterator;");
			itsIterator = (JInstance) theIteratorBehavior.invoke(null, theEntrySet, new JObject[] {});
		}
		
		while(itsEntries.size() < aCount)
		{
			JInstance theEntry = (JInstance) itsNextMethod.invoke(null, itsIterator, new JObject[] {});
			itsEntries.add(new EntryInfo(theEntry));
		}
	}
	
	public List<IEntryInfo> getEntries(int aRangeStart, int aRangeSize)
	{
		ensureEntriesAvailable(aRangeStart+aRangeSize);

		List<IEntryInfo> theResult = new ArrayList<IEntryInfo>();
			
		for(int i=aRangeStart;i<Math.min(aRangeStart+aRangeSize, getEntryCount());i++)
		{
			theResult.add(itsEntries.get(i));
		}
		
		

		return theResult;
	}

	public IEventBrowser getBrowser(IEntryInfo aEntry)
	{
		return null;
	}

	public EntryValue[] getEntryValue(IEntryInfo aEntry)
	{
		return null;
	}

	public EntryValue[] nextEntryValue(IEntryInfo aEntry)
	{
		return null;
	}

	public EntryValue[] previousEntryValue(IEntryInfo aEntry)
	{
		return null;
	}

	private class EntryInfo implements IEntryInfo
	{
		private final JInstance itsEntry;
		private final JInstance itsKey;
		private final JInstance itsValue;
		
		public EntryInfo(JInstance aEntry)
		{
			itsEntry = aEntry;
			itsKey = (JInstance) itsGetKeyMethod.invoke(null, itsEntry, new JObject[] {});
			itsValue = (JInstance) itsGetValueMethod.invoke(null, itsEntry, new JObject[] {});
		}

		public String getName()
		{
			return null;
		}
	}
}
