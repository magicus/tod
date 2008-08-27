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
package tod.gui.activities.objecthistory;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JTabbedPane;

import tod.core.database.browser.ObjectIdUtils;
import tod.gui.FontConfig;
import tod.gui.IContext;
import tod.gui.IGUIManager;
import tod.gui.activities.ActivityPanel;
import tod.gui.kit.html.HtmlComponent;
import tod.gui.kit.html.HtmlDoc;
import zz.utils.ui.StackLayout;

public class ObjectHistoryActivityPanel extends ActivityPanel<ObjectHistorySeed> 
{
	private ObjectEventsListPanel itsEventsListPanel;
	private ObjectMethodsPanel itsMethodsPanel;
	private HtmlComponent itsTitleComponent;
	
	public ObjectHistoryActivityPanel(IContext aContext)
	{
		super(aContext);
	}
	
	@Override
	public void init()
	{
		setLayout(new BorderLayout());

		// Title
		itsTitleComponent = new HtmlComponent();
		
		itsTitleComponent.setOpaque(false);
		add(itsTitleComponent, BorderLayout.NORTH);
		
		// Tabbed pane
		JTabbedPane theTabbedPane = new JTabbedPane();
		add(theTabbedPane, BorderLayout.CENTER);
		
		itsEventsListPanel = new ObjectEventsListPanel(this);
		theTabbedPane.add("All events", itsEventsListPanel);
		
		itsMethodsPanel = new ObjectMethodsPanel(this);
		theTabbedPane.add("By method", itsMethodsPanel);
	}
	
	@Override
	protected void connectSeed(ObjectHistorySeed aSeed)
	{
		String theTitle = ObjectIdUtils.getObjectDescription(
				getLogBrowser(), 
				aSeed.getObject(), 
				false);
		
		itsTitleComponent.setDoc(HtmlDoc.create("<b>"+theTitle+"</b>", FontConfig.BIG, Color.BLACK));

		itsEventsListPanel.connectSeed(aSeed);
		itsMethodsPanel.connectSeed(aSeed);
	}
	
	@Override
	protected void disconnectSeed(ObjectHistorySeed aSeed)
	{
		itsEventsListPanel.disconnectSeed(aSeed);
		itsMethodsPanel.disconnectSeed(aSeed);
	}
}

