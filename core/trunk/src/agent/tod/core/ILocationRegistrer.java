package tod.core;

import java.io.Serializable;

import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;

/**
 * Created by IntelliJ IDEA.
 * User: gpothier
 * Date: Apr 21, 2005
 * Time: 11:53:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ILocationRegistrer {
    
	public void registerFile (
			int aFileId,
			String aFileName);
	
	public void registerType (
			int aTypeId,
			String aTypeName,
			int aSupertypeId,
			int[] aInterfaceIds);
	
	/**
	 * Registers a behavior.
	 * @param aBehaviourType Type of behavior (constructor, static init, method)
	 * @param aBehaviourId Id assigned to the behavior
	 * @param aTypeId Id of the type that declares the behavior
	 * @param aBehaviourName Name of the behavior
	 * @param aSignature JVM signature of the method.
	 */
	public void registerBehavior (
			BehaviourType aBehaviourType,
			int aBehaviourId,
			int aTypeId,
			String aBehaviourName,
			String aSignature);

	/**
	 * Registers additional attributes of a behavior.
	 * These attributes cannot be registered at the same time the behavior is registered
	 * because the information might be unstable at that time.
	 */
	public void registerBehaviorAttributes (
			int aBehaviourId,
			LineNumberInfo[] aLineNumberTable,
			LocalVariableInfo[] aLocalVariableTable);
	
	public void registerField (
			int aFieldId,
			int aTypeId,
			String aFieldName);
	
	public void registerThread (
			long aThreadId,
			String aName);
    
    /**
     * Represents an entry of a method's LineNumberTable attribute.
     * @see http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#22856 
     * @author gpothier
     */
    public static class LineNumberInfo 
    {
        private short itsStartPc;
        private short itsLineNumber;
        
        public LineNumberInfo(short aStartPc, short aLineNumber)
        {
            itsStartPc = aStartPc;
            itsLineNumber = aLineNumber;
        }

        public short getLineNumber()
        {
            return itsLineNumber;
        }

        public short getStartPc()
        {
            return itsStartPc;
        }
        
        /**
         * Creates an array of {@link LineNumberInfo} corresponding to the given attribute
         */
        public static LineNumberInfo[] createTable (LineNumberAttribute aAttribute)
        {
        	if (aAttribute == null) return null;

        	int theLength = aAttribute.tableLength();
            LineNumberInfo[] theTable = new LineNumberInfo[theLength];
            
            for (int i=0;i<theLength;i++)
            {
                theTable[i] = new LineNumberInfo(
                		(short) aAttribute.startPc(i), 
                        (short) aAttribute.lineNumber(i));
            }
            
            return theTable;
        }
    }
    
    /**
     * Represents an entry of a method's LocalVariableTable attribute. 
     * @see http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#5956
     * @author gpothier
     */
    public static class LocalVariableInfo 
    {
        private short itsStartPc;
        private short itsLength;
        private String itsVariableName;
        private String itsVariableTypeName;
        private short itsIndex;
        
        public LocalVariableInfo(short aStartPc, short aLength, String aVariableName, String aVariableTypeName, short aIndex)
        {
            itsStartPc = aStartPc;
            itsLength = aLength;
            itsVariableName = aVariableName;
            itsVariableTypeName = aVariableTypeName;
            itsIndex = aIndex;
        }

        /**
         * Index of the local variable's storage in the frame's local variables array
         * @return
         */
        public short getIndex()
        {
            return itsIndex;
        }

        /**
         * Index of first bytecoed where this local variable can be used.
         */
        public short getStartPc()
        {
            return itsStartPc;
        }

        /**
         * Length of the bytecode span where this variable can be used.
         */
        public short getLength()
        {
            return itsLength;
        }

        /**
         * Name of the variable.
         */
        public String getVariableName()
        {
            return itsVariableName;
        }

        /**
         * Variable's type name.
         */
        public String getVariableTypeName()
        {
            return itsVariableTypeName;
        }
        
        /**
         * Indicates if this entry matches the local variable at the specified index
         * for the specified bytecode position
         * @param aPc A position in the bytecode where the variable is used.
         * @param aIndex Index of the local variable in the frame's local variables array.
         */
        public boolean match (int aPc, int aIndex)
        {
            return aIndex == getIndex() && aPc >= getStartPc() && aPc < getStartPc() + getLength();
        }
        
        /**
         * Creates an array of {@link LocalVariableInfo} corresponding to the given attribute
         */
        public static LocalVariableInfo[] createTable (LocalVariableAttribute aAttribute)
        {
        	if (aAttribute == null) return null;
        	
            int theLength = aAttribute.tableLength();
            LocalVariableInfo[] theTable = new LocalVariableInfo[theLength];
            
            for (int i=0;i<theLength;i++)
            {
                theTable[i] = new LocalVariableInfo(
                        (short) aAttribute.startPc(i), 
                        (short) aAttribute.codeLength(i),
                        aAttribute.variableName(i),
                        aAttribute.descriptor(i),
                        (short) aAttribute.index(i));
            }
            
            return theTable;
        }

    }

}
