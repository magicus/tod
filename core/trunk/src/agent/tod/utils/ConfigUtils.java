/*
 * Created on Oct 25, 2005
 */
package tod.utils;

public class ConfigUtils
{
	/**
	 * Reads a boolean from system properties.
	 */
	public static boolean readBoolean (String aPropertyName, boolean aDefault)
	{
		String theString = System.getProperty(aPropertyName);
		return theString != null ? Boolean.parseBoolean(theString) : aDefault;
	}
	
	/**
	 * Reads an int from system properties.
	 */
	public static int readInt (String aPropertyName, int aDefault)
	{
		String theString = System.getProperty(aPropertyName);
		return theString != null ? Integer.parseInt(theString) : aDefault;
	}
	
	/**
	 * Reads a string from system properties.
	 */
	public static String readString (String aPropertyName, String aDefault)
	{
		String theString = System.getProperty(aPropertyName);
		System.out.println(aPropertyName+"="+theString);
		return theString != null ? theString : aDefault;
	}
	

}
