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

import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import tod.gui.controlflow.CFlowView;

public class LocalVariableWriteNode extends AbstractEventNode
{
	private ILocalVariableWriteEvent itsEvent;

	public LocalVariableWriteNode(
			CFlowView aView,
			JobProcessor aJobProcessor,
			ILocalVariableWriteEvent aEvent)
	{
		super(aView, aJobProcessor);
		itsEvent = aEvent;
		createUI();
	}
	
	protected void createUI()
	{
		add(GUIUtils.createLabel(itsEvent.getVariable().getVariableName()));
		add(GUIUtils.createLabel(" = "));
		
		add(Hyperlinks.object(
				getLogBrowser(),
				getJobProcessor(),
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
