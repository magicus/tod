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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import tod.core.bci.IInstrumenter;
import tod.core.config.TODConfig;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.impl.bci.asm.attributes.AspectInfoAttribute;
import tod.impl.bci.asm.attributes.SootInlineAttribute;
import tod.impl.bci.asm.attributes.SootInstructionKindAttribute;
import tod.impl.bci.asm.attributes.SootInstructionShadowAttribute;
import tod.impl.bci.asm.attributes.SootInstructionSourceAttribute;
import zz.utils.Utils;

/**
 * This class instruments classes of the application VM
 * so that they send logging information to the debugger VM
 * @author gpothier
 */
public class ASMInstrumenter implements IInstrumenter
{
	private final IMutableStructureDatabase itsDatabase;
	private final ASMDebuggerConfig itsConfig;
	
	public ASMInstrumenter(IMutableStructureDatabase aDatabase, ASMDebuggerConfig aConfig)
	{
		itsDatabase = aDatabase;
		itsConfig = aConfig;
	}
	
	public void setTraceWorkingSet(String aWorkingSet)
	{
		itsConfig.setTraceWorkingSet(aWorkingSet);
	}
	
	public void setGlobalWorkingSet(String aWorkingSet)
	{
		itsConfig.setGlobalWorkingSet(aWorkingSet);
	}

	public InstrumentedClass instrumentClass (String aName, byte[] aBytecode)
    {
		if (! BCIUtils.acceptClass(aName, itsConfig.getGlobalSelector())) return null;
		
		String theChecksum = Utils.md5String(aBytecode);
		
		ClassReader theReader = new ClassReader(aBytecode);
		ClassWriter theWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		
		// Pass 1: collect method info 
		InfoCollector theInfoCollector = new InfoCollector();
		theReader.accept(theInfoCollector, ClassReader.SKIP_DEBUG);
		
		List<Integer> theTracedMethods = new ArrayList<Integer>();
		
		// Pass 2: actual instrumentation
		LogBCIVisitor theVisitor = new LogBCIVisitor(
				itsDatabase,
				itsConfig, 
				theInfoCollector,
				theWriter,
				theChecksum,
				theTracedMethods);
		
		Attribute[] theAttributes = new Attribute[] {
				new SootInstructionKindAttribute(),
				new SootInstructionShadowAttribute(),
				new SootInstructionSourceAttribute(),
				new SootInlineAttribute(),
				new AspectInfoAttribute(null),
		};
		
		theReader.accept(theVisitor, theAttributes, 0);
		
		byte[] theBytecode = theWriter.toByteArray();
		
		if (itsConfig.getTODConfig().get(TODConfig.WITH_ASPECTS))
			theVisitor.getClassInfo().setBytecode(theBytecode);
		
		return theVisitor.isModified()  
			? new InstrumentedClass(theBytecode, theTracedMethods) 
			: null;
    }

	/**
	 * Represents a range of bytecodes.
	 * @author gpothier
	 */
	public static class CodeRange
	{
		public final Label start;
		public final Label end;
		
		public CodeRange(Label aStart, Label aEnd)
		{
			start = aStart;
			end = aEnd;
		}
	}
	
	/**
	 * Eases the creation of code ranges.
	 * @author gpothier
	 */
	public static class RangeManager
	{
		private final MethodVisitor mv;
		private final List<CodeRange> itsRanges = new ArrayList<CodeRange>();
		private Label itsCurrentStart;
		
		public RangeManager(MethodVisitor aMv)
		{
			mv = aMv;
		}
		
		public List<CodeRange> getRanges()
		{
			return itsRanges;
		}

		public void start()
		{
			assert itsCurrentStart == null;
			itsCurrentStart = new Label();
			mv.visitLabel(itsCurrentStart);
		}
		
		public void end()
		{
			assert itsCurrentStart != null;
			Label theEnd = new Label();
			mv.visitLabel(theEnd);
			itsRanges.add(new CodeRange(itsCurrentStart, theEnd));
			itsCurrentStart = null;
		}
	}

}
