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
package tod.gui.view.controlflow.tree;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.gui.JobProcessor;
import tod.gui.seed.CFlowSeed;
import tod.gui.view.LogView;
import zz.utils.Utils;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.PropertyListener;
import zz.utils.ui.GridStackLayout;
import zz.utils.ui.ScrollablePanel;
import zz.utils.ui.StackLayout;

/**
 * A panel that displays a call stack, ie. a list of the ancestors of a given
 * event.
 * 
 * @author gpothier
 */
public class CallStackPanel extends JPanel
{
	private CFlowSeed itsSeed;
	private final JobProcessor itsJobProcessor;

	/**
	 * Root of the control flow tree for the current leaf event
	 */
	private IParentEvent itsRootEvent;
	
	private List<AbstractStackNode> itsStackNodes = new ArrayList<AbstractStackNode>();
	
	private AbstractStackNode itsCurrentStackNode;

	private JScrollPane itsScrollPane;
	
	private final IPropertyListener<ILogEvent> itsSelectedEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(
				IProperty<ILogEvent> aProperty,
				ILogEvent aOldValue,
				ILogEvent aNewValue)
		{
			selectedEventChanged(aNewValue);
		}
	};
	
	private final IPropertyListener<ILogEvent> itsLeafEventListener = new PropertyListener<ILogEvent>()
	{
		@Override
		public void propertyChanged(
				IProperty<ILogEvent> aProperty,
				ILogEvent aOldValue,
				ILogEvent aNewValue)
		{
			leafEventChanged();
		}
	};
	
	public CallStackPanel(JobProcessor aJobProcessor)
	{
		itsJobProcessor = aJobProcessor;
		createUI();
	}
	
	/**
	 * Called when the seed is connected to the CFlowView.
	 * @see LogView#connectSeed
	 */
	protected void connectSeed(CFlowSeed aSeed)
	{
		itsSeed = aSeed;
		itsSeed.pSelectedEvent().addHardListener(itsSelectedEventListener);
		itsSeed.pLeafEvent().addHardListener(itsLeafEventListener);
		rebuildStack();
	}

	/**
	 * Called when the seed is disconnected from the CFlowView.
	 * @see LogView#disconnectSeed
	 */
	protected void disconnectSeed(CFlowSeed aSeed)
	{
		itsSeed.pSelectedEvent().removeListener(itsSelectedEventListener);
		itsSeed.pLeafEvent().removeListener(itsLeafEventListener);
		itsSeed = null;
	}



	public JobProcessor getJobProcessor()
	{
		return itsJobProcessor;
	}

	public ILogBrowser getLogBrowser()
	{
		return itsSeed.getLogBrowser();
	}
	
	private void selectedEventChanged(ILogEvent aEvent)
	{
		// Search if parent is already in the call stack
		for (AbstractStackNode theNode : itsStackNodes)
		{
			if (theNode.getFrameEvent().equals(aEvent.getParent()))
			{
				setCurrentStackNode(theNode);
				return;
			}
		}
		
		// if not found, rebuild the stack.
		itsSeed.pLeafEvent().set(aEvent);
	}

	private void leafEventChanged()
	{
		rebuildStack();
	}
	
	private void setCurrentStackNode(AbstractStackNode aNode)
	{
		if (itsCurrentStackNode != null) itsCurrentStackNode.setCurrentStackFrame(false);
		itsCurrentStackNode = aNode;
		if (itsCurrentStackNode != null) itsCurrentStackNode.setCurrentStackFrame(true);
	}
	
	public IParentEvent getRootEvent()
	{
		if (itsRootEvent == null)
		{
			itsRootEvent = getLogBrowser().getCFlowRoot(itsSeed.getThread());
		}
		return itsRootEvent;
	}

	/**
	 * show the child event of the IParentEvent given in parameter in the
	 * current event list
	 */
	public void selectEvent(ILogEvent aEvent)
	{
		itsSeed.pSelectedEvent().set(aEvent);
//		Bus.get(this).postMessage(new EventSelectedMsg(aEvent, SelectionMethod.SELECT_IN_CALL_STACK));
	}

	private void createUI()
	{
		itsScrollPane = new JScrollPane();
		setLayout(new StackLayout());
		add(itsScrollPane);
	}

	private void rebuildStack()
	{
		itsRootEvent = null;
		itsCurrentStackNode = null;
		JComponent theStack = createStack();
		theStack.setOpaque(false);
		itsScrollPane.setViewportView(theStack);
		itsScrollPane.getViewport().setBackground(Color.WHITE);

		revalidate();
		repaint();
	}

	/**
	 * Builds the stack of ancestor events.
	 */
	private JComponent createStack()
	{
		JPanel theContainer = new ScrollablePanel(new GridStackLayout(1, 0, 2, true, false));
		ILogEvent theCurrentEvent = itsSeed.pLeafEvent().get();
		itsStackNodes.clear();
		
		ILogEvent theSelected = itsSeed.pSelectedEvent().get();
		IBehaviorCallEvent theSelectedFrame = theSelected != null ? theSelected.getParent() : null;
		
		while (theCurrentEvent != null)
		{
			AbstractStackNode theStackNode = buildStackNode(theCurrentEvent);
			itsStackNodes.add(theStackNode);
			theContainer.add(theStackNode);
			
			theCurrentEvent = theCurrentEvent.getParent();
			if (theCurrentEvent != null && theCurrentEvent.equals(theSelectedFrame)) setCurrentStackNode(theStackNode);
		}

		return theContainer;
	}

	private AbstractStackNode buildStackNode(ILogEvent aEvent)
	{
		// JobProcessor theJobProcessor = getJobProcessor();
		JobProcessor theJobProcessor = null;

		if (aEvent.getParent() == null || Utils.equalOrBothNull(aEvent.getParent(), getRootEvent()))
		{
			return new RootStackNode(theJobProcessor, getRootEvent(), this);
		}
		else 
		{
			return new NormalStackNode(theJobProcessor, aEvent, this);
		}
	}

}
