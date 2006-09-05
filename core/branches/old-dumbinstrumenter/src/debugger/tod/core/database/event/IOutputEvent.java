/*
 * Created on Nov 15, 2004
 */
package tod.core.database.event;

import tod.core.Output;

/**
 * @author gpothier
 */
public interface IOutputEvent extends ICallerSideEvent
{
	public String getData();
	public Output getOutput();
}
