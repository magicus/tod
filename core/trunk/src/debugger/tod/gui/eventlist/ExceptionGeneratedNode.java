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

import java.awt.Color;

import tod.Util;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.gui.FontConfig;
import tod.gui.Hyperlinks;
import tod.gui.kit.StdOptions;
import tod.gui.kit.html.HtmlBody;
import tod.gui.kit.html.HtmlText;

public class ExceptionGeneratedNode extends AbstractSimpleEventNode
{
	private IExceptionGeneratedEvent itsEvent;

	public ExceptionGeneratedNode(
			EventListPanel aListPanel,
			IExceptionGeneratedEvent aEvent)
	{
		super(aListPanel);
		itsEvent = aEvent;
		createUI();
	}
	
	@Override
	protected void createHtmlUI(HtmlBody aBody)
	{
		boolean theRed = getListPanel().getCurrentValue(StdOptions.EXCEPTION_EVENTS_RED);
		
		aBody.add(HtmlText.create(
				"Exception: ", 
				FontConfig.NORMAL, 
				theRed ? Color.RED : Color.BLACK));
		
		aBody.add(Hyperlinks.object(
				Hyperlinks.HTML,
				getGUIManager(),
				getJobProcessor(),
				itsEvent.getException(),
				itsEvent,
				showPackageNames()));

		boolean theLocation = getListPanel().getCurrentValue(StdOptions.SHOW_EVENTS_LOCATION);
		if (theLocation)
		{
			IBehaviorInfo theBehavior = getEvent().getOperationBehavior();
			String theBehaviorName = theBehavior != null ? 
					Util.getSimpleName(theBehavior.getType().getName()) + "." + theBehavior.getName() 
					: "<unknown>"; 
			
			aBody.addText(" (in "+theBehaviorName+")");
		}
				
		createDebugInfo(aBody);
	}
	
	@Override
	protected IExceptionGeneratedEvent getEvent()
	{
		return itsEvent;
	}

}
