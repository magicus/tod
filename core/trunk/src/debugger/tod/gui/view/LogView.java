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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tod.core.config.TODConfig;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.ObjectId;
import tod.gui.FontConfig;
import tod.gui.GUIUtils;
import tod.gui.IGUIManager;
import tod.gui.JobProcessor;
import tod.gui.formatter.EventFormatter;
import tod.gui.formatter.ObjectFormatter;
import tod.gui.kit.BusOwnerPanel;
import tod.gui.kit.IOptionsOwner;
import tod.gui.kit.Options;
import tod.gui.kit.SeedLinkLabel;
import tod.gui.seed.LogViewSeed;
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
implements IOptionsOwner
{
	private final ILogBrowser itsLog;
	private final IGUIManager itsGUIManager;
	
	private List<PropertyUtils.Connector> itsConnectors; 

	private ObjectFormatter itsObjectFormatter;
	private EventFormatter itsEventFormatter;
	
	private Options itsOptions;
	
	public LogView(IGUIManager aGUIManager, ILogBrowser aLog)
	{
		itsGUIManager = aGUIManager;
		itsOptions = new Options(itsGUIManager.getSettings(), getOptionsName(), itsGUIManager.getSettings().getOptions());
		initOptions(itsOptions);
		itsLog = aLog;
		itsObjectFormatter = new ObjectFormatter(itsLog);
		itsEventFormatter = new EventFormatter(itsLog);
	}
	
	/**
	 * Returns the name under which the options of this view are stored.
	 */
	protected String getOptionsName()
	{
		return getClass().getSimpleName();
	}

	/**
	 * Subclasses can override this method to add options to the options set.
	 */
	protected void initOptions(Options aOptions)
	{
	}
	
	public Options getOptions()
	{
		return itsOptions;
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
	
	@Override
	public boolean isValidateRoot()
	{
		return true;
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
	
	public TODConfig getConfig()
	{
		return getGUIManager().getSession().getConfig();
	}
	
	/**
	 * Helper method to obtain the default job processor for this view.
	 */
	public JobProcessor getJobProcessor()
	{
		return getGUIManager().getJobProcessor();
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
