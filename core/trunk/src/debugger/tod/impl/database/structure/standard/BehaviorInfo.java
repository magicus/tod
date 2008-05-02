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
package tod.impl.database.structure.standard;

import java.util.ArrayList;
import java.util.List;

import tod.agent.BehaviorKind;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IMutableBehaviorInfo;
import tod.core.database.structure.IShareableStructureDatabase;
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
	
	private boolean itsHasLineNumberTable = false;
	private transient LineNumberInfo[] itsLineNumberTable;
	
	private boolean itsHasLocalVariableTable = false;
	private transient List<LocalVariableInfo> itsLocalVariableTable;
	
	private boolean itsHasTagMap = false;
	private transient TagMap itsTagMap;

	public BehaviorInfo(
			IShareableStructureDatabase aDatabase, 
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
			TagMap aTagMap)
	{
		itsHasTrace = aTraced ? HasTrace.YES : HasTrace.NO;
		itsBehaviourKind = aKind;
		itsCodeSize = aCodeSize;
		
		itsLineNumberTable = aLineNumberInfos;
		itsHasLineNumberTable = itsLineNumberTable != null;
		
		itsTagMap = aTagMap;
		itsHasTagMap = itsTagMap != null;
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
		int thePc = aPc+17; // 17 is the size of our instrumentation
    	if (itsHasLocalVariableTable) for (LocalVariableInfo theInfo : getLocalVariables())
    	{
    		if (theInfo.match(thePc, aIndex)) return theInfo;
    	}
    	return null;
	}
    
    public LocalVariableInfo getLocalVariableInfo (int aSymbolIndex)
    {
        return itsHasLocalVariableTable && aSymbolIndex < getLocalVariables().size() ?
        		getLocalVariables().get(aSymbolIndex)
                : null;
    }
    
    public int getCodeSize()
    {
    	return itsCodeSize;
    }
    
    public int getLineNumber (int aBytecodeIndex)
    {
        if (itsHasLineNumberTable && getLineNumbers().length > 0)
        {
        	int theResult = getLineNumbers()[0].getLineNumber();
            
            for (LineNumberInfo theInfo : getLineNumbers())
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
        if (itsHasLineNumberTable && getLineNumbers().length > 0)
        {
        	List<Integer> theLocations = new ArrayList<Integer>();

        	int thePreviousPc = -1;
        	int theCurrentLine = -1;
            for (LineNumberInfo theInfo : getLineNumbers())
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
		return itsHasTagMap ? getTagMap().getTag(aType, aBytecodeIndex) : null;
	}

	public List<LocalVariableInfo> _getLocalVariables()
    {
    	return itsLocalVariableTable;
    }
	
	public List<LocalVariableInfo> getLocalVariables()
	{
		if (itsLocalVariableTable == null && itsHasLocalVariableTable)
		{
			assert ! isOriginal();
			itsLocalVariableTable = getDatabase()._getBehaviorLocalVariableInfo(getId());
		}
		return itsLocalVariableTable;
	}
	
	public void addLocalVariableInfo(LocalVariableInfo aInfo)
	{
		if (itsLocalVariableTable == null) itsLocalVariableTable = new ArrayList<LocalVariableInfo>();
		itsHasLocalVariableTable = true;
		itsLocalVariableTable.add(aInfo);
	}
	
	LineNumberInfo[] _getLineNumbers()
	{
		return itsLineNumberTable; 
	}
	
	public LineNumberInfo[] getLineNumbers()
	{
		if (itsLineNumberTable == null && itsHasLineNumberTable)
		{
			assert ! isOriginal();
			itsLineNumberTable = getDatabase()._getBehaviorLineNumberInfo(getId());
		}
		return itsLineNumberTable;
	}
	
	TagMap _getTagMap()
	{
		return itsTagMap;
	}
    
	private TagMap getTagMap()
	{
		if (itsTagMap == null && itsHasTagMap)
		{
			assert ! isOriginal();
			itsTagMap = getDatabase()._getBehaviorTagMap(getId());
		}
		return itsTagMap;
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
