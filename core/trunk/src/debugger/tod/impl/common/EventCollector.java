/*
 * Created on Jul 20, 2006
 */
package tod.impl.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import tod.core.ILogCollector;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ThreadInfo;
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
	private IHostInfo itsHost;
	
	private ILocationsRepository itsLocationsRepository;
	
	private List<IThreadInfo> itsThreads = new ArrayList<IThreadInfo>();
	private Map<Long, IThreadInfo> itsThreadsMap = new HashMap<Long, IThreadInfo>();
	
	public EventCollector(IHostInfo aHost, ILocationsRepository aLocationsRepository)
	{
		itsHost = aHost;
		itsLocationsRepository = aLocationsRepository;
	}

	public ILocationsRepository getLocationsRepository()
	{
		return itsLocationsRepository;
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
	
	public void registerString(long aObjectUID, String aString)
	{
		//TODO: implement
	}
	
	/**
	 * This method fetches the behavior identified by the supplied names.
	 */
	public void exception(
			int aThreadId,
			long aParentTimestamp, 
			short aDepth, 
			long aTimestamp,
			String aMethodName,
			String aMethodSignature,
			String aMethodDeclaringClassSignature,
			int aOperationBytecodeIndex,
			Object aException)
	{
		String theClassName = Type.getType(aMethodDeclaringClassSignature).getClassName();
		ITypeInfo theType = itsLocationsRepository.getType(theClassName);
		
		if (theType instanceof IClassInfo)
		{
			IClassInfo theClass = (IClassInfo) theType;
			
			ITypeInfo[] theArgumentTypes = itsLocationsRepository.getArgumentTypes(aMethodSignature);
			IBehaviorInfo theBehavior = theClass.getBehavior(aMethodName, theArgumentTypes);

			if (theBehavior == null) return; //TODO: don't do that...
			
			exception(
					aThreadId, 
					aParentTimestamp, 
					aDepth, 
					aTimestamp, 
					theBehavior.getId(), 
					aOperationBytecodeIndex, 
					aException);
		}
	}

	/**
	 * Same as {@link #exception(int, short, long, byte, String, String, String, int, Object)},
	 * with the behavior resolved.
	 */
	protected abstract void exception(
			int aThreadId, 
			long aParentTimestamp, 
			short aDepth,
			long aTimestamp, 
			int aBehaviorId,
			int aOperationBytecodeIndex,
			Object aException);
}
