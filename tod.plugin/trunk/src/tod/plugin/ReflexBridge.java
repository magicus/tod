/*
 * Created on May 8, 2007
 */
package tod.plugin;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class ReflexBridge implements ClassFileTransformer
{
	private static ReflexBridge INSTANCE = new ReflexBridge();

	public static ReflexBridge getInstance()
	{
		return INSTANCE;
	}

	private ReflexBridge()
	{
	}
	
	private ClassFileTransformer itsTransformer;
	
    public byte[] transform(
    		ClassLoader aLoader, 
    		String aClassName, 
    		Class< ? > aClassBeingRedefined, 
    		ProtectionDomain aProtectionDomain, 
    		byte[] aClassfileBuffer) throws IllegalClassFormatException
	{
    	try
		{
			byte[] theResult = null;
			
			if (itsTransformer != null) 
			{
				theResult = itsTransformer.transform(aLoader, aClassName, aClassBeingRedefined, aProtectionDomain, aClassfileBuffer);
			}
			
			return theResult;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
    
    public void setTransformer(ClassFileTransformer aTransformer)
	{
		itsTransformer = aTransformer;
	}

	public static void premain(String agentArgs, Instrumentation inst)
    {
		System.out.println("ReflexBridge loaded.");
        inst.addTransformer(ReflexBridge.getInstance());
    }
}