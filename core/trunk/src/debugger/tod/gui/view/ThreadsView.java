/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.gui.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.Timer;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IThreadInfo;
import tod.gui.IGUIManager;
import tod.gui.eventsequences.SequenceViewsDock;
import tod.gui.eventsequences.ThreadSequenceSeed;
import tod.gui.seed.ThreadsSeed;

/**
 * A view that lets the user select a thread and displays all the events 
 * of this thread.
 * @author gpothier
 */
public class ThreadsView extends LogView
{
	private ThreadsSeed itsSeed;
	
	private SequenceViewsDock itsDock;
	private Map<IThreadInfo, ThreadSequenceSeed> itsSeedsMap = new HashMap<IThreadInfo, ThreadSequenceSeed>();
	
	private JLabel itsEventsCountLabel;
	private long itsLastEventCount = -1;

	private Timer itsTimer;
	
	public ThreadsView(IGUIManager aGUIManager, ILogBrowser aLog, ThreadsSeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
		
		createUI();
	}
	
	private void createUI()
	{
		itsDock = new SequenceViewsDock(this);
		itsEventsCountLabel = new JLabel();
		
		setLayout(new BorderLayout());
		add (itsDock, BorderLayout.CENTER);
		add (itsEventsCountLabel, BorderLayout.NORTH);
		
		update();
		
		itsTimer = new Timer(500, new ActionListener()
						{
							public void actionPerformed(ActionEvent aE)
							{
								update();
							}
						});
		itsTimer.start();
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsTimer.stop();
	}

	private void update()
	{
		long theCount = getLogBrowser().getEventsCount();
		
		if (theCount != itsLastEventCount)
		{
			itsLastEventCount = theCount;
			
			itsEventsCountLabel.setText("Events registered: "+theCount);
			
			for (IThreadInfo theThread : getLogBrowser().getThreads())
			{
				ThreadSequenceSeed theSeed = itsSeedsMap.get(theThread);
				if (theSeed == null)
				{
					theSeed = new ThreadSequenceSeed(getLogBrowser(), theThread);
					itsSeedsMap.put(theThread, theSeed);
					
					itsDock.pSeeds().add(theSeed);
				}
			}
			
			itsDock.pStart().set(getLogBrowser().getFirstTimestamp());
			itsDock.pEnd().set(getLogBrowser().getLastTimestamp());
		}
	}
}
