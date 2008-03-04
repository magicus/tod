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
