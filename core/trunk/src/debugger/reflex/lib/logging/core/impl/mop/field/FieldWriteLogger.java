/*
 * Created on Nov 4, 2004
 */
package reflex.lib.logging.core.impl.mop.field;

import reflex.api.API;
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
import reflex.lib.logging.core.api.config.StaticConfig;
import reflex.lib.logging.core.impl.mop.Config;
import reflex.lib.logging.core.impl.mop.ReflexLocationPool;
import reflex.lib.logging.core.impl.mop.WhereParameter;
import reflex.std.installer.FieldAccessPool;
import reflex.std.installer.StdParameters;
import reflex.std.operation.FieldAccess;
import reflex.tools.selectors.FieldWriteOS;

/**
 * Logs field write accesses.
 * @author gpothier
 */
public class FieldWriteLogger 
{
	private static FieldWriteLogger INSTANCE = new FieldWriteLogger();

	public static FieldWriteLogger getInstance()
	{
		return INSTANCE;
	}

	private FieldWriteLogger()
	{
	}
	
    public void setup(ReflexConfig aConfig)
    {
        Hookset theHookset = new PrimitiveHookset(
                FieldAccess.class,
                StaticConfig.getInstance().getLoggingClassSelector(), 
				FieldWriteOS.getInstance());

        BLink theLink = aConfig.addBLink(
                theHookset,
                new MODefinition.SharedMO(this));

        theLink.setScope(Scope.GLOBAL);
        theLink.setControl(Control.AFTER);
        theLink.setActivation(Activation.DISABLED);
        
        FieldAccessPool thePool = FieldAccess.getParameterPool();

        Parameter[] theParameters = {
        		StdParameters.BYTECODE_INDEX,
        		new FieldIdParameter(),
        		thePool.getTargetObject(),				
	        	thePool.getValue()
        };
        
        theLink.setMOCall(
				new CallDescriptor(
						FieldWriteLogger.class.getName(),
						"fieldWrite",
						theParameters));
    }

	public void fieldWrite (
			int aOperationBytecodeIndex,
			int aFieldId, 
			Object aTarget,
			Object aValue)
	{
		System.out.println(String.format (
				"Field write: field: %d",
				aFieldId));

		
		Config.COLLECTOR.logFieldWrite(
				System.nanoTime(), 
				Thread.currentThread().getId(),
				aOperationBytecodeIndex,
				aFieldId,
				aTarget,
				aValue);
	}	

    
	private static class FieldIdParameter extends AbstractParameter
	{
		public String evaluate(Operation aOperation)
		{
			FieldAccess	theFieldAccess = (FieldAccess) aOperation;
			int theId = ReflexLocationPool.getLocationId(theFieldAccess.getField());
			return Integer.toString(theId);
		}
		
        public String getType(Operation aOperation)
        {
            return int.class.getName();
        }

	}


    
}
