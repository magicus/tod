/*
 * Created on Oct 25, 2005
 */
package tod.bci.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;


/**
 * This visitor simply records information about each method.
 * It should be run in a first pass, before actual instrumentation
 * is done. 
 * @author gpothier
 */
public class InfoCollector extends EmptyVisitor
{
	private List<ASMMethodInfo> itsMethodsInfo = new ArrayList<ASMMethodInfo>();
	private ASMMethodInfo itsCurrentMethodInfo;
	
	public ASMMethodInfo getMethodInfo (int aIndex)
	{
		return itsMethodsInfo.get(aIndex);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String aName, String aDesc, String aSignature, String[] aExceptions)
	{
		itsCurrentMethodInfo = new ASMMethodInfo(aName, aDesc, BCIUtils.isStatic(access));
		itsMethodsInfo.add(itsCurrentMethodInfo);
		return this;
	}
	
	@Override
	public void visitMaxs(int aMaxStack, int aMaxLocals)
	{
		itsCurrentMethodInfo.setMaxLocals(aMaxLocals);
	}
	
}
