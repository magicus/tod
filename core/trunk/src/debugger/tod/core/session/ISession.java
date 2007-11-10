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
package tod.core.session;

import java.net.URI;

import javax.swing.JComponent;

import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.ILocationsRepository;

public interface ISession
{
	/**
	 * Returns the current configuration of this session.
	 */
	public TODConfig getConfig();
	
	/**
	 * Returns the monitor of this session.
	 */
	public ISessionMonitor getMonitor();
	
	/**
	 * Returns a resource identifier for this session, that can be used to retrieve
	 * previous sessions.
	 * @see ISessionFactory#loadSession(URI);
	 */
	public URI getUri();
	public ILogBrowser getLogBrowser();
	
	/**
	 * Returns the information that permits to client VMs to connect
	 * to the log collector.
	 */
	public ConnectionInfo getConnectionInfo();
	
	/**
	 * Disconnects this session from the target VMs, if it is connected 
	 */
	public void disconnect();
	
	/**
	 * Flush currently buffered events so that they are made accessible. 
	 */
	public void flush();
	
	/**
	 * Creates a console that can be used to control this session.
	 */
	public JComponent createConsole();
	
	/**
	 * Returns true if this session is still alive.
	 * A dead session is usually the consequence of a communication error
	 * with the database. 
	 */
	public boolean isAlive();
}
