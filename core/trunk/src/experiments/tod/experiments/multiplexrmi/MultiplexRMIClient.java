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
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;

import zz.utils.net.MultiplexRMISocketFactory;

public class MultiplexRMIClient extends UnicastRemoteObject
implements RIMultiplexRMIClient
{
	static {
		MultiplexRMISocketFactory.createClient("client", "localhost", 6789);
	}
	
	public MultiplexRMIClient() throws RemoteException
	{
	}

	public String callback(String aMessage) 
	{
		System.out.println("Client received message: "+aMessage);
		return "Client processed message.";
	}
	
	public static void main(String[] args) throws IOException, NotBoundException
	{
		Registry theRegistry = LocateRegistry.getRegistry("server", 90, MultiplexRMISocketFactory.get());
		RIMultiplexRMIServer theServer = (RIMultiplexRMIServer) theRegistry.lookup("server");
		theServer.addClient(new MultiplexRMIClient());
	}

}
