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
package tod.gui.kit;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import tod.gui.GUIUtils;
import tod.gui.JobProcessor;

/**
 * A component whose content is retrieved asynchronously
 * @author gpothier
 */
public abstract class AsyncPanel extends JPanel
{
	public AsyncPanel(JobProcessor aJobProcessor)
	{
		super(GUIUtils.createSequenceLayout());
		setOpaque(false);
		add(GUIUtils.createLabel("..."));
		aJobProcessor.submit(new JobProcessor.Job<Object>()
				{
					@Override
					public Object run()
					{
						runJob();
						postUpdate();
						return null;
					}
				});
	}
	
	private void postUpdate()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				removeAll();
				update();
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
	
	/**
	 * Updates the UI once the long-running job of {@link #runJob()} is
	 * executed.
	 * This method is executed in the Swing thread. 
	 * It is not necessary to call {@link #revalidate()} nor {@link #repaint()}.
	 */
	protected abstract void update();
}
