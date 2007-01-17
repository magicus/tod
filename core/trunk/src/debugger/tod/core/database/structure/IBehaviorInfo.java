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
package tod.core.database.structure;

import tod.core.BehaviorKind;
import tod.core.ILocationRegisterer;
import tod.core.ILocationRegisterer.LineNumberInfo;
import tod.core.ILocationRegisterer.LocalVariableInfo;

public interface IBehaviorInfo extends IMemberInfo
{
	/**
	 * Indicates the kind of behavior represented by this object
	 * (method, constructor, etc.)
	 * @see BehaviorKind
	 */
	public BehaviorKind getBehaviourKind();

	/**
	 * Returns the types of the arguments to this behavior
	 */
	public ITypeInfo[] getArgumentTypes();

	/**
	 * Returns the type of the return value of this behavior. 
	 */
	public ITypeInfo getReturnType();

	/**
     * Returns the local variable symbolic information for a given bytecode index
     * and variable slot
     * @param aPc Bytecode index
     * @param aIndex Position of the variable's slot in the frame.
     */
	public LocalVariableInfo getLocalVariableInfo (int aPc, int aIndex);
    
    /**
     * Returns the symbolic variable information at the specified index. 
     */
    public LocalVariableInfo getLocalVariableInfo (int aSymbolIndex);
    
    /**
     * Returns the line number that corresponds to the specified bytecode index
     * according to the line number table. If the table is not available, returns -1.
     */
    public int getLineNumber (int aBytecodeIndex);
    
	/**
	 * Returns an array of all valid bytecode locations corresponding
	 * to the specified line in this method.
	 * @param aLine The source code line, relative to the whole file
	 * containing the method.
	 * @return Array of valid bytecodes, or null if no line number info is
	 * available.
	 */
	public int[] getBytecodeLocations(int aLine);
    
    public LocalVariableInfo[] getLocalVariables();
    
    /**
     * Indicates if this behavior is a constructor. 
     */
    public boolean isConstructor();

    /**
     * Indicates if this behavior is a static class initializer 
     */
    public boolean isStaticInit();
    
    /**
     * Indicates if this behavior is static.
     */
    public boolean isStatic();
    
}
