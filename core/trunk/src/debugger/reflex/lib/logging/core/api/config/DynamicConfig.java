/*
 * Created on Oct 9, 2004
 */
package reflex.lib.logging.core.api.config;

import java.util.EnumMap;
import java.util.Map;

import tod.core.Output;


/**
 * @author gpothier
 */
public class DynamicConfig
{
	private static DynamicConfig INSTANCE = new DynamicConfig();

	public static DynamicConfig getInstance()
	{
		return INSTANCE;
	}

	private DynamicConfig()
	{
		setOutputBehaviour(Output.OUT, false, false, false);
		setOutputBehaviour(Output.ERR, false, false, false);
	}
	
	private Map<Output, OutputBehaviour> itsOutputBehaviours =
		new EnumMap<Output, OutputBehaviour>(Output.class);
	
	private void setOutputBehaviour (
			Output aOutput,
			boolean aCollect,
			boolean aWriteThrough,
			boolean aThreadSafe)
	{
		itsOutputBehaviours.put (
				aOutput, 
				new OutputBehaviour(aCollect, aWriteThrough, aThreadSafe));
	}

	public OutputBehaviour getOutputBehaviour (Output aOutput)
	{
		return itsOutputBehaviours.get (aOutput);
	}
	
	public static class OutputBehaviour
	{
		private boolean itsCollect;
		private boolean itsWriteThrough;
		private boolean itsThreadSafe;
		
		public OutputBehaviour(boolean aCollect, boolean aWriteThrough, boolean aThreadSafe)
		{
			itsCollect = aCollect;
			itsWriteThrough = aWriteThrough;
			itsThreadSafe = aThreadSafe;
		}
		
		
		public boolean getCollect()
		{
			return itsCollect;
		}
		
		public boolean getThreadSafe()
		{
			return itsThreadSafe;
		}
		
		public boolean getWriteThrough()
		{
			return itsWriteThrough;
		}
	}
	
}
