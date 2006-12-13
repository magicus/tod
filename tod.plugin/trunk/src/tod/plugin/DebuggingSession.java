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

import org.eclipse.jdt.core.IJavaProject;

import tod.core.session.DelegatedSession;
import tod.core.session.ISession;

public class DebuggingSession extends DelegatedSession
{
	private IJavaProject itsJavaProject;

	public DebuggingSession(ISession aDelegate, IJavaProject aJavaProject)
	{
		super(aDelegate);
		itsJavaProject = aJavaProject;
	}

	public IJavaProject getJavaProject()
	{
		return itsJavaProject;
	}
}
