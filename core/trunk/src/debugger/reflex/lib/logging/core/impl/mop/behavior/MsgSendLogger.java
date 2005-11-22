/*
 * Created on Oct 9, 2004
 */
package reflex.lib.logging.core.impl.mop.behavior;

import reflex.api.ReflexConfig;
import reflex.api.call.AbstractParameter;
import reflex.api.call.CallDescriptor;
import reflex.api.call.Parameter;
import reflex.api.hookset.Hookset;
import reflex.api.hookset.Operation;
import reflex.api.hookset.OperationSelector;
import reflex.api.hookset.PrimitiveHookset;
import reflex.api.link.BLink;
import reflex.api.link.MODefinition;
import reflex.api.link.attribute.Activation;
import reflex.api.link.attribute.Control;
import reflex.api.link.attribute.Scope;
import reflex.api.model.RClass;
import reflex.lib.logging.core.impl.mop.Config;
import reflex.lib.logging.core.impl.mop.ReflexLocationPool;
import reflex.lib.logging.core.impl.mop.identification.ObjectIdentifier;
import reflex.std.installer.StdParameters;
import reflex.std.operation.MsgReceive;
import reflex.std.operation.MsgSend;
import tod.core.config.StaticConfig;

/**
 * Logs message sends.
 * 
 * @author gpothier
 */
public class MsgSendLogger 
{
	private static MsgSendLogger INSTANCE = new MsgSendLogger();

	public static MsgSendLogger getInstance()
	{
		return INSTANCE;
	}

	private MsgSendLogger()
	{
	}
	
	public void setup(ReflexConfig aConfig)
	{
		Hookset theHookset = new PrimitiveHookset(
				MsgSend.class, 
				StaticConfig.getInstance().getLoggingClassSelector(),
				new MyOperationSelector());

		BLink theLink = aConfig.addBLink(theHookset, new MODefinition.SharedMO(this));

		theLink.setScope(Scope.GLOBAL);
		theLink.setControl(Control.BEFORE_AFTER);
		theLink.setActivation(Activation.DISABLED);
		
		Parameter theBytecodeIndexParameter = StdParameters.BYTECODE_INDEX;
		
		Parameter theBehaviorIdParameter = new MethodIdParameter();
		
		Parameter theTargetParameter = MsgSend.getParameterPool().getTargetObject();
		
		Parameter theArgumentsParameter = StaticConfig.getInstance().getLogParameters() ? 
				StdParameters.ARGUMENTS_ARRAY 
				: new Parameter.Constant("null");
		
		Parameter theResultParameter = new Parameter.Constant("null"); 

		Parameter[] theBeforeParameters = {
				theBytecodeIndexParameter,
				theBehaviorIdParameter,
				theTargetParameter,
				theArgumentsParameter};

		Parameter[] theAfterParameters = { 
				theBytecodeIndexParameter,
				theBehaviorIdParameter, 
				theTargetParameter,
				theResultParameter};

		theLink.setMOCall(Control.BEFORE, new CallDescriptor(
				MsgSendLogger.class.getName(), 
				"before",
				theBeforeParameters));

		theLink.setMOCall(Control.AFTER, new CallDescriptor(
				MsgSendLogger.class.getName(), 
				"after",
				theAfterParameters));

	}

	public void before(
			int aOperationBytecodeIndex,
			int aBehaviourId, 
			Object aTarget, 
			Object[] aArguments)
	{
		System.out.println(String.format (
				"before MsgSend: index: %d, beh.: %d, target: %s, args: %s",
				aOperationBytecodeIndex,
				aBehaviourId,
				aTarget,
				aArguments));
		
		Config.COLLECTOR.logBeforeBehaviorCall(
				System.nanoTime(), 
				Thread.currentThread().getId(), 
				aOperationBytecodeIndex,
				aBehaviourId, 
				aTarget, 
				aArguments);
	}

	public void after(
			int aOperationBytecodeIndex,
			int aBehaviourId,
			Object aTarget, 
			Object aResult)
	{
		System.out.println(String.format (
				"after MsgSend: index: %d, beh.: %d, target: %s, result: %s",
				aOperationBytecodeIndex,
				aBehaviourId,
				aTarget,
				aResult));
		
		Config.COLLECTOR.logAfterBehaviorCall(
				System.nanoTime(), 
				Thread.currentThread().getId(), 
				aOperationBytecodeIndex,
				aBehaviourId, 
				aTarget, 
				aResult);
	}

	private static class TargetParameter extends AbstractParameter
	{
		public String evaluate(Operation aOperation)
		{
			return aOperation.isInStaticContext() ? null : "this";
		}
	}
	
	private static class MyOperationSelector implements OperationSelector
	{
		public boolean accept(Operation aOp, RClass aClass)
		{
			if (aOp instanceof MsgReceive)
			{
				MsgReceive theMsgReceive = (MsgReceive) aOp;
				if (ObjectIdentifier.FIELD_NAME.equals(theMsgReceive.getName())) return false;
			}
			return true;
		}
	}

	private static class MethodIdParameter extends AbstractParameter
	{
		public String evaluate(Operation aOperation)
		{
			MsgSend theMsgSend = (MsgSend) aOperation;
			int theId = ReflexLocationPool.getLocationId(theMsgSend.getMethod());
			return Integer.toString(theId);
		}
		
        public String getType(Operation aOperation)
        {
            return int.class.getName();
        }

	}

}
