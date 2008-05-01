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

import javax.swing.JFrame;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import tod.core.bci.IInstrumenter.InstrumentedClass;
import tod.core.config.TODConfig;
import tod.core.database.browser.LocationUtils;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.gui.view.structure.DisassemblyPanel;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.database.structure.standard.StructureDatabase;
import zz.utils.Utils;

public class Instrument
{
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		String theClassFile = "/home/gpothier/eclipse/ws-tod-daughter/TOD-evdbng/bin/tod/impl/evdbng/db/file/TupleIterator.class";
		byte[] theClassData = Utils.readInputStream_byte(new FileInputStream(theClassFile));
		
		ClassReader cr = new ClassReader(theClassData);
		ClassNode theClassNode = new ClassNode();
		cr.accept(theClassNode, 0);
		
		String theName = theClassNode.name.replace('/', '.');
		System.out.println(theName);
		
		TODConfig theConfig = new TODConfig();
		ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(theConfig);
		StructureDatabase theStructureDatabase = StructureDatabase.create(theConfig, "test");
		ASMInstrumenter theInstrumenter = new ASMInstrumenter(theStructureDatabase, theDebuggerConfig);
		
		InstrumentedClass theInstrumentedClass = theInstrumenter.instrumentClass(theName, theClassData);

		IClassInfo theClass = theStructureDatabase.getClass(theName, true);
		String theSig = "()Ltod/impl/evdbng/db/file/Tuple;";
		IBehaviorInfo theBehavior = theClass.getBehavior(
				"fetchNext", 
				LocationUtils.getArgumentTypes(theStructureDatabase, theSig), 
				LocationUtils.getReturnType(theStructureDatabase, theSig));
		
		JFrame theFrame = new JFrame("TOD - Instrument");
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.setContentPane(new DisassemblyPanel(theBehavior));
		theFrame.pack();
		theFrame.setVisible(true);
	}
	
}
