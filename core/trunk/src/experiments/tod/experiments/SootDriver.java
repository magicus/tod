/*
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
package tod.experiments;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.ClassProvider;
import soot.ClassSource;
import soot.CoffiClassSource;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.options.Options;
import soot.shimple.Shimple;
import soot.shimple.ShimpleBody;

public class SootDriver
{
	public static void main(String[] args) throws Exception
	{
		Options.v().set_soot_classpath("/home/gpothier/apps/java/jdk1.6.0_01/jre/lib/rt.jar");

		File f = new File("bin/tod/experiments/SootDummyClass.class");
		byte[] theBytecode = new byte[(int) f.length()];
		DataInputStream theStream = new DataInputStream(new FileInputStream(f));
		theStream.readFully(theBytecode);
		
		SourceLocator.v().setClassProviders((List) Arrays.asList(
				new MyClassProvider("tod.experiments.SootDummyClass", theBytecode),
				new PlatformClassProvider(
						"java.lang.Object"/*,
						"java.lang.String",
						"java.lang.Comparable",
						"java.lang.Comparable",
						"java.lang.CharSequence",
						"java.io.Serializable"*/)
				));
		
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_verbose(true);
		Scene.v().setPhantomRefs(true);
		SootClass theClass = Scene.v().loadClassAndSupport("tod.experiments.SootDummyClass");
		System.out.println(theClass);
		List<SootMethod> theMethods = theClass.getMethods();
		System.out.println(theMethods);
		
		for (SootMethod theMethod : theMethods)
		{
			Scene.v().setPhantomRefs(true);
			ShimpleBody theBody = Shimple.v().newBody(theMethod);
			Body theActiveBody = theMethod.retrieveActiveBody();
			theBody.importBodyContentsFrom(theActiveBody);
			theBody.rebuild();
			System.out.println(theBody);
		}
	}
	
	public static class MyClassProvider implements ClassProvider
	{
		private String itsName;
		private byte[] itsBytecode;

		public MyClassProvider(String aName, byte[] aBytecode)
		{
			itsName = aName;
			itsBytecode = aBytecode;
		}

		public ClassSource find(String aClassName)
		{
			if (itsName.equals(aClassName)) 
			{
				return new CoffiClassSource(itsName, new ByteArrayInputStream(itsBytecode));
			}
			else return null;
		}
	}
	
	public static class PlatformClassProvider implements ClassProvider
	{
		private Set<String> itsAllowedClasses;
		
		public PlatformClassProvider(String... aAllowedClasses)
		{
			itsAllowedClasses = new HashSet<String>();
			for (String theClass : aAllowedClasses) itsAllowedClasses.add(theClass);
		}

		public ClassSource find(String aClassName)
		{
			if (! itsAllowedClasses.contains(aClassName)) return null;
			
	        String fileName = aClassName.replace('.', '/') + ".class";
	        SourceLocator.FoundFile file = 
	            SourceLocator.v().lookupInClassPath(fileName);
	        if( file == null ) return null;
	        return new CoffiClassSource(aClassName, file.inputStream());
		}
		
	}
}
