/*
 * Created on Jul 27, 2006
 */
package tod.impl.dbgrid.monitoring;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import tod.impl.dbgrid.monitoring.Monitor.IndividualProbeValue;
import tod.impl.dbgrid.monitoring.Monitor.KeyMonitorData;
import zz.utils.treetable.AbstractTreeTableModel;
import zz.utils.treetable.JTreeTable;

public class MonitorUI extends JPanel
{
	private JTreeTable itsTreeTable;
	private Monitor itsMonitor;
	
	public MonitorUI(Monitor aMonitor)
	{
		itsMonitor = aMonitor;
		createUI();
	}
	
	public static void showFrame(Monitor aMonitor)
	{
		MonitorUI theUI = new MonitorUI(aMonitor);
		JFrame theFrame = new JFrame("Monitor");
		theFrame.setContentPane(theUI);
		theFrame.pack();
		theFrame.setVisible(true);
	}

	private void createUI()
	{
		setLayout(new BorderLayout());
		
		itsTreeTable = new JTreeTable();
		add(new JScrollPane(itsTreeTable), BorderLayout.CENTER);
		
		new Timer(10*1000, new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				update();
			}
		}).start();
	}
	
	private void update()
	{
		List<KeyMonitorData> theProbeData = itsMonitor.getProbeData();
		itsTreeTable.setTreeTableModel(new MyTreeModel(theProbeData));
	}
	
	private static class MyTreeModel extends AbstractTreeTableModel
	{
		private List<KeyMonitorData> itsData;
		
		public MyTreeModel(List<KeyMonitorData> aData)
		{
			super(aData);
			itsData = aData;
		}

		public int getColumnCount()
		{
			return 2;
		}

		public String getColumnName(int aColumn)
		{
			switch(aColumn)
			{
			case 0: return "a";
			case 1: return "b";
			default: return "What? "+aColumn;
			}
		}

		public Object getValueAt(Object aNode, int aColumn)
		{
			switch(aColumn)
			{
			case 0: return getTitle(aNode);
			case 1: return getValue(aNode);
			default: return "What? "+aColumn;
			}
		}

		private String getTitle(Object aNode)
		{
			if (aNode == itsData) return "root";
			else if (aNode instanceof KeyMonitorData)
			{
				KeyMonitorData theData = (KeyMonitorData) aNode;
				return theData.key;
			}
			else if (aNode instanceof IndividualProbeValue)
			{
				IndividualProbeValue theValue = (IndividualProbeValue) aNode;
				return theValue.instanceName;
			}
			else return "Unknown: "+aNode;
		}
		
		private String getValue(Object aNode)
		{
			if (aNode == itsData) return "root";
			else if (aNode instanceof KeyMonitorData)
			{
				KeyMonitorData theData = (KeyMonitorData) aNode;
				return ""+theData.aggregateValue;
			}
			else if (aNode instanceof IndividualProbeValue)
			{
				IndividualProbeValue theValue = (IndividualProbeValue) aNode;
				return ""+theValue.value;
			}
			else return "Unknown: "+aNode;
		}
		
		public Object getChild(Object aParent, int aIndex)
		{
			if (aParent == itsData) return itsData.get(aIndex);
			else if (aParent instanceof KeyMonitorData)
			{
				KeyMonitorData theData = (KeyMonitorData) aParent;
				return theData.individualValues.get(aIndex);
			}
			else throw new NoSuchElementException();
		}

		public int getChildCount(Object aParent)
		{
			if (aParent == itsData) return itsData.size();
			else if (aParent instanceof KeyMonitorData)
			{
				KeyMonitorData theData = (KeyMonitorData) aParent;
				return theData.individualValues.size();
			}
			else return 0;
		}
		
	}
}