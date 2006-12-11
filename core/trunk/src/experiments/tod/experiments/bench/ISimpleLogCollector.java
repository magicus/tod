/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.experiments.bench;

public abstract class ISimpleLogCollector
{
	public abstract void logFieldWrite(long tid, long seq, int fieldId, long target, long value);
	public abstract void logVarWrite(long tid, long seq, int varId, long value);
	public abstract void logBehaviorEnter(long tid, long seq, int behaviorId, long target, long[] args);
	public abstract void logBehaviorExit(long tid, long seq, long retValue);
	
	public abstract long getStoredSize();
	
	protected long time()
	{
		return 0;
//		return System.currentTimeMillis();
//		return System.nanoTime();
	}


}
