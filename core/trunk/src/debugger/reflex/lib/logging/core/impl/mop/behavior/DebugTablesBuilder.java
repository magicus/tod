/*
 * Created on Oct 28, 2005
 */
package reflex.lib.logging.core.impl.mop.behavior;

import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import tod.core.ILocationRegistrer.LineNumberInfo;
import tod.core.ILocationRegistrer.LocalVariableInfo;

public class DebugTablesBuilder
{
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
