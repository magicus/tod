package tod.bci.asm;

import org.objectweb.asm.Label;

import tod.core.ILocationRegistrer.LocalVariableInfo;

/**
 * Represents an entry of a method's LocalVariableTable attribute. 
 * @see http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#5956
 * @author gpothier
 */
public class ASMLocalVariableInfo
{
    private Label itsStart;
    private Label itsEnd;
    private String itsVariableName;
    private String itsVariableTypeDesc;
    private int itsIndex;
    
	public ASMLocalVariableInfo(Label aStart, Label aEnd, String aVariableName, String aVariableTypeDesc, int aIndex)
	{
		itsStart = aStart;
		itsEnd = aEnd;
		itsVariableName = aVariableName;
		itsVariableTypeDesc = aVariableTypeDesc;
		itsIndex = aIndex;
	}
    
	public LocalVariableInfo resolve()
	{
		return new LocalVariableInfo(
				(short) itsStart.getOffset(),
				(short) (itsEnd.getOffset() - itsStart.getOffset()),
				itsVariableName,
				BCIUtils.getClassName(itsVariableTypeDesc),
				(short) itsIndex);
	}
}