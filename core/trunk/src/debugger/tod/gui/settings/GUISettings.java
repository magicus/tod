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
package tod.gui.settings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import tod.gui.IGUIManager;
import tod.gui.MinerUI;
import tod.gui.formatter.CustomFormatterRegistry;
import tod.gui.kit.IOptionsOwner;
import tod.gui.kit.Options;
import zz.utils.Base64;

/**
 * Manages the persistence of TOD GUI settings.
 * @author gpothier
 */
public class GUISettings
implements IOptionsOwner
{
	private static final String PROPERTY_REGISTRY = "todGUI.customFormatterRegistry";

	private final IGUIManager itsGUIManager;
	private final Properties itsProperties = new Properties();
	private final Options itsRootOptions = new Options(this, "root", null);
	private CustomFormatterRegistry itsCustomFormatterRegistry;
	

	public GUISettings(IGUIManager aManager)
	{
		itsGUIManager = aManager;
		
		loadProperties();

		itsCustomFormatterRegistry = (CustomFormatterRegistry) getObjectProperty(PROPERTY_REGISTRY, null);
		if (itsCustomFormatterRegistry == null) itsCustomFormatterRegistry = new CustomFormatterRegistry();
	}

	public void save()
	{
		saveFormatters();
		saveProperties();
	}
	
	/**
	 * Returns the registry of custom object formatters.
	 */
	public CustomFormatterRegistry getCustomFormatterRegistry()
	{
		return itsCustomFormatterRegistry;
	}

	private void saveFormatters()
	{
		setObjectProperty(PROPERTY_REGISTRY, itsCustomFormatterRegistry);
	}


	
	/**
	 * Stores a persistent property, which can be retrieved
	 * with {@link #getProperty(String)}.
	 */
	public void setProperty(String aKey, String aValue)
	{
		itsProperties.setProperty(aKey, aValue);
	}
	
	/**
	 * Retrieves a persistent property previously stored with 
	 * {@link #setProperty(String, String)}.
	 * @see MinerUI#getIntProperty(IGUIManager, String, int)
	 * @see MinerUI#getBooleanProperty(IGUIManager, String, boolean)
	 */
	public String getProperty(String aKey)
	{
		return itsProperties.getProperty(aKey);
	}
	
	/**
	 * Returns the global GUI options of this GUI manager.
	 */
	public Options getOptions()
	{
		return itsRootOptions;
	}
	
	
	
	/**
	 * Loads stored properties and place them in the given properties map.
	 */
	public void loadProperties()
	{
		try
		{
			File theFile = new File("tod-properties.txt");
			if (theFile.exists()) itsProperties.load(new FileInputStream(theFile));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the given properties map.
	 */
	private void saveProperties()
	{
		try
		{
			File theFile = new File("tod-properties.txt");
			itsProperties.store(new FileOutputStream(theFile), "TOD configuration");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * Utility method for {@link #getProperty(String)}
	 */
	public boolean getBooleanProperty (String aPropertyName, boolean aDefault)
	{
		String theString = getProperty(aPropertyName);
		return theString != null ? Boolean.parseBoolean(theString) : aDefault;
	}
	
	/**
	 * Utility method for {@link #getProperty(String)}
	 */
	public int getIntProperty (String aPropertyName, int aDefault)
	{
		String theString = getProperty(aPropertyName);
		return theString != null ? Integer.parseInt(theString) : aDefault;
	}
	
	/**
	 * Utility method for {@link #getProperty(String)}
	 */
	public String getStringProperty (String aPropertyName, String aDefault)
	{
		String theString = getProperty(aPropertyName);
		return theString != null ? theString : aDefault;
	}
	
	/**
	 * Retrieves a serialized object.
	 */
	public Object getObjectProperty(String aPropertyName, Object aDefault)
	{
		try
		{
			String theString = getProperty(aPropertyName);
			if (theString == null) return aDefault;
			
			byte[] theByteArray = Base64.decode(theString);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(theByteArray));
			return ois.readObject();
		}
		catch (Exception e)
		{
			// avoid throwing new exception in case of new object format
			//throw new RuntimeException(e);
			System.err.println("---- Problem while loading GUI properties "+aPropertyName);
			//e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Saves a serialized object into a property.
	 */
	public void setObjectProperty(String aPropertyName, Object aValue)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(aValue);
			oos.flush();
			
			byte[] theByteArray = baos.toByteArray();
			String theString = Base64.encodeBytes(theByteArray);
			
			setProperty(aPropertyName, theString);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	


}
