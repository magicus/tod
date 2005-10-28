/*
 * Created on Oct 26, 2005
 */
package tod.core.model.trace;

import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.structure.TypeInfo;

/**
 * Permits to obtain the location info objects that have been registered during a 
 * {@link tod.session.ISession}
 * @author gpothier
 */
public interface ILocationTrace
{
	/**
	 * Returns the type object that corresponds to the given name.
	 */
	public TypeInfo getType(String aName);
	
	/**
	 * Returns all registered types.
	 */
	public Iterable<TypeInfo> getTypes();
	
	/**
	 * Returns all registered behaviours.
	 */
	public Iterable<BehaviorInfo> getBehaviours();
	
	/**
	 * Returns all registered fields.
	 */
	public Iterable<FieldInfo> getFields();
	
	/**
	 * Returns all registered files.
	 */
	public Iterable<String> getFiles();
	
	/**
	 * Returns all registered threads.
	 */
	public Iterable<ThreadInfo> getThreads();
}
