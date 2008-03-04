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
package tod.gui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import tod.Util;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMemberInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.BrowserData;
import tod.gui.IGUIManager;
import tod.gui.TimeScale;
import tod.gui.eventsequences.FieldSequenceView;
import tod.gui.eventsequences.MembersDock;
import tod.gui.eventsequences.MethodSequenceView;
import tod.gui.kit.SeedLinkLabel;
import tod.gui.seed.FilterSeed;
import tod.gui.seed.ObjectInspectorSeed;
import zz.utils.list.ICollection;
import zz.utils.list.ICollectionListener;
import zz.utils.properties.ISetProperty;
import zz.utils.properties.PropertyUtils;
import zz.utils.ui.GridStackLayout;

/**
 * This panel presents the history of an object's field writes and/or method calls.
 * @author gpothier
 */
public class ObjectInspectorView extends LogView
{
	private ObjectInspectorSeed itsSeed;
	private IObjectInspector itsInspector;
	private TimeScale itsTimeScale;
	
	private MembersDock itsDock;
	
	private JPanel itsTitlePanel;
	
	private MemberSelector itsMemberSelector;
	
	
	public ObjectInspectorView(IGUIManager aGUIManager, ILogBrowser aLog, ObjectInspectorSeed aInspectorSeed)
	{
		super(aGUIManager, aLog);
		itsSeed = aInspectorSeed;
	}
	
	@Override
	public void init()
	{
		createUI();
		setObject(itsSeed.getInspectedObject());
	}
	
	private void createUI()
	{
		setLayout(new BorderLayout());
		
		JPanel theNorthPanel = new JPanel(new GridStackLayout(1));
	
		// Setup title
		itsTitlePanel = new JPanel();
		theNorthPanel.add (itsTitlePanel);
		updateTitlePanel();
		
		// Setup time scale
		itsTimeScale = new TimeScale();
		itsTimeScale.pStart().set(getLogBrowser().getFirstTimestamp());
		itsTimeScale.pEnd().set(getLogBrowser().getLastTimestamp());
		connect (itsSeed.pSelectionStart(), itsTimeScale.pSelectionStart(), true);
		connect (itsSeed.pSelectionEnd(), itsTimeScale.pSelectionEnd(), true);
		connect (itsSeed.pCurrentPosition(), itsTimeScale.pCurrentPosition(), true);
		theNorthPanel.add (itsTimeScale);
		
		add (theNorthPanel, BorderLayout.NORTH);
		
		// Setup center view
		itsDock = new MembersDock(getGUIManager());
		add (itsDock, BorderLayout.CENTER);
		
		// These are hard connectors, as we are the owner of the timescale & dock
		PropertyUtils.connect(itsTimeScale.pSelectionStart(), itsDock.pStart(), true);
		PropertyUtils.connect(itsTimeScale.pSelectionEnd(), itsDock.pEnd(), true);
		
		// Setup member selector
		itsMemberSelector = new MemberSelector();
		itsMemberSelector.pSelectedMembers().addHardListener(new ICollectionListener<IMemberInfo>()
				{
					public void elementAdded(ICollection<IMemberInfo> aCollection, IMemberInfo aElement)
					{
						itsDock.addMember(itsInspector, aElement);
					}

					public void elementRemoved(ICollection<IMemberInfo> aCollection, IMemberInfo aElement)
					{
						itsDock.removeMember(aElement);
					}
				});
		connectSet(itsSeed.pSelectedMembers(), itsMemberSelector.pSelectedMembers(), true);
		add (itsMemberSelector, BorderLayout.WEST);
		
	}

	private void setObject (ObjectId aObjectId)
	{
		itsInspector = aObjectId != null ? getLogBrowser().createObjectInspector(aObjectId) : null;
		itsTimeScale.pEventBrowsers().clear();
		
		if (itsInspector != null)
		{
			// Update label
			ITypeInfo theType = itsInspector.getType();

			// Update timescale's browsers
			for (IMemberInfo theMember : itsInspector.getMembers())
			{
				IEventBrowser theBrowser = itsInspector.getBrowser(theMember);
				Color theColor;
				if (theMember instanceof IFieldInfo) theColor = FieldSequenceView.FIELD_COLOR;
				else if (theMember instanceof IBehaviorInfo) theColor = MethodSequenceView.METHOD_COLOR;
				else throw new RuntimeException("Not handled: "+theMember); 
				itsTimeScale.pEventBrowsers().add (new BrowserData(theBrowser, theColor));
			}
		}
		
		itsMemberSelector.pInspector().set(itsInspector);
//		itsMemberSelector.selectFields();

		updateTitlePanel();
	}
	
	private void updateTitlePanel()
	{
		itsTitlePanel.removeAll();
		itsTitlePanel.setLayout(new FlowLayout());
		
		String theTitle;
		ObjectId theObject;
		
		if (itsInspector != null)
		{
			// Update label
			ITypeInfo theType = itsInspector.getType();
			theObject = itsInspector.getObject();
			theTitle = String.format(
					"Object inspector for: %s (%s)",
					theObject,
					Util.getPrettyName(theType.getName()));
		}
		else 
		{
			theTitle = "Object inspector";
			theObject = null;
		}
		
		JLabel theTitleLabel = new JLabel(theTitle);
		itsTitlePanel.add(theTitleLabel);
		
		if (theObject != null)
		{
			FilterSeed theShowEventsSeed = new FilterSeed(
					getGUIManager(), 
					getLogBrowser(), 
					"All events on object: "+theObject.getId(),
					getLogBrowser().createTargetFilter(theObject));
			
			itsTitlePanel.add (new SeedLinkLabel(
					"Show all events", 
					theShowEventsSeed));
			
			FilterSeed theShowHistorySeed = new FilterSeed(
					getGUIManager(),
					getLogBrowser(),
					"History of object: "+theObject.getId(),
					getLogBrowser().createObjectFilter(theObject));
			
			itsTitlePanel.add (new SeedLinkLabel(
					"Show history", 
					theShowHistorySeed));
						
		}
			
		itsTitlePanel.revalidate();
		itsTitlePanel.repaint();
	}
	
	/**
	 * The property that contains the members whose history is displayed.
	 */
	public ISetProperty<IMemberInfo> pMembers()
	{
		return itsMemberSelector.pSelectedMembers();
	}
	
}
