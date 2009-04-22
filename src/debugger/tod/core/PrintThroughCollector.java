/*
 * Created on Oct 23, 2005
 */
package tod.core;


/**
 * A collector that prints events and send them to another collector.
 * @author gpothier
 */
public class PrintThroughCollector extends MultiCollector
{
	public PrintThroughCollector(ILogCollector aCollector)
	{
        super (aCollector, new PrintLogCollector());
	}
}
