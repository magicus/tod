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

import tod.core.BehaviourKind;
import tod.core.ILocationRegistrer;
import tod.core.ILocationRegistrer.LineNumberInfo;
import tod.core.ILocationRegistrer.LocalVariableInfo;


/**
 * Base class for behaviour (method/constructor) information.
 * @author gpothier
 */
public class BehaviorInfo extends MemberInfo implements IBehaviorInfo
{
	private final BehaviourKind itsBehaviourKind;
	
	private final String itsSignature;
	private final ITypeInfo[] itsArgumentTypes;
	private final ITypeInfo itsReturnType;

	private ILocationRegistrer.LineNumberInfo[] itsLineNumberTable;
	private ILocationRegistrer.LocalVariableInfo[] itsLocalVariableTable;

	public BehaviorInfo(
			BehaviourKind aBehaviourKind, 
			int aId, 
			ClassInfo aType, 
			String aName,
			String aSignature,
			ITypeInfo[] aArgumentTypes,
			ITypeInfo aReturnType,
			ILocationRegistrer.LineNumberInfo[] aLineNumberTable,
			ILocationRegistrer.LocalVariableInfo[] aLocalVariableTable)
	{
		super(aId, aType, aName);
		itsSignature = aSignature;
		itsArgumentTypes = aArgumentTypes;
		itsReturnType = aReturnType;
		itsBehaviourKind = aBehaviourKind;
		itsLineNumberTable = aLineNumberTable;
		itsLocalVariableTable = aLocalVariableTable;
	}
	
	public void setAttributes (
			ILocationRegistrer.LineNumberInfo[] aLineNumberTable,
			ILocationRegistrer.LocalVariableInfo[] aLocalVariableTable)
	{
		itsLineNumberTable = aLineNumberTable;
		itsLocalVariableTable = aLocalVariableTable;
	}
	
	public BehaviourKind getBehaviourKind()
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
    	if (itsLocalVariableTable != null) for (int i=0; i<itsLocalVariableTable.length; i++)
    	{
    	    LocalVariableInfo theInfo = itsLocalVariableTable[i];
    		if (theInfo.match(aPc, aIndex)) return theInfo;
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
    
    public LocalVariableInfo[] getLocalVariables()
    {
    	return itsLocalVariableTable;
    }
    
    public boolean isConstructor()
    {
    	return getBehaviourKind() == BehaviourKind.CONSTRUCTOR;
    }

    public boolean isStaticInit()
    {
    	return getBehaviourKind() == BehaviourKind.STATIC_BLOCK;
    }
    
    public boolean isStatic()
    {
    	return getBehaviourKind() == BehaviourKind.STATIC_BLOCK
    		|| getBehaviourKind() == BehaviourKind.STATIC_METHOD;
    }
    
	
	@Override
	public String toString()
	{
		return "Behavior ("+getId()+", "+getName()+") of "+getType();
	}
	
	public void register(ILocationRegistrer aRegistrer)
	{
		aRegistrer.registerBehavior(getBehaviourKind(), getId(), getType().getId(), getName(), itsSignature);
		aRegistrer.registerBehaviorAttributes(getId(), itsLineNumberTable, itsLocalVariableTable);
	}
}
