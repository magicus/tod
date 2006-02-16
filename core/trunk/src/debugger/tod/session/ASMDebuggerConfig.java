/*
 * Created on Oct 25, 2005
 */
package tod.session;

import java.io.File;

import reflex.Run;
import reflex.tools.parsers.ParseException;
import reflex.tools.parsers.workingset.WorkingSetFactory;
import tod.bci.IInstrumenter;
import tod.bci.asm.ASMInstrumenter;
import tod.bci.asm.ASMLocationPool;
import tod.core.ILogCollector;

public class ASMDebuggerConfig
{
	public static final String PARAM_COLLECTOR_PORT = "collector-port";
	
	private ILogCollector itsCollector;
	private Run.ClassNameSelector itsGlobalSelector;
	private Run.ClassNameSelector itsTraceSelector;
	
	private IInstrumenter itsInstrumenter;
	private File itsLocationsFile;

	private ASMLocationPool itsLocationPool;
	
	/**
	 * Creates a default debugger configuration.
	 */
	public ASMDebuggerConfig(
			ILogCollector aCollector, 
			File aLocationsFile,
			String aGlobalWorkingSet,
			String aTraceWorkingSet)
	{
		itsCollector = aCollector;
		
		itsLocationsFile = aLocationsFile;
		
		// Setup selectors
		try
		{
			itsGlobalSelector = WorkingSetFactory.parseWorkingSet(aGlobalWorkingSet);
			itsTraceSelector = WorkingSetFactory.parseWorkingSet(aTraceWorkingSet);
		}
		catch (ParseException e)
		{
			throw new RuntimeException("Cannot setup selectors", e);
		}
		
		itsInstrumenter = new ASMInstrumenter(this);
		itsLocationPool = new ASMLocationPool(this);
	}
	
	public ILogCollector getCollector()
	{
		return itsCollector;
	}

	/**
	 * Returns the selector that indicates which classes should be
	 * instrumented so that execution of their methods is traced.
	 */
	public Run.ClassNameSelector getTraceSelector()
	{
		return itsTraceSelector;
	}
	
	/**
	 * Returns the global selector. Classes not accepted by the global selector
	 * will not be instrumented at all even if they are accepted by other selectors.
	 */
	public Run.ClassNameSelector getGlobalSelector()
	{
		return itsGlobalSelector;
	}
	
	/**
	 * Returns the instrumenter to use
	 */
	public IInstrumenter getInstrumenter()
	{
		return itsInstrumenter;
	}

	/**
	 * Returns the name of the files where locations should be loaded and stored.
	 */
	public File getLocationsFile()
	{
		return itsLocationsFile;
	}

	public ASMLocationPool getLocationPool()
	{
		return itsLocationPool;
	}
}
