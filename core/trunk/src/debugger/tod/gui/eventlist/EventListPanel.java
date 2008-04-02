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
package tod.gui.eventlist;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.python.modules.newmodule;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.ShadowId;
import tod.core.database.browser.GroupingEventBrowser.EventGroup;
import tod.core.database.event.EventUtils;
import tod.core.database.event.IArrayWriteEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.IConstructorChainingEvent;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.IInstanceOfEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IMethodCallEvent;
import tod.core.database.event.INewArrayEvent;
import tod.core.database.structure.IAdviceInfo;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.gui.GUIUtils;
import tod.gui.IGUIManager;
import tod.gui.JobProcessor;
import tod.gui.eventlist.EventScroller.UnitScroll;
import tod.gui.kit.Bus;
import tod.gui.kit.BusPanel;
import tod.gui.kit.Options;
import tod.gui.kit.StdOptions;
import tod.gui.kit.Options.OptionDef;
import tod.gui.settings.IntimacySettings;
import tod.utils.TODUtils;
import zz.utils.cache.MRUBuffer;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.notification.IFireableEvent;
import zz.utils.notification.SimpleEvent;
import zz.utils.properties.IProperty;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyListener;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.ScrollablePanel;

public class EventListPanel extends BusPanel
implements MouseWheelListener
{
	private final IGUIManager itsGUIManager;
	private final ILogBrowser itsLogBrowser;
	private final JobProcessor itsJobProcessor;
	
	private EventListCore itsCore;
	private JPanel itsEventsPanel;
	
	private EventScroller itsScroller;
	
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
	 * Buffer for event nodes.
	 */
	private MRUBuffer<ILogEvent, AbstractEventNode> itsNodesBuffer =
		new NodesBuffer();

	private int itsSubmittedJobs = 0;
	
//	private IBusListener<EventSelectedMsg> itsEventSelectedListener = new IBusListener<EventSelectedMsg>()
//	{
//		public boolean processMessage(EventSelectedMsg aMessage)
//		{
//			return false;
//		}
//	};
	
	public EventListPanel(IGUIManager aGUIManager, Bus aBus, ILogBrowser aLogBrowser, JobProcessor aJobProcessor)
	{
		super(aBus);
		itsGUIManager = aGUIManager;
		itsLogBrowser = aLogBrowser;
		itsJobProcessor = aJobProcessor;
		createUI();
	}
	
	/**
	 * Creates an event list that shows all the event selected by the specified 
	 * filter, or all the events of the database if the filter is null.
	 */
	public EventListPanel(IGUIManager aGUIManager, Bus aBus, ILogBrowser aLogBrowser, JobProcessor aJobProcessor, IEventFilter aEventFilter)
	{
		this(aGUIManager, aBus, aLogBrowser, aJobProcessor);
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
	
	public IGUIManager getGUIManager()
	{
		return itsGUIManager;
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
		
		int theChildrenHeight = 0;
		int theTotalHeight = itsEventsPanel.getHeight();
		
		for (ILogEvent theEvent : theEvents) 
		{
			theChildrenHeight += createNode(theEvent);
		}
		
		while (theChildrenHeight < theTotalHeight)
		{
			ILogEvent theEvent = itsCore.incVisibleEvents();
			if (theEvent == null) break;
			
			theChildrenHeight += createNode(theEvent);
		}
		
		itsEventsPanel.revalidate();
		itsEventsPanel.repaint();
	}
	
	private int createNode(ILogEvent aEvent)
	{
		int theHeight = 0;
		
		AbstractEventNode theNode = itsNodesBuffer.get(aEvent);
		
		if (theNode != null) 
		{
			theHeight = theNode.getPreferredSize().height;
			itsEventsPanel.add(theNode);
		}

		return theHeight;
	}
	
	private void createUI()
	{
		itsScroller = new EventScroller(getGUIManager());
		
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
			@Override
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
		// Ensure intimacy level will allow to see the event
		if (aEvent instanceof ICallerSideEvent)
		{
			ICallerSideEvent theEvent = (ICallerSideEvent) aEvent;
			ProbeInfo theProbeInfo = theEvent.getProbeInfo();
			if (theProbeInfo != null && IntimacyLevel.isKnownRole(theProbeInfo.role))
			{
				IAdviceInfo theAdvice = getLogBrowser().getStructureDatabase().getAdvice(theProbeInfo.adviceSourceId);
				IntimacySettings theIntimacySettings = getGUIManager().getSettings().getIntimacySettings();
				IntimacyLevel theLevel = theIntimacySettings.getIntimacyLevel(theProbeInfo.adviceSourceId);
				if (theLevel == null || ! theLevel.showRole(theProbeInfo.role))
				{
					Set<BytecodeRole> theRoles = new HashSet<BytecodeRole>();
					if (theLevel != null) theRoles.addAll(theLevel.getRoles());
					theRoles.add(theProbeInfo.role);
					theIntimacySettings.setIntimacyLevel(theProbeInfo.adviceSourceId, new IntimacyLevel(theRoles));
				}
			}
		}
		
		AbstractEventNode theNode = itsNodesBuffer.get(aEvent);
		
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
			theNode = itsNodesBuffer.get(aEvent);
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
		return buildEventNode(getGUIManager(), this, aEvent);
	}
	
	/**
	 * Builds and event node for the given event, 
	 * or retrieves it from the event list's cache (if specified).
	 */
	public static AbstractEventNode getEventNode(
			IGUIManager aGUIManager, 
			EventListPanel aListPanel, 
			ILogEvent aEvent)
	{
		if (aListPanel == null) return buildEventNode(aGUIManager, null, aEvent);
		else return aListPanel.itsNodesBuffer.get(aEvent);
	}
	
	public static AbstractEventNode buildEventNode(
			IGUIManager aGUIManager, 
			EventListPanel aListPanel, 
			ILogEvent aEvent)
		{
		if (aEvent instanceof IFieldWriteEvent)
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;
			return new FieldWriteNode(aGUIManager, aListPanel, theEvent);
		}
		else if (aEvent instanceof IArrayWriteEvent)
		{
			IArrayWriteEvent theEvent = (IArrayWriteEvent) aEvent;
			return new ArrayWriteNode(aGUIManager, aListPanel, theEvent);
		}
		else if (aEvent instanceof ILocalVariableWriteEvent)
		{
			ILocalVariableWriteEvent theEvent = (ILocalVariableWriteEvent) aEvent;
			return new LocalVariableWriteNode(aGUIManager, aListPanel, theEvent);
		}
		else if (aEvent instanceof IInstanceOfEvent)
		{
			IInstanceOfEvent theEvent = (IInstanceOfEvent) aEvent;
			return new InstanceOfNode(aGUIManager, aListPanel, theEvent);
		}
		else if (aEvent instanceof IExceptionGeneratedEvent)
		{
			IExceptionGeneratedEvent theEvent = (IExceptionGeneratedEvent) aEvent;
			if (EventUtils.isIgnorableException(theEvent)) return null;
			else return new ExceptionGeneratedNode(aGUIManager, aListPanel, theEvent);
		}
		else if (aEvent instanceof IMethodCallEvent)
		{
			IMethodCallEvent theEvent = (IMethodCallEvent) aEvent;
			return new MethodCallNode(aGUIManager, aListPanel, theEvent);
		}
		else if (aEvent instanceof IInstantiationEvent)
		{
			IInstantiationEvent theEvent = (IInstantiationEvent) aEvent;
			return new InstantiationNode(aGUIManager, aListPanel, theEvent);
		}
		else if (aEvent instanceof INewArrayEvent)
		{
			INewArrayEvent theEvent = (INewArrayEvent) aEvent;
			return new NewArrayNode(aGUIManager, aListPanel, theEvent);
		}
		else if (aEvent instanceof IConstructorChainingEvent)
		{
			IConstructorChainingEvent theEvent = (IConstructorChainingEvent) aEvent;
			return new ConstructorChainingNode(aGUIManager, aListPanel, theEvent);
		}
		else if (aEvent instanceof IBehaviorExitEvent)
		{
			return null;
		}
		else if (aEvent instanceof EventGroup)
		{
			EventGroup theEventGroup = (EventGroup) aEvent;
			return buildEventGroupNode(aGUIManager, aListPanel, theEventGroup);
		}

		return new UnknownEventNode(aGUIManager, aListPanel, aEvent);
	}
	
	private static AbstractEventNode buildEventGroupNode(IGUIManager aGUIManager, EventListPanel aListPanel, EventGroup aEventGroup)
	{
		Object theGroupKey = aEventGroup.getGroupKey();
		if (theGroupKey instanceof ShadowId)
		{
			return new ShadowGroupNode(aGUIManager, aListPanel, aEventGroup);
		}
		
		return new UnknownEventNode(aGUIManager, aListPanel, aEventGroup);
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
	
	private class NodesBuffer extends MRUBuffer<ILogEvent, AbstractEventNode>
	{

		public NodesBuffer()
		{
			super(100);
		}

		@Override
		protected AbstractEventNode fetch(ILogEvent aId)
		{
			return buildEventNode(aId);
		}

		@Override
		protected ILogEvent getKey(AbstractEventNode aValue)
		{
			return aValue.getEvent();
		}
		
	}
	
}
