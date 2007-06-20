/*
 * Created on Jun 19, 2007
 */
package tod.plugin.views;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;

import javax.swing.JComponent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import zz.utils.ui.StackLayout;

public abstract class AbstractAWTView extends ViewPart 
{
	private Frame itsFrame;
	
	@Override
	public void createPartControl(Composite parent) 
	{
		final Composite theEmbedded = new Composite(parent, SWT.EMBEDDED | SWT.CENTER);
		parent.setLayout(new FillLayout());
		
		itsFrame = SWT_AWT.new_Frame(theEmbedded);
		Panel theRootPanel = new Panel(new BorderLayout());
		itsFrame.setLayout(new StackLayout());
		itsFrame.add(theRootPanel);

		theEmbedded.addControlListener(new ControlListener()
		{
			public void controlMoved(ControlEvent aE)
			{
			}

			public void controlResized(ControlEvent aE)
			{
				itsFrame.setSize(theEmbedded.getSize().x, theEmbedded.getSize().y);
			}
			
		});
		
		theRootPanel.add(createComponent());
	}
	
	protected abstract JComponent createComponent();
	
	@Override
	public void setFocus()
	{
	}


}