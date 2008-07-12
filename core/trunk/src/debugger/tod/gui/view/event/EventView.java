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
package tod.gui.view.event;

import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.gui.GUIUtils;
import tod.gui.IGUIManager;
import tod.gui.SeedHyperlink;
import tod.gui.seed.CFlowSeed;
import tod.gui.seed.FilterSeed;
import tod.gui.seed.LogViewSeed;
import tod.gui.view.LogView;

/**
 * Base class for event viewers. It sets a framework for the UI:
 * subclasses should override the {@link #init()} method
 * and create their UI here after calling super.
 * They should add the components to the panel with no
 * layout constraints; they will be stacked vertically.
 * @author gpothier
 */
public abstract class EventView<T extends LogViewSeed> extends LogView<T>
{
	public EventView(IGUIManager aManager)
	{
		super (aManager);
	}
	
	@Override
	public void init()
	{
		setLayout(GUIUtils.createStackLayout());
		add (createTitleLabel(getEventFormatter().getPlainText(getEvent())));
		
		ILogEvent theEvent = getEvent();
		IThreadInfo theThread = theEvent.getThread();
		IHostInfo theHost = theThread.getHost();
		
		// Thread, host & timestamp
		add (createTitledLink(
				"Host: ", 
				"\""+theHost.getName()+"\" ["+theHost.getId()+"]", 
				new FilterSeed (
						getLogBrowser(),
						"All events of host: "+theHost.getName(),
						getLogBrowser().createHostFilter(theHost))));
		
		add (createTitledLink(
				"(>" + theEvent.getDepth() + ") Thread: ", 
				"\""+theThread.getName()+"\" ["+theThread.getId()+"]", 
				new FilterSeed (
						getLogBrowser(), 
						"All events of thread: "+theThread.getDescription(),
						getLogBrowser().createThreadFilter(theThread))));

		add (createTitledPanel(
				"Timestamp: ", 
				GUIUtils.createLabel(""+theEvent.getTimestamp())));

		
		// CFlow
		SeedHyperlink theCFlowLabel = SeedHyperlink.create(
				getGUIManager(),
				new CFlowSeed(getLogBrowser(), theEvent),
				"View control flow");
		
		add (theCFlowLabel);
		
	}

	/**
	 * Returns the event represented by this view.
	 */
	protected abstract ILogEvent getEvent ();
	
}
