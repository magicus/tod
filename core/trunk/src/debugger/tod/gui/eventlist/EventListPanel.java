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
package tod.gui.eventlist;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.EventUtils;
import tod.core.database.event.IArrayWriteEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.IConstructorChainingEvent;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IMethodCallEvent;
import tod.gui.GUIUtils;
import tod.gui.JobProcessor;
import tod.gui.eventlist.MuralScroller.UnitScroll;
import tod.gui.kit.Options;
import tod.gui.kit.StdOptions;
import tod.gui.kit.Options.OptionDef;
import tod.utils.TODUtils;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.notification.IFireableEvent;
import zz.utils.notification.SimpleEvent;
import zz.utils.properties.IProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyListener;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.ScrollablePanel;

public class EventListPanel extends JPanel
implements MouseWheelListener
{
	private final ILogBrowser itsLogBrowser;
	private final JobProcessor itsJobProcessor;
	
	private EventListCore itsCore;
	private JPanel itsEventsPanel;
	
	private MuralScroller itsScroller;
	
	private long itsFirstDisplayedTimestamp;
	private long itsLastDisplayedTimestamp;
	
	private final IRWProperty<ILogEvent> pSelectedEvent = 
		new SimpleRWProperty<ILogEvent>()
		{
			@Override
			protected void changed(ILogEvent aOldValue, ILogEvent aNewValue)
			{
				makeVisible(aNewValue);
				repaint();
			}
		};
	
	private final IFireableEvent<ILogEvent> eEventActivated = new SimpleEvent<ILogEvent>();
		
	/**
	 * Maps currently displayed events to their graphic node.
	 */
	private Map<ILogEvent, AbstractEventNode> itsNodesMap = 
		new HashMap<ILogEvent, AbstractEventNode>();

	private int itsSubmittedJobs = 0;
	
//	private IBusListener<EventSelectedMsg> itsEventSelectedListener = new IBusListener<EventSelectedMsg>()
//	{
//		public boolean processMessage(EventSelectedMsg aMessage)
//		{
//			return false;
//		}
//	};
	
	public EventListPanel(ILogBrowser aLogBrowser, JobProcessor aJobProcessor)
	{
		itsLogBrowser = aLogBrowser;
		itsJobProcessor = aJobProcessor;
		createUI();
	}
	
	/**
	 * Creates an event list that shows all the event selected by the specified 
	 * filter, or all the events of the database if the filter is null.
	 */
	public EventListPanel(ILogBrowser aLogBrowser, JobProcessor aJobProcessor, IEventFilter aEventFilter)
	{
		this(aLogBrowser, aJobProcessor);
		setBrowser(aEventFilter);
	}
	
	public JobProcessor getJobProcessor()
	{
		return itsJobProcessor;
	}
	
	public ILogBrowser getLogBrowser()
	{
		return itsLogBrowser;
	}

	public void forward(final int aCount)
	{
		if (itsCore == null) return;
		if (itsSubmittedJobs > 5) return;
		
		itsSubmittedJobs++;
		getJobProcessor().submit(new JobProcessor.Job<Object>()
		{
			@Override
			public Object run()
			{
				itsCore.forward(aCount);
				itsSubmittedJobs--;
				if (itsSubmittedJobs == 0) postUpdateList();
				return null;
			}
		});
	}
	
	public void backward(final int aCount)
	{
		if (itsCore == null) return;
		if (itsSubmittedJobs > 5) return;
		
		itsSubmittedJobs++;
		getJobProcessor().runNow(new JobProcessor.Job<Object>()
		{
			@Override
			public Object run()
			{
				itsCore.backward(aCount);
				itsSubmittedJobs--;
				if (itsSubmittedJobs == 0) postUpdateList();
				return null;
			}
		});
	}
	
	public void setTimestamp(final long aTimestamp)
	{
		if (itsCore == null) return;
		getJobProcessor().submit(new JobProcessor.Job<Object>()
		{
			@Override
			public Object run()
			{
				TODUtils.log(1,"[EventListPanel.setTimestamp] Updating...");
				itsCore.setTimestamp(aTimestamp);
				postUpdateList();
				TODUtils.log(1,"[EventListPanel.setTimestamp] Done...");
				return null;
			}
		});
	}

	/**
	 * Convenience method to set the event browser (see {@link #setBrowser(IEventBrowser)}).
	 * @param aEventFilter A filter, or null for all events,
	 */
	public void setBrowser(IEventFilter aEventFilter)
	{
		setBrowser(aEventFilter != null ? 
				getLogBrowser().createBrowser(aEventFilter) 
				: getLogBrowser().createBrowser());
	}
	
	/**
	 * Sets the event browser that provides the content to this list.
	 */
	public void setBrowser(IEventBrowser aEventBrowser)
	{
		if (itsCore == null) itsCore = new EventListCore(aEventBrowser, 10);
		else itsCore.setBrowser(aEventBrowser);

		itsScroller.set(
				aEventBrowser.clone(),  // Cannot reuse the browser passed to EventListCore
				aEventBrowser.getFirstTimestamp(),
				aEventBrowser.getLastTimestamp());

		update();
	}
	
	/**
	 * Posts an {@link #updateList()} request to be executed by the
	 * swing thread.
	 */
	private void postUpdateList()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				updateList();
			}
		});
	}
	
	private void updateList()
	{
		itsEventsPanel.removeAll();
		List<ILogEvent> theEvents = itsCore.getDisplayedEvents();
		
		Map<ILogEvent, AbstractEventNode> theOldMap = itsNodesMap;
		itsNodesMap = new HashMap<ILogEvent, AbstractEventNode>();
		
		int theChildrenHeight = 0;
		int theTotalHeight = itsEventsPanel.getHeight();
		
		for (ILogEvent theEvent : theEvents)
		{
			AbstractEventNode theNode = theOldMap.get(theEvent);
			if (theNode == null) theNode = buildEventNode(theEvent);
			
			if (theNode != null) 
			{
				theChildrenHeight += theNode.getPreferredSize().height;
				itsEventsPanel.add(theNode);
				itsNodesMap.put(theEvent, theNode);
			}
		}
		
		while (theChildrenHeight < theTotalHeight)
		{
			ILogEvent theEvent = itsCore.incVisibleEvents();
			if (theEvent == null) break;
			AbstractEventNode theNode = theOldMap.get(theEvent);
			if (theNode == null) theNode = buildEventNode(theEvent);
			
			if (theNode != null) 
			{
				theChildrenHeight += theNode.getPreferredSize().height;
				itsEventsPanel.add(theNode);
				itsNodesMap.put(theEvent, theNode);
			}
		}
		
		
		itsEventsPanel.revalidate();
		itsEventsPanel.repaint();
	}
	
	private void createUI()
	{
		itsScroller = new MuralScroller();
		
		itsScroller.eUnitScroll().addListener(new IEventListener<UnitScroll>()
				{
					public void fired(IEvent< ? extends UnitScroll> aEvent, UnitScroll aData)
					{
						switch (aData)
						{
						case UP:
							backward(1);
							break;
							
						case DOWN:
							forward(1);
							break;
						}
					}
				});
		itsScroller.pTrackScroll().addHardListener(new PropertyListener<Long>()
				{
					@Override
					public void propertyChanged(IProperty<Long> aProperty, Long aOldValue, Long aNewValue)
					{
						setTimestamp(aNewValue);
					}
				});
		
		itsEventsPanel = new ScrollablePanel(GUIUtils.createStackLayout())
		{
			public boolean getScrollableTracksViewportHeight()
			{
				return true;
			}
		};
		itsEventsPanel.setOpaque(false);
		itsEventsPanel.addMouseWheelListener(this);
		
		final JTextField theGotoEventField = new JTextField(20);
		theGotoEventField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				String theText = theGotoEventField.getText();
				try
				{
					long theTimestamp = Long.parseLong(theText);
					setTimestamp(theTimestamp);
				}
				catch (NumberFormatException e)
				{
					theGotoEventField.setText("invalid");
				}
			}
		});

		
		setLayout(new BorderLayout());
		add(itsEventsPanel, BorderLayout.CENTER);
		add(theGotoEventField, BorderLayout.NORTH);
		add(itsScroller, BorderLayout.EAST);
	}
	
	private void update()
	{
		itsNodesMap.clear();
		updateList();
				
		revalidate();
		repaint();
	}
	
	public boolean isVisible(ILogEvent aEvent)
	{
		long theTimestamp = aEvent.getTimestamp();
		return theTimestamp >= itsFirstDisplayedTimestamp 
				&& theTimestamp <= itsLastDisplayedTimestamp;
	}
	
	/**
	 * Scrolls so that the given event is visible.
	 * @return The bounds of the graphic object that represent
	 * the event.
	 */
	public void makeVisible(ILogEvent aEvent)
	{
		AbstractEventNode theNode = itsNodesMap.get(aEvent);
		
		boolean isVisible = true;
		
		if (theNode != null) 
		{
			Rectangle theNodeBounds = theNode.getBounds();
			Rectangle theBounds = itsEventsPanel.getBounds();
			
			if (theNodeBounds.getMaxY() > theBounds.getHeight()) isVisible = false;
		}
		else isVisible = false;
		
		if (! isVisible)
		{
			setTimestamp(aEvent.getTimestamp());
			backward(2);
			theNode = itsNodesMap.get(aEvent);
		}
		
	}
	
	/**
	 * This property corresponds to the currently selected event.
	 */
	public IRWProperty<ILogEvent> pSelectedEvent()
	{
		return pSelectedEvent;
	}
	
	/**
	 * An event that is fired when a log event of this list is activated,
	 * ie. double clicked.
	 */
	public IEvent<ILogEvent> eEventActivated()
	{
		return eEventActivated;
	}
	
	/**
	 * Used by the nodes to notify activation.
	 */
	void eventActivated(ILogEvent aEvent)
	{
		eEventActivated.fire(aEvent);
	}
	
	public void mouseWheelMoved(MouseWheelEvent aEvent)
	{
		int theRotation = aEvent.getWheelRotation();
		if (theRotation < 0) backward(1);
		else if (theRotation > 0) forward(1);

		aEvent.consume();
	}
	
	private AbstractEventNode buildEventNode(ILogEvent aEvent)
	{
		if (aEvent instanceof IFieldWriteEvent)
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;
			return new FieldWriteNode(this, theEvent);
		}
		else if (aEvent instanceof IArrayWriteEvent)
		{
			IArrayWriteEvent theEvent = (IArrayWriteEvent) aEvent;
			return new ArrayWriteNode(this, theEvent);
		}
		else if (aEvent instanceof ILocalVariableWriteEvent)
		{
			ILocalVariableWriteEvent theEvent = (ILocalVariableWriteEvent) aEvent;
			return new LocalVariableWriteNode(this, theEvent);
		}
		else if (aEvent instanceof IExceptionGeneratedEvent)
		{
			IExceptionGeneratedEvent theEvent = (IExceptionGeneratedEvent) aEvent;
			if (EventUtils.isIgnorableException(theEvent)) return null;
			else return new ExceptionGeneratedNode(this, theEvent);
		}
		else if (aEvent instanceof IMethodCallEvent)
		{
			IMethodCallEvent theEvent = (IMethodCallEvent) aEvent;
			return new MethodCallNode(this, theEvent);
		}
		else if (aEvent instanceof IInstantiationEvent)
		{
			IInstantiationEvent theEvent = (IInstantiationEvent) aEvent;
			return new InstantiationNode(this, theEvent);
		}
		else if (aEvent instanceof IConstructorChainingEvent)
		{
			IConstructorChainingEvent theEvent = (IConstructorChainingEvent) aEvent;
			return new ConstructorChainingNode(this, theEvent);
		}
		else if (aEvent instanceof IBehaviorExitEvent)
		{
			return null;
		}

		return new UnknownEventNode(this, aEvent);
	}
	
	public <T> T getCurrentValue(OptionDef<T> aDef)
	{
		return Options.get(this).getProperty(aDef).get();
	}
	
	/**
	 * Places all the options necessary to operate an {@link EventListPanel}
	 * into the specified options container.
	 */
	public static void createDefaultOptions(
			Options aOptions,
			boolean aShowExceptionsInRed,
			boolean aShowEventsLocation)
	{
		aOptions.addOption(StdOptions.EXCEPTION_EVENTS_RED, aShowExceptionsInRed);
		aOptions.addOption(StdOptions.SHOW_EVENTS_LOCATION, aShowEventsLocation);
	}
	

}
