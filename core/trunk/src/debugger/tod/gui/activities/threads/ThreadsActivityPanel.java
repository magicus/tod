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
package tod.gui.activities.threads;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import tod.core.database.structure.IThreadInfo;
import tod.gui.IContext;
import tod.gui.IGUIManager;
import tod.gui.activities.ActivityPanel;
import tod.gui.components.eventsequences.SequenceViewsDock;
import tod.gui.components.eventsequences.ThreadSequenceSeed;
import zz.utils.Utils;

/**
 * A view that lets the user select a thread and displays all the events 
 * of this thread.
 * @author gpothier
 */
public class ThreadsActivityPanel extends ActivityPanel<ThreadsSeed>
{
	private SequenceViewsDock itsDock;
	private Map<IThreadInfo, ThreadSequenceSeed> itsSeedsMap = 
		new HashMap<IThreadInfo, ThreadSequenceSeed>();
	
	private JLabel itsEventsCountLabel;
	private JLabel itsDroppedEventsCountLabel;
	private long itsLastEventCount = -1;
	private int itsLastThreadCount = -1;

	
	private Timer itsTimer;
	
	public ThreadsActivityPanel(IContext aContext)
	{
		super(aContext);
		createUI();
	}
	
	@Override
	protected void connectSeed(ThreadsSeed aSeed)
	{
		connect(aSeed.pRangeStart(), itsDock.pStart());
		connect(aSeed.pRangeEnd(), itsDock.pEnd());
		
		itsTimer.start();
		update();
	}

	@Override
	protected void disconnectSeed(ThreadsSeed aSeed)
	{
		itsTimer.stop();

		disconnect(aSeed.pRangeStart(), itsDock.pStart());
		disconnect(aSeed.pRangeEnd(), itsDock.pEnd());
	}

	private void createUI()
	{
		itsDock = new SequenceViewsDock(getGUIManager());
		itsEventsCountLabel = new JLabel();
		itsDroppedEventsCountLabel = new JLabel();
		itsDroppedEventsCountLabel.setForeground(Color.RED);
		
		JPanel theCountsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		theCountsPanel.add(itsEventsCountLabel);
		theCountsPanel.add(itsDroppedEventsCountLabel);
		
		setLayout(new BorderLayout());
		add (theCountsPanel, BorderLayout.NORTH);
		add (itsDock, BorderLayout.CENTER);
		
		itsTimer = new Timer(1000, new ActionListener()
						{
							public void actionPerformed(ActionEvent aE)
							{
								update();
							}
						});
		
	}
	
	private void update()
	{
		long theEventCount = getLogBrowser().getEventsCount();

		List<IThreadInfo> theThreads = new ArrayList<IThreadInfo>();
		Utils.fillCollection(theThreads, getLogBrowser().getThreads());
		int theThreadCount = theThreads.size();
		
		if (theEventCount != itsLastEventCount || theThreadCount != itsLastThreadCount)
		{
			itsLastEventCount = theEventCount;
			itsLastThreadCount = theThreadCount;
			
			itsEventsCountLabel.setText("Events registered: "+theEventCount);
			
			long theDropped = getLogBrowser().getDroppedEventsCount();
			if (theDropped > 0) itsDroppedEventsCountLabel.setText("DROPPED: "+theDropped);
			
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
