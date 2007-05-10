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
package tod.utils;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMITest
{
	public static void main(String[] args) throws Exception
	{
		LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
		Registry theRegistry = LocateRegistry.getRegistry("localhost");

		MyRemote theRemote = new MyRemote();
		
		System.out.println("Binding...");
		theRegistry.rebind("test", theRemote);
		System.out.println("Bound");
		
		System.exit(0);
	}
	
	private static class MyRemote extends UnicastRemoteObject
	{
		protected MyRemote() throws RemoteException
		{
			super();
		}
	}
}