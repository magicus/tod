/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import tod.core.Output;
import tod.core.database.event.ILogEvent;
import tod.impl.common.event.OutputEvent;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.dbnode.Indexes;

public class GridOutputEvent extends GridEvent
{
	private static final long serialVersionUID = 2432106275871615061L;
	
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
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		OutputEvent theEvent = new OutputEvent();
		initEvent(aBrowser, theEvent);
		theEvent.setData(getData());
		theEvent.setOutput(getOutput());
		return theEvent;
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
