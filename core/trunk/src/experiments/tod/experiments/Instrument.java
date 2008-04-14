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
package tod.experiments;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

import tod.core.config.TODConfig;
import tod.core.database.structure.IMutableStructureDatabase;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.database.structure.standard.StructureDatabase;
import zz.utils.Utils;

public class Instrument
{
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		String theClassFile = args[0];
		byte[] theClassData = Utils.readInputStream_byte(new FileInputStream(theClassFile));
		
		ClassReader cr = new ClassReader(theClassData);
		
		PrintWriter theWriter = new PrintWriter(new OutputStreamWriter(System.out));
		TraceClassVisitor theTraceClassVisitor = new TraceClassVisitor(theWriter);
		cr.accept(theTraceClassVisitor, 0);
		

		
		ASMInstrumenter theInstrumenter = createInstrumenter();
		theInstrumenter.instrumentClass(
				"test", 
				theClassData);
	}
	
	/**
	 * Creates a functional {@link ASMInstrumenter}.
	 * @return
	 */
	public static ASMInstrumenter createInstrumenter()
	{
		TODConfig theConfig = new TODConfig();
		IMutableStructureDatabase theStructureDatabase = StructureDatabase.create(theConfig, "test");
		ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(theConfig);

		return new ASMInstrumenter(theStructureDatabase, theDebuggerConfig);
	}
	
}
