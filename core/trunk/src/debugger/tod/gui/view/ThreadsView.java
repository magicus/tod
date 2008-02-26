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
package tod.gui.view;

import infovis.panel.dqinter.DoubleRangeSlider;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IThreadInfo;
import tod.gui.IGUIManager;
import tod.gui.eventsequences.SequenceViewsDock;
import tod.gui.eventsequences.ThreadSequenceSeed;
import tod.gui.seed.ThreadsSeed;
import zz.utils.Utils;
import zz.utils.properties.PropertyUtils;

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
		itsDock = new SequenceViewsDock(getGUIManager());
		itsEventsCountLabel = new JLabel();
		
		setLayout(new BorderLayout());
		add (itsEventsCountLabel, BorderLayout.NORTH);
		add (itsDock, BorderLayout.CENTER);
		
		update();
		
		itsTimer = new Timer(1000, new ActionListener()
						{
							public void actionPerformed(ActionEvent aE)
							{
								update();
							}
						});
		itsTimer.start();
		
		PropertyUtils.connect(itsSeed.pRangeStart(), itsDock.pStart(), true);
		PropertyUtils.connect(itsSeed.pRangeEnd(), itsDock.pEnd(), true);
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
			
			List<IThreadInfo> theThreads = new ArrayList<IThreadInfo>();
			Utils.fillCollection(theThreads, getLogBrowser().getThreads());
			Collections.sort(theThreads, IThreadInfo.ThreadIdComparator.getInstance());
			
			for (IThreadInfo theThread : theThreads)
			{
				ThreadSequenceSeed theSeed = itsSeedsMap.get(theThread);
				if (theSeed == null)
				{
					theSeed = new ThreadSequenceSeed(getLogBrowser(), theThread);
					itsSeedsMap.put(theThread, theSeed);
					
					itsDock.pSeeds().add(theSeed);
				}
			}
		}
	}
	
}
