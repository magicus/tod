/*
 * Created on Oct 19, 2006
 */
package tod.impl.dbgrid.dbnode;

import tod.impl.dbgrid.GridMaster;

/**
 * This exception is thrown in {@link GridMaster#registerNode(RIDatabaseNode)}
 * if the node cannot be accepted by the master.
 * @author gpothier
 */
public class NodeRejectedException extends Exception
{
	private static final long serialVersionUID = 812840247790592220L;

	public NodeRejectedException()
	{
	}

	public NodeRejectedException(String aMessage)
	{
		super(aMessage);
	}
}
