/*
 * Created on Nov 17, 2004
 */
package reflex.lib.logging.miner.gui.seed;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.model.trace.IEventTrace;

/**
 * This seed permits to generate a view that reconstitutes an
 * object's fields values.
 * @author gpothier
 */
public class ObjectSimulationSeed extends Seed
{
	private Object itsObject;
	
	public ObjectSimulationSeed(IGUIManager aGUIManager, IEventTrace aLog, Object aObject)
	{
		super(aGUIManager, aLog);
		itsObject = aObject;
	}

	protected LogView requestComponent()
	{
		return null;
	}
}
