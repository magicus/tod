/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".

Contact: gpothier -at- dcc . uchile . cl
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
