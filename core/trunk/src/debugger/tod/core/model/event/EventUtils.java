/*
 * Created on Nov 2, 2005
 */
package tod.core.model.event;

import java.util.HashSet;
import java.util.Set;

import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.model.structure.IBehaviorInfo;
import tod.core.model.structure.ITypeInfo;

/**
 * Provides utility methods related to events
 * @author gpothier
 */
public class EventUtils
{
	private static final IgnorableExceptions IGNORABLE_EXCEPTIONS = new IgnorableExceptions();
	
	public static String getVariableName(ILocalVariableWriteEvent aEvent)
	{
		IBehaviorInfo theInfo = aEvent.getParent().getExecutedBehavior();
		
		int theBytecodeIndex = aEvent.getOperationBytecodeIndex();
		short theVariableIndex = aEvent.getVariable().getIndex();
		
		// 35 is the size of the instrumentation
		LocalVariableInfo theLocalVariableInfo = theInfo != null ?
				theInfo.getLocalVariableInfo(theBytecodeIndex+35, theVariableIndex)
                : null;
                
		String theName = theLocalVariableInfo != null ? 
				theLocalVariableInfo.getVariableName() 
				: "$("+aEvent.getOperationBytecodeIndex()+", "+theVariableIndex+")";


		return theName;
	}
	
	/**
	 * Indicates if the given exception is ignorable.
	 * Ignorable exceptions include:
	 * <li>Exceptions generated by the standard classloading mechanism</li>
	 */
	public static boolean isIgnorableException (IExceptionGeneratedEvent aEvent)
	{
		return IGNORABLE_EXCEPTIONS.isIgnorableException(aEvent);
	}
	
	private static class IgnorableExceptions
	{
		private Set<String> itsIgnorableExceptions = new HashSet<String>();

		public IgnorableExceptions()
		{
			ignore("java.lang.ClassLoader", "findBootstrapClass");
			ignore("java.net.URLClassLoader$1", "run");
			ignore("java.net.URLClassLoader", "findClass");
			ignore("sun.misc.URLClassPath", "getLoader");
			ignore("sun.misc.URLClassPath$JarLoader", "getJarFile");
		}
		
		private void ignore (String aType, String aBehavior)
		{
			itsIgnorableExceptions.add (aType+"."+aBehavior);
		}
		
		public boolean isIgnorableException (IExceptionGeneratedEvent aEvent)
		{
			IBehaviorInfo theBehavior = aEvent.getThrowingBehavior();
			if (theBehavior == null) return true; // TODO: this is temporary
			
			ITypeInfo theType = theBehavior.getType();
			return itsIgnorableExceptions.contains(theType.getName()+"."+theBehavior.getName());
		}
	}
}