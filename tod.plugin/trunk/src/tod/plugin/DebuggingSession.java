/*
TOD plugin - Eclipse pluging for TOD
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
package tod.plugin;

import org.eclipse.debug.core.ILaunch;

import tod.core.database.structure.SourceRange;
import tod.core.session.DelegatedSession;
import tod.core.session.ISession;

public class DebuggingSession extends DelegatedSession
{
	private final ILaunch itsLaunch;
	private final SourceRevealer itsGotoSourceDelegate;

	public DebuggingSession(
			ISession aDelegate, 
			ILaunch aLaunch, 
			SourceRevealer aGotoSourceDelegate)
	{
		super(aDelegate);
		itsLaunch = aLaunch;
		itsGotoSourceDelegate = aGotoSourceDelegate;
	}

	public void gotoSource(SourceRange aSourceRange)
	{
		if (itsGotoSourceDelegate != null) itsGotoSourceDelegate.gotoSource(aSourceRange);
	}
	

	public ILaunch getLaunch()
	{
		return itsLaunch;
	}
}
