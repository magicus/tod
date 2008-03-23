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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import tod.core.ILogCollector;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IThreadInfo;
import tod.impl.database.structure.standard.ThreadInfo;
import zz.utils.Utils;

/**
 * An abstract log collector. It simply holds a location repository
 * and provides the resolution of exception behavior 
 * (see {@link #exception(int, short, long, byte, int, int, Object)}).
 * If there are multiple debugged hosts there must be multiple collectors.
 * @author gpothier
 */
public abstract class EventCollector implements ILogCollector
{
	/**
	 * The host whose events are sent to this collector.
	 */
	private final IHostInfo itsHost;
	private final IStructureDatabase itsStructureDatabase;
	
	private List<IThreadInfo> itsThreads = new ArrayList<IThreadInfo>();
	private Map<Long, IThreadInfo> itsThreadsMap = new HashMap<Long, IThreadInfo>();
	
	public EventCollector(IHostInfo aHost, IStructureDatabase aStructureDatabase)
	{
		itsHost = aHost;
		itsStructureDatabase = aStructureDatabase;
	}

	/**
	 * Returns the host associated with this collector.
	 */
	public IHostInfo getHost()
	{
		return itsHost;
	}
	
	/**
	 * Returns the {@link IThreadInfo} object that describes the thread
	 * that has the specified JVM thread id.
	 */
	public IThreadInfo getThread(long aJVMThreadId)
	{
		return itsThreadsMap.get(aJVMThreadId);
	}
	
	/**
	 * Returns the thread info corresponding to the given id.
	 */
	public IThreadInfo getThread(int aId)
	{
		return itsThreads.get(aId);
	}
	
	/**
	 * Returns an iterable over all registered threads.
	 */
	public Iterable<IThreadInfo> getThreads()
	{
		return itsThreads;
	}

	public void thread(int aThreadId, long aJVMThreadId, String aName)
	{
		ThreadInfo theThread = createThreadInfo(getHost(), aThreadId, aJVMThreadId, aName);
		Utils.listSet(itsThreads, aThreadId, theThread);
		itsThreadsMap.put(aJVMThreadId, theThread);
		
		thread(theThread);
	}

	/**
	 * Subclasses can override this method is they need to be notified of thread registration
	 * and need the {@link IThreadInfo} object.
	 */
	protected void thread(ThreadInfo aThread)
	{
	}
	
	/**
	 * Instantiates a {@link ThreadInfo} object. Subclasses can override this 
	 * method if they need to instantiate a subclass.
	 */
	protected ThreadInfo createThreadInfo(IHostInfo aHost, int aId, long aJVMId, String aName)
	{
		return new ThreadInfo(aHost, aId, aJVMId, aName);
	}
	
	/**
	 * This method fetches the behavior identified by the supplied names.
	 */
	public void exception(
			int aThreadId,
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			int[] aAdviceCFlow,
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex, Object aException)
	{
		String theClassName;
		try
		{
			theClassName = Type.getType(aMethodDeclaringClassSignature).getClassName();
		}
		catch (Exception e)
		{
			throw new RuntimeException("Bad declaring class signature: "+aMethodDeclaringClassSignature, e);
		}
		
		int theId = itsStructureDatabase.getBehaviorId(theClassName, aMethodName, aMethodSignature);

		exception(
				aThreadId, 
				aParentTimestamp, 
				aDepth, 
				aTimestamp, 
				aAdviceCFlow,
				theId, 
				aOperationBytecodeIndex, 
				aException);
	}

	/**
	 * Same as {@link #exception(int, short, long, byte, int, String, String, String, int, Object)},
	 * with the behavior resolved.
	 */
	protected abstract void exception(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow, 
			int aBehaviorId,
			int aOperationBytecodeIndex,
			Object aException);
}
