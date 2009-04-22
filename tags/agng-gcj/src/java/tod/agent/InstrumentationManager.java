/*
 * Created on Dec 14, 2007
 */
package tod.agent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import tod.agent.ConnectionManager.InstrumentationResponse;
import tod.tools.parsers.ParseException;
import tod.tools.parsers.workingset.AbstractClassSet;
import tod.tools.parsers.workingset.WorkingSetFactory;
import zz.utils.Utils;

/**
 * cached classes are defined as [cachePath]/[prefix]/[className].[md5].class
 * /[className].[md5].tm
 * 
 * @author omotelet
 * 
 */
public class InstrumentationManager
{
	private final NativeAgentConfig itsConfig;

	private final ConnectionManager itsConnectionManager;

	private AbstractClassSet itsWorkingSet;

	/**
	 * A buffer of instrumented method ids. This is necessary because we can
	 * recieve instrumentation requests before the VMStart event occurs, and we
	 * cannot use {@link VMUtils#callTracedMethods_setTraced} before VMStart.
	 */
	private List<Integer> itsTmpTracedMethods = new ArrayList<Integer>();

	public InstrumentationManager(NativeAgentConfig aConfig, ConnectionManager aConnectionManager)
	{
		itsConfig = aConfig;
		itsConnectionManager = aConnectionManager;

		// Compute class cache prefix
		String theSigSrc = itsConfig.getWorkingSet() + "/" + itsConfig.getStructDbId();
		itsConfig.setClassCachePrefix(Utils.md5String(theSigSrc.getBytes()));
		itsConfig.logf(1, "Class cache prefix: %s", itsConfig.getClassCachePrefix());
	}

	public synchronized byte[] instrument(long aJniEnv, String aName, byte[] aOriginal)
	{
		if (isInScope(aName))
		{
			InstrumentationResponse theResponse;

			String theMD5 = Utils.md5String(aOriginal);
			theResponse = null;
			lookupInCache(aName, theMD5);

			if (theResponse == null)
			{
				theResponse = itsConnectionManager.sendInstrumentationRequest(aName, aOriginal);
				cache(theResponse, aName, theMD5);
			}

			if (theResponse.bytecode != null)
			{
				itsConfig.logf(1, "Redefined class %s (%d bytes, %d traced methods)", aName,
						theResponse.bytecode.length, theResponse.tracedMethods.length);
			}

			registerTracedMethods(aJniEnv, theResponse.tracedMethods);
			return theResponse.bytecode;
		}
		else return null;
	}

	/**
	 * Looks up a pre-instrumented class in the cache.
	 * 
	 * @param aName
	 *            Name of the class
	 * @return An {@link InstrumentationResponse} object if instrumentation data
	 *         for the class is found in the cache. Note that the returned
	 *         object can have a null bytecode array if the class is known to
	 *         not need instrumentation. If no information for the class is
	 *         found in the cache, returns null.
	 */
	private InstrumentationResponse lookupInCache(String aName, String aMD5Sum)
	{
		String thePath = itsConfig.getCachePath() + "/" + itsConfig.getClassCachePrefix() + "/" + aName + "." + aMD5Sum;
		File theClassFile = new File(thePath + ".class");
		if (!theClassFile.exists())
		{
			itsConfig.log(1, "No cache cache files for " + aName);
			return null;
		}
		File theClassFileTM = new File(thePath + ".tm");
		if (!theClassFileTM.exists()) return null;
		int theLength = (int) theClassFile.length();
		byte[] theClassBytes = null;
		int theTMLength = (int) (theClassFileTM.length() / 4);
		int[] theClassTMInt = new int[theTMLength];
		try
		{
			if (theLength != 0)
			{
				theClassBytes = new byte[theLength];
				new DataInputStream(new FileInputStream(theClassFile)).readFully(theClassBytes);
			}
			DataInputStream theDataTM = new DataInputStream(new FileInputStream(theClassFileTM));
			int i = 0;
			while (i < theTMLength)
			{
				theClassTMInt[i] = theDataTM.readInt();
				i++;
			}
		}
		catch (Exception e)
		{
			itsConfig.log(1, "Error while reading cache files of " + aName);
			e.printStackTrace();
			return null;
		}
		itsConfig.log(1, "Found Cache Files for " + aName);
		return new ConnectionManager.InstrumentationResponse(theClassBytes, theClassTMInt);
	}

	/**
	 * Stores instrumentation response in the cache.
	 */
	private void cache(InstrumentationResponse aResponse, String aName, String aMD5Sum)
	{
		String thePath = itsConfig.getCachePath() + "/" + itsConfig.getClassCachePrefix() + "/" + aName + "." + aMD5Sum;
		File theClassFile = new File(thePath + ".class");
		File theClassFileTM = new File(thePath + ".tm");
		try
		{
			theClassFile.getParentFile().mkdirs();
			theClassFile.createNewFile();
			theClassFileTM.createNewFile();
			if (aResponse.bytecode != null) new DataOutputStream(new FileOutputStream(theClassFile))
					.write(aResponse.bytecode);
			DataOutputStream theTMData = new DataOutputStream(new FileOutputStream(theClassFileTM));
			for (int theI = 0; theI < aResponse.tracedMethods.length; theI++)
			{
				theTMData.writeInt(aResponse.tracedMethods[theI]);
			}
			itsConfig.log(1, "Cached Files for " + aName);
		}
		catch (IOException e)
		{
			itsConfig.log(1, "Problem while writing cache files of " + aName);
			e.printStackTrace();
		}
	}

	private void registerTracedMethod(long aJniEnv, int aId)
	{
		VMUtils.callTracedMethods_setTraced(aJniEnv, aId);
	}

	/**
	 * Sends the traced methods buffer.
	 */
	public void flushTmpTracedMethods(long aJniEnv)
	{
		for (Integer theId : itsTmpTracedMethods)
			registerTracedMethod(aJniEnv, theId);
		itsTmpTracedMethods = null;
	}

	private void registerTracedMethods(long aJniEnv, int[] aIds)
	{
		if (aIds.length == 0) return;

		if (TodAgent.isVmStarted())
		{
			itsConfig.logf(1, "Registering %d traced methods", aIds.length);
			for (int theId : aIds)
				registerTracedMethod(aJniEnv, theId);
		}
		else
		{
			itsConfig.logf(1, "Buffering %d traced methods, will register later", aIds.length);
			for (int theId : aIds)
				itsTmpTracedMethods.add(theId);
		}
	}

	/**
	 * Indicates if the specified class is in the instrumentation scope.
	 */
	private boolean isInScope(String aName)
	{
		// Always skip classes used by the instrumentation agent
		String[] theForbiddenPaths = new String[]
		{ "tod/core/", "tod/agent/", "java/lang/", "java/io/", "java/util/ArrayList", "java/util/Arrays",
				"java/util/HashSet", "java/util/IdentityHashMap", "java/util/List", "java/util/Map", "java/util/Set",
				"java/util/StringTokenizer", "java/net/Socket", "java/net/SocketServer",
				"java/util/ConcurrentModificationException","java/security/","java/nio/","java/net", "java/util","sun/" };
		for (int i = 0; i < theForbiddenPaths.length; i++)
			if (aName.startsWith(theForbiddenPaths[i])) return false;

//		if (aName.startsWith("tod/core/") || aName.startsWith("tod/agent/") || aName.startsWith("java/lang/")
//				|| aName.startsWith("java/io/") || aName.startsWith("")) return false;

		try
		{
			AbstractClassSet theClassSet = getWorkingSet();
			boolean isAccept = theClassSet.accept(aName.replace('/', '.'));
			if (isAccept) itsConfig.log(0, "Instrumentation=" + isAccept + " for --" + aName + "--");
			return isAccept;
		}
		catch (Exception e)
		{
			itsConfig.log(0, "Error while parsing the working set: (using default filter) ");
			e.printStackTrace();
		}

		if (itsConfig.getSkipCoreClasses())
		{
			if (aName.startsWith("java/") || aName.startsWith("sun/") || aName.startsWith("javax/")
					|| aName.startsWith("com/sun/")) return false;
		}
		return true;

	}

	public AbstractClassSet getWorkingSet() throws ParseException
	{

		if (itsWorkingSet == null)
		{
			itsConfig.log(0, "creating WorkingSet with config: --" + itsConfig.getWorkingSet() + "--");
			itsWorkingSet = WorkingSetFactory.parseWorkingSet(itsConfig.getWorkingSet());
		}
		return itsWorkingSet;
	}
}