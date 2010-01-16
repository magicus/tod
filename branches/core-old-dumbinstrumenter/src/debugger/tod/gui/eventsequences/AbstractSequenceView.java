/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.BrowserData;
import tod.gui.IGUIManager;
import tod.gui.view.LogView;
import zz.csg.api.IDisplay;
import zz.csg.api.IRectangularGraphicObject;
import zz.utils.ItemAction;
import zz.utils.properties.IRWProperty;
import zz.utils.ui.text.XFont;

/**
 * Base class for sequence views. 
 * <li>Handles base actions.
 * <li>Provides helpers for baloons.
 * @author gpothier
 */
public abstract class AbstractSequenceView implements IEventSequenceView
{
	public static final XFont FONT = XFont.DEFAULT_XPLAIN.deriveFont(10);
	
	private MyMural itsMural;
	private final IDisplay itsDisplay;
	private final LogView itsLogView;
	
	private Collection<ItemAction> itsBaseActions;
	
	public AbstractSequenceView(IDisplay aDisplay, LogView aLogView)
	{
		itsDisplay = aDisplay;
		itsLogView = aLogView;
	}

	public LogView getLogView()
	{
		return itsLogView;
	}
	
	public IGUIManager getGUIManager()
	{
		return getLogView().getGUIManager();
	}
	
	/**
	 * Abstract method that lets subclasses provide a {@link IEventBrowser}.
	 */
	protected abstract List<BrowserData> getBrowsers();

	public IRectangularGraphicObject getEventStripe()
	{
		if (itsMural == null) 
		{
			itsMural = new MyMural(itsDisplay);
			for(BrowserData theData : getBrowsers())
			{
				itsMural.pEventBrowsers().add(theData);			
			}
		}
		return itsMural;
	}
	
	public IRWProperty<Long> pStart()
	{
		return itsMural.pStart();
	}
	
	public IRWProperty<Long> pEnd()
	{
		return itsMural.pEnd();
	}

	/**
	 * Adds an action that will always be available,
	 * ie. it will always be returned by {@link #getActions() }.
	 */
	public void addBaseAction (ItemAction aAction)
	{
		if (itsBaseActions == null) itsBaseActions = new ArrayList<ItemAction>();
		itsBaseActions.add(aAction);
	}
	
	/**
	 * Adds actions that will always be available,
	 * ie. they will always be returned by {@link #getActions() }.
	 */
	public void addBaseActions (Collection<ItemAction> aAction)
	{
		if (itsBaseActions == null) itsBaseActions = new ArrayList<ItemAction>();
		itsBaseActions.addAll(aAction);
	}
	
	/**
	 * Subclasses that override this method should call super and 
	 * add items to the returned collection.
	 */
	public Collection<ItemAction> getActions()
	{
		Collection<ItemAction> theActions = new ArrayList<ItemAction>();
		if (itsBaseActions != null) theActions.addAll(itsBaseActions);
		return theActions;
	}

	public Image getIcon()
	{
		return null;
	}

	/**
	 * Hook for subclasses to provide baloons. By returning a graphic object
	 * for some events a subclass can cause the mural to display baloons at the 
	 * right position.
	 * @see EventMural#getBaloon(ILogEvent)
	 */
	protected IRectangularGraphicObject getBaloon(ILogEvent aEvent)
	{
		return null;
	}

	private class MyMural extends EventMural
	{
		public MyMural(IDisplay aDisplay)
		{
			super (aDisplay);
		}
		
		@Override
		protected IRectangularGraphicObject getBaloon(ILogEvent aEvent)
		{
			return AbstractSequenceView.this.getBaloon(aEvent);
		}
	}
}