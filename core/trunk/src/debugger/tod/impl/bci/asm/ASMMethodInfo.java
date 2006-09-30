/*
 * Created on Oct 26, 2005
 */
package tod.impl.bci.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tod.core.ILocationRegistrer.LineNumberInfo;
import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.impl.bci.asm.InfoCollector.StoreInfo;

public class ASMMethodInfo
{
	private int itsMaxLocals;
	private final String itsName;
	private final String itsDescriptor;
	private final boolean itsStatic;
	
	private int itsNextFreeVariable;
	
	private List<ASMLineNumberInfo> itsLineNumberInfo = new ArrayList<ASMLineNumberInfo>();
	private List<ASMLocalVariableInfo> itsLocalVariableInfo = new ArrayList<ASMLocalVariableInfo>();
	
	/**
	 * An array of store operations to ignore.
	 * The index is the rank of the store operation within the method.
	 * @see InfoCollector.JSRAnalyserVisitor
	 */
	private boolean[] itsIgnoreStores;
	
	public ASMMethodInfo(String aName, String aDescriptor, boolean aStatic)
	{
		itsName = aName;
		itsDescriptor = aDescriptor;
		itsStatic = aStatic;
	}

	public int getMaxLocals()
	{
		return itsMaxLocals;
	}

	public void setMaxLocals(int aMaxLocals)
	{
		itsMaxLocals = aMaxLocals;
		itsNextFreeVariable = itsMaxLocals;
	}

//	/**
//	 * Returns the index of a free variable slot for the described method.
//	 */
//	public int getNextFreeVariable(Type aType)
//	{
//		return itsNextFreeVariable;
//	}
//	
//	/**
//	 * Allocates space for a variable of the given type for the
//	 * described method.
//	 */
//	public int createVariable(Type aType)
//	{
//		int theVariable = itsNextFreeVariable;
//		itsNextFreeVariable += aType.getSize();
//		return theVariable;
//	}
//	
	public String getDescriptor()
	{
		return itsDescriptor;
	}

	public String getName()
	{
		return itsName;
	}

	public boolean isStatic()
	{
		return itsStatic;
	}
	
	public void addLineNumber (ASMLineNumberInfo aInfo)
	{
		itsLineNumberInfo.add(aInfo);
	}
	
	public void addLocalVariable (ASMLocalVariableInfo aInfo)
	{
		itsLocalVariableInfo.add(aInfo);
	}
	
    /**
     * Creates an array of {@link LineNumberInfo} from a list of 
     * {@link ASMLineNumberInfo}
     */
    public LineNumberInfo[] createLineNumberTable ()
    {
    	int theLength = itsLineNumberInfo.size();
        LineNumberInfo[] theTable = new LineNumberInfo[theLength];

        int i=0;
        for (ASMLineNumberInfo theInfo : itsLineNumberInfo)
		{
            theTable[i++] = theInfo.resolve();
        }
        
        return theTable;
    }

    /**
     * Creates an array of {@link LocalVariableInfo} from a list of 
     * {@link ASMLocalVariableInfo}
     */
    public LocalVariableInfo[] createLocalVariableTable ()
    {
    	int theLength = itsLocalVariableInfo.size();
    	LocalVariableInfo[] theTable = new LocalVariableInfo[theLength];
    	
    	int i=0;
    	for (ASMLocalVariableInfo theInfo : itsLocalVariableInfo)
    	{
    		theTable[i++] = theInfo.resolve();
    	}
    	
    	return theTable;
    }
    
    public void setIgnoreStores(boolean[] aIgnoreStores)
	{
		itsIgnoreStores = aIgnoreStores;
	}

	/**
     * Indicates if the n-th store instruction should be ignored 
     * @param aIndex Rank of the store instruction.
     */
    public boolean shouldIgnoreStore(int aIndex)
    {
    	return itsIgnoreStores[aIndex];
    }
}