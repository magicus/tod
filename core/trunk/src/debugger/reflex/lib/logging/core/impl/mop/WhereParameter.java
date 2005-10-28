/*
 * Created on Sep 2, 2005
 */
package reflex.lib.logging.core.impl.mop;

import reflex.api.call.AbstractParameter;
import reflex.api.call.Parameter;
import reflex.api.hookset.Operation;
import reflex.std.operation.CallerSideOperation;

/**
 * A parameter that evaluates to the location id of the behavior
 * where an operation occurs. 
 * It is only valid for caller-side operations.
 * @author gpothier
 */
public class WhereParameter extends AbstractParameter
{
	public String evaluate(Operation aOperation)
	{
		CallerSideOperation theOperation = (CallerSideOperation) aOperation;
		int theId = ReflexLocationPool.getLocationId(theOperation.getWhere());
		return String.valueOf(theId);
	}
}
