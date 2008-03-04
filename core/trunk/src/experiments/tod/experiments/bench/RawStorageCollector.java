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
package tod.experiments.bench;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RawStorageCollector extends ISimpleLogCollector
{
	private DataOutputStream itsOutputStream;
	private File itsFile; 

	public RawStorageCollector()
	{
		try
		{
			itsFile = new File("/home/gpothier/tmp/tod-raw.bin");
			if (itsFile.exists()) itsFile.delete();
			itsOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(itsFile), 100000));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public long getStoredSize()
	{
		try
		{
			itsOutputStream.flush();
			return itsFile.length();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public synchronized void logBehaviorEnter(long aTid, long aSeq, int aBehaviorId, long aTarget, long[] args)
	{
		try
		{
			itsOutputStream.writeByte(EventType.BEHAVIOR_ENTER.ordinal());
			itsOutputStream.writeLong(aTid);
			itsOutputStream.writeLong(aSeq);
			itsOutputStream.writeLong(time());
			itsOutputStream.writeInt(aBehaviorId);
			itsOutputStream.writeLong(aTarget);
			for (int i = 0; i < args.length; i++)
			{
				long arg = args[i];
				itsOutputStream.writeLong(arg);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public synchronized void logBehaviorExit(long aTid, long aSeq, long aRetValue)
	{
		try
		{
			itsOutputStream.writeByte(EventType.BEHAVIOR_EXIT.ordinal());
			itsOutputStream.writeLong(aTid);
			itsOutputStream.writeLong(aSeq);
			itsOutputStream.writeLong(time());
			itsOutputStream.writeLong(aRetValue);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public synchronized void logFieldWrite(long aTid, long aSeq, int aFieldId, long aTarget, long aValue)
	{
		try
		{
			itsOutputStream.writeByte(EventType.FIELD_WRITE.ordinal());
			itsOutputStream.writeLong(aTid);
			itsOutputStream.writeLong(aSeq);
			itsOutputStream.writeLong(time());
			itsOutputStream.writeInt(aFieldId);
			itsOutputStream.writeLong(aTarget);
			itsOutputStream.writeLong(aValue);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public synchronized void logVarWrite(long aTid, long aSeq, int aVarId, long aValue)
	{
		try
		{
			itsOutputStream.writeByte(EventType.VAR_WRITE.ordinal());
			itsOutputStream.writeLong(aTid);
			itsOutputStream.writeLong(aSeq);
			itsOutputStream.writeLong(time());
			itsOutputStream.writeInt(aVarId);
			itsOutputStream.writeLong(aValue);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
