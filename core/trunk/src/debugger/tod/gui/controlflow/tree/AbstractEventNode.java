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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import tod.core.database.event.ILogEvent;
import tod.gui.GUIUtils;
import tod.gui.JobProcessor;
import tod.gui.MinerUI;
import tod.gui.controlflow.CFlowView;
import tod.gui.kit.Bus;
import tod.gui.kit.IBusListener;
import tod.gui.kit.OptionManager;
import tod.gui.kit.StdOptions;
import tod.gui.kit.html.HtmlBody;
import tod.gui.kit.html.HtmlComponent;
import tod.gui.kit.html.HtmlDoc;
import tod.gui.kit.html.HtmlElement;
import tod.gui.kit.messages.EventSelectedMsg;
import tod.gui.kit.messages.ShowCFlowMsg;
import tod.gui.kit.messages.EventSelectedMsg.SelectionMethod;
import zz.utils.properties.SimpleRWProperty;

public abstract class AbstractEventNode extends AbstractCFlowNode
{
	private HtmlComponent itsHtmlComponent;
	private HtmlDoc itsDoc;
	
	private boolean itsWasSelected;
	
	private IBusListener<EventSelectedMsg> itsEventSelectedListener = new IBusListener<EventSelectedMsg>()
	{
		public boolean processMessage(EventSelectedMsg aMessage)
		{
			if (aMessage.getEvent().equals(getEvent()))
			{
				if (! itsWasSelected)
				{
					selected();
					itsWasSelected = true;
				}
			}
			else
			{
				if (itsWasSelected)
				{
					deselected();
					itsWasSelected = false;
				}
			}
			return false;
		}
	};
	


	public AbstractEventNode(
			CFlowView aView,
			JobProcessor aJobProcessor)
	{
		super(aView, aJobProcessor);
		
		itsDoc = new HtmlDoc();
		itsHtmlComponent = new HtmlComponent();
		itsHtmlComponent.setOpaque(false);
		itsHtmlComponent.setDoc(itsDoc);
		itsHtmlComponent.addMouseListener(this);
	}

	@Override
	public void addNotify()
	{
		super.addNotify();
		itsWasSelected = isSelected();
		Bus.getBus(this).subscribe(EventSelectedMsg.ID, itsEventSelectedListener);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		Bus.getBus(this).unsubscribe(EventSelectedMsg.ID, itsEventSelectedListener);
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
		return getView().showPackageNames();
	}
	
	@Override
	public void mousePressed(MouseEvent aE)
	{
		ILogEvent theMainEvent = getEvent();
		if (theMainEvent != null)
		{
			getView().selectEvent(theMainEvent, SelectionMethod.SELECT_IN_LIST);
			aE.consume();			
		}
	}
	
	/**
	 * Called when this event is selected.
	 */
	protected void selected()
	{
		updateHtml();
	}

	/**
	 * Called when this node is deselected.
	 */
	protected void deselected()
	{
		updateHtml();
	}
	
	protected boolean isSelected()
	{
		ILogEvent theMainEvent = getEvent();
		return theMainEvent != null && getView().isEventSelected(theMainEvent);
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
		if (aEvent == getEvent()) return this;
		else return null;
	}

	private static class AAEditorPane extends JEditorPane
	{
		public AAEditorPane()
		{
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			super.paintComponent(g2);
		}
	}

}
