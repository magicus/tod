/*
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
