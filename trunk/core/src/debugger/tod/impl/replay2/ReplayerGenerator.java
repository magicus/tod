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
package tod.impl.replay2;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import tod.core.config.TODConfig;
import tod.core.database.browser.LocationUtils;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IStructureDatabase;
import zz.utils.Utils;

public class ReplayerGenerator
{
	private final TODConfig itsConfig;
	private final IStructureDatabase itsDatabase;
	private final ReplayerLoader itsLoader;

	
	/**
	 * The cached replayer classes, indexed by behavior id.
	 */
	private List<Class<InScopeReplayerFrame>> itsReplayers =
		new ArrayList<Class<InScopeReplayerFrame>>();

	public ReplayerGenerator(ReplayerLoader aLoader, TODConfig aConfig, IStructureDatabase aDatabase)
	{
		itsLoader = aLoader;
		itsConfig = aConfig;
		itsDatabase = aDatabase;
	}

	
	public static final String REPLAYER_NAME_PREFIX = "$tod$replayer2$";

	/**
	 * Returns the JVM name of the replayer class for the given method.
	 */
	public static String makeReplayerClassName(String aJvmClassName, String aJvmMethodName, String aDesc)
	{
		String theName = aJvmClassName+"_"+aJvmMethodName+"_"+aDesc;
		StringBuilder theBuilder = new StringBuilder(theName.length());
		for (int i=0;i<theName.length();i++)
		{
			char c = theName.charAt(i);
			switch(c)
			{
			case '/':
			case '(':
			case ')':
			case '<':
			case '>':
			case '[':
			case ';':
				c = '_';
				break;
			}
			theBuilder.append(c);
		}
		return REPLAYER_NAME_PREFIX+theBuilder.toString();
	}
	

	
	/**
	 * Returns the replayer class used to replay the given behavior.
	 */
	public Class<InScopeReplayerFrame> getReplayerClass(int aBehaviorId)
	{
		Class theReplayerClass = Utils.listGet(itsReplayers, aBehaviorId);
		if (theReplayerClass != null) return theReplayerClass;

		// Replayer class for this behavior not found 
		// Create replayers for all the behaviors in the class.
		IBehaviorInfo theBehavior = itsDatabase.getBehavior(aBehaviorId, true);
		IClassInfo theClass = theBehavior.getDeclaringType();

		byte[] theClassBytecode = theClass.getOriginalBytecode();
		ClassNode theClassNode = new ClassNode();
		ClassReader theReader = new ClassReader(theClassBytecode);
		theReader.accept(theClassNode, 0);

		for (MethodNode theMethodNode : (List<MethodNode>) theClassNode.methods)
		{
			// Get info about the method before transforming, as the generator modifies it.
			String theMethodName = theMethodNode.name;
			String theMethodDesc = theMethodNode.desc;
			
			MethodReplayerGenerator theGenerator = new MethodReplayerGenerator(
					itsConfig, 
					itsDatabase, 
					this,
					theClassNode, 
					theMethodNode);
			
			byte[] theReplayerBytecode = theGenerator.generate();
			
			String theReplayerName = makeReplayerClassName(
					theClassNode.name, 
					theMethodName, 
					theMethodDesc).replace('/', '.');
			
			itsLoader.addClass(theReplayerName, theReplayerBytecode);
			
			try
			{
				theReplayerClass = itsLoader.loadClass(theReplayerName).asSubclass(InScopeReplayerFrame.class);
			}
			catch (ClassNotFoundException e)
			{
				throw new RuntimeException(e);
			}
			
			theBehavior = LocationUtils.getBehavior(itsDatabase, theClass, theMethodName, theMethodDesc, false);
			Utils.listSet(itsReplayers, theBehavior.getId(), theReplayerClass);
		}
		
		return Utils.listGet(itsReplayers, aBehaviorId);
	}
	
	public InScopeReplayerFrame createInScopeFrame(int aBehaviorId)
	{
		Class<InScopeReplayerFrame> theClass = getReplayerClass(aBehaviorId);
		try
		{
			return theClass.newInstance();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

	}

	public UnmonitoredReplayerFrame createUnmonitoredFrame()
	{
		return new UnmonitoredReplayerFrame();
	}
	
	public ClassloaderWrapperReplayerFrame createClassloaderWrapperFrame()
	{
		return new ClassloaderWrapperReplayerFrame();
	}
}