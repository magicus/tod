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

import java.awt.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import tod.gui.IGUIManager;
import tod.gui.kit.messages.GetOptionManager;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * Manages a set of options. An option manager has a set of options each defined by
 * an {@link OptionDef} object; for each option it associates a value for specific targets.
 * For instance on option "show package names" could have values for targets 
 * "call stack view" and "event list".
 * 
 * @author gpothier
 */
public class OptionManager
{
	private IGUIManager itsGUIManager;
	private OptionManager itsParent;
	
	private Set<OptionDef> itsOptions = new HashSet<OptionDef>();
	private Map<OptionDef, Map<String, IRWProperty>> itsValues = 
		new HashMap<OptionDef, Map<String, IRWProperty>>();
	
	public OptionManager(IGUIManager aManager, OptionManager aParent)
	{
		itsGUIManager = aManager;
		itsParent = aParent;
	}

	/**
	 * Creates a component that permits to inspect and modify current options.
	 */
	public JComponent createComponent()
	{
		return null;
	}
	
	private <T> boolean addOption0(OptionDef<T> aDef, String aTarget, T aDefault)
	{
		if (itsOptions.contains(aDef))
		{
			Map<String, IRWProperty> theValues = itsValues.get(aDef);
			if (theValues == null)
			{
				theValues = new HashMap<String, IRWProperty>();
				itsValues.put(aDef, theValues);
			}
			
			IRWProperty<T> theProperty = (IRWProperty<T>) theValues.get(aTarget);
			if (theProperty == null) 
			{
				theProperty = new SimpleRWProperty<T>(null, aDefault);
				theValues.put(aTarget, theProperty);
				itsGUIManager.setProperty(aDef.getName()+"/"+aTarget, aDef.marshall(aDefault));
			}
			
			return true;
		}
		else return false;
	}
	
	private <T> boolean removeOption0(OptionDef<T> aDef, String aTarget)
	{
		if (itsOptions.contains(aDef))
		{
			Map<String, IRWProperty> theValues = itsValues.get(aDef);
			if (theValues == null) return false;
			
			Object theValue = theValues.remove(aTarget);
			if (theValue == null) return false;
			
			return true;
		}
		else return false;
	}
	
	/**
	 * Adds an option to this option manager, if not already present in parent.
	 */
	public <T> void addOption(OptionDef<T> aDef, String aTarget, T aDefault)
	{
		if (itsParent != null)
		{
			if (itsParent.addOption0(aDef, aTarget, aDefault)) return;
		}
		
		itsOptions.add(aDef);
		addOption0(aDef, aTarget, aDefault);
	}
	
	/**
	 * Removes an option from this manager, or from the parent that contains it.
	 */
	public <T> void removeOption(OptionDef<T> aDef, String aTarget)
	{
		if (itsParent != null)
		{
			if (itsParent.removeOption0(aDef, aTarget)) return;
		}
		
		itsOptions.add(aDef);
		removeOption0(aDef, aTarget);
	}
	
	/**
	 * Retrieves the value of the given option for the given target.
	 */
	public <T> T get(OptionDef<T> aDef, String aTarget)
	{
		IRWProperty<T> theProperty = getProperty(aDef, aTarget);
		return theProperty != null ? theProperty.get() : null;
	}
	
	/**
	 * Retrives the property object that holds the value of the given
	 * option for the given target.
	 */
	public <T> IRWProperty<T> getProperty(OptionDef<T> aDef, String aTarget)
	{
		IRWProperty<T> theProperty = null;
		Map<String, IRWProperty> theValues = itsValues.get(aDef);
		if (theValues != null) theProperty = (IRWProperty<T>) theValues.get(aTarget);
		
		if (theProperty == null && itsParent != null) theProperty = itsParent.getProperty(aDef, aTarget);
		if (theProperty == null)
		{
			String theString = itsGUIManager.getProperty(aDef.getName()+"/"+aTarget);
			if (theString != null)
			{
				T theValue = aDef.unmarshall(theString);
				theProperty = new SimpleRWProperty<T>(null, theValue);
			}
		}
		
		return theProperty;
	}
	
	/**
	 * Returns the option manager for the specified component.
	 */
	public static OptionManager get(Component aComponent)
	{
		GetOptionManager theRequest = new GetOptionManager();
		Bus.getBus(aComponent).postMessage(theRequest);
		return theRequest.getResult();
		
	}
	
	public static abstract class OptionDef<T>
	{
		private String itsName;
		
		public OptionDef(String aName)
		{
			itsName = aName;
		}
		
		public String getName()
		{
			return itsName;
		}

		public abstract String marshall(T aValue);
		public abstract T unmarshall(String aString);
	}
	
	public static class BooleanOptionDef extends OptionDef<Boolean>
	{
		public BooleanOptionDef(String aName)
		{
			super(aName);
		}

		@Override
		public String marshall(Boolean aValue)
		{
			return ""+aValue;
		}

		@Override
		public Boolean unmarshall(String aString)
		{
			return Boolean.parseBoolean(aString);
		}
	}
	
	public static class IntOptionDef extends OptionDef<Integer>
	{
		public IntOptionDef(String aName)
		{
			super(aName);
		}

		@Override
		public String marshall(Integer aValue)
		{
			return ""+aValue;
		}

		@Override
		public Integer unmarshall(String aString)
		{
			return Integer.parseInt(aString);
		}
	}
}
