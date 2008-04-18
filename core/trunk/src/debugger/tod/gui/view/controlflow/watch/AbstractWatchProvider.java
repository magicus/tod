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
package tod.gui.view.controlflow.watch;

import java.util.List;

import javax.swing.JComponent;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.ICompoundInspector.EntryValue;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ObjectId;
import tod.gui.IGUIManager;
import tod.gui.JobProcessor;

/**
 * Provider of watch data.
 * @author gpothier
 */
public abstract class AbstractWatchProvider
{
	private final IGUIManager itsGUIManager;
	private final String itsTitle;
	
	public AbstractWatchProvider(IGUIManager aGUIManager, String aTitle)
	{
		itsTitle = aTitle;
		itsGUIManager = aGUIManager;
	}

	/**
	 * Builds the title of the watch window.
	 * @param aJobProcessor A job processor that can be used if elements
	 * of the title are to be created asynchronously.
	 */
	public abstract JComponent buildTitleComponent(JobProcessor aJobProcessor);
	
	/**
	 * Returns a title for this watch provider.
	 */
	public String getTitle()
	{
		return itsTitle;
	}
	
	public IGUIManager getGUIManager()
	{
		return itsGUIManager;
	}
	
	public ILogBrowser getLogBrowser()
	{
		return getGUIManager().getSession().getLogBrowser();
	}


	/**
	 * Returns a current object. Currently this is only for
	 * stack frame reconstitution, represents the "this" variable.
	 */
	public abstract ObjectId getCurrentObject();
	
	/**
	 * Returns the event that serves as a temporal reference for the watched objects.
	 */
	public abstract ILogEvent getRefEvent();
	
	/**
	 * Returns the list of available entries.
	 * This might be a time-consuming operation.
	 */
	public abstract List<Entry> getEntries();

	public static abstract class Entry
	{
		/**
		 * Returns the name of this entry.
		 * This method should execute quickly.
		 */
		public abstract String getName();
		
		/**
		 * Returns the possible values for this entry.
		 * This might be a time-consuming operation.
		 */
		public abstract EntryValue[] getValue();
	}
}
