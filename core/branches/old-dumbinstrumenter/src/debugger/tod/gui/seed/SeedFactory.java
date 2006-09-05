/*
 * Created on Nov 10, 2004
 */
package tod.gui.seed;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.IGUIManager;

/**
 * @author gpothier
 */
public class SeedFactory
{
	private static Seed createSeed (
			IGUIManager aGUIManager, 
			ILogBrowser aLog,
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
			ILogBrowser aLog, 
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
