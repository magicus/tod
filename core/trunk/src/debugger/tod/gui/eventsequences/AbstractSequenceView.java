/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
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

	public long getFirstTimestamp()
	{
		long theFirst = Long.MAX_VALUE;
		for (BrowserData theData : getBrowsers())
		{
			theFirst = Math.min(theFirst, theData.getBrowser().getFirstTimestamp());
		}
		return theFirst;
	}

	public long getLastTimestamp()
	{
		long theLast = 0;
		for (BrowserData theData : getBrowsers())
		{
			theLast = Math.max(theLast, theData.getBrowser().getLastTimestamp());
		}
		return theLast;
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
