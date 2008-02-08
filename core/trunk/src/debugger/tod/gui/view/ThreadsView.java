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

	//slider with double handles defining the timestamp range in the murals
	private TimestampRangeSlider itsTimestampSlider;
	
	
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
		itsTimestampSlider = new TimestampRangeSlider();
		Box theTopBox = Box.createHorizontalBox();
		theTopBox.add(itsEventsCountLabel);
		theTopBox.add(Box.createHorizontalGlue());
		theTopBox.add(itsTimestampSlider);
		theTopBox.add(Box.createHorizontalGlue());
		
		setLayout(new BorderLayout());
		add (theTopBox, BorderLayout.NORTH);
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
			//TODO check why not working
			//itsDock.pStart().set(itsTimestampSlider.itsStartRangeTimestamp);
			itsDock.pStart().set(itsTimestampSlider.itsStartTimestamp);
			itsDock.pEnd().set(itsTimestampSlider.itsEndRangeTimestamp);
			itsTimestampSlider.updateLastLabel();
		}
	}
	
	/**
	 * shows a range slider for timestamp 
	 * it displays the fist and last timestamps above the slider 
	 * and the first and last timestamps of the chosen range below the slider 
	 * @author omotelet
	 */
	private class TimestampRangeSlider extends JPanel implements ChangeListener{

		private DoubleRangeSlider itsSlider;
		private JLabel itsStartLabel = new JLabel(), itsEndLabel= new JLabel(), 
						itsStartRangeLabel= new JLabel(), itsEndRangeLabel= new JLabel();
		private long itsStartTimestamp, itsEndTimestamp, itsStartRangeTimestamp, itsEndRangeTimestamp;
		
		public TimestampRangeSlider()
		{
			itsSlider = new DoubleRangeSlider(0, 1, 0, 1){
				@Override
				public String getToolTipText()
				{
					return "modify the range in the slider in order to zoom in or out in the event murals";
				} 
			};
			
			itsSlider.getModel().addChangeListener(this);
	
			itsStartLabel.setText(0+"ms");
			updateLastLabel();
			updateRangeLabels();

			initLayout();
		}
		
		private void updateRangeLabels()
		{
			double theLow = itsSlider.getLowValue()*(itsEndTimestamp-itsStartTimestamp);
			itsStartRangeTimestamp=(long) (itsStartTimestamp + theLow);
			double theHigh = itsSlider.getHighValue()*(itsEndTimestamp-itsStartTimestamp);
			itsEndRangeTimestamp=(long) (itsStartTimestamp + theHigh);
			
			itsStartRangeLabel.setText((int)(theLow/1000000)+"ms <");
			itsEndRangeLabel.setText("< "+(int)(theHigh/1000000)+"ms");
		}
		
		private void updateLastLabel(){
			boolean rangeNeedUpdate = itsEndTimestamp==itsEndRangeTimestamp;
			
			itsStartTimestamp = getLogBrowser().getFirstTimestamp();
			itsEndTimestamp = getLogBrowser().getLastTimestamp();
			itsEndLabel.setText(""+(itsEndTimestamp-itsStartTimestamp)/1000000+"ms");
			if (rangeNeedUpdate) updateViews();
		}
		
		private void initLayout()
		{
			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
			itsSlider.setEnabled(true);	
			
			Font theDerivedFont = itsStartLabel.getFont().deriveFont(9f);
			itsStartLabel.setFont(theDerivedFont);
			itsEndLabel.setFont(theDerivedFont);
			itsStartRangeLabel.setFont(theDerivedFont);
			itsEndRangeLabel.setFont(theDerivedFont);
			
			Box theTopBox = Box.createHorizontalBox();
			theTopBox.add(itsStartLabel);
			theTopBox.add(Box.createHorizontalGlue());
			theTopBox.add(itsStartRangeLabel);
			theTopBox.add(Box.createHorizontalGlue());
			theTopBox.add(itsEndRangeLabel);
			theTopBox.add(Box.createHorizontalGlue());
			theTopBox.add(itsEndLabel);
			
			add(theTopBox);
			add(itsSlider);
		
		}
		
		public void stateChanged(ChangeEvent aE)
		{
			updateViews();
		}

		private void updateViews()
		{
			updateRangeLabels();
			//update views
			if (!itsSlider.getModel().getValueIsAdjusting()){
				//TODO check why not working
				//itsDock.pStart().set(itsStartRangeTimestamp);
				itsDock.pEnd().set(itsEndRangeTimestamp);
			}
		}
		
		
	}
	
}
