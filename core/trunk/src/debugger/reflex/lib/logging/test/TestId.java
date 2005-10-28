/*
 * Created on Apr 7, 2005
 */
package reflex.lib.logging.test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

public class TestId
{
	public static void main(String[] args) throws Exception
	{
		CtClass theClass = ClassPool.getDefault().get("java.lang.Object");
		CtConstructor theConstructor1 = theClass.getConstructor("()V");
		CtConstructor theConstructor2 = theClass.getConstructor("()V");
		
		System.out.println("" + theConstructor1+ " " + theConstructor2);
		System.out.println(theConstructor1 == theConstructor2);
	}
}
