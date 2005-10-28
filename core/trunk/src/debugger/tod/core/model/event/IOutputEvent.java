/*
 * Created on Nov 15, 2004
 */
package tod.core.model.event;

import tod.core.Output;

/**
 * @author gpothier
 */
public interface IOutputEvent extends ILogEvent
{
	public String getData();
	public Output getOutput();
}
