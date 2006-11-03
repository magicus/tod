/*
 * Created on Nov 3, 2006
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
