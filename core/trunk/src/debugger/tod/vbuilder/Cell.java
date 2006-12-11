/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.vbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.database.structure.ObjectId;
import zz.csg.api.IGraphicContainer;
import zz.csg.impl.SVGGraphicContainer;

public class Cell
{
	private List<ICellListener> itsListeners = new ArrayList<ICellListener>();
	private Map<ObjectId, IObjectNode> itsNodesMap = new HashMap<ObjectId, IObjectNode>();
	
	private List<CellEvent> itsEventsQueue = new ArrayList<CellEvent>();
	
	/**
	 * The container into which the imported nodes will be placed.
	 */
	private IGraphicContainer itsGraphicContainer;
	
	/**
	 * Creates a cell with a new graphic container.
	 */
	public Cell()
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		theContainer.pBounds().set(0, 0, 500, 100);
		itsGraphicContainer = theContainer;
	}
	
	public Cell(IGraphicContainer aContainer)
	{
		itsGraphicContainer = aContainer;
	}

	public IGraphicContainer getGraphicContainer()
	{
		return itsGraphicContainer;
	}

	public void setGraphicContainer(IGraphicContainer aGraphicContainer)
	{
		itsGraphicContainer = aGraphicContainer;
	}

	public void addListener(ICellListener aCellListener)
	{
		itsListeners.add(aCellListener);
	}
	
	public void removeCellListener (ICellListener aCellListener)
	{
		itsListeners.remove(aCellListener);
	}
	
	public void queueEvent(CellEvent aEvent)
	{
		itsEventsQueue.add(aEvent);
	}
	
	protected void fireEvents()
	{
		while (! itsEventsQueue.isEmpty())
		{
			List<CellEvent> theQueueCopy = new ArrayList<CellEvent>(itsEventsQueue);
			itsEventsQueue.clear();
			
			for (ICellListener theListener : itsListeners) theListener.changed(theQueueCopy);
		}
	}
	
	/**
	 * Creates and returns a node for the specified object. If a node for this object already
	 * exists in this cell, it is returned and no new node is created.
	 */
	public IObjectNode importNode (ObjectId aObjectId)
	{
		IObjectNode theNode = itsNodesMap.get(aObjectId);
		if (theNode == null)
		{
			theNode = new ObjectNode(this, aObjectId);
			itsGraphicContainer.pChildren().add(theNode);
			queueEvent(new CellEvent.NodeImported(theNode));
			itsNodesMap.put(aObjectId, theNode);
		}
		
		return theNode;
	}
}
