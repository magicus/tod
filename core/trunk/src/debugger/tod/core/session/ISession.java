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
package tod.core.session;

import java.net.URI;

import javax.swing.JComponent;

import tod.core.database.browser.ILocationsRepository;
import tod.core.database.browser.ILogBrowser;

public interface ISession
{
	/**
	 * Returns a resource identifier for this session, that can be used to retrieve
	 * previous sessions.
	 * @see ISessionFactory#loadSession(URI);
	 */
	public URI getUri();
	public ILogBrowser getLogBrowser();
	
	/**
	 * Returns the path where the agent caches instrumented classes
	 */
	public String getCachedClassesPath();
	
	/**
	 * Disconnects this session from the target VM, if it is connected 
	 */
	public void disconnect();
	
	/**
	 * Creates a console that can be used to control this session.
	 */
	public JComponent createConsole();
}
