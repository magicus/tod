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
package tod.core.database.browser;

/**
 * Utility methods for log/event browsers
 * @author gpothier
 */
public class BrowserUtils
{
	/**
	 * Returns the timestamp of the first event available to the 
	 * given browser, or 0 if there is no event. 
	 */
	public static long getFirstTimestamp(IEventBrowser aBrowser)
	{
		aBrowser.setNextTimestamp(0);
		if (aBrowser.hasNext())
		{
			return aBrowser.next().getTimestamp();
		}
		else return 0;
	}
	
	/**
	 * Returns the timestamp of the last event available to the 
	 * given browser, or 0 if there is no event. 
	 */
	public static long getLastTimestamp(IEventBrowser aBrowser)
	{
		aBrowser.setPreviousTimestamp(Long.MAX_VALUE);
		if (aBrowser.hasPrevious())
		{
			return aBrowser.previous().getTimestamp();
		}
		else return 0;
	}

}
