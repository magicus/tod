/*
 * Created on Oct 24, 2005
 */
package tod.bci.asm;

import java.io.File;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import remotebci.IInstrumenter;
import remotebci.RemoteInstrumenter;
import tod.session.ASMDebuggerConfig;

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
    	
        return theVisitor.isModified() ? theWriter.toByteArray() : null;
    }

}
