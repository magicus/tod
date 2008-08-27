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
package tod.gui.view.objecthistory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tod.Util;
import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.eventlist.EventListPanel;
import tod.gui.kit.SavedSplitPane;
import tod.gui.kit.messages.ShowCFlowMsg;
import tod.gui.seed.ObjectHistorySeed;
import tod.gui.view.LogView;
import tod.gui.view.LogViewSubPanel;
import tod.tools.scheduling.Scheduled;
import tod.tools.scheduling.IJobScheduler.JobPriority;
import tod.utils.LocationComparator;
import zz.utils.SimpleListModel;
import zz.utils.Utils;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.ui.StackLayout;
import zz.utils.ui.UniversalRenderer;

/**
 * This panel displays the history of individual executions of the object.
 * @author gpothier
 */
public class ObjectMethodsPanel extends LogViewSubPanel<ObjectHistorySeed>
{
	private SimpleListModel itsMethodsListModel;
	private EventListPanel itsListPanel;
	private IObjectInspector itsInspector;
	private JList itsMethodsList;
	
	public ObjectMethodsPanel(LogView<ObjectHistorySeed> aView)
	{
		super(aView);
		createUI();
	}

	private void createUI()
	{
		JSplitPane theSplitPane = new SavedSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, 
				getGUIManager(), 
				"objectEventsListPanel.splitterPos");
		
		theSplitPane.setResizeWeight(0.5);
		
		itsListPanel = new EventListPanel (getGUIManager(), getBus(), getLogBrowser(), getJobScheduler());
		
		itsListPanel.eEventActivated().addListener(new IEventListener<ILogEvent>()
				{
					public void fired(IEvent< ? extends ILogEvent> aEvent, ILogEvent aData)
					{
						getBus().postMessage(new ShowCFlowMsg(aData));
					}
				});

		theSplitPane.setRightComponent(itsListPanel);
		
		itsMethodsListModel = new SimpleListModel();
		itsMethodsList = new JList(itsMethodsListModel);
		itsMethodsList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent aE)
			{
				if (aE.getValueIsAdjusting()) return;
				behaviorSelected((IBehaviorInfo) itsMethodsList.getSelectedValue());
			}
		});
		theSplitPane.setLeftComponent(new JScrollPane(itsMethodsList));
		
		setLayout(new StackLayout());
		add (theSplitPane);
	}

	@Override
	public void connectSeed(ObjectHistorySeed aSeed)
	{
		itsInspector = getLogBrowser().createObjectInspector(getSeed().getObject());
		setupMethodsList();
		connect(aSeed.pSelectedEvent(), itsListPanel.pSelectedEvent());
	}
	
	@Override
	public void disconnectSeed(ObjectHistorySeed aSeed)
	{
		itsInspector = null;
		disconnect(aSeed.pSelectedEvent(), itsListPanel.pSelectedEvent());
	}
	
	private void setupMethodsList()
	{
		ITypeInfo theType = itsInspector.getType();
		if (theType instanceof IClassInfo)
		{
			IClassInfo theClass = (IClassInfo) theType;
			itsMethodsList.setCellRenderer(new BehaviorRenderer(theClass));
			
			List<IBehaviorInfo> theBehaviors = new ArrayList<IBehaviorInfo>();
			
			while(theClass != null)
			{
				Utils.fillCollection(theBehaviors, theClass.getBehaviors());
				theClass = theClass.getSupertype();
			}
			
			Collections.sort(theBehaviors, LocationComparator.getInstance());
			itsMethodsListModel.setList(theBehaviors);
		}
		else 
		{
			itsMethodsListModel.setList(null);
		}
	}
	
	@Scheduled(value = JobPriority.EXPLICIT, cancelOthers = true)
	private void behaviorSelected(IBehaviorInfo aBehavior)
	{
		ICompoundFilter theFilter = getLogBrowser().createIntersectionFilter(
				getLogBrowser().createTargetFilter(getSeed().getObject()),
				getLogBrowser().createBehaviorCallFilter(aBehavior));
		
		itsListPanel.setBrowser(theFilter);
	}
	
	/**
	 * List cell renderer for behaviors.
	 * @author gpothier
	 */
	private static class BehaviorRenderer extends UniversalRenderer<IBehaviorInfo>
	{
		/**
		 * The class of the object. For methods of this class, only the method name 
		 * is displayed, otherwise the class name is also displayed.
		 */
		private IClassInfo itsClass;
		
		private BehaviorRenderer(IClassInfo aClass)
		{
			itsClass = aClass;
		}

		@Override
		protected String getName(IBehaviorInfo aBehavior)
		{
			StringBuilder theBuilder = new StringBuilder();
			theBuilder.append(Util.getFullName(aBehavior));
			
			IClassInfo theClass = aBehavior.getType();
			if (! theClass.equals(itsClass))
			{
				theBuilder.append(" [");
				theBuilder.append(theClass.getName());
				theBuilder.append("]");
			}
			
			return theBuilder.toString();
		}
		
	}
}
