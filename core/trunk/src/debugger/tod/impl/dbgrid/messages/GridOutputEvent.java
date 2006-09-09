/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import tod.core.Output;
import tod.impl.dbgrid.dbnode.Indexes;

public class GridOutputEvent extends GridEvent
{
	private String itsData;
	private Output itsOutput;
	
	public GridOutputEvent()
	{
	}


	public GridOutputEvent(String aData, Output aOutput)
	{
		set(aData, aOutput);
	}

	public void set(String aData, Output aOutput)
	{
		itsData = aData;
		itsOutput = aOutput;
	}
	
	@Override
	public MessageType getEventType()
	{
		return MessageType.OUTPUT;
	}

	public String getData()
	{
		return itsData;
	}

	public Output getOutput()
	{
		return itsOutput;
	}
	
	@Override
	public void index(Indexes aIndexes, long aPointer)
	{
		super.index(aIndexes, aPointer);
	}
	
	@Override
	public String toString()
	{
		return String.format(
				"%s (d: %d, o: %s, %s)",
				getEventType(),
				itsData,
				itsOutput,
				toString0());
	}

}
