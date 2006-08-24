/*
 * Created on Nov 10, 2004
 */
package tod.gui.seed;

import tod.core.database.browser.ILogBrowser;
import tod.gui.IGUIManager;
import tod.gui.view.LogView;

/**
 * A seed contains all the information needed to generate a view.
 * A seed can be in two states: inactive and active. When a seed
 * is active, it maintains a reference to a view component.
 * As many seeds can be stored in the browsing history of the GUI,
 * an inactive seed should keep as few references as possible to other
 * objects, so as to free resources. 
 * @author gpothier
 */
public abstract class Seed/*<T extends LogView>*/
{
	private IGUIManager itsGUIManager;
	private ILogBrowser itsLog;
	
	private /*T*/LogView itsComponent;
	
	public Seed(IGUIManager aGUIManager, ILogBrowser aLog)
	{
		itsGUIManager = aGUIManager;
		itsLog = aLog;
	}
	
	protected ILogBrowser getEventTrace()
	{
		return itsLog;
	}
	
	protected IGUIManager getGUIManager()
	{
		return itsGUIManager;
	}
	
	/**
	 * Creates the view component for this seed.
	 * This method is called when the seed becomes 
	 * active.
	 */
	protected abstract /*T*/LogView requestComponent ();
	
	/**
	 * Releases a component previously created by
	 * {@link #requestComponent()}. This method is called
	 * when the seed becomes inactive.
	 * The default implementation does nothing.
	 */
	protected void releaseComponent (/*T*/LogView aComponent)
	{
	}
	
	/**
	 * Returns the view component for this seed.
	 * It is not legal to call this method if the seed is inactive.
	 */
	public /*T*/LogView getComponent()
	{
		return itsComponent;
	}
	
	/**
	 * Activates this seed.
	 * An active seed can provide a view component.
	 */
	public void activate ()
	{
		itsComponent = requestComponent();
	}
	
	/**
	 * Deactivates this seed, freeing as much resources as possible.
	 */
	public void deactivate ()
	{
		releaseComponent(itsComponent);
		itsComponent = null;
	}
}
