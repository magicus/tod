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
package tod.impl.bci.asm;

import java.util.ArrayList;
import java.util.List;

import tod.agent.BehaviorKind;
import tod.core.database.structure.IStructureDatabase.LineNumberInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;

public class ASMMethodInfo
{
	private int itsMaxLocals;
	private final String itsName;
	private final String itsDescriptor;
	private final boolean itsStatic;
	
	private int itsNextFreeVariable;
	
	private int itsCodeSize;
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
	
	public BehaviorKind getKind()
	{
		if ("<init>".equals(getName())) return BehaviorKind.CONSTRUCTOR;
		else if ("<clinit>".equals(getName())) return BehaviorKind.STATIC_INIT;
		else return isStatic() ? BehaviorKind.STATIC_METHOD : BehaviorKind.METHOD;
	}
	
	public int getCodeSize()
	{
		return itsCodeSize;
	}

	public void setCodeSize(int aCodeSize)
	{
		itsCodeSize = aCodeSize;
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