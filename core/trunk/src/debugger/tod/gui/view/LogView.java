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
package tod.gui.view;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.ObjectId;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
import tod.gui.IGUIManager;
import tod.gui.formatter.EventFormatter;
import tod.gui.formatter.ObjectFormatter;
import tod.gui.kit.BusOwnerPanel;
import tod.gui.kit.SeedLinkLabel;
import tod.gui.seed.LogViewSeed;
import tod.gui.seed.LogViewSeedFactory;
import tod.gui.seed.ObjectInspectorSeed;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.ISetProperty;
import zz.utils.properties.PropertyUtils;
import zz.utils.ui.ZLabel;

/**
 * Base class for views.
 * @see tod.gui.seed.LogViewSeed
 * @author gpothier
 */
public abstract class LogView extends BusOwnerPanel
{
	private final ILogBrowser itsLog;
	private final IGUIManager itsGUIManager;
	
	private List<PropertyUtils.Connector> itsConnectors; 

	private ObjectFormatter itsObjectFormatter;
	private EventFormatter itsEventFormatter;
	
	
	public LogView(IGUIManager aGUIManager, ILogBrowser aLog)
	{
		itsGUIManager = aGUIManager;
		itsLog = aLog;
		itsObjectFormatter = new ObjectFormatter(itsLog);
		itsEventFormatter = new EventFormatter(itsLog);
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		if (itsConnectors != null) 
			for (PropertyUtils.Connector theConnector : itsConnectors) theConnector.connect();
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		if (itsConnectors != null) 
			for (PropertyUtils.Connector theConnector : itsConnectors) theConnector.disconnect();
	}
	
	/**
	 * Prepares a connection between two properties. 
	 * The connection is effective only once this component is shown. 
	 * The connection is removed when this component is hidden.
	 * @see PropertyUtils.Connector
	 */
	protected <T> void connect (IRWProperty<T> aSource, IRWProperty<T> aTarget, boolean aSymmetric)
	{
		if (itsConnectors == null) itsConnectors = new ArrayList<PropertyUtils.Connector>(); 
		PropertyUtils.SimpleValueConnector<T> theConnector = new PropertyUtils.SimpleValueConnector<T>(aSource, aTarget, aSymmetric, true);
		itsConnectors.add (theConnector);
	}
	
	/**
	 * Prepares a connection between two set properties. 
	 * The connection is effective only once this component is shown. 
	 * The connection is removed when this component is hidden.
	 * @see PropertyUtils.Connector
	 */
	protected <T> void connectSet (ISetProperty<T> aSource, ISetProperty<T> aTarget, boolean aSymmetric)
	{
		if (itsConnectors == null) itsConnectors = new ArrayList<PropertyUtils.Connector>(); 
		PropertyUtils.SetConnector<T> theConnector = new PropertyUtils.SetConnector<T>(aSource, aTarget, aSymmetric, true);
		itsConnectors.add (theConnector);
	}
	
	public ILogBrowser getLogBrowser()
	{
		return itsLog;
	}
	
	public IGUIManager getGUIManager()
	{
		return itsGUIManager;
	}
	
	/**
	 * Returns an event formatter that can be used in the context
	 * of this view.
	 */
	protected EventFormatter getEventFormatter()
	{
		return itsEventFormatter;
	}


	/**
	 * Returns an object formatter that can be used in the context 
	 * of this view.
	 */
	protected ObjectFormatter getObjectFormatter()
	{
		return itsObjectFormatter;
	}

	/**
	 * This method is called after the object is instantiated.
	 * It should be used to create the user interface.
	 */
	public void init()
	{
	}
	
	/**
	 * Creates a standard panel that shows a title and a link label.
	 */
	protected JComponent createTitledLink (String aTitle, String aLinkName, LogViewSeed aSeed)
	{
		return createTitledPanel(aTitle, new SeedLinkLabel(aLinkName, aSeed));
	}
	
	/**
	 * Creates a standard panel that shows a title and another component.
	 */
	protected JComponent createTitledPanel (String aTitle, JComponent aComponent)
	{
		JPanel thePanel = new JPanel(GUIUtils.createSequenceLayout());
		thePanel.add (GUIUtils.createLabel(aTitle));
		thePanel.add (aComponent);
		
		return thePanel;
	}
	
	
	
	/**
	 * Creates a title label, with a big font.
	 */
	protected JComponent createTitleLabel (String aTitle)
	{
		return ZLabel.create(aTitle, FontConfig.STD_HEADER_FONT, Color.BLACK);
	}
	
	/**
	 * Creates a link that jumps to an inspector for the specified object.
	 */
	protected JComponent createInspectorLink (Object aObject)
	{
		if (aObject instanceof ObjectId)
		{
			ObjectId theObjectId = (ObjectId) aObject;
			
			ObjectInspectorSeed theSeed = new ObjectInspectorSeed(
					getGUIManager(), 
					getLogBrowser(), 
					theObjectId);
			
			return new SeedLinkLabel (
					itsObjectFormatter.getPlainText(aObject), 
					theSeed);
		}
		else return new JLabel (itsObjectFormatter.getPlainText(aObject));
	}

}
