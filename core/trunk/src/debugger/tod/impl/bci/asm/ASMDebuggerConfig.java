/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.bci.asm;

import java.io.File;

import tod.core.config.ClassSelector;
import tod.core.config.TODConfig;
import tod.tools.parsers.ParseException;
import tod.tools.parsers.workingset.WorkingSetFactory;

/**
 * Aggregates configuration information for the ASM instrumenter.
 * @author gpothier
 */
public class ASMDebuggerConfig
{
	private ClassSelector itsGlobalSelector;
	private ClassSelector itsTraceSelector;
	
	/**
	 * Creates a default debugger configuration.
	 */
	public ASMDebuggerConfig(TODConfig aConfig)
	{
		// Setup selectors
		setGlobalWorkingSet(aConfig.get(TODConfig.SCOPE_GLOBAL_FILTER));
		setTraceWorkingSet(aConfig.get(TODConfig.SCOPE_TRACE_FILTER));
	}
	
	private ClassSelector parseWorkingSet(String aWorkingSet)
	{
		try
		{
			return WorkingSetFactory.parseWorkingSet(aWorkingSet);
		}
		catch (ParseException e)
		{
			throw new RuntimeException("Cannot parse selector: "+aWorkingSet, e);
		}
	}
	
	public void setTraceWorkingSet(String aWorkingSet)
	{
		itsTraceSelector = parseWorkingSet(aWorkingSet);
	}
	
	public void setGlobalWorkingSet(String aWorkingSet)
	{
		itsGlobalSelector = parseWorkingSet(aWorkingSet);
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
}
