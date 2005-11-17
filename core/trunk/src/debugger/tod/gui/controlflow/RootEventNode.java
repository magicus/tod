/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.model.event.ILogEvent;
import tod.core.model.event.IParentEvent;
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
