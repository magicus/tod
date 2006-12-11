/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.impl.dbgrid.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.IGridBrowserListener;
import tod.impl.dbgrid.monitoring.MonitorUI;
import tod.impl.dbgrid.monitoring.Monitor.MonitorData;
import zz.utils.ListMap;
import zz.utils.SimpleAction;

/**
 * Provides a monitoring console for the distributed database.
 * @author gpothier
 */
public class GridConsole extends JPanel
implements IGridBrowserListener
{
	private GridLogBrowser itsBrowser;
	private MonitorUI itsMonitorUI;

	/**
	 * Monitor data, per node. 
	 */
	private ListMap<Integer, MonitorData> itsMonitorData = new ListMap<Integer, MonitorData>();
	
	public GridConsole(GridLogBrowser aBrowser)
	{
		itsBrowser = aBrowser;
		createUI();
		
		itsBrowser.addListener(this);
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
					itsBrowser.getMaster().clear();
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


	public void monitorData(int aNodeId, MonitorData aData)
	{
		itsMonitorData.add(aNodeId, aData);
		itsMonitorUI.setData(aData);
	}
	
	
}
