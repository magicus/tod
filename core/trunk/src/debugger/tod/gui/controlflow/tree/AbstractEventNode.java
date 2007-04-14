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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import tod.core.database.event.ILogEvent;
import tod.gui.JobProcessor;
import tod.gui.controlflow.CFlowView;

public abstract class AbstractEventNode extends AbstractCFlowNode
{

	public AbstractEventNode(
			CFlowView aView,
			JobProcessor aJobProcessor)
	{
		super(aView, aJobProcessor);
	}

	/**
	 * Whether package names should be displayed.
	 */
	protected boolean showPackageNames()
	{
		return getView().showPackageNames();
	}
	
	@Override
	public void mousePressed(MouseEvent aE)
	{
		ILogEvent theMainEvent = getEvent();
		if (theMainEvent != null)
		{
			getView().selectEvent(theMainEvent);
			aE.consume();			
		}
	}
	
	@Override
	protected void paintComponent(Graphics aG)
	{
		ILogEvent theMainEvent = getEvent();
		boolean theSelected = theMainEvent != null 
				&& getView().isEventSelected(theMainEvent);
		
		aG.setColor(theSelected ? Color.YELLOW : Color.WHITE);
		aG.fillRect(0, 0, getWidth(), getHeight());
	}

	/**
	 * Returns the event that corresponds to this node.
	 */
	protected abstract ILogEvent getEvent();

	/**
	 * Searches the node that corresponds to the given event in this node's
	 * hierarchy.
	 */
	public AbstractEventNode getNode(ILogEvent aEvent)
	{
		if (aEvent == getEvent()) return this;
		else return null;
	}
	
	public void expand()
	{
	}

	public void collapse()
	{
	}

}
