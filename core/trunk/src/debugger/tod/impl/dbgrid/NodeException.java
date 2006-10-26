/*
 * Created on Oct 25, 2006
 */
package tod.impl.dbgrid;

import tod.impl.dbgrid.dbnode.DatabaseNode;

/**
 * Wraps an exception that occurred in a {@link DatabaseNode}
 * @author gpothier
 */
public class NodeException extends RuntimeException
{
	private static final long serialVersionUID = -2467881614217337652L;
	private int itsNodeId;

	public NodeException(int aNodeId, Throwable aCause)
	{
		super("Exception occurred in node "+aNodeId, aCause);
		itsNodeId = aNodeId;
	}
}
