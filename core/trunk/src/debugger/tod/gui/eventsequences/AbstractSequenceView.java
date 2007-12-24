/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui.eventsequences;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.BrowserData;
import tod.gui.IGUIManager;
import zz.utils.ItemAction;
import zz.utils.properties.IRWProperty;
import zz.utils.ui.Orientation;
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
	private final IGUIManager itsGUIManager;
	
	private Collection<ItemAction> itsBaseActions;
	
	public AbstractSequenceView(IGUIManager aManager)
	{
		itsGUIManager = aManager;
	}

	public IGUIManager getGUIManager()
	{
		return itsGUIManager;
	}
	
	public ILogBrowser getLogBrowser()
	{
		return getGUIManager().getSession().getLogBrowser();
	}

	/**
	 * Same role as {@link Component#addNotify()}
	 */
	public void addNotify()
	{
	}
	
	/**
	 * Same role as {@link Component#removeNotify()}
	 */
	public void removeNotify()
	{
	}


	/**
	 * Abstract method that lets subclasses provide a {@link IEventBrowser}.
	 */
	protected abstract List<BrowserData> getBrowsers();
	
	/**
	 * Update the mural to reflect changes in the borwsers.
	 */
	protected void update()
	{
		itsMural.pEventBrowsers().clear();
		for(BrowserData theData : getBrowsers())
		{
			itsMural.pEventBrowsers().add(theData);			
		}
		
	}

	public JComponent getEventStripe()
	{
		if (itsMural == null) 
		{
			itsMural = new MyMural();
			itsMural.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent aE)
				{
					muralClicked();
				}
			});
			update();
		}
		return itsMural;
	}

	/**
	 * Called when the mural is clicked, does nothing by default
	 */
	protected void muralClicked()
	{
	}
	
	/**
	 * Sets the mouse cursor shape to use for the mural.
	 */
	protected void setMuralCursor(Cursor aCursor)
	{
		getEventStripe().setCursor(aCursor);
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
	protected JComponent getBaloon(ILogEvent aEvent)
	{
		return null;
	}

	private class MyMural extends BaloonMural
	{
		private MyMural()
		{
			super(Orientation.HORIZONTAL);
		}

		@Override
		protected JComponent getBaloon(ILogEvent aEvent)
		{
			return AbstractSequenceView.this.getBaloon(aEvent);
		}
		
		@Override
		public void addNotify()
		{
			super.addNotify();
			AbstractSequenceView.this.addNotify();
		}
		
		@Override
		public void removeNotify()
		{
			super.removeNotify();
			AbstractSequenceView.this.removeNotify();
		}
	}
}
