/*
 * Created on Oct 11, 2004
 */
package reflex.lib.logging.core.impl.mop.behavior;

import java.io.IOException;
import java.io.PrintStream;

import reflex.lib.logging.core.api.config.DynamicConfig;
import reflex.lib.logging.core.api.config.DynamicConfig.OutputBehaviour;
import reflex.lib.logging.core.impl.mop.Config;
import tod.core.Output;


/**
 * Handles output collecting behaviours.
 * @author gpothier
 */
public class OutputHandler
{
	/**
	 * The handled output
	 */
	private Output itsOutput;
	
	private PrintStream itsCollectorStream;
	private ThreadLocal<PrintStream> itsOriginalSafeStream;
	private PrintStream itsOriginalStream;
	
	public OutputHandler(Output aOutput)
	{
		itsOutput = aOutput;
		itsCollectorStream = new PrintStream (new CollectorStream(this), true);
	}
	
	private PrintStream getOriginalStream(OutputBehaviour aBehaviour)
	{
		if (aBehaviour.getThreadSafe())
			return itsOriginalSafeStream.get();
		else return itsOriginalStream;

	}
	
	private void initOriginalStream(OutputBehaviour aBehaviour)
	{
		if (aBehaviour.getThreadSafe())
			itsOriginalSafeStream.set(itsOutput.get());
		else itsOriginalStream = itsOutput.get();
		
	}
	
	/**
	 * Called whenever the stream is flushed.
	 */
	public void handleFlush (CollectorStream aStream) throws IOException
	{
		OutputBehaviour theOutputBehaviour = 
			DynamicConfig.getInstance().getOutputBehaviour(itsOutput);
		
		if (theOutputBehaviour.getWriteThrough())
		{
			aStream.writeTo(getOriginalStream(theOutputBehaviour));
		}
		
		byte[] theData = aStream.toByteArray();
		Config.COLLECTOR.logOutput(
				System.nanoTime(), 
				Thread.currentThread().getId(),
				itsOutput,
				theData);

	}
	
	public void startCollecting()
	{
		OutputBehaviour theOutputBehaviour = 
			DynamicConfig.getInstance().getOutputBehaviour(itsOutput);
		
		if (theOutputBehaviour.getCollect())
		{
			initOriginalStream(theOutputBehaviour);
			itsOutput.set(itsCollectorStream);
		}
	}
	
	public void stopCollecting()
	{
		OutputBehaviour theOutputBehaviour = 
			DynamicConfig.getInstance().getOutputBehaviour(itsOutput);
		
		if (theOutputBehaviour.getCollect())
		{
			itsOutput.set(getOriginalStream(theOutputBehaviour));
		}			
	}
}