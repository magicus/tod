/*
 * Created on Nov 8, 2004
 */
package tod.core.model.structure;

import tod.core.BehaviourKind;
import tod.core.ILocationRegistrer;
import tod.core.ILocationRegistrer.LineNumberInfo;
import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.model.trace.ILocationTrace;


/**
 * Base class for behaviour (method/constructor) information.
 * @author gpothier
 */
public class BehaviorInfo extends MemberInfo implements IBehaviorInfo
{
	private final BehaviourKind itsBehaviourKind;
	
	private final ITypeInfo[] itsArgumentTypes;
	private final ITypeInfo itsReturnType;

	private ILocationRegistrer.LineNumberInfo[] itsLineNumberTable;
	private ILocationRegistrer.LocalVariableInfo[] itsLocalVariableTable;

	public BehaviorInfo(
			ILocationTrace aTrace, 
			BehaviourKind aBehaviourKind, 
			int aId, 
			ClassInfo aType, 
			String aName,
			ITypeInfo[] aArgumentTypes,
			ITypeInfo aReturnType,
			ILocationRegistrer.LineNumberInfo[] aLineNumberTable,
			ILocationRegistrer.LocalVariableInfo[] aLocalVariableTable)
	{
		super(aTrace, aId, aType, aName);
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
    	return "<init>".equals(getName());
    }

    public boolean isStaticInit()
    {
    	return "<clinit>".equals(getName());
    }
    
    public boolean isStatic()
    {
    	return getBehaviourKind() == BehaviourKind.STATIC_BLOCK
    		|| getBehaviourKind() == BehaviourKind.STATIC_METHOD;
    }
}
