/*
 * Created on Oct 25, 2004
 */
package tod.impl.common.event;

import tod.core.Output;
import tod.core.model.event.IOutputEvent;

/**
 * @author gpothier
 */
public class OutputEvent extends Event implements IOutputEvent
{
	private String itsData;
	private Output itsOutput;
	
	public String getData()
	{
		return itsData;
	}
	
	public void setData(String aData)
	{
		itsData = aData;
	}
	
	public Output getOutput()
	{
		return itsOutput;
	}
	
	public void setOutput(Output aOutput)
	{
		itsOutput = aOutput;
	}
}
