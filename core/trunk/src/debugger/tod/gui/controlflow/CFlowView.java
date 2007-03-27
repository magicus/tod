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
package tod.gui.controlflow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.Stepper;
import tod.core.database.event.ExternalPointer;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
import tod.gui.IGUIManager;
import tod.gui.MinerUI;
import tod.gui.Hyperlinks.ISeedFactory;
import tod.gui.controlflow.tree.CFlowTree;
import tod.gui.controlflow.watch.WatchPanel;
import tod.gui.formatter.EventFormatter;
import tod.gui.seed.CFlowSeed;
import tod.gui.seed.Seed;
import tod.gui.view.IEventListView;
import tod.gui.view.LogView;
import zz.utils.SimpleAction;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.PropertyListener;
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
	
	private IPropertyListener<ILogEvent> itsSelectedEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(IProperty<ILogEvent> aProperty, ILogEvent aOldValue, ILogEvent aNewValue)
		{
			itsCFlowTree.repaint();
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
	
	private IPropertyListener<IParentEvent> itsParentListener = new PropertyListener<IParentEvent>()
	{
		@Override
		public void propertyChanged(IProperty<IParentEvent> aProperty, IParentEvent aOldValue, IParentEvent aNewValue)
		{
			setParent(aNewValue);
		}
	};
	


	private JSplitPane itsSplitPane;
	
	public CFlowView(IGUIManager aGUIManager, ILogBrowser aLogBrowser, CFlowSeed aSeed)
	{
		super (aGUIManager, aLogBrowser);
		itsSeed = aSeed;
		itsFormatter = new EventFormatter(aLogBrowser);

		itsStepper = new Stepper(getLogBrowser(), itsSeed.getThread());
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
//		theCFlowPanel.add(theTreeScrollPane, BorderLayout.CENTER);
		theCFlowPanel.add(itsCFlowTree, BorderLayout.CENTER);
		
		// Create title
		JPanel theTitlePanel = new JPanel(GUIUtils.createStackLayout());
		IThreadInfo theThread = getSeed().getThread();
		IHostInfo theHost = theThread.getHost();
		
		theTitlePanel.add(ZLabel.create(
				String.format(
						"Host: \"%s\" [%d]",
						theHost.getName(),
						theHost.getId()),
				FontConfig.SMALL_FONT,
				Color.BLACK));

		theTitlePanel.add(GUIUtils.createLabel(String.format(
				"Thread: \"%s\" [%d]",
				theThread.getName(),
				theThread.getId())));
		
		JPanel theNorthPanel = new JPanel(new BorderLayout(0, 0));
		theNorthPanel.add(theTitlePanel, BorderLayout.WEST);
		theNorthPanel.add(createToolbar(), BorderLayout.CENTER);
		
		theCFlowPanel.add(theNorthPanel, BorderLayout.NORTH);
		
		// Create watch panel
		itsWatchPanel = new WatchPanel(this);

		itsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		itsSplitPane.setResizeWeight(0.5);
		itsSplitPane.setLeftComponent(theCFlowPanel);
		itsSplitPane.setRightComponent(itsWatchPanel);
		
		add(itsSplitPane, BorderLayout.CENTER);
		
		setParent(getSeed().pParentEvent().get());
		update();
	}
	
	
	private JComponent createToolbar()
	{
		JPanel theToolbar = new JPanel();
				
		theToolbar.add(new JButton(new SimpleAction("|<", "Backward step over")
		{
			public void actionPerformed(ActionEvent aE)
			{
				itsStepper.setCurrentEvent(itsSeed.pSelectedEvent().get());
				itsStepper.backwardStepOver();
				selectEvent(itsStepper.getCurrentEvent());
			}
		}));
		
		theToolbar.add(new JButton(new SimpleAction("{}<", "Backward step into")
		{
			public void actionPerformed(ActionEvent aE)
			{
				itsStepper.setCurrentEvent(itsSeed.pSelectedEvent().get());
				itsStepper.backwardStepInto();
				selectEvent(itsStepper.getCurrentEvent());
			}
		}));
		
		theToolbar.add(new JButton(new SimpleAction("/\\", "Step out")
		{
			public void actionPerformed(ActionEvent aE)
			{
				ILogEvent theSelectedEvent = itsSeed.pSelectedEvent().get();
				if (theSelectedEvent != null)
				{
					itsStepper.setCurrentEvent(theSelectedEvent);
					itsStepper.stepOut();
					selectEvent(itsStepper.getCurrentEvent());
				}
				else 
				{
					IParentEvent theParentEvent = itsSeed.pParentEvent().get();
					if (theParentEvent != null)
					{
						itsSeed.pParentEvent().set(theParentEvent.getParent());
					}
				}
			}
		}));
		
		theToolbar.add(new JButton(new SimpleAction(">{}", "Forward step into")
		{
			public void actionPerformed(ActionEvent aE)
			{
				itsStepper.setCurrentEvent(itsSeed.pSelectedEvent().get());
				itsStepper.forwardStepInto();
				selectEvent(itsStepper.getCurrentEvent());
			}
		}));
		
		theToolbar.add(new JButton(new SimpleAction(">|", "Forward step over")
		{
			public void actionPerformed(ActionEvent aE)
			{
				itsStepper.setCurrentEvent(itsSeed.pSelectedEvent().get());
				itsStepper.forwardStepOver();
				selectEvent(itsStepper.getCurrentEvent());
			}
		}));

		for (int i=0;i<theToolbar.getComponentCount();i++)
		{
			((JButton) theToolbar.getComponent(i)).setMargin(UIUtils.NULL_INSETS);
		}
	
		
		return theToolbar;
	}
	
	private void setParent(IParentEvent aEvent)
	{
		IParentEvent theParentEvent = aEvent != null ?
				aEvent
				: getSeed().pRootEvent().get();

		itsCFlowTree.setParent(theParentEvent);
	}
	
	private void update()
	{
		ILogEvent theSelectedEvent = itsSeed.pSelectedEvent().get();
		
		if (theSelectedEvent != null)
		{
			itsWatchPanel.showStackFrame();
			showEvent(theSelectedEvent);
		}
		
		if (theSelectedEvent != null) getGUIManager().gotoEvent(theSelectedEvent);
	}
	
	private void showEvent (ILogEvent aEvent)
	{
		IParentEvent theCurrentParent = getSeed().pParentEvent().get();
		ExternalPointer theParentPointer = theCurrentParent != null ?
				theCurrentParent.getPointer()
				: null;
				
		if (! aEvent.getParentPointer().equals(theParentPointer))
		{
			getSeed().pParentEvent().set(aEvent.getParent());
		}
		
		getSeed().pSelectedEvent().set(aEvent);
		
		itsCFlowTree.makeVisible(aEvent);
	}

	@Override
	public void addNotify()
	{
		super.addNotify();
		itsSeed.pSelectedEvent().addHardListener(itsSelectedEventListener);
		itsSeed.pRootEvent().addHardListener(itsRootEventListener);
		itsSeed.pParentEvent().addHardListener(itsParentListener);
		
		int theSplitterPos = MinerUI.getIntProperty(
				getGUIManager(), 
				PROPERTY_SPLITTER_POS, 400);
		
		itsSplitPane.setDividerLocation(theSplitterPos);
		
		update();
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsSeed.pSelectedEvent().removeListener(itsSelectedEventListener);
		itsSeed.pRootEvent().removeListener(itsRootEventListener);
		itsSeed.pParentEvent().removeListener(itsParentListener);
		
		getGUIManager().setProperty(
				PROPERTY_SPLITTER_POS, 
				""+itsSplitPane.getDividerLocation());
		
	}
	
	
	public void selectEvent(ILogEvent aEvent)
	{
		itsSeed.pSelectedEvent().set(aEvent);
	}
	
	public boolean isEventSelected(ILogEvent aEvent)
	{
		ILogEvent theSelectedEvent = itsSeed.pSelectedEvent().get();
		return theSelectedEvent != null && theSelectedEvent.equals(aEvent);
	}
	
	public IEventBrowser getEventBrowser()
	{
		IParentEvent theCurrentParent = getSeed().pParentEvent().get();
		return theCurrentParent.getChildrenBrowser();
	}
	
	public ILogEvent getSelectedEvent()
	{
		return getSeed().pSelectedEvent().get();
	}
	
	
	private class CFlowSeedFactory implements ISeedFactory
	{

		public Seed behaviorSeed(IBehaviorInfo aBehavior)
		{
			return null;
		}

		public Seed cflowSeed(final ILogEvent aEvent)
		{
			return new Seed()
			{
				public void open()
				{
					showEvent(aEvent);
				}
			};
		}

		public Seed typeSeed(ITypeInfo aType)
		{
			return null;
		}
		
	}
}
