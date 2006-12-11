/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.impl.bci.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import tod.core.bci.IInstrumenter;

/**
 * This class instruments classes of the application VM
 * so that they send logging information to the debugger VM
 * @author gpothier
 */
public class ASMInstrumenter implements IInstrumenter
{
	private final ASMDebuggerConfig itsConfig;
	
	public ASMInstrumenter(ASMDebuggerConfig aConfig)
	{
		itsConfig = aConfig;
	}

	public byte[] instrumentClass (String aName, byte[] aBytecode)
    {
		if (! BCIUtils.acceptClass(aName, itsConfig.getGlobalSelector())) return null;
    	
    	ClassReader theReader = new ClassReader(aBytecode);
    	ClassWriter theWriter = new ClassWriter(true);
    	
    	// Pass 1: collect method info 
    	InfoCollector theInfoCollector = new InfoCollector();
    	theReader.accept(theInfoCollector, true);
    	
    	// Pass 2: actual instrumentation
    	LogBCIVisitor theVisitor = new LogBCIVisitor(itsConfig, theInfoCollector, theWriter);
    	theReader.accept(theVisitor, false);
    	
        return theVisitor.isModified() && !  theVisitor.hasOverflow() 
        	? theWriter.toByteArray() 
        	: null;
    }

}
