/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
*/
package tod.core.database.structure;

import java.io.Serializable;


/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a thread.
 * @author gpothier
 */
public class ThreadInfo implements IThreadInfo, Serializable
{
	private IHostInfo itsHost;
	private int itsId;
	private long itsJVMId;
	private String itsName;
	
	public ThreadInfo(IHostInfo aHost, int aId, long aJVMId, String aName)
	{
		itsHost = aHost;
		itsId = aId;
		itsJVMId = aJVMId;
		itsName = aName;
	}

	public int getId()
	{
		return itsId;
	}
	
	public long getJVMId()
	{
		return itsJVMId;
	}

	public IHostInfo getHost()
	{
		return itsHost;
	}

	public String getName()
	{
		return itsName;
	}

	public void setName(String aName)
	{
		itsName = aName;
	}
	
	@Override
	public String toString()
	{
		return "Thread ("+getId()+", "+getJVMId()+", "+getName()+")";
	}

}
