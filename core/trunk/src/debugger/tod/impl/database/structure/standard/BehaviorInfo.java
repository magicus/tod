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
package tod.impl.database.structure.standard;

import java.util.HashMap;
import java.util.Map;

import tod.core.BehaviorKind;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ILocationsRepository.LineNumberInfo;
import tod.core.database.structure.ILocationsRepository.LocalVariableInfo;


/**
 * Base class for behaviour (method/constructor) information.
 * @author gpothier
 */
public class BehaviorInfo extends MemberInfo implements IBehaviorInfo
{
	private final BehaviorKind itsBehaviourKind;
	
	private final String itsSignature;
	private final ITypeInfo[] itsArgumentTypes;
	private final ITypeInfo itsReturnType;
	private final Map<BytecodeTagType, Object> itsTagsMap = new HashMap<BytecodeTagType, Object>();

	private LineNumberInfo[] itsLineNumberTable;
	private LocalVariableInfo[] itsLocalVariableTable;

	public BehaviorInfo(
			IStructureDatabase aDatabase, 
			BehaviorKind aBehaviourKind, 
			int aId, 
			ClassInfo aType, 
			String aName,
			String aSignature,
			ITypeInfo[] aArgumentTypes,
			ITypeInfo aReturnType,
			LineNumberInfo[] aLineNumberTable,
			LocalVariableInfo[] aLocalVariableTable)
	{
		super(aDatabase, aId, aType, aName);
		itsSignature = aSignature;
		itsArgumentTypes = aArgumentTypes;
		itsReturnType = aReturnType;
		itsBehaviourKind = aBehaviourKind;
		itsLineNumberTable = aLineNumberTable;
		itsLocalVariableTable = aLocalVariableTable;
	}
	
	public void setAttributes (
			LineNumberInfo[] aLineNumberTable,
			LocalVariableInfo[] aLocalVariableTable)
	{
		itsLineNumberTable = aLineNumberTable;
		itsLocalVariableTable = aLocalVariableTable;
	}
	
	public BehaviorKind getBehaviourKind()
	{
		return itsBehaviourKind;
	}

	public ITypeInfo[] getArgumentTypes()
	{
		return itsArgumentTypes;
	}

	public ITypeInfo getReturnType()
	{
		return itsReturnType;
	}

	public LocalVariableInfo getLocalVariableInfo (int aPc, int aIndex)
	{
		int thePc = aPc+35; // 35 is the size of our instrumentation
    	if (itsLocalVariableTable != null) for (int i=0; i<itsLocalVariableTable.length; i++)
    	{
    	    LocalVariableInfo theInfo = itsLocalVariableTable[i];
    		if (theInfo.match(thePc, aIndex)) return theInfo;
    	}
    	return null;
	}
    
    public LocalVariableInfo getLocalVariableInfo (int aSymbolIndex)
    {
        return itsLocalVariableTable != null && aSymbolIndex < itsLocalVariableTable.length ?
                itsLocalVariableTable[aSymbolIndex]
                : null;
    }
    
    public int getLineNumber (int aBytecodeIndex)
    {
        if (itsLineNumberTable != null && itsLineNumberTable.length > 0)
        {
        	int theResult = itsLineNumberTable[0].getLineNumber();
            
            for (LineNumberInfo theInfo : itsLineNumberTable)
			{
                if (aBytecodeIndex < theInfo.getStartPc()) break;
                theResult = theInfo.getLineNumber();
			}
            return theResult;
        }
        else return -1;
    }
    
	public int[] getBytecodeLocations(int aLine)
	{
        if (itsLineNumberTable != null && itsLineNumberTable.length > 0)
        {
        	int theStart = 0;
        	int theEnd = 0;
        	int theLastLine = 0;
            for (LineNumberInfo theInfo : itsLineNumberTable)
			{
            	short theLineNumber = theInfo.getLineNumber();
            	if (theLineNumber > aLine || theLineNumber < theLastLine)
            	{
            		theEnd = theInfo.getStartPc();
            		break;
            	}
            	if (theLineNumber <= aLine) theStart = theInfo.getStartPc();
            	
            	theLastLine = theLineNumber;
			}
            
            // TODO: do something to include only valid bytecode indexes.
            int[] theResult = new int[theEnd-theStart];
            for (int i=theStart;i<theEnd;i++) theResult[i-theStart] = i;
            
            return theResult;
        }
        else return null;
	}
	
	public <T> T getTag(BytecodeTagType<T> aType, int aBytecodeIndex)
	{
		T[] theTags = (T[]) itsTagsMap.get(aType);
		return theTags != null ? theTags[aBytecodeIndex] : null;
	}

	public LocalVariableInfo[] getLocalVariables()
    {
    	return itsLocalVariableTable;
    }
    
    public boolean isConstructor()
    {
    	return getBehaviourKind() == BehaviorKind.CONSTRUCTOR;
    }

    public boolean isStaticInit()
    {
    	return getBehaviourKind() == BehaviorKind.STATIC_BLOCK;
    }
    
    public boolean isStatic()
    {
    	return getBehaviourKind() == BehaviorKind.STATIC_BLOCK
    		|| getBehaviourKind() == BehaviorKind.STATIC_METHOD;
    }
    
	
	@Override
	public String toString()
	{
		return "Behavior ("+getId()+", "+getName()+") of "+getType();
	}
	
}
