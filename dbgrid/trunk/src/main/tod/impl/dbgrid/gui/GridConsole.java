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
package tod.impl.dbgrid.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.RIGridMasterListener;
import tod.impl.dbgrid.monitoring.MonitorUI;
import tod.impl.dbgrid.monitoring.Monitor.MonitorData;
import zz.utils.ListMap;
import zz.utils.SimpleAction;

/**
 * Provides a monitoring console for the distributed database.
 * @author gpothier
 */
public class GridConsole extends JPanel
{
	static
	{
		System.out.println("GridConsole loaded by: "+GridConsole.class.getClassLoader());
	}
	
	private RIGridMaster itsMaster;
	private MonitorUI itsMonitorUI;

	/**
	 * Monitor data, per node. 
	 */
	private ListMap<String, MonitorData> itsMonitorData = 
		new ListMap<String, MonitorData>();
	
	public GridConsole(RIGridMaster aMaster)
	{
		itsMaster = aMaster;
		createUI();
		
		try
		{
			itsMaster.addListener(new MasterListener());
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	

	private void createUI()
	{
		itsMonitorUI = new MonitorUI();
		setLayout(new BorderLayout());
		
		add(createToolbar(), BorderLayout.NORTH);
		add(itsMonitorUI, BorderLayout.CENTER);
	}
	
	private JComponent createToolbar()
	{
		JPanel theToolbar = new JPanel();
		JButton theClearButton = new JButton(new SimpleAction("Clear DB")
		{
			public void actionPerformed(ActionEvent aE)
			{
				try
				{
					itsMaster.clear();
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}
		});
		
		theToolbar.add(theClearButton);
		
		return theToolbar;
	}

	public void monitorData(String aNodeId, MonitorData aData)
	{
		itsMonitorData.add(aNodeId, aData);
		itsMonitorUI.setData(aData);
	}
	
	private class MasterListener extends UnicastRemoteObject
	implements RIGridMasterListener
	{
		private static final long serialVersionUID = -1912140049548993769L;

		public MasterListener() throws RemoteException
		{
		}

		public void eventsReceived()
		{
		}

		public void exception(Throwable aThrowable)
		{
		}

		public void monitorData(String aNodeId, MonitorData aData) 
		{
			GridConsole.this.monitorData(aNodeId, aData);
		}
		
	}
}