/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.experiments.multiplexrmi;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;

import zz.utils.net.MultiplexRMISocketFactory;

public class MultiplexRMIServer extends UnicastRemoteObject
implements RIMultiplexRMIServer
{
	static {
		try
		{
			MultiplexRMISocketFactory.createServer("server", 6789);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		} 
	}
	

	public MultiplexRMIServer() throws RemoteException
	{
		super(0, MultiplexRMISocketFactory.get(), MultiplexRMISocketFactory.get());
	}

	public void addClient(RIMultiplexRMIClient aClient) throws RemoteException 
	{
		System.out.println("Added client, saying hello");
		String theResponse = aClient.callback("Hello new client");
		System.out.println("Client responded: "+theResponse);
	}

	public static void main(String[] args) throws IOException, AlreadyBoundException
	{
		Registry theRegistry = LocateRegistry.createRegistry(90, MultiplexRMISocketFactory.get(), MultiplexRMISocketFactory.get());
		theRegistry.bind("server", new MultiplexRMIServer());
	}
	
}
