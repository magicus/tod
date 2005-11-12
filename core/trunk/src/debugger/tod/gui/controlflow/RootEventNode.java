/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.util.List;

import reflex.lib.logging.miner.gui.IGUIManager;
import tod.core.model.event.IParentEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventTrace;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.StackLayout;

public class RootEventNode extends AbstractEventNode
{
	private IParentEvent itsRootEvent;
	
	public RootEventNode(
			CFlowView aView,
			IParentEvent aRootEvent)
	{
		super (aView);
		itsRootEvent = aRootEvent;
		
		setLayoutManager(new StackLayout());

		List<IRectangularGraphicObject> theNodes = getBuilder().buildNodes(itsRootEvent);
		
		for (IRectangularGraphicObject theNode : theNodes)
		{
			pChildren().add(theNode);
		}		
	}
	
	@Override
	protected ILogEvent getMainEvent()
	{
		return null;
	}
}
