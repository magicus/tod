/*
 * Created on Sep 22, 2005
 */
package tod.gui.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.Timer;

import tod.core.model.browser.ILogBrowser;
import tod.core.model.structure.IThreadInfo;
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
		long theCount = getTrace().getEventsCount();
		
		if (theCount != itsLastEventCount)
		{
			itsLastEventCount = theCount;
			
			itsEventsCountLabel.setText("Events registered: "+theCount);
			
			for (IThreadInfo theThread : getTrace().getLocationTrace().getThreads())
			{
				ThreadSequenceSeed theSeed = itsSeedsMap.get(theThread);
				if (theSeed == null)
				{
					theSeed = new ThreadSequenceSeed(getTrace(), theThread);
					itsSeedsMap.put(theThread, theSeed);
					
					itsDock.pSeeds().add(theSeed);
				}
			}
			
			itsDock.pStart().set(getTrace().getFirstTimestamp());
			itsDock.pEnd().set(getTrace().getLastTimestamp());
		}
	}
}
