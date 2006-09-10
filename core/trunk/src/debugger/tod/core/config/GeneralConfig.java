/*
 * Created on Sep 9, 2006
 */
package tod.core.config;

import javax.security.auth.login.Configuration;

import tod.utils.ConfigUtils;

public class GeneralConfig
{
	public static final String PARAM_LOCATIONS_FILE = "locations-file";
	public static String LOCATIONS_FILE;
	
	public static final String PARAM_MASTER_HOST = "master-host";
	public static String MASTER_HOST;
	
	public static final String PARAM_NODE_DATA_DIR = "node-data-dir";
	public static String NODE_DATA_DIR;
	
	public static final String PARAM_STORE_EVENTS_FILE = "events-file";
	public static String STORE_EVENTS_FILE;
	
	
	
	static
	{
		LOCATIONS_FILE = ConfigUtils.readString(PARAM_LOCATIONS_FILE, "~/tmp/tod-locations");
		MASTER_HOST = ConfigUtils.readString(PARAM_MASTER_HOST, "localhost");
		NODE_DATA_DIR = ConfigUtils.readString(PARAM_NODE_DATA_DIR, ".");
		STORE_EVENTS_FILE = ConfigUtils.readString(PARAM_STORE_EVENTS_FILE, "events-raw.bin");


	}

}
