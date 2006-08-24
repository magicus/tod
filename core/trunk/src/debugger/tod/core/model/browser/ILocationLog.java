/*
 * Created on Oct 26, 2005
 */
package tod.core.model.browser;

import tod.core.model.structure.IBehaviorInfo;
import tod.core.model.structure.IClassInfo;
import tod.core.model.structure.IFieldInfo;
import tod.core.model.structure.IThreadInfo;
import tod.core.model.structure.ITypeInfo;
import tod.core.model.structure.ThreadInfo;

/**
 * Permits to obtain the location info objects that have been registered during a 
 * {@link tod.session.ISession}
 * @author gpothier
 */
public interface ILocationLog
{
	/**
	 * Returns the type object that corresponds to the given name.
	 */
	public ITypeInfo getType(String aName);
	
	/**
	 * Returns the thread info for a specific thread id.
	 */
	public ThreadInfo getThread (long aId);
	
	/**
	 * Returns all registered types.
	 */
	public Iterable<IClassInfo> getClasses();
	
	/**
	 * Returns all registered behaviours.
	 */
	public Iterable<IBehaviorInfo> getBehaviours();
	
	/**
	 * Returns all registered fields.
	 */
	public Iterable<IFieldInfo> getFields();
	
	/**
	 * Returns all registered files.
	 */
	public Iterable<String> getFiles();
	
	/**
	 * Returns all registered threads.
	 */
	public Iterable<IThreadInfo> getThreads();
}
