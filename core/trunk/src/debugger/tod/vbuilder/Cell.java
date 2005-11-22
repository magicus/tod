/*
 * Created on Jul 2, 2005
 */
package tod.vbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.model.structure.ObjectId;
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
