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

import tod.Util;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.structure.IClassInfo;
import tod.gui.JobProcessor;
import tod.gui.kit.html.HtmlElement;
import tod.gui.kit.html.HtmlText;

public class InstantiationNode extends BehaviorCallNode
{

	public InstantiationNode(
			EventListPanel aListPanel,
			IBehaviorCallEvent aEvent)
	{
		super(aListPanel, aEvent);
	}

	public IInstantiationEvent getEvent()
	{
		return (IInstantiationEvent) super.getEvent();
	}
	
	@Override
	protected HtmlElement createBehaviorNamePrefix()
	{
		return HtmlText.create("new ");
	}
	
	@Override
	protected HtmlElement createFullBehaviorName()
	{
		return createShortBehaviorName();
	}
	
	@Override
	protected HtmlElement createShortBehaviorName()
	{
		IClassInfo theType = getBehavior().getType();
		return HtmlText.create(
				showPackageNames() ? 
						theType.getName()
						: Util.getSimpleInnermostName(theType.getName()));
	}
	
	@Override
	protected String getResultPrefix()
	{
		return "Created";
	}

	@Override
	protected Object getResult()
	{
		return getEvent().getInstance();
	}
}