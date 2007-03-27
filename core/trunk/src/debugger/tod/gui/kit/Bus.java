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
package tod.gui.kit;

import java.awt.Component;
import java.util.List;

import tod.gui.kit.messages.Message;

import zz.utils.ListMap;

public class Bus
{
	public static final Bus ROOT_BUS = new Bus(null);
	
	/**
	 * Returns the bus that should be used by the given component.
	 */
	public static Bus getBus(Component aComponent)
	{
		while(true)
		{
			if (aComponent == null) return ROOT_BUS;
			else if (aComponent instanceof IBusOwner)
			{
				IBusOwner theBusOwner = (IBusOwner) aComponent;
				return theBusOwner.getBus();
			}
			else aComponent = aComponent.getParent();
		} 
	}
	
	private Component itsOwner;
	
	private ListMap<String, IBusListener> itsListeners = new ListMap<String, IBusListener>();
	
	public Bus(Component aOwner)
	{
		itsOwner = aOwner;
	}
	
	public void postMessage(Message aMessage)
	{
		List<IBusListener> theListeners = itsListeners.get(aMessage.getId());
		if (theListeners != null) for (IBusListener theListener : theListeners)
		{
			if (theListener.processMessage(aMessage)) return;
		}
			
		
		if (itsOwner != null) getBus(itsOwner.getParent()).postMessage(aMessage);
	}
	
	public void subscribe(String aId, IBusListener aListener)
	{
		itsListeners.add(aId, aListener);
	}
	
	public void unsubscribe(String aId, IBusListener aListener)
	{
		itsListeners.remove(aId, aListener);
	}
}
