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

import tod.core.database.event.IArrayWriteEvent;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import tod.gui.controlflow.CFlowView;

public class ArrayWriteNode extends AbstractEventNode
{
	private IArrayWriteEvent itsEvent;

	public ArrayWriteNode(
			CFlowView aView,
			JobProcessor aJobProcessor,
			IArrayWriteEvent aEvent)
	{
		super(aView, aJobProcessor);
		itsEvent = aEvent;
		createUI();
	}
	
	protected void createUI()
	{
		Object theCurrentObject = null;
		IBehaviorCallEvent theContainer = itsEvent.getParent();
		if (theContainer != null)
		{
			theCurrentObject = theContainer.getTarget();
		}
		
		add(Hyperlinks.object(
				getLogBrowser(), 
				getJobProcessor(), 
				theCurrentObject, 
				itsEvent.getTarget(),
				itsEvent,
				FontConfig.STD_FONT,
				showPackageNames()));
		
		add(GUIUtils.createLabel("["));
		add(GUIUtils.createLabel(""+itsEvent.getIndex()));
		add(GUIUtils.createLabel("] = "));
		
		add(Hyperlinks.object(
				getLogBrowser(), 
				getJobProcessor(), 
				theCurrentObject, 
				itsEvent.getValue(), 
				itsEvent,
				FontConfig.STD_FONT,
				showPackageNames()));
	}
	
	@Override
	protected ILogEvent getEvent()
	{
		return itsEvent;
	}
}
