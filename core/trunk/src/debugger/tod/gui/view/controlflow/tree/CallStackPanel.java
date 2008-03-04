/*
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
import tod.core.database.event.IConstructorChainingEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IMethodCallEvent;
import tod.core.database.event.IParentEvent;
import tod.gui.JobProcessor;
import tod.gui.kit.Bus;
import tod.gui.kit.messages.EventSelectedMsg;
import tod.gui.kit.messages.EventSelectedMsg.SelectionMethod;
import zz.utils.Utils;
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
	private final ILogBrowser itsLogBrowser;

	private final JobProcessor itsJobProcessor;

	/**
	 * the leaf is event is the event selected in the event list
	 */
	private ILogEvent itsLeafEvent;

	/**
	 * Root of the control flow tree for the current leaf event
	 */
	private IParentEvent itsRootEvent;

	private JScrollPane itsScrollPane;

	public CallStackPanel(ILogBrowser aLogBrowser, JobProcessor aJobProcessor)
	{
		itsLogBrowser = aLogBrowser;
		itsJobProcessor = aJobProcessor;
		createUI();
	}

	public JobProcessor getJobProcessor()
	{
		return itsJobProcessor;
	}

	public ILogBrowser getLogBrowser()
	{
		return itsLogBrowser;
	}

	public void setLeafEvent(ILogEvent aEvent)
	{
		itsLeafEvent = aEvent;
		itsRootEvent = null;
		update();
	}

	public ILogEvent getLeafEvent()
	{
		return itsLeafEvent;
	}

	public IParentEvent getRootEvent()
	{
		if (itsRootEvent == null && itsLeafEvent != null)
		{
			itsRootEvent = getLogBrowser().getCFlowRoot(itsLeafEvent.getThread());
		}
		return itsRootEvent;
	}

	/**
	 * return the child of the stack event given in parameter. Return null if
	 * this stack event has no child (i.e. it is the parent of the leafevent)
	 */
	public IParentEvent getChildOf(IParentEvent aEvent)
	{
		IParentEvent theChild = getLeafEvent().getParent();
		if (aEvent == theChild) return null;
		IParentEvent theParent = theChild.getParent();
		while (theParent != aEvent && theParent != null) //parent == null when the aEvent is root
		{
			theChild = theParent;
			theParent = theChild.getParent();
		}
		
		return theChild;
	}

	/**
	 * show the child event of the IParentEvent given in parameter in the
	 * current event list
	 */
	public void selectChildOf(IParentEvent aEvent)
	{
		IParentEvent theChildEvent = getChildOf(aEvent);
		if (theChildEvent != null) 
			Bus.get(this).postMessage(new EventSelectedMsg(theChildEvent, SelectionMethod.SELECT_IN_CALL_STACK));
	}

	private void createUI()
	{
		itsScrollPane = new JScrollPane();
		setLayout(new StackLayout());
		add(itsScrollPane);
	}

	@Override
	public void addNotify()
	{
		super.addNotify();
	}

	private void update()
	{
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
		List<IParentEvent> theAncestors = new ArrayList<IParentEvent>();
		IParentEvent theCurrentParent = getLeafEvent().getParent();
		IParentEvent theLastAdded = null;
		while (theCurrentParent != null)
		{
			theAncestors.add(theCurrentParent);
			theLastAdded = theCurrentParent;
			theCurrentParent = theCurrentParent.getParent();
		}

		IParentEvent theRootEvent = getRootEvent();
		if (!theRootEvent.equals(theLastAdded)) theAncestors.add(theRootEvent);

		JPanel theContainer = new ScrollablePanel(new GridStackLayout(1, 0, 2, true, false));

		if (theAncestors.size() > 0) for (int i = 0; i < theAncestors.size(); i++)
		{
			IParentEvent theAncestor = theAncestors.get(i);
			theContainer.add(buildStackNode(theAncestor, i == 0));
		}

		return theContainer;
	}

	private AbstractStackNode buildStackNode(IParentEvent aEvent, boolean aCurrentFrame)
	{
		// JobProcessor theJobProcessor = getJobProcessor();
		JobProcessor theJobProcessor = null;

		if (Utils.equalOrBothNull(aEvent, getRootEvent()))
		{
			return new RootStackNode(theJobProcessor, aEvent, aCurrentFrame,this);
		}
		else if (aEvent instanceof IMethodCallEvent)
		{
			return new NormalStackNode(theJobProcessor, (IMethodCallEvent) aEvent, aCurrentFrame,this);
		}
		else if (aEvent instanceof IInstantiationEvent)
		{
			return new NormalStackNode(theJobProcessor, (IInstantiationEvent) aEvent, aCurrentFrame,this);
		}
		else if (aEvent instanceof IConstructorChainingEvent)
		{
			return new NormalStackNode(theJobProcessor, (IConstructorChainingEvent) aEvent, aCurrentFrame,this);
		}
		else throw new RuntimeException("Not handled: " + aEvent);
	}

}
