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
package tod.gui.view.dyncross;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.SourceRange;
import tod.core.database.structure.IStructureDatabase.AspectInfo;
import tod.gui.IGUIManager;
import tod.gui.kit.SavedSplitPane;
import tod.gui.view.LogView;
import zz.utils.SimpleListModel;
import zz.utils.Utils;
import zz.utils.ui.StackLayout;

/**
 * This view displays the dynamic crosscutting of aspects
 * (aka. aspect murals).
 * @author gpothier
 */
public class DynamicCrosscuttingView extends LogView
{
	public DynamicCrosscuttingView(IGUIManager aGUIManager, ILogBrowser aLog)
	{
		super(aGUIManager, aLog);
	}

	@Override
	public void init()
	{
		super.init();
		
		IStructureDatabase theStructureDatabase = getLogBrowser().getStructureDatabase();
		Map<String, AspectInfo> theMap = theStructureDatabase.getAspectInfoMap();
		
		// Create aspects list
		List<AspectInfo> theAspectsList = new ArrayList<AspectInfo>();
		Utils.fillCollection(theAspectsList, theMap.values());
		
		// Create advices list
		List<SourceRange> theAdvicesList = new ArrayList<SourceRange>();
		for (AspectInfo theAspectInfo : theAspectsList)
		{
			for (int theAdviceSourceId : theAspectInfo.getAdviceIds())
			{
				theAdvicesList.add(theStructureDatabase.getAdviceSource(theAdviceSourceId));
			}
		}
		
		MouseListener theAddListener = new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent aE)
			{
				if (aE.getClickCount() == 2)
				{
					JList theList = (JList) aE.getSource();
					addHighlight((Highlight) theList.getSelectedValue());
				}
			}
		};
		
		JList theAspectsJList = new JList(new SimpleListModel(theAspectsList));
		JList theAdvicesJList = new JList(new SimpleListModel(theAdvicesList));
		
		theAspectsJList.addMouseListener(theAddListener);
		theAdvicesJList.addMouseListener(theAddListener);
		
		JTabbedPane theTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		theTabbedPane.addTab("Aspects", new JScrollPane(theAspectsJList));
		theTabbedPane.addTab("Advices", new JScrollPane(theAdvicesJList));
		
		JSplitPane theSplitPane = new SavedSplitPane(getGUIManager(), "dynamicCrosscuttingView.splitterPos");
		
		theSplitPane.setLeftComponent(theTabbedPane);
		
		setLayout(new StackLayout());
		add(theSplitPane);
		
	}
	
	private void addHighlight(Highlight aHighlight)
	{
		
	}
	
	private static abstract class Highlight
	{
		
	}
	
	private static class AspectHighlight extends Highlight
	{
		
	}
	
	private static class AdviceHighlight extends Highlight
	{
		
	}
}
