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
package tod.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import tod.core.IBookmarks;
import tod.core.IBookmarks.Bookmark;
import tod.core.IBookmarks.EventBookmark;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.IGUIManager;
import tod.gui.components.eventsequences.GlobalSequenceSeed;
import tod.gui.components.eventsequences.SequenceViewsDock;
import tod.gui.components.eventsequences.mural.IBalloonProvider;
import tod.gui.kit.Bus;
import tod.gui.kit.IBusListener;
import tod.gui.kit.Options;
import tod.gui.kit.StdOptions;
import tod.gui.kit.html.HtmlUtils;
import tod.gui.kit.messages.EventSelectedMsg;
import tod.gui.kit.messages.ShowCFlowMsg;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.notification.IFireableEvent;
import zz.utils.notification.SimpleEvent;

/**
 * This panel displays the bookmarks of an {@link IBookmarks},
 * and lets 
 * @author gpothier
 */
public class BookmarksMural extends JPanel
{
	private final IGUIManager itsGUIManager;
	private final ILogBrowser itsLogBrowser;
	private final IBookmarks itsBookmarks;
	
	private final IEventListener<Void> itsBookmarksListener = new IEventListener<Void>()
	{
		public void fired(IEvent< ? extends Void> aEvent, Void aData)
		{
			eChanged.fire(null);
		}
	};
	
	private ILogEvent itsCurrentEvent;
	
	private SequenceViewsDock itsDock;
	
	private final IFireableEvent<Void> eChanged = new SimpleEvent<Void>();
	
	public BookmarksMural(IGUIManager aGUIManager, ILogBrowser aLogBrowser, IBookmarks aBookmarks)
	{
		itsGUIManager = aGUIManager;
		itsLogBrowser = aLogBrowser;
		itsBookmarks = aBookmarks;

		createUI();
	}
	
	private void createUI()
	{
		itsDock = new SequenceViewsDock(itsGUIManager);
		itsDock.setPreferredStripeHeight(30);
		itsDock.setShowStripeTitle(false);

		setLayout(new BorderLayout());
		add (itsDock, BorderLayout.CENTER);

		itsDock.pSeeds().add(new GlobalSequenceSeed(itsLogBrowser));
		itsDock.getMural(0).setBalloonProvider(new BookmarksBalloonProvider());
	}
	
	public void setCurrentEvent(ILogEvent aCurrentEvent)
	{
		itsCurrentEvent = aCurrentEvent;
		eChanged.fire(null);
	}
	
	/**
	 * Translates bookmarks to balloons.
	 * @author gpothier
	 */
	private class BookmarksBalloonProvider implements IBalloonProvider
	{
		public List<Balloon> getBaloons(long aStartTimestamp, long aEndTimestamp)
		{
			List<Balloon> theResult = new ArrayList<Balloon>();
			
			Iterable<Bookmark> theBookmarks = itsBookmarks.getBookmarks();
			for (Bookmark theBookmark : theBookmarks)
			{
				if (theBookmark instanceof EventBookmark)
				{
					EventBookmark theEventBookmark = (EventBookmark) theBookmark;
					ILogEvent theEvent = theEventBookmark.getItem();
					if (theEvent.equals(itsCurrentEvent)) continue;
					
					long theTimestamp = theEvent.getTimestamp();
					if (theTimestamp < aStartTimestamp || theTimestamp > aEndTimestamp) continue;
					
					String theText = theEventBookmark.name;
					if (theText == null) theText = theEvent.toString();
					
					Color theColor = theEventBookmark.color;
					if (theColor == null) theColor = Color.BLACK;
					
					Balloon theBalloon = new Balloon(
							theEvent,
							"<span style='color: "+HtmlUtils.toString(theColor)+"'>"+theText+"</span>");
					
					theResult.add(theBalloon);
				}
			}
			
			if (itsCurrentEvent != null)
			{
				long theTimestamp = itsCurrentEvent.getTimestamp();
				if (theTimestamp >= aStartTimestamp && theTimestamp <= aEndTimestamp) 
				{
					Balloon theBalloon = new Balloon(itsCurrentEvent, "<span style='color: blue'>X</span>");
					theResult.add(theBalloon);
				}
			}
			
			Collections.sort(theResult, IBalloonProvider.COMPARATOR);
			return theResult;
		}

		public IEvent<Void> eChanged()
		{
			return eChanged;
		}
		
		
	}
}
