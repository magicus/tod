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
package tod.gui.controlflow.tree;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.gui.JobProcessor;
import tod.gui.MinerUI;
import tod.gui.controlflow.CFlowView;
import tod.gui.eventlist.EventListPanel;
import zz.utils.Utils;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyUtils;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.StackLayout;

public class CFlowTree extends JPanel
{
	private static final String PROPERTY_SPLITTER_POS = "cflowTree.splitterPos";
	
	private final CFlowView itsView;
	private CallStackPanel itsCallStackPanel;
	private EventListPanel itsEventListPanel;
	private JSplitPane itsSplitPane;
	private IParentEvent itsCurrentParent;
	
	private final IRWProperty<ILogEvent> pSelectedEvent = 
		new SimpleRWProperty<ILogEvent>()
		{
			@Override
			protected void changed(ILogEvent aOldValue, ILogEvent aNewValue)
			{
				IParentEvent theParent = aNewValue.getParent();
				if (theParent == null) theParent = itsView.getSeed().pRootEvent().get();
				
				if (Utils.different(itsCurrentParent, theParent))
				{
					itsEventListPanel.setBrowser(theParent.getChildrenBrowser());
					itsCurrentParent = theParent;
				}
				
				// Only update call stack, as this property is connected to event list
				itsCallStackPanel.setLeafEvent(aNewValue);
			}
		};
	
	public CFlowTree(CFlowView aView)
	{
		itsView = aView;
		createUI();
	}
	
	public JobProcessor getJobProcessor()
	{
		return itsView.getJobProcessor();
	}

	/**
	 * This property corresponds to the currently selected event.
	 * @return
	 */
	public IRWProperty<ILogEvent> pSelectedEvent()
	{
		return pSelectedEvent;
	}
	
	private void createUI()
	{
		itsSplitPane = new JSplitPane();
		setLayout(new StackLayout());
		add(itsSplitPane);
		
		itsEventListPanel = new EventListPanel(itsView.getLogBrowser(), getJobProcessor()); 
		itsSplitPane.setRightComponent(itsEventListPanel);
		
		PropertyUtils.connect(pSelectedEvent, itsEventListPanel.pSelectedEvent(), true);
		
		itsCallStackPanel = new CallStackPanel(itsView.getLogBrowser(), getJobProcessor());
		itsSplitPane.setLeftComponent(itsCallStackPanel);
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		
		int theSplitterPos = MinerUI.getIntProperty(
				itsView.getGUIManager(), 
				PROPERTY_SPLITTER_POS, 200);
		itsSplitPane.setDividerLocation(theSplitterPos);
	}

	@Override
	public void removeNotify()
	{
		super.removeNotify();
		itsView.getGUIManager().setProperty(
				PROPERTY_SPLITTER_POS, 
				""+itsSplitPane.getDividerLocation());
	}
	
	/**
	 * Scrolls so that the given event is visible.
	 * @return The bounds of the graphic object that represent
	 * the event.
	 */
	public void makeVisible(ILogEvent aEvent)
	{
		itsEventListPanel.makeVisible(aEvent);
	}
	
}
