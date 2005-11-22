/*
 * Created on Nov 10, 2004
 */
package tod.gui.seed;

import tod.core.model.structure.IBehaviorInfo;
import tod.core.model.structure.IFieldInfo;
import tod.core.model.structure.ILocationInfo;
import tod.core.model.structure.ITypeInfo;
import tod.core.model.trace.ICompoundFilter;
import tod.core.model.trace.IEventFilter;
import tod.core.model.trace.IEventTrace;
import tod.gui.IGUIManager;

/**
 * @author gpothier
 */
public class SeedFactory
{
	private static Seed createSeed (
			IGUIManager aGUIManager, 
			IEventTrace aLog,
			IEventFilter aFilter)
	{
		return new FilterSeed (aGUIManager, aLog, aFilter);
		
	}
	
	/**
	 * Returns a seed that can be used to view the events that
	 * are related to the specified location info.
	 */
	public static Seed getDefaultSeed (
			IGUIManager aGUIManager, 
			IEventTrace aLog, 
			ILocationInfo aInfo)
	{
		if (aInfo instanceof ITypeInfo)
		{
			ITypeInfo theTypeInfo = (ITypeInfo) aInfo;
			return createSeed(aGUIManager, aLog, aLog.createInstantiationsFilter(theTypeInfo));
		}
		else if (aInfo instanceof IBehaviorInfo)
		{
			IBehaviorInfo theBehaviourInfo = (IBehaviorInfo) aInfo;
			return createSeed(
					aGUIManager, 
					aLog, 
					aLog.createBehaviorCallFilter(theBehaviourInfo));
		}
		else if (aInfo instanceof IFieldInfo)
		{
			IFieldInfo theFieldInfo = (IFieldInfo) aInfo;
			
			ICompoundFilter theFilter = aLog.createIntersectionFilter();
			theFilter.add(aLog.createFieldWriteFilter());
			theFilter.add(aLog.createFieldFilter(theFieldInfo));
			
			return createSeed(aGUIManager, aLog, theFilter);
		}
		else return null;
	}
}
