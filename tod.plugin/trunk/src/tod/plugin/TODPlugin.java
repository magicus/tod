package tod.plugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import reflex.ide.eclipse.launcher.ReflexLauncherPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class TODPlugin extends AbstractUIPlugin
{

	// The shared instance.
	private static TODPlugin plugin;

	/**
	 * Temporarily we support a unique session.
	 */
	private TODSession itsSession;
	
	private String itsLibraryPath;

	
	/**
	 * The constructor.
	 */
	public TODPlugin()
	{
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		
		itsSession = TODSessionManager.getInstance().getCleanSession();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception
	{
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static TODPlugin getDefault()
	{
		return plugin;
	}

	public TODSession getSession()
	{
		return itsSession;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path)
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin("tod.plugin", path);
	}
	
	public String getLibraryPath()
	{
		if (itsLibraryPath == null)
			itsLibraryPath = ReflexLauncherPlugin.getLibraryPath(this);
		
		return itsLibraryPath;
	}
	

}
