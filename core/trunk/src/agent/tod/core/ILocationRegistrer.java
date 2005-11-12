package tod.core;


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
            return aIndex == getIndex() && available(aPc);
        }
        
        /**
         * Indicates if this entry is available at the specified bytecode position.
         * @param aPc A position in the bytecode where the variable is used.
         */
        public boolean available (int aPc)
        {
        	return aPc >= getStartPc() && aPc < getStartPc() + getLength();
        }
    }

}
