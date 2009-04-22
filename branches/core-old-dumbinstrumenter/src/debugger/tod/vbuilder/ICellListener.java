/*
 * Created on Jul 2, 2005
 */
package tod.vbuilder;

import java.util.List;

/**
 * An object that receives events from a {@link tod.vbuilder.Cell}.
 * TODO:
 * See if we can write simple listsner code and use SOM to enforce
 * a two phase execution: listener notification calls go to a queue
 * and are processed only when no other kind of call is in the queue.
 * @author gpothier
 */
public interface ICellListener
{
	public void changed (List<CellEvent> aChangeSet);
}
