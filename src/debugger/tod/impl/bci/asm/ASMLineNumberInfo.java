package tod.impl.bci.asm;

import org.objectweb.asm.Label;

import tod.core.ILocationRegistrer.LineNumberInfo;

/**
 * Represents an unresolved entry of a method's LineNumberTable attribute.
 * @see tod.core.ILocationRegistrer.LineNumberInfo
 * @see http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#22856 
 * @author gpothier
 */
public class ASMLineNumberInfo
{
    private Label itsStartPc;
    private int itsLineNumber;
    
    public ASMLineNumberInfo(Label aStartPc, int aLineNumber)
	{
		itsStartPc = aStartPc;
		itsLineNumber = aLineNumber;
	}
    
    public LineNumberInfo resolve()
    {
    	return new LineNumberInfo(
        		(short) itsStartPc.getOffset(), 
                (short) itsLineNumber);
    }

}