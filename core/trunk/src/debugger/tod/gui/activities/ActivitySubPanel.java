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
package tod.gui.activities;

import javax.swing.JPanel;

import tod.core.database.browser.ILogBrowser;
import tod.gui.IGUIManager;
import tod.gui.kit.Bus;
import tod.gui.kit.IBusOwner;
import tod.tools.scheduling.IJobScheduler;
import tod.tools.scheduling.IJobSchedulerProvider;
import zz.utils.properties.IRWProperty;

/**
 * An abstract base class that eases the creation of sub panels for activity panels.
 * @author gpothier
 */
public abstract class ActivitySubPanel<T extends ActivitySeed> extends JPanel
implements IJobSchedulerProvider, IBusOwner
{
	private final ActivityPanel<T> itsActivityPanel;

	public ActivitySubPanel(ActivityPanel<T> aActivityPanel)
	{
		itsActivityPanel = aActivityPanel;
	}
	
	protected IGUIManager getGUIManager()
	{
		return itsActivityPanel.getGUIManager();
	}
	
	protected ILogBrowser getLogBrowser()
	{
		return itsActivityPanel.getLogBrowser();
	}

	public IJobScheduler getJobScheduler()
	{
		return itsActivityPanel.getJobScheduler();
	}

	protected T getSeed()
	{
		return itsActivityPanel.getSeed();
	}
	
	/**
	 * Called by the view when {@link ActivityPanel#connectSeed(ActivitySeed)}
	 * is called.
	 */
	public void connectSeed(T aSeed)
	{
	}
	
	/**
	 * Called by the view when {@link ActivityPanel#disconnectSeed(ActivitySeed)}
	 * is called.
	 */
	public void disconnectSeed(T aSeed)
	{
	}
	
	protected <V> void connect (IRWProperty<V> aSource, IRWProperty<V> aTarget)
	{
		itsActivityPanel.connect(aSource, aTarget);
	}
	
	protected <V> void disconnect (IRWProperty<V> aSource, IRWProperty<V> aTarget)
	{
		itsActivityPanel.disconnect(aSource, aTarget);
	}

	public Bus getBus() 
	{
		return itsActivityPanel.getBus();
	}

}
