/*
 * Created on Oct 9, 2004
 */
package reflex.lib.logging.core.impl.mop.behavior;

import reflex.api.call.AbstractParameter;
import reflex.api.hookset.Operation;
import reflex.lib.logging.core.impl.mop.ReflexLocationPool;
import reflex.std.operation.Creation;

/**
 * @author gpothier
 */
public class ConstructorLogger extends AbstractBehaviorLogger
{
	private static ConstructorLogger INSTANCE = new ConstructorLogger();

	public static ConstructorLogger getInstance()
	{
		return INSTANCE;
	}

	private ConstructorLogger()
	{
		super (Creation.class, new ConstructorIdParameter());
	}
	
	private static class ConstructorIdParameter extends AbstractParameter
	{
		public String evaluate(Operation aOperation)
		{
			Creation theCreation = (Creation) aOperation;
			int theId = ReflexLocationPool.getLocationId(theCreation.getConstructor());
			return String.valueOf(theId);
		}

	}

}
