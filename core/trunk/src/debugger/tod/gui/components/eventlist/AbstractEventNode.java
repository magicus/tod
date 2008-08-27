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
package tod.gui.components.eventlist;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.GUIUtils;
import tod.gui.IGUIManager;
import tod.gui.kit.BusPanel;
import tod.tools.scheduling.IJobScheduler;
import tod.tools.scheduling.IJobSchedulerProvider;
import zz.utils.Utils;

/**
 * Base class for all event nodes.
 * @author gpothier
 */
public abstract class AbstractEventNode extends BusPanel
implements IJobSchedulerProvider
{
	private final IGUIManager itsGUIManager;
	private final EventListPanel itsListPanel;
	
	private JComponent itsCaption;
	private JComponent itsGutter;
	
	/**
	 * We need to give the gui manager separate from the list panel
	 * because the list panel can be null.
	 */
	public AbstractEventNode(IGUIManager aGUIManager, EventListPanel aListPanel)
	{
		super(aListPanel != null ? aListPanel.getBus() : null);
		itsGUIManager = aGUIManager;
		itsListPanel = aListPanel;
	}
	
	public EventListPanel getListPanel()
	{
		return itsListPanel;
	}
	
	public IGUIManager getGUIManager()
	{
		return itsGUIManager;
	}
	
	public ILogBrowser getLogBrowser()
	{
		if (getGUIManager() == null) return null;
		return getGUIManager().getSession().getLogBrowser();
	}
	
	public TODConfig getConfig()
	{
		if (getLogBrowser() == null) return null;
		return getLogBrowser().getSession().getConfig();
	}

	public IJobScheduler getJobScheduler()
	{
		return getListPanel() != null ? 
				getListPanel().getJobScheduler()
				: getGUIManager().getJobScheduler();
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

		revalidate();
		repaint();
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
