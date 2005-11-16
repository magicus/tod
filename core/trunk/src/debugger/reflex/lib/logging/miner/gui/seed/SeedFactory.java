/*
 * Created on Nov 10, 2004
 */
package reflex.lib.logging.miner.gui.seed;

import java.util.List;

import reflex.lib.logging.miner.gui.IGUIManager;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.LocationInfo;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.ICompoundFilter;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IEventFilter;

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
			LocationInfo aInfo)
	{
		if (aInfo instanceof TypeInfo)
		{
			TypeInfo theTypeInfo = (TypeInfo) aInfo;
			return createSeed(aGUIManager, aLog, aLog.createInstantiationsFilter(theTypeInfo));
		}
		else if (aInfo instanceof BehaviorInfo)
		{
			BehaviorInfo theBehaviourInfo = (BehaviorInfo) aInfo;
			return createSeed(
					aGUIManager, 
					aLog, 
					aLog.createBehaviorCallFilter(theBehaviourInfo));
		}
		else if (aInfo instanceof FieldInfo)
		{
			FieldInfo theFieldInfo = (FieldInfo) aInfo;
			
			ICompoundFilter theFilter = aLog.createIntersectionFilter();
			theFilter.add(aLog.createFieldWriteFilter());
			theFilter.add(aLog.createFieldFilter(theFieldInfo));
			
			return createSeed(aGUIManager, aLog, theFilter);
		}
		else return null;
	}
}
