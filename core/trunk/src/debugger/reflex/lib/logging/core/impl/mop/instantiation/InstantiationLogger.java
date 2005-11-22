/*
 * Created on Oct 9, 2004
 */
package reflex.lib.logging.core.impl.mop.instantiation;

import reflex.api.ReflexConfig;
import reflex.api.call.AbstractParameter;
import reflex.api.call.CallDescriptor;
import reflex.api.call.Parameter;
import reflex.api.hookset.Hookset;
import reflex.api.hookset.Operation;
import reflex.api.hookset.PrimitiveHookset;
import reflex.api.link.BLink;
import reflex.api.link.MODefinition;
import reflex.api.link.attribute.Activation;
import reflex.api.link.attribute.Control;
import reflex.api.link.attribute.Scope;
import reflex.lib.logging.core.impl.mop.Config;
import reflex.lib.logging.core.impl.mop.ReflexLocationPool;
import reflex.std.installer.StdParameters;
import reflex.std.installer.instantiation.InstantiationPool;
import reflex.std.operation.Instantiation;
import reflex.tools.selectors.AllOS;
import tod.core.config.StaticConfig;

/**
 * @author gpothier
 */
public class InstantiationLogger 
{
	private static InstantiationLogger INSTANCE = new InstantiationLogger();

	public static InstantiationLogger getInstance()
	{
		return INSTANCE;
	}

	private InstantiationLogger()
	{
	}

    public void setup(ReflexConfig aConfig)
    {
        Hookset theHookset = new PrimitiveHookset(
                Instantiation.class,
                StaticConfig.getInstance().getLoggingClassSelector(), 
				AllOS.getInstance());

        BLink theLink = aConfig.addBLink(
                theHookset,
                new MODefinition.SharedMO(this));

        theLink.setScope(Scope.GLOBAL);
        theLink.setControl(Control.AFTER);
        theLink.setActivation(Activation.DISABLED);
        
        InstantiationPool thePool = Instantiation.getParameterPool();

        Parameter[] theParameters = 
        {
        		StdParameters.BYTECODE_INDEX,
        		new TypeIdParameter(),
				thePool.getResult(),
        };
        
        theLink.setMOCall(
        		Control.AFTER,
				new CallDescriptor(
						getClass().getName(),
						"instantiate",
						theParameters));
    	
    }

	
	public void instantiate (
			int aOperationBytecodeIndex,
			int aTypeId,
			Object aInstance)
	{
		System.out.println(String.format (
				"Instantiated: type: %d",
				aTypeId));

		Config.COLLECTOR.logInstantiation(Thread.currentThread().getId());
	}

	private static class TypeIdParameter extends AbstractParameter
	{
		public String evaluate(Operation aOperation)
		{
			Instantiation theInstantiation = (Instantiation) aOperation;
			int theId = ReflexLocationPool.getLocationId(theInstantiation.getTargetType());
			return Integer.toString(theId);
		}
		
        public String getType(Operation aOperation)
        {
            return int.class.getName();
        }

	}
}
