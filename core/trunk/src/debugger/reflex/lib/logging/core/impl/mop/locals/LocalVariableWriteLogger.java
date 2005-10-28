/*
 * Created on Nov 4, 2004
 */
package reflex.lib.logging.core.impl.mop.locals;

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
import reflex.api.model.RLocalVariable;
import reflex.lib.logging.core.api.config.StaticConfig;
import reflex.lib.logging.core.impl.mop.Config;
import reflex.std.installer.StdParameters;
import reflex.std.installer.localvariableaccess.LocalVariableAccessPool;
import reflex.std.operation.LocalVariableAccess;
import reflex.tools.selectors.LocalVariableWriteOS;

/**
 * Logs local variable write accesses.
 * @author gpothier
 */
public class LocalVariableWriteLogger 
{
	private static LocalVariableWriteLogger INSTANCE = new LocalVariableWriteLogger();

	public static LocalVariableWriteLogger getInstance()
	{
		return INSTANCE;
	}

	private LocalVariableWriteLogger()
	{
	}
	
    public void setup(ReflexConfig aConfig)
    {
        Hookset theHookset = new PrimitiveHookset(
                LocalVariableAccess.class,
                StaticConfig.getInstance().getLoggingClassSelector(), 
				LocalVariableWriteOS.getInstance());

        BLink theLink = aConfig.addBLink(
                theHookset,
                new MODefinition.SharedMO(this));

        theLink.setScope(Scope.GLOBAL);
        theLink.setControl(Control.AFTER);
        theLink.setActivation(Activation.DISABLED);
        
        LocalVariableAccessPool thePool = LocalVariableAccess.getParameterPool();

        Parameter[] theParameters = {
        		StdParameters.BYTECODE_INDEX,
        		new VariableIdParameter(),
        		thePool.getTargetObject(),				
	        	thePool.getValue()
        };
        
        theLink.setMOCall(
				new CallDescriptor(
						LocalVariableWriteLogger.class.getName(),
						"localWrite",
						theParameters));
    }

	public void localWrite (
			int aOperationBytecodeIndex,
			int aVariableId, 
			Object aTarget,
			Object aValue)
	{
		Config.COLLECTOR.logLocalVariableWrite(
				System.nanoTime(), 
				Thread.currentThread().getId(),
				aOperationBytecodeIndex,
				aVariableId,
				aTarget,
				aValue);
	}	

    
	private static class VariableIdParameter extends AbstractParameter
	{
		public String evaluate(Operation aOperation)
		{
			LocalVariableAccess	theLocalVariableAccess = (LocalVariableAccess) aOperation;
			RLocalVariable theVariable = theLocalVariableAccess.getVariable();
			
			int theIndex = theVariable.getSymbolIndex();
			if (theIndex < 0) theIndex = -theVariable.getIndex()-1;
			
			return Integer.toString(theIndex);
		}
		
        public String getType(Operation aOperation)
        {
            return int.class.getName();
        }
	}


    
}
