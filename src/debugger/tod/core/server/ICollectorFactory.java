/*
 * Created on Aug 25, 2006
 */
package tod.core.server;

import tod.core.ILogCollector;

/**
 * A factory of {@link ILogCollector}. As there must be one collector
 * per host, the server must have a factory of collectors.
 * @author gpothier
 */
public interface ICollectorFactory
{
	public ILogCollector create();
	
	/**
	 * Flushes all the collectors created by this factory. 
	 * This method is called when the target VM terminates.
	 */
	public void flushAll();
}
