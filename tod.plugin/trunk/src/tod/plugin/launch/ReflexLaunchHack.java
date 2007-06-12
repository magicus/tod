/*
 * Created on Jun 7, 2007
 */
package tod.plugin.launch;

import java.util.ArrayList;
import java.util.List;

import reflex.api.API;
import reflex.api.ReflexConfig;
import reflex.api.call.CallDescriptor;
import reflex.api.call.Parameter;
import reflex.api.hookset.PrimitiveHookset;
import reflex.api.link.BLink;
import reflex.api.link.MODefinition;
import reflex.api.link.attribute.Activation;
import reflex.api.link.attribute.Control;
import reflex.api.link.attribute.DeclaredType;
import reflex.api.link.attribute.Initialization;
import reflex.api.link.attribute.Scope;
import reflex.api.mop.IExecutionPointClosure;
import reflex.std.installer.StdParameters;
import reflex.std.operation.MsgReceive;
import reflex.tools.selectors.NameCS;
import reflex.tools.selectors.NameOS;
import tod.ReflexRiver;
import zz.utils.Utils;

/**
 * This configuration intercepts calls to 
 * @author gpothier
 */
public class ReflexLaunchHack extends ReflexConfig
{
	private static final ThreadLocal<String[]> itsArgs = new ThreadLocal<String[]>();
	
	public static void setupReflex()
	{
		ReflexRiver.addConfigClasses(ReflexLaunchHack.class);
		ReflexRiver.addWSEntries("+org.eclipse.jdt.launching.VMRunnerConfiguration");
	}

	@Override
	public void initReflex()
	{
		// Setup hookset
        PrimitiveHookset theHookset = new PrimitiveHookset(
        		MsgReceive.class, 
        		new NameCS("org.eclipse.jdt.launching.VMRunnerConfiguration"),
        		new NameOS("setVMArguments"));
        
        // Setup link
        MODefinition theMODefinition = new MODefinition.SharedMO(new MO());
        BLink theLink = API.links().createBLink(theHookset, theMODefinition);
        addLink(theLink);

        theLink.setControl(Control.AROUND);
        theLink.setScope(Scope.GLOBAL);
        
        // Optimizations
        theLink.setDeclaredType(new DeclaredType(MO.class.getName()));
		theLink.setActivation(Activation.DISABLED);
		theLink.setInitialization(Initialization.EAGER);
		
		// Setup call
		theLink.setCall(Control.AROUND, new CallDescriptor(
				MO.class.getName(),
				"setVMArguments",
				Parameter.CLOSURE,
				new StdParameters.IndexedArgument(0)));
		
		theLink.setCall(Control.AFTER, new CallDescriptor(
				"reflex.lib.pom.impl.POMMetaobject",
				"returnTrap"));
	}
	
	/**
	 * Activates/desactivates the hack on the current thread.
	 */
	public static void setArgs(String[] aArgs)
	{
		itsArgs.set(aArgs);
	}
	
	private static class MO
	{
		private static MO INSTANCE = new MO();

		public static MO getInstance()
		{
			return INSTANCE;
		}

		private MO()
		{
		}
		
		public Object setVMArguments(IExecutionPointClosure aClosures, String[] args)
		{
			String[] theAdditionalArgs = itsArgs.get();
			if (theAdditionalArgs != null)
			{
				List<String> theArgs = new ArrayList<String>();
				Utils.fillCollection(theArgs, theAdditionalArgs);
				Utils.fillCollection(theArgs, args);
				
				aClosures.setArg(0, theArgs.toArray(new String[theArgs.size()]));
			}
			
			return aClosures.proceed();
		}
	}
}
