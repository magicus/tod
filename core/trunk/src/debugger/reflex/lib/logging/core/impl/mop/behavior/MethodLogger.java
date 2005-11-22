/*
 * Created on Oct 9, 2004
 */
package reflex.lib.logging.core.impl.mop.behavior;

import reflex.api.call.AbstractParameter;
import reflex.api.hookset.Operation;
import reflex.lib.logging.core.impl.mop.ReflexLocationPool;
import reflex.std.operation.MsgReceive;

/**
 * @author gpothier
 */
public class MethodLogger extends AbstractBehaviorLogger
{
	private static MethodLogger INSTANCE = new MethodLogger();

	public static MethodLogger getInstance()
	{
		return INSTANCE;
	}

	private MethodLogger()
	{
		super (MsgReceive.class, new MethodIdParameter());
	}

	private static class MethodIdParameter extends AbstractParameter
	{
		public String evaluate(Operation aOperation)
		{
			MsgReceive theMsgReceive = (MsgReceive) aOperation;
			int theId = ReflexLocationPool.getLocationId(theMsgReceive.getMethod());
			return String.valueOf(theId);
		}
	}

}
