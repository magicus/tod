/*
 * Created on Sep 22, 2005
 */
package reflex.lib.logging.miner.gui.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.formatter.LocationFormatter;
import reflex.lib.logging.miner.gui.seed.CFlowSeed;
import reflex.lib.logging.miner.gui.seed.ThreadsSeed;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.trace.IEventTrace;
import zz.utils.SimpleComboBoxModel;
import zz.utils.Utils;
import zz.utils.ui.FormattedRenderer;

/**
 * A view that lets the user select a thread and displays all the events 
 * of this thread.
 * @author gpothier
 */
public class ThreadsView extends LogView
{
	private ThreadsSeed itsSeed;
	
	public ThreadsView(IGUIManager aGUIManager, IEventTrace aLog, ThreadsSeed aSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aSeed;
		
		createUI();
	}
	
	private void createUI()
	{
		List<ThreadInfo> theThreads = new ArrayList<ThreadInfo>();
		Utils.fillCollection(theThreads, getEventTrace().getLocationTrace().getThreads());
		
		final JComboBox theThreadsCombo = new JComboBox(new SimpleComboBoxModel(theThreads));
		theThreadsCombo.setRenderer(new FormattedRenderer(LocationFormatter.getInstance()));
		theThreadsCombo.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent aE)
					{
						showThread((ThreadInfo) theThreadsCombo.getSelectedItem());
					}
				}
		);
		
		JPanel theTopPanel = new JPanel();
		theTopPanel.add(new JLabel("Select thread"));
		theTopPanel.add(theThreadsCombo);
		
		setLayout(new BorderLayout());
		add (theTopPanel, BorderLayout.NORTH);
	}
	
	private void showThread(ThreadInfo aThread)
	{
		getGUIManager().openSeed(new CFlowSeed(getGUIManager(), getEventTrace(), aThread), false);
	}

}
