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
package tod.gui.view.controlflow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import tod.core.config.TODConfig;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.LocationUtils;
import tod.core.database.browser.Stepper;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
import tod.gui.IGUIManager;
import tod.gui.IntimacyLevelSelector;
import tod.gui.MinerUI;
import tod.gui.Resources;
import tod.gui.eventlist.EventListPanel;
import tod.gui.eventlist.IntimacyLevel;
import tod.gui.formatter.EventFormatter;
import tod.gui.kit.Bus;
import tod.gui.kit.IBusListener;
import tod.gui.kit.Options;
import tod.gui.kit.SavedSplitPane;
import tod.gui.kit.StdOptions;
import tod.gui.kit.StdProperties;
import tod.gui.kit.messages.EventSelectedMsg;
import tod.gui.kit.messages.ShowCFlowMsg;
import tod.gui.kit.messages.EventSelectedMsg.SelectionMethod;
import tod.gui.seed.CFlowSeed;
import tod.gui.view.IEventListView;
import tod.gui.view.LogView;
import tod.gui.view.controlflow.tree.CFlowTree;
import tod.gui.view.controlflow.watch.WatchPanel;
import zz.utils.SimpleAction;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyListener;
import zz.utils.properties.SimplePropertyListener;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.UIUtils;
import zz.utils.ui.ZLabel;

public class CFlowView extends LogView implements IEventListView
{
	public static final boolean SHOW_PARENT_FRAMES = false;
	private static final String PROPERTY_SPLITTER_POS = "cflowView.splitterPos";

	private CFlowSeed itsSeed;
	private EventFormatter itsFormatter;
	private Stepper itsStepper;
	
	private CFlowTree itsCFlowTree;
	private WatchPanel itsWatchPanel;
	
	private ZLabel itsHostLabel;
	private ZLabel itsThreadLabel;
	
	private IPropertyListener<ILogEvent> itsSelectedEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
		{
			update();
		}
	};

	private IPropertyListener<ILogEvent> itsRootEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
		{
			update();
		}
	};
	
	private IBusListener<ShowCFlowMsg> itsShowCFlowListener = new IBusListener<ShowCFlowMsg>()
	{
		public boolean processMessage(ShowCFlowMsg aMessage)
		{
			showEvent(aMessage.getEvent());
			return true;
		}
	};
	
	private IBusListener<EventSelectedMsg> itsEventSelectedListener = new IBusListener<EventSelectedMsg>()
	{
		public boolean processMessage(EventSelectedMsg aMessage)
		{
			if (aMessage.getSelectionMethod() == SelectionMethod.SELECT_IN_CALL_STACK)
			{
				showEvent(aMessage.getEvent());
				return true;
			}
			
			return false;
		}
	};
	

	private IRWProperty<Boolean> itsShowPackages;
	private IPropertyListener<Boolean> itsShowPackagesListener = new SimplePropertyListener<Boolean>()
	{
		@Override
		protected void changed(IProperty<Boolean> aProperty)
		{
			//TODO: re-create UI
		}
	};
	

	public CFlowView(IGUIManager aGUIManager, ILogBrowser aLogBrowser, CFlowSeed aSeed)
	{
		super (aGUIManager, aLogBrowser);
		itsSeed = aSeed;
		itsFormatter = new EventFormatter(aLogBrowser);

		itsStepper = new Stepper(getLogBrowser());
	}
	
	public CFlowSeed getSeed()
	{
		return itsSeed;
	}

	public EventFormatter getFormatter()
	{
		return itsFormatter;
	}

	@Override
	public void init()
	{
		setLayout(new BorderLayout());
		
		// Create tree panel
		itsCFlowTree = new CFlowTree(this);
		
		JPanel theCFlowPanel = new JPanel(new BorderLayout());
		theCFlowPanel.add(itsCFlowTree, BorderLayout.CENTER);
		
		// Create title
		JPanel theTitlePanel = new JPanel(GUIUtils.createStackLayout());
		
		itsHostLabel = ZLabel.create("", FontConfig.SMALL_FONT, Color.BLACK);
		itsThreadLabel = GUIUtils.createLabel("");
		
		theTitlePanel.add(itsHostLabel);
		theTitlePanel.add(itsThreadLabel);
		
		JPanel theNorthPanel = new JPanel(new BorderLayout(0, 0));
		theNorthPanel.add(theTitlePanel, BorderLayout.WEST);
		theNorthPanel.add(createToolbar(), BorderLayout.CENTER);
		
		theCFlowPanel.add(theNorthPanel, BorderLayout.NORTH);
		
		// Create watch panel
		itsWatchPanel = new WatchPanel(this);

		JSplitPane theSplitPane = new SavedSplitPane(JSplitPane.HORIZONTAL_SPLIT, getGUIManager(), PROPERTY_SPLITTER_POS);
		theSplitPane.setResizeWeight(0.5);
		theSplitPane.setLeftComponent(theCFlowPanel);
		theSplitPane.setRightComponent(itsWatchPanel);
		
		add(theSplitPane, BorderLayout.CENTER);
		
		getBus().putProperty(
				StdProperties.INTIMACY_LEVEL, 
				new SimpleRWProperty<IntimacyLevel>(this, IntimacyLevel.FULL_OBLIVIOUSNESS), 
				true);
	}
	
	private void backwardStepOver() 
	{
		itsStepper.setCurrentEvent(itsSeed.pSelectedEvent().get());
		itsStepper.backwardStepOver();
		ILogEvent theEvent = itsStepper.getCurrentEvent();
		if (theEvent != null) selectEvent(theEvent, SelectionMethod.BACKWARD_STEP_OVER);
	}
	

	private void backwardStepInto() 
	{
		itsStepper.setCurrentEvent(itsSeed.pSelectedEvent().get());
		itsStepper.backwardStepInto();
		ILogEvent theEvent = itsStepper.getCurrentEvent();
		if (theEvent != null) selectEvent(theEvent, SelectionMethod.BACKWARD_STEP_INTO);
	}
	
	private void stepOut() 
	{
		ILogEvent theSelectedEvent = itsSeed.pSelectedEvent().get();
		if (theSelectedEvent != null)
		{
			itsStepper.setCurrentEvent(theSelectedEvent);
			itsStepper.stepOut();
			ILogEvent theEvent = itsStepper.getCurrentEvent();
			if (theEvent != null) selectEvent(theEvent, SelectionMethod.STEP_OUT);				
		}
	}

	/**
	 * Perform a step into action on the specified event.
	 */
	public void forwardStepInto(ILogEvent aEvent)
	{
		itsStepper.setCurrentEvent(aEvent);
		itsStepper.forwardStepInto();
		ILogEvent theEvent = itsStepper.getCurrentEvent();
		if (theEvent != null) selectEvent(theEvent, SelectionMethod.FORWARD_STEP_INTO);
	}
	
	private void forwardStepInto() 
	{
		forwardStepInto(itsSeed.pSelectedEvent().get());
	}

	private void forwardStepOver() 
	{
		itsStepper.setCurrentEvent(itsSeed.pSelectedEvent().get());
		itsStepper.forwardStepOver();
		ILogEvent theEvent = itsStepper.getCurrentEvent();
		if (theEvent != null) selectEvent(theEvent, SelectionMethod.FORWARD_STEP_OVER);
	}

	private JComponent createToolbar()
	{
		JPanel theToolbar = new JPanel();
		
		// Setup intimacy level selector
		if (getConfig().get(TODConfig.WITH_ASPECTS))
		{
			theToolbar.add(new IntimacyLevelSelector());
		}
				
		theToolbar.add(new JButton(new SimpleAction(
				Resources.ICON_BACKWARD_STEP_OVER.asIcon(20), 
				"Backward step over")
		{
			public void actionPerformed(ActionEvent aE)
			{
				backwardStepOver();				
			}
		}));
		
		theToolbar.add(new JButton(new SimpleAction(
				Resources.ICON_BACKWARD_STEP_INTO.asIcon(20), 
				"Backward step into")
		{
			public void actionPerformed(ActionEvent aE)
			{
				backwardStepInto();				
			}
		}));
		
		theToolbar.add(new JButton(new SimpleAction(
				Resources.ICON_STEP_OUT.asIcon(20), 
				"Step out")
		{
			public void actionPerformed(ActionEvent aE)
			{
				stepOut();
			}
		}));
		
		theToolbar.add(new JButton(new SimpleAction(
				Resources.ICON_FORWARD_STEP_INTO.asIcon(20), 
				"Forward step into")
		{
			public void actionPerformed(ActionEvent aE)
			{
				forwardStepInto();				
			}
		}));
		
		theToolbar.add(new JButton(new SimpleAction(
				Resources.ICON_FORWARD_STEP_OVER.asIcon(20), 
				"Forward step over")
		{
			public void actionPerformed(ActionEvent aE)
			{
				forwardStepOver();				
			}

		}));

		for (int i=0;i<theToolbar.getComponentCount();i++)
		{
			Component theComponent = theToolbar.getComponent(i);
			if (theComponent instanceof JButton)
			{
				JButton theButton = (JButton) theComponent;
				theButton.setMargin(UIUtils.NULL_INSETS);
			}
		}
		
		return theToolbar;
	}
	
	private void update()
	{
		IThreadInfo theThread = getSeed().getThread();
		IHostInfo theHost = theThread.getHost();
		
		itsHostLabel.setText(String.format(
				"Host: \"%s\" [%d]",
				theHost.getName(),
				theHost.getId()));
		
		itsHostLabel.revalidate();
		itsHostLabel.repaint();
		
		itsThreadLabel.setText(String.format(
				"Thread: \"%s\" [%d]",
				theThread.getName(),
				theThread.getId()));
		
		itsThreadLabel.revalidate();
		itsThreadLabel.repaint();

		ILogEvent theSelectedEvent = itsSeed.pSelectedEvent().get();
		
		if (theSelectedEvent != null)
		{
			itsWatchPanel.showStackFrame();
			showEvent(theSelectedEvent);
		}
		
		LocationUtils.gotoSource(getGUIManager(), theSelectedEvent);
	}
	
	private void showEvent (ILogEvent aEvent)
	{
		getSeed().pSelectedEvent().set(aEvent);
	}

	@Override
	protected void initOptions(Options aOptions)
	{
		super.initOptions(aOptions);
		aOptions.addOption(StdOptions.SHOW_PACKAGE_NAMES, true);
		EventListPanel.createDefaultOptions(aOptions, true, false);
	}
	
	@Override
	public void addNotify()
	{
		itsSeed.pSelectedEvent().addHardListener(itsSelectedEventListener);
		connect(itsSeed.pSelectedEvent(), itsCFlowTree.pSelectedEvent(), true);
		itsSeed.pRootEvent().addHardListener(itsRootEventListener);
		
		Bus.get(this).subscribe(ShowCFlowMsg.ID, itsShowCFlowListener);
		Bus.get(this).subscribe(EventSelectedMsg.ID, itsEventSelectedListener);
		
		itsShowPackages = Options.get(this).getProperty(StdOptions.SHOW_PACKAGE_NAMES);
		itsShowPackages.addHardListener(itsShowPackagesListener);
		
		super.addNotify();
		update();
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsSeed.pSelectedEvent().removeListener(itsSelectedEventListener);
		itsSeed.pRootEvent().removeListener(itsRootEventListener);
		
		Bus.get(this).unsubscribe(ShowCFlowMsg.ID, itsShowCFlowListener);
		Bus.get(this).unsubscribe(EventSelectedMsg.ID, itsEventSelectedListener);
		
		itsShowPackages.removeListener(itsShowPackagesListener);
	}
	
	/**
	 * Whether package names should be dosplayed.
	 */
	public boolean showPackageNames()
	{
//		return itsShowPackages.get();
		return false;
	}
	
	public void selectEvent(ILogEvent aEvent, SelectionMethod aMethod)
	{
		getSeed().pSelectedEvent().set(aEvent);
		Bus.get(CFlowView.this).postMessage(new EventSelectedMsg(aEvent, aMethod));
	}
	
	public boolean isEventSelected(ILogEvent aEvent)
	{
		ILogEvent theSelectedEvent = itsSeed.pSelectedEvent().get();
		return theSelectedEvent != null && theSelectedEvent.equals(aEvent);
	}
	
	public IEventBrowser getEventBrowser()
	{
		ILogEvent theSelectedEvent = getSeed().pSelectedEvent().get();
		IParentEvent theParent = theSelectedEvent.getParent();
		return theParent.getChildrenBrowser();
	}
	
	public ILogEvent getSelectedEvent()
	{
		return getSeed().pSelectedEvent().get();
	}

	
	
}
