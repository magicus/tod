/*
 * Created on Nov 4, 2005
 */
package reflex.lib.logging.miner.impl.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.ILocalVariableWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.IVariablesInspector;

public class VariablesInspector implements IVariablesInspector
{
	private IBehaviorCallEvent itsBehaviorCall;
	private int itsCurrentIndex;
	private ILogEvent itsCurrentEvent;
	private List<LocalVariableInfo> itsVariables;
	
	public VariablesInspector(IBehaviorCallEvent aBehaviorCall)
	{
		assert aBehaviorCall.isDirectParent();
		itsBehaviorCall = aBehaviorCall;
	}

	public IBehaviorCallEvent getBehaviorCall()
	{
		return itsBehaviorCall;
	}
	
	public BehaviorInfo getBehavior()
	{
		return getBehaviorCall().getExecutedBehavior();
	}
	
	public List<LocalVariableInfo> getVariables()
	{
		if (itsVariables == null)
		{
			itsVariables = Arrays.asList(getBehavior().getLocalVariables());
		}
		return itsVariables;
	}
	
	public List<LocalVariableInfo> getVariables(int aBytecodeIndex)
	{
		List<LocalVariableInfo> theResult = new ArrayList<LocalVariableInfo>();
		for (LocalVariableInfo theVariable : getVariables())
		{
			if (theVariable.available(aBytecodeIndex)) theResult.add(theVariable);
		}
		
		return theResult;
	}
	
	public void setCurrentEvent(ILogEvent aEvent)
	{
		int theIndex = getBehaviorCall().getChildren().indexOf(aEvent);
		if (theIndex == -1) throw new RuntimeException("Event not found in method execution");
		
		itsCurrentEvent = aEvent;
		itsCurrentIndex = theIndex;
	}

	public ILogEvent getCurrentEvent()
	{
		return itsCurrentEvent;
	}

	public Object getVariableValue(LocalVariableInfo aVariable)
	{
		for (int i=itsCurrentIndex; i>=0; i--)
		{
			ILogEvent theEvent = getBehaviorCall().getChildren().get(i);
			if (theEvent instanceof ILocalVariableWriteEvent)
			{
				ILocalVariableWriteEvent theLocalVariableWriteEvent = (ILocalVariableWriteEvent) theEvent;
				if (theLocalVariableWriteEvent.getVariable() == aVariable)
				{
					return theLocalVariableWriteEvent.getValue();
				}
			}
		}
		
		// If we did not find a variable write corresponding to the variable,
		// we consider the behavior call's initial argument values
		BehaviorInfo theBehavior = itsBehaviorCall.getExecutedBehavior();
		TypeInfo[] theArgumentTypes = theBehavior.getArgumentTypes();
		int theSlot = theBehavior.isStatic() ? 0 : 1;
		for (int i = 0; i < theArgumentTypes.length; i++)
		{
			if (aVariable.getIndex() == theSlot)
			{
				return itsBehaviorCall.getArguments()[i];
			}
			else
			{
				TypeInfo theType = theArgumentTypes[i];
				theSlot += theType.getSize();
			}
		}
		
		return null;
	}
	
	
}
