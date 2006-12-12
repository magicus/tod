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
package tod.gui.controlflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import zz.csg.api.layout.StackLayout;

public class RootEventNode extends AbstractEventNode
{
	private IParentEvent itsRootEvent;
	
	private Map<ILogEvent, AbstractEventNode> itsNodesMap = 
		new HashMap<ILogEvent, AbstractEventNode>();
	

	public RootEventNode(
			CFlowView aView,
			IParentEvent aRootEvent)
	{
		super (aView);
		itsRootEvent = aRootEvent;
		
		setLayoutManager(new StackLayout());

		List<AbstractEventNode> theNodes = getBuilder().buildNodes(itsRootEvent);
		
		for (AbstractEventNode theNode : theNodes)
		{
			pChildren().add(theNode);
			itsNodesMap.put(theNode.getEvent(), theNode);
		}		
	}
	
	@Override
	protected ILogEvent getEvent()
	{
		return itsRootEvent;
	}
	
	@Override
	public AbstractEventNode getNode(ILogEvent aEvent)
	{
		AbstractEventNode theNode = super.getNode(aEvent);
		if (theNode != null) return theNode;
		else return itsNodesMap.get(aEvent);
	}
}
