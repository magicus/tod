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

import java.util.ArrayList;
import java.util.List;

import tod.agent.BehaviorKind;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.IStructureDatabase.LineNumberInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;


/**
 * Base class for behaviour (method/constructor) information.
 * @author gpothier
 */
public class BehaviorInfo extends MemberInfo implements IMutableBehaviorInfo
{
	private static final long serialVersionUID = 8645425455286128491L;
	
	private BehaviorKind itsBehaviourKind;
	private HasTrace itsHasTrace = HasTrace.UNKNOWN;
	
	private final String itsSignature;
	private final ITypeInfo[] itsArgumentTypes;
	private final ITypeInfo itsReturnType;

	private int itsCodeSize;
	private LineNumberInfo[] itsLineNumberTable;
	private LocalVariableInfo[] itsLocalVariableTable;
	private TagMap itsTagMap;

	public BehaviorInfo(
			StructureDatabase aDatabase, 
			int aId, 
			ClassInfo aType, 
			String aName,
			String aSignature,
			ITypeInfo[] aArgumentTypes,
			ITypeInfo aReturnType)
	{
		super(aDatabase, aId, aType, aName);
		itsSignature = aSignature;
		itsArgumentTypes = aArgumentTypes;
		itsReturnType = aReturnType;
		
//		System.out.println(String.format(
//				"[Struct] behavior info [id: %d, sig: %s.%s(%s)]",
//				aId,
//				aType.getName(),
//				aName,
//				aSignature));
	}
	
	public void setup(
			boolean aTraced,
			BehaviorKind aKind,
			int aCodeSize,
			LineNumberInfo[] aLineNumberInfos,
			LocalVariableInfo[] aLocalVariableInfos,
			TagMap aTagMap)
	{
		itsHasTrace = aTraced ? HasTrace.YES : HasTrace.NO;
		itsBehaviourKind = aKind;
		itsCodeSize = aCodeSize;
		itsLineNumberTable = aLineNumberInfos;
		itsLocalVariableTable = aLocalVariableInfos;
		itsTagMap = aTagMap;
	}
	
	@Override
	public IClassInfo getType()
	{
		return (IClassInfo) super.getType();
	}
	
	public HasTrace hasTrace()
	{
		return itsHasTrace;
	}
	
	public BehaviorKind getBehaviourKind()
	{
		return itsBehaviourKind;
	}

	public ITypeInfo[] getArgumentTypes()
	{
		return itsArgumentTypes;
	}
	
	public String getSignature()
	{
		return itsSignature;
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
    
    public int getCodeSize()
    {
    	return itsCodeSize;
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
    
    /**
     * Adds a range of numbers to the list.
     * @param aStart First number to add, inclusive
     * @param aEnd Last number to add, exclusive.
     */
    private void addRange(List<Integer> aList, int aStart, int aEnd)
    {
    	for(int i=aStart;i<aEnd;i++) aList.add(i);
    }
    
	public int[] getBytecodeLocations(int aLine)
	{
        if (itsLineNumberTable != null && itsLineNumberTable.length > 0)
        {
        	List<Integer> theLocations = new ArrayList<Integer>();

        	int thePreviousPc = -1;
        	int theCurrentLine = -1;
            for (LineNumberInfo theInfo : itsLineNumberTable)
            {
            	if (thePreviousPc == -1)
            	{
            		thePreviousPc = theInfo.getStartPc();
            		theCurrentLine = theInfo.getLineNumber();
            		continue;
            	}
            	
            	if (theCurrentLine == aLine)
            	{
            		addRange(theLocations, thePreviousPc, theInfo.getStartPc());
            	}

            	thePreviousPc = theInfo.getStartPc();
            	theCurrentLine = theInfo.getLineNumber();
            }
            
            if (theCurrentLine == aLine)
            {
            	addRange(theLocations, thePreviousPc, getCodeSize());
            }
            
            // TODO: do something to include only valid bytecode indexes.
            int[] theResult = new int[theLocations.size()];
            for (int i=0;i<theResult.length;i++) theResult[i] = theLocations.get(i);
            
            return theResult;
        }
        else return null;
	}
	
	public <T> T getTag(BytecodeTagType<T> aType, int aBytecodeIndex)
	{
		return itsTagMap != null ? itsTagMap.getTag(aType, aBytecodeIndex) : null;
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
    	return getBehaviourKind() == BehaviorKind.STATIC_INIT;
    }
    
    public boolean isStatic()
    {
    	return getBehaviourKind() == BehaviorKind.STATIC_INIT
    		|| getBehaviourKind() == BehaviorKind.STATIC_METHOD;
    }
    
	
	@Override
	public String toString()
	{
		return "Behavior ("+getId()+", "+getName()+") of "+getType();
	}
	
}
