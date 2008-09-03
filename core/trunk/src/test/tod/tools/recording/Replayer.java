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
package tod.tools.recording;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.config.TODConfig;
import tod.core.session.ISession;
import tod.core.session.SessionTypeManager;
import zz.utils.Utils;

public class Replayer
{
	private final File itsFile;
	private final ISession itsSession;
	
	
	public Replayer(File aFile, ISession aSession)
	{
		itsFile = aFile;
		itsSession = aSession;
	}
	
	public void process() throws Exception
	{
		Map<Integer, Object> theObjects = new HashMap<Integer, Object>();
		theObjects.put(1, itsSession.getLogBrowser());
		
		List<Record> theRecords = load(itsFile);
		
		long t0 = System.currentTimeMillis();
		
		int i=0;
		int theCount = 0;
		for (Record theRecord : theRecords) 
		{
			try
			{
				theRecord.process(theObjects);
				theCount++;
			}
			catch (Throwable e)
			{
				System.err.println("Exception processing record "+i+": "+theRecord);
				e.printStackTrace();
			}
			i++;
		}
		
		long t1 = System.currentTimeMillis();

		Utils.println(
				"Replayed %d records in %d seconds (%d failures).", 
				theRecords.size(), 
				(t1-t0)/1000,
				theRecords.size()-theCount);
	}
	
	private List<Record> load(File aFile) throws Exception
	{
		List<Record> theRecords = new ArrayList<Record>();
		
		ObjectInputStream theStream = new ObjectInputStream(new FileInputStream(aFile));

		while(true)
		{
			try
			{
				Object theObject = theStream.readObject();
				Record theRecord = (Record) theObject;
				theRecords.add(theRecord);
//				System.out.println((theRecords.size()-1)+": "+theRecord);
			}
			catch (EOFException e)
			{
				break;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		Utils.println("Loaded %d records.", theRecords.size());
		
		return theRecords;
	}
	
	public static void main(String[] args) throws Exception
	{
		try
		{
			URI theUri = URI.create(args[1]);
			TODConfig theConfig = new TODConfig();
			String theScheme = theUri != null ? theUri.getScheme() : null;
			ISession theSession = SessionTypeManager.getInstance().createSession(theScheme, theUri, theConfig);

			Replayer theReplayer = new Replayer(new File(args[0]), theSession);
			theReplayer.process();
		}
		finally
		{
//			Thread.sleep(1000);
//			System.exit(0);
		}
	}
}
	