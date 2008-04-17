/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
 * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
 */
package tod.impl.bci.asm;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

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
 * This class instruments classes of the application VM so that they send
 * logging information to the debugger VM
 * 
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

	public InstrumentedClass instrumentClass(String aName, byte[] aBytecode)
	{
		if (!BCIUtils.acceptClass(aName, itsConfig.getGlobalSelector())) return null;
		if (aName.startsWith("sun/reflect/")) return null; // Strange things
		// happen inside
		// those classes...

		String theChecksum = Utils.md5String(aBytecode);

		ClassReader theReader = new ClassReader(aBytecode);
		ClassWriter theWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		Attribute[] theAttributes =
				new Attribute[]
				{
						new SootInstructionKindAttribute(),
						new SootInstructionShadowAttribute(),
						new SootInstructionSourceAttribute(),
						new SootInlineAttribute(),
						new AspectInfoAttribute(null), };

		// Pass 1: collect method info
		InfoCollector theInfoCollector = new InfoCollector();
		theReader.accept(theInfoCollector, theAttributes, ClassReader.SKIP_DEBUG);

		List<Integer> theTracedMethods = new ArrayList<Integer>();

		// Pass 2: actual instrumentation
		LogBCIVisitor theVisitor =
				new LogBCIVisitor(
						itsDatabase,
						itsConfig,
						theInfoCollector,
						theWriter,
						theChecksum,
						theTracedMethods);

		try
		{
			theReader.accept(theVisitor, theAttributes, 0);
		}
		catch (RuntimeException e)
		{
			System.err.println("Error while instrumenting: ");
			e.printStackTrace();
			printClass(theReader);
			// throw e;
			return null;
		}

		byte[] theBytecode = theWriter.toByteArray();

		if (itsConfig.getTODConfig().get(TODConfig.WITH_ASPECTS)) theVisitor
				.getClassInfo()
				.setBytecode(theBytecode);

		return theVisitor.isModified() ? new InstrumentedClass(theBytecode, theTracedMethods)
				: null;
	}

	private void printClass(ClassReader aReader)
	{
		aReader.accept(
				new TraceClassVisitor(new PrintWriter(new OutputStreamWriter(System.err))),
				null,
				0);
	}

	/**
	 * Represents a range of bytecodes.
	 * 
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
	 * 
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
