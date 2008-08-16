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
package tod.gui.kit;

import javax.swing.SwingUtilities;

import tod.gui.GUIUtils;
import tod.tools.monitoring.ITaskMonitor;
import tod.tools.monitoring.TaskMonitor.TaskCancelledException;
import tod.tools.scheduling.IJobScheduler;
import tod.tools.scheduling.IJobScheduler.JobPriority;
import zz.utils.notification.IEvent;
import zz.utils.notification.IEventListener;
import zz.utils.ui.MousePanel;

/**
 * A component whose content is retrieved asynchronously
 * @author gpothier
 */
public abstract class AsyncPanel extends MousePanel
implements IEventListener<Void>
{
	private final IJobScheduler itsJobScheduler;
	private final JobPriority itsJobPriority;
	private ITaskMonitor itsMonitor;
	private boolean itsCancelled;

	public AsyncPanel(IJobScheduler aJobScheduler, JobPriority aJobPriority)
	{
		itsJobScheduler = aJobScheduler;
		itsJobPriority = aJobPriority;
		setOpaque(false);
		createUI();
	}
	
	protected IJobScheduler getJobScheduler()
	{
		return itsJobScheduler;
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		if (itsMonitor == null)
		{
			itsMonitor = itsJobScheduler.submit(itsJobPriority, new Runnable()
				{
					public void run()
					{
						try
						{
							runJob();
							if (! itsCancelled) postUpdate(Outcome.SUCCESS);
						}
						catch (TaskCancelledException e)
						{
							postUpdate(Outcome.CANCELLED);
						}
						catch (Throwable e)
						{
							System.err.println("Error executing job:");
							e.printStackTrace();
							postUpdate(Outcome.FAILURE);
						}
					}
				});
			itsMonitor.eCancelled().addListener(this);
		}
	}
	
	public void fired(IEvent< ? extends Void> aEvent, Void aData)
	{
		itsCancelled = true;
		postUpdate(Outcome.CANCELLED);
	}

	public void cancelJob()
	{
		if (itsMonitor != null) itsMonitor.cancel();
	}
	
	@Override
	public void removeNotify()
	{
		cancelJob();
		super.removeNotify();
	}
	
	/**
	 * Creates the initial UI of this panel.
	 * By default, displays "...".
	 */
	protected void createUI()
	{
		setLayout(GUIUtils.createSequenceLayout());
		add(GUIUtils.createLabel("..."));
	}

	/**
	 * Updates the UI once the long-running job of {@link #runJob()} is
	 * finished.
	 * This method is executed in the Swing thread. 
	 * It is not necessary to call {@link #revalidate()} nor {@link #repaint()}.
	 * @param aOutcome Indicates if the job run successfully or not.
	 */
	protected void update(Outcome aOutcome)
	{
		switch(aOutcome)
		{
		case SUCCESS:
			updateSuccess();
			break;
			
		case CANCELLED:
			updateCancelled();
			break;
			
		case FAILURE:
			updateFailure();
			break;
			
		default:
			throw new RuntimeException("Not handled: "+aOutcome);
		}
	}
	
	/**
	 * Called by default by {@link #update(Outcome)} if the job finished successfully.
	 * This method is executed in the Swing thread. 
	 * It is not necessary to call {@link #revalidate()} nor {@link #repaint()}.
	 */
	protected abstract void updateSuccess();
	
	protected void updateCancelled()
	{
	}
	
	protected void updateFailure()
	{
	}
	
	

	private void postUpdate(final Outcome aOutcome)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				removeAll();
				update(aOutcome);
				revalidate();
				repaint();
			}
		});
	}
	
	/**
	 * This method should perform a long-running task. It will be 
	 * executed by the {@link JobProcessor}.
	 * Once this method terminates the {@link #update()} method will
	 * be scheduled for execution in the Swing thread.
	 */
	protected abstract void runJob();

	public enum Outcome
	{
		SUCCESS, CANCELLED, FAILURE;
	}
	
}
