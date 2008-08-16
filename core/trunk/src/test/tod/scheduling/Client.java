/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.scheduling;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tod.Util;
import tod.gui.GUIUtils;
import tod.gui.kit.AsyncPanel;
import tod.tools.monitoring.MonitoringClient.MonitorId;
import tod.tools.scheduling.JobScheduler;
import tod.tools.scheduling.JobSchedulerMonitor;
import tod.tools.scheduling.IJobScheduler.JobPriority;

public class Client extends JPanel
{
	private RIServer itsServer;
	private JComponent itsResultContainer;
	private JobScheduler itsJobScheduler = new JobScheduler();
	
	public Client(RIServer aServer)
	{
		itsServer = aServer;
		createUI();
	}

	private void createUI()
	{
		setLayout(new BorderLayout());
		
		JPanel theNorthPanel = new JPanel(GUIUtils.createSequenceLayout());
		add(theNorthPanel, BorderLayout.NORTH);

		JButton theTask1Button = new JButton("Task!");
		theTask1Button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				addTask(JobPriority.EXPLICIT);
			}
		});
		
		theNorthPanel.add(theTask1Button);
		
		JButton theTask2Button = new JButton("(task)");
		theTask2Button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				addTask(JobPriority.AUTO);
			}
		});
		
		theNorthPanel.add(theTask2Button);
		
		JButton theCancelButton = new JButton("Cancel");
		theCancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				itsJobScheduler.cancelAll();
			}
		});
		
		theNorthPanel.add(theCancelButton);
		
		theNorthPanel.add(new JobSchedulerMonitor(itsJobScheduler));
		
		itsResultContainer = new JPanel(GUIUtils.createStackLayout());
		add(itsResultContainer, BorderLayout.CENTER);
	}
	
	private void addTask(final JobPriority aJobPriority)
	{
		itsResultContainer.add(new AsyncPanel(itsJobScheduler, aJobPriority)
		{
			private int itsResult;
			
			@Override
			protected void runJob()
			{
				try
				{
					itsResult = itsServer.doTask(MonitorId.get(), (int) (Math.random()*5)+2);
				}
				catch (RemoteException e)
				{
					throw new RuntimeException(e);
				}
			}
			
			@Override
			protected void createUI()
			{
				setLayout(GUIUtils.createSequenceLayout());
				add(GUIUtils.createLabel(aJobPriority+"..."));
			}

			@Override
			protected void updateSuccess()
			{
				add(new JLabel("Done: "+itsResult));
			}
			
			@Override
			protected void updateCancelled()
			{
				add(new JLabel("Cancelled!"));
			}
			
			@Override
			protected void updateFailure()
			{
				add(new JLabel("Failure!"));
			}
		});
		
		itsResultContainer.revalidate();
		itsResultContainer.repaint();
	}
	
	public static void main(String[] args) throws Exception
	{
		Registry theRegistry = LocateRegistry.getRegistry("localhost", Util.TOD_REGISTRY_PORT);
		RIServer theServer = (RIServer) theRegistry.lookup("server");
		
		JFrame theFrame = new JFrame("Scheduling test");
		theFrame.setContentPane(new Client(theServer));
		theFrame.pack();
		theFrame.setVisible(true);
	}
}
