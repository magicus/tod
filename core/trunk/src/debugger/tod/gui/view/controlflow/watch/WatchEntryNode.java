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

import javax.swing.JPanel;

import tod.core.database.browser.ICompoundInspector.EntryValue;
import tod.core.database.event.ILogEvent;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;
import tod.gui.JobProcessor;
import tod.gui.kit.AsyncPanel;
import tod.gui.view.controlflow.watch.AbstractWatchProvider.Entry;

/**
 * Represents a watch entry (field or variable).
 * @author gpothier
 */
public class WatchEntryNode extends JPanel
{
	private final JobProcessor itsJobProcessor;
	
	private final IGUIManager itsGUIManager;
	private final WatchPanel itsWatchPanel;
	private final AbstractWatchProvider itsProvider;
	private final Entry itsEntry;
	
	private EntryValue[] itsValue;
	
	public WatchEntryNode(
			IGUIManager aGUIManager,
			JobProcessor aJobProcessor,
			WatchPanel aWatchPanel,
			AbstractWatchProvider aProvider, 
			Entry aEntry)
	{
		super(GUIUtils.createSequenceLayout());
		itsWatchPanel = aWatchPanel;
		setOpaque(false);
		itsJobProcessor = aJobProcessor;
		itsGUIManager = aGUIManager;
		itsProvider = aProvider;
		itsEntry = aEntry;
		createUI();
	}
	
	protected boolean showPackageNames()
	{
		return itsWatchPanel.showPackageNames();
	}

	private void createUI()
	{
		String theName = itsEntry.getName();
		add(GUIUtils.createLabel(theName + " = "));
		add(new AsyncPanel(itsJobProcessor)
		{
			@Override
			protected void runJob()
			{
				itsValue = itsEntry.getValue();
			}

			@Override
			protected void update()
			{
				if (itsValue != null)
				{
					boolean theFirst = true;
					for (int i=0;i<itsValue.length;i++)
					{
						if (theFirst) theFirst = false;
						else add(GUIUtils.createLabel(" / "));
						
						add(Hyperlinks.object(
								itsGUIManager, 
								Hyperlinks.SWING, 
								itsJobProcessor,
								itsProvider.getCurrentObject(),
								itsValue[i].value,
								itsProvider.getRefEvent(),
								showPackageNames()));
						
						ILogEvent theSetter = itsValue[i].setter;
						if (theSetter != null)
						{
							add(GUIUtils.createLabel(" ("));
							add(Hyperlinks.event(itsGUIManager, Hyperlinks.SWING, "why?", theSetter));
							add(GUIUtils.createLabel(")"));
						}
					}
				}
			}
		});
	}
	
	
}
