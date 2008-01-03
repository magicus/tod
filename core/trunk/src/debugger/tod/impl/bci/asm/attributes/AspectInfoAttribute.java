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
package tod.impl.bci.asm.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;

/**
 * A class-level attribute that contains information about the advice source ids
 * used in a class.
 * See AspectInfoAttr in zz.abc 
 * @author gpothier
 */
public class AspectInfoAttribute extends DataAttribute
{
	private Map<Integer, Position> itsAdviceInfoMap;
	
	
	public AspectInfoAttribute(Map<Integer, Position> aAdviceInfoMap)
	{
		super("zz.abc.AspectInfoAttr");
		itsAdviceInfoMap = aAdviceInfoMap;
	}

	@Override
	protected Attribute read(DataInputStream aStream, Label[] aLabels) throws IOException
	{
		Map<Integer, Position> theMap = new HashMap<Integer, Position>();
		int theCount = aStream.readInt();
		
		for(int i=0;i<theCount;i++)
		{
			int theId = aStream.readInt();
			String theSourceFile = aStream.readUTF();
			int theStartLine = aStream.readInt();
			int theStartColumn = aStream.readInt();
			int theEndLine = aStream.readInt();
			int theEndColumn = aStream.readInt();
			
			theMap.put(theId, new Position(
					theSourceFile, 
					theStartLine, 
					theStartColumn, 
					theEndLine, 
					theEndColumn));
		}
		
		return new AspectInfoAttribute(theMap);
	}
	
	@Override
	protected void write(DataOutputStream aStream, int aLen, int aMaxStack, int aMaxLocals) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Corresponds to Soot's Position class
	 * @author gpothier
	 */
	public static class Position
	{
		public final String sourceFile;
		public final int startLine;
		public final int startColumn;
		public final int endLine;
		public final int endColumn;
		
		public Position(String aSourceFile, int aStartLine, int aStartColumn, int aEndLine, int aEndColumn)
		{
			sourceFile = aSourceFile;
			startLine = aStartLine;
			startColumn = aStartColumn;
			endLine = aEndLine;
			endColumn = aEndColumn;
		}
	}
}
