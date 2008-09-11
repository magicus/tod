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
package tod.tools.scheduling;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import zz.utils.Utils;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.ui.UIUtils;

/**
 * A components that displays the queue size of a 
 * {@link JobScheduler}.
 * @author gpothier
 */
public class JobSchedulerMonitor extends JPanel
implements IPropertyListener<Integer>
{
	private final JobScheduler itsJobScheduler;
	
	private final MaxUpdaterThread itsMaxUpdaterThread;
	private int itsCurrentMax;
	private int itsCurrentVal;
	
	private JProgressBar itsProgressBar;
	
	public JobSchedulerMonitor(JobScheduler aJobScheduler)
	{
		itsJobScheduler = aJobScheduler;
		createUI();
		itsMaxUpdaterThread = new MaxUpdaterThread();
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		itsJobScheduler.pQueueSize().addHardListener(this);
	}
	
	@Override
	public void removeNotify()
	{
		itsJobScheduler.pQueueSize().removeListener(this);
		super.removeNotify();
	}

	private void createUI()
	{
		setLayout(new BorderLayout());
		itsProgressBar = new JProgressBar();
		add(itsProgressBar, BorderLayout.CENTER);
		
		JButton theCancelAllButton = new JButton("(X)");
		theCancelAllButton.setToolTipText("Cancel all pending jobs");
		theCancelAllButton.setMargin(UIUtils.NULL_INSETS);
		theCancelAllButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent aE)
			{
				itsJobScheduler.cancelAll();
			}
		});
		add(theCancelAllButton, BorderLayout.WEST);
		
		update();
	}
	
	private void update()
	{
		itsCurrentVal = itsJobScheduler.pQueueSize().get();
		if (itsCurrentVal > itsCurrentMax) itsCurrentMax = itsCurrentVal;
		itsProgressBar.setMaximum(itsCurrentMax);
		itsProgressBar.setValue(itsCurrentVal);
	}

	public void propertyChanged(IProperty<Integer> aProperty, Integer aOldValue, Integer aNewValue)
	{
		update();
	}

	public void propertyValueChanged(IProperty<Integer> aProperty)
	{
	}

	/**
	 * This thread periodically resets the max.
	 * @author gpothier
	 */
	private class MaxUpdaterThread extends Thread
	{
		public MaxUpdaterThread()
		{
			super("MaxUpdaterThread");
			setDaemon(true);
			start();
		}
		
		@Override
		public void run()
		{
			while(true)
			{
				if (itsJobScheduler.pQueueSize().get() == 0)
				{
					itsCurrentMax = 0;
					update();
				}
				
				Utils.sleep(300);
			}
		}
	}
	
}
