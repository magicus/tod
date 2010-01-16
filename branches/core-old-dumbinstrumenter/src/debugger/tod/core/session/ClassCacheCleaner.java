/*
 * Created on Oct 27, 2005
 */
package tod.core.session;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import zz.utils.IProgress;


/**
 * Permits to remove cached instrumented class files for modified classes. 
 * @author gpothier
 */
public class ClassCacheCleaner
{
	private final File[] itsClasspath;
	private Map<File, JarFile> itsJarsMap = new HashMap<File, JarFile>();
	
	private IProgress itsProgress;
	
	public static void deleteUpdatedClasses (
			ISession aSession, 
			String[] aBootClasspath, 
			String[] aClasspath)
	{
		deleteUpdatedClasses(aSession, aBootClasspath, aClasspath, null);
	}
	
	public static void deleteUpdatedClasses (
			ISession aSession, 
			String[] aBootClasspath, 
			String[] aClasspath,
			IProgress aProgress)
	{
		try
		{
			String theCachedClassesPath = aSession.getCachedClassesPath();
			if (theCachedClassesPath == null) return;
			
			File[] theSearchPath = new File[aBootClasspath.length+aClasspath.length];
			int i=0;
			for (int j = 0; j < aBootClasspath.length; j++) theSearchPath[i++] = new File(aBootClasspath[j]);
			for (int j = 0; j < aClasspath.length; j++) theSearchPath[i++] = new File(aClasspath[j]);
			
			File theRoot = new File(theCachedClassesPath);
			new ClassCacheCleaner(theSearchPath, aProgress).deleteUpdatedClasses(theRoot);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	private ClassCacheCleaner(File[] aClasspath, IProgress aProgress)
	{
		itsClasspath = aClasspath;
		itsProgress = aProgress;
	}
	
	private void deleteUpdatedClasses (File aDirectory) throws IOException
	{
		if (itsProgress!= null)
		{
			int theCount = countClasses(aDirectory);
			itsProgress.setTotal(theCount);
		}
		
		deleteUpdatedClasses(aDirectory, "");
	}
	
	private void deleteUpdatedClasses (File aDirectory, String aPrefix) throws IOException
	{
		// Count files
		
		// Delete stale classes
		for (File theFile : aDirectory.listFiles())
		{
			if (itsProgress != null && itsProgress.isCancelled()) return;
			
			if (theFile.isDirectory()) 
			{
				String thePrefix = aPrefix.length() == 0 ?
						theFile.getName()
						: aPrefix+"/"+theFile.getName();
						
				deleteUpdatedClasses(theFile, thePrefix);
			}
			else if (theFile.getName().endsWith(".class"))
			{
				String theFileName = theFile.getName();
				String theClassName = aPrefix+"/"+theFileName.subSequence(0, theFileName.length()-6);
				long theCacheModified = theFile.lastModified();
				long theOriginalModified = getModifiedDate(theClassName);
				
				if (theOriginalModified == -1)
				{
					System.out.println("Class not found in classpath: "+theClassName);
				}
				
				if (theOriginalModified > theCacheModified)
				{
					System.out.println("Deleting cached class: "+theClassName);
					theFile.delete();
				}
				
				if (itsProgress != null) itsProgress.inc();
			}
		}
	}
	
	private int countClasses (File aPath)
	{
		int theCount = 0;
		for (File theFile : aPath.listFiles())
		{
			if (theFile.isDirectory()) 
			{
				theCount += countClasses(theFile);
			}
			else if (theFile.getName().endsWith(".class"))
			{
				theCount++;
			}
		}
		return theCount;
	}
	
	private long getModifiedDate(String aClassName) throws IOException
	{
		for (File theEntry : itsClasspath)
		{
			long theDate = getModifiedDate(aClassName, theEntry);
			if (theDate != -1) return theDate;
		}
		return -1;
	}
	
	private long getModifiedDate(String aClassName, File aEntry) throws IOException
	{
		if (! aEntry.exists()) return -1;
		if (aEntry.isDirectory())
		{
			File theFile = new File(aEntry, aClassName+".class");
			if (theFile.exists()) return theFile.lastModified();
		}
		else if (aEntry.getName().endsWith(".jar"))
		{
			JarFile theJarFile = getJarFile(aEntry);
			if (theJarFile.getEntry(aClassName+".class") != null) return aEntry.lastModified();
		}
		return -1;
	}
	
	private JarFile getJarFile (File aFile) throws IOException
	{
		JarFile theJarFile = itsJarsMap.get(aFile);
		if (theJarFile == null)
		{
			theJarFile = new JarFile(aFile);
			itsJarsMap.put (aFile, theJarFile);
		}
		return theJarFile;
	}

}