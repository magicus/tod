/*
 * Created on Oct 26, 2005
 */
package tod.core.bci;

public interface IInstrumenter
{
    /**
     * Instruments the given class.
     * @param aClassName JVM internal class name (eg. "java/lang/Object")
     * @param aBytecode Original bytecode of the class
     * @return New bytecode, or null if no instrumentation is performed.
     */
	public byte[] instrumentClass (String aClassName, byte[] aBytecode);
}
