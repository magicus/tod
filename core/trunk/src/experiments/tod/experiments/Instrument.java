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
package tod.experiments;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import tod.core.config.TODConfig;
import tod.core.database.structure.IStructureDatabase;
import tod.impl.bci.asm.ASMDebuggerConfig;
import tod.impl.bci.asm.ASMInstrumenter;
import tod.impl.database.structure.standard.StructureDatabase;
import zz.utils.Utils;

public class Instrument
{
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		String theClassFile = args[0];
		ASMInstrumenter theInstrumenter = createInstrumenter();
		theInstrumenter.instrumentClass(
				"test", 
				Utils.readInputStream_byte(new FileInputStream(theClassFile)));
	}
	
	/**
	 * Creates a functional {@link ASMInstrumenter}.
	 * @return
	 */
	public static ASMInstrumenter createInstrumenter()
	{
		TODConfig theConfig = new TODConfig();
		IStructureDatabase theStructureDatabase = StructureDatabase.create("test");
		ASMDebuggerConfig theDebuggerConfig = new ASMDebuggerConfig(theConfig);

		return new ASMInstrumenter(theStructureDatabase, theDebuggerConfig);
	}
	
}
