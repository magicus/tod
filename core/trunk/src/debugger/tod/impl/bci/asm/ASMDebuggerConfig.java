/*
 * Created on Oct 25, 2005
 */
package tod.impl.bci.asm;

import java.io.File;

import tod.core.ILocationRegistrer;
import tod.core.config.ClassSelector;
import tod.core.config.TODConfig;
import tod.tools.parsers.ParseException;
import tod.tools.parsers.workingset.WorkingSetFactory;

public class ASMDebuggerConfig
{
	public static final String PARAM_COLLECTOR_PORT = "collector-port";
	
	private final ILocationRegistrer itsLocationRegistrer;
	private ClassSelector itsGlobalSelector;
	private ClassSelector itsTraceSelector;
	
	private ASMLocationPool itsLocationPool;

	
	/**
	 * Creates a default debugger configuration.
	 */
	public ASMDebuggerConfig(
			TODConfig aConfig,
			ILocationRegistrer aLocationRegistrer)
	{
		itsLocationRegistrer = aLocationRegistrer;
		
		File theLocationsFile = new File(aConfig.get(TODConfig.INSTRUMENTER_LOCATIONS_FILE));
		String theGlobalWorkingSet = aConfig.get(TODConfig.SCOPE_GLOBAL_FILTER); 
		String theTraceWorkingSet = aConfig.get(TODConfig.SCOPE_TRACE_FILTER);
		
		
		// Setup selectors
		try
		{
			itsGlobalSelector = WorkingSetFactory.parseWorkingSet(theGlobalWorkingSet);
			itsTraceSelector = WorkingSetFactory.parseWorkingSet(theTraceWorkingSet);
		}
		catch (ParseException e)
		{
			throw new RuntimeException("Cannot setup selectors", e);
		}
		
		itsLocationPool = new ASMLocationPool(itsLocationRegistrer, theLocationsFile);
	}
	
	/**
	 * Returns the selector that indicates which classes should be
	 * instrumented so that execution of their methods is traced.
	 */
	public ClassSelector getTraceSelector()
	{
		return itsTraceSelector;
	}
	
	/**
	 * Returns the global selector. Classes not accepted by the global selector
	 * will not be instrumented at all even if they are accepted by other selectors.
	 */
	public ClassSelector getGlobalSelector()
	{
		return itsGlobalSelector;
	}
	
	public ASMLocationPool getLocationPool()
	{
		return itsLocationPool;
	}
}
