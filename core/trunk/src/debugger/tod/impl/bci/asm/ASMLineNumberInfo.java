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

import org.objectweb.asm.Label;

import tod.core.ILocationRegisterer.LineNumberInfo;

/**
 * Represents an unresolved entry of a method's LineNumberTable attribute.
 * @see tod.core.ILocationRegisterer.LineNumberInfo
 * @see http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#22856 
 * @author gpothier
 */
public class ASMLineNumberInfo
{
    private Label itsStartPc;
    private int itsLineNumber;
    
    public ASMLineNumberInfo(Label aStartPc, int aLineNumber)
	{
		itsStartPc = aStartPc;
		itsLineNumber = aLineNumber;
	}
    
    public LineNumberInfo resolve()
    {
    	return new LineNumberInfo(
        		(short) itsStartPc.getOffset(), 
                (short) itsLineNumber);
    }

}