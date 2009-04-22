/*
 * Created on Jul 4, 2005
 */
package tod.vbuilder;

import java.util.List;

/**
 * Reprents an event that occured within a {@link tod.vbuilder.Cell}.
 * Concrete events are declared as inner classes of this class.
 * @author gpothier
 */
public abstract class CellEvent
{
	public abstract Type getType();
	
	/**
	 * Dispatches the events contained in the provided list to an event processor
	 */
	public static void dispatch (List<CellEvent> aEvents, ICellEventProcessor aProcessor)
	{
		for (CellEvent theEvent : aEvents)
		{
			switch (theEvent.getType())
			{
			case NODE_IMPORTED:
				NodeImported ni = (NodeImported) theEvent;
				aProcessor.nodeImported(ni.getNode());
				break;

			case NODE_REMOVED:
				NodeRemoved nr = (NodeRemoved) theEvent;
				aProcessor.nodeRemoved(nr.getNode());
				break;
				
			case ATTRIBUTE_CHANGED:
				AttributeChanged ac = (AttributeChanged) theEvent;
				aProcessor.attributeChanged(ac.getNode(), ac.getAttributeName(), ac.getOldValue(), ac.getNewValue());
				break;
				
			default: throw new RuntimeException("AEvent not handled: " + theEvent);
			}
		}
	}
	
	public static abstract class NodeEvent extends CellEvent
	{
		private IObjectNode itsNode;

		public NodeEvent(IObjectNode aNode)
		{
			itsNode = aNode;
		}

		public IObjectNode getNode()
		{
			return itsNode;
		}
	}
	
	public static class NodeImported extends NodeEvent
	{
		public NodeImported(IObjectNode aNode)
		{
			super(aNode);
		}

		@Override
		public Type getType()
		{
			return Type.NODE_IMPORTED;
		}
	}
	
	public static class NodeRemoved extends NodeEvent
	{
		public NodeRemoved(IObjectNode aNode)
		{
			super(aNode);
		}
		
		@Override
		public Type getType()
		{
			return Type.NODE_REMOVED;
		}
	}
	
	public static class AttributeChanged extends NodeEvent
	{
		private String itsAttributeName;
		private Object itsOldValue;
		private Object itsNewValue;
		

		public AttributeChanged(IObjectNode aNode, String aAttributeName, Object aOldValue, Object aNewValue)
		{
			super(aNode);
			
			itsAttributeName = aAttributeName;
			itsNewValue = aNewValue;
			itsOldValue = aOldValue;
		}


		public Object getNewValue()
		{
			return itsNewValue;
		}


		public Object getOldValue()
		{
			return itsOldValue;
		}


		public String getAttributeName()
		{
			return itsAttributeName;
		}


		@Override
		public Type getType()
		{
			return Type.ATTRIBUTE_CHANGED;
		}
	}
	
	public enum Type
	{
		NODE_IMPORTED, NODE_REMOVED, ATTRIBUTE_CHANGED
	}
}
