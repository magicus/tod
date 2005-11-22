/*
 * Created on Nov 18, 2004
 */
package tod.core.model.trace;

import tod.core.model.event.ILogEvent;
import tod.core.model.structure.IThreadInfo;
import zz.utils.tree.ITree;

/**
 * Permits to determine control flow information of a given thread, 
 * providing a tree view of the events.
 * @author gpothier
 */
public interface ICFlowBrowser extends ITree<ILogEvent, ILogEvent>
{
	/**
	 * Returns the thread considered by this browser.
	 */
	public IThreadInfo getThread();
}
