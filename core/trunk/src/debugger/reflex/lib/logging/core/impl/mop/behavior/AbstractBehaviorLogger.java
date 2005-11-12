/*
 * Created on Oct 9, 2004
 */
package reflex.lib.logging.core.impl.mop.behavior;

import javassist.NotFoundException;
import reflex.api.API;
import reflex.api.ReflexConfig;
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
import reflex.api.model.RBehavior;
import reflex.api.model.RClass;
import reflex.api.model.iterator.RBehaviorIterator;
import reflex.core.build.Builder;
import reflex.core.build.InstrumentationListener;
import reflex.lib.logging.core.api.config.StaticConfig;
import reflex.lib.logging.core.impl.mop.Config;
import reflex.lib.logging.core.impl.mop.ReflexLocationPool;
import reflex.lib.logging.core.impl.mop.identification.ObjectIdentifier;
import reflex.std.operation.MsgReceive;
import tod.core.ILocationRegistrer;
import tod.core.Output;

/**
 * Base class for method and constructor loggers. Implements the logging
 * mechanism and output collection. Subclasses must provide information used for
 * setup and reification.
 * <br/>
 * This class is also used as an instrumentation listener so as to
 * send behavior attributes when a class is instrumented.
 * 
 * @author gpothier
 */
public abstract class AbstractBehaviorLogger implements InstrumentationListener 
{
	private OutputHandler[] itsHandlers =
	{ new OutputHandler(Output.OUT), new OutputHandler(Output.ERR), };

	/**
	 * The class of operation this logger should intercept.
	 */
	private Class itsOperationClass;

	/**
	 * A parameter that reifies the id of the intercepted behaviour.
	 */
	private Parameter itsBehaviourIdParameter;

	public AbstractBehaviorLogger(
			Class aOperationClass, 
			Parameter aBehaviourIdParameter)
	{
		itsOperationClass = aOperationClass;
		itsBehaviourIdParameter = aBehaviourIdParameter;
	}

	public void setup(ReflexConfig aConfig)
	{
        Builder.addInstrumentationListener(this);
        
		Hookset theHookset = new PrimitiveHookset(
				itsOperationClass, 
				StaticConfig.getInstance().getLoggingClassSelector(),
				new MyOperationSelector());

		BLink theLink = aConfig.addBLink(theHookset, new MODefinition.SharedMO(this));

		theLink.setScope(Scope.GLOBAL);
		theLink.setControl(Control.BEFORE_AFTER);
		theLink.setActivation(Activation.DISABLED);
		
		Parameter[] theParameters = {itsBehaviourIdParameter};

		theLink.setMOCall(Control.BEFORE, new CallDescriptor(
				AbstractBehaviorLogger.class.getName(), 
				"enter",
				theParameters));

		theLink.setMOCall(Control.AFTER, new CallDescriptor(
				AbstractBehaviorLogger.class.getName(), 
				"exit",
				theParameters));

	}

	public void classGenerated(String aClassName)
	{
	}

	public void classInstrumented(String aClassName)
	{
        try
		{
			RClass theClass = API.getClassPool().get(aClassName);
            RBehaviorIterator theIterator = theClass.getDeclaredBehaviorIterator();
            while (theIterator.hasNext())
            {
                RBehavior theBehavior = (RBehavior) theIterator.next();
                int theBehaviorId = ReflexLocationPool.getLocationId(theBehavior);
                
                Config.COLLECTOR.registerBehaviorAttributes(
                        theBehaviorId,
                        DebugTablesBuilder.createTable(theBehavior.getLineNumberTable()),
                        DebugTablesBuilder.createTable(theBehavior.getLocalVariableTable()));
            }
		}
		catch (NotFoundException e)
		{
			e.printStackTrace();
		}
        
	}

	public void enter(int aBehaviourId, Object aObject, Object[] aArguments)
	{
		System.out.println(String.format (
				"Behavior enter: beh: %d",
				aBehaviourId));
		
		Config.COLLECTOR.logBehaviorEnter(
				System.nanoTime(), 
				Thread.currentThread().getId(), 
				aBehaviourId,
				aObject,
				aArguments);

		for (OutputHandler theHandler : itsHandlers)
			theHandler.startCollecting();
	}

	public void exit(int aBehaviourId, Object aResult)
	{
		System.out.println(String.format (
				"Behavior exit: beh: %d",
				aBehaviourId));
		
		for (OutputHandler theHandler : itsHandlers)
			theHandler.stopCollecting();
		
		Config.COLLECTOR.logBehaviorExit(
				System.nanoTime(), 
				Thread.currentThread().getId(), 
				aBehaviourId,
				aResult);
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
}
