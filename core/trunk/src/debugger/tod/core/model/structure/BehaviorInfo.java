/*
 * Created on Nov 8, 2004
 */
package tod.core.model.structure;

import org.objectweb.asm.Type;

import tod.core.BehaviourType;
import tod.core.ILocationRegistrer;
import tod.core.ILocationRegistrer.LineNumberInfo;
import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.model.trace.ILocationTrace;


/**
 * Base class for behaviour (method/constructor) information.
 * @author gpothier
 */
public class BehaviorInfo extends MemberInfo
{
	private final BehaviourType itsBehaviourType;
	
	private final TypeInfo[] itsArgumentTypes;
	private final TypeInfo itsReturnType;

	private ILocationRegistrer.LineNumberInfo[] itsLineNumberTable;
	private ILocationRegistrer.LocalVariableInfo[] itsLocalVariableTable;

	public BehaviorInfo(
			ILocationTrace aTrace, 
			BehaviourType aBehaviourType, 
			int aId, 
			ClassInfo aType, 
			String aName,
			TypeInfo[] aArgumentTypes,
			TypeInfo aReturnType,
			ILocationRegistrer.LineNumberInfo[] aLineNumberTable,
			ILocationRegistrer.LocalVariableInfo[] aLocalVariableTable)
	{
		super(aTrace, aId, aType, aName);
		itsArgumentTypes = aArgumentTypes;
		itsReturnType = aReturnType;
		itsBehaviourType = aBehaviourType;
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
	
	public BehaviourType getBehaviourType()
	{
		return itsBehaviourType;
	}

	public TypeInfo[] getArgumentTypes()
	{
		return itsArgumentTypes;
	}

	public TypeInfo getReturnType()
	{
		return itsReturnType;
	}

	/**
     * Returns the local variable symbolic information for a given bytecode index
     * and variable slot
     * @param aPc Bytecode index
     * @param aIndex Position of the variable's slot in the frame.
     */
	public LocalVariableInfo getLocalVariableInfo (int aPc, int aIndex)
	{
    	if (itsLocalVariableTable != null) for (int i=0; i<itsLocalVariableTable.length; i++)
    	{
    	    LocalVariableInfo theInfo = itsLocalVariableTable[i];
    		if (theInfo.match(aPc, aIndex)) return theInfo;
    	}
    	return null;
	}
    
    /**
     * Returns the symbolic variable information at the specified index. 
     */
    public LocalVariableInfo getLocalVariableInfo (int aSymbolIndex)
    {
        return itsLocalVariableTable != null && aSymbolIndex < itsLocalVariableTable.length ?
                itsLocalVariableTable[aSymbolIndex]
                : null;
    }
    
    /**
     * Returns the line number that corresponds to the specified bytecode index
     * according to the line number table. If the table is not available, returns -1.
     */
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
    
    public ILocationRegistrer.LocalVariableInfo[] getLocalVariables()
    {
    	return itsLocalVariableTable;
    }
    
    /**
     * Indicates if this behavior is a constructor. 
     */
    public boolean isConstructor()
    {
    	return "<init>".equals(getName());
    }

    /**
     * Indicates if this behavior is a static class initializer 
     */
    public boolean isStaticInit()
    {
    	return "<clinit>".equals(getName());
    }
    
    /**
     * Indicates if this behavior is static.
     */
    public boolean isStatic()
    {
    	return getBehaviourType() == BehaviourType.STATIC_BLOCK
    		|| getBehaviourType() == BehaviourType.STATIC_METHOD;
    }
}
