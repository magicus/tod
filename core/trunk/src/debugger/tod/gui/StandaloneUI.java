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
package tod.gui;

import java.net.URI;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import tod.core.config.TODConfig;
import tod.core.database.structure.SourceRange;
import tod.core.session.ISession;
import tod.core.session.SessionTypeManager;
import zz.utils.ui.StackLayout;

public class StandaloneUI extends JPanel
{
	private ISession itsSession;
	private MyTraceView itsTraceView;

	public StandaloneUI(URI aUri)
	{
		TODConfig theConfig = new TODConfig();
		String theScheme = aUri != null ? aUri.getScheme() : null;
		itsSession = SessionTypeManager.getInstance().createSession(theScheme, aUri, theConfig);
		createUI();
	}

	private void createUI()
	{
		setLayout(new StackLayout());
		JTabbedPane theTabbedPane = new JTabbedPane();
		add (theTabbedPane);
		
		itsTraceView = new MyTraceView();
		itsTraceView.setSession(itsSession);
		theTabbedPane.addTab("Trace view", itsTraceView);
		
		JComponent theConsole = itsSession.createConsole();
		if (theConsole != null) theTabbedPane.addTab("Console", theConsole);
	}

	private class MyTraceView extends MinerUI
	{
		public void gotoSource(SourceRange aSourceRange)
		{
		}
	}
	
	public static void main(String[] args)
	{
//		ReflexRiver.setup();
		URI theUri = args.length > 0 ? URI.create(args[0]) : null;
		
		JFrame theFrame = new JFrame("TOD");
		theFrame.setContentPane(new StandaloneUI(theUri));
		theFrame.pack();
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.setVisible(true);
	}
}
