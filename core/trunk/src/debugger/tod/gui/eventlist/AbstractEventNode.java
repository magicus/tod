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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.GUIUtils;
import tod.gui.IGUIManager;
import tod.gui.JobProcessor;
import tod.gui.formatter.CustomFormatterRegistry;
import tod.gui.kit.BusPanel;
import zz.utils.Utils;

/**
 * Base class for all event nodes.
 * @author gpothier
 */
public abstract class AbstractEventNode extends BusPanel
{
	private final EventListPanel itsListPanel;
	
	private JComponent itsCaption;
	private JComponent itsGutter;
	
	public AbstractEventNode(EventListPanel aListPanel)
	{
		super(aListPanel.getBus());
		itsListPanel = aListPanel;
	}
	
	public EventListPanel getListPanel()
	{
		return itsListPanel;
	}
	
	public ILogBrowser getLogBrowser()
	{
		return getListPanel().getLogBrowser();
	}
	
	public TODConfig getConfig()
	{
		return getLogBrowser().getSession().getConfig();
	}

	public JobProcessor getJobProcessor()
	{
		return getListPanel().getJobProcessor();
	}

	public IGUIManager getGUIManager()
	{
		return getListPanel().getGUIManager();
	}
	
	/**
	 * Default UI creation. 
	 * The html component is placed at the center of a {@link BorderLayout}.
	 */
	protected void createUI()
	{
		setLayout(GUIUtils.createBorderLayout());
		removeAll();
		itsGutter = null;
		itsCaption = null;
		add(getCenterComponent(), BorderLayout.CENTER);
	}
	
	
	/**
	 * Adds a component to this node's gutter.
	 */
	protected void addToCaption(JComponent aComponent)
	{
		if (itsCaption == null)
		{
			itsCaption = new Box(BoxLayout.X_AXIS);
			itsCaption.setOpaque(false);
			add(itsCaption, BorderLayout.NORTH);
		}
		
		itsCaption.add(aComponent);
		revalidate();
		repaint();
	}
	
	/**
	 * Adds a component to this node's gutter.
	 */
	protected void addToGutter(JComponent aComponent)
	{
		if (itsGutter == null)
		{
			itsGutter = new Box(BoxLayout.X_AXIS);
			itsGutter.setOpaque(false);
			add(itsGutter, BorderLayout.WEST);
		}
		
		itsGutter.add(aComponent);
		revalidate();
		repaint();
	}
	
	
	/**
	 * Returns the component that displays the html text.
	 * Subclasses should use this method when they create their GUI.
	 */
	protected abstract JComponent getCenterComponent();
	
	/**
	 * Whether package names should be displayed.
	 */
	protected boolean showPackageNames()
	{
		return true;
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
