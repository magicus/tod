/*
 * Created on Sep 22, 2005
 */
package tod.gui.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tod.core.model.structure.IThreadInfo;
import tod.core.model.trace.IEventTrace;
import tod.gui.IGUIManager;
import tod.gui.formatter.LocationFormatter;
import tod.gui.seed.CFlowSeed;
import tod.gui.seed.ThreadsSeed;
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
		List<IThreadInfo> theThreads = new ArrayList<IThreadInfo>();
		Utils.fillCollection(theThreads, getEventTrace().getLocationTrace().getThreads());
		
		final JComboBox theThreadsCombo = new JComboBox(new SimpleComboBoxModel(theThreads));
		theThreadsCombo.setRenderer(new FormattedRenderer(LocationFormatter.getInstance()));
		theThreadsCombo.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent aE)
					{
						showThread((IThreadInfo) theThreadsCombo.getSelectedItem());
					}
				}
		);
		
		JPanel theTopPanel = new JPanel();
		theTopPanel.add(new JLabel("Select thread"));
		theTopPanel.add(theThreadsCombo);
		
		setLayout(new BorderLayout());
		add (theTopPanel, BorderLayout.NORTH);
	}
	
	private void showThread(IThreadInfo aThread)
	{
		getGUIManager().openSeed(new CFlowSeed(getGUIManager(), getEventTrace(), aThread), false);
	}

}
