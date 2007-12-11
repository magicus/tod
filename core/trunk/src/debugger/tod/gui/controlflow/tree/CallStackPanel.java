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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
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
import zz.utils.Utils;
import zz.utils.ui.GridStackLayout;
import zz.utils.ui.MousePanel;
import zz.utils.ui.ScrollablePanel;
import zz.utils.ui.StackLayout;

/**
 * A panel that displays a call stack, ie. a list of the ancestors
 * of a given event.
 * @author gpothier
 */
public class CallStackPanel extends JPanel
{
	private final ILogBrowser itsLogBrowser;
	private final JobProcessor itsJobProcessor;
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
		if (! theRootEvent.equals(theLastAdded)) theAncestors.add(theRootEvent);
		
		JPanel theContainer = new ScrollablePanel(new GridStackLayout(1, 0, 2, true, false));

		
		if (theAncestors.size() > 0) for(int i=0;i<theAncestors.size();i++)
		{
			IParentEvent theAncestor = theAncestors.get(i);
			theContainer.add(buildStackNode(theAncestor, i==0));
		}

		return theContainer;
	}
	
	
	private AbstractStackNode buildStackNode(IParentEvent aEvent, boolean aCurrentFrame)
	{
//		JobProcessor theJobProcessor = getJobProcessor();
		JobProcessor theJobProcessor = null;
		
		if (Utils.equalOrBothNull(aEvent, getRootEvent()))
		{
			return new RootStackNode(
					theJobProcessor,
					aEvent,
					aCurrentFrame);
		}
		else if (aEvent instanceof IMethodCallEvent)
		{
			return new NormalStackNode(
					theJobProcessor, 
					(IMethodCallEvent) aEvent, 
					aCurrentFrame);
		}
		else if (aEvent instanceof IInstantiationEvent)
		{
			return new NormalStackNode(
					theJobProcessor, 
					(IInstantiationEvent) aEvent, 
					aCurrentFrame);
		}
		else if (aEvent instanceof IConstructorChainingEvent)
		{
			return new NormalStackNode(
					theJobProcessor, 
					(IConstructorChainingEvent) aEvent, 
					aCurrentFrame);
		}
		else throw new RuntimeException("Not handled: "+aEvent);
	}
	
}

