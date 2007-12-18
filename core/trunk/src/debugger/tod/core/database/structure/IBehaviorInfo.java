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

import java.util.ArrayList;
import java.util.List;

import tod.core.BehaviorKind;
import tod.core.database.structure.IStructureDatabase.LineNumberInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;

public interface IBehaviorInfo extends IMemberInfo
{
	/**
	 * Sets up the attributes of this behavior.
	 * This method should be called only once.
	 */
	public void setup(
			boolean aTraced,
			BehaviorKind aKind,
			int aCodeSize,
			LineNumberInfo[] aLineNumberInfos,
			LocalVariableInfo[] aLocalVariableInfos);
	
	/**
	 * The type of a behavior is always a class.
	 */
	public IClassInfo getType();
	
	/**
	 * Whether this behavior is traced, ie. emits at least behavior
	 * enter and behavior exit events.
	 * The possible values are YES, NO and UNKNOWN, the latter being returned
	 * when the behavior has been created but not yet set up.
	 */
	public HasTrace hasTrace();
	
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
	 * Returns the JVM signature of this behavior.
	 */
	public String getSignature();

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
     * Returns the size of the bytecode of the method (after instrumentation).
     */
    public int getCodeSize();
    
    /**
     * Returns the tag associated to the specified bytecode.
     * @param aType The type of requested tag (one of the constants in {@link BytecodeTagType}).
     * @param aBytecodeIndex The index of the bytecode.
     * @return The value of the tag, or null if not found.
     */
    public <T> T getTag(BytecodeTagType<T> aType, int aBytecodeIndex);
    
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

    /**
     * An enumeration of all possible bytecode tag types.
     * Bytecode tags are information associated to each bytecode in a behavior.
     * @author gpothier
     */
    public static class BytecodeTagType<T>
    {
    	public static final BytecodeTagType<Integer> SOURCE_POSITION = new BytecodeTagType<Integer>();
    	public static final BytecodeTagType<BytecodeRole> BYTECODE_ROLE = new BytecodeTagType<BytecodeRole>();
    }
    
    /**
     * Enumerates the different possible roles of a bytecode. For instance, base code
     * which comes straight from a class source file, or inlined adviced code produced
     * by a weaver.
     * @author gpothier
     */
    public enum BytecodeRole
    {
    	BASE_CODE,
    	
    	WOVEN_CODE,
    	ASPECTJ_CODE(WOVEN_CODE),
    	
    	ADVICE_ARG_SETUP(ASPECTJ_CODE),
    	ADVICE_TEST(ASPECTJ_CODE),
    	INLINED_ADVICE(ASPECTJ_CODE);
    	
    	private final BytecodeRole itsParentRole;
    	private final List<BytecodeRole> itsChildrenRoles = new ArrayList<BytecodeRole>();
    	
    	BytecodeRole()
		{
    		itsParentRole = null;
		}
    	
    	BytecodeRole(BytecodeRole aParentRole)
    	{
    		itsParentRole = aParentRole;
    		itsParentRole.itsChildrenRoles.add(this);
    	}
    }
    
	public enum HasTrace
	{
		YES, NO, UNKNOWN;
	}
	
 
}
