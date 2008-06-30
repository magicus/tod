/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.messages;

import tod.agent.Output;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IStructureDatabase;
import tod.impl.common.event.OutputEvent;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.evdbng.db.Indexes;

public class GridOutputEvent extends GridEventNG
{
	private static final long serialVersionUID = 2432106275871615061L;
	
	private String itsData;
	private Output itsOutput;
	
	public GridOutputEvent(IStructureDatabase aStructureDatabase)
	{
		super(aStructureDatabase);
	}


	public GridOutputEvent(IStructureDatabase aStructureDatabase, String aData, Output aOutput)
	{
		super(aStructureDatabase);
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
		OutputEvent theEvent = new OutputEvent(aBrowser);
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
	public void index(Indexes aIndexes, int aId)
	{
		super.index(aIndexes, aId);
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
