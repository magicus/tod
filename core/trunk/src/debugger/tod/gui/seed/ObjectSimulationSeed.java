/*
 * Created on Nov 17, 2004
 */
package tod.gui.seed;

import tod.core.model.browser.ILogBrowser;
import tod.gui.IGUIManager;
import tod.gui.view.LogView;

/**
 * This seed permits to generate a view that reconstitutes an
 * object's fields values.
 * @author gpothier
 */
public class ObjectSimulationSeed extends Seed
{
	private Object itsObject;
	
	public ObjectSimulationSeed(IGUIManager aGUIManager, ILogBrowser aLog, Object aObject)
	{
		super(aGUIManager, aLog);
		itsObject = aObject;
	}

	protected LogView requestComponent()
	{
		return null;
	}
}
