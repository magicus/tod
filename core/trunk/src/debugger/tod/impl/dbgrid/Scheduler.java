/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

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
package tod.impl.dbgrid;

import reflex.lib.pom.POMGroupDef;
import reflex.lib.pom.POMScheduler;
import reflex.lib.pom.Request;

/**
 * This POM scheduler ensures that all calls to the database are serialized.
 * @author gpothier
 */
public class Scheduler extends POMScheduler implements POMGroupDef
{
	private boolean itsWorking = false;
	
	@Override
	protected void schedule()
	{
		if (! itsWorking) itsWorking = executeOldest();
	}

	@Override
	protected void leave(Request aReq)
	{
		itsWorking = false;
	}
	
	@Override
	protected void scheduling(Request aReq)
	{
		System.out.println(String.format(
				"Scheduler (%s) - executing %s.",
				this,
				aReq));
	}
	
	public Object getGroup(Object aObject)
	{
		return "dbgrid";
	}

}
