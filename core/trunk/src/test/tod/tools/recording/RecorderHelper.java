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
package tod.tools.recording;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import tod.core.database.browser.ICompoundInspector;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ExternalPointer;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.core.session.ISession;
import zz.utils.Utils;

public class RecorderHelper
{
	private static RecorderHelper INSTANCE = new RecorderHelper();

	public static RecorderHelper getInstance()
	{
		return INSTANCE;
	}

	private ObjectOutputStream out;

	private RecorderHelper()
	{
		try
		{
			out = new ObjectOutputStream(new FileOutputStream("rec.bin"));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private Map<Object, Integer> itsObjectIdsMap = new IdentityHashMap<Object, Integer>();
	private Map<Thread, Integer> itsThreadIdsMap = new IdentityHashMap<Thread, Integer>();
	private int itsLastObjectId = 1;
	private int itsLastThreadId = 1;

	private synchronized int nextObjectId()
	{
		return itsLastObjectId++;
	}
	
	private synchronized int nextThreadId()
	{
		return itsLastThreadId++;
	}
	
	private int getObjectId(Object aObject, boolean aAllowNewId)
	{
		Integer theId = itsObjectIdsMap.get(aObject);
		if (theId == null)
		{
			theId = nextObjectId();
			if (! aAllowNewId && theId != 1) 
			{
				throw new RuntimeException("Object has no id: "+aObject);
			}
			itsObjectIdsMap.put(aObject, theId);
		}
		
		return theId;
	}
	
	/**
	 * Checks that the given object has an id.
	 * Only for debugging.
	 */
	public void checkId(Object aObject)
	{
		if (! itsObjectIdsMap.containsKey(aObject)) throw new RuntimeException("Not id: "+aObject);
	}
	
	private int getThreadId(Thread aThread)
	{
		Integer theId = itsThreadIdsMap.get(aThread);
		if (theId == null)
		{
			theId = nextThreadId();
			itsThreadIdsMap.put(aThread, theId);
		}
		
		return theId;
	}
	
	/**
	 * Indicates if the given object should be recorded by id.
	 */
	private boolean isRecorded(Object aObject)
	{
		return (aObject instanceof ILogBrowser)
			|| (aObject instanceof IEventBrowser)
			|| (aObject instanceof IStructureDatabase)
			|| (aObject instanceof ILocationInfo)
			|| (aObject instanceof IEventFilter)
			|| (aObject instanceof IThreadInfo)
			|| (aObject instanceof ILogEvent)
			|| (aObject instanceof ProbeInfo)
			|| (aObject instanceof ICompoundInspector)
			|| (aObject instanceof ICompoundInspector.EntryValue)
			|| (aObject instanceof ExternalPointer)
//			|| ("tod.impl.dbgrid.event.BehaviorCallEvent$CallInfoBuilder".equals(aObject.getClass().getName()))
//			|| ("tod.impl.dbgrid.event.BehaviorCallEvent$CallInfo".equals(aObject.getClass().getName()))
			;
	}
	
	private boolean isIgnored(Object aObject)
	{
		return (aObject instanceof ISession);
	}
	
	private Object[] transformArray(Object[] aArray, boolean aAllowNewId)
	{
		Object[] theResult = new Object[aArray.length];
		for (int i=0;i<theResult.length;i++)
		{
			theResult[i] = transform(aArray[i], aAllowNewId);
		}
		
		return theResult;
	}
	
	private Object transform(Object aObject, boolean aAllowNewId)
	{
		if (aObject == null) return null;
		else if (isIgnored(aObject)) return null;
		else if (aObject.getClass().isArray()) 
		{
			if (aObject.getClass().getComponentType().isPrimitive()) return aObject;
			else return transformArray((Object[]) aObject, aAllowNewId);
		}
		else if (aObject instanceof Iterable)
		{
			Iterable theIterable = (Iterable) aObject;
			List theList = new ArrayList();
			Utils.fillCollection(theList, theIterable);
			return transformArray(theList.toArray(), aAllowNewId);
		}
		else if (isRecorded(aObject)) return new Record.ProxyObject(getObjectId(aObject, aAllowNewId));
		else return aObject;
	}
	
	private synchronized void write(Record aRecord)
	{
		try
		{
			out.writeObject(aRecord);
			out.flush();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void recordCall(
			Object aTarget, 
			String aMethod,
			Class[] aFormalsType,
			Object[] aArgs, 
			Object aReturn,
			String aLocation)
	{
		Record theRecord = new Record.Call(
				getThreadId(Thread.currentThread()),
				new Record.ProxyObject(getObjectId(aTarget, false)),
				new Record.MethodSignature(aMethod, aFormalsType),
				transformArray(aArgs, false),
				transform(aReturn, true),
				aLocation);
		
		write(theRecord);
	}
	
	public void recordNew(
			String aMethod,
			Class[] aFormalsType,
			Object[] aArgs, 
			Object aReturn,
			String aLocation)
	{
		Record theRecord = new Record.New(
				getThreadId(Thread.currentThread()),
				new Record.MethodSignature(aMethod, aFormalsType),
				transformArray(aArgs, false),
				transform(aReturn, true),
				aLocation);
		
		write(theRecord);
	}
}