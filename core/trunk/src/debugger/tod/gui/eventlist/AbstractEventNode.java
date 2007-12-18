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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import tod.agent.DebugFlags;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILogEvent;
import tod.gui.GUIUtils;
import tod.gui.JobProcessor;
import tod.gui.kit.Bus;
import tod.gui.kit.html.HtmlBody;
import tod.gui.kit.html.HtmlComponent;
import tod.gui.kit.html.HtmlDoc;
import tod.gui.kit.html.HtmlElement;
import tod.gui.kit.html.HtmlParentElement;
import tod.gui.kit.html.HtmlText;
import tod.gui.kit.messages.EventActivatedMsg;
import tod.gui.kit.messages.EventSelectedMsg;
import tod.gui.kit.messages.EventActivatedMsg.ActivationMethod;
import tod.gui.kit.messages.EventSelectedMsg.SelectionMethod;
import zz.utils.Utils;
import zz.utils.ui.MousePanel;

public abstract class AbstractEventNode extends MousePanel
{
	private final EventListPanel itsListPanel;
	
	private HtmlComponent itsHtmlComponent;
	private HtmlDoc itsDoc;
	
	public AbstractEventNode(EventListPanel aListPanel)
	{
		itsListPanel = aListPanel;
		
		itsDoc = new HtmlDoc();
		itsHtmlComponent = new HtmlComponent();
		itsHtmlComponent.setOpaque(false);
		itsHtmlComponent.setDoc(itsDoc);
		itsHtmlComponent.addMouseListener(this);
	}

	public EventListPanel getListPanel()
	{
		return itsListPanel;
	}
	
	public ILogBrowser getLogBrowser()
	{
		return getListPanel().getLogBrowser();
	}

	public JobProcessor getJobProcessor()
	{
		return getListPanel().getJobProcessor();
	}

	/**
	 * Default UI creation. 
	 * The html component is placed at the center of a {@link BorderLayout}.
	 */
	protected void createUI()
	{
		setLayout(GUIUtils.createBorderLayout());
		add(getHTMLComponent(), BorderLayout.CENTER);
		updateHtml();
	}
	
	protected void updateHtml()
	{
		HtmlBody theBody = itsDoc.getRoot();
		theBody.clear();
		createHtmlUI(theBody);
		itsDoc.update(theBody);
	}
	
	protected abstract void createHtmlUI(HtmlBody aBody);
	
	/**
	 * Adds debugging info to the given element, if debugging info is enabled.
	 */
	protected void createDebugInfo(HtmlParentElement aParent)
	{
		if (DebugFlags.SHOW_DEBUG_GUI)
		{
			String theLocation = "?";
			if (getEvent() instanceof ICallerSideEvent)
			{
				ICallerSideEvent theCallerSideEvent = (ICallerSideEvent) getEvent();
				theLocation = ""+theCallerSideEvent.getOperationBytecodeIndex();
			}
			
			aParent.add(HtmlText.createf(
					" (ts: %d, loc: %s)",
					getEvent().getTimestamp(),
					theLocation));
		}
	}
	
	/**
	 * Returns the component that displays the html text.
	 * Subclasses should use this method when they create their GUI.
	 */
	protected JComponent getHTMLComponent()
	{
		return itsHtmlComponent;
	}
	
	/**
	 * Whether package names should be displayed.
	 */
	protected boolean showPackageNames()
	{
		return true;
	}
	
	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		getListPanel().pSelectedEvent().set(getEvent());
		ILogEvent theEvent = getEvent();
		Bus.get(this).postMessage(new EventSelectedMsg(theEvent, SelectionMethod.SELECT_IN_LIST));
		aEvent.consume();			
		
		if (aEvent.getClickCount() == 2)
		{
			Bus.get(this).postMessage(new EventActivatedMsg(getEvent(), ActivationMethod.DOUBLE_CLICK));
			getListPanel().eventActivated(getEvent());
		}
	}
	
	/**
	 * Called when this event is selected.
	 */
	protected void selected()
	{
//		updateHtml();
		repaint();
	}

	/**
	 * Called when this node is deselected.
	 */
	protected void deselected()
	{
//		updateHtml();
		repaint();
	}
	
	protected boolean isSelected()
	{
		return Utils.equalOrBothNull(getEvent(), getListPanel().pSelectedEvent().get());
	}
	
	@Override
	protected void paintComponent(Graphics aG)
	{
		aG.setColor(isSelected() ? Color.YELLOW : Color.WHITE);
		aG.fillRect(0, 0, getWidth(), getHeight());
	}

	/**
	 * Returns the event that corresponds to this node.
	 */
	protected abstract ILogEvent getEvent();

	/**
	 * Searches the node that corresponds to the given event in this node's
	 * hierarchy.
	 */
	public AbstractEventNode getNode(ILogEvent aEvent)
	{
		if (Utils.equalOrBothNull(aEvent, getEvent())) return this;
		else return null;
	}
}
